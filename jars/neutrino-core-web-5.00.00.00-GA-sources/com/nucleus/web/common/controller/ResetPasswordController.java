package com.nucleus.web.common.controller;

import static com.nucleus.web.security.AesUtil.PASS_PHRASE;

import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.authentication.session.SessionAuthenticationException;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.nucleus.authenticationToken.AuthenticationTokenService;
import com.nucleus.businessmapping.service.UserManagementServiceCore;
import com.nucleus.config.persisted.service.ConfigurationService;
import com.nucleus.config.persisted.vo.ConfigurationVO;
import com.nucleus.entity.SystemEntity;
import com.nucleus.logging.BaseLoggers;
import com.nucleus.user.User;
import com.nucleus.user.UserInfo;
import com.nucleus.user.UserProfile;
import com.nucleus.user.UserSecurityQuestionAnswer;
import com.nucleus.user.UserService;
import com.nucleus.web.security.CustomUsernamePasswordAuthenticationFilter;
import com.nucleus.web.security.InvalidLoginTokenException;
import com.nucleus.web.security.SystemSetupUtil;

/**
 *
 * @author Nucleus Software Exports Limited
 */
@Controller
@Transactional
@RequestMapping(value = "/resetPassword")
public class ResetPasswordController extends BaseController {

	@Inject
	@Named("userService")
	protected UserService                          userService;

	@Inject
	@Named("customUsernamePasswordAuthenticationFilter")
	private AbstractAuthenticationProcessingFilter customUsernamePasswordAuthenticationFilter;

	@Inject
	@Named("userManagementServiceCore")
	private UserManagementServiceCore              userManagementServiceCore;

	@Inject
	@Named("authenticationTokenService")
	protected AuthenticationTokenService           authenticationTokenService;

	@Inject
	@Named("configurationService")
	protected ConfigurationService       configurationService;

	@Inject
	@Named("messageSource")
	protected MessageSource              messageSource;

	@Value("${core.web.config.webClientToEncryptpwd}")
	private String                                 webClientToEncryptpwd;

	@Inject
	@Named(value = "systemSetupUtil")
	private SystemSetupUtil 					   systemSetupUtil;

	private static final String CAPTCHA_VALIDATION_FAILED 	  = "login.controller.captcha.validation.failed";

	private static final String USER_ALREADY_LOGGED 		  = "login.controller.user.already.logged";

	private static final String INVALID_CREDENTIALS 		  = "login.controller.invalid.credentials";

	private static final String BRANCH_TIME_VALIDATION_FAILED = "login.controller.invalid.login.time";

	private static final String NOT_WORKING_DAY 			  = "login.controller.invalid.working.day";

	private static final String INVALID_PAGE_SESSION 		  = "ERR.PAGE.EXPIRED.MSG";

	private static final String INVALID_CURRENT_SESSION 	  = "ERR.INVALIDSESSION.MSG";

	private static final String LICENSE_ACCEPTED 			  = "licenseAccepted";

	private static final String FORCE_RESET_PSWD_PAGE 	      = "forcePasswordReset";

	private static final String REDIRECT_KEY	      		  = "redirect:";

	private static final String FAILED_PSWD_RESET_ATTEMPTS     = "config.user.allowed.failedPasswordResetAttempts";

	private static final String RESET_PSWD     			      = "resetPassword";

	private static final String TOKEN_VALID     			  = "tokenValid";

	private static final String AUTHENTICATION_SUCCESS 		  = "onAuthenticationSuccess";

	private static final String ELIGIBLE_FOR_DIRECT_RESET_PSWD = "eligibleForDirectResetPassword";

	@RequestMapping(value = "/resetPwd/{timeTokenID}")
	public String resetPassword(@PathVariable String timeTokenID, ModelMap model) {
		BaseLoggers.flowLogger.debug("Checking the Time bound Token Expiry");
		User user = userService.findUserByPasswordResetTimeToken(timeTokenID);
		if (user != null) {
			model.put("userId", user.getId());
			if (user.getPasswordResetToken() != null) {
				model.put("token", timeTokenID);
				model.put("tokenized", true);
				model.put(TOKEN_VALID, authenticationTokenService.isTokenValid(user.getId(), timeTokenID));

			} else {
				model.put(TOKEN_VALID, "false");
			}
		}
		return RESET_PSWD;

	}

	@RequestMapping(value = "/redirectDirectToResetPasswordPage")
	public String redirectToResetPasswordPage() {
		return RESET_PSWD;
	}

	@RequestMapping(value = "/redirectDirectToResetPasswordPage/{userId}/{token}")
	public String redirectToResetPasswordPageTokenized(@PathVariable Long userId, @PathVariable String token, ModelMap model) {
		model.put("tokenized", true);
		model.put(TOKEN_VALID, authenticationTokenService.isTokenValid(userId, token));
		model.put("userId", userId);
		model.put("token", token);
		return RESET_PSWD;
	}

	@RequestMapping(value = "/forceResetPasswordOnLogin/{username}")
	public String forcePasswordResetOnFirstLogin(@PathVariable("username") String username, ModelMap map,
												 HttpServletRequest request) {
		request.getSession().setAttribute(PASS_PHRASE, RandomStringUtils.randomNumeric(8));
		map.put("isWebClientToEncryptpwd", "Y".equalsIgnoreCase(webClientToEncryptpwd));
		map.put("username", username);
		User user = userService.findUserByUsername(username);
		String targetURL = null;
		boolean isUserAuthenticated = Boolean.FALSE;
		boolean isUserEligibleForDirectResetPassword = Boolean.FALSE;
		boolean isLicenseAccepted = Boolean.FALSE;
		if (request.getSession().getAttribute(AUTHENTICATION_SUCCESS) != null) {
			isUserAuthenticated = Boolean.valueOf(request.getSession().getAttribute(AUTHENTICATION_SUCCESS).toString());
			request.getSession().removeAttribute(AUTHENTICATION_SUCCESS);
		}
		if (request.getSession().getAttribute(ELIGIBLE_FOR_DIRECT_RESET_PSWD) != null) {
			isUserEligibleForDirectResetPassword = Boolean.valueOf(request.getSession().getAttribute(ELIGIBLE_FOR_DIRECT_RESET_PSWD).toString());
			request.getSession().removeAttribute(ELIGIBLE_FOR_DIRECT_RESET_PSWD);
		}
		if (request.getAttribute(LICENSE_ACCEPTED) != null) {
			isLicenseAccepted = Boolean.valueOf(request.getAttribute(LICENSE_ACCEPTED).toString());
			request.getSession().setAttribute(LICENSE_ACCEPTED, isLicenseAccepted);
		}
		targetURL = doDetermineTargetUrl(user, isUserAuthenticated, isUserEligibleForDirectResetPassword, isLicenseAccepted);

		UserProfile userProfile = new UserProfile();
		map.put("userProfile", userProfile);
		return targetURL;
	}

	@RequestMapping(value = "/resetPasswordOnLogin")
	@ResponseBody
	public String resetPasswordOnLogin(UserProfile userProfile,@RequestParam(required = true) String newPassword,
									   HttpServletRequest request, HttpServletResponse response) {
		boolean captchaEnabled = false;
		Boolean isLicenseAccepted = null;
		String result = null;
		if (request.getSession().getAttribute(LICENSE_ACCEPTED) != null) {
			isLicenseAccepted = Boolean.valueOf(request.getSession().getAttribute(LICENSE_ACCEPTED).toString());
		}
		// in case of CustomUsernamePasswordAuthenticationFilter capcha needs to be disabled
		if (customUsernamePasswordAuthenticationFilter instanceof CustomUsernamePasswordAuthenticationFilter) {
			CustomUsernamePasswordAuthenticationFilter newName = (CustomUsernamePasswordAuthenticationFilter) customUsernamePasswordAuthenticationFilter;
			captchaEnabled = newName.isCaptchaEnabled();
			newName.setCaptchaEnabled(captchaEnabled);
		}

		Authentication authentication = null;

		try {
			// since reset password is applicable only for db users and not ldap
			authentication = customUsernamePasswordAuthenticationFilter.attemptAuthentication(request, response);
		} catch (Exception exception) {
			result = getErrorMessageFromException(exception,request);
			flushCurrentTransaction();
			return result;
		} finally {
			/** Set catchaEnabled to its prior value**/
			if (customUsernamePasswordAuthenticationFilter instanceof CustomUsernamePasswordAuthenticationFilter) {
				// in case of CustomUsernamePasswordAuthenticationFilter capcha needs to be set to default value after
				// authentication
				CustomUsernamePasswordAuthenticationFilter newName = (CustomUsernamePasswordAuthenticationFilter) customUsernamePasswordAuthenticationFilter;
				newName.setCaptchaEnabled(captchaEnabled);
			}
		}

		if (authentication.isAuthenticated()) {
			getUserDetails();
			
			List<UserSecurityQuestionAnswer> securityQuestionAnswers = null;
			if (userProfile != null && userProfile.getAssociatedUser() != null
					&& userProfile.getAssociatedUser().getSecurityQuestionAnswers() != null) {
				securityQuestionAnswers = userProfile.getAssociatedUser().getSecurityQuestionAnswers();
			} 
			if(securityQuestionAnswers == null || securityQuestionAnswers.size()==2) {
				String oldPassword = ((CustomUsernamePasswordAuthenticationFilter) customUsernamePasswordAuthenticationFilter)
						.decryptPass(request.getParameter("password"), (String) request.getSession(false).getAttribute(PASS_PHRASE));
				String passwordNew = ((CustomUsernamePasswordAuthenticationFilter) customUsernamePasswordAuthenticationFilter)
						.decryptPass(newPassword, (String) request.getSession(false).getAttribute(PASS_PHRASE));
				result = userManagementServiceCore.updateUserPassword((UserInfo) authentication.getPrincipal(),
						oldPassword, passwordNew, null, false, isLicenseAccepted);
				if (("success").equalsIgnoreCase(result)) {
					if(isLicenseAccepted != null && isLicenseAccepted) {
						request.getSession().removeAttribute(LICENSE_ACCEPTED);	
					}
					if(securityQuestionAnswers!=null) {
						userService.updateUserSecurityQuestionAnswer(request.getParameter("username"),
								userProfile.getAssociatedUser().getSecurityQuestionAnswers());
						User user = userService.findUserByUsername(request.getParameter("username"), true);
						user.setPasswordHintQuestion(userProfile.getAssociatedUser().getPasswordHintQuestion());
						user.setPasswordHintAnswer(userProfile.getAssociatedUser().getPasswordHintAnswer());
						userService.updateUser(user);	
					}				
				}
			} else {
				result = "Answer to all Security Questions are not provided";
			}			
		} else {
			result = "failure";
		}
		flushCurrentTransaction();
		return result;
	}

	public String getWebClientToEncryptpwd() {
		return webClientToEncryptpwd;
	}

	public void setWebClientToEncryptpwd(String webClientToEncryptpwd) {
		this.webClientToEncryptpwd = webClientToEncryptpwd;
	}

	private String doDetermineTargetUrl(User user, boolean isUserAuthenticated, boolean isUserEligibleForDirectResetPassword,
										boolean isLicenseAccepted) {
		String redirectToLoginFormUrl = REDIRECT_KEY + systemSetupUtil.getLoginFormUrl();
		String targetUrl = null;
		if (isUserAuthenticated && user != null && user.isForcePasswordResetOnLogin()) {
			if (isUserEligibleForDirectResetPassword || isLicenseAccepted) {
				targetUrl = FORCE_RESET_PSWD_PAGE;
			} else {
				targetUrl = redirectToLoginFormUrl;
			}
		} else {
			targetUrl = redirectToLoginFormUrl;
		}
		return targetUrl;
	}

	private String getErrorMessageFromException(Exception exception, HttpServletRequest request) {
		// Assign an error message
		String errorMessage = null;
		Locale locale = request.getLocale();
		Class<?> exceptionClass = exception.getClass();
		if (InvalidLoginTokenException.class.isAssignableFrom(exceptionClass)) {
			errorMessage = "Invalid Login Token";
		} else if (LockedException.class.isAssignableFrom(exceptionClass)) {
			errorMessage = getErrorMessageFromLockedException(exception);
		} else {
			errorMessage = getErrorMessage(exception, locale, exceptionClass, request);
		}
		return errorMessage;
	}


	private String getErrorMessageFromLockedException(Exception exception) {
		String username = null;
		User currentUser = null;
		String msg = null;
		username = (String)SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		//username = (String) ((AuthenticationException) exception).

		currentUser = userService.findUserByUsername(username);
		if (currentUser != null) {
			Map<String, ConfigurationVO> conf = configurationService.getFinalConfigurationForEntity(SystemEntity.getSystemEntityId());
			if (currentUser.getNumberOfFailedLoginAttempts() >= Integer.parseInt(conf.get(FAILED_PSWD_RESET_ATTEMPTS).getPropertyValue())) {
				msg = "Wrong Old Password...As per Company Policy You are blocked because of more failed reset password attempts than allowed .Please contact the Administrator";
			} else {
				msg = "User is blocked .Please contact the Administrator";
			}
		}
		return msg;
	}

	private String getErrorMessage(Exception exception, Locale locale, Class<?> exceptionClass,
								   HttpServletRequest request) {
		String exceptionMessage = exception.getMessage();
		String msg = null;
		if (exceptionMessage == null) {
			BaseLoggers.flowLogger.error("Exception occured while Logging In");
			BaseLoggers.flowLogger.error(ExceptionUtils.getFullStackTrace(exception));
		} else if (("CaptchaValidationFailed").equals(exceptionMessage)) {
			msg = messageSource.getMessage(CAPTCHA_VALIDATION_FAILED, null, locale);
		} else if (exceptionMessage.contains("Maximum sessions of") || exceptionClass.equals(SessionAuthenticationException.class)) {
			msg = messageSource.getMessage(USER_ALREADY_LOGGED, null, locale);
		} else if (request.getParameter("errCode") != null && !("".equals(request.getParameter("errCode").trim()))) {
			msg = messageSource.getMessage(INVALID_CURRENT_SESSION, null, locale);
		} else if (("branchTimeValidationFailed").equals(exceptionMessage)) {
			msg = messageSource.getMessage(BRANCH_TIME_VALIDATION_FAILED, null, locale);
		} else if (("notWorkingDay").equals(exceptionMessage)) {
			msg = messageSource.getMessage(NOT_WORKING_DAY, null, locale);
		} else if (("currentSessionExpired").equals(exceptionMessage)) {
			msg = messageSource.getMessage(INVALID_PAGE_SESSION, null, locale);
		} else {
			msg = messageSource.getMessage(INVALID_CREDENTIALS, null, locale);
		}
		return msg;
	}



}
