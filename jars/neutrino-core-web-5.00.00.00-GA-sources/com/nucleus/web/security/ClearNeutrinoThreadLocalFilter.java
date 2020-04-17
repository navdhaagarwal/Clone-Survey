package com.nucleus.web.security;

import java.io.IOException;

import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.filter.OncePerRequestFilter;

import com.nucleus.standard.context.INeutrinoExecutionContextHolder;

public class ClearNeutrinoThreadLocalFilter extends OncePerRequestFilter {
	
	@Inject
	@Named("neutrinoExecutionContextHolder")
	INeutrinoExecutionContextHolder neutrinoExecutionContextHolder;
	
	
	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		
		neutrinoExecutionContextHolder.clearGlobalContext();
		neutrinoExecutionContextHolder.clearLocalContext();

		filterChain.doFilter(request, response);
		
		neutrinoExecutionContextHolder.clearGlobalContext();
		neutrinoExecutionContextHolder.clearLocalContext();
	}

}
