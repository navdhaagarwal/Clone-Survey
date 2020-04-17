package com.nucleus.rules.simulation.service;

import java.io.Serializable;

public class AssignmentMatrixRowDataElementsPojo implements Serializable {

    private static final long   serialVersionUID = 8138092206966107876L;

    private String              tokenKey;

    private String              expectedValue;

    private String              actualValue;

    private String              operation;

    private String              result;



    public String getTokenKey() {
        return tokenKey;
    }

    public void setTokenKey(String tokenKey) {
        this.tokenKey = tokenKey;
    }

    public String getExpectedValue() {
        return expectedValue;
    }

    public void setExpectedValue(String expectedValue) {
        this.expectedValue = expectedValue;
    }

    public String getActualValue() {
        return actualValue;
    }

    public void setActualValue(String actualValue) {
        this.actualValue = actualValue;
    }

    public String getOperation() {
        return operation;
    }

    public void setOperation(String operation) {
        this.operation = operation;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }


}
