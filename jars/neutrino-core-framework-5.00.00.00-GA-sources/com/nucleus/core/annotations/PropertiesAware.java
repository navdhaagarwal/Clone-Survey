package com.nucleus.core.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 
 * Annotation at the class level
 * <p>
 * Typically used for marking classes in which we wants to use spring &#64;Value
 * annotation to inject simple properties from properties files in non final
 * static fields of non bean classes injection.
 * 
 * @author gajendra.jatav
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE })
@BeanFactoryPostProcessorAware(getMethod = "postProcessBeanFactory", getQualifiedClass = "com.nucleus.core.beans.config.PropertiesBeanPostProcessor")
@Inherited
public @interface PropertiesAware {

}
