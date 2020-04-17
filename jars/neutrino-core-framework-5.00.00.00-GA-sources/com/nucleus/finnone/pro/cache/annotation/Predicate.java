package com.nucleus.finnone.pro.cache.annotation;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Retention(RUNTIME)
@Target({})
public @interface Predicate {

	public static String IS_NULL = " IS NULL ";
	public static String IS_NOT_NULL = " IS NOT NULL ";
	public static String EQUAL = " = ";
	public static String NOT_EQUAL = " <> ";
	public static String NOT_IN = " NOT IN ";
	public static String IN = " IN ";
	public static String AND = " AND ";
	public static String OR = " OR ";
	public static String LESS_THAN = " < ";
	public static String LESS_THAN_EQUAL = " <= ";
	public static String GREATER_THAN = " > ";
	public static String GREATER_THAN_EQUAL = " >= ";

	enum Operator {
		IS_NULL, IS_NOT_NULL, EQUAL, NOT_EQUAL, NOT_IN, IN, AND, OR, LESS_THAN, LESS_THAN_EQUAL, GREATER_THAN, GREATER_THAN_EQUAL
	}

	Operator operator();

	String[] value();

	String field();

}
