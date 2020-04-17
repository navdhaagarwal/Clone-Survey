package com.nucleus.finnone.pro.cache.annotation;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Retention(RUNTIME)
@Target({})
public @interface CompositePredicate {

	enum Operator {
		AND, OR
	}

	Operator operator() default Operator.AND;

	Predicate[] predicates();

}
