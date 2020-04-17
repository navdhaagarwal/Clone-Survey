package com.nucleus.core.generator;

import java.util.HashMap;
import java.util.Map;

import org.reflections.Reflections;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;

import com.nucleus.core.annotations.CustomIdGeneration;

public class CustomIdGenerationMapUtility {
	
	private static Map<String,Integer> optimizedEntityMap = new HashMap<>(); 
	
	public static void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory, Reflections reflections) {
		createOptimizedEntityMap(reflections);
	}
	
	public static void createOptimizedEntityMap(Reflections reflections) {

		for (Class<?> clazz : reflections.getTypesAnnotatedWith(CustomIdGeneration.class)) {
			CustomIdGeneration annotation = clazz.getAnnotation(CustomIdGeneration.class);
			if(annotation.incrementSize()>0){
				optimizedEntityMap.put(clazz.getName(), Integer.valueOf(annotation.incrementSize()) );
			}
		}

	}
	
	public static int getOptimizedEntityDetail(String entityName) {
		Integer incrementSize = optimizedEntityMap.get(entityName);
		if(incrementSize == null){
			return 0;
		}
		else{
			return incrementSize;
		}
	}
	
	
}