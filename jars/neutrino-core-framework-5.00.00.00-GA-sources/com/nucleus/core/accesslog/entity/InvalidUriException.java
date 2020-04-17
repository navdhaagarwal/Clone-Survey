package com.nucleus.core.accesslog.entity;

public class InvalidUriException extends Exception {

	private static final long serialVersionUID = 1L;
	
	public InvalidUriException(String msg){
		super(msg);
	}

}
