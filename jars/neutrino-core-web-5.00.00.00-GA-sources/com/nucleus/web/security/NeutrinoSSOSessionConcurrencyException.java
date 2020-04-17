package com.nucleus.web.security;

import org.springframework.security.access.AccessDeniedException;

public class NeutrinoSSOSessionConcurrencyException extends AccessDeniedException{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public NeutrinoSSOSessionConcurrencyException(String msg) {
		super(msg);
	}


}
