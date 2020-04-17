package com.nucleus.web.security.oauth.util;

import java.util.LinkedHashMap;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.oauth2.common.DefaultOAuth2AccessToken;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.TokenRequest;

import com.nucleus.password.reset.ResetPasswordService;
import com.nucleus.user.User;
import com.nucleus.user.UserService;

@Named("neutrinoTokenUtility")
public class NeutrinoTokenUtility {
	
	@Inject
	@Named(value = "userService")
	private UserService userService;
	
	@Inject
	@Named("resetPasswordService")
	private ResetPasswordService resetPasswordService;
	
	private static final String SECURITY_QUESTIONS = "security_questions";
	private static final String FORCE_PASSWORD_RESET_FOR_LOGIN = "force_password_reset_for_login";

	public OAuth2AccessToken setAdditionalInfoInToken(TokenRequest tokenRequest, OAuth2AccessToken token) {
		Map<String, Object> additionalInformationMap = new LinkedHashMap<>(token.getAdditionalInformation());
		Map<String, String> parameters = new LinkedHashMap<>(tokenRequest.getRequestParameters());
		String username = parameters.get("username");
		if (username == null) {
			throw new AuthenticationServiceException("username is null");
		}
		User user = userService.findUserByUsername(username.toLowerCase());
		if (user.isForcePasswordResetOnLogin()) {
			additionalInformationMap.put(SECURITY_QUESTIONS, resetPasswordService.getSecurityQuestionsList());
		}

		additionalInformationMap.put(FORCE_PASSWORD_RESET_FOR_LOGIN,
				Boolean.valueOf(user.isForcePasswordResetOnLogin()));
		((DefaultOAuth2AccessToken) token).setAdditionalInformation(additionalInformationMap);
		
		return token;
	}
}
