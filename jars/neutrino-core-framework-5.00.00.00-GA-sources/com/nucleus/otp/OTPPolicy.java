package com.nucleus.otp;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Value;

import javax.inject.Named;

/**
 * Created by gajendra.jatav on 4/17/2019.
 */
public class OTPPolicy {

    private Integer otpLength;

    private OTPType otpType;

    private static final Integer DEFAULT_LENGTH = 6;

    public Integer getOtpLength() {
        return otpLength;
    }

    public void setOtpLength(String otpLength) {
        if (StringUtils.isEmpty(otpLength)) {
            this.otpLength = DEFAULT_LENGTH;
        }
        this.otpLength = Integer.valueOf(otpLength);;
    }

    public OTPType getOtpType() {
        return otpType;
    }

    public void setOtpType(String otpType) {
        if (StringUtils.isEmpty(otpType)) {
            this.otpType = OTPType.NUMERIC;
            return;
        }
        if(otpType.equalsIgnoreCase("numeric")){
            this.otpType = OTPType.NUMERIC;
        }else if(otpType.equalsIgnoreCase("character")){
            this.otpType = OTPType.CHARACTER;
        }else {
            this.otpType = OTPType.UUID_BASED;
        }
    }

    public static Builder builder(){
        return new Builder();
    }

    public static class Builder{

        private Integer otpLength;

        private String otpType;

        public Builder(){
            this.otpLength = DEFAULT_LENGTH;
        }

        public Builder setOtpLength(Integer otpLength) {
            this.otpLength = otpLength;
            return this;
        }

        public Builder setOtpType(String otpType) {
            this.otpType=otpType;
            return this;
        }

        public OTPPolicy build()
        {
            OTPPolicy otpPolicy = new OTPPolicy();
            otpPolicy.setOtpType(this.otpType);
            otpPolicy.setOtpLength(this.otpLength.toString());
            return otpPolicy;
        }

    }
}
