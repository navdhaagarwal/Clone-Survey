package com.nucleus.web.security.federated;

import java.util.ArrayList;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

public class FederatedLoginAuthenticationToken extends AbstractAuthenticationToken {
	
	private static final long serialVersionUID = 12343289L;
	
	private Object principal;
	private String authorizationCode;
	private String idpAccessToken;
	private String idpRefreshToken;

	public FederatedLoginAuthenticationToken(Object principal, String authorizationCode, String idpAccessToken, String idpRefreshToken) {
		super(new ArrayList<GrantedAuthority>());
		this.principal = principal;
		this.authorizationCode = authorizationCode;
		this.idpAccessToken = idpAccessToken;
		this.idpRefreshToken = idpRefreshToken;
		setAuthenticated(true);
	}

	@Override
	public Object getCredentials() {
		return this.authorizationCode;
	}

	@Override
	public Object getPrincipal() {
		return this.principal;
	}
	
	
	@Override
    public boolean equals(Object obj) {
        if (!super.equals(obj)) {
            return false;
        }

        if (obj instanceof FederatedLoginAuthenticationToken) {
        	FederatedLoginAuthenticationToken test = (FederatedLoginAuthenticationToken) obj;

            if(this.getCredentials() != test.getCredentials()) {
                return false;
            }

            return true;
        }

        return false;
    }

	public String getIdpAccessToken() {
		return idpAccessToken;
	}

	public void setIdpAccessToken(String idpAccessToken) {
		this.idpAccessToken = idpAccessToken;
	}

	public String getIdpRefreshToken() {
		return idpRefreshToken;
	}

	public void setIdpRefreshToken(String idpRefreshToken) {
		this.idpRefreshToken = idpRefreshToken;
	}
	
}
