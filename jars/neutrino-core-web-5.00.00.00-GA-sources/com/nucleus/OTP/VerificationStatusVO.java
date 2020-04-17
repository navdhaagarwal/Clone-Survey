package com.nucleus.OTP;

import com.nucleus.otp.VerificationStatus;

import java.io.Serializable;

/**
 * Created by gajendra.jatav on 4/22/2019.
 */
public class VerificationStatusVO implements Serializable{

    private String status;

    private String passwordResetToken;
    
    private String errorDescription;

    public String getStatus() {
        return status;
    }

    public void setStatus(VerificationStatus.STATUS status) {
        this.status = status.name();
    }

    public String getPasswordResetToken() {
        return passwordResetToken;
    }

    public void setPasswordResetToken(String passwordResetToken) {
        this.passwordResetToken = passwordResetToken;
    }

	public String getErrorDescription() {
		return errorDescription;
	}

	public void setErrorDescription(String errorDescription) {
		this.errorDescription = errorDescription;
	}
}
