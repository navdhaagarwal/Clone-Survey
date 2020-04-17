package com.nucleus.web.util;

import java.util.List;

/**
 * 
 * @author gajendra.jatav
 *
 */
public class RequestMatcherConfig {

	private List<String> includedPatterns;
	
	private List<String> excludedPatterns;
	
	private String targetRequestMatcher;

	public List<String> getIncludedPatterns() {
		return includedPatterns;
	}

	public void setIncludedPatterns(List<String> includedPatterns) {
		this.includedPatterns = includedPatterns;
	}

	public List<String> getExcludedPatterns() {
		return excludedPatterns;
	}

	public void setExcludedPatterns(List<String> excludedPatterns) {
		this.excludedPatterns = excludedPatterns;
	}

	public String getTargetRequestMatcher() {
		return targetRequestMatcher;
	}

	public void setTargetRequestMatcher(String targetRequestMatcher) {
		this.targetRequestMatcher = targetRequestMatcher;
	}
	
}
