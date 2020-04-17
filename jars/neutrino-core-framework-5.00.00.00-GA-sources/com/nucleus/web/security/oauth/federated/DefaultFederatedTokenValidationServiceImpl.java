package com.nucleus.web.security.oauth.federated;

import org.springframework.security.oauth2.provider.ClientDetails;

import com.nucleus.security.oauth.domainobject.OauthTokenDetails;

public class DefaultFederatedTokenValidationServiceImpl implements FederatedTokenValidationService{

	@Override
	public OauthTokenDetails getAccessTokenBasedOnAuthCode(String idpAuthCode, ClientDetails client) {
		throw new UnsupportedOperationException("This method is not implemented");
	}

	@Override
	public OauthTokenDetails getAccessTokenBasedOnRefreshToken(String refreshToken, ClientDetails client) {
		throw new UnsupportedOperationException("This method is not implemented");
	}
	

}
