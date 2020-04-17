package com.nucleus.otp;

/**
 * Created by gajendra.jatav on 4/22/2019.
 */
public class OTPSendStatus {

    public static enum STATUS {
        SENT_ON_EMAIL,SENT_ON_MOBILE, SENT_ON_BOTH, MOBILE_NOT_FOUND, ERROR_IN_SENDING_SMS, USER_NOT_EXISTS, ERROR,ERROR_LDAP_USER,MAX_SENDING_LIMIT_EXHAUSTED
    }

    private STATUS status;
    
    private String errorDescription;

    private String maskedMobile;

    private String maskedEmail;

    public STATUS getStatus() {
        return status;
    }

    public void setStatus(STATUS status) {
        this.status = status;
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

	public String getErrorDescription() {
		return errorDescription;
	}

	public void setErrorDescription(String errorDescription) {
		this.errorDescription = errorDescription;
	}
}
