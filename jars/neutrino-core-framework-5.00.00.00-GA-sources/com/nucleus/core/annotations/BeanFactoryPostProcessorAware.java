package com.nucleus.core.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * To be applied on Annotations which wants to get call back on
 * BeanFactoryPostProcessor call
 * 
 * @author gajendra.jatav
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.ANNOTATION_TYPE })
public @interface BeanFactoryPostProcessorAware {

	String getQualifiedClass();

	String getMethod();

}
