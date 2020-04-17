package com.nucleus.web.apimgmt.filter;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

public class ApiAuditAndLoggingFilter implements Filter{

	private String primaryServerForLogging;

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		//Required implementation : Hence empty. 
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
			
		chain.doFilter(request, response);
		
	}

	@Override
	public void destroy() {
		//Required implementation : Hence empty.
	}

	public String getPrimaryServerForLogging() {
		return primaryServerForLogging;
	}

	public void setPrimaryServerForLogging(String primaryServerForLogging) {
		this.primaryServerForLogging = primaryServerForLogging;
	}

}
