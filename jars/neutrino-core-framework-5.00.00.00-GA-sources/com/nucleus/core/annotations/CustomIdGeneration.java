package com.nucleus.core.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE })
@BeanFactoryPostProcessorAware(getMethod = "postProcessBeanFactory", getQualifiedClass = "com.nucleus.core.generator.CustomIdGenerationMapUtility")
@Inherited
public @interface CustomIdGeneration {
	
	int incrementSize();

}