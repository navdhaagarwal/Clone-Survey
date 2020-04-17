package com.nucleus.security.oauth.domainobject;

import javax.persistence.Entity;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.nucleus.core.annotations.Synonym;
import com.nucleus.entity.BaseEntity;

@Entity
@DynamicUpdate
@DynamicInsert
@NamedQueries({
	
	@NamedQuery(
			name="getTokenDetails",
			query = "select tokenDetails from OauthTokenDetails tokenDetails "+					
					"where tokenDetails.clientId = :clientId"
		),
	@NamedQuery(
			name="getTokenDetailsByUsername",
			query = "select tokenDetails from OauthTokenDetails tokenDetails "+					
					"where tokenDetails.clientId = :clientId and tokenDetails.userName=:userName "
		)	
})
@Synonym(grant="ALL")
public class OauthTokenDetails extends BaseEntity {

    private String           token;
	private String           clientId;
    private String           refreshToken;
    private String           scope;
    private String           userName;
    private Integer          expiryTime;
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
	public String getUserName() {
		return userName;
	}
	public void setUserName(String userName) {
		this.userName = userName;
	}
	public Integer getExpiryTime() {
		return expiryTime;
	}
	public void setExpiryTime(Integer expiryTime) {
		this.expiryTime = expiryTime;
	}

}
