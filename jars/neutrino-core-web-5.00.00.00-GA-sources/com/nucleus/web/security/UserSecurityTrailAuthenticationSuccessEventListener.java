package com.nucleus.web.security;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.context.ApplicationListener;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;

import com.nucleus.finnone.pro.base.utility.CoreUtility;
import com.nucleus.logging.BaseLoggers;
import com.nucleus.security.core.session.SessionModuleService;

@Named("userSecurityTrailAuthenticationSuccessEventListener")
public class UserSecurityTrailAuthenticationSuccessEventListener implements ApplicationListener<AuthenticationSuccessEvent> {

	@Inject
	@Named("neutrinoSecurityUtility")
	private NeutrinoSecurityUtility neutrinoSecurityUtility;

	@Inject
	@Named(value = "sessionModuleService")
	private SessionModuleService sessionModuleService;
	
	@Inject
    @Named("coreUtility")
	private CoreUtility coreUtility;
	
	private boolean ssoEnabled=false;
	
	@PostConstruct
	public void init() {
		ssoEnabled =coreUtility.isSsoEnabled();
	}

    @Override
    public void onApplicationEvent(AuthenticationSuccessEvent authenticationSuccessEvent) {
    	if (ssoEnabled)
        {
          BaseLoggers.flowLogger.debug("UserSecurityTrailAuthenticationSuccessEventListener called for SSO profile.Saving Authentication Success Events");
          neutrinoSecurityUtility.createAuthenticationSuccessEventEntry(authenticationSuccessEvent, null);
			sessionModuleService.createSessionModuleMapping(authenticationSuccessEvent);
        }
      }	
    }
