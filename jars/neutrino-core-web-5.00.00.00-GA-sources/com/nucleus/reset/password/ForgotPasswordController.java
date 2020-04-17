package com.nucleus.reset.password;

import com.nucleus.password.reset.ResetPasswordService;
import com.nucleus.security.oauth.TrustedSourceInfo;
import com.nucleus.security.oauth.domainobject.OauthClientDetails;
import com.nucleus.security.oauth.service.TrustedSourceService;
import com.nucleus.sso.password.SsoResetPasswordService;
import com.nucleus.user.User;
import com.nucleus.user.UserService;
import com.nucleus.user.questions.SecurityQuestionService;
import com.nucleus.web.common.controller.NonTransactionalBaseController;
import com.nucleus.web.security.AesUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

import static com.nucleus.logging.BaseLoggers.exceptionLogger;

/**
 * @author namrata.varshney
 *
 */
@RestController
@RequestMapping("/client-credential-auth/reset-password/")
public class ForgotPasswordController extends NonTransactionalBaseController {
	
	private static final String EXCEPTION_CONSTANT = "Exception";
	private static final String STATUS = "status";
	private static final String MESSAGE = "message";
	private static final String FAILURE = "failure";
	
	@Inject
	@Named("ssoResetPasswordService")
	private SsoResetPasswordService ssoResetPasswordService;
	
	@Inject
	@Named("clientDetails")
	private TrustedSourceService trustedSourceService;
	
    @Inject
    @Named("securityQuestionService")
    private SecurityQuestionService securityQuestionService;
    
    @Inject
    @Named("userService")
    private UserService userService;
    
	@Inject
	@Named("resetPasswordService")
	private ResetPasswordService resetPasswordService;
	
    @Value(value = "#{'${core.web.config.webClientToEncryptpwd}'}")
    private String webClientToEncryptpwd;

	
	@RequestMapping(value = "/getSecurityQuestionsList", method = RequestMethod.POST)
	public ResponseEntity<Map<String, Object>> getSecurityQuestionsList(@RequestAttribute("username") String username) {
		Map<String, Object> resultMap = new HashMap<>();
		
		try{
			resultMap = securityQuestionService.getSecurityQuestionsList(username);
			
		}catch(Exception e){
			exceptionLogger.error("Error getting security questions list", e);
			resultMap.put(STATUS, FAILURE);
			return new ResponseEntity<>(resultMap, HttpStatus.INTERNAL_SERVER_ERROR);
		}
		
		return new ResponseEntity<>(resultMap, HttpStatus.OK);
	}
	
	@RequestMapping(value = "/checkSecurityQuestionAnswers", method = RequestMethod.POST)
	public ResponseEntity<Map<String, String>> checkSecurityQuestionAnswers(@RequestAttribute("username") String username, @RequestAttribute("answer1") String answer1,
																			@RequestAttribute("answer2") String answer2, @RequestAttribute("question1") String question1, @RequestAttribute("question2") String question2){
		Map<String, String> resultMap = new HashMap<>();
		Boolean validInput = true;
		try{
			
			if(StringUtils.isEmpty(question1) || StringUtils.isEmpty(question2) || StringUtils.isEmpty(answer1) || StringUtils.isEmpty(answer2)){
				resultMap.put(STATUS, FAILURE);
				resultMap.put(MESSAGE, "IncompleteSecurityQuestionOrAnswer");
				validInput = false;
			}
			
			if(validInput && StringUtils.isNotEmpty(question1) && question1.equals(question2)){
				resultMap.put(STATUS, FAILURE);
				resultMap.put(MESSAGE, "DuplicateQuestions");
				validInput = false;
			}
				
			if(validInput){
				String[] questionArray = {question1, question2};
				String[] answerArray = {answer1, answer2};	
				resultMap = securityQuestionService.checkSecurityQuestionAnswers(username, answerArray, questionArray);
			}
			
		}catch(Exception e){
			exceptionLogger.error(EXCEPTION_CONSTANT, e);
			resultMap.put(STATUS, FAILURE);
			return new ResponseEntity<>(new HashMap<>(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
		return new ResponseEntity<>(resultMap, HttpStatus.OK);
	}
	
	@RequestMapping(value = "/resetUserPassword", method = RequestMethod.POST)
	public ResponseEntity<Map<String, String>> resetPassword(@RequestAttribute("newPassword") String newPassword,
															 @RequestAttribute("username") String username, @RequestAttribute("resetToken") String resetToken,  @RequestAttribute(value = "passPhrase",required = false) String passPhrase) {

			Map<String, String> result = new HashMap<>();
			try{
				User user = userService.findUserByUsername(username);
				if(!userService.isUserValidForPasswordReset(user)){
					result.put(STATUS, "invalidUser");
					return new ResponseEntity<>(result, HttpStatus.OK);
				}
				
				if(!UserService.SOURCE_DB.equals(user.getSourceSystem())){
					result.put(STATUS, "ldapUserError");
					return new ResponseEntity<>(result, HttpStatus.OK);
				}
				String decryptednewPassword = AesUtil.decrypt(newPassword, passPhrase,"Y".equalsIgnoreCase(webClientToEncryptpwd));
				String status = resetPasswordService.resetUserPassword(user.isLicenseAccepted(), username, null, decryptednewPassword, resetToken);
				result.put(STATUS, status);
			}catch(Exception e){
				exceptionLogger.error(EXCEPTION_CONSTANT, e);
				result.put(STATUS, FAILURE);
				return new ResponseEntity<>(result, HttpStatus.INTERNAL_SERVER_ERROR);
			}
		return new ResponseEntity<>(result, HttpStatus.OK);
	}
	
	@RequestMapping(value = "/sendMailToResetPassword", method = RequestMethod.POST)
	public ResponseEntity<Map<String, String>> sendForgotPasswordMail(@RequestAttribute("username") String username, HttpServletRequest request) {
		Map<String, String> resultMap = new HashMap<>();
		resultMap.put(STATUS, FAILURE);
		Boolean validInput = true;
		try{
			User user = userService.findUserByUsername(username);
			if(!userService.isUserValidForPasswordReset(user)){
				resultMap.put(MESSAGE, "invalidUser");
			}
			
			if(validInput && user.getMailId()==null){
				resultMap.put(MESSAGE, "NoMailRegistered");
				validInput = false;
			}
			
			if(validInput){
				resultMap = ssoResetPasswordService.sendResetPasswordMail(user);
			}
			
		}catch(Exception e){
			exceptionLogger.error(EXCEPTION_CONSTANT, e);
			resultMap.put(STATUS, FAILURE);
			return new ResponseEntity<>(resultMap, HttpStatus.INTERNAL_SERVER_ERROR);
		}
		
		return new ResponseEntity<>(resultMap, HttpStatus.OK);
	}
	
}
