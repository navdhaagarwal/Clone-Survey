package com.nucleus.finnone.pro.cache.annotation;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import com.nucleus.finnone.pro.cache.constants.FWCacheConstants;

@Retention(RUNTIME)
@Target({})
public @interface CustomCache {

	public enum Type {
		ID, OBJECT
	}

	public enum AndOr {
		AND, OR
	}

	String name();
	
	String regionName();
	
	String groupName() default FWCacheConstants.EMPTY_VALUE;

	CustomCache.Type type() default Type.ID;

	Predicate[] predicates() default {};

	CompositePredicate[] compositePredicates() default {};

	AndOr andOr() default AndOr.AND;

}
