package com.nucleus.core.mutitenancy.businessobject;

import javax.inject.Inject;
import javax.inject.Named;

import com.nucleus.core.mutitenancy.dao.IMultiTenantDao;
import com.nucleus.entity.BaseTenant;

@Named("multiTenantBusinessObject")
public class MultiTenantBusinessObjectImpl implements MultiTenantBusinessObject {
	@Inject
	@Named("multiTenantDaoImpl")
	IMultiTenantDao multiTenantDao;

	@Override
	public BaseTenant getTenantById(Long id) {
		
		return multiTenantDao.getTenantById(id);
	}

}
