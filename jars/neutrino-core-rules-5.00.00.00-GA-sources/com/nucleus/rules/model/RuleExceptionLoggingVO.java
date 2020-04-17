package com.nucleus.rules.model;

import java.util.Map;

public class RuleExceptionLoggingVO {
    private Rule rule;
    private Parameter parameter;
    private Map<Object, Object> contextMap;
    private Exception e;
    private String exceptionOwner;

    public Rule getRule() {
        return rule;
    }

    public void setRule(Rule rule) {
        this.rule = rule;
    }

    public Map<Object, Object> getContextMap() {
        return contextMap;
    }

    public void setContextMap(Map<Object, Object> contextMap) {
        this.contextMap = contextMap;
    }

    public Exception getE() {
        return e;
    }

    public void setE(Exception e) {
        this.e = e;
    }

    public String getExceptionOwner() {
        return exceptionOwner;
    }

    public void setExceptionOwner(String exceptionOwner) {
        this.exceptionOwner = exceptionOwner;
    }

    public Parameter getParameter() {
        return parameter;
    }

    public void setParameter(Parameter parameter) {
        this.parameter = parameter;
    }
}
