package com.nucleus.rules.service;

import java.math.BigDecimal;
import java.util.*;

import javax.inject.Inject;
import javax.inject.Named;

import com.nucleus.rules.model.*;
import com.nucleus.rules.utils.DataContext;
import net.bull.javamelody.MonitoredWithSpring;

import org.apache.commons.lang.StringUtils;
import org.mvel2.MVEL;

import com.nucleus.dao.query.RuleQueryExecutor;
import com.nucleus.entity.Entity;
import com.nucleus.entity.EntityId;
import com.nucleus.logging.BaseLoggers;
import com.nucleus.rules.exception.NullValueException;
import com.nucleus.rules.exception.RuleException;
import org.springframework.context.annotation.Lazy;

/**
 * 
 * @author Nucleus Software Exports Limited
 */
@Named(value = "expressionEvaluator")
public class ExpressionEvaluatorImpl extends BaseRuleServiceImpl implements ExpressionEvaluator {

    @Inject
    @Named("ruleService")
    private RuleService               ruleService;

    @Inject
    @Named("compiledExpressionBuilder")
    private CompiledExpressionBuilder compiledExpressionBuilder;
    
    @Inject
    @Named("sQLRuleExecutor")
    private SQLRuleExecutor sqlRuleExecutor;

    @Lazy
    @Inject
    @Named("ruleExceptionLoggingServiceImpl")
    private RuleExceptionLoggingService ruleExceptionLoggingService;


    private char evaluateRuleExpression(String ruleExpression, Map<Object, Object> map, boolean isStrictMode) {
        StringBuilder expression = new StringBuilder();

        // splitting expression with space - assuming that expression can have only brackets, and or operator and condition
        // id.
        String[] tokens = ruleExpression.split(" ");
        if (tokens != null && tokens.length > 0) {
            for (String token : tokens) {
                token = token.trim();
                // if token is bracket and operator
                if (token.equals(RuleConstants.LEFT_PAREN) || token.equals(RuleConstants.RIGHT_PAREN)
                        || commaDelimitesString(ExpressionValidationConstants.RULE_OPERATORS).indexOf(token) != -1) {
                    expression.append(token).append(" ");
                } else {
                    Condition condition = entityDao.find(Condition.class, Long.parseLong(token));
                    if (condition != null) {
                        expression.append(
                                buildConditionExpressionToCompile(condition.getConditionExpression(), map, isStrictMode))
                                .append(" ");
                    }
                }
            }
        }
        if (expression.length() > 0) {

            Boolean result = (Boolean) RuleExpressionMvelEvaluator.evaluateExpression(expression.toString(), map);

            if (result != null && result) {
                return RuleConstants.RULE_RESULT_PASS;
            } else {
                return RuleConstants.RULE_RESULT_FAIL;
            }
        }

        return ' ';

    }

    private String buildConditionExpressionToCompile(String conditionExpression, Map<Object, Object> map,
            boolean isStrictMode) {
        StringBuilder expression = new StringBuilder();

        // splitting expression with space - assuming that expression can have only brackets, and arithmetic and relative
        // operator and parameter id.
        String[] tokens = conditionExpression.split(" ");
        if (tokens != null && tokens.length > 0) {
            for (String token : tokens) {
                token = token.trim();
                // if token is bracket and operator
                if (token.equals(RuleConstants.LEFT_PAREN) || token.equals(RuleConstants.RIGHT_PAREN)
                        || commaDelimitesString(ExpressionValidationConstants.ARITHMETIC_OPS).indexOf(token) != -1
                        || commaDelimitesString(ExpressionValidationConstants.REL_OPS).indexOf(token) != -1) {
                    expression.append(token).append(" ");
                } else {
                    Parameter parameter = entityDao.find(Parameter.class, Long.parseLong(token));
                    if (parameter != null) {
                        expression.append(buildParameterExpressionToCompile(parameter, map, isStrictMode)).append(" ");
                    }
                }
            }
        }
        if (expression.length() > 0) {
            return expression.toString();
        }
        return null;

    }

    /**
     * Execute the Parameter for charge Policy
     * computation
     * @param parameter
     * @param map
     * @param isStrictMode
     * @param contextMap
     * @return
     */
    @Override
    public Object executeParameter(Parameter parameter, Map<Object, Object> contextMap, boolean isStrictMode) {
        String expression = evaluateParameter(parameter, contextMap, isStrictMode);
        Object result = RuleExpressionMvelEvaluator.evaluateExpression(expression, contextMap);

        if (null != result && result instanceof Double) {
            if (RuleConstants.MVEL_RESULT.indexOf(result.toString()) != -1) {
                result = null;
            }
        }
        return result;
    }

    /**
     * Evaluates the Parameter's Value
     * 
     * @param parameter
     * @param map
     * @return
     */
    protected String evaluateParameter(Parameter parameter, Map<Object, Object> map, boolean isStrictMode) {
        if (parameter == null) {
            throw new RuleException("Parameter Cannot be null/empty");
        }
        if(map instanceof DataContext){
            DataContext dataContext = (DataContext)map;
            dataContext.setExecutionStarted(true);
        }
        Object parameterValue = null;
        String parameterKey = RulesConverterUtility.replaceSpace(parameter.getName()) + RuleConstants.PARAMETER_NAME_ID
                + parameter.getId();

        try {
            if (parameter instanceof ObjectGraphParameter) {
                String obj = ((ObjectGraphParameter) parameter).getObjectGraph();

                if (!isStrictMode) {

                    if (((ObjectGraphParameter) parameter).getDataType() == ParameterDataType.PARAMETER_DATA_TYPE_DATE)
                        parameterKey = "( ?" + RulesConverterUtility.getNullSafeObjectGraph(obj)
                                + RuleConstants.RULE_TIME_IN_MILLIS + " )";
                    else if (((ObjectGraphParameter) parameter).getDataType() == ParameterDataType.PARAMETER_DATA_TYPE_JAVA_UTIL_DATE)
                        parameterKey = "( ?" + RulesConverterUtility.getNullSafeObjectGraph(obj)
                                + RuleConstants.RULE_TIME_FOR_JAVA_UTIL_DATE + " )";
                    else
                        parameterKey = "( ?" + RulesConverterUtility.getNullSafeObjectGraph(obj) + " )";

                } else {

                    if (((ObjectGraphParameter) parameter).getDataType() == ParameterDataType.PARAMETER_DATA_TYPE_DATE) {
                        parameterKey = obj + RuleConstants.RULE_TIME_IN_MILLIS;
                    } else if (((ObjectGraphParameter) parameter).getDataType() == ParameterDataType.PARAMETER_DATA_TYPE_JAVA_UTIL_DATE) {
                        parameterKey = obj + RuleConstants.RULE_TIME_FOR_JAVA_UTIL_DATE;
                    } else {
                        parameterKey = obj;
                    }

                    String nullSafeObj = RulesConverterUtility.getNullSafeObjectGraph(parameterKey);

                    if (MVEL.eval(nullSafeObj, map) == null) {
                        throw new NullValueException(RuleConstants.RULE_NULL_VALUE_EXCEPTION);
                    }
                }

            } else if (parameter instanceof ConstantParameter) {
                parameterValue = ((ConstantParameter) parameter).getLiteralValue();
                map.put(parameterKey, parameterValue);

            } else if (parameter instanceof ReferenceParameter) {
                parameterValue = entityDao.get(((ReferenceParameter) parameter).getReferenceEntityId());
                map.put(parameterKey, parameterValue);

            } else if (parameter instanceof QueryParameter) {
                RuleQueryExecutor queryCriteria = new RuleQueryExecutor(((QueryParameter) parameter).getQuery());

                List<QueryParameterAttribute> queryParameterAttributes = ((QueryParameter) parameter)
                        .getQueryParameterAttributes();
                if (queryParameterAttributes != null) {
                    for (QueryParameterAttribute queryAttribute : queryParameterAttributes) {
                        queryCriteria.addQueryParameter(queryAttribute.getQueryParameterName(),
                                RuleExpressionMvelEvaluator.evaluateExpression(queryAttribute.getObjectGraph(), map));
                    }
                }

                List list = entityDao.executeQuery(queryCriteria);
                if (list != null && list.size() > 0) {
                    parameterValue = list.get(0);
                }
                map.put(parameterKey, parameterValue);

            } else if (parameter instanceof SystemParameter) {

                if (((SystemParameter) parameter).getSystemParameterType() == SystemParameterType.SYSTEM_PARAMETER_TYPE_CURRENT_USER) {

                    parameterValue = entityDao.get(EntityId.fromUri(map.get("user.referenceURI").toString()));

                    map.put(parameterKey, parameterValue);

                } else {
                    parameterValue = ((SystemParameter) parameter).getSystemParameterValue();
                    map.put(parameterKey, parameterValue);
                }

            } else if (parameter instanceof CompoundParameter) {
                parameterValue = buildCompoundParameterExpression(((CompoundParameter) parameter).getParameterExpression(), map,
                        isStrictMode);
                return parameterValue.toString();

            } else if (parameter instanceof NullParameter) {
                map.put(parameterKey, null);

            } else if (parameter instanceof ScriptParameter) {
                ScriptParameter scriptParameter = ((ScriptParameter) parameter);

                if (scriptParameter.getScriptCodeType() == RuleConstants.SCRIPTCODETYPE_SHELL_SCRIPT) {
                    Object result = evaluateScriptParameter(scriptParameter, map);
                    map.put(parameterKey, result);

                } else if (scriptParameter.getScriptCodeType() == RuleConstants.SCRIPTCODETYPE_MVEL_SCRIPT) {
                    Object result = evaluateMvelParameterScript(scriptParameter, map);
                    map.put(parameterKey, result);
                }

            } else if (parameter instanceof SQLParameter) {
                Map<String, Object> resultMap = sqlRuleExecutor.getParameterValue((SQLParameter) parameter, map);
                Object result = resultMap.get(RuleConstants.SQL_PARAM_RESULT_FOUND);
                if (result != null) {
                    map.put(parameterKey, result);
                }
            }
        }catch(Exception e){
            BaseLoggers.exceptionLogger.error("Exception occured while evaluating parameter :" ,e);
            RuleExceptionLoggingVO ruleExceptionLoggingVO = new RuleExceptionLoggingVO();
            ruleExceptionLoggingVO.setContextMap(map);
            ruleExceptionLoggingVO.setE(e);
            ruleExceptionLoggingVO.setParameter(parameter);
            ruleExceptionLoggingVO.setExceptionOwner(RuleConstants.PARAMETER_EXCEPTION);
            ruleExceptionLoggingService.saveRuleErrorLogs(ruleExceptionLoggingVO);
            throw new RuleException("Error occured while evaluating parameter : "+parameter.getCode()+" : ",e);
        }

        return parameterKey;
    }

    @Override
    @MonitoredWithSpring(name = "EEI_EVAL_RULE")
    public char evaluateRule(Rule rule, Map<Object, Object> map) {
        return evaluateRule(rule, map, false);
    }

    @Override
    @MonitoredWithSpring(name = "EEI_EVAL_RULE_WITH_STRICT_MODE")
    public char evaluateRule(Rule rule, Map<Object, Object> map, boolean isStrictMode) {
        if (rule == null) {
            throw new RuleException("Rule is null");
        }
        if (rule instanceof ScriptRule) {
            if (((ScriptRule) rule).getScriptCodeType() == RuleConstants.SCRIPTCODETYPE_SHELL_SCRIPT) {
                return evaluateScriptRule((ScriptRule) rule, map, isStrictMode);

            } else if (((ScriptRule) rule).getScriptCodeType() == RuleConstants.SCRIPTCODETYPE_MVEL_SCRIPT) {
                return evaluateMvelRuleScript((ScriptRule) rule, map, isStrictMode);
            }
        }else if(rule instanceof SQLRule){
        	return sqlRuleExecutor.evaluateSQLRule((SQLRule)rule, map, isStrictMode);
        }

        return evaluateRuleExpression(rule.getRuleExpression(), map, isStrictMode);

    }

    @Override
    @MonitoredWithSpring(name = "EEI_EVAL_RULE_WITH_ENTITY")
    public char evaluateRule(Rule rule, List<Entity> entities) {
        return evaluateRule(rule, entities, false);
    }

    @Override
    @MonitoredWithSpring(name = "EEI_EVAL_RULE_WITH_ENTITY_AND_STRICT_MODE")
    public char evaluateRule(Rule rule, List<Entity> entities, boolean isStrictMode) {
        return evaluateRule(rule, getContextObjectMap(entities), isStrictMode);
    }

    @Override
    @MonitoredWithSpring(name = "EEI_EVAL_RULE_ACTION")
    public void executeRuleAction(RuleAction ruleAction, Map<Object, Object> map) {

        if (ruleAction instanceof AssignmentAction) {
            AssignmentAction assignment = (AssignmentAction) ruleAction;
            String parameterExpression = evaluateParameter(assignment.getRightValue(), map, true);
            parameterExpression = assignment.getLeftValue() + "=" + parameterExpression;
            RuleExpressionMvelEvaluator.evaluateExpression(parameterExpression, map);
        }

    }

    @Override
    @MonitoredWithSpring(name = "EEI_EVAL_RULE_SET")
    public char[] evaluateRuleSet(RuleSet ruleSet, Map<Object, Object> map) {

        if (ruleSet != null) {
            char[] result = new char[ruleSet.getRules().size()];

            List<Rule> rules = ruleSet.getRules();
            int i = 0;
            for (Rule rule : rules) {
                try {
                    result[i] = evaluateRule(rule, map);
                } catch (Exception e) {
                    result[i] = RuleConstants.RULE_RESULT_NORESULT;
                    BaseLoggers.exceptionLogger.error("Exception occured while evaluating rule '" + rule.getDisplayName()
                            + "' : " + e.getMessage());
                }
                i++;
            }
            return result;
        } else
            return null;
    }

    private char evaluateMvelRuleScript(ScriptRule scriptRule, Map contextMap, boolean isStrictEvaluation) {

        try {

            Boolean result = (Boolean) RuleExpressionMvelEvaluator
                    .evaluateExpression(scriptRule.getScriptCode(), contextMap);

            if (isStrictEvaluation) {
                if (result == null) {
                    return RuleConstants.RULE_RESULT_NORESULT;
                }
                return result.booleanValue() ? RuleConstants.RULE_RESULT_PASS : RuleConstants.RULE_RESULT_FAIL;
            }
            return (result == null || !result.booleanValue()) ? RuleConstants.RULE_RESULT_FAIL
                    : RuleConstants.RULE_RESULT_PASS;

        } catch (Exception e) {
            BaseLoggers.exceptionLogger.debug("Error occured for Rule ::" + scriptRule.getName() + "::" + e);
            throw new RuleException(e);
        }
    }

    @Override
    public boolean placeHolderParamInCondition(String conditionExpression) {

        if (conditionExpression == null) {
            throw new RuleException("condition expression is null");
        }
        String[] tokens = conditionExpression.split(" ");
        if (tokens != null && tokens.length > 0) {
            for (String token : tokens) {
                token = token.trim();
                // if token is bracket and operator
                if (!(token.equals(RuleConstants.LEFT_PAREN) || token.equals(RuleConstants.RIGHT_PAREN)
                        || commaDelimitesString(ExpressionValidationConstants.ARITHMETIC_OPS).indexOf(token) != -1 || commaDelimitesString(
                            ExpressionValidationConstants.REL_OPS).indexOf(token) != -1)) {

                    Parameter parameter = entityDao.find(Parameter.class, Long.parseLong(token));
                    if (parameter instanceof PlaceHolderParameter) {
                        return true;
                    }
                }
            }
        }

        return false;

    }

    @Override
    public boolean placeHolderParamInRule(String ruleExpression) {

        if (ruleExpression == null) {
            throw new RuleException("Rule expression is null");
        }
        String[] tokens = ruleExpression.split(" ");
        if (tokens != null && tokens.length > 0) {
            for (String token : tokens) {
                token = token.trim();
                // if token is bracket and operator
                if (!(token.equals(RuleConstants.LEFT_PAREN) || token.equals(RuleConstants.RIGHT_PAREN) || commaDelimitesString(
                        ExpressionValidationConstants.RULE_OPERATORS).indexOf(token) != -1)) {

                    Condition condition = entityDao.find(Condition.class, Long.parseLong(token));
                    if (condition.isCriteriaConditionFlag()) {
                        return true;
                    }
                }
            }
        }

        return false;

    }

    @Override
    public String getRuleExpressionWithValues(String ruleExpression, Map contextMap, Map<String, String> ognlNameMapping) {

        StringBuilder finalExp = new StringBuilder();
        String[] tokens = ruleExpression.split(" ");

        for (String token : tokens) {
            token = token.trim();
            if (token.startsWith("?contextObject")) {
                Object tokenValue = RuleExpressionMvelEvaluator.evaluateExpression(token, contextMap);
                if (tokenValue instanceof BigDecimal) {
                    tokenValue = ((BigDecimal) tokenValue).setScale(3, BigDecimal.ROUND_HALF_DOWN);
                }
                token = StringUtils.remove(token, '?');
                // String tokenName = ruleService.findNamethroughObjectGraph(token);
                String tokenName = ognlNameMapping.get(token);
                finalExp.append(tokenName).append(" ( ").append(tokenValue).append(" ) ");

            } else if (token.contains("$")) {
                String tokenName = token.substring(0, token.indexOf("$"));
                if (tokenName.contains("checkNullValue")) {
                    tokenName = StringUtils.substringAfter(tokenName, "(");
                    token = StringUtils.substringBefore(StringUtils.substringAfter(token, "("), ")");
                }
                Object tokenValue = contextMap.get(token);
                finalExp.append(tokenName).append(" ( ").append(tokenValue).append(" ) ");

            } else if (!(token.contains("checkNullValue") || token.equals("))"))) {
                finalExp.append(" ").append(token).append(" ");
            }
        }
        return finalExp.toString();

    }

    @Override
    public void getOgnlParamMapForParameter(Parameter parameter, Map<String, String> ognlParamMap,
            Map<Object, Object> contextMap) {

        Set<Parameter> parameters = new HashSet<Parameter>();
        String compoundExpression = compiledExpressionBuilder.buildParameterExpressionToCompile(parameter, parameters, null,
                true, null, 0);
        String[] tokens = compoundExpression.split(" ");

        for (String token : tokens) {
            token = token.trim();
            if (token.startsWith("?contextObject")) {
                token = StringUtils.remove(token, '?');
                String tokenName = ruleService.findNamethroughObjectGraph(token);
                ognlParamMap.put(token, tokenName);

            }
        }

    }


    @Override
    public Map getRuleExpressionKeyElementsForSimulation(String ruleExpressionUnit, Map contextMap, Map<String, String> ognlNameMapping) {
        Map tokenKeyMap = new HashMap();
        //StringBuilder finalExp = new StringBuilder();
        if(!ruleExpressionUnit.contains("||")) {
            ruleExpressionUnit = ruleExpressionUnit.replaceAll("[\\(\\)\\{\\}]+", "");
            String[] tokens = ruleExpressionUnit.trim().split("\\s+");
            if (tokens.length != 3) {
                return tokenKeyMap;
            }
            //token1
            String leftToken = tokens[0].trim();
            if (leftToken.startsWith("?contextObject")) {
                leftToken = StringUtils.remove(leftToken, '?');
            }

            //token2
            String operation = tokens[1].trim();

            //token3
            String rightToken = tokens[2].trim();

            if (rightToken.startsWith("?contextObject")) {
                rightToken = StringUtils.remove(rightToken, '?');
            }

            for (String token : tokens) {
                token = token.trim();
                if (token.startsWith("?contextObject")) {
                    Object tokenValue = RuleExpressionMvelEvaluator.evaluateExpression(token, contextMap);
                    if (tokenValue instanceof BigDecimal) {
                        tokenValue = ((BigDecimal) tokenValue).setScale(3, BigDecimal.ROUND_HALF_DOWN);
                    }
                    token = StringUtils.remove(token, '?');
                    // String tokenName = ruleService.findNamethroughObjectGraph(token);
                    String tokenName = ognlNameMapping.get(token);
                    if (token.equals(leftToken)) {
                        tokenKeyMap.put("tokenName", tokenName);
                        tokenKeyMap.put("actualValue", tokenValue);
                    } else if (token.equals(rightToken)) {
                        tokenKeyMap.put("expectedValue", tokenValue);
                    } else {
                        tokenKeyMap.put("operator", tokenValue);
                    }
                    //finalExp.append(tokenName).append(" ( ").append(tokenValue).append(" ) ");

                } else if (token.contains("$")) {
                    String tokenName = token.substring(0, token.indexOf("$"));
                    if (tokenName.contains("checkNullValue")) {
                        tokenName = StringUtils.substringAfter(tokenName, "(");
                        token = StringUtils.substringBefore(StringUtils.substringAfter(token, "("), ")");
                    }
                    Object tokenValue = contextMap.get(token);
                    if (token.equals(leftToken)) {
                        tokenKeyMap.put("tokenName", tokenName);
                        tokenKeyMap.put("actualValue", tokenValue);
                    } else if (token.equals(rightToken)) {
                        tokenKeyMap.put("expectedValue", tokenValue);
                    } else {
                        tokenKeyMap.put("operator", tokenValue);
                    }

                } else if (!(token.contains("checkNullValue") || token.equals("))"))) {
                    //finalExp.append(" ").append(token).append(" ");
                    if (token.equals(leftToken)) {
                        tokenKeyMap.put("tokenName", token);
                        tokenKeyMap.put("actualValue", token);
                    } else if (token.equals(rightToken)) {
                        tokenKeyMap.put("expectedValue", token);
                    } else {
                        tokenKeyMap.put("operator", token);
                    }
                }
            }
        }else{
            Map OrTokenKeyMap = new HashMap();
            String [] expression = ruleExpressionUnit.split("\\|\\|");
            int index=0;
            for(String exp : expression){
                OrTokenKeyMap = getRuleExpressionKeyElementsForSimulation(exp,contextMap,ognlNameMapping);
                if(index==0) {

                    tokenKeyMap.put("expectedValue","( " + OrTokenKeyMap.get("expectedValue"));
                }else {

                    tokenKeyMap.put("expectedValue", tokenKeyMap.get("expectedValue") + " or " + OrTokenKeyMap.get("expectedValue"));
                }
                index++;
            }
            tokenKeyMap.put("expectedValue", tokenKeyMap.get("expectedValue") + " )");
            tokenKeyMap.put("tokenName",OrTokenKeyMap.get("tokenName"));
            tokenKeyMap.put("operator",OrTokenKeyMap.get("operator"));
            tokenKeyMap.put("actualValue",OrTokenKeyMap.get("actualValue") );

        }

        return tokenKeyMap;

    }

}
