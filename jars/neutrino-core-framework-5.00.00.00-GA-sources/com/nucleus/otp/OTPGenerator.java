package com.nucleus.otp;

/**
 * Created by gajendra.jatav on 4/17/2019.
 */
public interface OTPGenerator {

    public String generateOTP(OTPPolicy otpPolicy);

    public String generateOTP();
}
