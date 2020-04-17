package com.nucleus.web.security;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

public class CustomRestHttpServletRequest extends HttpServletRequestWrapper{

	 private Map<String, String> customHeaderMap = null;
	 
	 
	 public CustomRestHttpServletRequest(HttpServletRequest request) {
		  super(request);
		  customHeaderMap = new HashMap<String, String>();
	}
	 
	 
	 @Override
	 public String getParameter(String name) {
	  String paramValue = super.getParameter(name); // query Strings
	  if (paramValue == null) {
	   paramValue = customHeaderMap.get(name);
	  }
	  return paramValue;
	 }
	 
	 
	  public Map<String, String> getCustomParameterMap()
	    {
	        //Return an unmodifiable collection because we need to uphold the interface contract.
	        return this.customHeaderMap;
	    }
	 
	 
}
