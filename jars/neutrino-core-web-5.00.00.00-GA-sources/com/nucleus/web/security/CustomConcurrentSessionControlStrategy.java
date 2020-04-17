package com.nucleus.web.security;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.session.SessionInformation;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.web.authentication.session.ConcurrentSessionControlAuthenticationStrategy;
import org.springframework.security.web.authentication.session.SessionAuthenticationException;

import com.nucleus.finnone.pro.base.utility.CoreUtility;
import com.nucleus.finnone.pro.cache.common.NeutrinoCachePopulator.Action;
import com.nucleus.license.cache.BaseLicenseService;
import com.nucleus.license.content.model.LicenseDetail;
import com.nucleus.license.utils.LicenseSetupUtil;
import com.nucleus.logging.BaseLoggers;
import com.nucleus.security.oauth.dao.CustomOauthTokenStoreDAO;
import com.nucleus.sso.client.session.ServiceTicketToSessionIdCachePopulator;
import com.nucleus.sso.client.session.SessionIdToServiceTicketCachePopulator;
import com.nucleus.user.UserInfo;
import com.nucleus.user.UserSessionManagerService;
import com.nucleus.web.login.LoginConstants;

public class CustomConcurrentSessionControlStrategy extends ConcurrentSessionControlAuthenticationStrategy {
	
	private final SessionRegistry sessionRegistry;

	//Conditional bean that might be null if API portal is enabled.
	@Autowired(required = false)
	private TokenStore tokenStore;

	@Inject
	@Named("licenseClientCacheService")
	private   BaseLicenseService licenseClientCacheService;
	
     @Autowired
     private   LicenseSetupUtil licenseSetupUtil;
  
     @Inject
     @Named(value = "userSessionManagerService")
     UserSessionManagerService userSessionManagerService;
     
     @Inject
     @Named("coreUtility")
     private CoreUtility coreUtility;
     
     @Inject
     @Named("serviceTicketToSessionIdCachePopulator")
     private ServiceTicketToSessionIdCachePopulator serviceTicketToSessionCache;
     
     @Inject
     @Named("sessionIdToServiceTicketCachePopulator")
     private SessionIdToServiceTicketCachePopulator sessionIdToServiceTicketCache;

    private boolean exceptionIfMaximumExceeded = false;


    private HttpSession httpSession;
	public CustomConcurrentSessionControlStrategy(
			SessionRegistry sessionRegistry) {
		super(sessionRegistry);
		this.sessionRegistry = sessionRegistry;
	}

	@Override
	public void onAuthentication(Authentication authentication,
			HttpServletRequest request, HttpServletResponse response) {
		checkAuthenticationAllowed(authentication, request);

		// Allow the parent to create a new session if necessary
		super.onAuthentication(authentication, request, response);
		/*sessionRegistry.registerNewSession(request.getSession().getId(),
				authentication.getPrincipal());*/
	}

	public void checkAuthenticationAllowed(Authentication authentication,

			HttpServletRequest request)  {
	
		 LicenseDetail licenseInformation = licenseClientCacheService.getCurrentProductLicenseDetail();
       if (licenseSetupUtil.isSystemSetup() ) {
           if (licenseInformation!=null 
        		   && licenseInformation.getMaxConcurrentUsers() != -1 
        		   && licenseInformation.getMaxConcurrentUsers() <= userSessionManagerService.getAllLoggedInUsers().size()) {
            	throw new SessionAuthenticationException("label.license.concurrent.user.exceeds");
            }
       }

			
		this.httpSession = request.getSession(false);


		final List<SessionInformation> sessions = sessionRegistry
				.getAllSessions(authentication.getPrincipal(), false);
		
		
		//Removing sessionInformation from cache if sessionInformation exists in cache 
		//which can happen only if for some reason session is not removed from cache in case of SSO
		//for example: If we restart the server before logging out from the application 
		//this scenario will not occur in case of session replication 
		if(!sessions.isEmpty() && coreUtility.isSsoEnabled()){
			SessionInformation sessionInfo = sessions.get(0);
			String sessionId = sessionInfo.getSessionId();
			sessionRegistry.removeSessionInformation(sessionId);
			removeServiceTicketFromCache(sessionId);
			sessions.clear();
		}
		
		Object principal = authentication.getPrincipal();
		int oauthUsers = 0;
		if (principal instanceof UserInfo && tokenStore != null) {
			String userName = ((UserInfo) principal).getUsername();
			oauthUsers = ((CustomOauthTokenStoreDAO) tokenStore).findActiveTokensCountByUserName(userName);
		}
		int sessionCount = sessions.size() + oauthUsers;

		int allowedSessions = getMaximumSessionsForThisUser(authentication);

		if (sessionCount < allowedSessions) {
            clearForcedLoginSessionAttributes(httpSession);
			// They haven't got too many login sessions running at present
			return;
		}

		if (allowedSessions == -1) {
			// We permit unlimited logins
			return;
		}

		if (sessionCount == allowedSessions) {
			HttpSession session = request.getSession(false);

			if (session != null) {
				// Only permit it though if this request is associated with one
				// of the already registered sessions
				for (SessionInformation si : sessions) {
					if (si.getSessionId().equals(session.getId())) {
						return;
					}
				}
			}
			// If the session is null, a new one will be created by the parent
			// class, exceeding the allowed number
		}

		allowableSessionsExceeded(sessions, allowedSessions, sessionRegistry);
    }

	@Override
	public void allowableSessionsExceeded(List<SessionInformation> sessions,
			int allowableSessions, SessionRegistry registry)
			throws SessionAuthenticationException {
        if (exceptionIfMaximumExceeded || (sessions == null)) {
        	if(allowableSessions == 1 && httpSession != null){
        		httpSession.setAttribute(LoginConstants.SINGLE_USER_SESSION_EXCEEDED_FLAG, true);
        	}
            throw new SessionAuthenticationException(messages.getMessage("ConcurrentSessionControlStrategy.exceededAllowed",
                    new Object[] {Integer.valueOf(allowableSessions)},
                    "Maximum sessions of {0} for this principal exceeded"));
        }

        // Determine least recently used session, and mark it for invalidation
        SessionInformation leastRecentlyUsed = null;

        for (SessionInformation session : sessions) {
            if ((leastRecentlyUsed == null)
                    || session.getLastRequest().before(leastRecentlyUsed.getLastRequest())) {
                leastRecentlyUsed = session;
            }
        }

        leastRecentlyUsed.expireNow();
    }

	
	
	@Override
	public void setExceptionIfMaximumExceeded(boolean exceptionIfMaximumExceeded) {
        this.exceptionIfMaximumExceeded = exceptionIfMaximumExceeded;		
	}

	private void clearForcedLoginSessionAttributes(HttpSession session) {
		if(session != null){
			BaseLoggers.flowLogger.info("Removing session attribute values on forced login.");
			session.removeAttribute(LoginConstants.SESSION_USERNAME_PARAMETER);
			session.removeAttribute(LoginConstants.SESSION_PASSWORD_PARAMETER);
			session.removeAttribute(LoginConstants.SINGLE_USER_SESSION_EXCEEDED_FLAG);
		}				
	}
	
	
	private void removeServiceTicketFromCache(String sessionId){
		String mappingId = (String) sessionIdToServiceTicketCache.get(sessionId);
		sessionIdToServiceTicketCache.update(Action.DELETE, sessionId);
		
		if (mappingId != null) {
			serviceTicketToSessionCache.update(Action.DELETE, mappingId);
		}
	}
}
