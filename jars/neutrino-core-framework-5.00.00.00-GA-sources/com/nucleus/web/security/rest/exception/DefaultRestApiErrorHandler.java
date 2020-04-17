package com.nucleus.web.security.rest.exception;

import java.util.Map;

import javax.inject.Named;

import org.springframework.http.HttpStatus;

@Named("defaultRestApiErrorHandler")
public class DefaultRestApiErrorHandler implements RestApiErrorHandler {

	@Override
	public void handlerErrorCodes(HttpStatus status, Map<String, String> response) {
		throw new UnsupportedOperationException("This method is not implemented");
	}

}
