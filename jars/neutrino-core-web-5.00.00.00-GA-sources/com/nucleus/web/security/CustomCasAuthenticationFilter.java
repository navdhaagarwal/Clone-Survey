package com.nucleus.web.security;

import java.io.IOException;

import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.nucleus.finnone.pro.base.utility.CoreUtility;
import com.nucleus.security.core.session.SessionModuleService;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.cas.web.CasAuthenticationFilter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.session.SessionAuthenticationException;

import com.nucleus.logging.BaseLoggers;
import com.nucleus.master.BaseMasterService;
import com.nucleus.user.UserBranchProductService;
import com.nucleus.user.UserInfo;

import static com.nucleus.web.login.LoginConstants.INVALID_LOGIN_HIGH_CONCURRENCY;

public class CustomCasAuthenticationFilter extends CasAuthenticationFilter {

	@Inject
	@Named("customConcurrentSessionControlStrategy")
	private CustomConcurrentSessionControlStrategy customConcurrentSessionControlStrategy;

    
	  @Inject
	  @Named("baseMasterService")
	  private BaseMasterService baseMasterService;
    
	  @Inject
	  @Named("userBranchProductService")
	  private UserBranchProductService userBranchProductService;

	@Inject
	@Named(value = "sessionModuleService")
	private SessionModuleService sessionModuleService;
	  

	
	@Override
	public Authentication attemptAuthentication(final HttpServletRequest request, final HttpServletResponse response)
			throws IOException {
		Authentication auth = null;
		String username = null;
		try{
			auth = super.attemptAuthentication(request, response);
			if (auth != null) {
				UserInfo userInfo = (UserInfo) auth.getPrincipal();
				username = userInfo.getUsername();
				userInfo.setMappedSessionId(request.getSession().getId());
				CoreUtility.syncSecurityContextHolderInSession(userInfo.getMappedSessionId());
			}
//		String id = request.getParameter("branchId");
//		if(!id.equals("")){
//			Long branchId = Long.parseLong(id);
//			updateBranch(branchId,(UserInfo) auth.getPrincipal());	
//		}
//		
			if (!sessionModuleService.isAllowLoginForConcurrencyMode(auth)) {
				throw new SessionAuthenticationException(INVALID_LOGIN_HIGH_CONCURRENCY);
			}
			customConcurrentSessionControlStrategy.checkAuthenticationAllowed(auth, request);
		} catch (SessionAuthenticationException e) {
			SecurityContextHolder.clearContext();
			BaseLoggers.flowLogger.error("SSO Sessions exceeded " + e.getMessage());
			if(e.getMessage().contains("label.license.concurrent.user.exceeds")){
				throw new NeutrinoSSOConcurrencyException(username);
			}else{
				throw new NeutrinoSSOSessionConcurrencyException(username);
			}
		}catch(LockedException e){
			SecurityContextHolder.clearContext();
			BaseLoggers.flowLogger.error("LockedException while authenticating service ticket:" , e);
			throw new NeutrinoSSOLockedException("User Account is Locked");
		}catch(Exception e){
			BaseLoggers.flowLogger.error("Exception while authenticating service ticket:" ,e);
		}

		return auth;
	}
	
//	private void updateBranch(Long branchId, UserInfo userInfo){
//		OrgBranchInfo orgBranchInfo = new OrgBranchInfo();
//        OrganizationBranch organizationBranch = baseMasterService
//                .getMasterEntityById(OrganizationBranch.class, branchId);
//        orgBranchInfo.setId(branchId);
//        orgBranchInfo.setOrgName(organizationBranch.getName());
//        userInfo.setLoggedInBranch(orgBranchInfo);
//        userBranchProductService.updateUserInfoLoggedInBranchProducts(userInfo);
//	}

}
