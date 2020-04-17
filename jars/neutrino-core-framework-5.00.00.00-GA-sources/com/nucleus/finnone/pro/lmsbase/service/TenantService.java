/**
 * Author: Incredible Creation Date: 17/08/2012 Copyright: Nucleus Software
 * Exports Ltd Description: Fetching Tenant Data based on Id
 * --------------------
 * ----------------------------------------------------------
 * ---------------------------------- Revision: Version Last Revision Date Name
 * Function / Module affected Modifications Done
 * --------------------------------
 * ----------------------------------------------
 * ---------------------------------- 1.0 27/08/2012 Dhananjay Kumar Jha Code
 * for getTenant
 * ----------------------------------------------------------------
 * ------------------------------------------------
 * 
 */
package com.nucleus.finnone.pro.lmsbase.service;

import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.nucleus.finnone.pro.base.exception.BusinessException;
import com.nucleus.finnone.pro.base.exception.ExceptionBuilder;
import com.nucleus.finnone.pro.lmsbase.businessobject.ITenantBusinessObject;
import com.nucleus.finnone.pro.lmsbase.constants.TenantStatusEnum;
import com.nucleus.finnone.pro.lmsbase.domainobject.Tenant;
import com.nucleus.finnone.pro.lmsbase.serviceinterface.ITenantService;
import com.nucleus.standard.context.INeutrinoExecutionContextHolder;

/**
 * @author iss
 * @author Dhananjay Kumar Jha
 * 
 */
@Named("tenantService")
public class TenantService implements ITenantService {
  
	@Inject
	@Named("tenantBusinessObject")
	private ITenantBusinessObject tenantBusinessObject;
	
	@Resource(name="tenantProperties")
	private Map<String, String> tenantProperties;

  
  @Inject
  @Named("neutrinoExecutionContextHolder")
  private INeutrinoExecutionContextHolder neutrinoExecutionContextHolder;
  
	private Long tenantId=null;
	private String amountFormatI=null;
	private String amountFormatM=null;

	public static final String TENANTID_NOT_PROVIDED ="Tenantid is not provided in configuration.";
	public static final String TENANTID_NOT_NUMERIC ="Tenantid provided in configuration is not numeric.";
  
  /**
   * 
   * @see ITenantService#getTenant(Long id)
   */
  
  @Override
  @Transactional(readOnly = true)
  public Tenant getTenant(Long id) {
    
    return  tenantBusinessObject.getTenant(id);
    
  }
  
  @Override
  public List<Tenant> findAllTenant() {
    return tenantBusinessObject.findAllTenant();
  }
  
  
  /**
   * This method is used to call the method of Business object
   */
  @Override
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public Tenant updateTenant(Tenant tenant) {
    return tenantBusinessObject.updateTenant(tenant);
  }
  
  @Transactional
  public Tenant createTenant(Tenant tenant) {
    return tenantBusinessObject.createTenant(tenant);
  }
  
  /**
   * Method is to get branch id(s) for particular branch groups.
   * 
   * @param branchGroupIdList
   * @return
   */
  @Override
  public List<Long> getBranches(List<Long> branchGroupIdList) {
    return tenantBusinessObject.getBranches(branchGroupIdList);
  }
  
  @Override
  public Boolean isSystemPendingForTheBusinessRelease(){
	  if(TenantStatusEnum.SYSTEM_RELEASED_FOR_BUSINESS.equalsValue(getTenant(neutrinoExecutionContextHolder.getTenantId()).getEodStatus())){
          return Boolean.FALSE;
      }
      return Boolean.TRUE;
  }

	@Override
	public Long getDefaultTenantId() {
		return tenantId;
	}
	
	@Override
	public Tenant getDefaultTenant() {
		return getTenant(getDefaultTenantId());
	}
	
	@PostConstruct
	public void validateConfiguration()
	{
		String tenantIdAsString = tenantProperties.get("tenantId");
		if (tenantIdAsString == null) {
			throw ExceptionBuilder.getInstance(BusinessException.class, TENANTID_NOT_PROVIDED,
					"Tenantid is not provided in configuration.").setMessage(TENANTID_NOT_PROVIDED).build();
		}

		try {
			tenantId = Long.valueOf(tenantIdAsString.trim());
		} catch (NumberFormatException nfe) {
			throw ExceptionBuilder
					.getInstance(BusinessException.class, TENANTID_NOT_NUMERIC,
							"Tenantid provided in configuration is not numeric.")
					.setMessage(TENANTID_NOT_NUMERIC).build();
		}
		
		if(tenantProperties.get("amountFormatI")!=null){
			amountFormatI = tenantProperties.get("amountFormatI").trim();
		}
		if(tenantProperties.get("amountFormatM")!=null){
			amountFormatM = tenantProperties.get("amountFormatM").trim();
		}

	}

	@Override
	public String getAmountFormatWithoutPrecision() {
		Tenant tenant = getTenant(tenantId);
		Character decimalSeparator = tenant.getDecimalSeparatorSymbol();
		Character groupingSeparator = tenant.getDigitGroupingSymbol();
		String amountFormatWithoutPrecision = amountFormatM;
		String numberFormat = "##,###,###,###,##0.00";
		if("I".equals(tenant.getAmountFormat()))
		 {
		 	numberFormat = "#,##,##,##,##,##,##0.00";
		 	amountFormatWithoutPrecision = amountFormatI;
		 }
		
		amountFormatWithoutPrecision = amountFormatWithoutPrecision.replaceAll("C", String.valueOf(groupingSeparator ));
		amountFormatWithoutPrecision = amountFormatWithoutPrecision.replaceAll("D", String.valueOf(decimalSeparator ));
		return amountFormatWithoutPrecision;
	}
  
}