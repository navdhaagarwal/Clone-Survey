package com.nucleus.web.password;

import static com.nucleus.logging.BaseLoggers.exceptionLogger;

import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.nucleus.core.exceptions.InvalidDataException;
import com.nucleus.core.exceptions.SystemException;
import com.nucleus.password.reset.ResetPasswordService;
import com.nucleus.password.reset.UserFirstTimeLoginDetails;
import com.nucleus.security.oauth.domainobject.OauthClientDetails;
import com.nucleus.web.security.AesUtil;

/**
 * @author shivendra.kumar
 *
 */
@RestController
@RequestMapping(value = "/restservice")
public class ResetPasswordRestController {

	private static final String USER_NOT_AUTHENTICATED = "user_not_authenticated.";

	@Inject
	@Named("resetPasswordService")
	private ResetPasswordService resetPasswordService;


	@Value(value = "#{'${core.web.config.webClientToEncryptpwd}'}")
	private String webClientToEncryptpwd;


	@RequestMapping(value = "/resetPassword", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<String> resetPassword(@RequestBody UserFirstTimeLoginDetails userFirstTimeLoginDetails, HttpServletRequest httpServletRequest) {
		String accessToken = (String) httpServletRequest.getAttribute("access_token");
		return setPasswordOnlogin(userFirstTimeLoginDetails, accessToken, (String) httpServletRequest.getAttribute("passPhrase"));

	}

	/**Decrypts the user passwords with pass phrase and calls resetPassowrd method
	 * @param username
	 * @param userFirstTimeLoginDetails
	 * @param newPassword
	 * @param oldPassword
	 * @param token
	 * @return
	 */
	private ResponseEntity<String> setPasswordOnlogin(UserFirstTimeLoginDetails userFirstTimeLoginDetails,String token,String passPhrase) {

		String result = USER_NOT_AUTHENTICATED;
		String passwordPassPhrase= passPhrase;
		try {
		
			if(passwordPassPhrase==null) {
				passwordPassPhrase = AesUtil.Decrypt(resetPasswordService.getPassPhrase(token), OauthClientDetails.SHARED_OAUTH_ENCYPTION_PASS_PHRASE);
			}
			
			Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
			if (authentication.isAuthenticated()) {

				String decryptedOldPassword = AesUtil.decrypt(userFirstTimeLoginDetails.getUserLoginCredentials().getOldPassword(), passwordPassPhrase, "Y".equalsIgnoreCase(webClientToEncryptpwd));
				String decryptedNewPassword = AesUtil.decrypt(userFirstTimeLoginDetails.getUserLoginCredentials().getNewPassword(), passwordPassPhrase, "Y".equalsIgnoreCase(webClientToEncryptpwd));
			
				result = resetPasswordService.resetPasswordOnLogin(userFirstTimeLoginDetails, decryptedOldPassword,
						decryptedNewPassword, token);
			}

		} catch (Exception e) {

			if (BadCredentialsException.class.isAssignableFrom(e.getClass())) {
				result = "badCredentials";
			} else if (LockedException.class.isAssignableFrom(e.getClass())) {
				result = "accountLocked";
			} else if (SystemException.class.isAssignableFrom(e.getClass())) {
				result = e.getMessage();
			} else if (InvalidDataException.class.isAssignableFrom(e.getClass())) {
				result = "InvalidQuestionId";
			} else {
				result = "failure";
			}

			exceptionLogger.error("Error while resetting password on first time login", e);
		}
		
		
		return new ResponseEntity<>(result, HttpStatus.OK);

	}

}
