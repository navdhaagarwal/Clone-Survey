package com.nucleus.web.security;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.web.RedirectStrategy;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.filter.GenericFilterBean;

import com.nucleus.finnone.pro.general.util.ValidatorUtils;
import com.nucleus.license.utils.LicenseSetupUtil;

import com.nucleus.web.security.SystemSetupUtil;

public class LicenseApplicationURLControlFilter extends GenericFilterBean {
	
	@Inject
	@Named(value = "systemSetupUtil")
	private SystemSetupUtil systemSetupUtil;
	@Inject
	@Named(value = "licenseSetupUtil")
	private LicenseSetupUtil licenseSetupUtil;
	
	@Value("${core.web.config.default.target.url.setup}")	
	private String targetUrlForSetup;
	
	@Value("${core.web.config.default.target.url.update.license}")	
	private String targetUrlForUpdateLicense;
	private Set<String> acceptedURL;	
	private List<AntPathRequestMatcher> excludedUriList = new ArrayList<>();
	private RedirectStrategy redirectStrategy ;

	public LicenseApplicationURLControlFilter() {
		super();
	}	

	

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		boolean isSystemSetup = systemSetupUtil.isSystemSetup();
		boolean isLicenseExpired = licenseSetupUtil.isLicenseExpired();
		if ((isSystemSetup && !isLicenseExpired) || isExcludedUri(request)) {
			chain.doFilter(request, response);
		}

		else if (isSystemSetup && isLicenseExpired) {
			redirectToApplyLicense(request, response, targetUrlForUpdateLicense);
		} else {

			redirectToApplyLicense(request, response, targetUrlForSetup);
		}
	}

	private void redirectToApplyLicense(ServletRequest request, ServletResponse response, String url)
			throws IOException {
		HttpServletRequest httpReq = (HttpServletRequest) request;
		HttpServletResponse httpResponse = (HttpServletResponse) response;
		redirectStrategy.sendRedirect(httpReq, httpResponse, url);
	}
	private void addAllUrlToList(Set<String> excludeForAllFiltersList) {
		if (!excludedUriList.isEmpty()){
			return;
		}
		if (ValidatorUtils.hasNoElements(excludeForAllFiltersList)) {
			return;
		}
		for (String uri : excludeForAllFiltersList) {
			excludedUriList.add(new AntPathRequestMatcher(uri));
		}
	}

	private boolean isExcludedUri(ServletRequest request) {
		addAllUrlToList(acceptedURL);
		for (AntPathRequestMatcher antPathRequestMatcher : excludedUriList) {
			if (antPathRequestMatcher.matches((HttpServletRequest) request)) {
				return true;
			}
		}
		return false;
	}

	
	
	
	public Set<String> getAcceptedURL() {
		return acceptedURL;
	}

	public void setAcceptedURL(Set<String> acceptedURL) {
		this.acceptedURL = acceptedURL;
	}
	
	public List<AntPathRequestMatcher> getExcludedUriList() {
		return excludedUriList;
	}
	public RedirectStrategy getRedirectStrategy() {
		return redirectStrategy;
	}

	public void setRedirectStrategy(RedirectStrategy redirectStrategy) {
		this.redirectStrategy = redirectStrategy;
	}
}
