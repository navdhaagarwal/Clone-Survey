package com.nucleus.password.reset;

import com.nucleus.businessmapping.service.UserManagementServiceCore;
import com.nucleus.core.common.NeutrinoRestTemplateFactory;
import com.nucleus.core.exceptions.SystemException;
import com.nucleus.core.genericparameter.service.GenericParameterService;
import com.nucleus.core.jwt.util.JotUtil;
import com.nucleus.security.oauth.TrustedSourceInfo;
import com.nucleus.security.oauth.dao.CustomOauthTokenStoreDAO;
import com.nucleus.security.oauth.dao.TrustedSourceDao;
import com.nucleus.security.oauth.service.TrustedSourceService;
import com.nucleus.user.*;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author shivendra.kumar
 *
 */
@Named("resetPasswordService")
public class ResetPasswordServiceImpl implements ResetPasswordService {

	private static final String SUCCESS_MESSAGE = "success";
	private static final String WRONG_OLD_PASS = "wrong_old_password";
	private static final String PASS_MATCHES_RECENT_PASS = "password_matches_recent_password";
	private static final String MAXIUM_NUMBER_OF_ATTEMPTS_EXCEEDED = "maximum_number_of_attempts_exceeded";

	private Map<String, String> messageMap;

	@Autowired
	private NeutrinoRestTemplateFactory restTemplateFactory;

	@Value("${INTG_BASE_URL}/restservice/revokeToken")
	private String revokeAccessTokenURL;

	@PostConstruct
	private void init() {
		messageMap  = new HashMap<>();
		messageMap.put("Wrong Old Password", WRONG_OLD_PASS);
		messageMap.put("New Password matches the recent passwords list . Please set another password .",
				PASS_MATCHES_RECENT_PASS);
		messageMap.put("Your attempts to reset password have failed", MAXIUM_NUMBER_OF_ATTEMPTS_EXCEEDED);

	}

	@Inject
	@Named("trustedSourceDao")
	private TrustedSourceDao trustedSourceDao;

	@Inject
	@Named("userService")
	private UserService userService;

	@Inject
	@Named("userManagementServiceCore")
	private UserManagementServiceCore userManagementServiceCore;

	@Inject
	@Named("genericParameterService")
	private GenericParameterService genericParameterService;

	@Autowired(required=false)
	private CustomOauthTokenStoreDAO tokenStore;

	@Inject
	@Named("userSessionManagerService")
	private UserSessionManagerServiceImpl userSessionManagerServiceImpl;

	@Inject
	@Named("clientDetails")
	private TrustedSourceService trustedSourceService;

	@Transactional
	@Override
	public String resetPasswordOnLogin(UserFirstTimeLoginDetails userFirstTimeLoginDetails, String decryptedOldPassword,
			String decryptedNewPassword, String accessToken) {

		String username = userFirstTimeLoginDetails.getUserLoginCredentials().getUsername();
		String result = this.resetUserPassword(userFirstTimeLoginDetails.getLicenseAccepted(), username,
				decryptedOldPassword, decryptedNewPassword, null);
		return this.setUserSecurityQuestionAnswer(userFirstTimeLoginDetails, username, accessToken, result);

	}

	@Transactional
	@Override
	public String resetUserPassword(Boolean isLicenseAccepted, String username, String oldPassword, String newPassword,
			String timeToken) {
		User user = userService.findUserByUsername(username, true);
		if (user == null) {
			throw new SystemException("User_can't_be_null");
		}
		UserInfo userInfo = new UserInfo(user);

		return userManagementServiceCore.updateUserPassword(userInfo, oldPassword, newPassword, timeToken, false,
				isLicenseAccepted);
	}

	private String setUserSecurityQuestionAnswer(UserFirstTimeLoginDetails userFirstTimeLoginDetails, String username,
			String token, String responseMessage) {

		if (!(SUCCESS_MESSAGE).equalsIgnoreCase(responseMessage)) {
			return getResponseMessage(responseMessage);
		}
		User user = userService.findUserByUsername(username, true);
		userFirstTimeLoginDetails.populateQuestionAnswersMap();

		if (userFirstTimeLoginDetails.getUserSecurityQuestionAnswer() == null
				|| userFirstTimeLoginDetails.getUserSecurityQuestionAnswer().size() != 2) {
			throw new SystemException("User_security_answers_not_complete");
		}

		List<UserSecurityQuestionAnswer> securityQuestionAnswers = createSecurityQuestionAnswersList(
				userFirstTimeLoginDetails);
		if (securityQuestionAnswers.size() != 2) {
			throw new SystemException("Two_security_question_answers_should_be_there.");
		}

		userService.updateUserSecurityQuestionAnswer(username, securityQuestionAnswers);
		user.setPasswordHintQuestion(userFirstTimeLoginDetails.getPasswordHintQuestion());
		user.setPasswordHintAnswer(userFirstTimeLoginDetails.getPasswordHintAnswer());
		userService.updateUser(user);
		if(!this.invalidateAccessToken(token).equals(HttpStatus.OK.value())) {
			throw new RuntimeException("Access token could not be revoked!!!!");
		}
		this.logOutLoggedInUser(user);

		
		return getResponseMessage(responseMessage);

	}

	private void logOutLoggedInUser(User user) {
		userSessionManagerServiceImpl.invalidateUserSession(user.getId());

	}

	@Override
	public Integer invalidateAccessToken(String accessTokenValue) {
		if (accessTokenValue != null && !accessTokenValue.isEmpty()) {
			if(tokenStore!=null) {
			OAuth2AccessToken accessToken = tokenStore.readAccessToken(accessTokenValue);
			if (accessToken != null) {
				SecurityContextHolder.getContext().setAuthentication(null);
				tokenStore.removeRefreshToken(accessToken.getRefreshToken());
				tokenStore.removeAccessToken(accessToken);
				return HttpStatus.OK.value();
			}
			}
		
		else {
			HttpHeaders headers = new HttpHeaders();
			headers.add("access_token", accessTokenValue);
			headers.add("accessToken", accessTokenValue);
			HttpEntity<Map<String, String>> entity = new HttpEntity<Map<String, String>>(new HashMap<>(), headers);

			RestTemplate restTemplate = restTemplateFactory.createRestTemplate();
			ResponseEntity<Map> response = restTemplate.exchange(revokeAccessTokenURL, HttpMethod.POST, entity, Map.class);
			String result = ((Map<String, Object>) JotUtil.decrypt("revokeresponse", response.getBody(), Map.class)).get("revokerequest").toString();
			if ("true".equals(result)) {
				return HttpStatus.OK.value();
			}
		}
		}

		return HttpStatus.UNAUTHORIZED.value();
	}

	/**
	 * Updates database with User's security questions and answers
	 * 
	 * @param userFirstTimeLoginDetails
	 * @return
	 */
	private List<UserSecurityQuestionAnswer> createSecurityQuestionAnswersList(
			UserFirstTimeLoginDetails userFirstTimeLoginDetails) {
		List<UserSecurityQuestionAnswer> securityQuestionAnswersList = new ArrayList<>();

		for (Map.Entry<Long, String> entry : userFirstTimeLoginDetails.getUserSecurityQuestionAnswer().entrySet()) {
			UserSecurityQuestionAnswer userSecurityQuestionAnswer = new UserSecurityQuestionAnswer();
			UserSecurityQuestion userSecurityquestion = genericParameterService.findById(entry.getKey(),
					UserSecurityQuestion.class);
			String answer = entry.getValue().toLowerCase();
			if (userSecurityquestion != null && answer != null && !answer.isEmpty()) {
				if(answer.length()>50){
					throw new SystemException("Max_50_characters_are_allowed_for_security_answer.");
				}
				userSecurityQuestionAnswer.setQuestion(userSecurityquestion);
				userSecurityQuestionAnswer.setAnswer(answer);
				securityQuestionAnswersList.add(userSecurityQuestionAnswer);
			}
		}
		return securityQuestionAnswersList;
	}

	@Override
	public String getPassPhrase(String token) {
		if(null!=tokenStore) {
		String clientId = tokenStore.readAuthentication(token).getOAuth2Request().getClientId();
		TrustedSourceInfo trustedSourceVO = (TrustedSourceInfo) trustedSourceService.loadClientByClientId(clientId);
		return trustedSourceVO.getPassPhrase();
		}
		return null;

	}

	@Transactional
	@Override
	public List<Object> getSecurityQuestionsList() {
		List<Object> finalSecurityQuestionList = new ArrayList<>();
		List<UserSecurityQuestion> securityQuestionList = null;
		securityQuestionList = (List<UserSecurityQuestion>) genericParameterService
				.retrieveTypes(UserSecurityQuestion.class);
		securityQuestionList.stream().forEach(entry -> {
			Map<String, Object> securityQuestionsMap = new HashMap<>();
			securityQuestionsMap.put("description", entry.getDescription());
			securityQuestionsMap.put("id", entry.getId());
			finalSecurityQuestionList.add(securityQuestionsMap);
		});

		return finalSecurityQuestionList;
	}
	
	private String getResponseMessage(String responseMessage){
		if (messageMap.get(responseMessage) != null) {
			responseMessage =  messageMap.get(responseMessage);
		}
		else if(responseMessage.contains(MAXIUM_NUMBER_OF_ATTEMPTS_EXCEEDED)){
			responseMessage = MAXIUM_NUMBER_OF_ATTEMPTS_EXCEEDED;
		}
		
		return responseMessage;
	}

}
