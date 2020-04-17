package com.nucleus.security.oauth.dao;

import org.springframework.security.oauth2.provider.ClientRegistrationException;

import com.nucleus.security.oauth.domainobject.OauthClientDetails;

public interface TrustedSourceDao {
	public OauthClientDetails loadClientByClientId(String clientId) throws ClientRegistrationException;
	public OauthClientDetails loadUnproxiedClientByClientId(String clientId);
}
