package com.nucleus.infinispan.console.entity;

import javax.persistence.Entity;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.nucleus.authenticationToken.AuthenticationToken;


/**
 * 
 * @author gajendra.jatav
 *
 */
@Entity
@DynamicUpdate
@DynamicInsert
public class ClusterCommunicationToken extends AuthenticationToken{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private String communicationTokenCode;
	
	private Boolean isValidToken=true;
	
	public String getCommunicationTokenCode() {
		return communicationTokenCode;
	}

	public void setCommunicationTokenCode(String communicationTokenCode) {
		this.communicationTokenCode = communicationTokenCode;
	}

	public Boolean getIsValidToken() {
		return isValidToken;
	}

	public void setIsValidToken(Boolean isValidToken) {
		this.isValidToken = isValidToken;
	}

	
}
