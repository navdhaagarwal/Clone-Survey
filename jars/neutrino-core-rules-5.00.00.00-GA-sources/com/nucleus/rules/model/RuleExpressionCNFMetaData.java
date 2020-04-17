package com.nucleus.rules.model;

import java.io.*;
import java.util.*;

public class RuleExpressionCNFMetaData implements Serializable {

    private long ruleId;
    private String ruleCode;
    private long numberOfGroups;
    private long numberOfDistinctVariables;
    private Set<String> groupVariableSet;
    private String ruleExpression;
    private String cnfForm;

    public long getRuleId() {
        return ruleId;
    }

    public void setRuleId(long ruleId) {
        this.ruleId = ruleId;
    }

    public String getRuleCode() {
        return ruleCode;
    }

    public void setRuleCode(String ruleCode) {
        this.ruleCode = ruleCode;
    }

    public long getNumberOfGroups() {
        return numberOfGroups;
    }

    public void setNumberOfGroups(long numberOfGroups) {
        this.numberOfGroups = numberOfGroups;
    }

    public long getNumberOfDistinctVariables() {
        return numberOfDistinctVariables;
    }

    public void setNumberOfDistinctVariables(long numberOfDistinctVariables) {
        this.numberOfDistinctVariables = numberOfDistinctVariables;
    }

    public Set<String> getGroupVariableSet() {
        return groupVariableSet;
    }

    public void setGroupVariableSet(Set<String> groupVariableSet) {
        this.groupVariableSet = groupVariableSet;
    }

    public String getRuleExpression() {
        return ruleExpression;
    }

    public void setRuleExpression(String ruleExpression) {
        this.ruleExpression = ruleExpression;
    }

    public String getCnfForm() {
        return cnfForm;
    }

    public void setCnfForm(String cnfForm) {
        this.cnfForm = cnfForm;
    }

}
