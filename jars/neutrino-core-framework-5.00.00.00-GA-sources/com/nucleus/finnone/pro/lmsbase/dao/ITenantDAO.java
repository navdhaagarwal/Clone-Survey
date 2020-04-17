/**
 * 
 */
package com.nucleus.finnone.pro.lmsbase.dao;

import java.util.List;

import com.nucleus.finnone.pro.lmsbase.domainobject.Tenant;
import com.nucleus.persistence.EntityDao;


/** 
 * @author iss
 * @generated "UML to Java V5.0 (com.ibm.xtools.transform.uml2.java5.internal.UML2JavaTransform)"
 */
public interface ITenantDAO extends EntityDao{
	/**
	 * @param id
	 * @return
	 * @generated "UML to Java V5.0 (com.ibm.xtools.transform.uml2.java5.internal.UML2JavaTransform)"
	 */
	Tenant getTenant(Long id);
	
	/**
	 * 
	 * @param tenant
	 * @return
	 */
	Tenant updateTenant(Tenant tenant);
	
	/**
	 * Method is to get branch id(s).
	 * 
	 * @param branchGroupIdList
	 * @return
	 */
	List<Long> getBranches(List<Long> branchGroupIdList);
	
	Tenant createTenant(Tenant tenant);
	
	List<Tenant> findAllTenant();
}