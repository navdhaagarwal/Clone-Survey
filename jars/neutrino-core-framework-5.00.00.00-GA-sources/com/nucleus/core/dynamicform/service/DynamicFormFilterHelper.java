package com.nucleus.core.dynamicform.service;

import static com.nucleus.finnone.pro.general.util.ValidatorUtils.notNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Named;

import com.nucleus.core.dynamicform.exception.InvalidDynamicFormDataException;
import com.nucleus.core.dynamicform.vo.DynamicFormDataVO;
import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.hibernate.proxy.HibernateProxyHelper;

import com.nucleus.core.dynamicform.dao.FormDefinitionDao;
import com.nucleus.core.dynamicform.entities.DynamicFormFilter;
import com.nucleus.core.dynamicform.entities.FieldFilterMapping;
import com.nucleus.core.dynamicform.entities.PlaceholderFilterMapping;
import com.nucleus.core.dynamicform.entities.ServicePlaceholderFilterMapping;
import com.nucleus.core.dynamicform.entities.ServicePlaceholderMapping;
import com.nucleus.core.formDefinition.FormDefinitionUtility;
import com.nucleus.core.formsConfiguration.DynamicForm;
import com.nucleus.core.formsConfiguration.DynamicFormScreenMappingDetail;
import com.nucleus.core.formsConfiguration.FieldMetaData;
import com.nucleus.core.formsConfiguration.FormVO;
import com.nucleus.core.formsConfiguration.IDynamicForm;
import com.nucleus.core.formsConfiguration.ModelMetaData;
import com.nucleus.core.formsConfiguration.MultipleForm;
import com.nucleus.core.formsConfiguration.MultipleFormData;
import com.nucleus.core.formsConfiguration.ScreenId;
import com.nucleus.core.formsConfiguration.SingleDynamicForm;
import com.nucleus.core.formsConfiguration.UIMetaData;
import com.nucleus.core.genericparameter.service.GenericParameterService;
import com.nucleus.core.initialization.ProductInformationLoader;
import com.nucleus.finnone.pro.general.util.ValidatorUtils;
import com.nucleus.logging.BaseLoggers;
import com.nucleus.rules.model.SourceProduct;

/**
 * @author gajendra.jatav
 */
@Named("dynamicFormFilterHelper")
public class DynamicFormFilterHelper {

    @Inject
    @Named("formDefinitionDao")
    private FormDefinitionDao formDefinitionDao;

    @Inject
    @Named("formDefinitionUtility")
    private FormDefinitionUtility formDefinitionUtility;

    @Inject
    @Named("genericParameterService")
    private GenericParameterService genericParameterService;

    @Inject
    @Named("dynamicFormFilterDao")
    private IDynamicFormFilterDao dynamicFormFilterDao;

    public void getFieldWiseJsonDataMap(Map<String, String> fieldWiseJsonData,
                                        Map<String, Map<String, Object>> dynamicFormDataMap, List<FieldFilterMapping> fieldFilterMappings) {
        Map<String, JSONObject> filterDefCache = new HashMap<String, JSONObject>();
        for (FieldFilterMapping fieldFilterMapping : fieldFilterMappings) {
            String jsonData = null;
            try {
                jsonData = formDefinitionUtility.getFilteredJsonData(
                        fieldFilterMapping.getServiceFieldName().getFieldName(), dynamicFormDataMap,
                        fieldFilterMapping.getDynamicFormFilter(), filterDefCache);
                fieldWiseJsonData.put(fieldFilterMapping.getServiceFieldName().getFieldName(), jsonData);
            } catch (JSONException e) {
                BaseLoggers.exceptionLogger.debug("exception while getFieldWiseJsonDataMap", e);
            }

        }

    }

    public void removeExtraMappings(List<FieldFilterMapping> fieldFilterMappings, String[] fields) {
        if (fields != null && fields.length != 0) {
            Set<String> set = new HashSet<String>();
            Collections.addAll(set, fields);
            Iterator<FieldFilterMapping> iterator = fieldFilterMappings.iterator();
            while (iterator.hasNext()) {
                FieldFilterMapping fieldFilterMapping = iterator.next();
                if (!set.contains(fieldFilterMapping.getServiceFieldName().getFieldName())) {
                    iterator.remove();
                }
            }
        }

    }

    public Map<String, Object> prepareMapFromData(List<DynamicFormDataVO> dynamicFormDataList, boolean isFlatJson) throws JSONException {

        Map<String, Object> mergedJsonObject = new HashMap<String, Object>();
        for (DynamicFormDataVO formData : dynamicFormDataList) {
            // Filter Data
            if(isFlatJson){
                parseDataAndUpdateJsonObject(mergedJsonObject, formData);
            }else {
                for (Map.Entry<String, Object> entry : formData.entrySet()) {
                    parseDataAndUpdateJsonObject(mergedJsonObject, (Map<String, Object>) entry.getValue());
                }
            }

        }

        return mergedJsonObject;
    }

    private void parseDataAndUpdateJsonObject(Map<String, Object> mergedJsonObject, Map<String, Object> map)
            throws JSONException {


        Set<String> panelKeys = null;
        Set<String> tableKeys = null;
        if (map.containsKey(DynamicFormFilter.PANEL_KEY_LIST)) {
            List<String> panelKeysList = (List<String>) map.get(DynamicFormFilter.PANEL_KEY_LIST);
            panelKeys = new HashSet<String>();
            panelKeys.addAll(panelKeysList);
        }
        if (map.containsKey(DynamicFormFilter.TABLE_KEY_LIST)) {
            List<String> tableKeysList = (List<String>) map.get(DynamicFormFilter.TABLE_KEY_LIST);
            tableKeys = new HashSet<String>();
            tableKeys.addAll(tableKeysList);

        }
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            addFieldData(key, value, mergedJsonObject, panelKeys, tableKeys);
        }

    }

    private void addFieldData(String key, Object fieldData, Map<String, Object> mergedJsonObject, Set<String> panelKeys,
                              Set<String> tableKeys) {

        if (panelKeys != null && panelKeys.contains(key)) {
            addFieldDataFromContainer(fieldData, mergedJsonObject);
            List<String> panelKeysList = (List<String>) mergedJsonObject.get(DynamicFormFilter.PANEL_KEY_LIST);
            if (panelKeysList == null) {
                panelKeysList = new ArrayList<String>();
                mergedJsonObject.put(DynamicFormFilter.PANEL_KEY_LIST, panelKeysList);
            }
            panelKeysList.add(key);
            return;
        }
        if (tableKeys != null && tableKeys.contains(key)) {

            List<String> tableKeysList = (List<String>) mergedJsonObject.get(DynamicFormFilter.PANEL_KEY_LIST);
            if (tableKeysList == null) {
                tableKeysList = new ArrayList<String>();
                mergedJsonObject.put(DynamicFormFilter.TABLE_KEY_LIST, tableKeysList);
            }
            tableKeysList.add(key);
        }
        mergedJsonObject.put(key, fieldData);
    }

    private void addFieldDataFromContainer(Object fieldData, Map<String, Object> mergedJsonObject) {

        Map<String, Object> fieldJsonData = ((Map<String, Object>) fieldData);
        //Iterator<Entry<String, Object>> panelFieldsKeys = fieldJsonData.entrySet().iterator();
        for (Map.Entry<String, Object> entry : fieldJsonData.entrySet()) {
            String key = entry.getKey();
            mergedJsonObject.put(key, entry.getValue());
        }
    }

    private Object filterAndAddMapFields(String formName, Object fieldData, Map<String, Object> filterData,
                                         Map<String, String> fieldKeyMap) throws JSONException {

        Map<String, Object> fieldDataJson = (Map<String, Object>) fieldData;
        Map<String, Object> jsonFieldMap = new HashMap<String, Object>();
        for (Map.Entry<String, Object> entry : fieldDataJson.entrySet()) {
            String fKey = entry.getKey();

            if (fieldKeyMap.containsKey(fKey)) {
                jsonFieldMap.put(fKey, fieldDataJson.get(fKey));
            }
        }
        return jsonFieldMap;
    }

    public void prepareMetaDataForDynamicFormUpdate(String serviceIdentifierCode, String[] placeHolderCode,
                                                    ServicePlaceholderFilterMapping servicePlaceholderFilterMapping,
                                                    Map<String, List<String>> entityPlaceholderMap, Map<String, Set<String>> placeholderFilterMap,
                                                    Map<String, Map<UIMetaData, ModelMetaData>> placeholderUiMetaDataMap) throws JSONException {

        if (ValidatorUtils.notNull(servicePlaceholderFilterMapping)
                && !servicePlaceholderFilterMapping.isActiveFlag()) {
            return;
        }
        Set<String> placeholderCodes = new HashSet<String>();
        boolean selectivePlaceholders = false;
        if (placeHolderCode.length > 0) {
            Collections.addAll(placeholderCodes, placeHolderCode);
            selectivePlaceholders = true;
        }

        if (ValidatorUtils.notNull(servicePlaceholderFilterMapping)
                && ValidatorUtils.hasElements((servicePlaceholderFilterMapping.getPlaceholderFilterMappings()))) {
            for (PlaceholderFilterMapping placeholderFilterMapping : servicePlaceholderFilterMapping
                    .getPlaceholderFilterMappings()) {
                String placeholderCode = placeholderFilterMapping.getScreenId().getScreenCode();
                if (selectivePlaceholders && placeholderCodes.contains(placeholderCode)) {
                    updatePlaceholderFilterMap(placeholderCode, placeholderFilterMapping.getScreenId().getId(),
                            placeholderFilterMapping.getDynamicFormFilter(), placeholderFilterMap,
                            placeholderUiMetaDataMap);

                    addPlaceholderToEntityMap(placeholderFilterMapping.getScreenId().getEntityClass(), placeholderCode,
                            entityPlaceholderMap);
                } else if (!selectivePlaceholders) {
                    updatePlaceholderFilterMap(placeholderCode, placeholderFilterMapping.getScreenId().getId(),
                            placeholderFilterMapping.getDynamicFormFilter(), placeholderFilterMap,
                            placeholderUiMetaDataMap);
                    addPlaceholderToEntityMap(placeholderFilterMapping.getScreenId().getEntityClass(), placeholderCode,
                            entityPlaceholderMap);
                }

            }

        }
        if (ValidatorUtils.hasNoEntry(placeholderFilterMap)) {
            fetchAndUpdatePlaceholderMappings(serviceIdentifierCode, placeholderFilterMap, selectivePlaceholders,
                    placeholderCodes, entityPlaceholderMap, placeholderUiMetaDataMap);
        }
        return;
    }

    private void updatePlaceholderFilterMap(String placeholderCode, Long screenId, DynamicFormFilter dynamicFormFilter,
                                            Map<String, Set<String>> placeholderFilterMap,
                                            Map<String, Map<UIMetaData, ModelMetaData>> placeholderUiMetaDataMap) throws JSONException {

        Set<String> allowedKeys = null;
        if (dynamicFormFilter != null) {
            String fields = dynamicFormFilter.getFilterFieldsJsonMap();
            JSONObject jsonObject = new JSONObject(fields);
            Iterator<String> formsKeys = jsonObject.keys();
            allowedKeys = new HashSet<String>();
            while (formsKeys.hasNext()) {
                String key = formsKeys.next();
                JSONObject formMetaData = jsonObject.getJSONObject(key);
                JSONArray jsonArray = formMetaData.getJSONArray(DynamicFormFilter.FORM_FIELDS);
                for (int i = 0; i < jsonArray.length(); i++) {
                    allowedKeys.add(jsonArray.getString(i));
                }

            }

        }

        List<DynamicFormScreenMappingDetail> formScreenMappingDetails = formDefinitionUtility
                .fetchDynamicFormsMappedToScreenIdAndSourceProduct(screenId, getSourceProduct().getId(), null);
        Map<UIMetaData, ModelMetaData> map = new HashMap<UIMetaData, ModelMetaData>();
        for (DynamicFormScreenMappingDetail formScreenMappingDetail : formScreenMappingDetails) {
            UIMetaData uiMetaData = formScreenMappingDetail.getFormConfigurationMapping().getUiMetaData();
            ModelMetaData modelMetaData = formScreenMappingDetail.getFormConfigurationMapping().getModelMetaData();
            map.put(uiMetaData, modelMetaData);

        }
        placeholderUiMetaDataMap.put(placeholderCode, map);
        placeholderFilterMap.put(placeholderCode, allowedKeys);
    }

    private void fetchAndUpdatePlaceholderMappings(String serviceIdentifierCode,
                                                   Map<String, Set<String>> placeholderFilterMap, boolean selectivePlaceholders, Set<String> placeholderCodes,
                                                   Map<String, List<String>> entityPlaceholderMap,
                                                   Map<String, Map<UIMetaData, ModelMetaData>> placeholderUiMetaDataMap) throws JSONException {

        List<ScreenId> screenIds = dynamicFormFilterDao
                .getServicePlaceholderMappingByServiceCode(serviceIdentifierCode);
        if (ValidatorUtils.hasNoElements(screenIds)) {
            return;
        }


        if (selectivePlaceholders) {
            Map<String, ScreenId> screenIdMap = new HashMap<String, ScreenId>();
            for (ScreenId screenId : screenIds) {
                screenIdMap.put(screenId.getScreenCode(), screenId);
            }
            for (String placeholder : placeholderCodes) {
                if (screenIdMap.containsKey(placeholder)) {
                    updatePlaceholderFilterMap(placeholder, screenIdMap.get(placeholder).getId(), null,
                            placeholderFilterMap, placeholderUiMetaDataMap);
                    ScreenId screenId = screenIdMap.get(placeholder);
                    addPlaceholderToEntityMap(screenId.getEntityClass(), placeholder, entityPlaceholderMap);
                }

            }

        } else {
            for (ScreenId screenId : screenIds) {
                String placeholder = screenId.getScreenCode();
                updatePlaceholderFilterMap(placeholder, screenId.getId(), null, placeholderFilterMap,
                        placeholderUiMetaDataMap);
                addPlaceholderToEntityMap(screenId.getEntityClass(), placeholder, entityPlaceholderMap);
            }

        }

    }

    private void addPlaceholderToEntityMap(String entity, String placeholderCode,
                                           Map<String, List<String>> entityPlaceholderMap) {

        List<String> entityPlaceholderList = entityPlaceholderMap.get(entity);
        if (entityPlaceholderList == null) {
            entityPlaceholderList = new ArrayList<String>();
            entityPlaceholderMap.put(entity, entityPlaceholderList);
        }
        entityPlaceholderList.add(placeholderCode);

    }

    public SourceProduct getSourceProduct() {
        SourceProduct sourceProduct = null;
        FormVO formVO = new FormVO();
        if (notNull(ProductInformationLoader.getProductCode())) {
            sourceProduct = genericParameterService.findByCode(ProductInformationLoader.getProductCode(),
                    SourceProduct.class);
        }

        return sourceProduct;

    }

    void updateDynamicForms(Map<String, Object> mergedFormData, SingleDynamicForm dynamicForm) {

		                /*
		* List<UIMetaData>
		* uiMetaDataList=formDefinitionUtility.fetchUiMetaDataMappedToScreenId(
		* screenId, sourceProductId); for(UIMetaData uiMetaData:
		* uiMetaDataList) {
		* formDefinitionUtility.updateDynamicForms(dynamicFormJsonString,
		* multipleFormObj);
		* 
		 * }
		* 
		 * formDefinitionUtility.mergeAndFilterSingleDynamicFormObj(
		* dynamicFormObj,modelMetaData, dynamicFormValue,allowedKeysPanMap);
		*/
    }

    void updateDynamicForms(Map<String, Object> mergedFormData, MultipleForm dynamicForm) {
        // TODO Auto-generated method stub

    }

    void updateDynamicForms(Map<String, Object> mergedFormData, MultipleFormData dynamicForm) {
        // TODO Auto-generated method stub

    }

    public void updateDynamicFormObject(IDynamicForm dynamicForm, Map<String, List<String>> entityPlaceholderMap,
                                        Map<String, Set<String>> placeholderFilterMap,
                                        Map<String, Map<UIMetaData, ModelMetaData>> placeholderUiMetaDataMap, Map<String, Object> mergedFormData,boolean doValidate) {
        String entityClass = HibernateProxyHelper.getClassWithoutInitializingProxy(dynamicForm).getName();
        if (!entityPlaceholderMap.containsKey(entityClass)) {
            BaseLoggers.flowLogger.info(entityClass + " is not mapped with provided placeholders");
            DynamicFormValidator.throwValidationException(InvalidDynamicFormDataException.ERROR_CODE.INVALID_MASTER_MAINTENANCE,entityClass + " is not mapped with provided placeholders",doValidate,"");
            return;
        }

        if (SingleDynamicForm.class.isAssignableFrom(dynamicForm.getClass())) {
            List<String> placeholders = entityPlaceholderMap.get(entityClass);
            if (ValidatorUtils.hasNoElements(placeholders)) {
                return;
            }
            SingleDynamicForm singleDynamicForm = (SingleDynamicForm) dynamicForm;
            DynamicForm existingDynamicForm = singleDynamicForm.getDynamicForm();
            if (existingDynamicForm == null) {
                existingDynamicForm = new DynamicForm();
                singleDynamicForm.setDynamicForm(existingDynamicForm);
            }
            Map<UIMetaData, ModelMetaData> uiMetaDataMap = placeholderUiMetaDataMap.get(placeholders.get(0));
            if (uiMetaDataMap == null) {
                return;
            }
            Set<String> allowedKeys = placeholderFilterMap.get(placeholders.get(0));
            for (Map.Entry<UIMetaData, ModelMetaData> entry : uiMetaDataMap.entrySet()) {
                formDefinitionUtility.mergeDataAndUpdateDynamicForm(existingDynamicForm, placeholders.get(0), entry.getKey(), entry.getValue(), allowedKeys, mergedFormData,doValidate);
                break;
            }


        } else if (MultipleForm.class.isAssignableFrom(dynamicForm.getClass())) {


            MultipleForm multipleForm = (MultipleForm) dynamicForm;
            List<String> placeholders = entityPlaceholderMap.get(entityClass);
            if (ValidatorUtils.hasNoElements(placeholders)) {
                return;
            }
            Map<String, DynamicForm> existingDynamicForms = multipleForm.getAllDynamicFormAsMap();
            if (existingDynamicForms == null) {
                existingDynamicForms = new HashMap<String, DynamicForm>();
            }
            for (String placeholder : placeholders) {

                Map<UIMetaData, ModelMetaData> uiMetaDataMap = placeholderUiMetaDataMap.get(placeholder);
                Set<String> allowedKeys = placeholderFilterMap.get(placeholders.get(0));
                for (Map.Entry<UIMetaData, ModelMetaData> entry : uiMetaDataMap.entrySet()) {
                    UIMetaData uiMetaData = entry.getKey();
                    DynamicForm existingDynamicForm = existingDynamicForms.get(uiMetaData.getFormName());
                    if (existingDynamicForm == null) {
                        existingDynamicForm = new DynamicForm();
                        multipleForm.addMultiDynamicForm(existingDynamicForm);
                    }
                    formDefinitionUtility.mergeDataAndUpdateDynamicForm(existingDynamicForm, placeholder, entry.getKey(), entry.getValue(), allowedKeys, mergedFormData,doValidate);

                }
            }
		                
		                  /* 
		              multipleForm.get
		              dynamicFormFilterHelper.updateDynamicForms(mergedFormData,
		              (MultipleForm) dynamicForm); */
        }
		            /*else if
		            * (MultipleFormData.class.isAssignableFrom(dynamicForm.getClass()))
		            * { dynamicFormFilterHelper.updateDynamicForms(mergedFormData,
		            * (MultipleFormData) dynamicForm); }
		            */

    }

}
