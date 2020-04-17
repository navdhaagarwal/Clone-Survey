package com.nucleus.core.security.filesanitizer;

import com.nucleus.finnone.pro.base.exception.ServiceInputException;

/**
 * 
 * @author gajendra.jatav
 *
 */
public class XxeException extends ServiceInputException{
	
	private static final long serialVersionUID = 1L;

	public XxeException() {
		super();
	}

	public XxeException(String message) {
		super(message);
	}

	public XxeException(Throwable cause) {
		super(cause);
	}

	public XxeException(String message, Throwable cause) {
		super(message, cause);
	}

}
