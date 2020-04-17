package com.nucleus.web.security;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.filter.GenericFilterBean;

import com.nucleus.api.security.APISecurityService;
import com.nucleus.core.initialization.ProductInformationLoader;
import com.nucleus.core.jwt.util.JotUtil;
import com.nucleus.logging.BaseLoggers;
import com.nucleus.user.UserInfo;
import com.nucleus.user.UserSecurityService;

/**
 * This filter is responsible for extracting access token and username.
 * 
 */
public class APISecurityFilter extends GenericFilterBean {

	@Inject
	@Named("userSecurityService")
	private UserSecurityService userSecurityService;

	@Inject
	@Named("apiSecurityService")
	APISecurityService apiSecurityService;

	private Set<String> acceptedUrls = new HashSet<>();
	private Set<String> excludedUrls = new HashSet<>();

	private List<AntPathRequestMatcher> acceptedUrlMatcherList = new ArrayList<>();
	private List<AntPathRequestMatcher> excludedUrlMatcherList = new ArrayList<>();

	private Boolean isApiManagerEnabled = null;

	private static final String UNAUTHORIZED_MESSAGE = "Request is not authorized for given resource access.";

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain)
			throws IOException, ServletException {
		HttpServletRequest httpRequest = (HttpServletRequest) request;
		
		String cookieId = getCrossOriginCookie(httpRequest);
		if(!StringUtils.isEmpty(cookieId) && isFilterationRequired(httpRequest)) {
			String csrfToken = httpRequest.getHeader("CSRFToken");
			
			if(StringUtils.isEmpty(csrfToken) || !apiSecurityService.checkCsrfToken(cookieId, csrfToken)) {
				sendErrorResponse((HttpServletResponse) response, HttpStatus.UNAUTHORIZED, UNAUTHORIZED_MESSAGE);
				BaseLoggers.exceptionLogger.error(
						"Attempt for unauthorized resource access was done for uri: {}.The CSRF token passed is empty or invalid.",
						httpRequest.getRequestURI());
				return;
			}
			filterChain.doFilter(httpRequest, response);
			return;
		}

		/* DO not remove this IF block. This is important */
		if (isApiManagerEnabled == null && !isApiManagerEnabled()) {
			BaseLoggers.flowLogger.debug("API manager is not enabled.");
		}

		if (!isApiManagerEnabled) {
			filterChain.doFilter(httpRequest, response);
		} else {
			if (isFilterationRequired(httpRequest) && !isFilterationForExclusion(httpRequest)) {
				String headerVal = httpRequest.getHeader("payload");
				boolean errorResponse = headerVal == null;
				Map headerMap = null;
				if (!errorResponse) {
					Map<String, String> encryptedHeaders = new HashMap<>();
					encryptedHeaders.put("payload", headerVal);
					headerMap = JotUtil.decrypt("encryptedHeaders", encryptedHeaders, Map.class);
					errorResponse = headerMap == null;
				}

				if (errorResponse) {
					sendErrorResponse((HttpServletResponse) response, HttpStatus.UNAUTHORIZED, UNAUTHORIZED_MESSAGE);
					BaseLoggers.exceptionLogger.error(
							"Attempt for unauthorized resource access was done for uri: {}  . It may because of request is not coming via NIF layer. Please check.",
							httpRequest.getRequestURI());
					return;
				}
				String accessToken = (String) headerMap.get("accessToken");
				String username = (String) headerMap.get("username");
				String trustedSourceName = (String) headerMap.get("trustedSourceName");
				httpRequest.setAttribute("trustedSourceName", trustedSourceName);
				String apiCode = (String) headerMap.get("apiCode");
				if (accessToken != null && trustedSourceName != null) {
					if (username != null) {
						// user info is not null.
						UserInfo userInfo = userSecurityService.getCompleteUserFromUsername(username);
						if (userInfo != null) {
							SecurityContextHolder.getContext().setAuthentication(
									new UsernamePasswordAuthenticationToken(userInfo, null, userInfo.getAuthorities()));
						}
						httpRequest.setAttribute("passPhrase", headerMap.get("passPhrase"));
						httpRequest.setAttribute("access_token", accessToken);
					}
					BaseLoggers.flowLogger.error(
							"Successful resource access was done for uri: {} , trustedSource: {} and apiCode: {}",
							httpRequest.getRequestURI(), trustedSourceName, apiCode);
				} else {
					BaseLoggers.exceptionLogger.error(UNAUTHORIZED_MESSAGE + httpRequest.getRequestURI());
					sendErrorResponse((HttpServletResponse) response, HttpStatus.UNAUTHORIZED, UNAUTHORIZED_MESSAGE);
					return;
				}

			}
			filterChain.doFilter(httpRequest, response);
		}
	}

	private boolean isApiManagerEnabled() {
		Environment environment = getEnvironment();
		String[] defaultProfiles = environment.getDefaultProfiles();
		String[] activeProfiles = environment.getActiveProfiles();

		// check if profile is api-manager-enabled then this filter is not required.
		if (defaultProfiles != null && defaultProfiles.length > 0
				&& (Arrays.asList(defaultProfiles).contains("api-manager-enabled"))
				|| (activeProfiles != null && activeProfiles.length > 0
						&& (Arrays.asList(activeProfiles).contains("api-manager-enabled")))) {
			isApiManagerEnabled = Boolean.TRUE;
		} else {
			isApiManagerEnabled = Boolean.FALSE;
		}
		return isApiManagerEnabled;
	}

	private boolean isFilterationRequired(HttpServletRequest request) {
		addAcceptedAllUrlToList();
		for (AntPathRequestMatcher antPathRequestMatcher : acceptedUrlMatcherList) {
			if (antPathRequestMatcher.matches(request)) {
				return true;
			}
		}
		return false;
	}

	private boolean isFilterationForExclusion(HttpServletRequest request) {
		addExcludedAllUrlToList();
		for (AntPathRequestMatcher antPathRequestMatcher : excludedUrlMatcherList) {
			if (antPathRequestMatcher.matches(request)) {
				return true;
			}
		}
		return false;
	}

	private void addAcceptedAllUrlToList() {
		if (!acceptedUrlMatcherList.isEmpty()) {
			return;
		}
		for (String acceptedUri : acceptedUrls) {
			acceptedUrlMatcherList.add(new AntPathRequestMatcher(acceptedUri));
		}
	}

	private void addExcludedAllUrlToList() {
		if (!excludedUrlMatcherList.isEmpty()) {
			return;
		}
		for (String excludedUri : excludedUrls) {
			excludedUrlMatcherList.add(new AntPathRequestMatcher(excludedUri));
		}
	}

	private void sendErrorResponse(HttpServletResponse httpResponse, HttpStatus httpStatus, String detailedMessage) {
		try {
			httpResponse.setStatus(httpStatus.value());
			httpResponse.getOutputStream().write(detailedMessage.getBytes());
		} catch (IOException e) {
			BaseLoggers.exceptionLogger.error("Error while writing respose for unauthorized request.");
		}
	}

	private String getCrossOriginCookie(HttpServletRequest request) {
		
		Cookie[] cookies = request.getCookies();
		
		if(cookies==null) {
			return null;
		}
		
		String cookieId=null;
		String expectedCookieName = ProductInformationLoader.getProductName()+"_SECURITY";
		
		for(Cookie cookie: cookies){
			String cookieName = cookie.getName();
			if(expectedCookieName.equals(cookieName)){
				cookieId = cookie.getValue();
				break;
			}
		}
		
		return cookieId;
	}
	
	public Set<String> getAcceptedUrls() {
		return acceptedUrls;
	}

	public void setAcceptedUrls(Set<String> acceptedUrls) {
		this.acceptedUrls = acceptedUrls;
	}

	public Set<String> getExcludedUrls() {
		return excludedUrls;
	}

	public void setExcludedUrls(Set<String> excludedUrls) {
		this.excludedUrls = excludedUrls;
	}

}
