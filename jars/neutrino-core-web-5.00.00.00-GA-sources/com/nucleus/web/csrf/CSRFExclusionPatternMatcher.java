package com.nucleus.web.csrf;

import java.util.List;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.util.Assert;


public class CSRFExclusionPatternMatcher implements RequestMatcher {

	private Pattern allowedMethods = Pattern
			.compile("^(GET|HEAD|TRACE|OPTIONS)$");

	private AntPathRequestMatcher[] unprotectedMatcher = null;

	private List<List<String>> patternsList;
	
	/*
	 * { new AntPathRequestMatcher("/ws/**") };
	 */

	
	
	@Override
	public boolean matches(HttpServletRequest request) {

		if (allowedMethods.matcher(request.getMethod()).matches()) {
			return false;
		} else {
			for (AntPathRequestMatcher rm : unprotectedMatcher) {
				if (rm.matches(request)) {
					return false;
				}
			}

			return true;
		}
	}

	public List<List<String>> getPatternsList() {
		return patternsList;
	}

	public void setPatterns(String... patterns) {
		Assert.notEmpty(patterns, "'patterns' must not be empty");
		if(unprotectedMatcher==null)
		{
			unprotectedMatcher=new AntPathRequestMatcher[patterns.length];
		}
		for (int i = 0; i < patterns.length; i++) {
			unprotectedMatcher[i]=new AntPathRequestMatcher(patterns[i]);
		}
		
	}
	
	public void setPatternsList(List<List<String>> patternsList) {
		Assert.notEmpty(patternsList, "'patterns' must not be empty");
		int size=0;
		for(List<String> patterns:patternsList)
		{			
			size=size+patterns.size();
		}

		if(unprotectedMatcher==null)
		{
			unprotectedMatcher=new AntPathRequestMatcher[size];
		}
		int i=0;
		for(List<String> patterns:patternsList)
		{
			for (String pattern:patterns) {
				unprotectedMatcher[i++]=new AntPathRequestMatcher(pattern);
			}

		}
		
	}

}