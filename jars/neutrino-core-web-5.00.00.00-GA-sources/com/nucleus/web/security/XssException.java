package com.nucleus.web.security;

import com.nucleus.finnone.pro.base.exception.ServiceInputException;

public class XssException extends ServiceInputException {

	private static final long serialVersionUID = 1L;

	public XssException() {
		super();
	}

	public XssException(String message) {
		super(message);
	}

	public XssException(Throwable cause) {
		super(cause);
	}

	public XssException(String message, Throwable cause) {
		super(message, cause);
	}
}