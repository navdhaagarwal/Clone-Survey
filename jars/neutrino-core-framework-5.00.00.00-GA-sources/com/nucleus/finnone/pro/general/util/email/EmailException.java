/*
 * Author: Merajul Hasan Ansari
 * Creation Date: 13-May-2013
 * Copyright: Nucleus Software Exports Ltd.
 * Description: This is custom exception class which will be raised in case of a exception in sending Email.
 *
 * ------------------------------------------------------------------------------------------------------------------------------------
 * Revision:  Version         Last Revision Date                   Name                Function / Module affected  Modifications Done
 * ------------------------------------------------------------------------------------------------------------------------------------
 *                1.0             13/05/2013                    Merajul Hasan Ansari             Initial Version created 
 
 */
package com.nucleus.finnone.pro.general.util.email;

import com.nucleus.finnone.pro.base.exception.BaseException;

public class EmailException extends BaseException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public EmailException() {
		super();
	}

	public EmailException(String message) {
		super(message);
	}

	public EmailException(String message, Throwable cause) {
		super(message, cause);
	}

	public EmailException(Throwable cause) {
		super(cause);
	}
}	