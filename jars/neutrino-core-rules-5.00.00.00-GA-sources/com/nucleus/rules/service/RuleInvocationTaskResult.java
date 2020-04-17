package com.nucleus.rules.service;

import java.util.Iterator;
import java.util.Map;

import com.nucleus.core.event.EventExecutionResult;

public class RuleInvocationTaskResult extends EventExecutionResult {
	
	 /**
	 * 
	 */
	private static final long serialVersionUID = 1445886495286576868L;


	/**
	 * 
	 */
	

	@SuppressWarnings("rawtypes")
	 public Object getRuleInvocationTaskResult(){
	 	 if(resultMap!=null){
	 		 Iterator iterator=resultMap.entrySet().iterator();
	 		 while(iterator.hasNext()){
	 			 Map.Entry entry=(Map.Entry) iterator.next();
	 			 if(entry.getValue() instanceof RuleInvocationResult){
	 				 return entry.getValue();
	 			 }
	 		 }
	 		 
	 	 }
	 	return null;
	   
	  }
	  

	public RuleInvocationTaskResult(Map<Object, Object> resultMap) {
		super(resultMap);
		// TODO Auto-generated constructor stub
	}

}
