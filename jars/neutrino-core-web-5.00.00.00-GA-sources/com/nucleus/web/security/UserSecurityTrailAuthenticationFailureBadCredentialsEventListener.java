package com.nucleus.web.security;

import java.util.Arrays;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;

import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.core.env.Environment;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.event.AuthenticationFailureBadCredentialsEvent;
import org.springframework.security.web.authentication.WebAuthenticationDetails;

import com.nucleus.core.initialization.ProductInformationLoader;
import com.nucleus.core.misc.util.DateUtils;
import com.nucleus.event.EventService;
import com.nucleus.event.EventTypes;
import com.nucleus.event.UserSecurityTrailEvent;
import com.nucleus.user.UserInfo;
import com.nucleus.user.UserService;

@Named("userSecurityTrailAuthenticationFailureBadCredentialsEventListener")
public class UserSecurityTrailAuthenticationFailureBadCredentialsEventListener implements
        ApplicationListener<AuthenticationFailureBadCredentialsEvent> {

    @Autowired
    private EventService eventService;

    @Inject
    @Named(value = "userService")
    private UserService  userService;

    @Autowired
	private Environment environment;
    
    private boolean ssoEnabled=false;
	
	@PostConstruct
	public void init() {
		String[] activeProfiles = environment.getActiveProfiles();
		if ((activeProfiles != null) && (activeProfiles.length > 0) && (Arrays.asList(activeProfiles).contains("sso")))
        {
        	ssoEnabled=true;
        }
	}
    
    @Override
    public void onApplicationEvent(AuthenticationFailureBadCredentialsEvent badCredentialsEvent) {

        UserInfo userInfo = null;
        

        String username = null;
        String remoteIp = null;
        String sessionId = null;
        

        if (badCredentialsEvent != null && badCredentialsEvent.getSource() != null) {
            
                if (((AbstractAuthenticationToken) badCredentialsEvent.getSource()).getPrincipal() != null) {
                    username = String
                            .valueOf(((AbstractAuthenticationToken) badCredentialsEvent.getSource()).getPrincipal());
                }
                if (((AbstractAuthenticationToken) badCredentialsEvent.getSource()).getDetails() != null) {
                    remoteIp = ((WebAuthenticationDetails) ((AbstractAuthenticationToken) badCredentialsEvent.getSource())
                            .getDetails()).getRemoteAddress();
                    sessionId = ((WebAuthenticationDetails) ((AbstractAuthenticationToken) badCredentialsEvent.getSource())
                            .getDetails()).getSessionId();
                }
            
        }

        UserSecurityTrailEvent userSecurityTrailEvent = new UserSecurityTrailEvent(EventTypes.USER_SECURITY_TRAIL_LOGIN_FAIL);

        userInfo = userService.getUserFromUsername(username);

        userSecurityTrailEvent.setUsername(username);
        if (userInfo != null) {
            userSecurityTrailEvent.setAssociatedUserUri(userInfo.getUserEntityId().getUri());
            userService.incrementFailedLoginCount(userInfo.getId());
        }
        updateModuleNameForEvent(userSecurityTrailEvent);
        userSecurityTrailEvent.setRemoteIpAddress(remoteIp);
        userSecurityTrailEvent.setSessionId(sessionId);
        eventService.createEventEntry(userSecurityTrailEvent);

    }

	private void updateModuleNameForEvent(UserSecurityTrailEvent userSecurityTrailEvent) {
		if (ssoEnabled) {
			userSecurityTrailEvent.setModuleNameForEvent("SSO");
		} else {
			userSecurityTrailEvent.setModuleNameForEvent(ProductInformationLoader.getProductName());
		}
	}
	
}
