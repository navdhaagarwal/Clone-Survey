package com.nucleus.password.reset;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * @author shivendra.kumar
 *
 */
public class UserFirstTimeLoginDetails implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 14765L;

	private String passwordHintQuestion;

	private String passwordHintAnswer;

	private UserLoginCredentials userLoginCredentials;

	private Boolean licenseAccepted;
	
	private Long question1;
	
	private Long question2;
	
	private String answer1;
	
	private String answer2;

	private Map<Long, String> userSecurityQuestionAnswer = new HashMap<>();

	public Boolean getLicenseAccepted() {
		return licenseAccepted;
	}

	public void setLicenseAccepted(Boolean licenseAccepted) {
		this.licenseAccepted = licenseAccepted;
	}

	public String getPasswordHintQuestion() {
		return passwordHintQuestion;
	}

	public void setPasswordHintQuestion(String passwordHintQuestion) {
		this.passwordHintQuestion = passwordHintQuestion;
	}

	public Long getQuestion1() {
		return question1;
	}

	public void setQuestion1(Long question1) {
		this.question1 = question1;
	}

	public Long getQuestion2() {
		return question2;
	}

	public void setQuestion2(Long question2) {
		this.question2 = question2;
	}

	public String getAnswer1() {
		return answer1;
	}

	public void setAnswer1(String answer1) {
		this.answer1 = answer1;
	}

	public String getAnswer2() {
		return answer2;
	}

	public void setAnswer2(String answer2) {
		this.answer2 = answer2;
	}

	public String getPasswordHintAnswer() {
		return passwordHintAnswer;
	}

	public void setPasswordHintAnswer(String passwordHintAnswer) {
		this.passwordHintAnswer = passwordHintAnswer;
	}

	public Map<Long, String> getUserSecurityQuestionAnswer() {
		return userSecurityQuestionAnswer;
	}

	public void setUserSecurityQuestionAnswer(Map<Long, String> userSecurityQuestionAnswer) {
		this.userSecurityQuestionAnswer = userSecurityQuestionAnswer;
	}

	public UserLoginCredentials getUserLoginCredentials() {
		return userLoginCredentials;
	}

	public void setUserLoginCredentials(UserLoginCredentials userLoginCredentials) {
		this.userLoginCredentials = userLoginCredentials;
	}
	
	public void populateQuestionAnswersMap(){
		userSecurityQuestionAnswer.put(question1, answer1);
		userSecurityQuestionAnswer.put(question2, answer2);
		
	}

}
