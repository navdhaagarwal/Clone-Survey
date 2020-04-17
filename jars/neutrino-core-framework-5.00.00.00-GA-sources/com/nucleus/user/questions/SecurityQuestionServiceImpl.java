package com.nucleus.user.questions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;

import com.nucleus.authenticationToken.AuthenticationTokenService;
import com.nucleus.core.genericparameter.service.GenericParameterService;
import com.nucleus.user.User;
import com.nucleus.user.UserSecurityQuestion;
import com.nucleus.user.UserService;

/**
 * @author namrata.varshney
 *
 */
@Named("securityQuestionService")
public class SecurityQuestionServiceImpl implements SecurityQuestionService {
	
	private static final String MESSAGE = "message";
	private static final String STATUS = "status";
	private static final String SUCCESS = "success";
	private static final String FAILURE = "failure";

    @Value("${core.web.config.token.validity.time.millis}")
    private String tokenValidityTimeInMillis;
	
	@Inject
	@Named("genericParameterService")
	private GenericParameterService genericParameterService;
	
    @Inject
    @Named("userService")
    private UserService userService;
    
    @Inject
    @Named("authenticationTokenService")
    protected AuthenticationTokenService authenticationTokenService;

    @Override
   	public Map<String, Object> getSecurityQuestionsList(String username) {
       	Map<String, Object> resultMap = new HashMap<>();
   		List<Object> finalSecurityQuestionList = new ArrayList<>();
   		
   		User user = userService.findUserByUsername(username);
   		
   		if(!userService.isUserValidForPasswordReset(user)){
   			resultMap.put(STATUS, FAILURE);
   			resultMap.put(MESSAGE, "InvalidUsername");
   			return resultMap;
   		}
   		
   		if(!UserService.SOURCE_DB.equals(user.getSourceSystem())){
   			resultMap.put(STATUS, FAILURE);
   			resultMap.put(MESSAGE, "ldapUserError");
   			return resultMap;
   		}
   		
           List<UserSecurityQuestion> userSecurityQuestionList = userService.getUserSecurityQuestions(username);
           if(CollectionUtils.isEmpty(userSecurityQuestionList)){
           	resultMap.put(STATUS, FAILURE);
           	resultMap.put(MESSAGE, "NoSecurityQuestionFound");
           	return resultMap;
           }

   	    List<UserSecurityQuestion> securityQuestionList = null;
   	    securityQuestionList = genericParameterService.retrieveTypes(UserSecurityQuestion.class);
   	    securityQuestionList.stream().forEach(entry -> {
   	    Map<String,Object>	securityQuestionsMap = new HashMap<>();
   	    securityQuestionsMap.put("description", entry.getDescription());
   	    securityQuestionsMap.put("id", entry.getId());
   	    finalSecurityQuestionList.add(securityQuestionsMap);
   	    });
   	    
   	    resultMap.put(STATUS, "success");
   		resultMap.put("security_questions", finalSecurityQuestionList);
   	
   	    return resultMap;
   	}
    
    @Override
	public Map<String, String> checkSecurityQuestionAnswers(String username, String[] answerArray, String[] quesArray){

		Map<String, String> resultMap = new HashMap<>();
		resultMap.put(STATUS, "failure");

		String answer;
		boolean flag = false;
		
		User user = userService.findUserByUsername(username);
		
		if(!userService.isUserValidForPasswordReset(user)){
			resultMap.put(MESSAGE, "InvalidUsername");
			return resultMap;
		}
		
		if(!UserService.SOURCE_DB.equals(user.getSourceSystem())){
			resultMap.put(MESSAGE, "ldapUserError");
			return resultMap;
		}
		
		Map<Long, String> questionAnswerMap = userService.getUserQuestionAnswerMap(username);
		
		if(questionAnswerMap!=null){
			for (int i = 0; i < quesArray.length; i++) {
				answer = questionAnswerMap.get(Long.valueOf(quesArray[i]));
				
				if (StringUtils.isNotBlank(answer) && answer.trim().equalsIgnoreCase(answerArray[i].trim())) {
					flag = true;
				} else {
					flag = false;
					break;
				}
			}
		}

		if (flag) {
			String timeToken = this.authenticationTokenService.generatePasswordResetTokenForUser(user,
                    this.tokenValidityTimeInMillis);
			resultMap.put(STATUS, SUCCESS);
			resultMap.put("passwordResetToken", timeToken);
		}else{
			resultMap.put(MESSAGE, "WrongAnswers");
		}
		
		return resultMap;
	}
	

}
