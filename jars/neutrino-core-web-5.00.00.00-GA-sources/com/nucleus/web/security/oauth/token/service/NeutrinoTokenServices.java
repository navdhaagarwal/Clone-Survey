package com.nucleus.web.security.oauth.token.service;

import java.util.Date;
import java.util.Set;
import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.common.DefaultExpiringOAuth2RefreshToken;
import org.springframework.security.oauth2.common.DefaultOAuth2AccessToken;
import org.springframework.security.oauth2.common.DefaultOAuth2RefreshToken;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.common.OAuth2RefreshToken;
import org.springframework.security.oauth2.common.exceptions.InvalidGrantException;
import org.springframework.security.oauth2.common.exceptions.InvalidScopeException;
import org.springframework.security.oauth2.common.exceptions.InvalidTokenException;
import org.springframework.security.oauth2.provider.ClientDetails;
import org.springframework.security.oauth2.provider.ClientDetailsService;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.OAuth2Request;
import org.springframework.security.oauth2.provider.TokenRequest;
import org.springframework.security.oauth2.provider.token.DefaultTokenServices;
import org.springframework.security.oauth2.provider.token.TokenEnhancer;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.transaction.annotation.Transactional;

import com.nucleus.security.oauth.constants.TrustedSourceRegistrationConstant;
import com.nucleus.security.oauth.domainobject.OauthTokenDetails;
import com.nucleus.web.security.federated.FederatedLoginAuthenticationToken;
import com.nucleus.web.security.oauth.federated.FederatedTokenValidationService;

/**
 * This class is extended from {@link}DefaultTokenServices to handle
 * the flow of refresh token in case of federated grant type  
 * 
 * @author namrata.varshney
 *
 */
public class NeutrinoTokenServices extends DefaultTokenServices{
	
	private boolean supportRefreshToken = false;
	
	private boolean reuseRefreshToken = true;
	
	private TokenStore tokenStore;

	private ClientDetailsService clientDetailsService;

	private TokenEnhancer accessTokenEnhancer;
	
	private AuthenticationManager authenticationManager;
	
	@Inject
	@Named("federatedOauthValidationService")
	FederatedTokenValidationService federatedTokenValidationService;

	@Override
	public void setSupportRefreshToken(boolean supportRefreshToken) {
		super.setSupportRefreshToken(supportRefreshToken);
		this.supportRefreshToken = supportRefreshToken;
	}

	public TokenStore getTokenStore() {
		return tokenStore;
	}

	@Override
	public void setTokenStore(TokenStore tokenStore) {
		super.setTokenStore(tokenStore);
		this.tokenStore = tokenStore;
	}

	public ClientDetailsService getClientDetailsService() {
		return clientDetailsService;
	}
	
	@Override
	public void setAuthenticationManager(AuthenticationManager authenticationManager) {
		super.setAuthenticationManager(authenticationManager);
		this.authenticationManager = authenticationManager;
	}

	@Override
	public void setClientDetailsService(ClientDetailsService clientDetailsService) {
		super.setClientDetailsService(clientDetailsService);
		this.clientDetailsService = clientDetailsService;
	}
	
	@Override
	public void setReuseRefreshToken(boolean reuseRefreshToken) {
		super.setReuseRefreshToken(reuseRefreshToken);
		this.reuseRefreshToken = reuseRefreshToken;
	}
	
	@Override
	public void setTokenEnhancer(TokenEnhancer accessTokenEnhancer) {
		super.setTokenEnhancer(accessTokenEnhancer);
		this.accessTokenEnhancer = accessTokenEnhancer;
	}
	
	/* (non-Javadoc)
	 * Custom implementation of refreshAccessToken where in case of federated grant type 
	 * a rest call to the server is made to the IdP to check if its refresh token is valid
	 *
	 */
	@Override
	@Transactional(noRollbackFor={InvalidTokenException.class, InvalidGrantException.class})
	public OAuth2AccessToken refreshAccessToken(String refreshTokenValue, TokenRequest tokenRequest){

		if (!supportRefreshToken) {
			throw new InvalidGrantException("Invalid refresh token: " + refreshTokenValue);
		}

		OAuth2RefreshToken refreshToken = tokenStore.readRefreshToken(refreshTokenValue);
		if (refreshToken == null) {
			throw new InvalidGrantException("Invalid refresh token: " + refreshTokenValue);
		}

		OAuth2Authentication authentication = tokenStore.readAuthenticationForRefreshToken(refreshToken);
		
		if (this.authenticationManager != null && !authentication.isClientOnly()) {
			// The client has already been authenticated, but the user authentication might be old now, so give it a
			// chance to re-authenticate.
			Authentication user = new PreAuthenticatedAuthenticationToken(authentication.getUserAuthentication(), "", authentication.getAuthorities());
			user = authenticationManager.authenticate(user);
			Object details = authentication.getDetails();
			authentication = new OAuth2Authentication(authentication.getOAuth2Request(), user);
			authentication.setDetails(details);
		}
		String clientId = authentication.getOAuth2Request().getClientId();
		if (clientId == null || !clientId.equals(tokenRequest.getClientId())) {
			throw new InvalidGrantException("Wrong client for this refresh token: " + refreshTokenValue);
		}
		
		// clear out any access tokens already associated with the refresh
		// token.
		tokenStore.removeAccessTokenUsingRefreshToken(refreshToken);

		if (isExpired(refreshToken)) {
			tokenStore.removeRefreshToken(refreshToken);
			throw new InvalidTokenException("Invalid refresh token (expired): " + refreshToken);
		}
		
		validateIdpToken(authentication, clientId);

		authentication = createCustomRefreshedAuthentication(authentication, tokenRequest);

		if (!reuseRefreshToken) {
			tokenStore.removeRefreshToken(refreshToken);
			refreshToken = createCustomRefreshToken(authentication);
		}

		OAuth2AccessToken accessToken = createCustomAccessToken(authentication, refreshToken);
		tokenStore.storeAccessToken(accessToken, authentication);
		if (!reuseRefreshToken) {
			tokenStore.storeRefreshToken(refreshToken, authentication);
		}
		return accessToken;
	}
	
	private OAuth2Authentication createCustomRefreshedAuthentication(OAuth2Authentication authentication, TokenRequest request) {
		OAuth2Authentication narrowed;
		Set<String> scope = request.getScope();
		OAuth2Request clientAuth = authentication.getOAuth2Request().refresh(request);
		if (scope != null && !scope.isEmpty()) {
			Set<String> originalScope = clientAuth.getScope();
			if (originalScope == null || !originalScope.containsAll(scope)) {
				throw new InvalidScopeException("Unable to narrow the scope of the client authentication to " + scope
						+ ".", originalScope);
			}
			else {
				clientAuth = clientAuth.narrowScope(scope);
			}
		}
		narrowed = new OAuth2Authentication(clientAuth, authentication.getUserAuthentication());
		return narrowed;
	}
	
	private OAuth2RefreshToken createCustomRefreshToken(OAuth2Authentication authentication) {
		if (!isSupportRefreshToken(authentication.getOAuth2Request())) {
			return null;
		}
		int validitySeconds = getRefreshTokenValiditySeconds(authentication.getOAuth2Request());
		String value = UUID.randomUUID().toString();
		if (validitySeconds > 0) {
			return new DefaultExpiringOAuth2RefreshToken(value, new Date(System.currentTimeMillis()
					+ (validitySeconds * 1000L)));
		}
		return new DefaultOAuth2RefreshToken(value);
	}

	private OAuth2AccessToken createCustomAccessToken(OAuth2Authentication authentication, OAuth2RefreshToken refreshToken) {
		DefaultOAuth2AccessToken token = new DefaultOAuth2AccessToken(UUID.randomUUID().toString());
		int validitySeconds = getAccessTokenValiditySeconds(authentication.getOAuth2Request());
		if (validitySeconds > 0) {
			token.setExpiration(new Date(System.currentTimeMillis() + (validitySeconds * 1000L)));
		}
		token.setRefreshToken(refreshToken);
		token.setScope(authentication.getOAuth2Request().getScope());

		return accessTokenEnhancer != null ? accessTokenEnhancer.enhance(token, authentication) : token;
	}
	
	private void validateIdpToken(OAuth2Authentication authentication, String clientId) {
		
		ClientDetails clientDetails = clientDetailsService.loadClientByClientId(clientId);
		if(clientDetails != null && !clientDetails.getAuthorizedGrantTypes().contains(TrustedSourceRegistrationConstant.GRANT_TYPE_FEDERATED)) {
			return;
		}
		
		FederatedLoginAuthenticationToken userAuth = (FederatedLoginAuthenticationToken) authentication.getUserAuthentication();
		String idpRefreshToken = userAuth.getIdpRefreshToken();
	
		OauthTokenDetails details= federatedTokenValidationService.getAccessTokenBasedOnRefreshToken(idpRefreshToken, clientDetails);
		
		userAuth.setIdpAccessToken(details.getToken());
		userAuth.setIdpRefreshToken(details.getRefreshToken());
		
	}
	

}
