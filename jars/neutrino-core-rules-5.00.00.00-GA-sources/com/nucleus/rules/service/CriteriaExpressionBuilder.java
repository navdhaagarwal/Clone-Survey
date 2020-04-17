package com.nucleus.rules.service;

import java.util.Map;

import com.nucleus.rules.model.RuleGroupExpression;
import com.nucleus.service.BaseService;

/**
 * 
 * @author Nucleus Software Exports Limited
 * TO build the expression for Rules Criteria
 */
public interface CriteriaExpressionBuilder extends BaseService {

    /**
     * 
     * Method to build and execute rule criteria query  
     * @param ruleGroupExpression
     * @param map
     * @return
     */
    public String buildCriteriaRuleQuery(RuleGroupExpression ruleGroupExpression, Map<Object, Object> map,
            Map<String, String> joinsMap);
}
