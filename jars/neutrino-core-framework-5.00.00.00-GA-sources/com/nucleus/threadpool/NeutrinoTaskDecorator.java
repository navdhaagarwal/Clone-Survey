package com.nucleus.threadpool;

import org.springframework.core.task.TaskDecorator;

import com.nucleus.core.NeutrinoSpringAppContextUtil;
import com.nucleus.standard.context.INeutrinoExecutionContextHolder;

public class NeutrinoTaskDecorator implements TaskDecorator {

	INeutrinoExecutionContextHolder neutrinoExecutionContextHolder;
	
	@Override
	public Runnable decorate(Runnable runnable) {
		
		if(neutrinoExecutionContextHolder==null)neutrinoExecutionContextHolder = NeutrinoSpringAppContextUtil.getBeanByName("neutrinoExecutionContextHolder", com.nucleus.standard.context.INeutrinoExecutionContextHolder.class);
		return new NeutrinoDecoratedRunnable(runnable);
	}
	
	class NeutrinoDecoratedRunnable implements Runnable{
		
		Runnable runnableToDecorate;
		
		public NeutrinoDecoratedRunnable(Runnable runnableToDecorate) {
			this.runnableToDecorate = runnableToDecorate;
		}
		
		@Override
		public void run() {
			neutrinoExecutionContextHolder.clearGlobalContext();
			neutrinoExecutionContextHolder.clearLocalContext();
			runnableToDecorate.run();
			neutrinoExecutionContextHolder.clearGlobalContext();
			neutrinoExecutionContextHolder.clearLocalContext();
			
		}
		
	}

}
