package com.nucleus.master.audit.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface NeutrinoAuditableMaster {

	public String identifierColumn() default "id";
	
	public NeutrinoAuditableDisJointChild[] disJointChild() default {};
	
}
