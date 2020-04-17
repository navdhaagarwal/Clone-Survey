package com.nucleus.otp;


import javax.inject.Named;
import java.security.SecureRandom;
import java.util.UUID;

/**
 * Created by gajendra.jatav on 4/17/2019.
 */
@Named("otpGenerator")
public class OTPGeneratorImpl implements OTPGenerator{

    private static final String characterValues = "6789ABCDEFGHIJKLMNO012345PQRSTUVWXYZ0123456789";

    private static final String numbers = "0123456789";

    private OTPPolicy defaultPolicy = OTPPolicy.builder().setOtpType("uuid_based").build();

    private SecureRandom randomMethod = new SecureRandom();

    @Override
    public String generateOTP(OTPPolicy otpPolicy) {
        if(otpPolicy == null){
            otpPolicy = defaultPolicy;
        }
        if(otpPolicy.getOtpType() == OTPType.NUMERIC){
            char[] otp = new char[otpPolicy.getOtpLength()];
            for (int i = 0; i < otpPolicy.getOtpLength(); i++)
            {
                otp[i] =
                        numbers.charAt(randomMethod.nextInt(numbers.length()));
            }
            return String.valueOf(otp);
        }else if(otpPolicy.getOtpType() == OTPType.CHARACTER){
            char[] otp = new char[otpPolicy.getOtpLength()];
            for (int i = 0; i < otpPolicy.getOtpLength(); i++)
            {
                otp[i] =
                        characterValues.charAt(randomMethod.nextInt(characterValues.length()));
            }
            return String.valueOf(otp);
        }else{
            return UUID.randomUUID().toString();
        }
    }

    @Override
    public String generateOTP() {
        return generateOTP(defaultPolicy);
    }
}
