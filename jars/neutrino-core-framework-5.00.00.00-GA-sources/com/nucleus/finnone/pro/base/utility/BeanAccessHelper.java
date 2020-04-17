/**
 * 
 */
package com.nucleus.finnone.pro.base.utility;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.inject.Named;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author dhananjay.jha
 * @param <T>
 *
 */
@Named("beanAccessHelper")
public class BeanAccessHelper {
	
	@Autowired
	private BeanFactory beanFactory;
	
    //beans cache for singleton beans. Prototype beans cache is excluded to avoid memory leaks.
	private Map<String, Object> beansCache = new ConcurrentHashMap<>();
	
	@SuppressWarnings("unchecked")
	public <T> T getBean(String beanName, Class<T> requiredType) throws BeansException {
		String keyCode = beanName + requiredType.getName();
		T beanObject = (T) beansCache.get(keyCode);
		if (beanObject == null) {
			beanObject = beanFactory.getBean(beanName, requiredType);
			//Allow caching of singleton beans only. Excluding prototype beans caching otherwise it will be a serious memory leaks.
			if (beanFactory.isSingleton(beanName)) {
				beansCache.put(keyCode, beanObject);
			}
		}
		return beanObject;
	}  
}
