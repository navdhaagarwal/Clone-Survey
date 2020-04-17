package com.nucleus.otp;

import java.io.Serializable;

/**
 * Created by gajendra.jatav on 4/24/2019.
 */
public class VerificationStatus implements Serializable{

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public static enum STATUS {VALID, INVALID, ERROR,MAX_TIME_INVALID,OTP_EXPIRED};

    private STATUS status;
    
    private String errorDescription;

    private String passwordResetToken;

    public STATUS getStatus() {
        return status;
    }

    public void setStatus(STATUS status) {
        this.status = status;
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
