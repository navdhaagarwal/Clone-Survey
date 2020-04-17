package com.nucleus.rules.service;

import com.nucleus.rules.model.RuleGroupExpression;

public interface RuleExpressionBuilder {

    /**
     * 
     * Build Rule level Expression -- convert rule id expression into rulename_id expression
     * @param ruleGroupExpression
     * @return
     */
    public String buildRuleLevelRuleExpression(String ruleGroupExpression);

    /**
     * 
     * Parse Rule Group Expression
     * @param exp
     * @return
     */
    public RuleGroupExpression parseRuleGroupFlatExpression(String exp);
}
