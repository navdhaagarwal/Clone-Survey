package com.nucleus.web.apimgmt.exception;

import org.springframework.security.access.AccessDeniedException;

public class ThrottleCheckException extends AccessDeniedException{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	
	public ThrottleCheckException(String err){
		super(err);
	}
}
