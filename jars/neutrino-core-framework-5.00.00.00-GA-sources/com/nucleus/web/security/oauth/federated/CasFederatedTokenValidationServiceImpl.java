package com.nucleus.web.security.oauth.federated;

import java.util.Map;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.common.exceptions.InvalidRequestException;
import org.springframework.security.oauth2.provider.ClientDetails;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.nucleus.core.common.NeutrinoRestTemplateFactory;
import com.nucleus.logging.BaseLoggers;
import com.nucleus.security.oauth.TrustedSourceInfo;
import com.nucleus.security.oauth.constants.RESTfulSecurityConstants;
import com.nucleus.security.oauth.domainobject.OauthTokenDetails;
import com.nucleus.web.security.rest.exception.RestApiErrorHandler;

public class CasFederatedTokenValidationServiceImpl implements FederatedTokenValidationService {
	
	@Autowired
    private NeutrinoRestTemplateFactory neutrinoRestTemplateFactory;
	
	private RestTemplate restTemplate;
	
	@Value("${INTG_BASE_URL}/restservice/idp/oauth/token")
	private String idpTokenEndpointUrl;
	
	@Inject
	@Named("casRestApiErrorHandler")
	RestApiErrorHandler restApiErrorHandler;
	
	@PostConstruct
	void initializeRestTemplate() {
		this.restTemplate = neutrinoRestTemplateFactory.createRestTemplate(null, null,
				null, null, null);
	}
	
	@Override
	public OauthTokenDetails getAccessTokenBasedOnAuthCode(String token, ClientDetails client) {
		MultiValueMap<String, String> requestParam = null;
		
		requestParam = prepareRequestParamForIdp(token, client);
		
		return sendRestCallToIdP(requestParam);
	}
	
	@Override
	public OauthTokenDetails getAccessTokenBasedOnRefreshToken(String idpRefreshToken, ClientDetails client) {
		MultiValueMap<String, String> requestParam = null;
		
		requestParam = prepareRequestParamForRefreshTokenRequest(idpRefreshToken, client);
		
		return sendRestCallToIdP(requestParam);
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private OauthTokenDetails sendRestCallToIdP(MultiValueMap<String, String> requestParam) {
		OauthTokenDetails tokenDetails = null;
		try {
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
			HttpEntity<MultiValueMap<String, String>> httpEntity = new HttpEntity<>(requestParam, headers);
			ResponseEntity<Map> tokenResponse = restTemplate.postForEntity(idpTokenEndpointUrl, httpEntity, Map.class);
			Map<String, String> responseMap = tokenResponse.getBody();
			restApiErrorHandler.handlerErrorCodes(tokenResponse.getStatusCode(), responseMap);
			String accessToken = responseMap.get(RESTfulSecurityConstants.ACCESS_TOKEN);
			if(!StringUtils.isEmpty(accessToken)) {
				tokenDetails = new OauthTokenDetails();
				tokenDetails.setToken(accessToken);
				tokenDetails.setRefreshToken(responseMap.get(RESTfulSecurityConstants.REFRESH_TOKEN));
			}else {
				BaseLoggers.exceptionLogger.error("The parameters sent for communication with IdP were incorrect: {}", responseMap.get("error"));
				throw new InvalidRequestException("The parameters sent for communication with IdP were incorrect");
			}
			
		}catch(ResourceAccessException e) {
			BaseLoggers.exceptionLogger.error(RESTfulSecurityConstants.EXCEPTION_MSG, e);
			throw new InvalidRequestException("Failed to get the IdP resource");
		}catch(HttpClientErrorException e) {
			BaseLoggers.exceptionLogger.error(RESTfulSecurityConstants.EXCEPTION_MSG, e);
			throw new InvalidRequestException("Error occurred in communicating with the IdP server.");
		}catch(HttpServerErrorException e) {
			BaseLoggers.exceptionLogger.error(RESTfulSecurityConstants.EXCEPTION_MSG, e);
			throw new InvalidRequestException("Error occurred on the IdP server while authenticating the user.");
		}catch(RestClientException e) {
			BaseLoggers.exceptionLogger.error(RESTfulSecurityConstants.EXCEPTION_MSG, e);
			throw new InvalidRequestException("Error occured while retrieving access token from IdP");
		}
		
		return tokenDetails;
		
	}
	
	private MultiValueMap<String, String> prepareRequestParamForIdp(String idpAuthCode, ClientDetails client){
		MultiValueMap<String, String> requestParam = new LinkedMultiValueMap<>();
		TrustedSourceInfo trustedSourceInfo = (TrustedSourceInfo) client;
		requestParam.add(RESTfulSecurityConstants.CLIENT_ID, trustedSourceInfo.getIdpClientId());
		requestParam.add(RESTfulSecurityConstants.CLIENT_SECRET, trustedSourceInfo.getIdpClientSecret());
		requestParam.add(RESTfulSecurityConstants.GRANT_TYPE, RESTfulSecurityConstants.AUTHORIZATION_CODE);
		requestParam.add(RESTfulSecurityConstants.REDIRECT_URI, trustedSourceInfo.getRedirectUri());
		requestParam.add(RESTfulSecurityConstants.CODE, idpAuthCode);
		
		return requestParam;
	}
	
	private MultiValueMap<String, String> prepareRequestParamForRefreshTokenRequest(String refreshToken, ClientDetails client){
		MultiValueMap<String, String> requestParam = new LinkedMultiValueMap<>();
		TrustedSourceInfo trustedSourceInfo = (TrustedSourceInfo) client;
		requestParam.add(RESTfulSecurityConstants.CLIENT_ID, trustedSourceInfo.getIdpClientId());
		requestParam.add(RESTfulSecurityConstants.CLIENT_SECRET, trustedSourceInfo.getIdpClientSecret());
		requestParam.add(RESTfulSecurityConstants.GRANT_TYPE, RESTfulSecurityConstants.REFRESH_TOKEN);
		requestParam.add(RESTfulSecurityConstants.REFRESH_TOKEN, refreshToken);
		return requestParam;
	}

}
