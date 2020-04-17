package com.nucleus.web.security;

import org.springframework.security.access.AccessDeniedException;
public class NeutrinoSSOConcurrencyException extends AccessDeniedException{

	private static final long serialVersionUID = 4832303198455252582L;

	public NeutrinoSSOConcurrencyException(String msg) {
		super(msg);
	}

	
	
}
