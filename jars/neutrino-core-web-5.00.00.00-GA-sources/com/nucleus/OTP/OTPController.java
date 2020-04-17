package com.nucleus.OTP;

import java.io.IOException;

import javax.inject.Inject;
import javax.inject.Named;
import javax.mail.MessagingException;
import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.nucleus.authenticationToken.AuthenticationTokenService;
import com.nucleus.cfi.mail.service.MailMessageIntegrationService;
import com.nucleus.cfi.sms.service.ShortMessageIntegrationService;
import com.nucleus.core.exceptions.InvalidDataException;
import com.nucleus.logging.BaseLoggers;
import com.nucleus.mail.MailService;
import com.nucleus.mail.MimeMailMessageBuilder;
import com.nucleus.token.CustomLoginTokenHandlingService;
import com.nucleus.user.User;
import com.nucleus.user.UserService;
import com.nucleus.web.common.controller.BaseController;

@Controller
@Transactional
@RequestMapping("/OTP")
public class OTPController extends BaseController {

    @Inject
    @Named("authenticationTokenService")
    protected AuthenticationTokenService  authenticationTokenService;

    @Inject
    @Named("userService")
    private UserService                   userService;

    @Named("shortMessageIntegrationService")
    ShortMessageIntegrationService        shortMessageIntegrationService;

    @Inject
    @Named("customLoginTokenHandlingService")
    CustomLoginTokenHandlingService       customLoginTokenHandlingService;

    @Inject
    @Named("mailService")
    private MailService                   mailService;

    @Inject
    @Named("mailMessageIntegrationService")
    private MailMessageIntegrationService mailMessageIntegrationService;

    @Value("${core.web.config.email.validation.token.validity.time.millis}")
    private String                        tokenValidityTimeInMillis;

	@RequestMapping(value = "/sendOTPtoRegdMobile", method = RequestMethod.POST)
	public @ResponseBody String sendOTPtoRegdMobile(@RequestParam(value = "token", required = true) String token,
			ModelMap map, HttpServletRequest request) {

		String response = "Success";
		User user;
		try {
			user = customLoginTokenHandlingService.getUserAssociatedWithLoginToken(request);
		} catch (InvalidDataException e) {

			BaseLoggers.exceptionLogger.error("Exception in OTPController:", e.getMessage());
			response = e.getMessage();

			return response;
		}
		/*
		 * Below code will be removed by below commented code when user's mobile numbers
		 * starts getting saved
		 */
		// Email sending code starts here
		if (user != null) {
			String mailId = user.getMailId();
			if (mailId != null) {
				String otp = authenticationTokenService.getRandomOTP(user.getId(), 6, tokenValidityTimeInMillis);
				String emailBody = "Dear " + user.getUsername() + ", your OTP(One Time Password) is:" + otp
						+ ". Please do not share this to anyone.";
				MimeMailMessageBuilder mimeMailMessageBuilder = mailService.createMimeMailBuilder();

				mimeMailMessageBuilder.setFrom("neutrino@nucleussoftware.com").setTo(mailId)
						.setSubject("One Time Password").setHtmlBody(emailBody);
				try {
					mailMessageIntegrationService
							.sendMailMessageToIntegrationServer(mimeMailMessageBuilder.getMimeMessage());
				} catch (MessagingException e) {
					BaseLoggers.exceptionLogger.error("Exception in OTPController:", e.getMessage());
					response = e.getMessage();
				} catch (IOException e) {
					BaseLoggers.exceptionLogger.error("Exception in OTPController:", e.getMessage());
					response = e.getMessage();
				}
			} else
				response = "Email ID not found";
		}

		else
			response = "User not found";

		// Email sending code ends here

		// This code will be used when User will have Mobile Number
		/*
		 * if (user != null) { PhoneNumber mobileNumber =
		 * userService.getUserMobileNumber(user.getId()); if (mobileNumber != null &&
		 * mobileNumber.getIsdCode() != null && mobileNumber.getPhoneNumber() != null) {
		 * String otp = authenticationTokenService.getRandomOTP(user.getId(), 6,
		 * emailValidationTokenvalidityInMilliSecond); SmsMessage smsMessage = new
		 * SmsMessage();
		 * 
		 * smsMessage.setTo(mobileNumber.getPhoneNumber());
		 * smsMessage.setBody("Dear csutomer your OTP is: " + otp +
		 * ". Please do not share this with anyone."); smsMessage.setFrom("Neutrino");
		 * shortMessageIntegrationService.sendShortMessage(smsMessage); } }
		 */
		return response;
	}

    /*    @RequestMapping(value = "/processOTP/{otp}")
        public String verifyOTP(@PathVariable("otp") String otp, @RequestParam("token") String token) {
            TokenDetails previoustoken = authenticationTokenService.getOTPTokenDetailsByTokenId(token);

             Check if previous token is valid or not then only go for OTP verification 
            if (previoustoken != null && previoustoken.getStatus() != null) {
                if (previoustoken.getStatus().equals(AuthenticationTokenConstants.INVALID_TOKEN)) {
                    return "INVALID_TOKEN";
                } else if (previoustoken.getStatus().equals(AuthenticationTokenConstants.PAGE_EXPIRED)) {
                    return "PAGE_EXPIRED";
                } else if (previoustoken.getStatus().equals(AuthenticationTokenConstants.VALID_TOKEN)) {
                    TokenDetails tokenDetails = authenticationTokenService.getEmailAuthenticationTokenByTokenId(otp);
                    if (tokenDetails.getStatus().equals(AuthenticationTokenConstants.INVALID_TOKEN)) {
                        return "INVALID_TOKEN";
                    } else if (tokenDetails.getStatus().equals(AuthenticationTokenConstants.PAGE_EXPIRED)) {
                        return "PAGE_EXPIRED";
                    } else if (tokenDetails.getStatus().equals(AuthenticationTokenConstants.VALID_TOKEN)) {
                        Loggin uesr programatically  
                    }
                }
            }
            return "failure";
        }*/
}
