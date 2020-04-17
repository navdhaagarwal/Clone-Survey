package com.nucleus.web.apimgmt.vo;

import java.io.Serializable;
import java.util.Map;

/**
 * This DTO being used for two request of different type.
 * Two different requests are differentiated based on <code>grantType</code>.
 * 
 * @author syambrij.maurya
 *
 */
public class CredentialValidationRequestDTO implements Serializable {
	
	private static final long serialVersionUID = 979423704210L;
	private String username;
	private String password;
	private String deviceId;
	private String deviceType;
	private String grantType;
	private Map<String, Object> additionalData;
	
	public CredentialValidationRequestDTO() {
		//default constructor.
	}
	
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public String getDeviceId() {
		return deviceId;
	}
	public void setDeviceId(String deviceId) {
		this.deviceId = deviceId;
	}
	public String getDeviceType() {
		return deviceType;
	}
	public void setDeviceType(String deviceType) {
		this.deviceType = deviceType;
	}
	public String getGrantType() {
		return grantType;
	}
	public void setGrantType(String grantType) {
		this.grantType = grantType;
	}
	public Map<String, Object> getAdditionalProperties() {
		return additionalData;
	}
	public void setAdditionalProperties(Map<String, Object> additionalProperties) {
		this.additionalData = additionalProperties;
	}

	@Override
	public String toString() {
		return "CredentialValidationRequestDTO [ username=" + username + ", password=" + password + ", deviceId="
				+ deviceId + ", deviceType=" + deviceType + ", grantType=" + grantType + " ]";
	}
	
	
}
