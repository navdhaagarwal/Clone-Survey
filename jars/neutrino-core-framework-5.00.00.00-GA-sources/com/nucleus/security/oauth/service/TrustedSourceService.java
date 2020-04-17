package com.nucleus.security.oauth.service;

import java.util.HashSet;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.security.oauth2.provider.ClientDetails;
import org.springframework.security.oauth2.provider.ClientDetailsService;
import org.springframework.security.oauth2.provider.ClientRegistrationException;
import org.springframework.transaction.annotation.Transactional;

import com.nucleus.security.oauth.TrustedSourceInfo;
import com.nucleus.security.oauth.apim.APIDetails;
import com.nucleus.security.oauth.dao.TrustedSourceDao;
import com.nucleus.security.oauth.domainobject.OauthClientDetails;

@Named("clientDetails")
public class TrustedSourceService implements ClientDetailsService {
  
  @Inject
  @Named("trustedSourceDao")
  private TrustedSourceDao customTrustedSourceDao;
  
  @Transactional
  public ClientDetails loadClientByClientId(String clientId) throws ClientRegistrationException {
    
    TrustedSourceInfo trustedSourceInfo = new TrustedSourceInfo();
    
    OauthClientDetails trustedSource = customTrustedSourceDao.loadClientByClientId(clientId);
    if (trustedSource != null) {
      trustedSourceInfo.setClientId(trustedSource.getClientId());
      trustedSourceInfo.setAuthorizedGrantTypes(trustedSource.getAuthorizedGrantTypes());
      trustedSourceInfo.setAccessTokenValiditySeconds(trustedSource.getAccessTokenValiditySeconds());
      trustedSourceInfo.setClientSecret(trustedSource.getClientSecret());
      trustedSourceInfo.setRefreshTokenValiditySeconds(trustedSource.getRefreshTokenValiditySeconds());
      trustedSourceInfo.setHashKey(trustedSource.getHashKey());
      trustedSourceInfo.setScope(trustedSource.getScopeList());
      trustedSourceInfo.setPassPhrase(trustedSource.getPassPhrase());
      trustedSourceInfo.setMailId(trustedSource.getMailId());
      trustedSourceInfo.setEcryptedClientSecret(trustedSource.getEncryptedSecret());
      trustedSourceInfo.setApiBasedAuthorities(getAllAuthorities(trustedSource));
      trustedSourceInfo.setIsInternal(trustedSource.getIsInternal());
      trustedSourceInfo.setIdpClientId(trustedSource.getIdpClientId());
      trustedSourceInfo.setIdpClientSecret(trustedSource.getIdpClientSecret());
      trustedSourceInfo.setRedirectUri(trustedSource.getRedirectUri());
    }
    return trustedSourceInfo;
  
 }
  
  
  private Set<String> getAllAuthorities(OauthClientDetails otrustedSource){
	  Set<String> authorities = new HashSet<String>();
	  for(APIDetails api : otrustedSource.getMappedAPIs()){
		  authorities.add(api.getAuthority().getAuthCode());
		  
	  }	  
	  return authorities;
  }
}
