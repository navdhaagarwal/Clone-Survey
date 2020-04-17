package com.nucleus.security.oauth.vo;

import java.io.Serializable;

import org.joda.time.DateTime;

import com.nucleus.security.oauth.domainobject.OauthTokenDetails;

public class OauthTokenDetailsVo implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String token;
	private String clientId;
	private String refreshToken;
	private String scope;
	private Integer expiryTime;
	public DateTime creationTimeStamp;
	public DateTime updatedTimeStamp;
	
	public OauthTokenDetailsVo(OauthTokenDetails oauthTokenDetails) {
		setToken(oauthTokenDetails.getToken());
		setClientId(oauthTokenDetails.getClientId());
		setRefreshToken(oauthTokenDetails.getRefreshToken());
		setScope(oauthTokenDetails.getScope());
		setExpiryTime(oauthTokenDetails.getExpiryTime());
		setCreationTimeStamp(DateTime.now());
		setUpdatedTimeStamp(DateTime.now());
	}
	
	public DateTime getCreationTimeStamp() {
		return creationTimeStamp;
	}

	public void setCreationTimeStamp(DateTime creationTimeStamp) {
		this.creationTimeStamp = creationTimeStamp;
	}

	public DateTime getUpdatedTimeStamp() {
		return updatedTimeStamp;
	}

	public void setUpdatedTimeStamp(DateTime updatedTimeStamp) {
		this.updatedTimeStamp = updatedTimeStamp;
	}


	public OauthTokenDetailsVo() {

	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public String getClientId() {
		return clientId;
	}

	public void setClientId(String clientId) {
		this.clientId = clientId;
	}

	public String getRefreshToken() {
		return refreshToken;
	}

	public void setRefreshToken(String refreshToken) {
		this.refreshToken = refreshToken;
	}

	public String getScope() {
		return scope;
	}

	public void setScope(String scope) {
		this.scope = scope;
	}

	public Integer getExpiryTime() {
		return expiryTime;
	}

	public void setExpiryTime(Integer expiryTime) {
		this.expiryTime = expiryTime;
	}

}
