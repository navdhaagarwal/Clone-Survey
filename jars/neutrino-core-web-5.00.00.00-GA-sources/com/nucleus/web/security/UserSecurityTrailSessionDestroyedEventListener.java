package com.nucleus.web.security;

import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.core.session.SessionDestroyedEvent;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.security.web.session.HttpSessionDestroyedEvent;

import com.nucleus.broadcast.service.BroadcastMessageService;
import com.nucleus.core.initialization.ProductInformationLoader;
import com.nucleus.event.EventService;
import com.nucleus.event.EventTypes;
import com.nucleus.event.UserSecurityTrailEvent;
import com.nucleus.security.core.session.NeutrinoSessionInformation;
import com.nucleus.security.core.session.NeutrinoSessionRegistry;
import com.nucleus.security.core.session.SessionModuleService;
import com.nucleus.user.UserInfo;
import com.nucleus.user.UserSessionManagerService;

@Named("userSecurityTrailSessionDestroyedEventListener")
public class UserSecurityTrailSessionDestroyedEventListener implements ApplicationListener<SessionDestroyedEvent> {

    @Autowired
    private EventService eventService;
    
    @Inject
    @Named("sessionRegistry")
    private NeutrinoSessionRegistry sessionRegistry;
    
    @Inject
    @Named("userSessionManagerService")
    private UserSessionManagerService  userSessionManagerService;

    @Inject
    @Named(value = "sessionModuleService")
    private SessionModuleService sessionModuleService;
    
	@Inject
	@Named("broadcastMessageService")
	private BroadcastMessageService broadcastMessageService;
    
    @Override
    public void onApplicationEvent(SessionDestroyedEvent event) {
        
        SecurityContextImpl securityContext = null;

		if (event != null) {
			HttpSession session = ((HttpSessionDestroyedEvent) event).getSession();
			securityContext = (SecurityContextImpl) session.getAttribute("SPRING_SECURITY_CONTEXT");
		}        
        
		if (securityContext != null && securityContext.getAuthentication() != null
				&& securityContext.getAuthentication().getPrincipal() instanceof UserInfo
				&& securityContext.getAuthentication().getDetails() instanceof WebAuthenticationDetails) {
			UserInfo userInfo = (UserInfo) securityContext.getAuthentication().getPrincipal();
			if (userInfo.getUsername() != null) {
				UserSecurityTrailEvent userSecurityTrailEvent = new UserSecurityTrailEvent(
						EventTypes.USER_SECURITY_TRAIL_LOGOUT);
				userSecurityTrailEvent.setRemoteIpAddress(
						((WebAuthenticationDetails) securityContext.getAuthentication().getDetails())
								.getRemoteAddress());

				userSecurityTrailEvent.setSessionId(event.getId());
				setLogoutInfoToUserSecurityTrailEvent(userSecurityTrailEvent, userInfo);
				userSecurityTrailEvent.setUsername(userInfo.getUsername());
				userSecurityTrailEvent.setAssociatedUserUri(userInfo.getUserEntityId().getUri());
				userSecurityTrailEvent.setModuleNameForEvent(ProductInformationLoader.getProductName());
				eventService.createEventEntry(userSecurityTrailEvent);
			}

			broadcastMessageService.removeFluxForLoggedOutUser(userInfo.getUuid());

		}
        
        if (event != null && event.getId() != null) {
            sessionModuleService.deleteSessionModuleMapping(event.getId());
            sessionRegistry.removeSessionInformation(event.getId());
        }
    }

	private void setLogoutInfoToUserSecurityTrailEvent(UserSecurityTrailEvent userSecurityTrailEvent,
			UserInfo userInfo) {
		NeutrinoSessionInformation neutrinoSessionInformation = (NeutrinoSessionInformation) sessionRegistry
				.getSessionInformation(userSecurityTrailEvent.getSessionId());
		if (neutrinoSessionInformation != null && neutrinoSessionInformation.getLogOutType() != null) {
			userSecurityTrailEvent.setLogOutType(neutrinoSessionInformation.getLogOutType());
			if (neutrinoSessionInformation.getLogOutBy() != null) {
				userSecurityTrailEvent.setLogOutBy(neutrinoSessionInformation.getLogOutBy().toString());
			}
			if (neutrinoSessionInformation.getForceLogOutIP() != null) {
				userSecurityTrailEvent.setForceLogOutIP(neutrinoSessionInformation.getForceLogOutIP());
			}
		} else {
			userSecurityTrailEvent.setLogOutType(NeutrinoSessionInformation.LOGOUT_TYPE_ON_SESSION_TIME_OUT);
			if (neutrinoSessionInformation != null && !neutrinoSessionInformation.isExpired()) {
				userSessionManagerService.invalidateUserSessionAndUpdateRegistry(userInfo.getId(),
						neutrinoSessionInformation);
			}
		}

	}
}
