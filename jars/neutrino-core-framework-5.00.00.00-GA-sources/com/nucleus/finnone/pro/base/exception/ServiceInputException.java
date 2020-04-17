/*
 * Author: Abhishek Pallav
 * Creation Date: 24-Aug-2012
 * Copyright: Nucleus Software Exports Ltd.
 * Description: This is custom exception class which will be raised in case of a service input exception.
 *
 * ------------------------------------------------------------------------------------------------------------------------------------
 * Revision:  Version         Last Revision Date                   Name                Function / Module affected  Modifications Done
 * ------------------------------------------------------------------------------------------------------------------------------------
 *                1.0             24/08/2012                    Abhishek Pallav             Initial Version created 
 
 */
package com.nucleus.finnone.pro.base.exception;

public class ServiceInputException extends BaseException {

	public ServiceInputException() {
		super();
	}

	public ServiceInputException(String message) {
		super(message);
	}

	public ServiceInputException(String message, Throwable cause) {
		super(message, cause);
	}

	public ServiceInputException(Throwable cause) {
		super(cause);
	}
}