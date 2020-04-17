package com.nucleus.web.security;

import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.security.web.authentication.logout.SimpleUrlLogoutSuccessHandler;

import com.nucleus.security.core.session.NeutrinoSessionInformation;

public class CustomUrlLogoutSuccessHandler extends SimpleUrlLogoutSuccessHandler {

	@Inject
	@Named(value = "systemSetupUtil")
	private SystemSetupUtil systemSetupUtil;

	@Override
	protected String determineTargetUrl(HttpServletRequest request, HttpServletResponse response) {
		if (NeutrinoSessionInformation.LOGOUT_TYPE_BY_PAGE_REFRESH
				.equals(request.getAttribute(NeutrinoSessionInformation.LOGOUT_TYPE_BY_PAGE_REFRESH))
				|| NeutrinoSessionInformation.LOGOUT_TYPE_BY_MALICIOUS_URL_ACCESS
						.equals(request.getAttribute(NeutrinoSessionInformation.LOGOUT_TYPE_BY_MALICIOUS_URL_ACCESS))) {
			HttpSession session = request.getSession();
			session.setAttribute(NeutrinoSessionInformation.LOGOUT_TYPE_BY_PAGE_REFRESH, NeutrinoSessionInformation.LOGOUT_TYPE_BY_PAGE_REFRESH);
		} else if (NeutrinoSessionInformation.LOGOUT_TYPE_BY_INVALID_CONFIGURATION
				.equals(request.getAttribute(NeutrinoSessionInformation.LOGOUT_TYPE_BY_INVALID_CONFIGURATION))) {
			HttpSession session = request.getSession();
			session.setAttribute(NeutrinoSessionInformation.LOGOUT_TYPE_BY_INVALID_CONFIGURATION, NeutrinoSessionInformation.LOGOUT_TYPE_BY_INVALID_CONFIGURATION);
		}
		return systemSetupUtil.getLogoutSuccessTargetUrl();
	}

}
