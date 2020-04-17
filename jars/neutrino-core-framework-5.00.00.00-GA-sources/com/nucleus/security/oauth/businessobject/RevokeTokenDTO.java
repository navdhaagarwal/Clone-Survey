package com.nucleus.security.oauth.businessobject;

import java.io.Serializable;
import java.util.List;

public class RevokeTokenDTO implements Serializable {

	private static final long serialVersionUID = 1L;

	private String clientID;

	private List<String> usernameList;

	private String accessToken;
	
	public RevokeTokenDTO() {
		super();
	}

	public String getAccessToken() {
		return accessToken;
	}

	public void setAccessToken(String accessToken) {
		this.accessToken = accessToken;
	}

	public String getClientID() {
		return clientID;
	}

	public void setClientID(String clientID) {
		this.clientID = clientID;
	}

	public List<String> getUsernameList() {
		return usernameList;
	}

	public void setUsernameList(List<String> usernameList) {
		this.usernameList = usernameList;
	}
}
