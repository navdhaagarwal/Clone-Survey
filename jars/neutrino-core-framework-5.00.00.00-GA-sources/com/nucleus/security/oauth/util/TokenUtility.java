package com.nucleus.security.oauth.util;

import javax.inject.Named;

import org.joda.time.DateTime;
import org.joda.time.Seconds;

import com.nucleus.security.oauth.constants.RESTfulSecurityConstants;
import com.nucleus.security.oauth.domainobject.OauthTokenDetails;
import com.nucleus.security.oauth.vo.OauthTokenDetailsVo;

@Named("tokenUtil" )
public class TokenUtility {
	
	public boolean isExpiredToken(OauthTokenDetails tokenDetails) {
		int expirySeconds = tokenDetails.getExpiryTime();
		DateTime tokenCreationTime = tokenDetails.getEntityLifeCycleData().getLastUpdatedTimeStamp();
		if (tokenCreationTime == null) {
			tokenCreationTime=tokenDetails.getEntityLifeCycleData().getCreationTimeStamp();
		}
		DateTime now = DateTime.now();
		Seconds seconds = Seconds.secondsBetween( tokenCreationTime, now);
		return seconds.getSeconds() > (expirySeconds - RESTfulSecurityConstants.NETWORK_LATENCY);
	}
	
	public boolean isExpiredToken(OauthTokenDetailsVo tokenDetails) {
		int expirySeconds = tokenDetails.getExpiryTime();
		DateTime tokenCreationTime = tokenDetails.getUpdatedTimeStamp();
		if (tokenCreationTime == null) {
			tokenCreationTime=tokenDetails.getCreationTimeStamp();
		}
		DateTime now = DateTime.now();
		Seconds seconds = Seconds.secondsBetween( tokenCreationTime, now);
		return seconds.getSeconds() > (expirySeconds - RESTfulSecurityConstants.NETWORK_LATENCY);
	}
	
	public boolean isInvalidRefreshToken(String accessToken, org.json.simple.JSONObject json) {
		String error = (String) json.get(RESTfulSecurityConstants.ERROR_DESCRIPTION);
		return error.contains(RESTfulSecurityConstants.INVALID_REFRESH_TOKEN);
	}
}
