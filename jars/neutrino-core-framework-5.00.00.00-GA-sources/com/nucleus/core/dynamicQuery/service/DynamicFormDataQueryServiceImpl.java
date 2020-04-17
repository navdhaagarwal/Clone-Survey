/**
 * This file and a proportion of its content is copyright of Nucleus Software Exports Limited - © 2012. All rights reserved.
 * Any redistribution or reproduction of part or all of the contents in any form is prohibited other than the following:
 * - you cannot print or download to a local hard disk extract contents either part or full for personal/ commercial/
 * academic or any other use
 * - you may not copy the content to individual/ third parties for any type of use, either as compiled or source format
 * without the knowledge and consent of Nucleus Software
 * - You may not, except with our express written permission, distribute or commercially exploit the content. Nor may you
 * transmit it or store it in any other web site or other form of electronic retrieval system.
 */
package com.nucleus.core.dynamicQuery.service;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;
import org.springframework.util.ReflectionUtils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nucleus.core.exceptions.SystemException;
import com.nucleus.core.formsConfiguration.FieldDataType;
import com.nucleus.core.formsConfiguration.UIMetaData;
import com.nucleus.core.genericparameter.entity.GenericParameter;
import com.nucleus.core.money.entity.Money;
import com.nucleus.dao.query.JPAQueryExecutor;
import com.nucleus.entity.Entity;
import com.nucleus.entity.EntityId;
import com.nucleus.master.BaseMasterEntity;
import com.nucleus.money.MoneyService;
import com.nucleus.persistence.EntityDao;
import com.nucleus.service.BaseServiceImpl;

/**
 * @author Nucleus Software Exports Limited
 * 
 */

@Named("dynamicFormDataQueryService")
public class DynamicFormDataQueryServiceImpl extends BaseServiceImpl implements DynamicFormDataQueryService {

    private static final Logger       LOGGER                               = LoggerFactory
                                                                                   .getLogger(DynamicFormDataQueryServiceImpl.class);

    @Inject
    @Named(value = "entityDao")
    EntityDao                         entityDao;

    @Inject
    @Named("moneyService")
    protected MoneyService            moneyService;

    private static final String       FORM_DATA_BY_DATE_QUERY              = "select pfd.dataJsonString from PersistentFormData pfd where pfd.formUri=:formURI and pfd.entityLifeCycleData.creationTimeStamp <= :toDateFilter and pfd.entityLifeCycleData.creationTimeStamp >= :fromDateFilter and pfd.entityLifeCycleData.persistenceStatus = 0 ";

    private static final String       PANEL_KEY_TYPE_QUERY                 = "select pd.panelKey,pd.panelType from FormConfigurationMapping uim join uim.uiMetaData.panelDefinitionList pd where uim.uiMetaData.id = :uiMetaDataId and uim.entityLifeCycleData.persistenceStatus = 0 " +
    		                                                                               " AND uim.masterLifeCycleData.approvalStatus IN (0,3,4,6) AND uim.activeFlag = true ";

    private static final String       FIELD_KEY_TYPE_QUERY                 = "select fd.fieldKey,fd.fieldDataType,fd.id from FormConfigurationMapping uim join uim.uiMetaData.panelDefinitionList pd join pd.fieldDefinitionList fd where uim.uiMetaData.id=:uiMetaDataId  and uim.entityLifeCycleData.persistenceStatus = 0 "+
    																						" AND uim.masterLifeCycleData.approvalStatus IN (0,3,4,6) AND uim.activeFlag = true";

    private static final String       URI_REF_FIELD_RESOLVE_QUERY_TEMPLATE = "select en.%s from %s en where en.id = :entityLocalId and en.entityLifeCycleData.persistenceStatus = 0";

    private static final ObjectMapper OBJECT_MAPPER                        = new ObjectMapper();

    private static final String       NAME                                 = "name";

    @SuppressWarnings("rawtypes")
    @Override
    public List<Map<String, Object>> getFormDataByDate(Long dynamicFormId, DateTime from, DateTime to,
            Set<Long> projectionFields, Long groupById) {

        List<Map<String, Object>> returnMaps = new ArrayList<Map<String, Object>>();

        JPAQueryExecutor<String> jpaQueryExecutor = new JPAQueryExecutor<String>(FORM_DATA_BY_DATE_QUERY);

        jpaQueryExecutor.addParameter("formURI", UIMetaData.class.getName().concat(":").concat(dynamicFormId.toString()));
        jpaQueryExecutor.addParameter("fromDateFilter", from);
        jpaQueryExecutor.addParameter("toDateFilter", to);
        List<String> dataJsonStrings = entityDao.executeQuery(jpaQueryExecutor);

        for (String json : dataJsonStrings) {

            try {
                Map mapObject = OBJECT_MAPPER.readValue(json, new TypeReference<HashMap>() {
                });
                normalizeNestedMapsAndAddToList(mapObject, dynamicFormId, returnMaps, projectionFields, groupById);
                // returnMaps.add(postProcessFieldMap(fieldValueMap, dynamicFormId));
            } catch (Exception e) {
                LOGGER.error("Error while deserializing json string {} to Map", json);
                throw new SystemException("Error while deserializing json string", e);
            }

        }

        return returnMaps;

    }

    @Override
    public List<Map<String, Object>> getFormDataByCriteriaQuery(Long dynamicFormId, String queryString,
            Set<Long> projectionFields, Long groupById) {
        throw new SystemException("operation not yet implemented.");
    }

    // Normalizing panel--->map and list type relationships
    @SuppressWarnings({ "unchecked", "rawtypes" })
    private void normalizeNestedMapsAndAddToList(Map dynamicFormDataMap, Long dynamicFormId,
            List<Map<String, Object>> returnMaps, Set<Long> projectionFields, Long groupById) {

        JPAQueryExecutor<Object[]> jpaQueryExecutor = new JPAQueryExecutor<Object[]>(PANEL_KEY_TYPE_QUERY);
        jpaQueryExecutor.addParameter("uiMetaDataId", dynamicFormId);
        List<Object[]> panelKeyAndTypes = entityDao.executeQuery(jpaQueryExecutor);

        // create panel key--->type map for type lookup
        Map<String, Integer> panelKeyAndType = toKeyAndTypeMap(panelKeyAndTypes);

        Map<String, Object> finalMap = new HashMap<String, Object>();

        for (Object key : dynamicFormDataMap.keySet()) {

            // check if key is panelKey,If yes then get its type
            Integer ifPanelThenType = panelKeyAndType.get(key);
            if (ifPanelThenType != null && (ifPanelThenType == 0 || ifPanelThenType == 2)) {

                // for panel type 0 and 2 we get map of actual fields in panel
                Object val = dynamicFormDataMap.get(key);
                if (val instanceof Map) {
                    finalMap.putAll((Map<? extends String, ? extends Object>) val);
                }
            }

            // check if key is panelKey,If yes then get its type
            if (ifPanelThenType != null && ifPanelThenType == 3) {
                // for panel type 3 we get list of map
                Object val = dynamicFormDataMap.get(key);
                if (val instanceof List) {
                    List<Map> listOfMap = (List<Map>) val;

                    // add only first row to create a single final consolidated map(only get first row from oneToMany)
                    finalMap.putAll(listOfMap.get(0));
                    listOfMap.remove(0);
                }
            }

        }

        // first of all fetch fields for formId
        JPAQueryExecutor<Object[]> jpaQueryExecutor1 = new JPAQueryExecutor<Object[]>(FIELD_KEY_TYPE_QUERY);
        jpaQueryExecutor1.addParameter("uiMetaDataId", dynamicFormId);
        List<Object[]> fieldKeyAndTypes = entityDao.executeQuery(jpaQueryExecutor1);

        // create field key--->type map for type lookup
        Map<String, Object[]> fieldKeyAndType = toKeyAndTypeIDMap(fieldKeyAndTypes);

        // update existing map and add to final list
        postProcessFieldMap(finalMap, dynamicFormId, projectionFields, fieldKeyAndType);
        returnMaps.add(finalMap);

        // now create maps for one to many relationship based on keys in this single consolidated map
        Map<String, Object> templateMap = new HashMap<String, Object>();
        for (String key : finalMap.keySet()) {
            // IMP--->keep repeating group-by column,Not others
            if (groupById != null && groupById.equals(fieldKeyAndType.get(key)[1])) {
                templateMap.put(key, finalMap.get(key));
            } else {
                templateMap.put(key, null);
            }
        }

        for (Object key : dynamicFormDataMap.keySet()) {

            // check if key is panelKey,If yes then get its type
            Integer ifPanelThenType = panelKeyAndType.get(key);
            if (ifPanelThenType != null && ifPanelThenType == 3) {
                // for panel type 3 we get list of map
                Object val = dynamicFormDataMap.get(key);
                if (val instanceof List) {
                    List<Map> listOfMap = (List<Map>) val;

                    for (Map map : listOfMap) {
                        postProcessFieldMap(map, dynamicFormId, projectionFields, fieldKeyAndType);
                        Map<String, Object> mapToAdd = new HashMap<String, Object>(templateMap);
                        mapToAdd.putAll(map);
                        returnMaps.add(mapToAdd);
                    }

                }
            }

        }

    }

    // Post process methods to resolve values in map
    private void postProcessFieldMap(Map<String, Object> fieldValueMap, Long dynamicFormId, Set<Long> projectionFields,
            Map<String, Object[]> fieldKeyAndType) {

        // Update map by resolving values for some specific types
        Iterator<Entry<String, Object>> it = fieldValueMap.entrySet().iterator();
        Map.Entry<String, Object> entry;

        while (it.hasNext()) {
            entry = it.next();

            // first of all remove fields which are not selected by user
            if (!projectionFields.contains(fieldKeyAndType.get(entry.getKey())[1])) {
                it.remove();
                continue;
            }

            // check if key is fieldKey,If yes then get its type
            Integer ifFieldThenType = (Integer) (fieldKeyAndType.get(entry.getKey()))[0];
            Object valueBeforeChange = entry.getValue();
            if (valueBeforeChange != null && ifFieldThenType != null) {
                if (ifFieldThenType == FieldDataType.DATA_TYPE_TEXT_REFERENCE) {
                    // its an uri of master/generic parameter,resolve it to a value
                    String uriRefValue = (String) valueBeforeChange;
                    String actualValue = resolveUriReference(StringUtils.substringBetween(uriRefValue, "{", "}"));
                    entry.setValue(actualValue);
                } else if (ifFieldThenType == FieldDataType.DATA_TYPE_MONEY) {
                    Money money = moneyService.parseMoney((String) valueBeforeChange, null);
                    entry.setValue(money.getNonBaseAmount().getValue());

                } else if (ifFieldThenType == FieldDataType.DATA_TYPE_DATE) {
                    Long inMillis = (Long) valueBeforeChange;
                    entry.setValue(new DateTime(inMillis));
                }
            }
        }

    }

    private String resolveUriReference(String uriRef) {

        if (StringUtils.isNoneBlank(uriRef)) {
            EntityId entityId = EntityId.fromUri(uriRef);
            Class<? extends Entity> entityClass = entityId.getEntityClass();
            Long id = entityId.getLocalId();

            String finalQuery = getFinalQuery(entityClass);
            if (finalQuery != null) {
                JPAQueryExecutor<String> jpaQueryExecutor = new JPAQueryExecutor<String>(finalQuery);

                jpaQueryExecutor.addParameter("entityLocalId", id);
                String actualValue = entityDao.executeQueryForSingleValue(jpaQueryExecutor);
                return actualValue;
            } else if (BaseMasterEntity.class.isAssignableFrom(entityClass)) {
                BaseMasterEntity baseMasterEntity = (BaseMasterEntity) entityDao.find(entityClass, id);
                return baseMasterEntity.getDisplayName();
            } else {
                // fall back mechanism
                return entityClass.getSimpleName().concat("-").concat(id.toString());
            }

        }

        return uriRef;
    }

    private String getFinalQuery(Class<? extends Entity> entityClass) {

        if (GenericParameter.class.isAssignableFrom(entityClass)) {

            return String.format(URI_REF_FIELD_RESOLVE_QUERY_TEMPLATE, NAME, entityClass.getSimpleName());
        } else {
            // there is no field with name "name" in some masters.These masters have fields like cityName,countryName etc.
            // So need to use this approach,try to find field which ends with "name"
            Field field = ReflectionUtils.findField(entityClass, NAME, String.class);
            if (field == null) {
                findFieldByNameSuffix(entityClass, NAME, String.class);
            }
            if (field != null) {
                return String.format(URI_REF_FIELD_RESOLVE_QUERY_TEMPLATE, field.getName(), entityClass.getSimpleName());
            }

        }
        return null;
    }

    // Utility methods
    private Map<String, Integer> toKeyAndTypeMap(List<Object[]> keyAndTypes) {
        Map<String, Integer> map = new HashMap<String, Integer>();

        for (Object[] keyAndType : keyAndTypes) {
            if (keyAndType != null && keyAndType.length > 1 && keyAndType[0] != null && keyAndType[1] != null) {
                map.put((String) keyAndType[0], (Integer) keyAndType[1]);
            }
        }

        return map;
    }

    // key--->type,id
    private Map<String, Object[]> toKeyAndTypeIDMap(List<Object[]> keyTypeAndIds) {
        Map<String, Object[]> map = new HashMap<String, Object[]>();

        for (Object[] keyAndType : keyTypeAndIds) {
            if (keyAndType != null && keyAndType.length > 2 && keyAndType[0] != null && keyAndType[1] != null
                    && keyAndType[2] != null) {
                map.put((String) keyAndType[0], new Object[] { keyAndType[1], keyAndType[2] });
            }
        }

        return map;
    }

    private Field findFieldByNameSuffix(Class<?> clazz, String nameSuffix, Class<?> type) {
        Assert.notNull(clazz, "Class must not be null");
        Assert.isTrue(nameSuffix != null || type != null, "Either name or type of the field must be specified");
        Class<?> searchType = clazz;
        while (!Object.class.equals(searchType) && searchType != null) {
            Field[] fields = searchType.getDeclaredFields();
            for (Field field : fields) {
                if ((nameSuffix == null || field.getName().toLowerCase().endsWith(nameSuffix.toLowerCase()))
                        && (type == null || type.equals(field.getType()))) {
                    return field;
                }
            }
            searchType = searchType.getSuperclass();
        }
        return null;
    }

}
