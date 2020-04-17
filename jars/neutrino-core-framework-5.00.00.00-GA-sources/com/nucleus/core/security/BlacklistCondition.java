package com.nucleus.core.security;

/**
 * 
 * @author gajendra.jatav
 *
 */
@FunctionalInterface
public interface BlacklistCondition {

	public boolean check(String input);
}
