package com.nucleus.core.user.listener;

import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.context.ApplicationListener;
import org.springframework.core.Ordered;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;

import com.nucleus.businessmapping.service.UserInfoOrgBranchMappingService;
import com.nucleus.core.exceptions.SystemException;
import com.nucleus.finnone.pro.base.utility.CoreUtility;
import com.nucleus.logging.BaseLoggers;
import com.nucleus.user.UserInfo;

@Named("userBranchMappingsListener")
public class UserBranchMappingsListener implements
		ApplicationListener<AuthenticationSuccessEvent>, Ordered {

	@Inject
	@Named("userInfoOrgBranchMappingService")
	private UserInfoOrgBranchMappingService userInfoOrgBranchMappingService;

	@Override
	public void onApplicationEvent(
			AuthenticationSuccessEvent authenticationSuccessEvent) {

		if (authenticationSuccessEvent != null
				&& authenticationSuccessEvent.getAuthentication() != null
				&& (authenticationSuccessEvent.getAuthentication()
						.getPrincipal() instanceof UserInfo)) {

			UserInfo userInfo = (UserInfo) authenticationSuccessEvent
					.getAuthentication().getPrincipal();

			try {
				userInfoOrgBranchMappingService.updateOrgBranchInfo(userInfo);
				CoreUtility.syncSecurityContextHolderInSession(userInfo.getMappedSessionId());
			} catch (Exception e) {
				BaseLoggers.exceptionLogger.info("Excetion occurred :: "
						+ e.getMessage());
				throw new SystemException(e);
			}

		}
	}
	
	@Override
	public int getOrder() {
		return Ordered.HIGHEST_PRECEDENCE;
	}

	

}