package com.nucleus.standard.context;

import java.util.Map;

 public interface IExecutionContextHolder {
	

	 void addToLocalContext(String key, Object value) ;

	 Map<Thread, Map<String, Object>> getAllFromLocalContext() ;

	 Object getFromLocalContext(String key);

	 void clearLocalContext();

	 void removeFromLocalContext();

	 void addToGlobalContext(String key, Object value);
	
	 Object getFromGlobalContext(String key);
	 
	  Map<String, Object> getAllFromGlobalContext() ;
	
	 void clearGlobalContext() ;

	 void removeFromGlobalContext(String key) ;
	
}
