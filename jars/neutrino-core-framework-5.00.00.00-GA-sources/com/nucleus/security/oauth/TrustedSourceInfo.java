package com.nucleus.security.oauth;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.provider.ClientDetails;

public class TrustedSourceInfo implements ClientDetails {
  
  private static final long serialVersionUID = 1L;
  private String hashKey;
  private String clientId;
  private String clientSecret;
  private Integer accessTokenValiditySeconds;
  private Integer refreshTokenValiditySeconds;
  private Set<String> authorizedGrantTypes;
  private Set<String> scope;
  
  private Set<String> apiBasedAuthorities;
  
  


private String passPhrase;
  private String mailId;
  private String ecryptedClientSecret;
	private Boolean isInternal;

	private String idpClientId;
	private String idpClientSecret;
	private String redirectUri;
	
  public Boolean getIsInternal() {
		return isInternal;
	}

	public void setIsInternal(Boolean isInternal) {
		this.isInternal = isInternal;
	}

public String getEcryptedClientSecret() {
	return ecryptedClientSecret;
}

public void setEcryptedClientSecret(String ecryptedClientSecret) {
	this.ecryptedClientSecret = ecryptedClientSecret;
}

public String getMailId() {
	return mailId;
}

public void setMailId(String mailId) {
	this.mailId = mailId;
}

public void setScope(Set<String> scope) {
    this.scope = scope;
  }
  
  public String getHashKey() {
    return hashKey;
  }
  
  public void setHashKey(String hashKey) {
    this.hashKey = hashKey;
  }
  
  public void setClientId(String clientId) {
    this.clientId = clientId;
  }
  
  public void setClientSecret(String clientSecret) {
    this.clientSecret = clientSecret;
  }
  
  public Integer getAccessTokenValiditySeconds() {
    return accessTokenValiditySeconds;
  }
  
  public void setAccessTokenValiditySeconds(Integer accessTokenValiditySeconds) {
    this.accessTokenValiditySeconds = accessTokenValiditySeconds;
  }
  
  public Integer getRefreshTokenValiditySeconds() {
    return refreshTokenValiditySeconds;
  }
  
  public void setRefreshTokenValiditySeconds(Integer refreshTokenValiditySeconds) {
    this.refreshTokenValiditySeconds = refreshTokenValiditySeconds;
  }
  
  public Set<String> getAuthorizedGrantTypes() {
    return authorizedGrantTypes;
  }
  
  public void setAuthorizedGrantTypes(Set<String> authorizedGrantTypes) {
    this.authorizedGrantTypes = authorizedGrantTypes;
  }
  
  @Override
  public boolean isAutoApprove(String scope) {
    
    return false;
  }
  
  public boolean isSecretRequired() {
    return this.clientSecret != null;
  }
  
  @Override
  public String getClientId() {
    
    return clientId;
  }
  
  @Override
  public Set<String> getResourceIds() {
    
    return Collections.emptySet();
  }
  
  @Override
  public String getClientSecret() {
    
    return this.clientSecret;
  }
  
  @Override
  public Set<String> getScope() {
    
    return scope;
    
  }
  
  @Override
  public Set<String> getRegisteredRedirectUri() {
    
    return Collections.emptySet();
  }
  
  // May be changed in future for controlling authority at client level
  @Override
  public Collection<GrantedAuthority> getAuthorities() {
    
    return Collections.emptySet();
  }
  
  @Override
  public boolean isScoped() {
    
    return false;
  }
  
  @Override
  public Map<String, Object> getAdditionalInformation() {
    
    return null;
  }
  
  public String getPassPhrase() {
    return passPhrase;
  }
  
  public void setPassPhrase(String passPhrase) {
    this.passPhrase = passPhrase;
  }
  public Set<String> getApiBasedAuthorities() {
	return apiBasedAuthorities;
}

public void setApiBasedAuthorities(Set<String> apiBasedAuthorities) {
	this.apiBasedAuthorities = apiBasedAuthorities;
}

	public String getIdpClientId() {
		return idpClientId;
	}
	
	public void setIdpClientId(String idpClientId) {
		this.idpClientId = idpClientId;
	}
	
	public String getIdpClientSecret() {
		return idpClientSecret;
	}
	
	public void setIdpClientSecret(String idpClientSecret) {
		this.idpClientSecret = idpClientSecret;
	}

	public String getRedirectUri() {
		return redirectUri;
	}

	public void setRedirectUri(String redirectUri) {
		this.redirectUri = redirectUri;
	}
	
}
