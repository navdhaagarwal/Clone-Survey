package com.nucleus.standard.context;

import javax.inject.Inject;
import javax.inject.Named;

import org.aspectj.lang.JoinPoint;


public class NeutrinoContextInitializationAspect {
	
	@Inject
	@Named("neutrinoExecutionContextInitializationHelper")
	private NeutrinoExecutionContextInitializationHelper neutrinoExecutionContextInitializationHelper;
	
	public void initializeDefaultUserContext(JoinPoint joinPoint){
		neutrinoExecutionContextInitializationHelper.initializeDefaultContext(true);
	}
	

	public void initializeNonLoginUserContext(JoinPoint joinPoint){
		neutrinoExecutionContextInitializationHelper.initializeDefaultContext(false);
	}
}
