package com.nucleus.web.security;

import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.springframework.security.core.session.SessionInformation;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.web.session.ConcurrentSessionFilter;
import org.springframework.security.web.session.SessionInformationExpiredStrategy;

import com.nucleus.finnone.pro.general.util.ValidatorUtils;
import com.nucleus.logging.BaseLoggers;
import com.nucleus.security.core.session.NeutrinoSessionInformation;
import com.nucleus.user.UserService;

/**
 * 
 * @deprecated as determineExpiredUrl no longer being called on session
 *             expiration in default implementation, also determineExpiredUrl is
 *             depricated. Same functionality can be acheived by implementing
 *             {@link SessionInformationExpiredStrategy} so we have added
 *             {@link NeutrinoSessionInformationExpiredStrategy}
 *
 */
@Deprecated
public class CustomConcurrentSessionFilter extends ConcurrentSessionFilter {

	private SessionRegistry sessionRegistry;
	
    @Inject
    @Named(value = "systemSetupUtil")
    private SystemSetupUtil systemSetupUtil;
    
    @Inject
    @Named("userService")
    private UserService               userService;
    
    private static final String 		 AUTHENTICATION_SUCCESS 	   = "onAuthenticationSuccess";
    
	public CustomConcurrentSessionFilter(SessionRegistry sessionRegistry) {
	    super(sessionRegistry);
	    this.sessionRegistry=sessionRegistry;
	}
	 
	@Override
	protected String determineExpiredUrl(HttpServletRequest request,
			SessionInformation info) {
		HttpSession session = request.getSession(false);
        if(session!=null && info!=null){
    		sessionRegistry.removeSessionInformation(session.getId());
        }

		String expiredUrl = request.getServletPath() + request.getPathInfo();
		String targetUrl = null;
		if (expiredUrl.contains(systemSetupUtil.getResetPasswordUrl())) {
			targetUrl = expiredUrl;
			request.getSession().setAttribute(AUTHENTICATION_SUCCESS, Boolean.TRUE);
			request.getSession().setAttribute("eligibleForDirectResetPassword", Boolean.TRUE);
		} else if (expiredUrl.contains(systemSetupUtil.getLicenseAgreementUrl())) {
			request.getSession().setAttribute(AUTHENTICATION_SUCCESS, Boolean.TRUE);
			targetUrl = expiredUrl;
		} else {

			// log no cookie..
			Cookie[] cookies = request.getCookies();
			if (cookies == null || cookies.length == 0) {
				BaseLoggers.exceptionLogger.info("User has either cleared cookies on browser or user this application "
						+ "for first time. No cookies received with request originated from IP:"
						+ request.getRemoteAddr());
			}
			targetUrl = systemSetupUtil.getCustomConcurrentSessionFilterExpiredUrl();
			if (ValidatorUtils.notNull(info)) {
				return validateAndPrepareTargetUrlForForcedLogout(info, targetUrl);

			}

		}
		return targetUrl;
	}
	

	public String validateAndPrepareTargetUrlForForcedLogout(
			SessionInformation info, String targetUrl) {

		NeutrinoSessionInformation information = (NeutrinoSessionInformation) info;
		
		if (NeutrinoSessionInformation.LOGOUT_TYPE_ON_DIFF_DEVICE_BROWSER
				.equalsIgnoreCase(information.getLogOutType())) {
			
			targetUrl = targetUrl.concat("&logoutByAdmin=false&forcedLogoutIP=" + information.getForceLogOutIP());
		}
		if (NeutrinoSessionInformation.LOGOUT_TYPE_BY_ADMIN.equalsIgnoreCase(information.getLogOutType())) {
			String loggedOutBY=userService.getUserNameByUserId(information.getLogOutBy());
			targetUrl = targetUrl.concat("&logoutByAdmin=true&loggedOutBY="+loggedOutBY+"&forcedLogoutIP=" + information.getForceLogOutIP());
		}
		return targetUrl;
	}
	
	
}
