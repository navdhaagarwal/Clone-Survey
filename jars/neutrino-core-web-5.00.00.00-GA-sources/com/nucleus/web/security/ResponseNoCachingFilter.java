package com.nucleus.web.security;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;

public class ResponseNoCachingFilter extends NeutrinoUrlExcludableFilter{

	public static final String RESPONSE_CACHE_DISABLED = "responseCacheDisabled";

	@Override
	public void initFilter(FilterConfig filterConfig) throws ServletException {
		// Nothing to initialize
	}
	
	@Override
	public void filter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		request.setAttribute(RESPONSE_CACHE_DISABLED, RESPONSE_CACHE_DISABLED);
		HttpServletResponse httpServletResponse = (HttpServletResponse) response;
		httpServletResponse.setHeader("Cache-Control", "no-cache, no-store, must-revalidate"); // HTTP
																								// 1.1.
		httpServletResponse.setHeader("Pragma", "no-cache"); // HTTP 1.0.
		httpServletResponse.setHeader("Expires", "0"); // Proxies.
		chain.doFilter(request, response);

	}



	@Override
	public void doDestroy() {
		// called when filter will be destroyed
	}

}
