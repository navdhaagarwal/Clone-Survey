package com.nucleus.security.oauth.dao;

import com.nucleus.security.oauth.domainobject.OauthTokenDetails;

public interface OauthTokenDao {
	
	OauthTokenDetails getTokenDetails(String clientId, String userName);
	
	public void saveOrupdateTokenDetails(OauthTokenDetails tokenDetailsFromDB, OauthTokenDetails tokenDetails);
	
}
