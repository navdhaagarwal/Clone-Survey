package com.nucleus.web.security;

import java.io.IOException;
import java.net.URLEncoder;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jadira.usertype.spi.utils.lang.StringUtils;
import org.jasig.cas.client.util.CommonUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.security.cas.ServiceProperties;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.util.Assert;

import com.nucleus.logging.BaseLoggers;


/**
 * 
 * This is the custom implementation of CasAuthenticationEntryPoint.
 * The user will be redirected to SSO URL with message parameter appended 
 * to the URL . 
 * 
 * @author namrata.varshney
 *
 */
public class CustomCasAuthenticationEntryPoint implements AuthenticationEntryPoint,InitializingBean {
	
	private ServiceProperties serviceProperties;

	private String loginUrl;

	private boolean encodeServiceUrlWithSessionId = true;

	
	@Override
	public void afterPropertiesSet() throws Exception {
		Assert.hasLength(this.loginUrl, "loginUrl must be specified");
		Assert.notNull(this.serviceProperties, "serviceProperties must be specified");
		Assert.notNull(this.serviceProperties.getService(),
				"serviceProperties.getService() cannot be null.");
	}
	

	@Override
	public void commence(HttpServletRequest servletRequest, HttpServletResponse response,
			AuthenticationException authException) throws IOException, ServletException {
		String urlEncodedService = createServiceUrl(servletRequest, response);
		String redirectUrl = createRedirectUrl(urlEncodedService);

		redirectUrl = preCommence(servletRequest, redirectUrl);

		response.sendRedirect(redirectUrl);
	}
	
	@SuppressWarnings("deprecation")
	public String createServiceUrl(HttpServletRequest request,
			HttpServletResponse response) {
		return CommonUtils.constructServiceUrl(null, response,
				this.serviceProperties.getService(), null,
				this.serviceProperties.getArtifactParameter(),
				this.encodeServiceUrlWithSessionId);
	}
	
	public String createRedirectUrl(String serviceUrl) {
		return CommonUtils.constructRedirectUrl(this.loginUrl,
				this.serviceProperties.getServiceParameter(), serviceUrl,
				this.serviceProperties.isSendRenew(), false);
	}

	/**
	 * 
	 * concatenate the logout message in the redirect url 
	 * @param request
	 * @param redirectUrl
	 * @return
	 */
	protected String preCommence(HttpServletRequest request, String redirectUrl) {
		try {
			String logoutMessage = request.getParameter("message");
			if(StringUtils.isNotEmpty(logoutMessage)) {
				String encodedMessage = URLEncoder.encode(logoutMessage, "UTF-8");
				redirectUrl = redirectUrl.concat("&message="+encodedMessage);
			}
		}catch(Exception e) {
			BaseLoggers.exceptionLogger.error("Error while creating redirect URL", e);
		}
		return redirectUrl;
	}

	
	public String getLoginUrl() {
		return this.loginUrl;
	}

	public ServiceProperties getServiceProperties() {
		return this.serviceProperties;
	}

	public void setLoginUrl(String loginUrl) {
		this.loginUrl = loginUrl;
	}

	public void setServiceProperties(ServiceProperties serviceProperties) {
		this.serviceProperties = serviceProperties;
	}

	/**
	 * Sets whether to encode the service url with the session id or not.
	 *
	 * @param encodeServiceUrlWithSessionId whether to encode the service url with the
	 * session id or not.
	 */
	public void setEncodeServiceUrlWithSessionId(
			boolean encodeServiceUrlWithSessionId) {
		this.encodeServiceUrlWithSessionId = encodeServiceUrlWithSessionId;
	}

	/**
	 * Sets whether to encode the service url with the session id or not.
	 * @return whether to encode the service url with the session id or not.
	 *
	 */
	protected boolean getEncodeServiceUrlWithSessionId() {
		return this.encodeServiceUrlWithSessionId;
	}


}
