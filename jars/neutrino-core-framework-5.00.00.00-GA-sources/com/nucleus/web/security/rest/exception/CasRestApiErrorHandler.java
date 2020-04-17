package com.nucleus.web.security.rest.exception;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.common.exceptions.InvalidRequestException;
import org.springframework.security.oauth2.common.exceptions.UnauthorizedClientException;
import org.springframework.stereotype.Component;

@Component("casRestApiErrorHandler")
public class CasRestApiErrorHandler implements RestApiErrorHandler {

	@Override
	public void handlerErrorCodes(HttpStatus status, Map<String, String> response) {
		switch(status) {
			case OK: 
				return;
			
			case BAD_REQUEST:
				throw new InvalidRequestException("Response from IdP server: " + response.get("error"));
				
			case NOT_FOUND:
				throw new InvalidRequestException("The IdP server has not found anything matching the request URI.");
				
			case UNAUTHORIZED:
				throw new UnauthorizedClientException("Unauthorised to access the request due to incorrect client data sent to IdP service.");
				
			case INTERNAL_SERVER_ERROR:
				throw new InvalidRequestException("An internal error has occurred at the IdP service.");
				
			default:
				throw new InvalidRequestException("Some error occurred while communicating with the IdP server");
		}
	}

}
