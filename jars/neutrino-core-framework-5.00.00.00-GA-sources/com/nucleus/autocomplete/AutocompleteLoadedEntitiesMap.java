package com.nucleus.autocomplete;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.nucleus.core.exceptions.SystemException;
import com.nucleus.logging.BaseLoggers;

public class AutocompleteLoadedEntitiesMap {
	
	private AutocompleteLoadedEntitiesMap(){
		throw new UnsupportedOperationException();
	}
	
	private static  Map<String,Class<?>> cachedClassMap= new ConcurrentHashMap<>();
	
	public static void addClassesToMap(String className) {
		String[] classList = className.split(",");

		for (String tempClass : classList) {
			if (!cachedClassMap.containsKey(tempClass)) {
				try {
					cachedClassMap.put(tempClass, Class.forName(tempClass));
				} catch (ClassNotFoundException e) {
					BaseLoggers.exceptionLogger.error("Class with name " + tempClass + " Not found: " + e.getMessage(), e);
				}
			}
		}
	}
	
	public static Class<?> getClassFromMap(String className) {
		
		Class cachedClass = cachedClassMap.get(className);
		
		if (cachedClass == null) {
			try {
				cachedClass = Class.forName(className);
				cachedClassMap.put(className, cachedClass);
			} catch (ClassNotFoundException e) {
				BaseLoggers.exceptionLogger.error("Class with name " + className + " Not found: " + e.getMessage(), e);
			}
		}
		return cachedClass;
	}
}
