package com.nucleus.web.apimgmt.vo;

import java.io.Serializable;
import java.util.Map;

import org.springframework.http.HttpStatus;

public class CredentialValidationResponseDTO implements Serializable {
	
	private static final long serialVersionUID = 979423704211L;
	
	private boolean isAuthenticationValid;
	private HttpStatus httpStatus;
	private Map<String, Object> additionalData;
	private DetailedMessage detailedMessage;
	
	public CredentialValidationResponseDTO() {
		//default Constructor.
	}
	
	public CredentialValidationResponseDTO(boolean isAuthenticationValid, HttpStatus httpStatus, DetailedMessage detailedMessage) {
		this.isAuthenticationValid = isAuthenticationValid;
		this.httpStatus = httpStatus;
		this.detailedMessage = detailedMessage;
	}

	public boolean isAuthenticationValid() {
		return isAuthenticationValid;
	}
	public void setAuthenticationValid(boolean isAuthenticationValid) {
		this.isAuthenticationValid = isAuthenticationValid;
	}
	public HttpStatus getHttpStatus() {
		return httpStatus;
	}
	public void setHttpStatus(HttpStatus httpStatus) {
		this.httpStatus = httpStatus;
	}
	public Map<String, Object> getAdditionalData() {
		return additionalData;
	}
	public void setAdditionalData(Map<String, Object> additionalData) {
		this.additionalData = additionalData;
	}
	public DetailedMessage getDetailedMessage() {
		return detailedMessage;
	}
	public void setDetailedMessage(DetailedMessage detailedMessage) {
		this.detailedMessage = detailedMessage;
	}

	@Override
	public String toString() {
		return "CredentialValidationResponseDTO [ isAuthenticationValid=" + isAuthenticationValid + ", httpStatus="
				+ httpStatus + ", detailedMessage=" + detailedMessage + " ]";
	}
	
}
