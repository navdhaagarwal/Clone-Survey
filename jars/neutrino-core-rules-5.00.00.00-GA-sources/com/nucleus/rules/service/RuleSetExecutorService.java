package com.nucleus.rules.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledThreadPoolExecutor;

import javax.inject.Inject;
import javax.inject.Named;

import com.nucleus.logging.BaseLoggers;
import com.nucleus.rules.model.Rule;
import com.nucleus.rules.model.RuleGroup;
import com.nucleus.rules.model.RuleSet;
import com.nucleus.rules.utils.DataContext;
import com.nucleus.service.BaseServiceImpl;

/**
 * 
 * @author Nucleus Software Exports Limited
 * class to execute Rule Set in different threads
 */

@Named(value = "ruleSetExecutorService")
public class RuleSetExecutorService extends BaseServiceImpl {

    @Inject
    @Named(value = "compiledExpressionBuilder")
    private CompiledExpressionBuilder compiledExpressionBuilder;

    @Inject
    @Named(value = "ruleExpressionBuilder")
    private RuleExpressionBuilder     ruleExpressionBuilder;

    private static final int          THREAD_COUNT = 75;

    private static ExecutorService    executor     = new ScheduledThreadPoolExecutor(THREAD_COUNT);

    /**
     * 
     * Method to execute the rules from ruleSet
     * @param ruleSet
     * @param relationMap
     * @return
     */

    public char[] evaluateRuleSet(RuleSet ruleSet, Map<Object, Object> relationMap) {

        if (ruleSet.getRules() == null || ruleSet.getRules().size() <= 0) {
            return null;
        }

        char[] result = new char[ruleSet.getRules().size()];
        List<Rule> rules = ruleSet.getRules();



        try {
            if(relationMap instanceof DataContext){
                int i = 0;
                for (Rule rule : rules) {
                    RuleSetCallWorker ruleSetCallWorker = new RuleSetCallWorker(i, rule, relationMap, compiledExpressionBuilder);
                    Map<String, Object> map = ruleSetCallWorker.call();
                    result[((Integer) map.get("INDEX")).intValue()] = ((Character) map.get("RESULT"));
                    i++;
                }
            } else {
                int i = 0;
                List<Callable<Map<String, Object>>> callables = new ArrayList<Callable<Map<String, Object>>>();

                for (Rule rule : rules) {
                    Callable<Map<String, Object>> callable = new RuleSetCallWorker(i, rule, relationMap, compiledExpressionBuilder);
                    callables.add(callable);
                    i++;
                }
                List<Future<Map<String, Object>>> future = executor.invokeAll(callables);

                if (future != null && future.size() > 0) {
                    for (Future futureResult : future) {
                        Map<String, Object> map = (Map<String, Object>) futureResult.get();
                        result[((Integer) map.get("INDEX")).intValue()] = ((Character) map.get("RESULT"));
                    }
                }
            }
        } catch (Exception e) {
            BaseLoggers.exceptionLogger.error("Exception Occured" + e);
            return null;
        }
        return result;
    }

    public Map<Object, Object> evaluateRuleGroup(RuleGroup ruleGroup, Map<Object, Object> relationMap) {

        char[] ruleGroupResult = evaluateRuleSet(ruleGroup, relationMap);
        relationMap.put(RuleConstants.RULE_GROUP_PATTERN_KEY, ruleGroupResult);

        for (int i = 0 ; i < ruleGroupResult.length ; i++) {
            Rule rule = ruleGroup.getRules().get(i);
            relationMap.put(
                    RulesConverterUtility.replaceSpace(rule.getName()) + RuleConstants.PARAMETER_NAME_ID + rule.getId(),
                    ruleGroupResult[i] == 'P' ? true : false);
        }

        Object resultVal = RuleExpressionMvelEvaluator.evaluateCompiledExpression(ruleGroup.getRuleLevelCompiledExpr(),
                relationMap);
        relationMap.put(RuleConstants.RULE_GROUP_RESULT_KEY, resultVal);

        return relationMap;
    }
}
