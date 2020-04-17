package com.nucleus.core.dynamicform.service;

import java.util.List;

import com.nucleus.core.formsConfiguration.FormConfigurationMapping;
import com.nucleus.core.formsConfiguration.ModelMetaData;
import com.nucleus.core.formsConfiguration.UIMetaData;
import com.nucleus.service.BaseService;

/**
 * 
 * @author Nucleus Software Exports Limited
 * Grid data service for Form Definition
 */

public interface FormDefinitionGridService extends BaseService {

    /**
     * 
     * Method to retrieve all the UiMetaDetas To display in grid
     * 
     * @return
     */
    public List<UIMetaData> getUIMetaDatas();

    /**
     * @Description returns all ModelMetaDetas
     */
    public List<ModelMetaData> getModelMetaDatas();

    /**
     * 
     * Method to load form config mapping
     * @return
     */

    public List<FormConfigurationMapping> getFormConfiguration();

}
