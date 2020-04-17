package com.nucleus.core.dynamicform.service;

import java.util.List;
import java.util.Map;

import com.nucleus.core.formsConfiguration.DynamicFormScreenMappingDetail;
import com.nucleus.core.formsConfiguration.FormConfigInvocMapping;
import com.nucleus.core.formsConfiguration.ModelMetaData;
import com.nucleus.core.formsConfiguration.UIMetaData;
import com.nucleus.service.BaseService;

/**
 * 
 * @author Nucleus Software Exports Limited
 * Form Configuration Mapping Service
 */
public interface FormConfigurationMappingService extends BaseService {

    /**
     * 
     * Load UI Meta Data based on invocation point
     * @param invocationPoint
     * @return
     */
    public Map<String, Object> getUiMetaData(String formName, String invocationPoint, String uri, String formVersion);

    /**
     * 
     * Load all the model meta data list
     * @return
     */
    public List<ModelMetaData> getModelMetaData();

    /**
     * 
     * Load UI MEta DAta based on Model Name
     * @param modelId
     * @return
     */
    public List<UIMetaData> getUIMetaDataByModel(long modelId);

    /**
     * 
     * Load FormConfigurationInvocationMapping data
     * @return
     */
    public List<FormConfigInvocMapping> getFormMappingPoints();

    /**
     * 
     * Load Model by form name
     * @param modelName
     * @return
     */
    public String getModelByFormName(String modelName);

    public UIMetaData getUiMetaDataTemplate(String formName, String invocationPoint);
    List<DynamicFormScreenMappingDetail> fetchDynamicFormsMappedToScreenIdAndSourceproductId(
            Long screenId,Long sourceProductId,Long productType);

    List<DynamicFormScreenMappingDetail> fetchDynamicFormsMappedToScreenIdAndSourceproductId(
            Long screenId,Long sourceProductId);

    List<DynamicFormScreenMappingDetail> fetchAllDynamicFormsMappedToScreenIdAndSourceproduct(
            Long screenId,Long sourceProductId);
   
}
