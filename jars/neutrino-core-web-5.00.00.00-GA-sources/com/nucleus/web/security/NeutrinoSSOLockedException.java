package com.nucleus.web.security;

import org.springframework.security.access.AccessDeniedException;

public class NeutrinoSSOLockedException extends AccessDeniedException{

	/**
	 * Thrown when authenticating locked user in SSO
	 */
	private static final long serialVersionUID = 123423534L;

	public NeutrinoSSOLockedException(String msg) {
		super(msg);
	}

}
