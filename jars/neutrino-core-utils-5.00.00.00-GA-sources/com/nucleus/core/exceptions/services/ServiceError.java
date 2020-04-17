package com.nucleus.core.exceptions.services;

/**
 * @author Nucleus Software Exports Limited
 * @description For Restful web services to respond with a proper error message and description of mine type application/json or application/xml.
 */

public class ServiceError {
	private String error;
	private String error_description;
	
	public ServiceError(String error, String error_description) {
		super();
		this.error = error;
		this.error_description = error_description;
	}

	public String getError() {
		return error;
	}

	public void setError(String error) {
		this.error = error;
	}

	public String getError_description() {
		return error_description;
	}

	public void setError_description(String error_description) {
		this.error_description = error_description;
	}
	
}
