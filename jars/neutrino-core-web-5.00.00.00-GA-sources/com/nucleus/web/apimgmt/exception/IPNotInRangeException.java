package com.nucleus.web.apimgmt.exception;

import org.springframework.security.access.AccessDeniedException;

public class IPNotInRangeException extends AccessDeniedException{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	
	public IPNotInRangeException(String err){
		super(err);
	}
}
