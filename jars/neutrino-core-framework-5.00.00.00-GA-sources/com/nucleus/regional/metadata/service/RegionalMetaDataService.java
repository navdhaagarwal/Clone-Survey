package com.nucleus.regional.metadata.service;

import static com.nucleus.finnone.pro.general.util.ValidatorUtils.hasElements;
import static com.nucleus.finnone.pro.general.util.ValidatorUtils.isNull;
import static org.apache.commons.collections4.MapUtils.isEmpty;
import static org.apache.commons.collections4.MapUtils.isNotEmpty;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.NestedNullException;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.collections.CollectionUtils;
import org.hibernate.proxy.HibernateProxyHelper;
import org.springframework.transaction.annotation.Transactional;

import com.nucleus.core.exceptions.SystemException;
import com.nucleus.logging.BaseLoggers;
import com.nucleus.persistence.EntityDao;
import com.nucleus.regional.RegionalData;
import com.nucleus.regional.RegionalDataConstants;
import com.nucleus.regional.RegionalEnabled;
import com.nucleus.regional.metadata.RegionalMetaData;
import com.nucleus.regional.metadata.RegionalMetaDataProcessingBean;
import com.nucleus.regional.metadata.dao.IRegionalMetaDataDao;

@Named("regionalMetaDataService")
@Transactional(readOnly = true)
public class RegionalMetaDataService implements IRegionalMetaDataService {

    private static final String EXCEPTION_OCCURED_STRING = "Exception while fetching value of regionalField";
    private static final String LOGICAL_NAME_FIELD_NAME_MAP = "LOGICAL_NAME_FIELD_NAME_MAP";
    private static final String FULLY_QUALIFIED_ENTITY_NAME_REGIONAL_METADATA_MAP = "FULLY_QUALIFIED_ENTITY_NAME_REGIONAL_METADATA_MAP";
    private static final Map<String,  Map<String, String>> regionalMetaDataLogicalNameFieldNameMap = new HashMap<String,  Map<String, String>>();
    
    private Map<String, Object> regionalDataMap = null;
    @Inject
    @Named("entityDao")
    private EntityDao entityDao;

    @Inject
    @Named("regionalMetaDataDao")
    private IRegionalMetaDataDao regionalMetaDataDao;
    @Inject
    @Named("regionalMetaDataProcessingBean")
    private RegionalMetaDataProcessingBean regionalMetaDataProcessingBean;

    @Override
	public Map<String, Object> getRegionalMetaData() {
		List<RegionalMetaData> regionalMetaDataList = entityDao
				.findAll(RegionalMetaData.class);

		Map<String, Object> regionalMetaDataFullyQualifiedEntityNameMap = (Map<String, Object>) prepareRegionalMetaDataMap(regionalMetaDataList);
		if (regionalMetaDataFullyQualifiedEntityNameMap != null
				&& regionalMetaDataFullyQualifiedEntityNameMap
						.get(LOGICAL_NAME_FIELD_NAME_MAP) != null
				&& (regionalMetaDataLogicalNameFieldNameMap == null || regionalMetaDataLogicalNameFieldNameMap
						.isEmpty())) {
				regionalMetaDataLogicalNameFieldNameMap
						.putAll((Map<String, Map<String, String>>) regionalMetaDataFullyQualifiedEntityNameMap
								.get(LOGICAL_NAME_FIELD_NAME_MAP));
		}
		return (Map<String, Object>) regionalMetaDataFullyQualifiedEntityNameMap
				.get(FULLY_QUALIFIED_ENTITY_NAME_REGIONAL_METADATA_MAP);

	}

    private Map<String, Object> prepareRegionalMetaDataMap(
			List<RegionalMetaData> regionalMetaDataList) {
		Map<String, Object> regionalMetaDataMap = new HashMap<String, Object>();

		Map<String, Object> regionalMetaDataFullyQualifiedEntityNameMap = new HashMap<String, Object>();
		Map<String, Map<String, String>> regionalMetaDataLogicalNameFieldName = new HashMap<String, Map<String, String>>();

		try {
			if (CollectionUtils.isEmpty(regionalMetaDataList)) {
				return regionalMetaDataMap;
			}
			for (RegionalMetaData regionalMetaData : regionalMetaDataList) {
				if (regionalMetaDataFullyQualifiedEntityNameMap
						.get(regionalMetaData.getFullyQualifiedEntityName()) == null) {
					Map<String, Object> logicalNameRegionalMetaDataMap = new HashMap<String, Object>();
					Map<String, String> fieldNameLogicalNameMap = new HashMap<String, String>();

					regionalMetaDataFullyQualifiedEntityNameMap.put(
							regionalMetaData.getFullyQualifiedEntityName(),
							logicalNameRegionalMetaDataMap);

					regionalMetaDataLogicalNameFieldName.put(
							regionalMetaData.getFullyQualifiedEntityName(),
							fieldNameLogicalNameMap);

				}
				Map<String, Object> logicalNameRegionalMetaDataMap = (Map<String, Object>) regionalMetaDataFullyQualifiedEntityNameMap
						.get(regionalMetaData.getFullyQualifiedEntityName());
				logicalNameRegionalMetaDataMap.put(
						regionalMetaData.getLogicalName(), regionalMetaData);

				Map<String, String> logicalNameFieldNameMap = (Map<String, String>) regionalMetaDataLogicalNameFieldName
						.get(regionalMetaData.getFullyQualifiedEntityName());
				logicalNameFieldNameMap.put(regionalMetaData.getLogicalName(),
						regionalMetaData.getFieldName());

			}

			regionalMetaDataMap.put(FULLY_QUALIFIED_ENTITY_NAME_REGIONAL_METADATA_MAP,
					regionalMetaDataFullyQualifiedEntityNameMap);
			regionalMetaDataMap.put(LOGICAL_NAME_FIELD_NAME_MAP,
					regionalMetaDataLogicalNameFieldName);

		} catch (Exception e) {
			BaseLoggers.exceptionLogger
					.debug("Exception occured at Regional MetaData service : "
							+ e);
		}
		return regionalMetaDataMap;

	}

    @Override
    public Map<String, Object> getRegionalDataAttributesValue(
            RegionalEnabled regionalEnabled) {
        Map<String, Object> logicalNameValueMap = new HashMap<String, Object>();
        if (isNull(regionalEnabled)
                || isNull(regionalEnabled.getRegionalData())) {
            return logicalNameValueMap;
        }

        String sourceEntityName = HibernateProxyHelper
                .getClassWithoutInitializingProxy(regionalEnabled).getName();
        Map<String, String> logicalNameAndFieldNameMap = getLogicalNameAndRegionalFieldMapping(sourceEntityName);

        for (Map.Entry<String, String> entry : logicalNameAndFieldNameMap
                .entrySet()) {
            String logicalName = entry.getKey();
            String regionalFieldName = entry.getValue();
            if (logicalName == null || regionalFieldName == null) {
                continue;
            }
            Object fieldValue = getFieldValueForRegionalFields(
                    regionalEnabled.getRegionalData(), regionalFieldName);
            logicalNameValueMap.put(logicalName, fieldValue);
        }
        return logicalNameValueMap;

    }

    private Object getFieldValueForRegionalFields(RegionalData regionalData,
            String regionalFieldName) {
        Object fieldValue = null;
        try {
            fieldValue = PropertyUtils.getProperty(regionalData,
                    regionalFieldName);
        } catch (NestedNullException exception) {
            BaseLoggers.exceptionLogger.error(EXCEPTION_OCCURED_STRING,
                    exception);
        } catch (IllegalArgumentException exception) {
            BaseLoggers.exceptionLogger.error(EXCEPTION_OCCURED_STRING,
                    exception);
        } catch (IllegalAccessException exception) {
            BaseLoggers.exceptionLogger.error(EXCEPTION_OCCURED_STRING,
                    exception);
        } catch (InvocationTargetException exception) {
            BaseLoggers.exceptionLogger.error(EXCEPTION_OCCURED_STRING,
                    exception);
        } catch (NoSuchMethodException exception) {
            BaseLoggers.exceptionLogger.error(EXCEPTION_OCCURED_STRING,
                    exception);
        } catch (Exception e) {
            throw new SystemException(e);
        }
        return fieldValue;
    }

    @Override
    public void setRegionalDataAttributesValue(
            Map<String, Object> logicalNameValueMap,
            RegionalEnabled regionalEnabled) {
        if (isNull(regionalEnabled) || isEmpty(logicalNameValueMap)) {
            return;
        }

        String sourceEntityName = HibernateProxyHelper
                .getClassWithoutInitializingProxy(regionalEnabled).getName();
        Map<String, String> logicalAndFieldNameMap = getLogicalNameAndRegionalFieldMapping(sourceEntityName);
        if (isNotEmpty(logicalAndFieldNameMap)) {
            RegionalData regionalData = regionalEnabled.getRegionalData();
            if (isNull(regionalData)) {
                regionalData = new RegionalData();
                regionalEnabled.setRegionalData(regionalData);
            }
            for (Map.Entry<String, String> entry : logicalAndFieldNameMap
                    .entrySet()) {
                String logicalName = entry.getKey();
                Object fieldValue = null;
                String fieldName = entry.getValue();
                if (logicalName == null || fieldName == null) {
                    continue;
                }

                if (logicalNameValueMap.containsKey(logicalName)) {
                    fieldValue = logicalNameValueMap.get(logicalName);
                    setLogicalFieldValueInRegionalData(regionalData, fieldName,
                            fieldValue);
                }

            }
        }

    }

    private void setLogicalFieldValueInRegionalData(RegionalData regionalData,
            String fieldName, Object fieldValue) {
        try {
            BeanUtils.setProperty(regionalData, fieldName, fieldValue);
        } catch (IllegalAccessException e) {
            BaseLoggers.exceptionLogger.error(
                    "Exception while setting value of regionalField", e);
        } catch (InvocationTargetException e) {
            BaseLoggers.exceptionLogger.error(
                    "Exception while setting value of regionalField", e);
        }
    }

    @Override
    public Map<String, String> getLogicalNameAndRegionalFieldMapping(
            String sourceEntityName) {
    	if(regionalMetaDataLogicalNameFieldNameMap.get(sourceEntityName)!=null){
         	return (Map<String, String>) regionalMetaDataLogicalNameFieldNameMap.get(sourceEntityName);
        }
    	return new HashMap<String, String>();
    }

    @Override
    public Map<String, Object> getRegionalDataAttributeValue(RegionalData data,
            String sourceEntity) {

        Map<String, Object> returnedMap = new HashMap<String, Object>();

        if (data != null && isNotBlank(sourceEntity)) {
            Map<String, Object> finalMap = new HashMap<String, Object>();

            Map<String, Object> regionDataInnerMap1 = null;

            Map<String, Object> map = getRegionalMetaData();
            if (!map.isEmpty()) {
                regionDataInnerMap1 = (Map<String, Object>) map
                        .get(sourceEntity);
            }

            for (Map.Entry<String, Object> entry : ((Map<String, Object>) regionDataInnerMap1)
                    .entrySet()) {
                String key = null;
                if (entry.getKey() == null || entry.getValue() == null) {
                    continue;
                } else {
                    key = entry.getKey();
                }

                RegionalMetaData regionalMetaData2 = (RegionalMetaData) entry
                        .getValue();
                finalMap.put(regionalMetaData2.getFieldName(), key);
            }

            String fieldName = "";
            Object fieldValue = "";
            try {
                for (int i = 1; i <= RegionalDataConstants.NUMBER_OF_REGIONAL_FIELDS; i++) {
                    fieldName = "regionalField" + i;
                    fieldValue = PropertyUtils.getProperty(data, fieldName);

                    if (fieldValue != null) {
                        List<Object> list = new ArrayList<Object>();
                        list.add(fieldValue);
                        list.add(fieldName);
                        returnedMap.put((String) finalMap.get(fieldName), list);

                    }
                }
            } catch (Exception e) {
                throw new SystemException(e);
            }

            return returnedMap;

        }
        return returnedMap;
    }

    public void setEntityDao(EntityDao entityDao) {
        this.entityDao = entityDao;
    }

    public void setRegionalMetaDataDao(IRegionalMetaDataDao regionalMetaDataDao) {
        this.regionalMetaDataDao = regionalMetaDataDao;
    }

    @Override
    public String getQueryAppenderForRegionalFields(String logicalName,
            RegionalEnabled regionalEnabled) {

        if (isNull(regionalDataMap)) {
            regionalDataMap = regionalMetaDataProcessingBean
                    .getRegionalMetaDataMap();
        }
        String sourceEntityName = HibernateProxyHelper
                .getClassWithoutInitializingProxy(regionalEnabled).getName();
        return regionalMetaDataProcessingBean.getRegionalPathAttribute(
                logicalName, sourceEntityName, regionalDataMap);
    }

    @Override
    public String getRegionalPathBasedOnSourceEntityAndLogicalField(
            String logicalName, String sourceEntityName) {
        StringBuilder regionalPath = new StringBuilder();
        Map<String, String> logicalNameAndFieldNameMap = getLogicalNameAndRegionalFieldMapping(sourceEntityName);
        for (Map.Entry<String, String> entry : logicalNameAndFieldNameMap
                .entrySet()) {
            String logicalNameForRegionalField = entry.getKey();
            String regionalFieldName = entry.getValue();
            if (logicalNameForRegionalField == null
                    || regionalFieldName == null) {
                continue;
            }
            if (logicalNameForRegionalField.equals(logicalName)) {
                regionalPath = regionalPath.append("regionalData.").append(
                        regionalFieldName);
            }
        }
        return regionalPath.toString();
    }

    @Override
    public List<RegionalMetaData> fetchRegionalMetaDataforSourceEntity(
            String sourceEntityName) {
        return regionalMetaDataDao
                .fetchRegionalMetaDataforSourceEntity(sourceEntityName);
    }

    @Override
    public Map<String, RegionalMetaData> getLogicalNameAndRegionalMetaDataMap(
            String sourceEntityName) {
        Map<String, RegionalMetaData> logicalNameRegionalMetaDataMap = new HashMap<String, RegionalMetaData>();
        List<RegionalMetaData> regionalMetaDataList = null;
        if (isNotBlank(sourceEntityName)) {
            regionalMetaDataList = fetchRegionalMetaDataforSourceEntity(sourceEntityName);
        }
        if (hasElements(regionalMetaDataList)) {
            for (RegionalMetaData regionalMetaDataObj : regionalMetaDataList) {
                if (!logicalNameRegionalMetaDataMap
                        .containsKey(regionalMetaDataObj.getLogicalName())) {
                    logicalNameRegionalMetaDataMap.put(
                            regionalMetaDataObj.getLogicalName(),
                            regionalMetaDataObj);
                }
            }
        }
        return logicalNameRegionalMetaDataMap;
    }
}
