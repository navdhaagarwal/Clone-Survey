package com.nucleus.core.mutitenancy.businessobject;

import com.nucleus.entity.BaseTenant;

public interface MultiTenantBusinessObject {
	
	public BaseTenant getTenantById(Long id);
}
