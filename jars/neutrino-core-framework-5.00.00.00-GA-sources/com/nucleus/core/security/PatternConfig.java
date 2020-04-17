package com.nucleus.core.security;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * 
 * @author gajendra.jatav
 *
 */
public class PatternConfig {

	private List<Pattern> patterns;

	private BlacklistCondition balckListCondition;

	private boolean applyPattern = true;

	public PatternConfig(BlacklistCondition beforePatternCondition) {
		super();
		this.balckListCondition = beforePatternCondition;
		this.applyPattern = false;
	}

	public PatternConfig(Pattern... configuredPattern) {
		super();
		this.patterns = new ArrayList<>();
		for(Pattern pattern:configuredPattern){
			this.patterns.add(pattern);
		}
		this.applyPattern = true;
	}

	public PatternConfig(BlacklistCondition beforePattern,Pattern... configuredPattern) {
		super();
		this.patterns = new ArrayList<>();
		for(Pattern pattern:configuredPattern){
			this.patterns.add(pattern);
		}
		this.balckListCondition = beforePattern;
		this.applyPattern = true;
	}

	public PatternConfig(BlacklistCondition blacklistCondition, List<Pattern> listOfPatterns) {
		super();
		this.patterns = listOfPatterns;
		this.balckListCondition = blacklistCondition;
		this.applyPattern = true;
	}

	public List<Pattern> getPatterns() {
		return patterns;
	}
	
	public BlacklistCondition getBalckListCondition() {
		return balckListCondition;
	}

	public boolean isApplyPattern() {
		return applyPattern;
	}

}
