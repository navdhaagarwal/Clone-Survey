/**
 * This file and a proportion of its content is copyright of Nucleus Software Exports Limited - � 2012. All rights reserved.
 * Any redistribution or reproduction of part or all of the contents in any form is prohibited other than the following:
 * - you cannot print or download to a local hard disk extract contents either part or full for personal/ commercial/
 * academic or any other use
 * - you may not copy the content to individual/ third parties for any type of use, either as compiled or source format
 * without the knowledge and consent of Nucleus Software
 * - You may not, except with our express written permission, distribute or commercially exploit the content. Nor may you
 * transmit it or store it in any other web site or other form of electronic retrieval system.
 */
package com.nucleus.rules.simulation.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;

import com.nucleus.rules.model.*;
import com.nucleus.rules.utils.DataContext;
import org.mvel2.MVEL;

import com.nucleus.dao.query.RuleQueryExecutor;
import com.nucleus.entity.BaseEntity;
import com.nucleus.logging.BaseLoggers;
import com.nucleus.rules.exception.RuleException;
import com.nucleus.rules.service.BaseRuleServiceImpl;
import com.nucleus.rules.service.ExpressionEvaluator;
import com.nucleus.rules.service.ExpressionValidationConstants;
import com.nucleus.rules.service.RuleCacheService;
import com.nucleus.rules.service.RuleConstants;
import com.nucleus.rules.service.RuleExpressionBuilder;
import com.nucleus.rules.service.RuleExpressionMvelEvaluator;
import com.nucleus.rules.service.RuleInvocationService;
import com.nucleus.rules.service.RuleSentenceBuilderService;
import com.nucleus.rules.service.RuleSetExecutorService;
import com.nucleus.rules.service.RulesConverterUtility;
import com.nucleus.rules.service.SQLRuleExecutor;
import com.nucleus.rules.service.ScriptRuleEvaluator;

/**
 * @author Nucleus Software Exports Limited
 */

public class RuleSimulationServiceImpl extends BaseRuleServiceImpl implements RuleSimulationService {

    @Inject
    @Named("ruleSentenceBuilderService")
    private RuleSentenceBuilderService   ruleSentenceBuilderService;

    @Inject
    @Named("ruleInvocationService")
    private RuleInvocationService        ruleInvocationService;

    @Inject
    @Named(value = "ruleExpressionBuilder")
    private RuleExpressionBuilder        ruleExpressionBuilder;

    @Inject
    @Named("ruleSetExecutorService")
    private RuleSetExecutorService       ruleSetExecutorService;

    @Inject
    @Named("expressionEvaluator")
    private ExpressionEvaluator          expressionEvaluator;

    @Inject
    @Named("defaultRuleSimulationProcess")
    private DefaultRuleSimulationProcess defaultRuleSimulationProcess;
    
    @Inject
	@Named("ruleCacheService")
	private RuleCacheService             ruleCacheService;
    
    @Inject
    @Named("sQLRuleExecutor")
    private SQLRuleExecutor sqlExecutor;
    
    private List<RuleSimulationProcess>  ruleSimulationProcesses;

    public List<RuleSimulationProcess> getRuleSimulationProcesses() {
        return ruleSimulationProcesses;
    }

    public void setRuleSimulationProcesses(List<RuleSimulationProcess> ruleSimulationProcesses) {
        this.ruleSimulationProcesses = ruleSimulationProcesses;
    }

    /**
     * Method to simulate the rules in application
     * for Rule set,rule group and criteria rules
     */
    private SimulationResultVO ruleSimulationAudit(Map<Object, Map<Object, Object>> ruleSimulationAudit,
            Map<Object, Object> Objectmap, String invocationPoint) {
        if (ruleSimulationAudit == null) {
            return null;
        }
        Map<Object, Object> ruleSetResults = ruleSimulationAudit.get(RuleConstants.RULESET_KEY);
        Map<Object, Object> ruleGroupResults = ruleSimulationAudit.get(RuleConstants.RULEGROUP_KEY);
        Map<Object, Object> criteriaRuleResults = ruleSimulationAudit.get(RuleConstants.CRITERIA_RULES_RESULT_KEY);

        SimulationResultVO rulesSimulationAuditVO = new SimulationResultVO();
        rulesSimulationAuditVO.setRuleInvocationPoint(invocationPoint);

        List<SimulationRuleVO> simulationResultList = new ArrayList<SimulationRuleVO>();

        // Setting for rule Set results
        if (null != ruleSetResults && ruleSetResults.size() > 0) {
            for (Map.Entry<Object, Object> entry : ruleSetResults.entrySet()) {
                char[] result = (char[]) entry.getValue();
                RuleSet ruleSet = (RuleSet) entry.getKey();
                List<Rule> ruleList = ruleSet.getRules();
                for (int i = 0 ; i < result.length ; i++) {
                    SimulationRuleVO ruleVO = new SimulationRuleVO();
                    Rule rule = ruleList.get(i);
                    ruleVO.setRuleName(rule.getName());
                    ruleVO.setRuleEnglishExpression(ruleEnglishExpression(rule));
                    ruleVO.setRuleResult(String.valueOf(getRuleResult(result[i])));
                    List<Parameter> parameters = new ArrayList<Parameter>();
                    if(rule instanceof SQLRule){
    					sqlExecutor.evaluateParameter((SQLRule)rule, Objectmap);
    					ruleVO.setAuditParameters(sqlExecutor.getParametersForSimulation(((SQLRule)rule), Objectmap));
    				}
    				else if (!(rule instanceof ScriptRule)) {
                        getParametersList(rule.getRuleExpression(), parameters);
                        ruleVO.setAuditParameters(setRuleAuditParameters(Objectmap, parameters));
                    }
                    simulationResultList.add(ruleVO);
                }
            }
        }

        // Setting for rule Group results
        if (null != ruleGroupResults && ruleGroupResults.size() > 0) {
            for (Map.Entry<Object, Object> entry : ruleGroupResults.entrySet()) {
                char[] result = (char[]) entry.getValue();
                RuleGroup ruleGroup = (RuleGroup) entry.getKey();
                List<Rule> ruleList = ruleGroup.getRules();
                for (int i = 0 ; i < result.length ; i++) {
                    SimulationRuleVO ruleVO = new SimulationRuleVO();
                    Rule rule = ruleList.get(i);
                    ruleVO.setRuleName(rule.getName());
                    ruleVO.setRuleResult(String.valueOf(getRuleResult(result[i])));
                    ruleVO.setRuleEnglishExpression(ruleEnglishExpression(rule));
                    List<Parameter> parameters = new ArrayList<Parameter>();
                    if(rule instanceof SQLRule){
    					sqlExecutor.evaluateParameter((SQLRule)rule, Objectmap);
    					ruleVO.setAuditParameters(sqlExecutor.getParametersForSimulation(((SQLRule)rule), Objectmap));
    				}else if (!(rule instanceof ScriptRule)) {
                        getParametersList(rule.getRuleExpression(), parameters);
                        ruleVO.setAuditParameters(setRuleAuditParameters(Objectmap, parameters));
                    }
                    simulationResultList.add(ruleVO);
                }
            }
        }

        // Setting for Criteria Rule
        if (null != criteriaRuleResults && criteriaRuleResults.size() > 0) {
            for (Map.Entry<Object, Object> entry : criteriaRuleResults.entrySet()) {
                char[] result = (char[]) entry.getValue();
                CriteriaRules criteriaRules = (CriteriaRules) entry.getKey();
                List<Rule> ruleList = criteriaRules.getRuleGroup().getRules();
                for (int i = 0 ; i < result.length ; i++) {
                    SimulationRuleVO ruleVO = new SimulationRuleVO();
                    Rule rule = ruleList.get(i);
                    ruleVO.setRuleName(rule.getName());
                    ruleVO.setRuleResult(String.valueOf(getRuleResult(result[i])));
                    ruleVO.setRuleEnglishExpression(ruleEnglishExpression(rule));
                    List<Parameter> parameters = new ArrayList<Parameter>();
                    if(rule instanceof SQLRule){
    					sqlExecutor.evaluateParameter((SQLRule)rule, Objectmap);
    					ruleVO.setAuditParameters(sqlExecutor.getParametersForSimulation(((SQLRule)rule), Objectmap));
    				}else if (!(rule instanceof ScriptRule)) {
                        getParametersList(rule.getRuleExpression(), parameters);
                        ruleVO.setAuditParameters(setRuleAuditParameters(Objectmap, parameters));
                    }
                    simulationResultList.add(ruleVO);
                }
            }
        }
        rulesSimulationAuditVO.setRules(simulationResultList);
        return rulesSimulationAuditVO;

    }

    /**
     * Simulate a single Rule For any application
     * from rules screen in Add new,Edit and View 
     * Mode. 
     */
    @Override
    public SimulationRuleVO ruleSimulationParametersForSingleRule(Rule rule, Map<Object, Object> contextMap) {

        char result = expressionEvaluator.evaluateRule(rule, contextMap);

        SimulationRuleVO simulationRuleVO = new SimulationRuleVO();
        List<Parameter> parameters = new ArrayList<Parameter>();

        simulationRuleVO.setRuleName(rule.getName());
        if (result == 'P') {
            simulationRuleVO.setRuleResult("true");
        } else {
            simulationRuleVO.setRuleResult("false");
        }
        simulationRuleVO.setRuleEnglishExpression(ruleEnglishExpression(rule));
        if(rule instanceof SQLRule){
        	simulationRuleVO.setAuditParameters(sqlExecutor.getParametersForSimulation(((SQLRule) rule),contextMap));
        }
        else if (!(rule instanceof ScriptRule)) {
            getParametersList(rule.getRuleExpression(), parameters);
            simulationRuleVO.setAuditParameters(setRuleAuditParameters(contextMap, parameters));
        }

        return simulationRuleVO;

    }

    /**
     * Simulate script Rule
     */
    /*public SimulationRuleVO ruleSimulationForScriptRule(Rule rule, char result) {

        SimulationRuleVO simulationRuleVO = new SimulationRuleVO();

        simulationRuleVO.setRuleName(rule.getName());
        if (result == 'P') {
            simulationRuleVO.setRuleResult("true");
        } else {
            simulationRuleVO.setRuleResult("false");
        }
        simulationRuleVO.setRuleEnglishExpression(ruleEnglishExpression(rule));
        return simulationRuleVO;

    }*/

    /**
     * Evaluate Script Rule
     * @param ruleId
     * @param contextMap
     * @param isStrictEvaluation
     * @return
     */
    @Override
    public char evaluateScriptRule(ScriptRule scriptRule, Map contextMap, boolean isStrictEvaluation) {

        try {
        	ScriptRuleEvaluator evaluator = ruleCacheService.getScriptRuleEvaluatorByIdFromCache(scriptRule);
            Boolean result = evaluator.evaluateRule(contextMap);
            if (isStrictEvaluation) {
                if (result == null) {
                    return RuleConstants.RULE_RESULT_NORESULT;
                }
                return result.booleanValue() ? RuleConstants.RULE_RESULT_PASS : RuleConstants.RULE_RESULT_FAIL;
            }
            return (result == null || !result.booleanValue()) ? RuleConstants.RULE_RESULT_FAIL
                    : RuleConstants.RULE_RESULT_PASS;

        } catch (Exception e) {
            BaseLoggers.exceptionLogger.debug("Error" + e);
            throw new RuleException(e);
        }
    }

    /**
     * Return English Expression of Rule from RuleId
     * @param ruleId
     * @return
     */
    private String ruleEnglishExpression(Rule rule) {
        if (rule instanceof ScriptRule) {
            return RuleConstants.SCRIPT_RULE_SENTENCE;
        }if (rule instanceof SQLRule) {
            return RuleConstants.SQL_RULE_SENTENCE;
        } else {
            return ruleSentenceBuilderService.buildRuleSentence(rule.getRuleExpression());
        }
    }

    /* *//**
                                                                                  * get param list
                                                                                  * @param rule
                                                                                  * @param parametersList
                                                                                  */
    /*
    private void getParametersList(RuleExpression rule, List<Parameter> parametersList) {
     if (isLeafNode(rule)) {
         getCondition(rule.getConditions().getExpression(), parametersList);
         return;
     }
     if (rule.getLeftExpression() != null) {
         getParametersList((RuleExpression) rule.getLeftExpression(), parametersList);
     }
     if (rule.getRightExpression() != null) {
         getParametersList((RuleExpression) rule.getRightExpression(), parametersList);
     }
     return;
    }*/

    private void getParametersList(String ruleExpression, List<Parameter> parametersList) {
        String[] tokens = ruleExpression.split(" ");
        if (tokens != null && tokens.length > 0) {
            for (String token : tokens) {
                token = token.trim();
                // if token is bracket and operator
                if (!(token.equals(RuleConstants.LEFT_PAREN) || token.equals(RuleConstants.RIGHT_PAREN) || commaDelimitesString(
                        ExpressionValidationConstants.RULE_OPERATORS).indexOf(token) != -1)) {

                    Condition condition = entityDao.find(Condition.class, Long.parseLong(token));
                    if (condition != null) {
                        buildConditionExpressionToCompile(condition.getConditionExpression(), parametersList);
                    }
                }
            }
        }
    }

    private void buildConditionExpressionToCompile(String conditionExpression, List<Parameter> parametersList) {

        // splitting expression with space - assuming that expression can have only brackets, and arithmetic and relative
        // operator and parameter id.
        String[] tokens = conditionExpression.split(" ");
        if (tokens != null && tokens.length > 0) {
            for (String token : tokens) {
                token = token.trim();
                // if token is bracket and operator
                if (!(token.equals(RuleConstants.LEFT_PAREN) || token.equals(RuleConstants.RIGHT_PAREN)
                        || commaDelimitesString(ExpressionValidationConstants.ARITHMETIC_OPS).indexOf(token) != -1 || commaDelimitesString(
                            ExpressionValidationConstants.REL_OPS).indexOf(token) != -1)) {
                    Parameter parameter = entityDao.find(Parameter.class, Long.parseLong(token));
                    if (parameter != null) {
                        parametersList.add(parameter);
                    }
                }
            }
        }
    }

    /**
     * get condition
     * @param condition
     * @param parametersList
     */
    /*
    private void getCondition(ConditionExpression condition, List<Parameter> parametersList) {

     if (isLeafNode(condition)) {
         parametersList.add(condition.getParameter());
         return;
     }
     if (condition.getLeftExpression() != null) {
         getCondition((ConditionExpression) condition.getLeftExpression(), parametersList);
     }
     if (condition.getLeftExpression() != null) {
         getCondition((ConditionExpression) condition.getRightExpression(), parametersList);
     }
     return;
    }
    */
    /**
     * get result
     * @param result
     * @return
     */
    private boolean getRuleResult(char result) {
        if (result == RuleConstants.RULE_RESULT_PASS) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Set parameters in the list
     * @param objectMap
     * @param parameters
     * @return
     */
    private List<SimulationParameterVO> setRuleAuditParameters(Map<Object, Object> objectMap, List<Parameter> parameters) {

        List<SimulationParameterVO> ruleAuditParamList = new ArrayList<SimulationParameterVO>();

        for (int j = 0 ; j < parameters.size() ; j++) {

            Parameter parameter = parameters.get(j);
            Object parameterResult = null;
            SimulationParameterVO simulationParameterVO = new SimulationParameterVO();
            simulationParameterVO.setParameterName(parameter.getName());
            if (parameter instanceof ConstantParameter) {
                simulationParameterVO.setParameterValue(((ConstantParameter) parameter).getLiteral());

            } else if (parameter instanceof NullParameter) {
                simulationParameterVO.setParameterValue("null");

            } else if (parameter instanceof SystemParameter) {
                simulationParameterVO.setParameterValue(String.valueOf(((SystemParameter) parameter)
                        .getSystemParameterValue()));

            } else if (parameter instanceof QueryParameter) {
                RuleQueryExecutor queryCriteria = new RuleQueryExecutor(((QueryParameter) parameter).getQuery());

                List<QueryParameterAttribute> queryParameterAttributes = ((QueryParameter) parameter)
                        .getQueryParameterAttributes();
                if (queryParameterAttributes != null) {
                    for (QueryParameterAttribute queryAttribute : queryParameterAttributes) {
                        queryCriteria.addQueryParameter(queryAttribute.getQueryParameterName(),
                                RuleExpressionMvelEvaluator.evaluateExpression(queryAttribute.getObjectGraph(), objectMap));
                    }
                }
                List list = entityDao.executeQuery(queryCriteria);
                if (list != null && list.size() > 0) {
                    if (null != list.get(0)) {
                        simulationParameterVO.setParameterValue(list.get(0).toString());
                    }
                }
            } else if (parameter instanceof CompoundParameter) {
                String compoundExpression = buildCompoundParameterExpression(
                        ((CompoundParameter) parameter).getParameterExpression(), objectMap, false);
                parameterResult = RuleExpressionMvelEvaluator.evaluateExpression(compoundExpression, objectMap);
                simulationParameterVO.setParameterValue(parameterResult!=null ? parameterResult.toString() : null);

            } else if (parameter instanceof ReferenceParameter) {
                simulationParameterVO.setParameterValue(entityDao.get(
                        ((ReferenceParameter) parameter).getReferenceEntityId()).getUri());

            } else if (parameter instanceof ObjectGraphParameter) {
                if (((ObjectGraphParameter) parameter).getDataType() == ParameterDataType.PARAMETER_DATA_TYPE_DATE) {
                    parameterResult = RuleExpressionMvelEvaluator.evaluateExpression(
                            "( ?"
                                    + RulesConverterUtility.getNullSafeObjectGraph(((ObjectGraphParameter) parameter)
                                            .getObjectGraph() + RuleConstants.RULE_TIME_IN_MILLIS) + " )", objectMap);
                } else if (((ObjectGraphParameter) parameter).getDataType() == ParameterDataType.PARAMETER_DATA_TYPE_JAVA_UTIL_DATE) {
                    parameterResult = RuleExpressionMvelEvaluator.evaluateExpression(
                            "( ?"
                                    + RulesConverterUtility.getNullSafeObjectGraph(((ObjectGraphParameter) parameter)
                                            .getObjectGraph() + RuleConstants.RULE_TIME_FOR_JAVA_UTIL_DATE) + " )", objectMap);
                } else {
                    parameterResult = RuleExpressionMvelEvaluator.evaluateExpression(
                            "( ?"
                                    + RulesConverterUtility.getNullSafeObjectGraph(((ObjectGraphParameter) parameter)
                                            .getObjectGraph() + " )"), objectMap);
                }

                if (parameterResult instanceof BigDecimal) {
                    parameterResult = ((BigDecimal) parameterResult).setScale(3, BigDecimal.ROUND_HALF_DOWN);
                }

                if (parameterResult != null) {
                    simulationParameterVO.setParameterValue(parameterResult.toString());
                } else {
                    simulationParameterVO.setParameterValue(null);
                }

            } else if (parameter instanceof ScriptParameter) {
                ScriptParameter scriptParameter = ((ScriptParameter) parameter);

                if (scriptParameter.getScriptCodeType() == RuleConstants.SCRIPTCODETYPE_SHELL_SCRIPT) {
                    Object result = evaluateScriptParameter(scriptParameter, objectMap);
                    simulationParameterVO.setParameterValue(result != null ? result.toString() : null);

                } else if (scriptParameter.getScriptCodeType() == RuleConstants.SCRIPTCODETYPE_MVEL_SCRIPT) {
                    Object result = evaluateMvelParameterScript(scriptParameter, objectMap);
                    simulationParameterVO.setParameterValue(result != null ? result.toString() : null);
                }

            } else if(parameter instanceof SQLParameter){
                Map<String, Object> resultMap = sqlExecutor.getParameterValue((SQLParameter)parameter,objectMap);
                if(resultMap.containsKey(RuleConstants.SQL_PARAM_RESULT_FOUND)){
                    simulationParameterVO.setParameterValue(resultMap.get(RuleConstants.SQL_PARAM_RESULT_FOUND).toString());
                }else if(resultMap.containsKey(RuleConstants.SQL_PARAM_RESULT_NOT_FOUND)){
                    simulationParameterVO.setParameterValue(resultMap.get(RuleConstants.SQL_PARAM_RESULT_NOT_FOUND).toString());
                }
            }
            ruleAuditParamList.add(simulationParameterVO);
        }
        return ruleAuditParamList;
    }

    @Override
    public Object find(Long id, Class className) {
        return entityDao.find(className, id);
    }

    @Override
    public List findAll(Class className) {
        return entityDao.findAll(className);
    }

    @Override
    public SimulationResultVO invokeRuleForSimulation(String invocationPoint, final Map<Object, Object> contextObjectMap,
            List<SimulationResultVO> simulationResultVOs) {

        Map<String, Object> resultMap = new HashMap<String, Object>();
        RuleInvocationMapping ruleInvocationMapping = ruleInvocationService.getRuleMapping(invocationPoint);
        if (ruleInvocationMapping == null) {
            return null;
        }

        Map<Object, Object> ruleSetResults = new HashMap<Object, Object>();
        Map<Object, Object> ruleGroupResults = new HashMap<Object, Object>();
        Map<Object, Object> criteriaResult = new HashMap<Object, Object>();

        final Map<Object, Map<Object, Object>> ruleInvocationMappingResults = new HashMap<Object, Map<Object, Object>>();

        /**
         *  Evaluate Rule from RuleGroup -- Start
         */

        evaluateRuleGroup(contextObjectMap, resultMap, ruleInvocationMapping, ruleGroupResults);

        // //////////////////////////////////////////////////////////////////////////////////////////////////////

        ruleInvocationMappingResults.put(RuleConstants.RULEGROUP_KEY, ruleGroupResults);
        ruleInvocationMappingResults.put(RuleConstants.RULESET_KEY, ruleSetResults);
        ruleInvocationMappingResults.put(RuleConstants.CRITERIA_RULES_RESULT_KEY, criteriaResult);

        return ruleSimulationAudit(ruleInvocationMappingResults, contextObjectMap, invocationPoint);

    }

    @Override
    public <T extends BaseEntity> Map<Object, Object> populateContextObject(T baseEntity, Class<T> entityClass) {

        List<RuleSimulationProcess> ruleSimulationProcessList = getRuleSimulationProcesses();

        boolean processorFound = false;

        Map<Object, Object> contextMap = new HashMap<Object, Object>();

        for (RuleSimulationProcess ruleSimulationProcess : ruleSimulationProcessList) {
            if (ruleSimulationProcess.canHandleEntity(entityClass)) {
                contextMap = ruleSimulationProcess.populateContextObject(baseEntity, entityClass);
                processorFound = true;

            }

        }
        if (!processorFound) {
            contextMap = defaultRuleSimulationProcess.populateContextObject(baseEntity, entityClass);

        }
        return contextMap;
    }

    @Override
    public List<BaseEntity> listEntityProcess(EntityType entityType, EntityTypeFilterCriteria entityTypeFilterCriteria)
            throws ClassNotFoundException {
        String className = entityType.getClassName();
        Class entityClass = Class.forName(className);
        List<RuleSimulationProcess> ruleSimulationProcessList = getRuleSimulationProcesses();
        boolean processorFound = false;
        List<BaseEntity> entityList = new ArrayList();
        for (RuleSimulationProcess ruleSimulationProcess : ruleSimulationProcessList) {
            if (ruleSimulationProcess.canHandleEntity(entityClass)) {
                entityList = ruleSimulationProcess.listEntityProcess(entityType, entityTypeFilterCriteria);
                processorFound = true;

            }

        }
        if (!processorFound) {
            entityList = defaultRuleSimulationProcess.listEntityProcess(entityType, entityTypeFilterCriteria);

        }
        return entityList;
    }

    /**
     * 
     * method to evaluateRuleGroup
     * @param contextObjectMap
     * @param resultMap
     * @param ruleInvocationMapping
     * @param ruleGroupResults
     */

    private void evaluateRuleGroup(final Map<Object, Object> contextObjectMap, Map<String, Object> resultMap,
            RuleInvocationMapping ruleInvocationMapping, Map<Object, Object> ruleGroupResults) {
        // Evaluate Rule Group
        if (null != ruleInvocationMapping.getRuleGroup()) {
            Map<Object, Object> relationMap = executeRuleGroup(ruleInvocationMapping.getRuleGroup(), contextObjectMap);

            ruleGroupResults
                    .put(ruleInvocationMapping.getRuleGroup(), relationMap.get(RuleConstants.RULE_GROUP_PATTERN_KEY));

            boolean result = (Boolean) relationMap.get(RuleConstants.RULE_GROUP_RESULT_KEY);
            resultMap.put(RuleInvocationPoint.RULE_INVOCATION_RESULT_VALUE, result);
        }
    }

    protected Map<Object, Object> executeRuleGroup(RuleGroup ruleGroup, Map<Object, Object> relationMap) {

        try {
            if(relationMap instanceof DataContext){
                DataContext dataContext = (DataContext)relationMap;
                dataContext.setExecutionStarted(true);
            }
            // char[] ruleGroupResult = expressionEvaluator.evaluateRule(ruleGroup, relationMap);
            char[] ruleGroupResult = ruleSetExecutorService.evaluateRuleSet(ruleGroup, relationMap);
            relationMap.put(RuleConstants.RULE_GROUP_PATTERN_KEY, ruleGroupResult);

            for (int i = 0 ; i < ruleGroupResult.length ; i++) {
                Rule rule = ruleGroup.getRules().get(i);
                relationMap.put(
                        RulesConverterUtility.replaceSpace(rule.getName()) + RuleConstants.PARAMETER_NAME_ID + rule.getId(),
                        ruleGroupResult[i] == 'P' ? true : false);
            }

            String ruleGroupExpression = ruleExpressionBuilder.buildRuleLevelRuleExpression(ruleGroup
                    .getRuleGroupExpression());
            Object result = MVEL.eval(ruleGroupExpression, relationMap);
            relationMap.put(RuleConstants.RULE_GROUP_RESULT_KEY, result);
            relationMap.put(RuleConstants.RULE_GROUP_PATTERN_KEY, ruleGroupResult);

            return relationMap;
        } catch (Exception e) {
            BaseLoggers.exceptionLogger.error("Exception Occured" + e);
            return null;
        }
    }

    @Override
    public String encryptScriptRule(String scriptRuleExp) {
        return encryptString(scriptRuleExp);
    }

    @Override
    public List<SimulationParameterVO> simulateParamter(Parameter parameter, Map<Object, Object> contextMap) {

        // expressionEvaluator.executeParameter(parameter, contextMap, false);
        List<Parameter> parameters = new ArrayList<Parameter>();
        parameters.add(parameter);
        if (parameter instanceof CompoundParameter) {
            getParameterListForCompoundParameter((CompoundParameter) parameter, parameters);

        }
        if(parameter instanceof SQLParameter){
            getParameterListForSQLParameter((SQLParameter) parameter, parameters);
        }
        List<SimulationParameterVO> simulationParameterVOs = setRuleAuditParameters(contextMap, parameters);
        return simulationParameterVOs;

    }

    /**
     * Gets the parameter list for compound parameter required for showing all the parameters with their values 
     *
     * @param parameter the parameter
     * @param parameters the parameters
     * @return the parameter list for compound parameter
     */
    private void getParameterListForCompoundParameter(CompoundParameter parameter, List<Parameter> parameters) {

        // splitting expression with space - assuming that expression can have only brackets, and arithmetic and relative
        // operator and parameter id.
        String[] tokens = parameter.getParameterExpression().split(" ");
        if (tokens != null && tokens.length > 0) {
            for (String token : tokens) {
                token = token.trim();
                // if token is bracket and operator
                if (token.equals(RuleConstants.LEFT_PAREN) || token.equals(RuleConstants.RIGHT_PAREN)
                        || commaDelimitesString(ExpressionValidationConstants.ARITHMETIC_OPS).indexOf(token) != -1) {

                } else {
                    Parameter innerParameter = entityDao.find(Parameter.class, Long.parseLong(token));
                    if (innerParameter != null) {
                        parameters.add(innerParameter);

                    }
                }
            }
        }

    }

    private void getParameterListForSQLParameter(SQLParameter parameter, List<Parameter> parameters) {
        parameter.getParamMapping().forEach(sqlParameterMapping -> {
            if(sqlParameterMapping.getParameter()!=null && sqlParameterMapping.getParameter().getId()!=null){
                Parameter innerParam = entityDao.find(Parameter.class, sqlParameterMapping.getParameter().getId());
                parameters.add(innerParam);
            }
        });
    }
}
