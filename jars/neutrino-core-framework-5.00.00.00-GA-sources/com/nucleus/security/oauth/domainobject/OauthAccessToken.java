package com.nucleus.security.oauth.domainobject;

import java.sql.Blob;

import javax.persistence.Entity;
import javax.persistence.Index;
import javax.persistence.Lob;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.nucleus.core.annotations.Synonym;
import com.nucleus.entity.BaseEntity;
@Entity
@DynamicUpdate
@DynamicInsert
@Table(indexes = { @Index(name = "OAUTH_ACCESS_TOKEN_IDX1", columnList = "authenticationId") ,
					@Index(name="OAUTH_ACCESS_TOKEN_IDX2",columnList ="tokenId"),
					@Index(name="OAUTH_ACCESS_TOKEN_IDX3",columnList ="userName"),
					@Index(name="OAUTH_ACCESS_TOKEN_IDX4",columnList ="clientId"),
					@Index(name="OAUTH_ACCESS_TOKEN_IDX5",columnList ="refreshToken"),
                    @Index(name="OAUTH_ACCESS_TOKEN_IDX6",columnList ="userName,clientId")})
@NamedQueries({
@NamedQuery(name = "getOauthAccessTokenDetailsByrefreshToken",query = "select accessToken from OauthAccessToken accessToken where accessToken.refreshToken = :refreshToken"),	
@NamedQuery(name = "getOauthAccessTokenDetailsByAuthID",query = "select accessToken from OauthAccessToken accessToken where accessToken.authenticationId = :authenticationId"),		
@NamedQuery(name = "getOauthAccessTokenDetailsByTokenID",query = "select accessToken from OauthAccessToken accessToken where accessToken.tokenId = :tokenId"),
@NamedQuery(name = "getOauthAccessTokenDetailsByClientID",query = "select accessToken from OauthAccessToken accessToken where accessToken.clientId = :clientId"),
@NamedQuery(name = "getOauthAccessTokenDetailsByUserName",query = "select accessToken from OauthAccessToken accessToken where accessToken.userName = :userName"),
@NamedQuery(name = "getAllAnonymousOauthAccessTokenDetails",query = "select accessToken from OauthAccessToken accessToken where accessToken.isAnonymous= :true"),
@NamedQuery(name = "getOauthAccessTokenDetailsByUserNameAndClientID",query ="select accessToken from OauthAccessToken accessToken where accessToken.userName = :userName and accessToken.clientId = :clientId "),
@NamedQuery(name = "getOauthAccessTokenDetailsByUserNameWithDiffClient",query ="select accessToken from OauthAccessToken accessToken where accessToken.userName = :userName and accessToken.clientId != :clientId ")
})
@Synonym(grant="ALL")
public class OauthAccessToken extends BaseEntity {
/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
String tokenId;
@Lob
Blob token ;
String  authenticationId ;
String  userName ;
String  clientId ;
Boolean isAnonymous;


@Lob
Blob  authentication ;
String	  refreshToken ;
public String getTokenId() {
	return tokenId;
}
public void setTokenId(String tokenId) {
	this.tokenId = tokenId;
}
public Blob getToken() {
	return token;
}
public void setToken(Blob token) {
	this.token = token;
}
public String getAuthenticationId() {
	return authenticationId;
}
public void setAuthenticationId(String authenticationId) {
	this.authenticationId = authenticationId;
}

public Blob getAuthentication() {
	return authentication;
}
public void setAuthentication(Blob authentication) {
	this.authentication = authentication;
}
public String getUserName() {
	return userName;
}
public void setUserName(String userName) {
	this.userName = userName;
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

public Boolean isAnonymous() {
	if(isAnonymous==null)
	{
		return Boolean.FALSE;
	}
	return isAnonymous;
}
public void setAnonymous(Boolean isAnonymous) {
	this.isAnonymous = isAnonymous;
}

}
