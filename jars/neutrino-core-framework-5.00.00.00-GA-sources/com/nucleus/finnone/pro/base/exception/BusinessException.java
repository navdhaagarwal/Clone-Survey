/*
 * Author: Abhishek Pallav
 * Creation Date: 24-Aug-2012
 * Copyright: Nucleus Software Exports Ltd.
 * Description: This is business exception class which will be raised in case of a business exception.
 *
 * ------------------------------------------------------------------------------------------------------------------------------------
 * Revision:  Version         Last Revision Date                   Name                Function / Module affected  Modifications Done
 * ------------------------------------------------------------------------------------------------------------------------------------
 *                1.0             24/08/2012                    Abhishek Pallav             Initial Version created 
 
 */
package com.nucleus.finnone.pro.base.exception;

public class BusinessException extends BaseException {

	public BusinessException() {
		super();
	}

	public BusinessException(String message) {
		super(message);
	}

	public BusinessException(String message, Throwable cause) {
		super(message, cause);
	}

	public BusinessException(Throwable cause) {
		super(cause);
	}
}