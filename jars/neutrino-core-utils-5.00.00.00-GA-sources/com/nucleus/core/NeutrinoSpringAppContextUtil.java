/**
 * This file and a proportion of its content is copyright of Nucleus Software Exports Limited - � 2012. All rights reserved.
 * Any redistribution or reproduction of part or all of the contents in any form is prohibited other than the following:
 * - you cannot print or download to a local hard disk extract contents either part or full for personal/ commercial/
 * academic or any other use
 * - you may not copy the content to individual/ third parties for any type of use, either as compiled or source format
 * without the knowledge and consent of Nucleus Software
 * - You may not, except with our express written permission, distribute or commercially exploit the content. Nor may you
 * transmit it or store it in any other web site or other form of electronic retrieval system.
 */
package com.nucleus.core;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.inject.Named;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * @author Nucleus Software Exports Limited
 * This utility class is used to access spring beans in non managed objects.
 */
@Named
public class NeutrinoSpringAppContextUtil implements ApplicationContextAware {

    private static ApplicationContext applicationContext;
    
    //beans cache for singleton beans. Prototype beans cache is excluded to avoid memory leaks.
    private static Map<String, Object> beanObjectCache = new ConcurrentHashMap<>();

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        NeutrinoSpringAppContextUtil.applicationContext = applicationContext;
    }

	@SuppressWarnings("unchecked")
    public static <T> T getBeanByName(String name, Class<T> requiredType) {
    	String keyCode = name + requiredType.getName();
		T beanObject = (T) beanObjectCache.get(keyCode);
		if (beanObject == null) {
			beanObject = applicationContext.getBean(name, requiredType);
			//Allow caching of singleton beans only. Excluding prototype beans caching otherwise it will be a serious memory leaks.
			if (applicationContext.isSingleton(name)) {
				beanObjectCache.put(keyCode, beanObject);
			}
		}
        return beanObject;
    }

	public static <T> T getBeanByType(Class<T> requiredType) {
    	return applicationContext.getBean(requiredType);
    }
    
    public static <T> Map<String, T> getBeansOfType(Class<T> requiredType) {
		return applicationContext.getBeansOfType(requiredType);
	}
}
