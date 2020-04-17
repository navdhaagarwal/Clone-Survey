package com.nucleus.rules.service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

import com.nucleus.logging.BaseLoggers;
import com.nucleus.rules.model.Rule;

/**
 * 
 * MultiThreaded Execution for Rule Set
 * Worker class to evaluate rule
 */

public class RuleSetCallWorker implements Callable<Map<String, Object>> {

    private CompiledExpressionBuilder compiledExpressionService;

    private Rule                      rule;
    private int                       index;

    private Map<Object, Object>       relationMap;

    /**
     * 
     * Parameters passed to constructor
     * @param index
     * @param rule
     * @param map
     * @param ruleService
     */
    public RuleSetCallWorker(int index, Rule rule, Map<Object, Object> map,
            CompiledExpressionBuilder compiledExpressionService) {
        this.index = index;
        this.rule = rule;
        this.compiledExpressionService = compiledExpressionService;
        this.relationMap = map;
    }

    public RuleSetCallWorker() {

    }

    /**
     * call method overriden
     */

    @Override
    public Map<String, Object> call() {
        Map<String, Object> resultMap = new HashMap<String, Object>();
        resultMap.put("INDEX", index);
        try {

            resultMap.put("RESULT", compiledExpressionService.evaluateRule(rule.getId(), relationMap));
        } catch (Exception e) {
            BaseLoggers.exceptionLogger.error("Exception Occured" , e);
            resultMap.put("RESULT", RuleConstants.RULE_RESULT_NORESULT);
        }
        return resultMap;
    }
}
