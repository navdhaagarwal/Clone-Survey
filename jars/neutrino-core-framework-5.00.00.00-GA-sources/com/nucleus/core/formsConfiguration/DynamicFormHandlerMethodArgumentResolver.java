package com.nucleus.core.formsConfiguration;

import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import com.nucleus.core.dynamicform.service.FormService;
import com.nucleus.core.formDefinition.FormDefinitionUtility;

public class DynamicFormHandlerMethodArgumentResolver implements HandlerMethodArgumentResolver {

    @Inject
    @Named("formConfigService")
    protected FormService                     formService;
        
    @Inject
    @Named("formDefinitionUtility")
    private FormDefinitionUtility formDefinitionUtility;
        
    @Override
    public boolean supportsParameter(MethodParameter parameter) {
    	return parameter.getParameterType().equals(DynamicFormData.class);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Object resolveArgument(MethodParameter parameter,
                    ModelAndViewContainer mavContainer, NativeWebRequest webRequest,
                    WebDataBinderFactory binderFactory) throws Exception {
            
            String dynamicFormData = webRequest.getParameter("dynamicFormData");
            return formDefinitionUtility.deserializeMultipleDynamicFormDataAsPlaceholderMap(dynamicFormData);
    }

}
