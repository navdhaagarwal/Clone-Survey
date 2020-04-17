package com.nucleus.web.security;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.web.DefaultRedirectStrategy;

import com.nucleus.web.csrf.CSRFTokenManager;
/**
 * 
 * Adds security token to location header 
 * 
 * @author gajendra.jatav
 *
 */
public class NeutrinoRedirectStrategy extends DefaultRedirectStrategy{
	
	private static final int CUSTOM_SESSION_EXPIRED_ERROR_CODE = 901;

	@Override
	public void sendRedirect(HttpServletRequest request, HttpServletResponse response, String url) throws IOException {

		NeutrinoResponseWrapper neutrinoResponseWrapper;

		String ajaxHeader = request.getHeader("X-Requested-With");

		if ("XMLHttpRequest".equals(ajaxHeader) && url.contains("ERR.INVALIDSESSION.MSG")) {
			if(url.contains("message")) {
				response.setHeader("appendedErrorParam", url.substring(url.indexOf("message")));
			}
			response.sendError(CUSTOM_SESSION_EXPIRED_ERROR_CODE);
		} else {
			if (NeutrinoResponseWrapper.class.isAssignableFrom(response.getClass())) {
				neutrinoResponseWrapper = (NeutrinoResponseWrapper) response;
			} else {
				neutrinoResponseWrapper = new NeutrinoResponseWrapper(response);
			}
			String csrfToken = CSRFTokenManager.getTokenForSession(request);
			neutrinoResponseWrapper.setCsrfToken(csrfToken);
			super.sendRedirect(request, neutrinoResponseWrapper, url);
		}
	}
}
