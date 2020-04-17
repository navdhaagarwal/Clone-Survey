package com.nucleus.standard.context;

import static com.nucleus.finnone.pro.general.util.ValidatorUtils.isNull;
import static com.nucleus.finnone.pro.general.util.ValidatorUtils.notNull;

import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import com.nucleus.businessmapping.service.UserManagementServiceCore;
import com.nucleus.core.exceptions.SystemException;
import com.nucleus.core.mutitenancy.service.MultiTenantService;
import com.nucleus.core.organization.entity.Organization;
import com.nucleus.currency.Currency;
import com.nucleus.entity.BaseTenant;
import com.nucleus.finnone.pro.general.util.ValidatorUtils;
import com.nucleus.logging.BaseLoggers;
import com.nucleus.money.MoneyService;
import com.nucleus.user.OrgBranchInfo;
import com.nucleus.user.UserInfo;
import com.nucleus.user.UserService;
@Named("neutrinoExecutionContextInitializationHelper")
public class NeutrinoExecutionContextInitializationHelper {
	@Inject
	@Named("moneyService")
	MoneyService moneyService;
	@Inject
	@Named("neutrinoExecutionContextHolder")
	INeutrinoExecutionContextHolder neutrinoExecutionContextHolder;
	@Inject
	@Named("multiTenantService")
	private MultiTenantService multiTenantService;
	
	@Inject
    @Named("userService")
    private UserService userService;
	
	@Inject
	@Named("userManagementServiceCore")
    private UserManagementServiceCore  userManagementService;
	
	public static final String DEFAULT_USER = "system";
	public void initializeContextOnAuthentication(){
		
		UserInfo userInfo = getUserDetails();//
		Currency baseCurrency =moneyService.getBaseCurrency(); 
		BaseTenant baseTenant = null;
		
		if(notNull(userInfo))
		{
			neutrinoExecutionContextHolder.addToGlobalContext(NeutrinoExecutionContextHolder.USERINFO, userInfo);
			baseTenant = userInfo.getBaseTenant();
		}
		
		if(isNull(baseTenant)){
			 baseTenant = multiTenantService.getDefaultTenant();
		}
		
		if(notNull(baseCurrency))
		{
			neutrinoExecutionContextHolder.addToGlobalContext(NeutrinoExecutionContextHolder.CURRENCY, baseCurrency);
		}
		if(notNull(baseTenant))
		{
			neutrinoExecutionContextHolder.addToGlobalContext(NeutrinoExecutionContextHolder.TENANT, baseTenant);
		
		}
	}
	public void initializeContextOnDemand(){
	
		initializeContextOnAuthentication();
		
		
	}
	
	public void initializeDefaultContext(boolean initializeUser){
		
		if(initializeUser && ValidatorUtils.isNull(NeutrinoExecutionContextSupport
				.getFromGlobalContext(NeutrinoExecutionContextHolder.USERINFO))){
				UserInfo userInfo = userService.getUserFromUsername(DEFAULT_USER);
				updateUserPrimaryBranchInfo(userInfo);				
				neutrinoExecutionContextHolder.addToGlobalContext(NeutrinoExecutionContextHolder.USERINFO, userInfo);
			
		}
		if(ValidatorUtils.isNull(NeutrinoExecutionContextSupport
				.getFromGlobalContext(NeutrinoExecutionContextHolder.CURRENCY))){
			Currency baseCurrency =moneyService.getBaseCurrency();
			neutrinoExecutionContextHolder.addToGlobalContext(NeutrinoExecutionContextHolder.CURRENCY, baseCurrency);
		}
		if(ValidatorUtils.isNull(NeutrinoExecutionContextSupport
				.getFromGlobalContext(NeutrinoExecutionContextHolder.TENANT))){
			BaseTenant baseTenant = multiTenantService.getDefaultTenant();
			neutrinoExecutionContextHolder.addToGlobalContext(NeutrinoExecutionContextHolder.TENANT, baseTenant);
		}
		
	}
	
	private void updateUserPrimaryBranchInfo(UserInfo userInfo) {
		try{
			Organization organizationBranch = userManagementService.getUserPrimaryOrganizationBranch(userInfo.getId());
			// set the logged in branch of the user
			if (userInfo != null && organizationBranch != null) {
	                OrgBranchInfo orgBranchInfo = new OrgBranchInfo();
	                orgBranchInfo.setId(organizationBranch.getId());
	                orgBranchInfo.setOrgName(organizationBranch.getName());
	                userInfo.setPrimaryOrgBranchInfo(orgBranchInfo);
	                BaseLoggers.flowLogger.info("Setting users login branch  :" + organizationBranch.getName());
	                userInfo.setLoggedInBranch(orgBranchInfo);
	         }
		}catch (Exception e) {
            BaseLoggers.exceptionLogger.info("Excetion occurred :: " + e.getMessage());
            throw new SystemException(e);
        }		
	}
	
	private UserInfo getUserDetails()
	  {
	    UserInfo userInfo = null;
	    SecurityContext securityContext = SecurityContextHolder.getContext();
	    if (securityContext != null&&securityContext.getAuthentication()!=null) {
	      Object principal = securityContext.getAuthentication().getPrincipal();
	      if (UserInfo.class.isAssignableFrom(principal.getClass()))
	        userInfo = (UserInfo)principal;
	    }
	    		    
	    return userInfo;
	    }
	  }
