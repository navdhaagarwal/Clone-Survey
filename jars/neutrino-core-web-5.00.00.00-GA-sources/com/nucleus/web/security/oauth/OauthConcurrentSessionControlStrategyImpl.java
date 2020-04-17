package com.nucleus.web.security.oauth;

import java.util.LinkedHashMap;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.provider.TokenRequest;

import com.nucleus.security.oauth.dao.CustomOauthTokenStoreDAO;
import com.nucleus.security.oauth.domainobject.OauthAccessToken;
import com.nucleus.user.UserSessionManagerService;

public class OauthConcurrentSessionControlStrategyImpl implements OauthConcurrentSessionControlStrategy {
	
	@Inject
	@Named("userSessionManagerService")
	private UserSessionManagerService    userSessionManagerService;
	
	//Conditional bean that might be null if API portal is enabled.
	@Autowired(required = false)
	private CustomOauthTokenStoreDAO tokenStore;
	
	 private int maximumSessions = -1;
	 
	 
	 private int maxExternalClients = -1;


	public UserSessionManagerService getUserSessionManagerService() {
		return userSessionManagerService;
	}

	public void setUserSessionManagerService(
			UserSessionManagerService userSessionManagerService) {
		this.userSessionManagerService = userSessionManagerService;
	}
	
    @Override
	public int getMaximumSessions() {
		return maximumSessions;
	}

	public void setMaximumSessions(int maximumSessions) {
		this.maximumSessions = maximumSessions;
	}

	public CustomOauthTokenStoreDAO getTokenStore() {
		return tokenStore;
	}

	public void setTokenStore(CustomOauthTokenStoreDAO tokenStore) {
		this.tokenStore = tokenStore;
	}
	
	public int getMaxExternalClients() {
		return maxExternalClients;
	}

	public void setMaxExternalClients(int maxExternalClients) {
		this.maxExternalClients = maxExternalClients;
	}

	public boolean validateOauthReqeust(TokenRequest tokenRequest)
	{
		if (tokenStore == null) {
			return false;
		}
		Map<String, String> parameters = new LinkedHashMap<String, String>(tokenRequest.getRequestParameters());
		String clientId = tokenRequest.getClientId();
		String grant_type = tokenRequest.getGrantType();

		if (grant_type != null && "password".equals(grant_type)) {

			String username = parameters.get("username");
			return validateOauthUser(username, clientId);
		}
		if (grant_type != null && "refresh_token".equals(grant_type)) {
			String refreshToken = parameters.get("refresh_token");
			String username = getUserForRefreshToken(refreshToken);
			if (username == null) {
				return true;
			}
			validateOauthUser(username, clientId);
		}

		return true;
	}
	
	/**
	 * 
	 * 
	 * 
	 */
	@Override
	public boolean validateOauthAnonymousReqeust(TokenRequest tokenRequest) {
		if (tokenStore == null) {
			return false;
		}
		String clientId = tokenRequest.getClientId();
		String grant_type =	tokenRequest.getGrantType();
		if (grant_type != null && "anonymous".equals(grant_type)) {
			return validateAnonymousUser(clientId) ;
		}
		return false;
	}
	
	
	
	private boolean validateAnonymousUser(String clientId){
		int oauthUsersCount=0;
		oauthUsersCount=tokenStore.findActiveTokensForExternalClients(clientId);
		if(oauthUsersCount < maxExternalClients){
			return true;
		}
		if (maximumSessions == -1) {
            // We permit unlimited logins
            return true;
        }


	
		return false;
	}
	
	
	
	private boolean validateOauthUser(String username,String clientId) 
	{
		int webUserCount=userSessionManagerService.getCurrentWebUserSessionCount(username);
		int oauthUsersCount=0;
	
		oauthUsersCount=tokenStore.findActiveTokensCountByUserNameWithOtherClients(username,clientId);
		
		int loggedInUsers=webUserCount+oauthUsersCount;
		  if (loggedInUsers < maximumSessions) {
	            // They haven't got too many login sessions running at present
	            return true;
	        }

	        if (maximumSessions == -1) {
	            // We permit unlimited logins
	            return true;
	        }


		
	return false;
	
}
	private String getUserForRefreshToken(String refreshToken) 
	{
       String username=null;
       OauthAccessToken oauthAccessToken=  tokenStore.findTokensByRefreshToken(refreshToken);
       if(oauthAccessToken!=null)
       {
    	   username= oauthAccessToken.getUserName();
       }
		
	return username;
	
}
	
}