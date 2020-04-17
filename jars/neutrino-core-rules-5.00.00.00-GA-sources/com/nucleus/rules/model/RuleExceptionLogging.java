package com.nucleus.rules.model;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;

@Entity
@DynamicUpdate
@DynamicInsert
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
public class RuleExceptionLogging extends RuleEngineExceptionLogging {
    private String                             ruleInvocationUUID;

    public String getRuleInvocationUUID() {
        return ruleInvocationUUID;
    }

    public void setRuleInvocationUUID(String ruleInvocationUUID) {
        this.ruleInvocationUUID = ruleInvocationUUID;
    }
}
