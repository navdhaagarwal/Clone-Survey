package com.nucleus.web.security;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.web.access.intercept.FilterInvocationSecurityMetadataSource;
import org.springframework.security.web.access.intercept.FilterSecurityInterceptor;

public class CustomFilterSecurityInterceptor extends FilterSecurityInterceptor{
	
	@Inject
	@Named("securityMetadataSource")
	private FilterInvocationSecurityMetadataSource securityMetadataSource;
	
	@Inject
	@Named("additionalSecurityMetadataSource")
	private FilterInvocationSecurityMetadataSource additionalSecurityMetadataSource;
	
	@Override
	public void afterPropertiesSet() throws Exception {
		
		super.setSecurityMetadataSource(new FilterInvocationSecurityMetadataSourceWrapper());
		super.afterPropertiesSet();
		
	}


	class FilterInvocationSecurityMetadataSourceWrapper implements FilterInvocationSecurityMetadataSource{

		
		@Override
		public Collection<ConfigAttribute> getAttributes(Object object){
			Collection<ConfigAttribute> configAttributesList =null;
			configAttributesList=additionalSecurityMetadataSource.getAttributes(object);
			if(configAttributesList==null) {
				configAttributesList=securityMetadataSource.getAttributes(object);
				return configAttributesList;
			}
			configAttributesList.addAll(securityMetadataSource.getAttributes(object));
			return configAttributesList;
		}

		@Override
		public Collection<ConfigAttribute> getAllConfigAttributes() {
			List<ConfigAttribute> configAttributeList = new ArrayList<>();
			configAttributeList.addAll(additionalSecurityMetadataSource.getAllConfigAttributes());
			configAttributeList.addAll(securityMetadataSource.getAllConfigAttributes());
			return configAttributeList;
		}

		@Override
		public boolean supports(Class<?> clazz) {
			return securityMetadataSource.supports(clazz) || additionalSecurityMetadataSource.supports(clazz);
		}
		
	}
	
}
