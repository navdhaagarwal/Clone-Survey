package com.nucleus.core.mutitenancy.service;

import java.util.Locale;

import com.nucleus.entity.BaseTenant;

public interface MultiTenantService {

	
	BaseTenant getTenantById(Long id);
	BaseTenant getDefaultTenant();
	Long getDefaultTenantId();
	
	Locale getSystemLocale();
		
}
