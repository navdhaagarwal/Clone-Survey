package com.nucleus.security.oauth.businessobject;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.nucleus.core.common.NeutrinoRestTemplateFactory;
import com.nucleus.core.json.util.GsonUtil;
import com.nucleus.core.jwt.util.JotUtil;
import com.nucleus.finnone.pro.base.Message;
import com.nucleus.finnone.pro.base.Message.MessageType;
import com.nucleus.finnone.pro.base.exception.ExceptionBuilder;
import com.nucleus.finnone.pro.base.exception.SystemException;
import com.nucleus.finnone.pro.base.utility.CoreUtility;
import com.nucleus.finnone.pro.general.constants.ExceptionSeverityEnum;
import com.nucleus.logging.BaseLoggers;
import com.nucleus.security.oauth.constants.RESTfulSecurityConstants;
import com.nucleus.security.oauth.constants.RestfulMessageConstant;
import com.nucleus.security.oauth.domainobject.OauthTokenDetails;
import com.nucleus.security.oauth.util.TokenUtility;


@Named("tokenBusinessObject")
public class RestfulTokenBusinessObjectImpl implements RestfulTokenBusinessObject{
	@Inject
	@Named("tokenUtil")
	private TokenUtility tokenUtil;
	@Autowired
	JSONParser jsonParser ;
	
	@Inject
	@Named("coreUtility")
	private CoreUtility coreUtility;
	
	@Value("${oauth2.token.restTemplate.max_conn_total}")
	Integer maxConnTotal;
	
	@Value("${oauth2.token.restTemplate.default_max_conn_per_route}")
	Integer defaultMaxPerRoute;
	
	@Value("${oauth2.token.restTemplate.read_time_out}")
	Integer readTimeOut;

	@Value("${oauth2.token.restTemplate.conn_time_out}")
	Integer connTimeOut;

	@Value("${oauth2.token.restTemplate.conn_request_time_out}")
	Integer connRequestTimeOut;
	
	private RestTemplate restTemplate;
	
    @Autowired
    private NeutrinoRestTemplateFactory neutrinoRestTemplateFactory;
    
	public JSONParser getJsonParser() {
		return jsonParser;
	}
	public void setJsonParser(JSONParser jsonParser) {
		this.jsonParser = jsonParser;
	}
	
	@PostConstruct
	void initializeRestTemplate() {
		this.restTemplate = neutrinoRestTemplateFactory.createRestTemplate(maxConnTotal, defaultMaxPerRoute,
				readTimeOut, connRequestTimeOut, connTimeOut);
	}
	
	@Override
	public void prepareHttpEntity(HttpPost httppost,
			List<NameValuePair> nameValuePairs) {
		try {
			httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
		} catch (UnsupportedEncodingException e1) {
			BaseLoggers.exceptionLogger.error(
					RESTfulSecurityConstants.EXCEPTION_MSG, e1);
			
			Message message = new Message(RestfulMessageConstant.UNSUPPORTED_ENCODING_EXCEPTION,MessageType.ERROR);
			throw ExceptionBuilder.getInstance(SystemException.class).setMessage(message)
			.setSeverity(ExceptionSeverityEnum.SEVERITY_HIGH.getEnumValue()).build();
		}
	}
	@Override
	public OauthTokenDetails getAccessToken(String url, MultiValueMap<String, String> requestParamMap,
			String clientId, String userName) 
	{
		OauthTokenDetails tokenDetails = null;
		String refreshToken;
		String scope;
		int expiryTime;
		String accessToken = null;
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

		ResponseEntity<JSONObject> tokenResponse;
		HttpEntity<MultiValueMap<String, String>> httpEntity = new HttpEntity<>(requestParamMap, headers);
		try {

			tokenResponse = restTemplate.postForEntity(url, httpEntity, JSONObject.class);

			JSONObject json = tokenResponse.getBody();
			if (json == null) {
				Message message = new Message(RESTfulSecurityConstants.EXCEPTION_MSG, MessageType.ERROR);
				throw ExceptionBuilder.getInstance(SystemException.class).setMessage(message)
						.setSeverity(ExceptionSeverityEnum.SEVERITY_HIGH.getEnumValue()).build();
			}
			accessToken = (String) json.get(RESTfulSecurityConstants.ACCESS_TOKEN);
			if (tokenResponse.getStatusCodeValue() == 200 || tokenUtil.isInvalidRefreshToken(accessToken, json)) {
				if (accessToken != null) {
					refreshToken = (String) json.get(RESTfulSecurityConstants.REFRESH_TOKEN);
					expiryTime = getExpiryTime(json.get(RESTfulSecurityConstants.EXPIRY_TIME));
					scope = (String) json.get(RESTfulSecurityConstants.SCOPE);
					tokenDetails = new OauthTokenDetails();
					tokenDetails.setClientId(clientId);
					tokenDetails.setExpiryTime(expiryTime);
					tokenDetails.setUserName(userName);
					tokenDetails.setScope(scope);
					tokenDetails.setRefreshToken(refreshToken);
					tokenDetails.setToken(accessToken);
				}
			}
		} catch (RestClientException e) {
			BaseLoggers.exceptionLogger.error(RESTfulSecurityConstants.EXCEPTION_MSG, e);
			Message message = new Message(RestfulMessageConstant.IOEXCEPTION, MessageType.ERROR);
			throw ExceptionBuilder.getInstance(SystemException.class).setMessage(message).setOriginalException(e)
					.setSeverity(ExceptionSeverityEnum.SEVERITY_HIGH.getEnumValue()).build();

		}

		return tokenDetails;

	}
	
	@Override
	public Map<String, List<String>> getLoggedInUsersTrustedSourceDetails(String url, String token, String clientId) 
	{
		Map<String, List<String>> loggedInUsersTrustedSourceDetails = new HashMap<String, List<String>> ();

		try {
			Map<String, String> requestParameters = new HashMap<String, String>();
			requestParameters.put(RESTfulSecurityConstants.ACCESS_TOKEN, token);
			requestParameters.put(RESTfulSecurityConstants.CLIENT_ID, clientId);
			
			Map<String, String> encryptedParameters = JotUtil.encrypt("usertrustedsourcerequest", requestParameters);
			
			HttpHeaders headers = new HttpHeaders();
			headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_UTF8_VALUE);
			headers.add(RESTfulSecurityConstants.ACCESS_TOKEN, token);
			HttpEntity<Map<String, String>> httpEntity = new HttpEntity<>(encryptedParameters,headers);
			
			ResponseEntity<Map> response = this.restTemplate.exchange(url, HttpMethod.POST, httpEntity, Map.class);
			
			Map responseMap = (Map) JotUtil.decrypt("usertrustedsourceresponse", response.getBody(), Map.class);
			
			String json = (String)responseMap.get("resultmap");
			
			if (json == null) {
				Message message = new Message(RESTfulSecurityConstants.GET_LOGGED_IN_USERS_TRUSTED_SOURCE_ERROR_MSG, MessageType.ERROR);
				throw ExceptionBuilder.getInstance(SystemException.class).setMessage(message)
						.setSeverity(ExceptionSeverityEnum.SEVERITY_HIGH.getEnumValue()).build();
			}
			
			if (response.getStatusCodeValue() == 200) {
				loggedInUsersTrustedSourceDetails = GsonUtil.parseJson(json,getTypeForGsonConversion());
			}
			
		} catch (RestClientException | JsonSyntaxException e) {
			BaseLoggers.exceptionLogger.error(RESTfulSecurityConstants.GET_LOGGED_IN_USERS_TRUSTED_SOURCE_ERROR_MSG, e);
			Message message = new Message(RestfulMessageConstant.IOEXCEPTION, MessageType.ERROR);
			
			throw ExceptionBuilder.getInstance(SystemException.class).setMessage(message).setOriginalException(e)
					.setSeverity(ExceptionSeverityEnum.SEVERITY_HIGH.getEnumValue()).build();
		}
		return loggedInUsersTrustedSourceDetails;
	}
	
	@Override
	public String revokeTokenByUsers(String url, String token, RevokeTokenDTO revokeTokensDTO) {
		HttpHeaders headers = new HttpHeaders();
		headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_UTF8_VALUE);
		headers.add(RESTfulSecurityConstants.ACCESS_TOKEN, token);
		//ResponseEntity<String> response;
		
		Map<String, String> encryptedParameters = JotUtil.encrypt("revokerequest", revokeTokensDTO);
		
		HttpEntity<Map> httpEntity = new HttpEntity<>(encryptedParameters, headers);
		
		try {
			//response = restTemplate.postForEntity(url, httpEntity, String.class);
			
			ResponseEntity<Map> response = this.restTemplate.exchange(url, HttpMethod.POST, httpEntity, Map.class);
			
			Map responseMap = (Map) JotUtil.decrypt("revokeresponse", response.getBody(), Map.class);
			
			String json = (String)responseMap.get("revokerequest");
			
			if (json == null) {
				Message message = new Message(RESTfulSecurityConstants.REVOKE_TOKENS_BY_USER_ERROR_MSG, MessageType.ERROR);
				throw ExceptionBuilder.getInstance(SystemException.class).setMessage(message)
						.setSeverity(ExceptionSeverityEnum.SEVERITY_HIGH.getEnumValue()).build();
			}
			
			if (response.getStatusCodeValue() == 200) {
				return "success";
			}
			
		} catch (RestClientException e) {
			BaseLoggers.exceptionLogger.error(RESTfulSecurityConstants.REVOKE_TOKENS_BY_USER_ERROR_MSG, e);
			Message message = new Message(RestfulMessageConstant.IOEXCEPTION, MessageType.ERROR);
			throw ExceptionBuilder.getInstance(SystemException.class).setMessage(message).setOriginalException(e)
					.setSeverity(ExceptionSeverityEnum.SEVERITY_HIGH.getEnumValue()).build();
		}
		return "failed";
	}
	
	private int getExpiryTime(Object expiryTimeObj) {
		if (expiryTimeObj instanceof Long) {
			return ((Long) expiryTimeObj).intValue();
		}
		if(expiryTimeObj instanceof String) {
			return Integer.valueOf((String) expiryTimeObj);
		}
		return (Integer) expiryTimeObj;

	}
	
	@Override
	public MultiValueMap<String, String> prepareRequestParamForPassGrant(String username, String clientId,
			String clientSecret, String password) {
		MultiValueMap<String, String> requestParamForPassGrant = new LinkedMultiValueMap<>();
		requestParamForPassGrant.add(RESTfulSecurityConstants.GRANT_TYPE, RESTfulSecurityConstants.PASS_WORD);
		requestParamForPassGrant.add(RESTfulSecurityConstants.USERNAME, username);
		requestParamForPassGrant.add(RESTfulSecurityConstants.CLIENT_ID, clientId);
		requestParamForPassGrant.add(RESTfulSecurityConstants.CLIENT_SECRET, clientSecret);
		requestParamForPassGrant.add(RESTfulSecurityConstants.PASS_WORD, password);
		return requestParamForPassGrant;
	}

	@Override
	public MultiValueMap<String, String> prepareRequestParamForAnonymousGrant(String clientId,
			String clientSecret) {
		MultiValueMap<String, String> requestParamForPassGrant = new LinkedMultiValueMap<>();
		if(coreUtility.isApiManagerEnabled()) {
			requestParamForPassGrant.add(RESTfulSecurityConstants.GRANT_TYPE, RESTfulSecurityConstants.CLIENT_CREDENTIALS);
		}
		else {
		requestParamForPassGrant.add(RESTfulSecurityConstants.GRANT_TYPE, RESTfulSecurityConstants.ANONYMOUS);
		}
		requestParamForPassGrant.add(RESTfulSecurityConstants.CLIENT_ID, clientId);
		requestParamForPassGrant.add(RESTfulSecurityConstants.CLIENT_SECRET, clientSecret);
		return requestParamForPassGrant;
	}
	
	
	@Override
	public MultiValueMap<String, String> prepareRequestParamForRefreshTokenGrant(String refreshToken, String clientId,
			String clientSecret) {
		MultiValueMap<String, String> requestParamForRefreshTokenGrant = new LinkedMultiValueMap<>();
		requestParamForRefreshTokenGrant.add(RESTfulSecurityConstants.GRANT_TYPE,
				RESTfulSecurityConstants.REFRESH_TOKEN);
		requestParamForRefreshTokenGrant.add(RESTfulSecurityConstants.REFRESH_TOKEN, refreshToken);
		requestParamForRefreshTokenGrant.add(RESTfulSecurityConstants.CLIENT_ID, clientId);
		requestParamForRefreshTokenGrant.add(RESTfulSecurityConstants.CLIENT_SECRET, clientSecret);
		return requestParamForRefreshTokenGrant;
	}
	
	@Override
	public MultiValueMap<String, String> prepareRequestParamForRevokeTokenByUsers(List<String> usernameList, String clientID) {
		MultiValueMap<String, String> requestParamForRevokeAllTokens = new LinkedMultiValueMap<>();
		requestParamForRevokeAllTokens.put(RESTfulSecurityConstants.USERNAME, usernameList);
		requestParamForRevokeAllTokens.add(RESTfulSecurityConstants.CLIENT_ID, clientID);
		return requestParamForRevokeAllTokens;
	}
	
	private Type getTypeForGsonConversion() {
		return new TypeToken<Map<String, List<String>>>() {}.getType();
	}

}
