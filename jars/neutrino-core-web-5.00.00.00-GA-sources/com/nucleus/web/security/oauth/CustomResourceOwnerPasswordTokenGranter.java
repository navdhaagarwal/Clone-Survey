package com.nucleus.web.security.oauth;

import java.util.LinkedHashMap;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.AccountStatusException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.SpringSecurityMessageSource;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.common.exceptions.InvalidGrantException;
import org.springframework.security.oauth2.provider.ClientDetails;
import org.springframework.security.oauth2.provider.ClientDetailsService;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.OAuth2Request;
import org.springframework.security.oauth2.provider.OAuth2RequestFactory;
import org.springframework.security.oauth2.provider.TokenRequest;
import org.springframework.security.oauth2.provider.token.AbstractTokenGranter;
import org.springframework.security.oauth2.provider.token.AuthorizationServerTokenServices;
import org.springframework.security.web.authentication.session.SessionAuthenticationException;

import com.nucleus.finnone.pro.base.constants.CoreConstant;
import com.nucleus.finnone.pro.general.util.ValidatorUtils;
import com.nucleus.logging.BaseLoggers;
import com.nucleus.reason.BlockReason;
import com.nucleus.reason.ReasonVO;
import com.nucleus.security.oauth.TrustedSourceInfo;
import com.nucleus.security.oauth.dao.CustomOauthTokenStoreDAO;
import com.nucleus.security.oauth.dao.TrustedSourceDao;
import com.nucleus.security.oauth.domainobject.OauthClientDetails;
import com.nucleus.security.oauth.service.TrustedSourceService;
import com.nucleus.user.User;
import com.nucleus.user.UserDeviceMapping;
import com.nucleus.user.UserInfo;
import com.nucleus.user.UserService;
import com.nucleus.web.security.AesUtil;
import com.nucleus.web.security.oauth.util.NeutrinoTokenUtility;

public class CustomResourceOwnerPasswordTokenGranter extends AbstractTokenGranter {
	private Map<String, AuthenticationManager> authenticationManagerMap;
	private static final String GRANT_TYPE_PASSWORD = "password";
	
	private static final String SECURITY_QUESTIONS = "security_questions";
	private static final String FORCE_PASSWORD_RESET_FOR_LOGIN = "force_password_reset_for_login";
	
	
	@Value(value = "#{'${core.web.config.webClientToEncryptpwd}'}")
	private String webClientToEncryptpwd;

	private AuthenticationManager authenticationManager;
	@Inject
	@Named(value = "userService")
	private UserService userService;

	@Inject
	@Named("clientDetails")
	private TrustedSourceService trustedSourceService;
	
	@Inject
	@Named("trustedSourceDao")
	private TrustedSourceDao trustedSourceDao;
	
	@Inject
	@Named("neutrinoTokenUtility")
	private NeutrinoTokenUtility neutrinoTokenUtility;
	
	@Autowired(required=false)
	private CustomOauthTokenStoreDAO tokenStore;

	protected MessageSourceAccessor messages = SpringSecurityMessageSource.getAccessor();
	@Autowired
	OauthConcurrentSessionControlStrategy oauthConcurrentSessionControlStrategy;

	  public CustomResourceOwnerPasswordTokenGranter(AuthenticationManager authenticationManager, AuthorizationServerTokenServices tokenServices, ClientDetailsService clientDetailsService,
				OAuth2RequestFactory requestFactory) {
			this(authenticationManager, tokenServices, clientDetailsService, requestFactory, GRANT_TYPE_PASSWORD);
		}

	  protected CustomResourceOwnerPasswordTokenGranter(AuthenticationManager authenticationManager, AuthorizationServerTokenServices tokenServices, ClientDetailsService clientDetailsService,
				OAuth2RequestFactory requestFactory, String grantType) {
			super(tokenServices, clientDetailsService, requestFactory, grantType);
			this.authenticationManager = authenticationManager;
		}
	  
	
	public Map<String, AuthenticationManager> getAuthenticationManagerMap() {
		return authenticationManagerMap;
	}

	public void setAuthenticationManagerMap(Map<String, AuthenticationManager> authenticationManagerMap) {
		this.authenticationManagerMap = authenticationManagerMap;
	}

	public AuthenticationManager getAuthenticationManager() {
		return authenticationManager;
	}

	public void setAuthenticationManager(AuthenticationManager authenticationManager) {
		this.authenticationManager = authenticationManager;
	}



	@Override
	protected OAuth2Authentication getOAuth2Authentication(ClientDetails client, TokenRequest tokenRequest) {

		Map<String, String> parameters = new LinkedHashMap<>(tokenRequest.getRequestParameters());
			String username = parameters.get("username");
			if(username==null){
				throw new AuthenticationServiceException("username is null");
			}
			username = username.toLowerCase();
			
			String sourceSystem = userService.getUserSourceSystemByUsername(username);
			if (StringUtils.isBlank(sourceSystem)) {
				throw new AuthenticationServiceException("User's source system is not available");
			}
			// if sourceSystem does not belong to 'db or 'ldap'
			if (!"db".equals(sourceSystem) && !"ldap".equals(sourceSystem)) {
				throw new AuthenticationServiceException("User does not belong to ldap or db");
			}

    // Replacing the default authenticationManager previously set with the
    // required authentication manager from the map based on sourceSystem
			setAuthenticationManager(authenticationManagerMap.get(sourceSystem));
			String password = parameters.get(GRANT_TYPE_PASSWORD);
			// Protect from downstream leaks of password
			parameters.remove(GRANT_TYPE_PASSWORD);
			String decryptedPassword = getDecryptedPassword(password, client);
			OauthClientDetails clientDetails = trustedSourceDao.loadUnproxiedClientByClientId(client.getClientId());
			if(ValidatorUtils.hasElements(clientDetails.getTrustedUsers())){
				if(!clientDetails.getIsInternal()){
				
				if(!checkIfUserIsTrusted(clientDetails, username)){
					 BaseLoggers.apiManagementLogger.info( clientDetails.getClientId() +  " used a non trusted user for logging in");
					throw new AuthenticationServiceException("User is not a trusted User for the Client");
					
					}
				}
			}
			Authentication userAuth = new UsernamePasswordAuthenticationToken(username, decryptedPassword);
			((AbstractAuthenticationToken) userAuth).setDetails(parameters);
			try {

				userAuth = authenticationManager.authenticate(userAuth);
			} catch (AccountStatusException ase) {
      // covers expired, locked, disabled cases (mentioned in section 5.2, draft
				// 31)
				BaseLoggers.flowLogger.error("Cannot continue login - Account expired/locked/disabled. Please check. : " + ase.getMessage());

				String accountStatusException = ase.getMessage();
				if(LockedException.class.isAssignableFrom(ase.getClass())) {
					accountStatusException = prepareLockedExceptionMessage(username,ase.getMessage());	
				}
							
				throw new InvalidGrantException(accountStatusException);
			} catch (BadCredentialsException e) {
      // If the username/password are wrong the spec says we should send
				// 400/invalid grant
				BaseLoggers.flowLogger.error("Bad Credentials while trying to login through Oauth : " + e.getMessage());
				userService.incrementFailedLoginCount(userService.getUserIdByUserName(username));
				throw new InvalidGrantException(e.getMessage());
			}
			if (userAuth == null || !userAuth.isAuthenticated()) {
				throw new InvalidGrantException("Could not authenticate user: " + username);
			}

			UserInfo userInfo = (UserInfo) userAuth.getPrincipal();
			/*if (!userInfo.isMobileLoginAllowed()) {
				throw new InvalidGrantException("Mobile login is not allowed");
			}*/
		
		if (!oauthConcurrentSessionControlStrategy.validateOauthReqeust(tokenRequest)) {
      throw new SessionAuthenticationException(messages.getMessage("OauthConcurrentSessionControlStrategy.exceededAllowed",
          new Object[] { Integer.valueOf(oauthConcurrentSessionControlStrategy.getMaximumSessions()) }, "Maximum sessions of {0} for this user exceeded"));
		}

		
//    Added to check IMEI/MEID number validation.
    if(userInfo.isDeviceAuthEnabled()){
			
    	String deviceType = parameters.get("deviceType");
    	String deviceId = parameters.get("deviceId");
    	
    	if(deviceType==null || deviceId==null){
    		throw new InvalidGrantException("Mandatory request param deviceType or deviceId is missing");
	}

    	UserDeviceMapping udm;
    	boolean isDeviceAuthorized =  false;
    	for (int i = 0; i < userInfo.getRegisteredDevices().size(); i++) {
    		udm = userInfo.getRegisteredDevices().get(i);
    		if(udm.getDeviceId().equals(deviceId) && udm.getDeviceType().getCode().equalsIgnoreCase(deviceType)){
    			isDeviceAuthorized = true;
    			break;
    		}
		}
    	
    	if(!isDeviceAuthorized){
    		throw new InvalidGrantException("This device is not Authorized to connect.");
    	}
    }
    OAuth2Request storedOAuth2Request = getRequestFactory().createOAuth2Request(client, tokenRequest);


	 BaseLoggers.apiManagementLogger.info( clientDetails.getClientId() +  " obtained new token for access with password grant type");
		
    return new OAuth2Authentication(storedOAuth2Request, userAuth);
  }

	private String prepareLockedExceptionMessage(String username, String message) {
		BlockReason blockReason = userService.getUserBlockReasonByUsername(username);
		
		if (blockReason != null) {
			message = CoreConstant.LOCKED_REASON_DESCRIPTION + blockReason.getDescription();
		}

		return message;
	}

	private String getDecryptedPassword(String password, ClientDetails client) {
		String decryptedPassword;
		if ("Y".equalsIgnoreCase(webClientToEncryptpwd)) {
			TrustedSourceInfo trustedSourceVO = (TrustedSourceInfo) trustedSourceService
					.loadClientByClientId(client.getClientId());
			String decryptedPassPhrase = AesUtil.Decrypt(trustedSourceVO.getPassPhrase(),
					OauthClientDetails.SHARED_OAUTH_ENCYPTION_PASS_PHRASE);
			decryptedPassword = AesUtil.Decrypt(password, decryptedPassPhrase);
		} else {
			decryptedPassword = password;
		}
		return decryptedPassword;
	}
  
	private boolean checkIfUserIsTrusted(OauthClientDetails clientDetails, String username){
		boolean userIsTrustedUser = false;
		if(clientDetails.getTrustedUsers() != null || clientDetails.getTrustedUsers().isEmpty()){
			for (User user : clientDetails.getTrustedUsers()){
					if(username.equals(user.getUsername())){
						userIsTrustedUser = true;
						break;
					}
			}
	}
	else{
		
		userIsTrustedUser = true;

	}

		return userIsTrustedUser;
	}
 
	/* Creates access token and adds additional data in additionInformationMap
	 * (non-Javadoc)
	 * @see org.springframework.security.oauth2.provider.token.AbstractTokenGranter#getAccessToken(org.springframework.security.oauth2.provider.ClientDetails, org.springframework.security.oauth2.provider.TokenRequest)
	 */
	@Override
	protected OAuth2AccessToken getAccessToken(ClientDetails client, TokenRequest tokenRequest) {
		OAuth2AccessToken token = super.getAccessToken(client, tokenRequest);
		
		token = neutrinoTokenUtility.setAdditionalInfoInToken(tokenRequest, token);
		
		if(null!=tokenStore) {
		tokenStore.storeAccessToken(token, getOAuth2Authentication(client, tokenRequest));
		}
		return token;

	}
}
