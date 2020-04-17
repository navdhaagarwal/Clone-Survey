package com.nucleus.authenticationToken;

import javax.persistence.Entity;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

@Entity
@DynamicUpdate
@DynamicInsert
public class OTPToken extends AuthenticationToken {

    private static final long serialVersionUID = 7905753223563991879L;

    private Long              userId;

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

}
