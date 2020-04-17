package com.nucleus.standard.context;

import java.util.Map;

import javax.inject.Named;

@Named("executionContextHolder")
public class ExecutionContextHolder implements IExecutionContextHolder{

	@Override
	public void addToLocalContext(String key, Object value) {
		NeutrinoExecutionContextSupport.addToLocalContext(key, value);
		
	}

	@Override
	public Map<Thread, Map<String, Object>> getAllFromLocalContext() {
		return NeutrinoExecutionContextSupport.getAllFromLocalContext();
	}

	@Override
	public Object getFromLocalContext(String key) {
		return NeutrinoExecutionContextSupport.getFromLocalContext(key);
	}

	@Override
	public void clearLocalContext() {
		NeutrinoExecutionContextSupport.clearLocalContext();
		
	}

	@Override
	public void removeFromLocalContext() {
		NeutrinoExecutionContextSupport.removeFromLocalContext();
		
	}

	@Override
	public void addToGlobalContext(String key, Object value) {
		NeutrinoExecutionContextSupport.addToGlobalContext(key, value);
		
	}

	@Override
	public Object getFromGlobalContext(String key) {
		return NeutrinoExecutionContextSupport.getFromGlobalContext(key);
	}

	@Override
	public void clearGlobalContext() {
		NeutrinoExecutionContextSupport.clearGlobalContext();
		
	}

	@Override
	public void removeFromGlobalContext(String key) {
		NeutrinoExecutionContextSupport.removeFromGlobalContext(key);
		
	}

	@Override
	public Map<String, Object> getAllFromGlobalContext() {
		return NeutrinoExecutionContextSupport.getAllFromGlobalContext();
	}

	
}
