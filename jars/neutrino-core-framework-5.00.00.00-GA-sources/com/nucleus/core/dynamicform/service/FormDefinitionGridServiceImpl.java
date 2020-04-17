package com.nucleus.core.dynamicform.service;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import com.nucleus.core.dynamicform.dao.FormDefinitionDao;
import com.nucleus.core.formsConfiguration.FormConfigurationMapping;
import com.nucleus.core.formsConfiguration.ModelMetaData;
import com.nucleus.core.formsConfiguration.UIMetaData;
import com.nucleus.service.BaseServiceImpl;

@Named(value = "formDefinitionGridService")
public class FormDefinitionGridServiceImpl extends BaseServiceImpl implements FormDefinitionGridService {

	
    @Inject
    @Named("formDefinitionDao")
    private FormDefinitionDao formDefinitionDao; 

	
    @Override
    public List<ModelMetaData> getModelMetaDatas() {
        return entityDao.findAll(ModelMetaData.class);
    }

    @Override
    public List<UIMetaData> getUIMetaDatas() {
    	
    	return formDefinitionDao.getAllUIMetaData();
    }

    @Override
    public List<FormConfigurationMapping> getFormConfiguration() {
        return entityDao.findAll(FormConfigurationMapping.class);
    }

}