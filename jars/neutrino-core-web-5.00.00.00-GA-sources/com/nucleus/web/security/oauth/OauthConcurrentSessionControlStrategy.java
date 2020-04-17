package com.nucleus.web.security.oauth;

import org.springframework.security.oauth2.provider.TokenRequest;

public interface  OauthConcurrentSessionControlStrategy {

boolean validateOauthReqeust(TokenRequest tokenRequest);
int getMaximumSessions();
/**
 * 
 * For anonymous client type
 * 
 */
boolean validateOauthAnonymousReqeust(TokenRequest tokenRequest);
int getMaxExternalClients();

}


