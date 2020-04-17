package com.nucleus.master.audit.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface EmbedInAuditAsValueObject {

	public String displayKey() default "";

	public String displayValue() default "";
	
	public String identifierColumn() default "id";
	
	public String getterName() default "";
	
	public String setterName() default "";
	
	public boolean addAsEntityDef() default false;
	
	public String columnToDisplay() default "";
	
	public boolean skipInDisplay() default false;
	
}
