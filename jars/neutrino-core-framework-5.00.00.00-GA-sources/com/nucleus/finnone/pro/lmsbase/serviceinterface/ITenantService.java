/**
 * 
 */
package com.nucleus.finnone.pro.lmsbase.serviceinterface;

import java.util.List;

import com.nucleus.entity.BaseTenant;
import com.nucleus.finnone.pro.lmsbase.domainobject.Tenant;

/**
 * @author iss
 * @generated 
 *            "UML to Java V5.0 (com.ibm.xtools.transform.uml2.java5.internal.UML2JavaTransform)"
 */
public interface ITenantService {
	/**
	 * @param id
	 * @return
	 * @generated 
	 *            "UML to Java V5.0 (com.ibm.xtools.transform.uml2.java5.internal.UML2JavaTransform)"
	 */
	Tenant getTenant(Long id);

	/**
	 * 
	 * @param tenant
	 * @return
	 */
	Tenant updateTenant(Tenant tenant);
	
	/**
	 * Method is to get branch id(s) for particular branch groups.
	 * 
	 * @param branchGroupIdList
	 * @return
	 */
	List<Long> getBranches(List<Long> branchGroupIdList);
	List<Tenant> findAllTenant();
	Boolean isSystemPendingForTheBusinessRelease();
	Long getDefaultTenantId();	
	Tenant getDefaultTenant();
	String getAmountFormatWithoutPrecision();
}