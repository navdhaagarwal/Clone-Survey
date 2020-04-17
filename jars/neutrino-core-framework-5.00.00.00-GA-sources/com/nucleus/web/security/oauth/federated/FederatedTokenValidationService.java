package com.nucleus.web.security.oauth.federated;

import org.springframework.security.oauth2.provider.ClientDetails;

import com.nucleus.security.oauth.domainobject.OauthTokenDetails;

public interface FederatedTokenValidationService {

	OauthTokenDetails getAccessTokenBasedOnAuthCode(String idpAuthCode, ClientDetails client);
	
	OauthTokenDetails getAccessTokenBasedOnRefreshToken(String refreshToken, ClientDetails client);

}
