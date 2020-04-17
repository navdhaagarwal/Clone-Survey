package com.nucleus.standard.logging.annotation;

@java.lang.annotation.Target(value = { java.lang.annotation.ElementType.METHOD })
@java.lang.annotation.Retention(value = java.lang.annotation.RetentionPolicy.RUNTIME)
public @interface ParameterInterceptor {

	public enum LoggingContext {
		GLOBAL, LOCAL
	}

	LoggingContext loggingContext() default LoggingContext.LOCAL;

	public abstract java.lang.String name();

	public abstract int index() default 0;
	
	public abstract java.lang.String key() default "";

}
