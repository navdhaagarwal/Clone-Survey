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

@NamedQueries({

		@NamedQuery(name = "getOauthRefreshTokenDetailsByTokenID", query = "select refreshToken from OauthRefreshToken refreshToken where refreshToken.refreshTokenId = :refreshTokenId"),

})

@Table(indexes = { @Index(name = "OAUTH_REFRESH_TOKEN_INDX_1", columnList = "refreshTokenId") })

@Synonym(grant = "ALL")
public class OauthRefreshToken extends BaseEntity {

	/**
		 * 
		 */
	private static final long serialVersionUID = 1L;
	private String refreshTokenId;
	@Lob
	private Blob refreshToken;
	@Lob
	private Blob authentication;

	public String getRefreshTokenId() {
		return refreshTokenId;
	}

	public void setRefreshTokenId(String refreshTokenId) {
		this.refreshTokenId = refreshTokenId;
	}

	public Blob getRefreshToken() {
		return refreshToken;
	}

	public void setRefreshToken(Blob refreshToken) {
		this.refreshToken = refreshToken;
	}

	public Blob getAuthentication() {
		return authentication;
	}

	public void setAuthentication(Blob authentication) {
		this.authentication = authentication;
	}
}
