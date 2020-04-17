package com.nucleus.web.security;

import javax.inject.Inject;
import javax.inject.Named;

import com.nucleus.finnone.pro.base.utility.CoreUtility;
import com.nucleus.user.UserInfo;
import org.springframework.context.ApplicationListener;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.web.authentication.session.SessionFixationProtectionEvent;

@Named("sessionFixationProtectionEventListener")
public class SessionFixationProtectionEventListener implements  ApplicationListener<SessionFixationProtectionEvent>{
	
	@Inject
	@Named("neutrinoSecurityUtility")
	private NeutrinoSecurityUtility neutrinoSecurityUtility;

    @Override
	public void onApplicationEvent(SessionFixationProtectionEvent sessionFixationProtectionEvent) {

		Object obj = sessionFixationProtectionEvent.getAuthentication().getPrincipal();
		if (obj instanceof UserInfo){
			UserInfo userInfo = (UserInfo) obj;
			userInfo.setMappedSessionId(sessionFixationProtectionEvent.getNewSessionId());
			CoreUtility.syncSecurityContextHolderInSession(userInfo.getMappedSessionId());
		}
		AuthenticationSuccessEvent authenticationSuccessEvent = new AuthenticationSuccessEvent(
				sessionFixationProtectionEvent.getAuthentication());
		neutrinoSecurityUtility.createAuthenticationSuccessEventEntry(authenticationSuccessEvent,
				sessionFixationProtectionEvent);
	}

}


