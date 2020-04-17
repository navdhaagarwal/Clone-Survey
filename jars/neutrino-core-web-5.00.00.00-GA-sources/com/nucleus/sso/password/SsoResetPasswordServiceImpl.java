package com.nucleus.sso.password;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;

import com.nucleus.password.reset.ResetPasswordEmailHelper;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;

import com.nucleus.authenticationToken.AuthenticationTokenService;
import com.nucleus.businessmapping.service.UserManagementServiceCore;
import com.nucleus.core.genericparameter.service.GenericParameterService;
import com.nucleus.finnone.pro.base.Message;
import com.nucleus.logging.BaseLoggers;
import com.nucleus.mail.MailService;
import com.nucleus.otp.Utility;
import com.nucleus.passwordpolicy.PasswordCreationConfigurationFactory;
import com.nucleus.passwordpolicy.PasswordCreationPolicy;
import com.nucleus.passwordpolicy.PasswordPolicyDictWords;
import com.nucleus.passwordpolicy.PasswordPolicySpecChars;
import com.nucleus.passwordpolicy.passwordvalidations.AbstractPasswordValidation;
import com.nucleus.passwordpolicy.service.PasswordValidationService;
import com.nucleus.persistence.EntityDao;
import com.nucleus.service.BaseServiceImpl;
import com.nucleus.user.User;
import com.nucleus.user.UserInfo;
import com.nucleus.user.UserSecurityQuestion;
import com.nucleus.user.UserSecurityQuestionAnswer;
import com.nucleus.user.UserService;
import com.nucleus.user.UserSessionManagerService;
import com.nucleus.web.master.CommonFileIOMasterGridLoad;
import com.nucleus.web.security.CustomUsernamePasswordAuthenticationFilter;

import flexjson.JSONSerializer;

@Named("ssoResetPasswordService")
public class SsoResetPasswordServiceImpl extends BaseServiceImpl implements SsoResetPasswordService {
	
    private static final String                  PSWDKEY               = "password_pattren";
    private static final String           CONFIGERRORCODE       = "msg.8000001";
    private static final String RESET_PASSWORD_NOT_ALLOWED_LDAPUSER = "label.error.reset.password.not.allowed.ldapuser";
	private static final String SUCCESS = "success";
	private static final String FAILURE = "failure";
	private static final String MESSAGE = "message";

    @Value("${core.web.config.token.validity.time.millis}")
    private String                       tokenValidityTimeInMillis;
    
    @Value(value = "#{'${system.forgotPassword.mail.from}'}")
    private String                       forgotPasswordFromMail;
    
    @Value(value = "#{'${core.web.config.SSO.ticketvalidator.url.value}'}")
	private String ssoUrl;
    
    @Inject
    @Named("entityDao")
    protected EntityDao               entityDao;
    
    @Inject
    @Named("messageSource")
    protected MessageSource              messageSource;

    @Inject
    @Named("genericParameterService")
    private GenericParameterService   genericParameterService;
    
    @Inject
    @Named(value="passwordValidationService")
    private PasswordValidationService passwordValidationService;
    
    @Inject
    @Named("masterXMLDocumentBuilder")
    private CommonFileIOMasterGridLoad    commonFileIOMasterGridLoad;
    
    @Inject
    @Named("userService")
    private UserService userService;
    
    @Inject
    @Named("customUsernamePasswordAuthenticationFilter")
    private CustomUsernamePasswordAuthenticationFilter customUsernamePasswordAuthenticationFilter;
    
    @Inject
    @Named("userManagementServiceCore")
    private UserManagementServiceCore userManagementServiceCore;
    
    @Inject
    @Named("authenticationTokenService")
    protected AuthenticationTokenService authenticationTokenService;
    
    @Inject
    @Named("mailService")
    private MailService                  mailService;
    
    @Inject
    @Named("userSessionManagerService")
    private UserSessionManagerService     userSessionManagerService;


    @Inject
	@Named("resetPasswordEmailHelper")
	private ResetPasswordEmailHelper resetPasswordEmailHelper;


	@Override
	public String getSecurityQuestionList() {
		String securityQuestionJsonList = "";
		List<UserSecurityQuestion> securityQuestionList = null;
		securityQuestionList = genericParameterService.retrieveTypes(UserSecurityQuestion.class);
		
		try {
			securityQuestionJsonList = new JSONSerializer().include("id", "description").exclude("*")
					.serialize(securityQuestionList);
		} catch (Exception e) {
			BaseLoggers.exceptionLogger.error("Error in JSON Serialization" + e);
		}
		
		return securityQuestionJsonList;
	}

	@Override
	public String validatePassword(String username, String password, String passPhrase, HttpServletRequest request) throws IOException {
		
		if (StringUtils.isBlank(password))
            return "Password cannot be empty";
        String errorMsg = "";
        String passwordEntered = customUsernamePasswordAuthenticationFilter.decryptPass(password,
				passPhrase);
        if(StringUtils.isEmpty(passwordEntered)){
        	return "Password cannot be empty";
        }
        
        List<PasswordCreationPolicy> passwordCreationPolicies = passwordValidationService.getEnabledPasswordPolicy();
        if(passwordCreationPolicies.isEmpty()){
            String passwordPattern = commonFileIOMasterGridLoad.getResourceBundleFileReader(request, PSWDKEY);
            Boolean result = false;
            result = passwordEntered.matches(passwordPattern);
            if(!result)
                return "Invalid Password";
        }else {
            List dictWords = new ArrayList<>();
            List specialChars = new ArrayList<>();
            List<PasswordPolicyDictWords> dictWordsEntity = entityDao.findAll(PasswordPolicyDictWords.class);
            List<PasswordPolicySpecChars> specialCharsEntity = entityDao.findAll(PasswordPolicySpecChars.class);
            for(PasswordPolicyDictWords passwordPolicyDictWord : dictWordsEntity){
                dictWords.add(passwordPolicyDictWord.getDictWords());
            }
            for(PasswordPolicySpecChars passwordPolicySpecChars : specialCharsEntity){
                specialChars.add(passwordPolicySpecChars.getSpecChar());
            }

            for (PasswordCreationPolicy passwordCreationPolicy : passwordCreationPolicies) {

                    Message configError = new Message();
                    configError.setI18nCode(CONFIGERRORCODE);
                    configError.setMessageArguments(passwordCreationPolicy.getName());
                    String configErrorMsg = passwordValidationService.getMessageDescription(configError, passwordValidationService.getLocale());

                    Message message = new Message();
                    message.setI18nCode(passwordCreationPolicy.getErrorCode());
                    message.setMessageArguments(passwordCreationPolicy.getConfigValue());
                    String validationError = passwordValidationService.getMessageDescription(message, passwordValidationService.getLocale());

                    AbstractPasswordValidation abstractPasswordValidation = PasswordCreationConfigurationFactory.getPasswordValidationInstance(passwordCreationPolicy.getName());
                    String err = abstractPasswordValidation.validate(passwordEntered, username, passwordCreationPolicy.getConfigValue(), configErrorMsg, validationError,dictWords,specialChars);
                    StringBuilder bld = new StringBuilder();
                    bld.append(errorMsg);
                    if (err != null && !err.isEmpty())
                        bld.append(err + "\n");
                    errorMsg = bld.toString();
            }

        }
        return errorMsg;
		
	}

	@Override
	public String resetPasswordOnLogin(JSONObject userInfoStr, String username, HttpServletRequest request, String passPhrase) throws JSONException {
		
		String result = null;
		String oldPassword = request.getHeader("oldPassword");
		String newPassword = request.getHeader("newPassword");
		String isLicenseAccepted = request.getHeader("isLicenseAccepted");
		Boolean licenseAccepted = null;
		if(!StringUtils.isEmpty(isLicenseAccepted)){
			licenseAccepted = Boolean.valueOf(isLicenseAccepted);
		}
		List<UserSecurityQuestionAnswer> securityQuestionAnswers = null;
		if(userInfoStr.getString("userSecurityQuestionAnswer")!=null) {
			securityQuestionAnswers = setSecurityQuestionAnswer(userInfoStr);
		}
		if(securityQuestionAnswers==null || securityQuestionAnswers.size()==2) {
			UserInfo userInfo = userService.getUserFromUsername(username);
			oldPassword = customUsernamePasswordAuthenticationFilter.decryptPass(oldPassword,
					passPhrase);
			newPassword = customUsernamePasswordAuthenticationFilter.decryptPass(newPassword,
					passPhrase);
			result = userManagementServiceCore.updateUserPassword(userInfo, oldPassword, newPassword, null, false,licenseAccepted);
			if ((SUCCESS).equalsIgnoreCase(result) && userInfoStr.getString("userSecurityQuestionAnswer")!=null){
				userService.updateUserSecurityQuestionAnswer(username, securityQuestionAnswers);
				User user=userService.findUserByUsername(username,true);
				user.setPasswordHintQuestion(userInfoStr.getString("passwordHintQuestion"));
				user.setPasswordHintAnswer(userInfoStr.getString("passwordHintAnswer"));
				userService.updateUser(user);
			}
		} else {
			result = "Answer to all Security Questions are not provided";
		}
		
		
		return result;
	}

	
	private List<UserSecurityQuestionAnswer> setSecurityQuestionAnswer(JSONObject userInfoStr) throws JSONException {
		List<UserSecurityQuestionAnswer> securityQuestionAnswersList = new ArrayList<>();
			JSONObject jsonObj = (JSONObject)userInfoStr.get("userSecurityQuestionAnswer");
			
			@SuppressWarnings("unchecked")
			Iterator<String> keys = (Iterator<String>)jsonObj.keys();
			
			while(keys.hasNext()){
				UserSecurityQuestionAnswer userSecurityQuestionAnswer = new UserSecurityQuestionAnswer();
				String key = keys.next();
				Long id = Long.parseLong(key);
				UserSecurityQuestion userSecurityquestion = genericParameterService.findById(id, UserSecurityQuestion.class);
				userSecurityQuestionAnswer.setQuestion(userSecurityquestion);
				userSecurityQuestionAnswer.setAnswer((String)jsonObj.getString(key));
				securityQuestionAnswersList.add(userSecurityQuestionAnswer);
			}
		return securityQuestionAnswersList;
	}

	@Override
	public Map<String, String> sendForgotPasswordMail(String username, String userEmailId) {
		Map<String, String> messageMap = new HashMap<>();
        User lstUser = userService.userExistenceInForgotPassword(username, userEmailId);
        if (lstUser != null) {
        	messageMap = sendResetPasswordMail(lstUser);
        	} 
        else {
        	 messageMap.put(MESSAGE, FAILURE);
        }
        return messageMap;
	}

	public String getForgotPasswordFromMail() {
        return forgotPasswordFromMail;
    }

    public void setForgotPasswordFromMail(String forgotPasswordFromMail) {
        BaseLoggers.flowLogger.info("Setting forgotPasswordFromMail to {}", forgotPasswordFromMail);
        this.forgotPasswordFromMail = forgotPasswordFromMail;
    }

    @Override
	public Map<String, Object> getForgotPasswordSecurityQuestions(String username) {
		
		Map<String, Object> map = new HashMap<>();
		
		 User lstUser = userService.findUserByUsername(username);
		
		 if(!userService.isUserValidForPasswordReset(lstUser)){
			 map.put(MESSAGE, "InvalidUsername");
			 return map;
		 }
		 
		 String sourceSystem = lstUser.getSourceSystem();
		 if (!"db".equals(sourceSystem)) {
		    map.put("ldapMessage", messageSource.getMessage(RESET_PASSWORD_NOT_ALLOWED_LDAPUSER, new String[]{lstUser.getUsername()}, getUserLocale()));
		    return map;
		 } 
		 List<UserSecurityQuestion> userSecurityQuestionList = null;
		
		 userSecurityQuestionList = userService.getUserSecurityQuestions(username);
		
		 if (CollectionUtils.isNotEmpty(userSecurityQuestionList)) {
		    map.put(MESSAGE, "success");
		    map.put("userSecurityQuestionList", getSecurityQuestionList());
		 } else {
		    map.put(MESSAGE, "NoSecurityQuestionFound");
		}	
	        
		return map;
	}


	@Override
	public String resetForgotPassword(String changedPassword, String oldPassword, Long id,String name,
			String token, String passPhrase) {
		
		User thisUser = null;
		
		Long userId = id;
		if(id==null){
			userId = userService.getUserIdByUserName(name);
		}
		
		if(!checkIfTokenIsValid(userId, token)){
			return "not a valid token or the token has expired";
		}
		 
		Map<String, ?> thisUserDetails = userManagementServiceCore.findUserById(userId);
        thisUser = (User) thisUserDetails.get("user");
        String username = thisUser.getUsername();
        String decryptedChangedPassword = customUsernamePasswordAuthenticationFilter
                .decryptPass(changedPassword, passPhrase);
        String decryptedOldPassword = customUsernamePasswordAuthenticationFilter
                .decryptPass(oldPassword, passPhrase);
        
        if ((StringUtils.isNotBlank(decryptedChangedPassword) && (StringUtils.isNotBlank(decryptedOldPassword) || StringUtils
                .isNotBlank(token))) || (userId != null && token != null)) {
            User user = null;
            UserInfo userInfo = null;
            String sourceSystem = null;
            if (StringUtils.isNotBlank(token) && userId != null) {
                user = thisUser;
            } else {
                user = userService.findUserByUsername(username);
            }
            
            if (user != null) {
                userInfo = new UserInfo(user);
                sourceSystem = user.getSourceSystem();
            }
            
            if (StringUtils.isNotBlank(sourceSystem) && sourceSystem.equals(UserService.SOURCE_DB)) {
				boolean forceResetPasswordFlagToSet = (thisUser.getSecurityQuestionAnswers() == null || thisUser.getSecurityQuestionAnswers().size() == 0) ? user.isForcePasswordResetOnLogin() : false;
				String result = userManagementServiceCore.updateUserPassword(userInfo, decryptedOldPassword, decryptedChangedPassword, token, forceResetPasswordFlagToSet, userInfo.getLicenseAccepted());
                if (result.equals(SUCCESS)) {
                   invalidateLoggedinUser(username);
                    return SUCCESS;
                } else {
                    return result;
                }
            }
        }
        return FAILURE;
	}
	
	private Boolean checkIfTokenIsValid(Long userId, String token){
		if (userId != null && token != null) {
            Map<String, ?> thisUserDetails = userManagementServiceCore.findUserById(userId);
            User user = (User) thisUserDetails.get("user");
			if (!(user.getPasswordResetToken().getTokenId() != null
					&& (user.getPasswordResetToken().getTokenId().equals(authenticationTokenService.getEncryptedToken(token))
					|| user.getPasswordResetToken().getTokenId().equals(authenticationTokenService.getEncryptedTokenMD5(token)))
					&& user.getPasswordResetToken().getTokenValidity().isAfter(DateTime.now()))) {
				return false;
			}
		}
		
		return true;
	}
	
	private void invalidateLoggedinUser(String username){
		 if (StringUtils.isBlank(username)) {
             BaseLoggers.flowLogger.info("Expiring all user sessions and telling user to login again");
             userSessionManagerService.invalidateCurrentLoggedinUserSession();
         }
	}

	@Override
	public Map<String, String> forgotSecurityQuestion(String username, HttpServletRequest request) {
		
		Map<String, String> map = new HashMap<>();
		
        User user = userService.findUserByUsername(username);

		if(!userService.isUserValidForPasswordReset(user)){
			map.put(MESSAGE, "InvalidUsername");
			return map;
		}

        if(StringUtils.isEmpty(user.getMailId())){
        	map.put(MESSAGE, "NoMailRegistered");
        	return map;
        }

        map = sendResetPasswordMail(user);
        return map;
	}
	
	@Override
	public Map<String, String> sendResetPasswordMail(User user) {
		Map<String, String> messageMap = new HashMap<>();
		String sourceSystem = user.getSourceSystem();
		if (!"db".equals(sourceSystem)) {
			messageMap.put("ldapMessage", messageSource.getMessage(RESET_PASSWORD_NOT_ALLOWED_LDAPUSER,new String[]{user.getUsername()} , getUserLocale()));
			return messageMap;
		} 
		
		String mailId = user.getMailId();
		Long userId = user.getId();
		String timeToken = this.authenticationTokenService.generatePasswordResetTokenForUser(user,
                this.tokenValidityTimeInMillis);
      
        BaseLoggers.flowLogger.debug("User updated with Token ID and reset Password Time Stamp");
        String subject = resetPasswordEmailHelper.getForgotPasswordEmailSubject();
        String fromEmaiId = forgotPasswordFromMail;
		Map<String, Object> dataMap = new HashMap<>();
		String tokenLink = ssoUrl+ "/login?u="
				+ userId
				+ "&tt="
				+ timeToken;

		dataMap.put("resetPasswordLink",tokenLink);
		dataMap.put("userName",user.getDisplayName());
		dataMap.put("tokenValidityMinites",Long.parseLong(this.tokenValidityTimeInMillis)/60000);

		String htmlBody = resetPasswordEmailHelper.getEmailBody(dataMap,"forgotPasswordEmail.vo");
        BaseLoggers.flowLogger.debug("Sending Mail to the Registered User Id....");
        mailService.sendMail(htmlBody, subject, mailId, fromEmaiId);
        BaseLoggers.flowLogger.debug("Mail Sent to the Registered Email ID of %s", user.getUsername());
        messageMap.put(MESSAGE, SUCCESS);
        messageMap.put("maskedEmail", getMaskedEmail(mailId));
	
		return messageMap;
		
	}
	
	private String getMaskedEmail(String mailId) {
		String[] parts = mailId.split("@");
		return Utility.maskString(parts[0], 1, parts[0].length() - 1, "*") + '@' + parts[1];
		}

}
