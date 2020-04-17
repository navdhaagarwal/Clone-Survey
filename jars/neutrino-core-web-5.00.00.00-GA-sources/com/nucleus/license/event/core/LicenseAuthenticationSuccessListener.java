package com.nucleus.license.event.core;

import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.context.ApplicationListener;
import org.springframework.core.Ordered;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.stajistics.Stats;
import org.stajistics.session.StatsSession;
import org.stajistics.session.StatsSessionManager;

import com.nucleus.license.service.LoginService;
import com.nucleus.license.utils.Constants;
import com.nucleus.logging.BaseLoggers;
import com.nucleus.user.UserInfo;

@Named("licenseAuthenticationSuccessListener")
public class LicenseAuthenticationSuccessListener implements ApplicationListener<AuthenticationSuccessEvent>, Ordered{
	
	@Inject
	@Named("loginService")
	private LoginService loginService;

	@Override
	public int getOrder() {
		return 0;
	}

	@Override
	public void onApplicationEvent(AuthenticationSuccessEvent authenticationSuccessEvent) {
		if(authenticationSuccessEvent!=null && (authenticationSuccessEvent.getAuthentication() != null ?
				(authenticationSuccessEvent.getAuthentication().getPrincipal() instanceof  UserInfo) : true))
		{
		loginService.login();
		StatsSessionManager sessManager = Stats.getSessionManager();
	    sessManager.getSessions();
		StatsSession session = sessManager.getSession(Constants.statsKey);
		if(session!=null){
			String log="LicenseAuthenticationSuccessListener onApplicationEvent(): "+session.getSum();
			BaseLoggers.flowLogger.debug(log);
			session.getKey().getName();
		 }
	}
	}
}
