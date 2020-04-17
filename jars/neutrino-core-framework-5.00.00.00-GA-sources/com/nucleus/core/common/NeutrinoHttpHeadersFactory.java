package com.nucleus.core.common;

import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.nucleus.security.oauth.service.RESTfulAuthenticationService;

/**
 * @author shivendra.kumar
 *
 */
@Named("neutrinoHttpHeadersFactory")
@Component
public class NeutrinoHttpHeadersFactory {
	
	
	@Value("${soap.service.trusted.client.id}")
	private String clientID;
	
	@Inject
	@Named("oauthauthenticationService")
	private RESTfulAuthenticationService authenticationService;
	
	public NeutrinoHttpHeaders create() {
		NeutrinoHttpHeaders headers = new NeutrinoHttpHeaders();
		headers.setAccessToken(authenticationService.getSecurityToken(clientID));
		return headers;
	}

}
