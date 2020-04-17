package com.nucleus.web.util;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.util.Assert;

/**
 * 
 * @author gajendra.jatav
 *
 */
public class GenericRequestMatcher implements RequestMatcher{

	private List<AntPathRequestMatcher> excludedPatters = new ArrayList<>();
	
	private List<AntPathRequestMatcher> includedPatterns=new ArrayList<>();

	@Override
	public boolean matches(HttpServletRequest request) {
		for(AntPathRequestMatcher matcher:includedPatterns){
			if(!matcher.matches(request)){
				return false;
			}
		}
		for(AntPathRequestMatcher matcher:excludedPatters){
			if(matcher.matches(request)){
				return false;
			}
		}
		return true;
	}

	
	public void setExcludedPatters(List<String>  patterns) {
		Assert.notEmpty(patterns, "'patterns' must not be empty");
		for (String pattern: patterns) {
			excludedPatters.add(new AntPathRequestMatcher(pattern));
		}
	}
	
	public void setIncludedPatterns(List<String>  patterns) {
		Assert.notEmpty(patterns, "'patterns' must not be empty");
		for (String pattern: patterns) {
			includedPatterns.add(new AntPathRequestMatcher(pattern));
		}
	}
}
