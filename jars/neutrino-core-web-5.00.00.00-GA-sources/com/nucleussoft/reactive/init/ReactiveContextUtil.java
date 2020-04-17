package com.nucleussoft.reactive.init;

import org.springframework.context.ApplicationContext;

/**
 * 
 * @author gajendra.jatav
 *
 */
public abstract class ReactiveContextUtil {

	private ReactiveContextUtil(){
		
	}
	
	private static ApplicationContext applicationContext;
	
	public static ApplicationContext getReactiveAppContext(){
		return applicationContext;
	}

	public static void setReactiveAppContext(ApplicationContext applicationContext){
		ReactiveContextUtil.applicationContext=applicationContext;
	}

}

