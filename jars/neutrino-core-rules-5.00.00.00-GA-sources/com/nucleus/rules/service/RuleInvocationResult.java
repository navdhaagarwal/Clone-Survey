package com.nucleus.rules.service;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import com.nucleus.entity.Entity;
import com.nucleus.rules.model.RuleInvocationPoint;

public class RuleInvocationResult implements Serializable {

    private static final long         serialVersionUID = -1426131360281541421L;

    private final Map<String, Object> resultMap;

    public RuleInvocationResult(Map<String, Object> map) {
        this.resultMap = map;
    }

    public String getTransactionId() {
        return (String) resultMap.get(RuleInvocationPoint.RULE_INVOCATION_RESULT_TRANSACTION_ID);
    }

    public boolean getRuleGroupResult() {
        if (resultMap.containsKey(RuleInvocationPoint.RULE_INVOCATION_RESULT_VALUE)) {
            return (Boolean) resultMap.get(RuleInvocationPoint.RULE_INVOCATION_RESULT_VALUE);
        }
        return false;
    }

    public List<? extends Entity> getRuleCriteriaResult() {
        List<? extends Entity> recordsList = null;
        Map<String, Object> ruleGroupResults = (Map<String, Object>) resultMap
                .get(RuleInvocationPoint.RULE_INVOCATION_CRITERIA_RULE_RESULT_VALUE);
        if (null != ruleGroupResults && ruleGroupResults.size() > 0) {
            for (Map.Entry<String, Object> entry : ruleGroupResults.entrySet()) {
                recordsList = (List<? extends Entity>) entry.getValue();
            }
        }
        return recordsList;
    }

    public Map<Object, Object> getRuleExecutionMap() {
        return (Map<Object, Object>) resultMap.get(RuleConstants.ALL_RULES_RESULT);
    }

    public Boolean getRuleGroupResultIfRuleGroupExist() {
        if (resultMap.containsKey(RuleInvocationPoint.RULE_INVOCATION_RESULT_VALUE)) {
            return (Boolean) resultMap.get(RuleInvocationPoint.RULE_INVOCATION_RESULT_VALUE);
        }
        return null;
    }
}