package com.nucleus.web.security.oauth;

import java.util.LinkedHashMap;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.lang3.StringUtils;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.common.exceptions.InvalidGrantException;
import org.springframework.security.oauth2.common.exceptions.InvalidRequestException;
import org.springframework.security.oauth2.provider.ClientDetails;
import org.springframework.security.oauth2.provider.ClientDetailsService;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.OAuth2Request;
import org.springframework.security.oauth2.provider.OAuth2RequestFactory;
import org.springframework.security.oauth2.provider.TokenRequest;
import org.springframework.security.oauth2.provider.token.AbstractTokenGranter;
import org.springframework.security.oauth2.provider.token.AuthorizationServerTokenServices;

import com.nucleus.security.oauth.constants.RESTfulSecurityConstants;
import com.nucleus.security.oauth.domainobject.OauthTokenDetails;
import com.nucleus.user.User;
import com.nucleus.user.UserService;
import com.nucleus.user.login.UserLoginDetailsChecker;
import com.nucleus.web.security.federated.FederatedLoginAuthenticationToken;
import com.nucleus.web.security.oauth.federated.FederatedTokenValidationService;
import com.nucleus.web.security.oauth.util.NeutrinoTokenUtility;

/**
 * 
 * The class handles federated grant type. It first checks the auth code of the
 * IdP and then allows to generate access token
 * 
 * @author namrata.varshney
 *
 */
public class FederatedAuthorizationCodeTokenGranter extends AbstractTokenGranter {
	
	private static final String GRANT_TYPE = "federated";
	
	@Inject
	@Named("federatedOauthValidationService")
	FederatedTokenValidationService federatedTokenValidationService;
	
	@Inject
	@Named("userService")
	UserService userService;
	
	@Inject
	@Named("neutrinoTokenUtility")
	private NeutrinoTokenUtility neutrinoTokenUtility;
	
	@Inject
	@Named("userLoginDetailsChecker")
	private UserLoginDetailsChecker userchecker;
	
	public FederatedAuthorizationCodeTokenGranter(AuthorizationServerTokenServices tokenServices,
			ClientDetailsService clientDetailsService, OAuth2RequestFactory requestFactory) {
		this(tokenServices, clientDetailsService, requestFactory, GRANT_TYPE);
	}

	protected FederatedAuthorizationCodeTokenGranter(AuthorizationServerTokenServices tokenServices,
			ClientDetailsService clientDetailsService, OAuth2RequestFactory requestFactory, String grantType) {
		super(tokenServices, clientDetailsService, requestFactory, grantType);
	}
	
	@Override
	protected OAuth2Authentication getOAuth2Authentication(ClientDetails client, TokenRequest tokenRequest) {
		OauthTokenDetails idpTokenDetails = validateTokenRequest(client, tokenRequest);
		
		Map<String, String> parameters = new LinkedHashMap<>(tokenRequest.getRequestParameters());
		String idpAuthCode = parameters.get(RESTfulSecurityConstants.CODE);
		String username = parameters.get(RESTfulSecurityConstants.USERNAME);
		
		if(username==null){
			throw new AuthenticationServiceException("username is null");
		}
		
		username = username.toLowerCase();
		
		User user = userService.findUserByUsername(username);
		
		if(user==null) {
			throw new AuthenticationServiceException("User does not exists");
		}
		
		userchecker.validateLoginUser(user);
		
		Authentication userAuth = new FederatedLoginAuthenticationToken(username, idpAuthCode, idpTokenDetails.getToken(), idpTokenDetails.getRefreshToken());
		
		OAuth2Request storedOAuth2Request = getRequestFactory().createOAuth2Request(client, tokenRequest);
		return new OAuth2Authentication(storedOAuth2Request, userAuth);	
	}
	
	@Override
	protected OAuth2AccessToken getAccessToken(ClientDetails client, TokenRequest tokenRequest) {
		OAuth2AccessToken token  = super.getAccessToken(client, tokenRequest);
		token = neutrinoTokenUtility.setAdditionalInfoInToken(tokenRequest, token);
		return token;
	}
	
	private OauthTokenDetails validateTokenRequest(ClientDetails client, TokenRequest tokenRequest) {
		Map<String, String> parameters = new LinkedHashMap<>(tokenRequest.getRequestParameters());
		String idpAuthCode = parameters.get(RESTfulSecurityConstants.CODE);
		
		if (StringUtils.isEmpty(idpAuthCode)) {
			throw new InvalidRequestException("An authorization code must be supplied.");
		}
		
		OauthTokenDetails tokenDetails = null;
		if(!StringUtils.isEmpty(idpAuthCode)) {
			tokenDetails = federatedTokenValidationService.getAccessTokenBasedOnAuthCode(idpAuthCode, client);
		}
		
		if(tokenDetails==null) {
			throw new InvalidGrantException("Invalid request send to the IdP server");
		}
		
		return tokenDetails;
	}
	
	
}
