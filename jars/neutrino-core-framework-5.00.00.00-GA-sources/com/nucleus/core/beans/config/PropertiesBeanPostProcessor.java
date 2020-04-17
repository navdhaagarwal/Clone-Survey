package com.nucleus.core.beans.config;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.reflections.Reflections;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;

import com.nucleus.core.annotations.InvalidAnnotationUsesFoundException;
import com.nucleus.core.annotations.NeutrinoAnnotationsBeanPostProcessor;
import com.nucleus.core.annotations.PropertiesAware;
import com.nucleus.logging.BaseLoggers;

/**
 * This PostProcessor will inject values to static field in non bean classes.
 * This post PostProcessor will be invoked as per BeanPostProcessorAware.<br>
 * In order to use Springs &#64;Value in non final static fields of non bean
 * classes refer below example
 * 
 * <pre>
 * &#64;PropertiesAware
 * public class MyTestStatic {
 *
 *	&#64;Value("property.key.from.properties.file")
 *	private static String setValue;
 * 
 * 
 * @see NeutrinoAnnotationsBeanPostProcessor
 * @author gajendra.jatav
 *
 */
public class PropertiesBeanPostProcessor {

	private String basePackage;

	private static List<Properties> propertiesList = new ArrayList<>();
	
	public PropertiesBeanPostProcessor(String basePackage) {
		this.basePackage = basePackage;
	}

	
	public String getBasePackage() {
		return basePackage;
	}

	public void setBasePackage(String basePackage) {
		this.basePackage = basePackage;
	}


	public static void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory, Reflections reflections) {

		String[] beans = beanFactory.getBeanNamesForType(PropertySourcesPlaceholderConfigurer.class, false, false);
		
		for (String beanName : beans) {
			if (beanFactory.getBean(beanName) != null) {
				BaseLoggers.flowLogger.debug("getting properties from bean {} for static field injection", beanName);
				Properties localProperties = (Properties) ((PropertySourcesPlaceholderConfigurer) beanFactory
						.getBean(beanName)).getAppliedPropertySources()
								.get(PropertySourcesPlaceholderConfigurer.LOCAL_PROPERTIES_PROPERTY_SOURCE_NAME)
								.getSource();

				if (localProperties != null) {
					propertiesList.add(localProperties);
				}

			}
		}

		injectPropertiesInApplicableClasses(propertiesList, reflections);

	}

	private static void injectPropertiesInApplicableClasses(List<Properties> propertiesList, Reflections reflections) {

		for (Class<?> cl : reflections.getTypesAnnotatedWith(PropertiesAware.class)) {
			BaseLoggers.flowLogger.debug("--------- Injecting values from properties for class {} ----------------",cl.getName());
			findStaticFieldForInjectionAndInjectProp(cl, propertiesList);
		}

	}

	private static void findStaticFieldForInjectionAndInjectProp(Class<?> cl, List<Properties> propertiesList) {

		for (Field field : cl.getDeclaredFields()) {
			if (field.isAnnotationPresent(Value.class)) {
				validateModifiersAndInjectValue(cl, field, propertiesList);
			}
		}

	}

	private static void validateModifiersAndInjectValue(Class<?> cl, Field field, List<Properties> propertiesList) {

		if (!java.lang.reflect.Modifier.isStatic(field.getModifiers())
				&& !java.lang.reflect.Modifier.isFinal(field.getModifiers())) {
			String error = "Invalid " + Value.class.getSimpleName() + " annotation uses found in class " + cl.getName()
					+ " for field " + field.getName() + " " + Value.class.getSimpleName()
					+ " can only be used with non final static fields";
			BaseLoggers.flowLogger.error(error);
			throw new InvalidAnnotationUsesFoundException(error);
		}
		Value type = field.getAnnotation(Value.class);
		String propKey = type.value();
		field.setAccessible(true);
		try {
			Object propertyValue=getPropValue(propertiesList, propKey);
			BaseLoggers.flowLogger.debug("Injecting {} in {} field of class {}",propertyValue,field.getName(),cl.getName());
			field.set(null, propertyValue);
		} catch (IllegalArgumentException | IllegalAccessException e) {

			String error = e.getClass().getSimpleName() + " occurred while static field injection using "
					+ Value.class.getSimpleName() + " annotation in class " + cl.getName() + " for field "
					+ field.getName();
			BaseLoggers.exceptionLogger.debug(error, e);
			throw new InvalidAnnotationUsesFoundException(error, e);
		}

	}

	private static Object getPropValue(List<Properties> propertiesList, String propKey) {

		for (Properties properties : propertiesList) {
			if (properties.containsKey(propKey)) {
				return properties.get(propKey);
			}
		}
		return null;
	}
	
	public static Object getPropValue(String propKey) {

		for (Properties properties : propertiesList) {
			if (properties.containsKey(propKey)) {
				return properties.get(propKey);
			}
		}
		return null;
	}

}
