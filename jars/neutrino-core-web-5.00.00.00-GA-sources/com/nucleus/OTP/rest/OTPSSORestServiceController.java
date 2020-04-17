package com.nucleus.OTP.rest;

import com.nucleus.OTP.OTPGenerationStatus;
import com.nucleus.OTP.VerificationStatusVO;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller("oTPSSORestServiceController")
@RequestMapping("/sso/reset-password/")
public class OTPSSORestServiceController extends OTPRestServiceController {

    @ResponseBody
    @RequestMapping(value = {"/generateOTP"}, method = {org.springframework.web.bind.annotation.RequestMethod.POST})
    public OTPGenerationStatus generateOTP(@RequestParam("userName") String userName, @RequestParam(required = false, name = "isOTPForLogin") Boolean isOTPForLogin) {
        return generateOTPService(userName, isOTPForLogin);

    }

    @ResponseBody
    @RequestMapping(value = {"/verifyOTP"}, method = {org.springframework.web.bind.annotation.RequestMethod.POST})
    public ResponseEntity<VerificationStatusVO> verifyOTP(@RequestParam("userName") String userName, @RequestParam("otp") String otp, @RequestParam(required = false, name = "isOTPForLogin") Boolean isOTPForLogin, @RequestParam(required = false, name = "isAlreadyLoggedIn") Boolean isAlreadyLoggedIn) {
        return verifyOTPService(userName, otp, isOTPForLogin, isAlreadyLoggedIn);
    }
}