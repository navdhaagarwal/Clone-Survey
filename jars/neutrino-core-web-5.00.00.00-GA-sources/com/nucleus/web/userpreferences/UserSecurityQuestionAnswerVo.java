package com.nucleus.web.userpreferences;

import java.util.List;

import com.nucleus.user.UserSecurityQuestionAnswer;

public class UserSecurityQuestionAnswerVo {

    List<UserSecurityQuestionAnswer> securityQuestionAnswerList;

    public List<UserSecurityQuestionAnswer> getSecurityQuestionAnswerList() {
        return securityQuestionAnswerList;
    }

    public void setSecurityQuestionAnswerList(List<UserSecurityQuestionAnswer> securityQuestionAnswerList) {
        this.securityQuestionAnswerList = securityQuestionAnswerList;
    }

    private String questionCode;
    private String answer;

    public String getQuestionCode() {
        return questionCode;
    }

    public void setQuestionCode(String questionCode) {
        this.questionCode = questionCode;
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }
}
