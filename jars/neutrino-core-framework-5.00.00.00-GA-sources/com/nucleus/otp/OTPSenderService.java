package com.nucleus.otp;

/**
 * Created by gajendra.jatav on 4/17/2019.
 */
public interface OTPSenderService {

    String getOtpType();

    public OTPSendStatus sendOtp(String userName);

    public VerificationStatus verifyOTPandgeneratePasswordResetToken(String userName, String otp, Boolean isOTPForLogin);

    public OTPSendStatus sendOtpForLogin(String userName);

}
