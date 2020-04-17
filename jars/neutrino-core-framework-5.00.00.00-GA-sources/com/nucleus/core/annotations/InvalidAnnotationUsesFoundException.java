package com.nucleus.core.annotations;

/**
 * 
 * @author gajendra.jatav
 *
 */
public class InvalidAnnotationUsesFoundException extends RuntimeException{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public InvalidAnnotationUsesFoundException(String message) {
		super(message);
	}
	
	public InvalidAnnotationUsesFoundException(String message, Throwable cause) {
		super(message,cause);
	}
	
	

}
