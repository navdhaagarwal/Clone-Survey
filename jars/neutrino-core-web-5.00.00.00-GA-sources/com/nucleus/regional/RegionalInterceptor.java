package com.nucleus.regional;

import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.annotation.Resource;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.MDC;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;
import org.springframework.web.servlet.support.RequestContextUtils;
import org.springframework.web.servlet.view.RedirectView;
import org.springframework.web.servlet.view.UrlBasedViewResolver;

import com.nucleus.core.web.conversation.ConversationalSessionAttributeStore;
import com.nucleus.logging.BaseLoggers;
import com.nucleus.web.csrf.CSRFRequestDataValueProcessor;

/**
 * @author Nucleus Software Exports Limited
 
 */

public class RegionalInterceptor extends HandlerInterceptorAdapter {

	
	@Resource
	private RegionalDynamicFieldController regionalDynamicFieldController;
	    	
	@Override
	public void postHandle(HttpServletRequest request,
			HttpServletResponse response, Object handler,
			ModelAndView modelAndView) throws Exception {
		
		if(modelAndView!=null){
			
			Map<String, Object> map = regionalDynamicFieldController.paintRegionalFields(modelAndView.getViewName());
			
			if(map !=null && map.size() > 0 && modelAndView.getModel()!=null ) {
				
				for (Map.Entry<String,Object> entry :  ((Map<String,Object>) map).entrySet()) {
					RegionalFieldConfig regionalFieldConfig = null;
					
						regionalFieldConfig=(RegionalFieldConfig)entry.getValue();
										
						if(regionalFieldConfig.getFieldName()!=null && regionalFieldConfig.getFieldName()!=""){
							modelAndView.getModel().put(regionalFieldConfig.getFieldName(), regionalFieldConfig.getFieldName());
					
						if(regionalFieldConfig.getMandatory()!=null && regionalFieldConfig.getMandatory()!=""){
							modelAndView.getModel().put(regionalFieldConfig.getFieldName()+"_mandatoryMode", regionalFieldConfig.getMandatory());
						}
						if(regionalFieldConfig.getDivId()!=null && regionalFieldConfig.getDivId()!=""){
							modelAndView.getModel().put(regionalFieldConfig.getFieldName()+"_divId", regionalFieldConfig.getDivId());
						}
						if(regionalFieldConfig.getFieldLabel()!=null && regionalFieldConfig.getFieldLabel()!=""){
							modelAndView.getModel().put(regionalFieldConfig.getFieldName()+"_label", regionalFieldConfig.getFieldLabel());
						}
						if(regionalFieldConfig.getFieldPlaceHolderKey()!=null && regionalFieldConfig.getFieldPlaceHolderKey()!=""){
							modelAndView.getModel().put(regionalFieldConfig.getFieldName()+"_placeHolderKey", regionalFieldConfig.getFieldPlaceHolderKey());
						}
						if(regionalFieldConfig.getFieldToolTipKey()!=null && regionalFieldConfig.getFieldToolTipKey()!=""){
							modelAndView.getModel().put(regionalFieldConfig.getFieldName()+"_toolTipKey", regionalFieldConfig.getFieldToolTipKey());
						}
						if(regionalFieldConfig.getSourceEntityName()!=null && regionalFieldConfig.getSourceEntityName()!=""){
							modelAndView.getModel().put(regionalFieldConfig.getFieldName()+"_sourceEntity", regionalFieldConfig.getSourceEntityName());
						}
						if(regionalFieldConfig.getViewMode()!=null && regionalFieldConfig.getViewMode()!=""){
							modelAndView.getModel().put(regionalFieldConfig.getFieldName()+"_viewMode", regionalFieldConfig.getViewMode());
						}
						if(regionalFieldConfig.getDisabled()!=null && regionalFieldConfig.getDisabled()!=""){
							modelAndView.getModel().put(regionalFieldConfig.getFieldName()+"_disabled", regionalFieldConfig.getDisabled());
						}
						
						if(regionalFieldConfig.getRegionalVisibility()!=null && regionalFieldConfig.getRegionalVisibility()!=""){
							modelAndView.getModel().put(regionalFieldConfig.getFieldName()+"_regionalVisibility", regionalFieldConfig.getRegionalVisibility());
						}
						if(regionalFieldConfig.getRegionalGenericParameterType()!=null && regionalFieldConfig.getRegionalGenericParameterType()!=""){
                            modelAndView.getModel().put(regionalFieldConfig.getFieldName()+"_regionalGenericParameterType", regionalFieldConfig.getRegionalGenericParameterType());
						}
						if(regionalFieldConfig.getIsGeneric()!=null && regionalFieldConfig.getIsGeneric()!=""){
						modelAndView.getModel().put(regionalFieldConfig.getFieldName()+"_isGeneric", regionalFieldConfig.getIsGeneric());
						}
						if(regionalFieldConfig.getRegionalParentId()!=null && regionalFieldConfig.getRegionalParentId()!=""){
							modelAndView.getModel().put(regionalFieldConfig.getFieldName()+"_regionalParentId", regionalFieldConfig.getRegionalParentId());
						}
						
						if(regionalFieldConfig.getRegionalItemValue()!=null && regionalFieldConfig.getRegionalItemValue()!=""){
						modelAndView.getModel().put(regionalFieldConfig.getFieldName()+"_regionalItemValue", regionalFieldConfig.getRegionalItemValue());
						}
						if(regionalFieldConfig.getRegionalItemLabel()!=null && regionalFieldConfig.getRegionalItemLabel()!=""){
						modelAndView.getModel().put(regionalFieldConfig.getFieldName()+"_regionalItemLabel", regionalFieldConfig.getRegionalItemLabel());
						}
						if(regionalFieldConfig.getRegionalListValue()!=null && regionalFieldConfig.getRegionalListValue()!=""){
						modelAndView.getModel().put(regionalFieldConfig.getFieldName()+"_regionalListValue", regionalFieldConfig.getRegionalListValue());
						}
						if(regionalFieldConfig.getRegionalItemCode()!=null && regionalFieldConfig.getRegionalItemCode()!=""){
							modelAndView.getModel().put(regionalFieldConfig.getFieldName()+"_regionalItemCode", regionalFieldConfig.getRegionalItemCode());
							}

					}
				 }
			}
		}			
			
	}
}
