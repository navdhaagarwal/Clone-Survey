package com.nucleus.web.security;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;
import org.springframework.util.Assert;

import com.nucleus.api.security.APISecurityService;
import com.nucleus.security.core.session.NeutrinoSessionInformation;
import com.nucleus.security.core.session.NeutrinoSessionRegistry;
import com.nucleus.web.csrf.CSRFTokenManager;


public class CustomRegisterSessionAuthenticationStrategy implements SessionAuthenticationStrategy {
	private final SessionRegistry sessionRegistry;

	private APISecurityService apiSecurityService;
	/**
	 * @param sessionRegistry
	 *            the session registry which should be updated when the
	 *            authenticated session is changed.
	 */
	public CustomRegisterSessionAuthenticationStrategy(SessionRegistry sessionRegistry, APISecurityService apiSecurityService) {
		Assert.notNull(sessionRegistry, "The sessionRegistry cannot be null");
		this.sessionRegistry = sessionRegistry;
		this.apiSecurityService = apiSecurityService;
	}

	/**
	 * In addition to the steps from the superclass, the sessionRegistry will be
	 * updated with the new session information.
	 */
	public void onAuthentication(Authentication authentication, HttpServletRequest request,
			HttpServletResponse response) {
		String sessionId = request.getSession().getId();
		sessionRegistry.registerNewSession(sessionId, authentication.getPrincipal());
		
		if(StringUtils.isEmpty((String)request.getSession().getAttribute("CSRF_TOKEN_FOR_SESSION_ATTR_NAME"))) {
			CSRFTokenManager.getTokenForSession(request);
		}
		List<String> proxySecurityKey = apiSecurityService.putSecurityKeysInCache(request.getSession());
		request.getSession(false).setAttribute("proxySecurityKeyList", proxySecurityKey);
		String requestIPaddress = request.getRemoteAddr();
		NeutrinoSessionInformation neutrinoSessionInformation = (NeutrinoSessionInformation) sessionRegistry
				.getSessionInformation(sessionId);
		if (neutrinoSessionInformation != null) {
			if (request.getParameter("ticket") != null) {
				neutrinoSessionInformation.setServiceTicketId(request.getParameter("ticket"));
			}
			neutrinoSessionInformation.setLoginIp(requestIPaddress);
			((NeutrinoSessionRegistry) sessionRegistry).updatRegisteredSession(neutrinoSessionInformation);
		}

	}
}
