package com.nucleus.core.mutitenancy.dao;

import javax.inject.Named;

import com.nucleus.entity.BaseTenant;
import com.nucleus.persistence.BaseDaoImpl;
@Named("multiTenantDaoImpl")
public class MultiTenantDaoImpl extends BaseDaoImpl implements IMultiTenantDao{
	
	public BaseTenant getTenantById(Long id){
		
				return (BaseTenant) getEntityManager().find(BaseTenant.class, id);
			}
}

