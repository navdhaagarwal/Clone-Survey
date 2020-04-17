package com.nucleus.web.security.oauth;

import java.util.HashSet;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.ClientDetails;
import org.springframework.security.oauth2.provider.ClientDetailsService;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.OAuth2Request;
import org.springframework.security.oauth2.provider.OAuth2RequestFactory;
import org.springframework.security.oauth2.provider.TokenRequest;
import org.springframework.security.oauth2.provider.token.AbstractTokenGranter;
import org.springframework.security.oauth2.provider.token.AuthorizationServerTokenServices;

import com.nucleus.config.persisted.service.ConfigurationService;
import com.nucleus.core.mutitenancy.service.MultiTenantService;
import com.nucleus.logging.BaseLoggers;
import com.nucleus.security.oauth.TrustedSourceInfo;
import com.nucleus.security.oauth.service.TrustedSourceService;
import com.nucleus.user.SpringSecurityAuthorityAdapter;

public class CustomAnonymousTokenGranter extends AbstractTokenGranter{

	@Inject
	@Named("clientDetails")
	private TrustedSourceService trustedSourceService;
    
	@Inject
	@Named("multiTenantService")
	private MultiTenantService multiTenantService;
    
    

    
    @Inject
    @Named("configurationService")
    private ConfigurationService configurationService;
    
    
	private AuthenticationManager authenticationManager;



	private static final String GRANT_TYPE = "anonymous";


	public CustomAnonymousTokenGranter(AuthenticationManager authenticationManager,
			AuthorizationServerTokenServices tokenServices, ClientDetailsService clientDetailsService,
			OAuth2RequestFactory requestFactory) {
		this(authenticationManager, tokenServices, clientDetailsService, requestFactory, GRANT_TYPE);
	}

	protected CustomAnonymousTokenGranter(AuthenticationManager authenticationManager,
			AuthorizationServerTokenServices tokenServices, ClientDetailsService clientDetailsService,
			OAuth2RequestFactory requestFactory, String grantType) {
		super(tokenServices, clientDetailsService, requestFactory, grantType);
		this.authenticationManager = authenticationManager;
	}
	@Override
	public OAuth2AccessToken grant(String grantType, TokenRequest tokenRequest) {
		return super.grant(grantType, tokenRequest);
		
		
		
		
	}

	
	@Override
	protected OAuth2Authentication getOAuth2Authentication(ClientDetails client, TokenRequest tokenRequest) {

		
		 BaseLoggers.apiManagementLogger.info(client.getClientId() + " has obtained token for anonymous grant type");
		 
		 
			OAuth2Request storedOAuth2Request = getRequestFactory().createOAuth2Request(client, tokenRequest);
			Authentication auth = createAuth(client);
			return new OAuth2Authentication(storedOAuth2Request, auth);	
	}

	
	public AuthenticationManager getAuthenticationManager() {
		return authenticationManager;
	}

	public void setAuthenticationManager(AuthenticationManager authenticationManager) {
		this.authenticationManager = authenticationManager;
	}

	

	private Authentication createAuth(ClientDetails client){
		Set<GrantedAuthority> authorities = new HashSet<GrantedAuthority>();
		for (String authorityCode : ((TrustedSourceInfo)client).getApiBasedAuthorities()){	
            authorities.add(new SpringSecurityAuthorityAdapter(authorityCode));
            
		}
		if(authorities.isEmpty()){
			authorities.add(new SpringSecurityAuthorityAdapter("DUMMY_AUTH"));
			
		}
		return new AnonymousAuthenticationToken(client.getClientId(),client,  authorities);
		
	}

}


