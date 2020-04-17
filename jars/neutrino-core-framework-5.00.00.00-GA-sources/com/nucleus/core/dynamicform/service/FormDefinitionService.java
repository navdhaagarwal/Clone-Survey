package com.nucleus.core.dynamicform.service;

import java.util.List;
import java.util.Map;

import com.nucleus.core.formsConfiguration.FieldDataType;
import com.nucleus.core.formsConfiguration.FormConfigEntityData;
import com.nucleus.core.formsConfiguration.FormConfigurationMapping;
import com.nucleus.core.formsConfiguration.FormVO;
import com.nucleus.core.formsConfiguration.ModelMetaData;
import com.nucleus.core.formsConfiguration.UIMetaData;
import com.nucleus.entity.Entity;
import com.nucleus.finnone.pro.lov.LovConfig;
import com.nucleus.service.BaseService;
import com.nucleus.user.User;

public interface FormDefinitionService extends BaseService {

    public List<FormConfigEntityData> getEntityNameList();

    /**
     * 
     * get field data types
     * 
     * @return
     */

    public List<FieldDataType> getDynamicFormFieldDataType();

    /**
     * 
     * Method to load Form Config Data based on entity name
     * @param entityName
     * @return
     */

    public FormConfigEntityData getFormConfigData(String entityName);
    
    public FormConfigEntityData getFormConfigData(String entityName,  Long parentId);

    /**
     * 
     * Method to save Model Meta Data
     * @param formVO
     * @param user 
     */

    public ModelMetaData saveModelMetaData(FormVO formVO, User user);

    /**
     * 
     * Method to save UI Meta Data
     * @param formVO
     * @param user 
     */

    public UIMetaData saveUIMetaData(FormVO formVO, String modelUri, User user);

    /**
     * 
     * Method to save form mapping
     * @param formVO
     * @param user 
     */

    public FormConfigurationMapping saveFormConfigMapping(ModelMetaData modelMetaData, UIMetaData uiMetaData, FormVO formVO, User user);

    /**
     * 
     * Validate form name 
     * @param formName
     * @return
     */
    public DynamicFormNameStatus validateFormNameAndSourceProduct(FormVO formVo);

    
    
    
    
    public boolean validateFilterNameAndSourceProduct(FormVO formVo);
    
    
    
    /**
     * 
     * Method to delete deleteDynamicForm
     * @param id
     */

    public void deleteDynamicForm(Long id);

    /**
     * 
     * Method to create VO object from Real
     * @param uiMetaData
     * @return
     */

    public FormVO createVOFromRealObject(UIMetaData uiMetaData);

    /**
     * 
     * Method to load form config mapping
     * @param id
     * @return
     */

    public FormConfigurationMapping loadFormConfigMapping(Long id);

    /**
     *
     * Method to load form config mapping by UIMetaData formName
     * @param id
     * @return
     */

    FormConfigurationMapping loadFormConfigMappingByFormName(Long id);

    /**
     * 
     * Method to get model by model name
     * @param modelName
     * @return
     */

    public ModelMetaData getModelMetaDataByUri(String modelName);

    /**
     * 
     * Method to load Form Config Data based on package name
     * @param packageName
     * @return
     */
    public FormConfigEntityData getFormConfigDataByPackageName(String packageName);
    
    
    public List<String> getUiMetaDataForFieldKey(String formName,String fieldKey,Long sourceProductId);

    public Boolean isDuplicateForm(FormVO formVo);
    
    public FormConfigEntityData getFormConfigEntityDataBasedOnBinderName(String  webDataBinderName);

    List<LovConfig> getLovKeyList();

    LovConfig getLovConfigForLovKey(String lovKey);

    /**
     * 
     * Method to get list of parent Form Config Data for Cascade Select Component
     * 
     * @return
     */
    
    public List<FormConfigEntityData> getCascadeEntityData();
    
    /**
     * 
     * Method to get list of child Form Config Data for Cascade Select Component based on parent entityName
     * @param entityName
     * @return
     */
    public List<FormConfigEntityData> getChildCascadeEntityData(String entityName);

    /**
     *
     * Method to get dropdown values for child in cascade select
     * @param entityName
     * @param parentId
     * @return
     */
    public Map<String, String> getChildCascadeDropdownData(String entityName, String parentName, Long parentId);
    /**
     * 
     * @param childEntityName
     * @param parentEntityName
     * @return
     */
    public FormConfigEntityData getFormConfigDataByParentChild(String childEntityName, String parentEntityName);
	public Map<String, String> getChildCascadeDropdownData(String dynamicForm, String entityName, String binderName,String parentName,
			Long parentId);
	
	 
	  public String findLabelForKey(Long uimetaDataId,String fieldKey);
}

