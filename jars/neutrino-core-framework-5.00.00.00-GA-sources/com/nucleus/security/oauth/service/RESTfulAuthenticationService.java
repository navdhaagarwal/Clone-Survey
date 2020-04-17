package com.nucleus.security.oauth.service;

import java.util.List;
import java.util.Map;

import com.nucleus.security.oauth.vo.OauthTokenDetailsVo;

public interface RESTfulAuthenticationService {
	
	String getSecurityToken(String clientID);

	Map<String, List<String>> getLoggedInUsersTrustedSourceDetails(String url, String clientId);

	String revokeTokenByUsers(String url, String clientId, List<String> usernames);

	OauthTokenDetailsVo getSecurityToken(String clientId, String userName, String password);
	
	OauthTokenDetailsVo getSecurityToken(String clientId, String refreshToken);
}
