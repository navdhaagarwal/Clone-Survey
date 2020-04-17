package com.nucleus.core.dynamicform.service;

import com.nucleus.service.BaseService;

public interface IDynamicFormPostProcessor extends BaseService {

    public void postProcessDynamicFormData(Object object);

}
