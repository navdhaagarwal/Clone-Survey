package com.nucleus.user.questions;

import java.util.Map;

/**
 * @author namrata.varshney
 *
 */
public interface SecurityQuestionService {

	/**
	 * Get the list of all security questions present in the database irrespective of user.
	 * It returns the list only if the security question answer has been set for the username provided.
	 * 
	 * @return
	 */
	public Map<String, Object> getSecurityQuestionsList(String username);
	
	/**
	 * Checks the security questions answers entered by the user with the security questions set by the user on first time login
	 * 
	 * @param username
	 * @param answerArray
	 * @param quesArray
	 * @return
	 */
	Map<String, String> checkSecurityQuestionAnswers(String username, String[] answerArray, String[] quesArray);
	
}
