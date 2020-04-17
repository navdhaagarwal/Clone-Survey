package com.nucleus.core.mutitenancy.service;

import static com.nucleus.finnone.pro.general.util.ValidatorUtils.notNull;

import java.util.Locale;

import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.transaction.annotation.Transactional;

import com.nucleus.config.persisted.enity.Configuration;
import com.nucleus.config.persisted.service.ConfigurationService;
import com.nucleus.config.persisted.vo.ConfigurationVO;
import com.nucleus.core.mutitenancy.businessobject.MultiTenantBusinessObject;
import com.nucleus.entity.BaseTenant;
import com.nucleus.entity.SystemEntity;
import com.nucleus.finnone.pro.base.exception.SystemException;
@Named("multiTenantService")
public class MultiTenantServiceImpl implements MultiTenantService {

	@Inject
	@Named("configurationService")
	private ConfigurationService configurationService;
	@Inject
	@Named("multiTenantBusinessObject")
	private MultiTenantBusinessObject multiTenantBusinessObject;
	
	private Locale systemLocale;
	
	@Override
	public BaseTenant getTenantById(Long id) {		
		return multiTenantBusinessObject.getTenantById(id);
	}

	@Override
	public BaseTenant getDefaultTenant() {
		return getTenantById(getDefaultTenantId());
	}

	@Override
	public Long getDefaultTenantId() {
		ConfigurationVO configurationVO = configurationService.getConfigurationPropertyFor(SystemEntity.getSystemEntityId(),Configuration.DEFAULT_TENANT_CONFIG_KEY );
		String tenantIdString=configurationVO.getText();
		if(notNull(tenantIdString))
		{
		return Long.parseLong(tenantIdString);
		}
		return null;
	}
	
	
	public Locale getSystemLocale() {
    	if(systemLocale==null) {
    		BaseTenant baseTenant=getDefaultTenant();
    		if(baseTenant==null || baseTenant.getLocale()==null) {
    			throw new SystemException("Default locale not set in base tenant ");
    		}
    		systemLocale=baseTenant.getLocaleObject();
    	}
		return systemLocale;
	}
	

}
