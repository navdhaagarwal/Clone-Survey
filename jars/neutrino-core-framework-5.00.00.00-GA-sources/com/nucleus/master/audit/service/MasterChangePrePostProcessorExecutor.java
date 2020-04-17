package com.nucleus.master.audit.service;

import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.stereotype.Component;

import com.nucleus.logging.BaseLoggers;
import com.nucleus.master.audit.MasterChangeDiffHolder;
import com.nucleus.master.audit.MasterChangeEntityHolder;
import com.nucleus.master.audit.service.util.MasterChangeExecutionHelper;

@Component("masterChangePrePostProcessorExecutor")
public class MasterChangePrePostProcessorExecutor {

	@Inject
	@Named("masterChangeJaversRegister")
	private MasterChangeJaversRegister helperRegister;

	public void executePreProcess(MasterChangeEntityHolder oldEntity, MasterChangeEntityHolder newEntity, Class entityClass) {
		MasterChangeExecutionHelper preProcessor;
		try {
			preProcessor = helperRegister.getDiffHelperInstance(entityClass);
			if (preProcessor != null) {
				preProcessor.preProcess(oldEntity, newEntity);
			}
		} catch (Exception e) {
			BaseLoggers.exceptionLogger.error("Error in preProcessor",e);
		}
		
	}
	
	public void executePostProcess(MasterChangeDiffHolder diffHolder,Class entityClass,MasterChangeEntityHolder oldEntity, MasterChangeEntityHolder newEntity){
		MasterChangeExecutionHelper postProcessor;
		try {
			postProcessor = helperRegister.getDiffHelperInstance(entityClass);
			if (postProcessor != null) {
				postProcessor.postProcess(diffHolder,oldEntity,newEntity);
			}
		} catch (Exception e) {
			BaseLoggers.exceptionLogger.error("Error in postProcessor",e);
		}
		
	}
}
