package com.nucleus.OTP;

import static com.nucleus.otp.OTPSendStatus.STATUS.SENT_ON_BOTH;
import static com.nucleus.otp.OTPSendStatus.STATUS.SENT_ON_MOBILE;

import java.io.Serializable;

import com.nucleus.otp.OTPSendStatus;

/**
 * Created by gajendra.jatav on 4/22/2019.
 */
public class OTPGenerationStatus implements Serializable{

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private String status;
	
	private String errorDescription;

    private String maskedMobile;

    private String maskedEmail;

    private String otpType;

    public String getStatus() {
        return status;
    }

    public void setStatus(OTPSendStatus.STATUS status) {
        this.status = status.name();
    }

    public String getMaskedMobile() {
        return maskedMobile;
    }

    public void setMaskedMobile(String maskedMobile) {
        this.maskedMobile = maskedMobile;
    }

    public String getMaskedEmail() {
        return maskedEmail;
    }

    public void setMaskedEmail(String maskedEmail) {
        this.maskedEmail = maskedEmail;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getOtpType() {
        return otpType;
    }

    public void setOtpType(String otpType) {
        this.otpType = otpType;
    }

	public String getErrorDescription() {
		return errorDescription;
	}

	public void setErrorDescription(String errorDescription) {
		this.errorDescription = errorDescription;
	}
}
