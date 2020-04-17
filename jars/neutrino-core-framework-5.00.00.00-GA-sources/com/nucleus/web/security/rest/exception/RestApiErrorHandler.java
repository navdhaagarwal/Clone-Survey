package com.nucleus.web.security.rest.exception;

import java.util.Map;

import org.springframework.http.HttpStatus;

public interface RestApiErrorHandler {
	
	void handlerErrorCodes(HttpStatus status, Map<String, String> response);

}
