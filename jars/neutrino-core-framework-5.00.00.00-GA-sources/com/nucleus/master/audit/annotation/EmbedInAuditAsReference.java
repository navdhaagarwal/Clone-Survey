package com.nucleus.master.audit.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface EmbedInAuditAsReference {

	public String displayKey() default "";

	public String displayValue() default "";
	
	public Class referenceClass() default Object.class;
	
	public String columnToDisplay() default "code";
	
	public String getterName() default "";
	
	public String setterName() default "";

}
