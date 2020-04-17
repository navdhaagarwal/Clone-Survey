package com.nucleus.threadpool;

import org.springframework.core.task.TaskDecorator;

import com.nucleus.core.NeutrinoSpringAppContextUtil;
import com.nucleus.core.thread.support.MdcRetainingRunnable;
import com.nucleus.standard.context.INeutrinoExecutionContextHolder;

public class NeutrinoTaskMdcRetainingDecorator implements TaskDecorator {

	INeutrinoExecutionContextHolder neutrinoExecutionContextHolder;
	
	@Override
	public Runnable decorate(Runnable runnable) {
		
		if(neutrinoExecutionContextHolder==null)neutrinoExecutionContextHolder = NeutrinoSpringAppContextUtil.getBeanByName("neutrinoExecutionContextHolder", com.nucleus.standard.context.INeutrinoExecutionContextHolder.class);
		
		return new MdcRetainingRunnable(){
			
			@Override
			protected void runWithMdc() {
				runnable.run();
				neutrinoExecutionContextHolder.clearGlobalContext();
				neutrinoExecutionContextHolder.clearLocalContext();	
			}
		};
	}

}