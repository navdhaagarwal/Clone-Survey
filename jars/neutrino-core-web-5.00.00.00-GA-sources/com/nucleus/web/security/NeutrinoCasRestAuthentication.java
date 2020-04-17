package com.nucleus.web.security;

import static com.nucleus.logging.BaseLoggers.exceptionLogger;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.nucleus.entity.ApprovalStatus;
import com.nucleus.user.*;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.jasig.cas.authentication.principal.DefaultPrincipalFactory;
import org.jasig.cas.authentication.principal.SimplePrincipal;
import org.jasig.cas.client.session.HashMapBackedSessionMappingStorage;
import org.jasig.cas.client.session.SingleSignOutHttpSessionListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.CredentialsExpiredException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.web.authentication.session.SessionAuthenticationException;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.google.gson.reflect.TypeToken;
import com.nucleus.authenticationToken.AuthenticationTokenService;
import com.nucleus.authority.Authority;
import com.nucleus.businessmapping.service.UserManagementServiceCore;
import com.nucleus.config.persisted.service.ConfigurationService;
import com.nucleus.core.exceptions.SystemException;
import com.nucleus.core.initialization.ProductInformationLoader;
import com.nucleus.core.json.util.GsonUtil;
import com.nucleus.dao.query.NamedQueryExecutor;
import com.nucleus.finnone.pro.base.utility.CoreUtility;
import com.nucleus.finnone.pro.cache.common.NeutrinoCachePopulator;
import com.nucleus.finnone.pro.cache.common.NeutrinoCachePopulator.Action;
import com.nucleus.finnone.pro.general.util.ValidatorUtils;
import com.nucleus.license.utils.LicenseSetupUtil;
import com.nucleus.logging.BaseLoggers;
import com.nucleus.persistence.EntityDao;
import com.nucleus.reason.BlockReason;
import com.nucleus.rules.model.SourceProduct;
import com.nucleus.security.core.session.AESEncryptionWithStaticKey;
import com.nucleus.security.core.session.NeutrinoSessionRegistryImpl;
import com.nucleus.security.core.session.SessionModuleService;
import com.nucleus.security.oauth.dao.CustomOauthTokenStoreDAO;
import com.nucleus.sso.BannerStorageVO;
import com.nucleus.sso.SsoUserInfo;
import com.nucleus.sso.password.SsoResetPasswordService;
import com.nucleus.sso.service.NeutrinoCasRestAuthenticationService;
import com.nucleus.sso.utils.SsoConfigUtility;
import com.nucleus.systemSetup.service.SystemSetupService;
import com.nucleus.user.questions.SecurityQuestionService;
import com.nucleus.web.common.CommonConfigUtility;
import com.nucleus.web.common.RenderImageUtility;
import com.nucleus.web.common.controller.NonTransactionalBaseController;
import com.nucleus.web.login.LoginConstants;

/**
 * 
 * Rest Services for integration with Central Authentication Service (The other
 * CAS)
 * 
 * 
 * Here, logic for simple authentication, unblocking and resetting the password
 * is written, which is remotely done through rest calls from the SSO server.
 * 
 * @author prateek.chachra
 *
 */
@RestController
@RequestMapping(value = { "/sso", "/restservice/sso" })
public class NeutrinoCasRestAuthentication extends NonTransactionalBaseController{

	private static final String PASS_PHRASE_CONSTANT = "PASS_PHRASE";
	private static final String EXCEPTION_CONSTANT = "exception";
	private static final String USERNAME_CONSTANT = "username";
	private static final String MODULE_CONSTANT = "moduleName";
	private static final String SESSION_ID_CONSTANT = "sessionId";
	private static final String IS_MFA_ENABLED = "isMFAEnabled";
	private static final String SUCCESS_MESSAGE = "success";
	private static final String FAILURE_MESSAGE = "failure";
	private static final String RESULT = "result";
	private static final String SESSIONID_ERROR_MESSAGE = "Some error occured";
	private static final String SESSIONID_LOGGER_MESSAGE = "Session was not found in cache SSO_PHRASE_MAP";
	private static final String OLD_PSWD = "oldPassword";
	private static final String STATUS = "status";

	private static final String CONCURRENCY_ERROR = "CONCURRENCY_ERROR";
	private static final String MESSAGE_CONSTANT="message";

	private Method sessionMappingStorageMethod = null;

	private Type typeForGsonConversion = null;

	@Inject
	@Named("sessionRegistry")
	private SessionRegistry sessionRegistry;

	@Inject
	@Named("neutrinoSecurityUtility")
	private NeutrinoSecurityUtility neutrinoSecurityUtility;
	@Inject
	@Named("commonConfigUtility")
	private CommonConfigUtility commonConfigUtility;
	@Inject
	@Named("customUsernamePasswordAuthenticationFilter")
	private CustomUsernamePasswordAuthenticationFilter customUsernamePasswordAuthenticationFilter;

	@Inject
	@Named("authenticationTokenService")
	private AuthenticationTokenService authenticationTokenService;

	@Inject
	@Named("configurationService")
	private ConfigurationService configurationService;

	@Inject
	@Named("userManagementServiceCore")
	private UserManagementServiceCore userManagementServiceCore;

	@Inject
	@Named("userService")
	private UserService userService;

	@Inject
	@Named("systemSetupUtil")
	private SystemSetupUtil systemSetupUtil;

	@Inject
	@Named("entityDao")
	private EntityDao entityDao;

	@Inject
	@Named("renderImageUtility")
	private RenderImageUtility renderImageUtility;

	@Autowired
	private LicenseSetupUtil licenseSetupUtil;

	@Value("${core.web.config.token.validity.time.millis}")
	private String tokenValidityTimeInMillis;

	@Inject
	@Named("systemSetupService")
	private SystemSetupService systemSetupService;

	@Value("${core.web.config.SSO.max.allowed.banner:10}")
	private String maxAllowedBanners;

	@Inject
	@Named("ssoConfigUtility")
	private SsoConfigUtility ssoConfigUtility;

	@Inject
	@Named("neutrinoCasRestAuthenticationServiceImpl")
	private NeutrinoCasRestAuthenticationService neutrinoCasRestAuthenticationServiceImpl;

	@Inject
	@Named("ssoResetPasswordService")
	private SsoResetPasswordService ssoResetPasswordService;

	@Value(value = "#{'${core.web.config.SSO.request.encryption.key}'}")
	private String ssoEncryptionKey;

	@Inject
	@Named("ssoPassPhraseCachePopulator")
	private NeutrinoCachePopulator ssoPassPhraseCachePopulator;
	
    @Inject
    @Named("sessionRegistry")
    private NeutrinoSessionRegistryImpl neutrinoSessionRegistry;

	@Inject
	@Named(value = "sessionModuleService")
	private SessionModuleService sessionModuleService;

	@Inject
	@Named(value = "userSecurityService")
	protected UserSecurityService securityService;
	
	//Conditional bean that might be null if API portal is enabled.
	@Autowired(required = false)
	private TokenStore tokenStore;
	
	@Inject
	@Named("securityQuestionService")
	private SecurityQuestionService securityQuestionService;

	@Inject
	@Named("messageSource")
	private MessageSource messageSource;

	private static final String UTF_8 = "UTF-8";

	/**
	 * This is the main method for authentication. Here, the Details of the
	 * allowed modules are forwarded through the reset service in case of
	 * selected authority. The exceptions are handled in accordance to the
	 * already existing code.
	 * 
	 * @see CustomUsernamePasswordAuthenticationFilter
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping(value = "/ssoauthenticate", method = RequestMethod.POST)
	@ResponseBody
	@Transactional(propagation = Propagation.REQUIRED)
	public ResponseEntity<SimplePrincipal> attemptAuthentication(HttpServletRequest request,
			HttpServletResponse response) {
		/*
		 * Checking for the license
		 * 
		 * 
		 */
		BaseLoggers.flowLogger
				.info("Logging into SSO : Currently connected with : " + ProductInformationLoader.getProductName());

		HttpHeaders headers = new HttpHeaders();

		if ((this.systemSetupUtil.isSystemSetup()) && (!(this.licenseSetupUtil.isSystemSetup()))) {
			headers.add(EXCEPTION_CONSTANT, "licenseNotEnabled");
			BaseLoggers.exceptionLogger.error(
					"Cannot login into SSO : License not enabled in " + ProductInformationLoader.getProductName());

			return new ResponseEntity<>(null, headers, HttpStatus.OK);

		}

		String sessionId = request.getHeader(SESSION_ID_CONSTANT);

		/*
		 * This is to ensure no captcha check takes place on the client side if
		 * SSO is on.
		 * 
		 * 
		 */
		customUsernamePasswordAuthenticationFilter.setCaptchaEnabled(false);

		Map<String, Object> principalAttributes = new HashMap<>();
		String credentials = request.getHeader("NeoAuthentication");
		String credentialsDecoded = new String(Base64.decodeBase64(credentials.substring(9)));
		String[] userPass = credentialsDecoded.split(":");
		CustomRestHttpServletRequest req = new CustomRestHttpServletRequest(request);
		
		String passPhraseFomCache = getPassPhraseFromCache(sessionId);
		if (SESSIONID_ERROR_MESSAGE.equals(passPhraseFomCache)) {
			BaseLoggers.exceptionLogger.error(SESSIONID_LOGGER_MESSAGE);
			headers.add(EXCEPTION_CONSTANT, "SessionId Not Found");
			
			//invalidate the session 
			//as cookie is disabled in this call and parameters are being passed as header
			if(request != null && request.getSession() != null) {
				request.getSession().invalidate();
			}
			
			return new ResponseEntity<>(null, headers, HttpStatus.OK);
		}

		req.getSession(false).setAttribute(PASS_PHRASE_CONSTANT, passPhraseFomCache);
		DefaultPrincipalFactory factory = new DefaultPrincipalFactory();
		Authentication authentication = null;
		try {
			
			if(userPass.length!=2){
				//This is a logger in order to know the credential sent while login. 
				//This will only print the value only if the credentials are invalid
				BaseLoggers.exceptionLogger.error("Invalid Credentials : {}" , credentialsDecoded );
				throw new BadCredentialsException("Invalid user credentials.");
			}
			req.getCustomParameterMap().put(USERNAME_CONSTANT, userPass[0]);
			req.getCustomParameterMap().put("password", userPass[1]);
			
			authentication = customUsernamePasswordAuthenticationFilter.attemptAuthentication(req, response);
			if (authentication == null) {
				headers.add(EXCEPTION_CONSTANT, "Unknown");
				BaseLoggers.exceptionLogger
						.error("Cannot login into SSO : Unknown error in %s for user: %s" , ProductInformationLoader.getProductName() , userPass[0]);
				return new ResponseEntity<>(null, headers, HttpStatus.OK);
			} else if (UserInfo.class.isAssignableFrom(authentication.getPrincipal().getClass())) {

				UserInfo userInfo = (UserInfo) authentication.getPrincipal();
				User userObj = userService.findUserByUsername(userInfo.getUsername());
				OutOfOfficeDetails outOfOffice = userObj.getOutOfOfficeDetails();
				if (outOfOffice != null && outOfOffice.isOutOfOffice()) {
					BaseLoggers.exceptionLogger.error("User out of office : logging user out");
					SecurityContextHolder.getContext().setAuthentication(null);
					headers.add(EXCEPTION_CONSTANT, "outOfOfficeException");
					return new ResponseEntity<>(null, headers, HttpStatus.OK);
				}

				if (userService.getForceResetPassForUserId(userInfo.getId())) {
					headers.add(EXCEPTION_CONSTANT, "passwordMustChange");

					BaseLoggers.exceptionLogger
							.error("Cannot login into SSO : Please change password for user " + userInfo.getUsername());
					return new ResponseEntity<>(null, headers, HttpStatus.OK);

				} else {
					String mfaEnabled=request.getHeader(IS_MFA_ENABLED);
					
                    if(!("true".equals(mfaEnabled)) ||!userObj.isMfaEnabled())
                    {
					handleConcurrentUserSessions(request, authentication,userPass[0]);
                    }

					headers.add(EXCEPTION_CONSTANT, "none");

					getAllAllowedModulesForUser(userInfo.getUsername(), principalAttributes);
					SsoUserInfo ssoUserInfo = new SsoUserInfo();
					prepareSsoUserObject(userInfo, ssoUserInfo);
					principalAttributes.put("userInfo", ssoUserInfo);
					principalAttributes.put("userMFAEnabled", userObj.isMfaEnabled());
					if (userInfo.hasAuthority("SSO_BANNER_UPLOAD")) {
						principalAttributes.put("hasUploadAuthority", true);
					} else {
						principalAttributes.put("hasUploadAuthority", false);
					}

					Map<String, String> ssoSessionMap = new HashMap<>();
					ssoSessionMap.put(SESSION_ID_CONSTANT, request.getSession().getId());
					ssoSessionMap.put(USERNAME_CONSTANT, userInfo.getUsername());
					ssoSessionMap.put("associatedUserUri", userInfo.getUserEntityId().getUri());
					ssoSessionMap.put("remoteIpAddress", request.getRemoteAddr());
					String ssoSessionMapJson = GsonUtil.convertToJson(ssoSessionMap, getTypeForGsonConversion());

					principalAttributes.put("ssoSessionMap", ssoSessionMapJson);
					
					setUserAuthoritiesInPrincipalAttribute(principalAttributes, userInfo);
					
					BaseLoggers.flowLogger.info("Login successful for " + userInfo.getUsername()
							+ "\nModule currently connected : " + ProductInformationLoader.getProductName());

					return new ResponseEntity<>(
							(SimplePrincipal) factory.createPrincipal(userInfo.getUsername(), principalAttributes),
							headers, HttpStatus.OK);

				}
			} else {
				headers.add(EXCEPTION_CONSTANT, "forbidden");
				BaseLoggers.exceptionLogger.error("Unknown error occured : Login forbidden for the user : %s", userPass[0]);
				return new ResponseEntity<>(null, headers, HttpStatus.OK);
			}
		} catch (Exception e) {

			handleExceptions(headers, e, userPass[0]);
			BaseLoggers.exceptionLogger.error("Error logging into SSO : Please Check.", e);
			return new ResponseEntity<>(null, headers, HttpStatus.OK);
		}finally {
			//invalidate the session 
			//as cookie is disabled in this call and parameters are being passed as header
			if(request != null && request.getSession() != null) {
				request.getSession().invalidate();
			}
		}
	}

	private void handleExceptions(HttpHeaders headers, Exception e, String userName) {
		
		/*
		 * Exceptions are being handled in accordance to the code itself
		 * 
		 */
		if (BadCredentialsException.class.isAssignableFrom(e.getClass())) {
			headers.add(EXCEPTION_CONSTANT, "invalidUserPass");
		} else if (DisabledException.class.isAssignableFrom(e.getClass())) {
			BaseLoggers.exceptionLogger.error("User has been disabled");
			headers.add(EXCEPTION_CONSTANT, "userDisabled");

		} else if (InvalidLoginTokenException.class.isAssignableFrom(e.getClass())) {
			headers.add(EXCEPTION_CONSTANT, "invalidLoginTokenException");
		} else if (AuthenticationServiceException.class.isAssignableFrom(e.getClass())) {
			headers.add(EXCEPTION_CONSTANT, e.getMessage());
		} else if (LockedException.class.isAssignableFrom(e.getClass())) {
			headers.add(EXCEPTION_CONSTANT, "userLocked");
			String unblock = userService.showUnblockLink(userName);
			populateBlockReason(userName,headers);
			headers.add(MESSAGE_CONSTANT , unblock);
		} else if (CredentialsExpiredException.class.isAssignableFrom(e.getClass())) {
			headers.add(EXCEPTION_CONSTANT, "resetPass");
		} else if (SessionAuthenticationException.class.isAssignableFrom(e.getClass())) {
			if (e.getMessage().contains("ConcurrentSessionControlStrategy.exceededAllowed")) {
				headers.add(EXCEPTION_CONSTANT, CONCURRENCY_ERROR);
			}
		}else {
			headers.add(EXCEPTION_CONSTANT, "unknownError");
		}
	}

	private void populateBlockReason(String userName, HttpHeaders headers) {
		BlockReason blockReason = userService. getUserBlockReasonByUsername(userName);
		UserInfo userInfo = userService. getUserFromUsername(userName);
		if(blockReason!= null) {
			headers.add("blockReason", blockReason.getDescription());
			if(userInfo.getApprovalStatus() != ApprovalStatus.APPROVED)
			{
				headers.add("disableUnblock", Boolean.TRUE.toString());
				headers.add("unblockTooltip", messageSource.getMessage(UserConstants.PFA_USER_BEING_UNBLOCKED,null,getUserLocale()));
			}else{
				headers.add("disableUnblock", StringUtils.EMPTY);
				headers.add("unblockTooltip", StringUtils.EMPTY);
			}


		}
	}

	private void handleConcurrentUserSessions(HttpServletRequest request, Authentication authentication, String userName) {
		int oauthUsers = 0;
		if (tokenStore != null) {
			oauthUsers = ((CustomOauthTokenStoreDAO) tokenStore).findActiveTokensCountByUserName(userName);
		}
		if ((LoginConstants.TGT.equals(request.getHeader(LoginConstants.TGT))
				&& !LoginConstants.TRUE.equals(request.getHeader(LoginConstants.IS_FORCED_LOGGED_IN_PARAMETER))) || oauthUsers >= 1) {
			// If request has header with name and value 'TGT', it means a TGT
			// already exists for this username. In such case, we
			// throw below exception
			throw new SessionAuthenticationException("ConcurrentSessionControlStrategy.exceededAllowed");
		}
		
		SSOAuthenticationSuccessEvent ssoAuthenticationSuccessEvent = new SSOAuthenticationSuccessEvent(
				authentication);
		neutrinoSecurityUtility.createAuthenticationSuccessEventEntry(ssoAuthenticationSuccessEvent, null);
	}
	
	
	/**
	 * Resetting the PASS_PHRASE parameter used for private key encryption
	 * 
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/resetPhrase", method = RequestMethod.POST)
	@ResponseBody
	@Transactional(propagation = Propagation.REQUIRED)
	public String resetPhrase(HttpServletRequest request) {
		String sessionId = request.getHeader(SESSION_ID_CONSTANT);
		ssoPassPhraseCachePopulator.update(Action.DELETE,sessionId);
		
		//invalidate the session
		if(request.getSession() != null) {
			request.getSession().invalidate();
		}
		return SUCCESS_MESSAGE;
	}

	/**
	 * This function is used for testing the connection of the server.
	 * 
	 * <p>
	 * If the connection is working, the server will send a 'passed' value.
	 * </p>
	 * 
	 * 
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/testing")
	@ResponseBody
	public String testConnection(HttpServletRequest request) {
		if(neutrinoSessionRegistry.isSsoActive()){
			return "passed";
		}
		return "failure";
	}

	/**
	 * Sending the PASS_PHRASE parameter to the SSO server for private key
	 * encryption
	 * 
	 * @param request
	 * @return
	 */

	@RequestMapping(value = "/getPhrase", method = RequestMethod.POST)
	@ResponseBody
	@Transactional(propagation = Propagation.REQUIRED)
	public Map<String, String> getPhrase(HttpServletRequest request) {
		String sessionId = request.getHeader(SESSION_ID_CONSTANT);
		ssoPassPhraseCachePopulator.update(Action.INSERT,sessionId);
		
		Map<String, String> map = new HashMap<>();
		String passPhraseFomCache = (String) ssoPassPhraseCachePopulator.get(sessionId);
		map.put(PASS_PHRASE_CONSTANT, passPhraseFomCache);
		
		//invalidate the session
		if(request.getSession() != null) {
			request.getSession().invalidate();
		}
		return map;
	}

	/**
	 * As the name of the function suggests, this function resets the password
	 * when the redirect for 'force reset' happens.
	 * 
	 * @param request
	 * @return
	 * @throws JSONException
	 */
	@RequestMapping(value = "/resetPasswordOnLogin", method = RequestMethod.POST)
	@ResponseBody
	@Transactional(propagation = Propagation.REQUIRED)
	public ResponseEntity<String> resetPasswordOnLogin(@RequestBody String userInfoStr, HttpServletRequest request,
			HttpServletResponse response) throws JSONException {

		customUsernamePasswordAuthenticationFilter.setCaptchaEnabled(false);
		CustomRestHttpServletRequest req = new CustomRestHttpServletRequest(request);
		String sessionId = request.getHeader(SESSION_ID_CONSTANT);
		String result = FAILURE_MESSAGE;
		try {
			String username = ssoConfigUtility.decryptuserName(request.getHeader(USERNAME_CONSTANT));
			req.getCustomParameterMap().put(USERNAME_CONSTANT, username);
			req.getCustomParameterMap().put("password", request.getHeader(OLD_PSWD));
			String passPhraseFomCache = getPassPhraseFromCache(sessionId);
	
			if (SESSIONID_ERROR_MESSAGE.equals(passPhraseFomCache)) {
				result = SESSIONID_ERROR_MESSAGE;
			}else{
	
			req.getSession(false).setAttribute(PASS_PHRASE_CONSTANT, passPhraseFomCache);
	
			Authentication authentication = null;
		
				authentication = customUsernamePasswordAuthenticationFilter.attemptAuthentication(req, response);
				if (authentication.isAuthenticated()) {
					JSONObject jsonObject = new JSONObject(userInfoStr);
					result = ssoResetPasswordService.resetPasswordOnLogin(jsonObject, username, request, passPhraseFomCache);
				}
			}
		} catch (Exception e) {
			
			if (BadCredentialsException.class.isAssignableFrom(e.getClass())) {
				result = "badCredentials";
			}else if (InvalidLoginTokenException.class.isAssignableFrom(e.getClass())) {
				result = "invalidLoginToken";
			}else if (LockedException.class.isAssignableFrom(e.getClass())) {
				result = "accountLocked";
			}else {
				result = "UnknownException";
			}
			
			exceptionLogger.error("Error while resetting password on first time login", e);
		}

		return new ResponseEntity<>(result, HttpStatus.OK);
	}

	/**
	 * 
	 * Reset password from SSO
	 * 
	 * 
	 * @param request
	 * @return
	 */

	@RequestMapping(value = "/resetPasswordFromSSO", method = RequestMethod.POST)
	@ResponseBody
	@Transactional(propagation = Propagation.REQUIRED)
	public ResponseEntity<String> resetPasswordFromSSO(HttpServletRequest request) {

		String result = FAILURE_MESSAGE;
		String sessionId = request.getHeader(SESSION_ID_CONSTANT);
		String oldPassword = request.getHeader(OLD_PSWD);
		String newPassword = request.getHeader("newPassword");
		try{
			String passPhraseFomCache = getPassPhraseFromCache(sessionId);
	
			if (SESSIONID_ERROR_MESSAGE.equals(passPhraseFomCache)) {
				result = SESSIONID_ERROR_MESSAGE;
			}else{
	
				String username = ssoConfigUtility.decryptuserName(request.getHeader(USERNAME_CONSTANT));
		
				UserInfo userInfo = userService.getUserFromUsername(username);
				oldPassword = customUsernamePasswordAuthenticationFilter.decryptPass(oldPassword, passPhraseFomCache);
				newPassword = customUsernamePasswordAuthenticationFilter.decryptPass(newPassword, passPhraseFomCache);
				result = userManagementServiceCore.updateUserPassword(userInfo, oldPassword, newPassword, null, false,
						userInfo.getUserReference().isLicenseAccepted());
			}
		}catch(Exception e){
			exceptionLogger.error(EXCEPTION_CONSTANT, e);
			return new ResponseEntity<>(result, HttpStatus.INTERNAL_SERVER_ERROR);
		}
		return new ResponseEntity<>(result, HttpStatus.OK);
	}

	@RequestMapping(value = "/sendMailToResetPassword", method = RequestMethod.POST)
	@Transactional(propagation = Propagation.REQUIRED)
	public ResponseEntity<Map<String, String>> sendForgotPasswordMail(HttpServletRequest request) {
		try{
			String username = ssoConfigUtility.decryptuserName(request.getHeader(USERNAME_CONSTANT));
			return new ResponseEntity<>(ssoResetPasswordService.forgotSecurityQuestion(username, request), HttpStatus.OK);
		}catch(Exception e){
			exceptionLogger.error(EXCEPTION_CONSTANT, e);
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	/**
	 * 
	 * Get all the security questions to display on reset password page on first
	 * time login
	 * 
	 * @param request
	 * @return
	 */

	@RequestMapping(value = "/getSecurityQuestionsList", method = RequestMethod.POST)
	@Transactional(propagation = Propagation.REQUIRED)
	public ResponseEntity<String> getSecurityQuestionsList(HttpServletRequest request) {
		try{
			return new ResponseEntity<>(ssoResetPasswordService.getSecurityQuestionList(), HttpStatus.OK);
		}catch(Exception e){
			exceptionLogger.error(EXCEPTION_CONSTANT, e);
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@RequestMapping(value = "/validatePassword", method = RequestMethod.POST)
	@Transactional(propagation = Propagation.REQUIRED)
	public ResponseEntity<String> validatePassword(HttpServletRequest request) throws IOException {
		
		String passwordEntered = request.getHeader("passwordEntered");
		String sessionId = request.getHeader(SESSION_ID_CONSTANT);
		String result = FAILURE_MESSAGE;
		try{
			String passPhraseFomCache = getPassPhraseFromCache(sessionId);
			if (SESSIONID_ERROR_MESSAGE.equals(passPhraseFomCache)) {
				result = SESSIONID_ERROR_MESSAGE;
			}else{
				String username = ssoConfigUtility.decryptuserName(request.getHeader(USERNAME_CONSTANT));
				result = ssoResetPasswordService.validatePassword(username, passwordEntered, passPhraseFomCache, request);
			}
		}catch(Exception e){
			exceptionLogger.error(EXCEPTION_CONSTANT, e);
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
		
		return new ResponseEntity<>(result, HttpStatus.OK);
	}

	private String getPassPhraseFromCache(String sessionId) {
		String passPhraseFomCache = (String) ssoPassPhraseCachePopulator.get(sessionId);
		if (StringUtils.isEmpty(passPhraseFomCache)) {
			BaseLoggers.exceptionLogger.error(SESSIONID_LOGGER_MESSAGE);
			return SESSIONID_ERROR_MESSAGE;
		}
		return passPhraseFomCache;
	}

	/**
	 * 
	 * Sending security questions to the server
	 * 
	 * 
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/askSecurityQuestions", method = RequestMethod.POST)
	@ResponseBody
	@Transactional(propagation = Propagation.REQUIRED)
	public ResponseEntity<Map<String, Object>> getSecurityQuestions(HttpServletRequest request) {
		String username = request.getHeader(USERNAME_CONSTANT);
		Map<String, Object> questionsmap = new HashMap<>();

		List<UserSecurityQuestion> userSecurityQuestionList = null;
		try{
			if (username != null && !"".equals(username)) {
				userSecurityQuestionList = userService.getUserSecurityQuestions(username);
			}
			BaseLoggers.flowLogger.info("Security questions being sent for %s from service : %s", username,
					ProductInformationLoader.getProductName());
			if (CollectionUtils.isNotEmpty(userSecurityQuestionList)) {
				for (UserSecurityQuestion userSecurityQuestion : userSecurityQuestionList) {
					if (userSecurityQuestion.getName() != null) {
						questionsmap.put(userSecurityQuestion.getName(), userSecurityQuestion.getId());
					}
				}
			}
			if (CollectionUtils.isEmpty(userSecurityQuestionList)) {
	
				BaseLoggers.exceptionLogger.error("No questions found for %s", username);
			}
		}catch(Exception e){
			exceptionLogger.error(EXCEPTION_CONSTANT, e);
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
		
		return new ResponseEntity<> (questionsmap, HttpStatus.OK);
	}

	@RequestMapping(value = "/getIvAndSalt")
	@ResponseBody
	public Map<String, String> getIvAndSaltValues(HttpServletRequest request) {
		BaseLoggers.flowLogger
				.info("IV and salt values sent for SSO from " + ProductInformationLoader.getProductName());
		Map<String, String> ivAndSalt = new HashMap<>();
		ivAndSalt.put("iv", AesUtil.getIv());
		ivAndSalt.put("salt", AesUtil.getSalt());
		return ivAndSalt;

	}

	/**
	 * Checking answers sent by server for security questions
	 * 
	 * 
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/checkAnswers")
	@ResponseBody
	@Transactional(propagation = Propagation.REQUIRED)
	public ResponseEntity<Map<String, String>> checkAnswers(HttpServletRequest request) {
		String username = request.getHeader(USERNAME_CONSTANT);
		String answers = request.getHeader("answers");
		String questions = request.getHeader("questions");

		Map<String, String> resultMap = new HashMap<>();
		resultMap.put(STATUS, FAILURE_MESSAGE);
		resultMap.put(USERNAME_CONSTANT, username);

		String[] answerArray = answers.split(",");
		String[] quesArray = questions.split(",");
		String answer;
		boolean flag = false;
		
		try{
			Map<Long, String> questionAnswerMap = userService.getUserQuestionAnswerMap(username);
	
			for (int i = 0; i < quesArray.length; i++) {
				answer = questionAnswerMap.get(Long.valueOf(quesArray[i]));
				if (answer.equalsIgnoreCase(answerArray[i])) {
					flag = true;
				} else {
					flag = false;
					break;
				}
			}
	
			if (flag) {
				// unblock the user
				User user = userService.findUserByUsername(username);
				if(user.isAccountLocked()){
					userService.activateUser(user);
				}
				resultMap.put(STATUS, SUCCESS_MESSAGE);
				resultMap.put("userid", user.getId().toString());
			}
		}catch(Exception e){
			return new ResponseEntity<>(new HashMap<>(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
		return new ResponseEntity<>(resultMap, HttpStatus.OK);

	}

	@RequestMapping(value = "/getForgotPswdSecurityQuestions", method = RequestMethod.POST)
	@ResponseBody
	@Transactional(propagation = Propagation.REQUIRED)
	public ResponseEntity<Map<String, Object>> getForgotPswdSecurityQuestions(HttpServletRequest request) {
		Map<String, Object> questionsmap = new HashMap<>();
		try {
			String username = ssoConfigUtility.decryptuserName(request.getHeader(USERNAME_CONSTANT));
			if (ssoConfigUtility.checkIfUserExists(username)) {
				questionsmap = ssoResetPasswordService.getForgotPasswordSecurityQuestions(username);
			}else{
				questionsmap.put("message", "InvalidUsername");
			}
		} catch (Exception e) {
			exceptionLogger.error(EXCEPTION_CONSTANT + ":", e);
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
		return new ResponseEntity<>(questionsmap, HttpStatus.OK);

	}

	@RequestMapping(value = "/resetForgotPassword", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<String> resetForgotPassword(HttpServletRequest request) {
		String result = FAILURE_MESSAGE;
		try {
			String changedPassword = request.getHeader("changedPassword");
			String oldPassword = request.getHeader(OLD_PSWD);
			String id = request.getHeader("userId");
			Long userId=null;
			if(StringUtils.isNotEmpty(id)){
				userId = Long.valueOf(id);
			}
			String token = request.getHeader("token");
			String sessionId = request.getHeader("tempHash");
			String passPhraseFomCache = getPassPhraseFromCache(sessionId);
			String username = request.getHeader(USERNAME_CONSTANT);

			if (SESSIONID_ERROR_MESSAGE.equals(passPhraseFomCache)) {
				result =  SESSIONID_ERROR_MESSAGE;
			}else{
				
			result = ssoResetPasswordService.resetForgotPassword(changedPassword, oldPassword, userId, username, token,
					passPhraseFomCache);
			}
		} catch (SystemException e) {
			exceptionLogger.error(EXCEPTION_CONSTANT + ":", e);
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}

		return new ResponseEntity<>(result, HttpStatus.OK);
	}

	@RequestMapping(value = "/checkTokenValidity", method = RequestMethod.POST)
	@ResponseBody
	@Transactional(propagation = Propagation.REQUIRED)
	public ResponseEntity<Boolean> checkTokenValidity(HttpServletRequest request) {
		Long userId = Long.valueOf(request.getHeader("userId"));
		String token = request.getHeader("token");
		try{
			return new ResponseEntity<>(Boolean.valueOf(authenticationTokenService.isTokenValid(userId, token)), HttpStatus.OK);
		}catch(Exception e){
			exceptionLogger.error(EXCEPTION_CONSTANT, e);
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
	@RequestMapping(value = "/removeOAuthAccessTokenByUsername")
	@Transactional(propagation = Propagation.REQUIRED)
	public void removeOAuthAccessTokenByUsername(HttpServletRequest request){ 
		
		String username = ssoConfigUtility.decryptuserName(request.getHeader(USERNAME_CONSTANT));
		BaseLoggers.flowLogger.debug("Clearing Oauth access token if it exists for username: {}", username);
		if (tokenStore != null) {
			((CustomOauthTokenStoreDAO) tokenStore).removeAccessTokensByUserName(username);
		}
	}

	@RequestMapping({ "/checkIfUserExists" })
	@ResponseBody
	@Transactional(propagation = Propagation.REQUIRED)
	public ResponseEntity<Map<String, Object>> checkIfUserExists(HttpServletRequest request) {
		String username = request.getHeader(USERNAME_CONSTANT);
		Map<String, Object> principalAttributes = new HashMap<>();
		try{
			String sourceSystem = userService.getUserSourceSystemByUsername(username);
	
			if (!StringUtils.isNotBlank(sourceSystem)) {
				principalAttributes.put(RESULT, "false");
			}else{
				getAllAllowedModulesForUser(username, principalAttributes);
				principalAttributes.put(RESULT, "true");
			}
		}catch(Exception e){
			exceptionLogger.error(EXCEPTION_CONSTANT, e);
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
		return new ResponseEntity<>(principalAttributes, HttpStatus.OK);
	}
	

	@SuppressWarnings("unchecked")
	@RequestMapping({ "/checkIfUserIdle" })
	@ResponseBody
	@Transactional(propagation = Propagation.REQUIRED)
	public Boolean checkIfUserIdle(@RequestBody String requestBody)
			throws IllegalAccessException, InvocationTargetException, NoSuchMethodException, NoSuchFieldException {

		if (requestBody == null) {
			throw new IllegalArgumentException("Required parameter requestBody is null");
		}

		String decryptedRequestBody = null;
		String decodedRequestBody = new String(java.util.Base64.getDecoder().decode(requestBody));
		try {
			decryptedRequestBody = AESEncryptionWithStaticKey.decrypt(decodedRequestBody,
					ssoEncryptionKey.getBytes(Charset.forName(UTF_8)));
		} catch (Exception e) {
			BaseLoggers.flowLogger.error("error occured in AESEncryptionWithStaticKey decrypt()" + e.getMessage(), e);
			throw fail(e);
		}
		String[] attributesArray = decryptedRequestBody.split(":");

		String clientBrowserIdleTimeout = attributesArray[0];
		String userId = attributesArray[1];
		String serviceTicketId = attributesArray[2];

		Boolean logoutDecision = Boolean.TRUE;
		if (sessionMappingStorageMethod == null) {
			sessionMappingStorageMethod = SingleSignOutHttpSessionListener.class
					.getDeclaredMethod("getSessionMappingStorage");
		}

		sessionMappingStorageMethod.setAccessible(true);
		HashMapBackedSessionMappingStorage hashMapBackedSessionMappingStorage = (HashMapBackedSessionMappingStorage) sessionMappingStorageMethod
				.invoke(null);
		Field field = hashMapBackedSessionMappingStorage.getClass().getDeclaredField("MANAGED_SESSIONS");
		field.setAccessible(true);
		Map<String, HttpSession> serviceTicketIdToSessionMap = (Map<String, HttpSession>) field
				.get(hashMapBackedSessionMappingStorage);
		HttpSession httpSession = serviceTicketIdToSessionMap.get(serviceTicketId);
		if (httpSession == null) {
			// there was no session corresponding to this TGT, hence it is okay
			// to consider this user inactive for this module
			return logoutDecision;
		}

		String updatedLogoutDecision = TimeoutHelper.checkIdleTimeout(userId, logoutDecision.toString(),
				Integer.valueOf(clientBrowserIdleTimeout), httpSession);
		return Boolean.valueOf(updatedLogoutDecision);
	}

	private IllegalStateException fail(Exception e) {
		return new IllegalStateException(e);
	}

	@RequestMapping(value = "/getAllUserModules")
	@ResponseBody
	@Transactional(propagation = Propagation.REQUIRED)
	public ResponseEntity<List<String>> getUserAllowedModules(HttpServletRequest request) {
		List<String> allowedModulesNames = new ArrayList<>();
		try {
			String username = ssoConfigUtility.decryptuserName(request.getHeader(USERNAME_CONSTANT));

			if (ssoConfigUtility.checkIfUserExists(username)) {
				NamedQueryExecutor<SourceProduct> query = new NamedQueryExecutor<SourceProduct>("Users.allowedModuleCodesByUsername")
						.addParameter(USERNAME_CONSTANT, username)
						.addParameter("activeFlag", true)
		        		.addParameter("userStatus", Arrays.asList(UserStatus.STATUS_ACTIVE,UserStatus.STATUS_LOCKED));

				List<SourceProduct> allAllowedModules = this.entityDao.executeQuery(query);
				for (SourceProduct Module : allAllowedModules) {
					allowedModulesNames.add(Module.getCode());
				}
			}
		} catch (Exception e) {
			exceptionLogger.error(EXCEPTION_CONSTANT + ":", e);
		}
		return new ResponseEntity<>(allowedModulesNames, HttpStatus.OK);

	}


	
	@RequestMapping(value = "/checkUserIsValid")
	@ResponseBody
	@Transactional(propagation = Propagation.REQUIRED)
	public ResponseEntity<Boolean> checkUserIsValid(@RequestBody String encyrptedUsername) {
		boolean result = false;
		try {
			String username = ssoConfigUtility.decryptuserName(encyrptedUsername);
			if(ssoConfigUtility.checkIfUserExists(username)){
				User user = userService.findUserByUsername(username);
				if(user.getUserStatus() == UserStatus.STATUS_LOCKED && user.getApprovalStatus() == ApprovalStatus.APPROVED){
					result = true;
				}
			}
		} catch (Exception e) {
			exceptionLogger.error(EXCEPTION_CONSTANT + ":", e);
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}

		return new ResponseEntity<>(result, HttpStatus.OK);
	}
	
	
	@RequestMapping(value = "/getMaxOTPResendCount")
	@ResponseBody
	@Transactional(propagation = Propagation.REQUIRED)
	public ResponseEntity<Integer> getMaxOTPResendCount() {
		Integer result = 3;
		try {
			 result = ssoConfigUtility.getMaxOTPResendCount();
			
		} catch (Exception e) {
			exceptionLogger.error(EXCEPTION_CONSTANT + ":", e);
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}

		return new ResponseEntity<>(result, HttpStatus.OK);
	}

	@RequestMapping(value = "/setloggedinbranch")
	@ResponseBody
	@Transactional(propagation = Propagation.REQUIRED)
	public ResponseEntity<OrgBranchInfo> setLoggedinBranch(HttpServletRequest request) {
		OrgBranchInfo orgBranchInfo = new OrgBranchInfo();
		try {
			String username = ssoConfigUtility.decryptuserName(request.getHeader(USERNAME_CONSTANT));

			if (ssoConfigUtility.checkIfUserExists(username)){
				Long branchId = Long.parseLong(request.getHeader("branchId"));
				Boolean allBranchesFlag = Boolean.valueOf(request.getHeader("allBranchesFlag"));
				orgBranchInfo = neutrinoCasRestAuthenticationServiceImpl.setLoggedinBranch(branchId, allBranchesFlag, username);
			}

		} catch (SystemException e) {
			exceptionLogger.error(EXCEPTION_CONSTANT + ":", e);
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
		
		return new ResponseEntity<>(orgBranchInfo, HttpStatus.OK);
	}

	@RequestMapping(value = "/getLoggedInUserPhoto")
	@Transactional(propagation = Propagation.REQUIRED)
	public void getLoggedInUserPhoto(HttpServletResponse response, HttpServletRequest request) throws IOException {

		String username;
		try {
			username = ssoConfigUtility.decryptuserName(request.getHeader(USERNAME_CONSTANT));
			if (ssoConfigUtility.checkIfUserExists(username)) {
				UserInfo ui = userService.getUserFromUsername(username);
				String userId = userService.getUserPhotoUrl(ui.getId());
				if (userId != null && !userId.isEmpty()) {
					renderImageUtility.renderImage(userId, response);
				}
			}
		} catch (SystemException e) {
			exceptionLogger.error(EXCEPTION_CONSTANT + ":", e);
		}

	}

	/**
	 * Parse all allowed modules for a given user.
	 * 
	 * 
	 * 
	 * @param username
	 * @param Attributes
	 *            from the Principal
	 * @return
	 */
	private void getAllAllowedModulesForUser(String username, Map<String, Object> principalAttributes) {

		NamedQueryExecutor<SourceProduct> query = new NamedQueryExecutor<SourceProduct>("Users.allowedModuleCodesByUsername")
				.addParameter(USERNAME_CONSTANT, username)
				.addParameter("activeFlag", true)
        		.addParameter("userStatus", Arrays.asList(UserStatus.STATUS_ACTIVE,UserStatus.STATUS_LOCKED));

		List<SourceProduct> allowedModuleCodes = this.entityDao.executeQuery(query);

		if (ValidatorUtils.hasNoElements(allowedModuleCodes)) {
			return;
		}
		for (SourceProduct allowedModuleCode : allowedModuleCodes) {
			principalAttributes.put(allowedModuleCode.getCode(), "allowed");

		}

		BaseLoggers.flowLogger.info("%s allowed for services : %s ", username  , allowedModuleCodes.toString());
	}
	
	
	private void setUserAuthoritiesInPrincipalAttribute(Map<String, Object> principalAttributes, UserInfo userInfo){
		
		Set<Authority> authorities = userInfo.getUserAuthorities();
		
		List<String> authorityList = new ArrayList<>();
		
		for(Authority authority : authorities){
			authorityList.add(authority.getAuthCode());
		}
		
		principalAttributes.put("authorities", authorityList);
	}

	private void prepareSsoUserObject(UserInfo userInfo, SsoUserInfo ssoUserInfo) {
		ssoUserInfo.setApprovedAndActiveUserBranchList(userInfo.getApprovedAndActiveUserBranchList());
		ssoUserInfo.setLoggedInBranch(userInfo.getLoggedInBranch());
	}

	@RequestMapping(value = "/uploadImages")
	@ResponseBody
	public ResponseEntity<String> uploadImage(@RequestParam(value = "file") MultipartFile file, HttpServletRequest request,
			HttpServletResponse response) {
		String result = "";
		try {
			String username = ssoConfigUtility.decryptuserName(request.getHeader(USERNAME_CONSTANT));
			if (!ssoConfigUtility.checkIfUserExists(username)
					&& !ssoConfigUtility.hasAuthority(username, "SSO_BANNER_UPLOAD")) {
				result = "Error occured while uploading";
			}else{

				Long imgCount = neutrinoCasRestAuthenticationServiceImpl.getActiveBannerCount();
	
				if (imgCount > Long.parseLong(maxAllowedBanners)) {
					result = "Can not upload more than " + maxAllowedBanners + " images.";
				}else{
	
					String imageTitle = request.getHeader("imageTitle");
					String imageCaption = request.getHeader("imageCaption");
					result = neutrinoCasRestAuthenticationServiceImpl.uploadBanner(file, imageTitle, imageCaption);
				}
			}
		} catch (SystemException e) {
			exceptionLogger.error(EXCEPTION_CONSTANT,e);
			result = e.getMessage();
		}catch(Exception e){
			exceptionLogger.error(EXCEPTION_CONSTANT,e);
			if(e.getMessage()!=null && e.getMessage().contains("FILE EXTENSION MISMATCH")){
				result = "corruptedImage";
			}else{
				return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
			}
		}
		
		return new ResponseEntity<>(result, HttpStatus.OK);
	}

	@RequestMapping(value = "/getUploadedImages")
	@ResponseBody
	@Transactional(propagation = Propagation.REQUIRED)
	public ResponseEntity<List<BannerStorageVO>> getUploadedImages(HttpServletRequest request, HttpServletResponse response) {
		try{
			return new ResponseEntity<>(neutrinoCasRestAuthenticationServiceImpl.findAllBanners(), HttpStatus.OK);
		}catch(Exception e){
			exceptionLogger.error(EXCEPTION_CONSTANT,e);
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@RequestMapping(value = "/deleteImage")
	@ResponseBody
	@Transactional(propagation = Propagation.REQUIRED)
	public ResponseEntity<String> deleteImage(HttpServletRequest request, HttpServletResponse response) {
		
		String result = null;
		
		try {
			String username = ssoConfigUtility.decryptuserName(request.getHeader(USERNAME_CONSTANT));
			if (!ssoConfigUtility.checkIfUserExists(username)
					&& !ssoConfigUtility.hasAuthority(username, "SSO_BANNER_UPLOAD")) {
				result =  "User is not authorized to delete the image";
			}else{
				String imageId = request.getHeader("imageId");
				neutrinoCasRestAuthenticationServiceImpl.deleteImage(imageId);
				result = SUCCESS_MESSAGE;
			}
		} catch (Exception e) {
			exceptionLogger.error(EXCEPTION_CONSTANT,e);
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
		return new ResponseEntity<>(result, HttpStatus.OK);
	}

	@RequestMapping(value = "/logoutSsoSession", method = RequestMethod.POST)
	@ResponseBody
	@Transactional(propagation = Propagation.REQUIRED)
	public String logoutSsoSession(HttpServletRequest request, HttpServletResponse response) {
		Map<String, String> ssoSessionMap = GsonUtil.parseJson(request.getHeader("ssoSessionMapJson"),
				getTypeForGsonConversion());
		neutrinoSecurityUtility.createSessionDestroyedEventEntry(ssoSessionMap);
		sessionModuleService.deleteSessionModuleMapping(ssoSessionMap.get(SESSION_ID_CONSTANT));
		sessionRegistry.removeSessionInformation(ssoSessionMap.get(SESSION_ID_CONSTANT));
		return SUCCESS_MESSAGE;
	}

	private Type getTypeForGsonConversion() {
		if (typeForGsonConversion == null) {
			typeForGsonConversion = new TypeToken<Map<String, String>>() {
			}.getType();
		}
		return typeForGsonConversion;
	}

	@RequestMapping(value = "/checkConcurrency", method = RequestMethod.POST)
	@ResponseBody
	@Transactional(propagation = Propagation.REQUIRED)
	public ResponseEntity<Map<String, String>> checkConcurrency(HttpServletRequest request) {
		String username = request.getHeader(USERNAME_CONSTANT);
		String module = request.getHeader(MODULE_CONSTANT);
		Map<String, String> questionsmap = new HashMap<>();
		UserInfo userInfo = securityService
				.getCompleteUserFromUsername(username);

		String isLoginAllowed=String.valueOf(sessionModuleService.checkForLoginFeasibility(userInfo,module));
		questionsmap.put("isLoginAllowed",isLoginAllowed);
		return new ResponseEntity<> (questionsmap, HttpStatus.OK);
	}
	
	@RequestMapping(value = "/checkSecurityQuestionAnswers")
	@ResponseBody
	public ResponseEntity<Map<String, String>> checkSecurityQuestionAnswers(@RequestParam("username")String username,@RequestParam("question1")String question1,
			@RequestParam("question2")String question2,@RequestParam("answer1")String answer1,@RequestParam("answer2")String answer2) {
		Map<String, String> resultMap = new HashMap<>();
		resultMap.put(STATUS, FAILURE_MESSAGE);

		try{
			if(StringUtils.isEmpty(question1) || StringUtils.isEmpty(question2) || StringUtils.isEmpty(answer1) || StringUtils.isEmpty(answer2)){
				resultMap.put(MESSAGE_CONSTANT, "IncompleteSecurityQuestionOrAnswer");
				return new ResponseEntity<>(resultMap, HttpStatus.OK);
			}
			
			if(question1.equals(question2)){
				resultMap.put(MESSAGE_CONSTANT, "DuplicateQuestions");
				return new ResponseEntity<>(resultMap, HttpStatus.OK);
			}
				
			String[] questionArray = {question1, question2};
			String[] answerArray = {answer1, answer2};	
			resultMap = securityQuestionService.checkSecurityQuestionAnswers(username, answerArray, questionArray);
		}catch(Exception e){
			exceptionLogger.error(EXCEPTION_CONSTANT, e);
			return new ResponseEntity<>(resultMap, HttpStatus.INTERNAL_SERVER_ERROR);
			
		}
		return new ResponseEntity<>(resultMap, HttpStatus.OK);
	}
	
	@RequestMapping(value="/validateUser", method = RequestMethod.POST)
	public ResponseEntity<SimplePrincipal> validateUser(@RequestParam("username")String username, HttpServletRequest request){
		HttpHeaders headers = new HttpHeaders();
		String decryptedUsername = ssoConfigUtility.decryptuserName(username);
		String[] arr = decryptedUsername.split(":");
		String userName = arr[0];
		Date date = new Date();
		
		if(arr.length>1 && date.getTime()>Long.parseLong(arr[1])) {
			headers.add(EXCEPTION_CONSTANT, "invalidRequest");
			return new ResponseEntity<>(null, headers, HttpStatus.OK);
		}
		
		DefaultPrincipalFactory factory = new DefaultPrincipalFactory();
		Map<String, Object> principalAttributes = new HashMap<>();
		
		String exception = neutrinoCasRestAuthenticationServiceImpl.checkValidFederatedUser(userName);
		
		if("none".equals(exception)) {
			if(handleSAMLUserConcurrency(request, userName)) {
				exception = CONCURRENCY_ERROR;
			}
			headers.add(EXCEPTION_CONSTANT, exception);
			setSamlUserAttributes(userName, principalAttributes);
			return new ResponseEntity<>(
					(SimplePrincipal) factory.createPrincipal(userName, principalAttributes),
					headers, HttpStatus.OK);
		}else {
			headers.add(EXCEPTION_CONSTANT, exception);
			return new ResponseEntity<>(null, headers, HttpStatus.OK);
		}
	}

	private boolean handleSAMLUserConcurrency(HttpServletRequest request, String username) {
		int oauthUsers = 0;
		if (tokenStore != null) {
			oauthUsers = ((CustomOauthTokenStoreDAO) tokenStore).findActiveTokensCountByUserName(username);
		}
		if ((LoginConstants.TGT.equals(request.getHeader(LoginConstants.TGT))
				&& !LoginConstants.TRUE.equals(request.getHeader(LoginConstants.IS_FORCED_LOGGED_IN_PARAMETER))) || oauthUsers >= 1) {
			return true;
		}
		
		return false;
	}

	private void setSamlUserAttributes(String userName, Map<String, Object> principalAttributes){
		getAllAllowedModulesForUser(userName, principalAttributes);
		UserInfo userInfo = securityService.getCompleteUserFromUsername(userName);
		setUserAuthoritiesInPrincipalAttribute(principalAttributes, userInfo);
		SsoUserInfo ssoUserInfo = new SsoUserInfo();
		prepareSsoUserObject(userInfo, ssoUserInfo);
		principalAttributes.put("userInfo", ssoUserInfo);
	}
	
	 	@RequestMapping(value = "/inOffice", method = RequestMethod.POST)
	 	@ResponseBody
	 	@Transactional
	    public boolean markInOffice(HttpServletRequest request) {
	 		String username = ssoConfigUtility.decryptuserName(request.getHeader("username"));
	        UserInfo userInfo = userService.getUserFromUsername(username);
	        User userObj = userService.findUserByUsername(userInfo.getUsername());
	        if (userObj.getOutOfOfficeDetails() != null) {
	            userObj.getOutOfOfficeDetails().setOutOfOffice(false);
	            userObj.getOutOfOfficeDetails().setFromDate(null);
	            userObj.getOutOfOfficeDetails().setToDate(null);
	            userObj.getOutOfOfficeDetails().setDelegatedToUserId(null);
	            userService.updateUser(userObj);
	            return true;
	        } else {
	            return false;
	        }

	    }
}