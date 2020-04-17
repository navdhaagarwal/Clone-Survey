package com.nucleus.rules.service;

import java.util.List;
import java.util.Map;

import com.nucleus.entity.Entity;
import com.nucleus.rules.model.Parameter;
import com.nucleus.rules.model.Rule;
import com.nucleus.rules.model.RuleAction;
import com.nucleus.rules.model.RuleSet;
import com.nucleus.service.BaseService;

/**
 * 
 * @author Nucleus Software Exports Limited
 */

public interface ExpressionEvaluator extends BaseService {

    /**
     * 
     * Evaluate the expression
     * @param ruleAction
     * @param map
     */
    public void executeRuleAction(RuleAction ruleAction, Map<Object, Object> map);

    /**
     * 
     * Evaluate Rule Set
     * @param ruleSet
     * @param map
     * @return
     */

    public char[] evaluateRuleSet(RuleSet ruleSet, Map<Object, Object> map);

    /**
     * Evaluate the rule instance
     * 
     * @param rule
     *            the rule instance
     * @param map
     *            map of context objects
     * @return
     */
    public char evaluateRule(Rule rule, Map<Object, Object> map);

    /**
     * 
     * Evaluate the null safe expression
     * @param rule
     * @param map
     * @return
     */
    public char evaluateRule(Rule rule, Map<Object, Object> map, boolean isStrictMode);

    /**
     * Evaluate the rule instance
     * 
     * @param rule
     *            the rule instance
     * @param entities
     *            list of context entities
     * @return
     */
    public char evaluateRule(Rule rule, List<Entity> entities);

    /**
     * 
     * Evaluate the null safe expression
     * @param rule
     * @param entities
     * @return
     */
    public char evaluateRule(Rule rule, List<Entity> entities, boolean isStrictMode);

    /**
     * 
     * @param parameter
     * @param map
     * @param isStrictMode
     * @param contextMap
     * @return
     */
    public Object executeParameter(Parameter parameter, Map<Object, Object> contextMap, boolean isStrictMode);

    /**
     * 
     * Validate if Place holder parameter exists in rule
     * @param ruleExpression
     * @return
     */
    public boolean placeHolderParamInRule(String ruleExpression);

    /**
     * 
     * Validate if Place holder parameter exists in condition
     * @param conditionExpression
     * @return
     */

    public boolean placeHolderParamInCondition(String conditionExpression);

    /**
     * Gets the rule expression with values.
     *
     * @param ruleExpression the rule expression
     * @param contextMap the context map
     * @return the rule expression with values
     */
    public String getRuleExpressionWithValues(String ruleExpression, Map contextMap, Map<String, String> ognlNameMapping);

    /**
     *Generates the ognl-ParameterName map for compound parameter
     */
    public void getOgnlParamMapForParameter(Parameter parameter, Map<String, String> ognlParamMap,
            Map<Object, Object> contextMap);

    public Map getRuleExpressionKeyElementsForSimulation(String ruleExpression, Map contextMap, Map<String, String> ognlNameMapping);
}
