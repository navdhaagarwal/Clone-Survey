/**
 * 
 */
package com.nucleus.finnone.pro.lmsbase.businessobject;

import java.util.List;

import com.nucleus.finnone.pro.lmsbase.domainobject.Tenant;

/** 
 * @author iss
 * @generated "UML to Java V5.0 (com.ibm.xtools.transform.uml2.java5.internal.UML2JavaTransform)"
 */
public interface ITenantBusinessObject {
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

	Tenant createTenant(Tenant tenant);
	
	/**
	 * Method is used to get branch id(s) for particular branch group id(s).
	 * 
	 * @param branchGroupIdList
	 * @return
	 */
	List<Long> getBranches(List<Long> branchGroupIdList);
	List<Tenant> findAllTenant();
}