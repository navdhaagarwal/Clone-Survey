package com.nucleus.web.security;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.Date;

import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.session.SessionInformation;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.web.RedirectStrategy;
import org.springframework.security.web.session.SessionInformationExpiredEvent;
import org.springframework.security.web.session.SessionInformationExpiredStrategy;

import com.nucleus.finnone.pro.general.util.ValidatorUtils;
import com.nucleus.logging.BaseLoggers;
import com.nucleus.security.core.session.AESEncryptionWithStaticKey;
import com.nucleus.security.core.session.NeutrinoSessionInformation;
import com.nucleus.user.UserService;

/**
 * 
 * @author gajendra.jatav
 *
 */
public class NeutrinoSessionInformationExpiredStrategy implements SessionInformationExpiredStrategy {

	private SessionRegistry sessionRegistry;
	
    @Inject
    @Named(value = "systemSetupUtil")
    private SystemSetupUtil systemSetupUtil;
    
    @Inject
    @Named("userService")
    private UserService               userService;
    
    @Value(value = "#{'${core.web.config.SSO.request.encryption.key}'}")
	private  String ssoEncryptionKey;
    
    @Value(value = "${max.message.valid.time:30000}")
    private String messageValidTime;
    
    private static final String 		 AUTHENTICATION_SUCCESS 	   = "onAuthenticationSuccess";
    
    private RedirectStrategy redirectStrategy;
    

    public SessionRegistry getSessionRegistry() {
		return sessionRegistry;
	}


	public void setSessionRegistry(SessionRegistry sessionRegistry) {
		this.sessionRegistry = sessionRegistry;
	}


	public RedirectStrategy getRedirectStrategy() {
		return redirectStrategy;
	}


	public void setRedirectStrategy(RedirectStrategy redirectStrategy) {
		this.redirectStrategy = redirectStrategy;
	}


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
				BaseLoggers.exceptionLogger.error("User has either cleared cookies on browser or user this application "
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
		
		if(StringUtils.isNotEmpty(information.getLogoutMessage())) {
			targetUrl = appendLogoutMessage(targetUrl, information);
		}
		return targetUrl;
	}

	@Override
	public void onExpiredSessionDetected(SessionInformationExpiredEvent event) throws IOException, ServletException {
		
		
		HttpServletRequest request=event.getRequest();
		SessionInformation info=event.getSessionInformation();
		String targetUrl=determineExpiredUrl(request,info);

	
		if (targetUrl != null) {
			redirectStrategy.sendRedirect(request, event.getResponse(), targetUrl);

			return;
		}
		else {
			event.getResponse().getWriter().print(
					"This session has been expired (possibly due to multiple concurrent "
							+ "logins being attempted as the same user).");
			event.getResponse().flushBuffer();
		}

		return;
	}
	
	private String appendLogoutMessage(String targetUrl, NeutrinoSessionInformation information) {
		try {
				Date date = new Date();
				long validTimeSpan = date.getTime()+Long.parseLong(messageValidTime);
				String logoutMessage = AESEncryptionWithStaticKey.encrypt(information.getLogoutMessage() +":"+ validTimeSpan, ssoEncryptionKey.getBytes(Charset.forName("UTF-8")));
				String encodedMessage = URLEncoder.encode(logoutMessage, "UTF-8");
				targetUrl = targetUrl.concat("&message="+encodedMessage);
		} catch (Exception e) {
			BaseLoggers.exceptionLogger.error("Error while creating redirect URL", e);
		}
		
		return targetUrl;
	}
	
}
