package com.nucleus.master.audit.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface EmbedInAuditAsValue {

	public String displayKey() default "";
	
	public String displayValue() default "";
	
	public boolean invokeCallBack() default false;
	
	public String getterName() default "";
	
	public String setterName() default "";
	
	public boolean skipInDisplay() default false;
	
}
