package com.nucleus.web.security;

import java.io.IOException;
import java.security.MessageDigest;
import java.util.Base64;

import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.codec.digest.DigestUtils;

import com.nucleus.core.CustomSizeLinkedHashMap;
import com.nucleus.core.NeutrinoSpringAppContextUtil;
import com.nucleus.logging.BaseLoggers;
import com.nucleus.security.core.session.NeutrinoSessionInformation;
import com.nucleus.security.core.session.NeutrinoSessionRegistry;
import com.nucleus.user.UserInfo;
import com.nucleus.user.UserSessionManagerService;
import com.nucleus.web.common.CommonConfigUtility;

public class NeutrinoPageRefreshSecurityFilter extends NeutrinoUrlExcludableFilter {
	private Boolean pageRefreshFilterEnabled;

	private Integer urlToTimeStampMapSize;
	
	private UserSessionManagerService userSessionManagerService;

	private String logoutURL;

	@Inject
	@Named("sessionRegistry")
	private NeutrinoSessionRegistry sessionRegistry;

	private String ssoTicketValidatorUrl;
	
	private boolean isSameRefererUrl;
	
	private boolean isSSOActive;
	
	@Override
	public void initFilter(FilterConfig filterConfig) throws ServletException {
		NeutrinoPageRefreshSecurityFilterConfig neutrinoPageRefreshSecurityFilterConfig = NeutrinoSpringAppContextUtil
				.getBeanByType(NeutrinoPageRefreshSecurityFilterConfig.class);
		Boolean filterEnabled = neutrinoPageRefreshSecurityFilterConfig.getPageRefreshFilterEnabled();
		if (filterEnabled == null) {
			this.pageRefreshFilterEnabled = false;
			return;
		}
		this.userSessionManagerService = NeutrinoSpringAppContextUtil.getBeanByName("userSessionManagerService",
				UserSessionManagerService.class);
		this.sessionRegistry = NeutrinoSpringAppContextUtil.getBeanByName("sessionRegistry",
				NeutrinoSessionRegistry.class);
		CommonConfigUtility commonConfigUtility = NeutrinoSpringAppContextUtil.getBeanByName("commonConfigUtility",
				CommonConfigUtility.class);

		if (commonConfigUtility.getSsoActive()) {
			this.logoutURL = commonConfigUtility.getSsoLogoutURL();
			this.isSSOActive=true;
			this.ssoTicketValidatorUrl =commonConfigUtility.getSsoTicketValidatorUrl();
		} else {
			this.logoutURL = commonConfigUtility.getLogoutURL();
			this.isSSOActive=false;
			this.ssoTicketValidatorUrl ="";
		}
		
		this.pageRefreshFilterEnabled = filterEnabled;
		this.urlToTimeStampMapSize = neutrinoPageRefreshSecurityFilterConfig.getUrlToTimeStampMapSize();
	}

	@Override
	public void filter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {

		HttpServletRequest httpServletRequest=(HttpServletRequest) request;
		boolean urlValidationRequired = isUrlValidationRequired(this.pageRefreshFilterEnabled,this.isSSOActive, 
																httpServletRequest,this.ssoTicketValidatorUrl);
		if(urlValidationRequired  && isUserLoggedin() ){
			validateTimeStampForURL(httpServletRequest, (HttpServletResponse) response);
		}
		
		chain.doFilter(request, response);

	}

	private boolean isUserLoggedin() {
		boolean isUserLoggedin = false;
		UserInfo user = userSessionManagerService.getLoggedinUserInfo();
		if (user != null) {
			isUserLoggedin = true;
		}
		return isUserLoggedin;
	}

	private void validateTimeStampForURL(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse)
			throws IOException {
		HttpSession session = httpServletRequest.getSession(false);
		if (session != null) {
			synchronized (session) {
				Long requestTimeStamp = getRequestTimeMillis(httpServletRequest, httpServletResponse);

				if (!ResponseNoCachingFilter.RESPONSE_CACHE_DISABLED
						.equals(httpServletRequest.getAttribute(ResponseNoCachingFilter.RESPONSE_CACHE_DISABLED))) {
					// If no cache filter is disabled but
					// PageRefreshSecurityFilter is enabled, logout the user
					performLogout(httpServletRequest, httpServletResponse,
							NeutrinoSessionInformation.LOGOUT_TYPE_BY_INVALID_CONFIGURATION);
				} else if (requestTimeStamp == null) {
					// If the request does not contain a timestamp or the
					// timestamp has been manipulated, logout the
					// user
					performLogout(httpServletRequest, httpServletResponse,
							NeutrinoSessionInformation.LOGOUT_TYPE_BY_MALICIOUS_URL_ACCESS);
				} else {
					performTimeStampValidation(session, httpServletRequest, httpServletResponse, requestTimeStamp);
				}
			}
		}
	}

	@SuppressWarnings("unchecked")
	private void performTimeStampValidation(HttpSession session, HttpServletRequest httpServletRequest,
			HttpServletResponse httpServletResponse, Long requestTimeStamp) throws IOException {
		String hkstd = getParameterHash(httpServletRequest);
		NeutrinoSessionInformation neutrinoSessionInformation = (NeutrinoSessionInformation)sessionRegistry.getSessionInformation(session.getId());
		if (hkstd != null && neutrinoSessionInformation!=null) {
			CustomSizeLinkedHashMap<String, Long> urlToTimeStampMap = neutrinoSessionInformation.getUrlToTimeStampMap();
			String requestURIWithHKSTD = httpServletRequest.getRequestURI() + hkstd;
			if (urlToTimeStampMap == null) {
				// first request, create map and add new entry
				urlToTimeStampMap = new CustomSizeLinkedHashMap<>(urlToTimeStampMapSize);
				neutrinoSessionInformation.setUrlToTimeStampMap(urlToTimeStampMap);
			}
			Long existingTimeStamp = urlToTimeStampMap.get(requestURIWithHKSTD);
			if (existingTimeStamp == null || existingTimeStamp.compareTo(requestTimeStamp) < 0) {
				// if existingTimeStamp is null, i.e., a new URI request for
				// this session, it needs to be added to map
				// If the request timestamp is more than the already
				// existing
				// timestamp for a particular URI, we override the existing
				// value against requestURIWithHKSCD
				urlToTimeStampMap.put(requestURIWithHKSTD, requestTimeStamp);
			} else if (existingTimeStamp.compareTo(requestTimeStamp) == 0
					|| existingTimeStamp.compareTo(requestTimeStamp) > 0) {
				// if the request timestamp is same, i.e, user is trying to
				// open a particular url again, we logout the user

				// If the request timestamp is less than the already
				// existing timestamp for a particular URI, we logout the user
				performLogout(httpServletRequest, httpServletResponse,
						NeutrinoSessionInformation.LOGOUT_TYPE_BY_PAGE_REFRESH);
			}
		}

	}

	private void performLogout(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse,
			String logOutType) throws IOException {
		
		String hkstd = getParameterHash(httpServletRequest);
		HttpSession session = httpServletRequest.getSession(false);
		String currentUri = httpServletRequest.getRequestURI();
		BaseLoggers.flowLogger.error("Current Url during perform logout  [ "+currentUri+" ]");

		if (hkstd != null && session != null) {
			NeutrinoSessionInformation neutrinoSessionInformation = (NeutrinoSessionInformation) sessionRegistry
					.getSessionInformation(session.getId());
			CustomSizeLinkedHashMap<String, Long> urlToTimeStampMap = neutrinoSessionInformation.getUrlToTimeStampMap();
			String requestURIWithHKSTD = currentUri + hkstd;
			Long existingTimeStamp = urlToTimeStampMap.get(requestURIWithHKSTD);
			BaseLoggers.flowLogger.error("Existing time stamp during perform logout  [ "+existingTimeStamp+" ]");
		}
		
		BaseLoggers.flowLogger.error("Logged-in user is going to logout.Logout type : "+logOutType);

		/*NeutrinoSessionInformation neutrinoSessionInformation = (NeutrinoSessionInformation) sessionRegistry
				.getSessionInformation(httpServletRequest.getSession(false).getId());
		neutrinoSessionInformation.setLogOutType(logOutType);
		sessionRegistry.updatRegisteredSession(neutrinoSessionInformation);
		httpServletResponse.sendRedirect(httpServletRequest.getContextPath() + logoutURL);*/
	}

	private static String getParameterHash(HttpServletRequest request) {

		String parameterHash = request.getHeader(NeutrinoUrlValidatorFilter.SECURITY_TOKEN);
		if (parameterHash == null) {
			parameterHash = request.getParameter(NeutrinoUrlValidatorFilter.SECURITY_TOKEN);
		}
		if (parameterHash != null) {
			parameterHash = parameterHash.substring(0, 32);
		}
		return parameterHash;
	}

	private Long getRequestTimeMillis(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse)
			throws IOException {
		String parameterHash = httpServletRequest.getHeader(NeutrinoUrlValidatorFilter.SECURITY_TOKEN);
		if (parameterHash == null) {
			parameterHash = httpServletRequest.getParameter(NeutrinoUrlValidatorFilter.SECURITY_TOKEN);
		}
		if (parameterHash == null) {
			return null;
		}
		try {
			String requestTimeMillisEncoded = parameterHash.substring(64);
			if (requestTimeMillisEncoded == null) {
				return null;
			}
			String requestTimeMillisString = new String(Base64.getDecoder().decode(requestTimeMillisEncoded));
			String requestTimestampHash = parameterHash.substring(32, 64);
			String calculatedTimeStampHash = DigestUtils.sha256Hex(requestTimeMillisString);
			if(MessageDigest.isEqual(requestTimestampHash.getBytes(),calculatedTimeStampHash.getBytes())) {
				return Long.parseLong(requestTimeMillisString);
			}

		} catch (IllegalArgumentException | IndexOutOfBoundsException e) {
			// these exceptions may occur if the URl is tampered, in such
			// cases we logout the user
			BaseLoggers.flowLogger.error(e.getMessage(), e);
			performLogout(httpServletRequest, httpServletResponse,
					NeutrinoSessionInformation.LOGOUT_TYPE_BY_MALICIOUS_URL_ACCESS);
		}
		return null;
	}

	@Override
	public void doDestroy() {
		// called when filter will be destroyed
	}
}
