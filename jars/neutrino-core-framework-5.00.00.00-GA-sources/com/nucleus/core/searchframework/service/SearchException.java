package com.nucleus.core.searchframework.service;

/**
 * 
 * @author Nucleus Software Exports Limited
 */
public class SearchException extends RuntimeException {
	
	private static final long serialVersionUID = 1L;

    public SearchException(){
		super();
	}
	
	public SearchException(String message) {
		super(message);
	}
	
	public SearchException(String message, Throwable cause) {
		super(message, cause);
	}
	
	public SearchException(Throwable cause) {
		super(cause);
	}
}
