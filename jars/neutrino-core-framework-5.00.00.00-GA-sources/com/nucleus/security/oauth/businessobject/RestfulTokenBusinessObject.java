package com.nucleus.security.oauth.businessobject;

import java.util.List;
import java.util.Map;

import org.apache.http.NameValuePair;
import org.apache.http.client.methods.HttpPost;
import org.springframework.util.MultiValueMap;

import com.nucleus.security.oauth.domainobject.OauthTokenDetails;

public interface RestfulTokenBusinessObject {
	public void prepareHttpEntity(HttpPost httppost,
			List<NameValuePair> nameValuePairs) ;
	public OauthTokenDetails getAccessToken(String tokenUrl, MultiValueMap<String, String> requestParamForRefreshTokenGrant,
			String clientId, String username);
	public MultiValueMap<String, String> prepareRequestParamForPassGrant(String username,String clientId,String clientSecret,String password);
	public MultiValueMap<String, String> prepareRequestParamForAnonymousGrant(String clientId,String clientSecret);
	public MultiValueMap<String, String> prepareRequestParamForRefreshTokenGrant(String refreshToken,String clientId,String clientSecret);
	public Map<String, List<String>> getLoggedInUsersTrustedSourceDetails(String url, String token, String clientId);
	public String revokeTokenByUsers(String url, String token, RevokeTokenDTO revokeTokenDTO);
	public MultiValueMap<String, String> prepareRequestParamForRevokeTokenByUsers(List<String> usernameList, String clientID);
}
