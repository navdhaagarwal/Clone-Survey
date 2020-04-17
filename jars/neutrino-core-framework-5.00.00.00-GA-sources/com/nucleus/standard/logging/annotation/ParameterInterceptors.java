package com.nucleus.standard.logging.annotation;

@java.lang.annotation.Target(value = { java.lang.annotation.ElementType.METHOD })
@java.lang.annotation.Retention(value = java.lang.annotation.RetentionPolicy.RUNTIME)
public @interface ParameterInterceptors {

	public ParameterInterceptor[] value();

}