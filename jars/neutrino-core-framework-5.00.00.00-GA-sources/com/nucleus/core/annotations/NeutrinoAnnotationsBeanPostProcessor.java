package com.nucleus.core.annotations;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.reflections.Reflections;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;

/**
 * This PostProcessor will enable custom annotations which has
 * BeanPostProcessorAware applied to do some tasks by calling qualified class
 * method. This post PostProcessor will be invoked after
 * PropertySourcesPlaceholderConfigurer as it does not implement PriorityOrdered
 * so it's part of non ordered PostProcessors and will be called after all
 * PostProcessor which implement PriorityOrdered. <br/>
 * <b>Uses:</b> In order to use get a call to static method for processing as
 * per Custom annotation annotate custom annotation as below
 * 
 * <pre>
 * Ex<br>
 * <code>
 * &#64;Retention(RetentionPolicy.RUNTIME)
 * &#64;Target({ElementType.TYPE})
 * &#64;BeanPostProcessorAware(getMethod = "postProcessBeanFactory", getQualifiedClass = "com.nucleus.core.beans.config.PropertiesBeanPostProcessor")
 * &#64;Inherited
 * public &#64;interface PropertiesAware {
 * </code>
 * 
 * </pre>
 * 
 * @author gajendra.jatav
 *
 */
public class NeutrinoAnnotationsBeanPostProcessor implements BeanFactoryPostProcessor {

	private String basePackage;

	public NeutrinoAnnotationsBeanPostProcessor(String basePackage) {
		this.basePackage = basePackage;
	}

	@Override
	public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) {

		Reflections reflection = new Reflections(basePackage);
		for (Class<?> cl : reflection.getTypesAnnotatedWith(BeanFactoryPostProcessorAware.class)) {
			processAnnotation(beanFactory, reflection, cl);
		}

	}

	private void processAnnotation(ConfigurableListableBeanFactory beanFactory, Reflections reflection, Class<?> cl) {

		BeanFactoryPostProcessorAware type = cl.getAnnotation(BeanFactoryPostProcessorAware.class);
		String qualifiedClass = type.getQualifiedClass();
		Class c;
		try {
			c = Class.forName(qualifiedClass);
			String methodName = type.getMethod();
			Method method = c.getMethod(methodName, ConfigurableListableBeanFactory.class, Reflections.class);
			method.invoke(null, beanFactory, reflection);

		} catch (ClassNotFoundException | NoSuchMethodException | SecurityException | IllegalAccessException
				| IllegalArgumentException | InvocationTargetException e) {
			String error = "Invalid " + cl.getSimpleName() + "  uses found in class ";
			throw new InvalidAnnotationUsesFoundException(error,e);
		}
		

	}

}
