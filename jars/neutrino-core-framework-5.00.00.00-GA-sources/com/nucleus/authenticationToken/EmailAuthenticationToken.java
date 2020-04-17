package com.nucleus.authenticationToken;

import javax.persistence.Entity;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

@Entity
@DynamicUpdate
@DynamicInsert
public class EmailAuthenticationToken extends AuthenticationToken {

    private static final long serialVersionUID = 9120871535331413234L;

    private String            emailId;

    private Long              userId;
    private String            taskId;
    private Long              emailUId;

    public Long getEmailUId() {
        return emailUId;
    }

    public void setEmailUId(Long emailUId) {
        this.emailUId = emailUId;
    }

    public String getEmailId() {
        return emailId;
    }

    public void setEmailId(String emailId) {
        this.emailId = emailId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }
}
