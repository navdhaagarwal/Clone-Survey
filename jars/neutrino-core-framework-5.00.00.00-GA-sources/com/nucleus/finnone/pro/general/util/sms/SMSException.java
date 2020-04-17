/*
 * Author: Merajul Hasan Ansari
 * Creation Date: 13-May-2013
 * Copyright: Nucleus Software Exports Ltd.
 * Description: This is custom exception class which will be raised in case of a exception in sending SMS.
 *
 * ------------------------------------------------------------------------------------------------------------------------------------
 * Revision:  Version         Last Revision Date                   Name                Function / Module affected  Modifications Done
 * ------------------------------------------------------------------------------------------------------------------------------------
 *                1.0             13/05/2013                    Merajul Hasan Ansari             Initial Version created 
 
 */
package com.nucleus.finnone.pro.general.util.sms;

import com.nucleus.finnone.pro.base.exception.BaseException;

public class SMSException extends BaseException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public SMSException() {
		super();
	}

	public SMSException(String message) {
		super(message);
	}

	public SMSException(String message, Throwable cause) {
		super(message, cause);
	}

	public SMSException(Throwable cause) {
		super(cause);
	}
	
}	