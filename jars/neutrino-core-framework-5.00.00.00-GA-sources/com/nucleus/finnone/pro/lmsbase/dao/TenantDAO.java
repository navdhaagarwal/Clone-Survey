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
package com.nucleus.finnone.pro.lmsbase.dao;


import static com.nucleus.finnone.pro.general.util.ValidatorUtils.hasNoElements;
import static com.nucleus.finnone.pro.lmsbase.constants.LMSBaseApplicationConstants.BRANCH_GROUPID_DOES_NOT_EXIST_MESSAGE;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Named;
import javax.persistence.Query;
import javax.persistence.TypedQuery;

import com.nucleus.finnone.pro.base.exception.BusinessException;
import com.nucleus.finnone.pro.base.exception.ExceptionBuilder;
import com.nucleus.finnone.pro.general.constants.ExceptionSeverityEnum;
import com.nucleus.finnone.pro.lmsbase.domainobject.Tenant;
import com.nucleus.persistence.EntityDaoImpl;

/**
 * @author iss
 * @author dhananjay.jha
 */
@Named("tenantDAO")
public class TenantDAO extends EntityDaoImpl implements ITenantDAO {
	
	
	/**
	 *
	 * @see ITenantDAO#getTenant(Long id)
	 *
	 */
	@Override
  public Tenant getTenant(Long id) {
		return (Tenant) getEntityManager().find(Tenant.class, id);
	}

	@Override
  public List<Tenant> findAllTenant() {
	    String qlString = "FROM " + Tenant.class.getSimpleName();
	    TypedQuery<Tenant> qry = getEntityManager().createQuery(qlString,Tenant.class);
	    return qry.getResultList();
	}
	/**
	 * Method to update the tenant Object
	 */
	@Override
  public Tenant updateTenant(Tenant tenant){
		return getEntityManager().merge(tenant); 
	}
	
	
	/*
	 * (non-Javadoc)
	 * @see com.nucleus.finnone.pro.lmsbase.dao.
	 * ITenantDAO#getBranches(java.util.List)
	 */
	@Override
	public List<Long> getBranches(List<Long> branchGroupIdList) {
		List<Long> branchIdList = new ArrayList<Long>();
		for (Long branchGroupId : branchGroupIdList) {
			Query query = getEntityManager().createNamedQuery("searchBranchId");
			query.setParameter("processBranchGroupId", branchGroupId);
			branchIdList.addAll(query.getResultList());
		}
		if (hasNoElements(branchIdList)) {
			throw ExceptionBuilder.getInstance(BusinessException.class).setLogMessage("Branch Group Id does not exist").setExceptionCode(BRANCH_GROUPID_DOES_NOT_EXIST_MESSAGE).setSeverity(ExceptionSeverityEnum.SEVERITY_HIGH.getEnumValue()).build();
		}
		return branchIdList;
	}
	
	@Override
  public Tenant createTenant(Tenant tenant){
		getEntityManager().persist(tenant);
		return tenant;
	}
	
}