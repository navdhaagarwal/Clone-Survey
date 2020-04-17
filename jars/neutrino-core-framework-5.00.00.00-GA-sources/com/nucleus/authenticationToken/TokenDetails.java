package com.nucleus.authenticationToken;

public class TokenDetails {

    String status;
    String taskId;
    Long   userId;
    String emailId;
    Long   emailUId;

    public Long getEmailUId() {
        return emailUId;
    }

    public void setEmailUId(Long emailUId) {
        this.emailUId = emailUId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getEmailId() {
        return emailId;
    }

    public void setEmailId(String emailId) {
        this.emailId = emailId;
    }

}
