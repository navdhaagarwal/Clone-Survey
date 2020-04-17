package com.nucleus.web.util;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.web.filter.GenericFilterBean;

public class CustomMatcherGenericFilter extends GenericFilterBean{

	
	private RequestMatcher requestMatcher;
	
	public RequestMatcher getRequestMatcher() {
		return requestMatcher;
	}

	public void setRequestMatcher(GenericRequestMatcher requestMatcher) {
		this.requestMatcher = requestMatcher;
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		if(requestMatcher!=null && requestMatcher.matches((HttpServletRequest) request)){
			filterInternal(request, response, chain);
		}else{
			chain.doFilter(request, response);
		}
		
	}
	
	
	public void filterInternal(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException{
		chain.doFilter(request, response);
	}

}
