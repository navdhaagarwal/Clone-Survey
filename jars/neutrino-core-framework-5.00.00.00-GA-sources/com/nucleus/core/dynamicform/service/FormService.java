package com.nucleus.core.dynamicform.service;

import java.util.List;
import java.util.Map;

import com.nucleus.core.dynamicform.entities.DynamicFormFilter;
import com.nucleus.core.formsConfiguration.*;
import com.nucleus.core.formsConfiguration.fieldcomponent.LOVFieldVO;
import com.nucleus.entity.BaseEntity;
import com.nucleus.entity.Entity;
import com.nucleus.entity.EntityId;
import com.nucleus.rules.model.SourceProduct;
import com.nucleus.service.BaseService;
import com.nucleus.user.UserInfo;

/**
 * 
 * @author Nucleus Software Exports Limited
 * Dynamic Form Service
 */

public interface FormService extends BaseService {

    /**
     * 
     * Load VO Object to render
     * @param formName
     * @param formKey
     * @param uri
     * @return
     */
	public Map<String, Object> loadPersistentDataMap(String uri, String formName, PersistentFormData persistentFormData);

    /**
     * 
     * Persist form data
     * @param taskId
     * @param uiMetaDataVo
     */

    public void persistFormData(String uri, Map<String, Object> dataMap, UIMetaDataVo uiMetaDataVo);

    /**
     * 
     * Create new object and persist
     * @param object
     */

    public void saveNewObject(Entity object);

    /**
     * 
     * Load Model by model name
     * @param modelName
     * @return
     */

    public ModelMetaData getModelByModelName(String modelName);

    /**
     * 
     * Load FormComponentType
     * @return
     */
    public List<FormComponentType> getFormComponentType();

    /**
     * 
     * Load reference
     * @param entityId
     * @return
     */
    public BaseEntity loadReference(EntityId entityId);

    /**
     * 
     * Load Transient Map
     * @param object
     * If ruleExecution is true, 
     *      then load object corresponding to Rule Execution
     * If false, 
     *      then load objects according to Display purpose
     *  
     * @return
     */
    public Object loadTransientMap(Object object, boolean ruleExecution);

    Object loadLOVFieldValueOrReference(LOVFieldVO lovFieldVO);

    /**
     * 
     * Method to load Unique forms based on UUID
     * @return
     */
    public List<UIMetaData> loadUniqueLatestForms();

    /**
     * 
     * Get latest form based on form uuid
     * @return
     */
    public UIMetaData getLatestFormByFormUuid(String formuuid);

    /**
     * Get List of Dynamic Form Names with unique uuid
     * @return
     */
    public List<Map<String, Object>> getFormsNamesGroupByuuid();

    /**
     * Loads form name on the basis of given form uuid 
     * @param formuuid
     * @return
     */
    public String getFormNameByuuid(String formuuid);

    public List<UIMetaData> getFormByName(String formName);

    public List<UIMetaData> getFormByNameAndVersion(String formName, String formVersion);

    /**
     * Generates the list of Uimetadata from offline template
     * @param formuuid
     * @return
     */
    public List<UIMetaDataVo> getUIMetaDataVoList(String jsonString);

    /**
     * 
     * Method to populate the Map that can be serialized
     * @param uiMetaDataVo
     * @param persistentFormData
     * @return
     */

    public Map<String, Object> getJsonMapToSave(UIMetaDataVo uiMetaDataVo);
    public UIMetaData getFormByModelUri(String modelUri);

    public Map<String, Object> loadDynamicFormDataMap(DynamicForm dynamicFormObj);
    
    public UserInfo getCurrentUser();
    public String getUserPreferredDateFormat();
    public String getSystemDateFormat();
    public void saveDynamicFormData(String uri,UIMetaDataVo uiMetaDataVo);
    
    public void saveOrUpdateDynamicFormDataForSingleDynamicForm(
			Map<String, Object> dynamicFormDataMap,
			ModelMetaData modelMetaDataObj, UIMetaData uiMetaDataObj,
			Object object,Long screenId);
    
    public void saveOrUpdateDynamicFormDataInMultipleForm(
			Map<String, Object> dynamicFormDataMap,ModelMetaData modelMetaDataObj, UIMetaData uiMetaDataObj,Object object,Long screenId
			);
    
    public void saveOrUpdateDynamicFormInObject(UIMetaDataVo uiMetaDataVo, Object object);
    
    
    public DynamicFormFilter getFilterByServiceIdentifier(Long id);
    
    
    public UIMetaData getUiMetaDataById(Long id);
    
    List<FormConfigurationMapping> loadUniqueDynamicForms();
    
    Boolean checkIfScreenIdIsSingleDynamicFormEnabled(Long screenId);
    List<FormConfigurationMapping> fetchSelectedFormConfigurations(List<Long> formConfigIds);
    DynamicFormScreenMapping fetchDynamicFormScreenMappingById(Long dynamicFormScreenMappingId);
    List<ScreenId> fetchPlaceHolderIdsMappedToSourceProduct(Long sourceProductId);
    List<FormConfigurationMapping> loadUniqueDynamicFormsForSourceProduct(Long sourceProductValue);
    ScreenId fetchScreenIdBasedOnId(Long screenIdValue);
   SourceProduct fetchSourceProductBasedOnId(Long sourceProductValue);
   List<UIMetaData> getFormByNameAndSourceProduct(String formName,Long sourceProductValue);
   Long getScreenIdbyScreenCode(String screenCode);
   List<UIMetaData> loadUniqueLatestFormsBasedOnSourceProduct(Long sourceProductValue);
   List<UIMetaData> getUniqueFormsBySourceProductAndPersistantStatus(Long sourceProductValue);

   DynamicForm prepareUIMetaDataVoFromUiMetaDataJSON(UIMetaDataVo uiMetaDataVo);

   public List<Map<String, ?>> fetchPlaceHolderListMappedToSourceProduct(String value, Long sourceProductIdValue, int page);
   
   List<SpecialTable> getSpecialTableMetaData();

   SpecialTable getSpecialTable(String key);

    List<ProductSchemeMetaData> getProductSchemeMetaDataColumn(String formComponentType);

    List<ProductSchemeMetaData> getProductSchemeMetaDataColumnForAssignmentMatrix(String formComponentType);

    ProductSchemeMetaData getProductSchemeMetaData(String key,String formComponentType);

    List<Object> getDataToBePopulatedForApplication(ProductSchemeMetaData hqlQuery, String dynamicFormData, String formComponentType);

    List<Object> getDataToBePopulatedForApplication(ProductSchemeMetaData hqlQuery, String dynamicFormData, String formComponentType,String assignmentMasterCode);

    void savePanelData(Map<String, Object> dataMap, String uri, UIMetaDataVo uiMetaDataVo, String panelId);
    
    public Map<String,Object> getJsonMapByUIMetadata(UIMetaData uiMetaData,Map<String, Object> dynamicFieldValueMap);

}
