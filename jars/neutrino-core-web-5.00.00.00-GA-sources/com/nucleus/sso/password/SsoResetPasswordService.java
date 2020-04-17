package com.nucleus.sso.password;

import java.io.IOException;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import com.nucleus.user.User;

public interface SsoResetPasswordService {
	
	public String getSecurityQuestionList();
	
	public String validatePassword(String username, String password, String passPhrase, HttpServletRequest request) throws IOException;
	
	public String resetPasswordOnLogin(JSONObject userInfo, String username, HttpServletRequest request , String passPhrase) throws JSONException;

	public Map<String, String> sendForgotPasswordMail(String username, String userEmailId);

	public Map<String, Object> getForgotPasswordSecurityQuestions(String username);

	public String resetForgotPassword(String changedPassword, String oldPassword, Long userId,String name,
			String token, String passPhrase);
	
	Map<String, String> forgotSecurityQuestion(String username, HttpServletRequest request);
	
	Map<String, String> sendResetPasswordMail(User user);
}
