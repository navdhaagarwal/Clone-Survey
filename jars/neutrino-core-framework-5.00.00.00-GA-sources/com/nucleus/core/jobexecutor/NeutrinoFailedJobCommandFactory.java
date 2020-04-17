package com.nucleus.core.jobexecutor;

import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.jobexecutor.FailedJobCommandFactory;

public class NeutrinoFailedJobCommandFactory implements FailedJobCommandFactory {
	
	@Override
	public Command<Object> getCommand(String jobId, Throwable exception) {
		return new NeutrinoFailedJobRetryCmd(jobId, exception);
	}

}