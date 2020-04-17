package com.nucleus.rules.service;

import com.nucleus.service.BaseService;

/**
 * 
 * @author Nucleus Software Exports Limited
 * Expression Builder
 */

public interface ExpressionBuilder extends BaseService {

    /**
     * 
     * Method to build the Compound Parameter Expression
     * assuming that it contains only Arithmetic Operators, brackets and parameter id's
     * @param parameterExpression
     * @return
     */
    public String buildParameterExpression(String parameterExpression);

    /**
     * Build Conditions Expression tree
     * 
     * @param conditionExpression
     * @param map
     * @return
     */
    public String buildConditionExpression(String conditionExpression);

    /**
     * Build the rules Expression tree  at condition level
     * 
     * @param rule
     * @param map
     * @return
     */
    public String buildConditionLevelRuleExpression(String ruleExpression);
}
