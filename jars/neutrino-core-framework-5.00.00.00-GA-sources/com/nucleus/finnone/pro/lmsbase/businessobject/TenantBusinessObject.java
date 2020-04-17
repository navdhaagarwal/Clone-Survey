/**
Author: Incredible
Creation Date: 17/08/2012
Copyright: Nucleus Software Exports Ltd
Description: Fetching Tenant Data based on Id
----------------------------------------------------------------------------------------------------------------
Revision:  Version	Last Revision Date	 	Name		Function / Module affected       Modifications Done
----------------------------------------------------------------------------------------------------------------	
	       1.0		27/08/2012		Dhananjay Kumar Jha 	  Code for getTenant    
----------------------------------------------------------------------------------------------------------------
 *
 */
package com.nucleus.finnone.pro.lmsbase.businessobject;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import com.nucleus.finnone.pro.lmsbase.dao.ITenantDAO;
import com.nucleus.finnone.pro.lmsbase.domainobject.Tenant;

/** 
 * @author iss
 * @author dhananjay.jha
 */
@Named("tenantBusinessObject")
public class TenantBusinessObject implements ITenantBusinessObject {
	
	@Inject
	@Named("tenantDAO")
	private ITenantDAO tenantDAO; 
	/**
	 * 
	 * @see ITenantBusinessObject#getTenant(Long id)
	 * 
	 */
	@Override
	public Tenant getTenant(Long id) {

		return tenantDAO.getTenant(id);

	}
	
	@Override
  public List<Tenant> findAllTenant() {
		return tenantDAO.findAllTenant();
	}
	
	/**
	 * This method is used to call the method of DAO to update the Tenant
	 */
	@Override
  public Tenant updateTenant(Tenant tenant){
		return tenantDAO.updateTenant(tenant);
	}
	@Override
  public Tenant createTenant(Tenant tenant){
		tenantDAO.createTenant(tenant);
		return tenant;
	}
	
	
	/*
	 * (non-Javadoc)
	 * 
	 */
	@Override
	public List<Long> getBranches(List<Long> branchGroupIdList) {
		return tenantDAO.getBranches(branchGroupIdList);
	}
}