package com.nucleus.OTP.rest;

import com.nucleus.OTP.OTPGenerationStatus;
import com.nucleus.OTP.VerificationStatusVO;
import com.nucleus.logging.BaseLoggers;
import com.nucleus.otp.OTPSendStatus;
import com.nucleus.otp.OTPSenderService;
import com.nucleus.otp.VerificationStatus;
import com.nucleus.security.oauth.dao.CustomOauthTokenStoreDAO;
import com.nucleus.user.UserService;
import com.nucleus.web.security.NeutrinoSecurityUtility;
import com.nucleus.web.security.SSOAuthenticationSuccessEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.web.authentication.session.SessionAuthenticationException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import javax.inject.Named;

@Controller
@RequestMapping("/client-credential-auth/reset-password/")
public class OTPRestServiceController
{
	/** The user service. */
    @Inject
    @Named("userService")
    private UserService                    userService;

    @Named("otpSenderService")
    @Inject
    private OTPSenderService otpSenderService;
    //Conditional bean that might be null if API portal is enabled.
  	@Autowired(required = false)
  	private TokenStore tokenStore;
  	@Inject
	@Named("neutrinoSecurityUtility")
	private NeutrinoSecurityUtility neutrinoSecurityUtility;


  	
    @ResponseBody
    @RequestMapping(value={"/generateOTP"}, method={org.springframework.web.bind.annotation.RequestMethod.POST})
	public OTPGenerationStatus generateOTP(@RequestAttribute("username") String username, @RequestAttribute(required = false, name = "isOTPForLogin") Boolean isOTPForLogin)
    {
		return generateOTPService(username,isOTPForLogin);
    }

    @ResponseBody
    @RequestMapping(value={"/verifyOTP"}, method={org.springframework.web.bind.annotation.RequestMethod.POST})
	public ResponseEntity<VerificationStatusVO> verifyOTP(@RequestAttribute("username") String username, @RequestAttribute("otp") String otp, @RequestAttribute(required = false, name = "isOTPForLogin") Boolean isOTPForLogin, @RequestAttribute(required = false, name = "isAlreadyLoggedIn") Boolean isAlreadyLoggedIn) {
		return verifyOTPService(username, otp, isOTPForLogin, isAlreadyLoggedIn);
	}

    private void handleConcurrentUserSessions(Boolean isAlreadyLoggedIn, String userName) {
		int oauthUsers = 0;
		if (tokenStore != null) {
			oauthUsers = ((CustomOauthTokenStoreDAO) tokenStore).findActiveTokensCountByUserName(userName);
		}
		if (isAlreadyLoggedIn || oauthUsers >= 1) {
			// If request has header with name and value 'TGT', it means a TGT
			// already exists for this username. In such case, we
			// throw below exception
			throw new SessionAuthenticationException("ConcurrentSessionControlStrategy.exceededAllowed");
		}
		
		UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userService.getUserFromUsername(userName),null);
		SSOAuthenticationSuccessEvent ssoAuthenticationSuccessEvent = new SSOAuthenticationSuccessEvent(authentication);
		neutrinoSecurityUtility.createAuthenticationSuccessEventEntry(ssoAuthenticationSuccessEvent, null);
	}

	OTPGenerationStatus generateOTPService(String username, Boolean isOTPForLogin){
		if(isOTPForLogin==null)
		{
			isOTPForLogin=Boolean.FALSE;
		}
		try
		{
			OTPGenerationStatus otpGenerationStatus = new OTPGenerationStatus();
			OTPSendStatus otpSendStatus ;
			if(isOTPForLogin)
			{
				otpSendStatus =this.otpSenderService.sendOtpForLogin(username);
			}
			else
			{
				otpSendStatus =this.otpSenderService.sendOtp(username);
			}
			otpGenerationStatus.setMaskedEmail(otpSendStatus.getMaskedEmail());
			otpGenerationStatus.setMaskedMobile(otpSendStatus.getMaskedMobile());
			otpGenerationStatus.setStatus(otpSendStatus.getStatus());
			otpGenerationStatus.setErrorDescription(otpSendStatus.getErrorDescription());
			otpGenerationStatus.setOtpType(this.otpSenderService.getOtpType());
			return otpGenerationStatus;
		} catch (Exception e) {
			BaseLoggers.exceptionLogger.error("Error in /generateOTP.", e);
			OTPGenerationStatus otpGenerationStatus = new OTPGenerationStatus();
			otpGenerationStatus.setStatus(OTPSendStatus.STATUS.ERROR);
			return otpGenerationStatus; }
	}

	ResponseEntity<VerificationStatusVO> verifyOTPService(String username, String otp, Boolean isOTPForLogin, Boolean isAlreadyLoggedIn) {
		if(isOTPForLogin==null)
		{
			isOTPForLogin=Boolean.FALSE;
		}
		if(isAlreadyLoggedIn==null)
		{
			isAlreadyLoggedIn=Boolean.FALSE;
		}

		try {
			VerificationStatusVO verificationStatusVO = new VerificationStatusVO();

			VerificationStatus verificationStatus = this.otpSenderService
					.verifyOTPandgeneratePasswordResetToken(username, otp, isOTPForLogin);
			if (verificationStatus.getStatus().equals(VerificationStatus.STATUS.VALID) && isOTPForLogin) {

				handleConcurrentUserSessions(isAlreadyLoggedIn, username);

			}
			verificationStatusVO.setStatus(verificationStatus.getStatus());
			verificationStatusVO.setErrorDescription(verificationStatus.getErrorDescription());
			verificationStatusVO.setPasswordResetToken(verificationStatus.getPasswordResetToken());
			return new ResponseEntity<>(verificationStatusVO, HttpStatus.OK);
		} catch (SessionAuthenticationException e) {
			HttpHeaders header = new HttpHeaders();
			header.add("exception", "CONCURRENCY_ERROR");
			return new ResponseEntity<>(null, header, HttpStatus.OK);
		}

		catch (Exception e) {
			VerificationStatusVO verificationStatusVO = new VerificationStatusVO();
			BaseLoggers.exceptionLogger.error("Error in /verifyOTP.", e);
			verificationStatusVO.setStatus(VerificationStatus.STATUS.ERROR);
			return new ResponseEntity<>(verificationStatusVO, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
}