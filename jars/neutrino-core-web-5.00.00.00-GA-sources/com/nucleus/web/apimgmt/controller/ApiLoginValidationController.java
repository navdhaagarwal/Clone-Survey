package com.nucleus.web.apimgmt.controller;

import com.nucleus.core.jwt.util.JotUtil;
import com.nucleus.logging.BaseLoggers;
import com.nucleus.password.reset.ResetPasswordService;
import com.nucleus.user.User;
import com.nucleus.user.UserDeviceMapping;
import com.nucleus.user.UserInfo;
import com.nucleus.user.UserService;
import com.nucleus.user.login.UserLoginDetailsChecker;
import com.nucleus.web.apimgmt.vo.CredentialValidationRequestDTO;
import com.nucleus.web.apimgmt.vo.CredentialValidationResponseDTO;
import com.nucleus.web.apimgmt.vo.DetailedMessage;
import com.nucleus.web.apimgmt.vo.UserFlightRequestDTO;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("apm")
public class ApiLoginValidationController {
	
	@Inject
	@Named("userService")
	private UserService userService;
	
	@Inject
	@Named("authdbManager")
	private ProviderManager dbAuthProviderManager;
	
	@Inject
	@Named("authldapManager")
	private ProviderManager ldapAuthProviderManager;
	

	@Inject
	@Named("resetPasswordService")
	private ResetPasswordService resetPasswordService;
	
	@Inject
	@Named("userLoginDetailsChecker")
	private UserLoginDetailsChecker userLoginDetailsChecker; 
	
	private static final String SECURITY_QUESTIONS = "security_questions";
	private static final String FORCE_PASSWORD_RESET_FOR_LOGIN = "force_password_reset_for_login";
	

	private static final String UNAUTHORIZED = "unauthorized";
	private static final String INVALID_GRANT ="invalid_grant";
	private static final String LOGIN_RESPONSE_ENC_KEY ="loginresponse";

	
	@PostMapping("/validateLogin")
	public ResponseEntity<Map<String, String>> validateLoginCredentials(
			@RequestBody Map<String, String> parameters) {
		CredentialValidationRequestDTO loginValidationDTO = JotUtil.decrypt("logindetails", parameters,
				CredentialValidationRequestDTO.class);
		ResponseEntity<Map<String, String>> responseEntity = null;
		if (loginValidationDTO == null) {
			responseEntity = createResponse(401, "JOT decryption failed for given payload.", HttpStatus.UNAUTHORIZED, false, UNAUTHORIZED);
			return responseEntity;
		}
		if (loginValidationDTO.getGrantType() != null) {
			if (loginValidationDTO.getGrantType().equals("refresh_token")) {
				responseEntity = getRefreshResponse(loginValidationDTO);
			} else if (loginValidationDTO.getGrantType().equals("password")) {
				responseEntity = getLoginValidationResponse(loginValidationDTO);
			} else if (loginValidationDTO.getGrantType().equals("federated")){
				responseEntity = getUserValidResponse(loginValidationDTO);
			}else {
				responseEntity = createResponse(401, "Grant type for request is invalid.", HttpStatus.UNAUTHORIZED, false, UNAUTHORIZED);
			}
		}
		return responseEntity;
	}

	private ResponseEntity<Map<String, String>> getRefreshResponse(CredentialValidationRequestDTO credentialValidationRequestDTO) {
		UserInfo userInfo = userService.getUserFromUsername(credentialValidationRequestDTO.getUsername());
		if (userInfo == null) {
			return createResponse(401, "No UserInfo found for given user name: " + credentialValidationRequestDTO.getUsername(), HttpStatus.UNAUTHORIZED, false,UNAUTHORIZED);
		}
		CredentialValidationResponseDTO credentialValidationResponseDTO = new CredentialValidationResponseDTO();
		credentialValidationResponseDTO.setAuthenticationValid(true);
		credentialValidationResponseDTO.setDetailedMessage(new DetailedMessage(200, "User name is valid and response is written into DTO additional data.",null));
		UserFlightRequestDTO userFlightRequestDTO = new UserFlightRequestDTO();
		userFlightRequestDTO.setAccountNonExpired(userInfo.isAccountNonExpired());
		userFlightRequestDTO.setAccountNonLocked(userInfo.isAccountNonLocked());
		userFlightRequestDTO.setCredentialsNonExpired(userInfo.isCredentialsNonExpired());
		userFlightRequestDTO.setEnabled(userInfo.isEnabled());
		userFlightRequestDTO.setPassword(userInfo.getPassword());
		userFlightRequestDTO.setUsername(credentialValidationRequestDTO.getUsername());
		userFlightRequestDTO.setSpringSecurityAuthorities(userInfo.getAuthorities());
		Map<String, Object> additionalData = new HashMap<>();
		additionalData.put("userFlightRequestDTO", userFlightRequestDTO);
		credentialValidationResponseDTO.setAdditionalData(additionalData);
		Map<String, String> encryptedBody = JotUtil.encrypt(LOGIN_RESPONSE_ENC_KEY, credentialValidationResponseDTO);
	    return new ResponseEntity<>(encryptedBody, new HttpHeaders(), HttpStatus.OK);
	}

	private ResponseEntity<Map<String, String>> getLoginValidationResponse(
			CredentialValidationRequestDTO loginValidationDTO) {
		String username = loginValidationDTO.getUsername();
		if (username == null) {
			BaseLoggers.exceptionLogger.error("User name is null.");
	    	return createResponse(401, "Username is null.", HttpStatus.UNAUTHORIZED, false, UNAUTHORIZED);
		}
		username = username.toLowerCase();
		String sourceSystem = userService.getUserSourceSystemByUsername(username);
		if (StringUtils.isBlank(sourceSystem)) {
			return createResponse(401, "User's source system is not available.", HttpStatus.UNAUTHORIZED, false, UNAUTHORIZED);
		}
		// if sourceSystem does not belong to 'db or 'ldap'
		if (!"db".equals(sourceSystem) && !"ldap".equals(sourceSystem)) {
			return createResponse(401, "User does not belong to ldap or db", HttpStatus.UNAUTHORIZED, false, UNAUTHORIZED);
		}
		ProviderManager providerManagerForUser = getAuthenticationProviderManager(sourceSystem);
		String password = loginValidationDTO.getPassword();
		Authentication userAuth = new UsernamePasswordAuthenticationToken(username, password);
		((AbstractAuthenticationToken) userAuth).setDetails(null);
		try {
			userAuth = providerManagerForUser.authenticate(userAuth);
		} catch (AccountStatusException ase) {
			return createResponse(401, "Cannot continue login account expired/locked/disabled. Details : " + ase.getMessage(), HttpStatus.UNAUTHORIZED, false, INVALID_GRANT);
		} catch (BadCredentialsException e) {
			userService.incrementFailedLoginCount(userService.getUserIdByUserName(username));
			return createResponse(401, "Bad Credentials while trying to login through Oauth : " + e.getMessage(), HttpStatus.UNAUTHORIZED, false,INVALID_GRANT);
		}
		if (userAuth == null || !userAuth.isAuthenticated()) {
			return createResponse(401, "Could not authenticate user: " + username, HttpStatus.UNAUTHORIZED, false,INVALID_GRANT);
		}
		UserInfo userInfo = (UserInfo) userAuth.getPrincipal();
		
		ResponseEntity<Map<String, String>> response = validateDeviceInfo(userInfo, loginValidationDTO);
		if(response!=null) {
			return response;
		}

		Map<String, Object> additionalInformationMap = new HashMap<>();
		Boolean isForcePasswordResetOnLogin = userInfo.getUserReference().isForcePasswordResetOnLogin();
		if (isForcePasswordResetOnLogin) {
			additionalInformationMap.put(SECURITY_QUESTIONS, resetPasswordService.getSecurityQuestionsList());
		}
		additionalInformationMap.put(FORCE_PASSWORD_RESET_FOR_LOGIN, isForcePasswordResetOnLogin);
		return createResponse(200, "Authentication successful for given username: " + username, HttpStatus.OK, true, null, additionalInformationMap);
	}

	private ProviderManager getAuthenticationProviderManager(String sourceSystem) {
		return "db".equals(sourceSystem) ? dbAuthProviderManager : ldapAuthProviderManager;
	}

	private UserDeviceMapping getUserDeviceMapping(String deviceId, String deviceType, UserInfo userInfo) {
		return userInfo.getRegisteredDevices()
				.stream().filter(udm -> udm.getDeviceId().equals(deviceId) 
						&& udm.getDeviceType().getCode().equalsIgnoreCase(deviceType)).findAny().orElse(null);
	}

	private ResponseEntity<Map<String, String>> createResponse(int code, String messageDescription, HttpStatus httpStatus, boolean isAuthorized,String errorCode) {
		DetailedMessage detailedMessage = new DetailedMessage(code, messageDescription,errorCode);
		CredentialValidationResponseDTO loginResponseDTO = new CredentialValidationResponseDTO(isAuthorized, httpStatus, detailedMessage);
		Map<String, String> encryptedBody = JotUtil.encrypt(LOGIN_RESPONSE_ENC_KEY, loginResponseDTO);
	    return new ResponseEntity<>(encryptedBody, new HttpHeaders(), HttpStatus.OK);
	}

	private ResponseEntity<Map<String, String>> createResponse(int code, String messageDescription, HttpStatus httpStatus, boolean isAuthorized, String errorCode, Map<String, Object> additionalInformationMap) {
		DetailedMessage detailedMessage = new DetailedMessage(code, messageDescription, errorCode);
		CredentialValidationResponseDTO loginResponseDTO = new CredentialValidationResponseDTO(isAuthorized, httpStatus, detailedMessage);
		Map<String, Object> additionalDataMap = new HashMap<>();
		Map<String, Object> tempMap = loginResponseDTO.getAdditionalData();
		if (tempMap != null) {
			additionalDataMap.putAll(tempMap);
		}
		additionalInformationMap.entrySet().stream().forEach(entry -> additionalDataMap.put(entry.getKey(), entry.getValue()));
		loginResponseDTO.setAdditionalData(additionalDataMap);
		Map<String, String> encryptedBody = JotUtil.encrypt(LOGIN_RESPONSE_ENC_KEY, loginResponseDTO);
		return new ResponseEntity<>(encryptedBody, new HttpHeaders(), HttpStatus.OK);
	}
	
	private ResponseEntity<Map<String, String>> getUserValidResponse(CredentialValidationRequestDTO loginValidationDTO) {
		String username = loginValidationDTO.getUsername();
		if (username == null) {
			BaseLoggers.exceptionLogger.error("User name is null.");
	    	return createResponse(401, "Username is null.", HttpStatus.UNAUTHORIZED, false, UNAUTHORIZED);
		}
		
		username = username.toLowerCase();
		UserInfo userInfo = userService.getUserFromUsername(username);
		
		if (userInfo == null) {
			return createResponse(401, "No UserInfo found for given user name: " + loginValidationDTO.getUsername(), HttpStatus.UNAUTHORIZED, false,UNAUTHORIZED);
		}
		
		User user = userService.findUserByUsername(username);
		
		String sourceSystem = user.getSourceSystem();
		if (!UserService.SOURCE_FEDERATED.equals(sourceSystem)) {
			return createResponse(401, "User does not belong to source system federated", HttpStatus.UNAUTHORIZED, false, UNAUTHORIZED);
		}
		
		try {
			userLoginDetailsChecker.validateLoginUser(user);
		}catch(AuthenticationException e) {
			return createResponse(401, e.getMessage(), HttpStatus.UNAUTHORIZED, false, UNAUTHORIZED);
		}
		
		ResponseEntity<Map<String, String>> response = validateDeviceInfo(userInfo, loginValidationDTO);
		if(response!=null) {
			return response;
		}
		
		return createResponse(200, "User login details are valid" + username, HttpStatus.OK, true,null);
		
	}
	
	private ResponseEntity<Map<String, String>> validateDeviceInfo(UserInfo userInfo, CredentialValidationRequestDTO loginValidationDTO) {
		if (userInfo.isDeviceAuthEnabled()) {
			String deviceId = loginValidationDTO.getDeviceId();
			String deviceType = loginValidationDTO.getDeviceType();
			if (StringUtils.isEmpty(deviceType)  || StringUtils.isEmpty(deviceId) ) {
				return createResponse(401, "Mandatory request param deviceType or deviceId is missing.", HttpStatus.UNAUTHORIZED, false,INVALID_GRANT);
			}
			UserDeviceMapping userDeviceMapping = getUserDeviceMapping(deviceId, deviceType, userInfo);
			if (userDeviceMapping == null) {
				return createResponse(401, "This device is not Authorized to connect.", HttpStatus.UNAUTHORIZED, false,INVALID_GRANT);
			}
		}
		
		return null;
	}
	
}
