package com.nucleus.otp;

import com.nucleus.authenticationToken.AuthenticationToken;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.Entity;

/**
 * Created by gajendra.jatav on 4/22/2019.
 */
@Entity
@DynamicUpdate
@DynamicInsert
public class PasswordResetOTPToken extends AuthenticationToken{

    private Long              userId;

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }
}
