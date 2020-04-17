package com.nucleus.rules.service;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import javax.inject.Inject;
import javax.inject.Named;

import com.nucleus.persistence.HibernateUtils;
import com.nucleus.rules.model.*;
import com.nucleus.rules.utils.DataContext;
import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;
import org.mvel2.MVEL;

import com.nucleus.core.exceptions.InvalidDataException;
import com.nucleus.dao.query.RuleQueryExecutor;
import com.nucleus.entity.Entity;
import com.nucleus.entity.EntityId;
import com.nucleus.logging.BaseLoggers;
import com.nucleus.rules.exception.NullValueException;
import com.nucleus.rules.exception.RuleException;
import com.nucleus.service.BaseServiceImpl;

/**
 * 
 * @author Nucleus Software Exports Limited
 * Expression Builder
 */

public abstract class BaseRuleServiceImpl extends BaseServiceImpl {

    @Inject
    @Named("stringEncryptor")
    protected StandardPBEStringEncryptor pbeEncryptor;

    protected Rule getRule(Long ruleId) {
        return entityDao.find(Rule.class, ruleId);
    }
    
    @Inject
	@Named("parameterService")
	private ParameterService             parameterService;
    
    @Inject
	@Named("ruleCacheService")
	private RuleCacheService             ruleCacheService;

    @Inject
    @Named("sQLRuleExecutor")
    private SQLRuleExecutor sqlRuleExecutor;
    
    protected Condition getCondition(Long conditionId) {
        return entityDao.find(Condition.class, conditionId);
    }

    protected Parameter getParameter(Long parameterId) {
        return entityDao.find(Parameter.class, parameterId);
    }

    /**
     * Checked if the Expression Tree's node is the leaf node or not.
     * 
     * @param exp
     * @return returns true if the node is a leaf node otherwise it returns
     *         false.
     */
    protected boolean isLeafNode(ExpressionTree exp) {

        if (null != exp && exp.getLeftExpression() == null && exp.getRightExpression() == null)
            return true;
        else
            return false;
    }

    /**
     * Compare precendece of two operators.
     * 
     * @param token1
     *            The first operator .
     * @param token2
     *            The second operator .
     * @return A negative number if token1 has a smaller precedence than token2,
     *         0 if the precendences of the two tokens are equal, a positive
     *         number otherwise.
     */
    protected int cmpPrecedence(String token1, String token2) {
        if (!isOperator(token1) || !isOperator(token2)) {
            throw new InvalidDataException("Invalied tokens: " + token1 + " " + token2);
        }
        return RuleConstants.OPERATORS.get(token1)[0] - RuleConstants.OPERATORS.get(token2)[0];
    }

    /**
     * Test if a certain is an operator .
     * 
     * @param token
     *            The token to be tested .
     * @return True if token is an operator . Otherwise False .
     */

    protected boolean isOperator(String token) {
        return RuleConstants.OPERATORS.containsKey(token);
    }

    /**
     * Test the associativity of a certain operator token .
     * 
     * @param token
     *            The token to be tested (needs to operator).
     * @param type
     *            LEFT_ASSOC or RIGHT_ASSOC
     * @return True if the tokenType equals the input parameter type .
     */
    protected boolean isAssociative(String token, int type) {
        if (!isOperator(token)) {
            throw new InvalidDataException("Invalid token: " + token);
        }
        if (RuleConstants.OPERATORS.get(token)[1] == type) {
            return true;
        }
        return false;
    }

    /**
     * 
     * Returns the infix form of the expression
     * @param inputTokens
     * @return
     */
    protected ArrayList<String> infixToRPN(String[] inputTokens) {
        ArrayList<String> out = new ArrayList<String>();
        Stack<String> stack = new Stack<String>();
        // For all the input tokens [S1] read the next token [S2]
        for (String token : inputTokens) {
            if (isOperator(token)) {
                // If token is an operator (x) [S3]
                while (!stack.empty() && isOperator(stack.peek())) {
                    // [S4]
                    if ((isAssociative(token, RuleConstants.LEFT_ASSOC) && cmpPrecedence(token, stack.peek()) <= 0)
                            || (isAssociative(token, RuleConstants.RIGHT_ASSOC) && cmpPrecedence(token, stack.peek()) < 0)) {
                        out.add(stack.pop()); // [S5] [S6]
                        continue;
                    }
                    break;
                }
                // Push the new operator on the stack [S7]
                stack.push(token);
            } else if (token.equals("(")) {
                stack.push(token); // [S8]
            } else if (token.equals(")")) {
                // [S9]
                while (!stack.empty() && !stack.peek().equals("(")) {
                    out.add(stack.pop()); // [S10]
                }
                stack.pop(); // [S11]
            } else {
                out.add(token); // [S12]
            }
        }
        while (!stack.empty()) {
            out.add(stack.pop()); // [S13]
        }
        return out;
    }

    /**
     * 
     * Method accepts array and converts to comma delimited string
     * @param list
     * @return
     */

    protected String commaDelimitesString(String[] list) {
        StringBuffer delimited = new StringBuffer("");
        for (int i = 0 ; list != null && i < list.length ; i++) {
            delimited.append(list[i]);
            if (i < list.length - 1) {
                delimited.append(',');
            }
        }
        return delimited.toString();
    }

    /**
     * 
     * @param entities
     * @return
     */
    public static Map getContextObjectMap(List entities) {
        Map<String, Object> contextMap = new HashMap<String, Object>();

        if (entities != null && entities.size() > 0) {
            for (Object entityObj : entities) {
                if (entityObj instanceof Entity) {
                    contextMap.put(RuleConstants.CONTEXT_OBJECT + entityObj.getClass().getSimpleName(), entityObj);
                }
            }
        }
        return contextMap;
    }

    /**
     * 
     * Evaluate Script Rule
     * @param scriptRule
     * @param contextMap
     * @param isStrictEvaluation
     * @return
     */

    protected char evaluateScriptRule(ScriptRule scriptRule, Map contextMap, boolean isStrictEvaluation) {

    	try {
    		scriptRule.setScriptCodeValue(decryptString(scriptRule.getScriptCode()));
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
            BaseLoggers.exceptionLogger.debug("Error occured for Rule ::" + scriptRule.getName() + "::" + e);
            throw new RuleException("Error occured for Rule ::" + scriptRule.getName(), e);
        }
    }

    /**
     * 
     * Evaluate Script Parameter
     * @param scriptParameter
     * @param contextMap
     * @return
     */

    protected Object evaluateScriptParameter(ScriptParameter scriptParameter, Map contextMap) {
        try {
			ScriptParameter cachedScriptParameter = null;
			if (scriptParameter != null && scriptParameter.getId() != null
					&& !scriptParameter.isScriptParameterSimulationInvoked()) {
				cachedScriptParameter = (ScriptParameter) parameterService
						.getParametersFromCacheById(scriptParameter.getId());
			}
        	if (cachedScriptParameter == null) {
        		cachedScriptParameter = scriptParameter;
        	}
        	ScriptParameterEvaluator evaluator = null;
        	if(!scriptParameter.isScriptParameterSimulationInvoked()) {
        		evaluator = parameterService.getScriptParameterEvaluatorById(cachedScriptParameter.getId());
        	} 
            if (evaluator == null) {
            	evaluator = parameterService.generateScriptParameterEvaluator(cachedScriptParameter);
            }
            return evaluator.evaluateParameter(contextMap);
        } catch (Exception e) {
            BaseLoggers.exceptionLogger.error("Error occured while evaluating Script Parameter :: "
                    + scriptParameter.getName() + e);
            throw new RuleException("Error occured while evaluating Script Parameter :: " + scriptParameter.getName(), e);
        }
    }

    /**
     * 
     * Encrypt Script Code
     * @param scriptCode
     * @return
     */

    protected String encryptString(String scriptCode) {
        if (null != scriptCode && !scriptCode.equals("")) {
            return pbeEncryptor.encrypt(scriptCode);
        }
        return scriptCode;
    }

    /**
     * 
     * Decrypt Script Code
     * @param scriptCode
     * @return
     */

    public String decryptString(String scriptCode) {
        if (null != scriptCode && !scriptCode.equals("")) {
            return pbeEncryptor.decrypt(scriptCode);
        }
        return scriptCode;
    }

    /**
     * 
     * Method to compile the rule expression to Mvel Script
     * @param expression
     * @return
     */
    protected Serializable compileExpression(String expression) {
        Serializable compiledExpression = null;

        try {
            compiledExpression = MVEL.compileExpression(expression);
        } catch (Exception e) {
            BaseLoggers.flowLogger.debug("Error in compiling expression :: " + expression);
        }
        return compiledExpression;
    }

    /**
     * method to evaluate mvel based script parameter
     * @param scriptParameter
     * @param contextMap
     * @return
     */
    protected Object evaluateMvelParameterScript(ScriptParameter scriptParameter, Map contextMap) {

        try {
            if(contextMap instanceof DataContext){
                DataContext dataContext = (DataContext)contextMap;
                dataContext.setExecutionStarted(true);
            }
            Object result = MVEL.eval(scriptParameter.getScriptCode(), contextMap);

            if (result instanceof List) {
                if (((ArrayList) result).size() > 0) {
                    return ((ArrayList) result).get(0);
                } else {
                    return null;
                }
            }
            return result;

        } catch (Exception e) {
            BaseLoggers.exceptionLogger.debug("Error occured for Parameter ::" + scriptParameter.getName() + "::" + e);
            throw new RuleException(e);
        }
    }

    protected void evaluateRuntimeParameters(Map map, Set<Parameter> parameters) {
        if (parameters != null && parameters.size() > 0) {
            if(map instanceof DataContext){
                DataContext dataContext = (DataContext)map;
                dataContext.setExecutionStarted(true);
            }
            for (Parameter parameter : parameters) {
                Object parameterValue = null;
                String parameterKey = RulesConverterUtility.replaceSpace(parameter.getName())
                        + RuleConstants.PARAMETER_NAME_ID + parameter.getId();

                if (parameter instanceof QueryParameter) {
                    RuleQueryExecutor queryCriteria = new RuleQueryExecutor(((QueryParameter) parameter).getQuery());

                    List<QueryParameterAttribute> queryParameterAttributes = ((QueryParameter) parameter)
                            .getQueryParameterAttributes();
                    if (queryParameterAttributes != null) {
                        for (QueryParameterAttribute queryAttribute : queryParameterAttributes) {
                            queryCriteria.addQueryParameter(queryAttribute.getQueryParameterName(),
                                    MVEL.eval(queryAttribute.getObjectGraph(), map));
                        }
                    }

                    List list = entityDao.executeQuery(queryCriteria);
                    if (list != null && list.size() > 0) {
                        parameterValue = list.get(0);
                    }

                    map.put(parameterKey, parameterValue);

                } else if (parameter instanceof SystemParameter) {
                    parameterValue = ((SystemParameter) parameter).getSystemParameterValue();
                    map.put(parameterKey, parameterValue);

                } else if (parameter instanceof ScriptParameter) {
                    ScriptParameter scriptParameter = ((ScriptParameter) parameter);

                    if (scriptParameter.getScriptCodeType() == RuleConstants.SCRIPTCODETYPE_SHELL_SCRIPT) {
                        Object result = evaluateScriptParameter(scriptParameter, map);
                        map.put(parameterKey, result);

                    } else if (scriptParameter.getScriptCodeType() == RuleConstants.SCRIPTCODETYPE_MVEL_SCRIPT) {
                        Object result = evaluateMvelParameterScript(scriptParameter, map);
                        map.put(parameterKey, result);
                    }
                }else if(parameter instanceof SQLParameter){
                    Map<String, Object> resultMap = sqlRuleExecutor.getParameterValue((SQLParameter)parameter,map);
                    Object result = resultMap.get(RuleConstants.SQL_PARAM_RESULT_FOUND);
                    if(result!=null){
                        map.put(parameterKey,result);
                    }
                }

            }
        }
    }

    /**
     * 
     * Method to evaluate the braces inside the Expression
     * @param exp
     * @return
     */
    protected List<ValidationError> validateExpressionBrackets(String exp, List<ValidationError> validationErrorsList) {

        int lastIndex = exp.lastIndexOf("(");
        int firstIndex = exp.indexOf(")", lastIndex);

        String content = "";
        if (lastIndex < firstIndex) {
            if (lastIndex != -1 && firstIndex != -1) {
                content = exp.substring(lastIndex + 1, firstIndex);
                if (content.trim().length() == 0) {
                    return setValidationErrors("label.invalid.expression.brackets", "", validationErrorsList);
                }
                exp = exp.substring(0, lastIndex) + exp.substring(firstIndex + 1, exp.length());
                return validateExpressionBrackets(exp, validationErrorsList);
            }
        }
        if (lastIndex == -1 && firstIndex == -1) {
            return validationErrorsList;
        } else {
            return setValidationErrors("label.invalid.expression.unbalanced.brackets", "", validationErrorsList);
        }
    }

    /**
     * 
     * Fucntuion to set the error messages and return the error list
     * @param key
     * @param errorMessage
     * @param validationErrorsList
     * @return
     */
    protected List<ValidationError> setValidationErrors(String key, String errorMessage,
            List<ValidationError> validationErrorsList) {
        ValidationError validationError = new ValidationError(key, errorMessage);
        validationErrorsList.add(validationError);
        return validationErrorsList;
    }

    /**
     * 
     * Function to check if both 
     *      leftDataType and rightDataType
     *          are supported with current token
     *          
     * @param rightDataType
     * @param leftDataType
     * @param token
     * @return
     */

    protected boolean isDataTypeSupported(int rightDataType, int leftDataType, String token) {
        boolean isDataTypeSupport = false;
        Map<Integer, List<Integer>> dataTypeMap = ExpressionValidationConstants.operatorsDataTypeMap.get(token);

        if (null != dataTypeMap) {
            List<Integer> supportedDataTypes = dataTypeMap.get(leftDataType);

            if (null != supportedDataTypes && supportedDataTypes.size() > 0) {
                if (supportedDataTypes.contains(rightDataType)) {
                    isDataTypeSupport = true;
                }
            }
        }
        return isDataTypeSupport;
    }

    /**
     * Builds the compound parameter expression.
     *
     * @param parameterExpression the parameter expression
     * @param map the map
     * @param isStrictMode the is strict mode
     * @return the string
     */
    protected String buildCompoundParameterExpression(String parameterExpression, Map<Object, Object> map,
            boolean isStrictMode) {
        StringBuilder expression = new StringBuilder();
        String paramVal = "";

        // splitting expression with space - assuming that expression can have only brackets, and arithmetic and relative
        // operator and parameter id.
        String[] tokens = parameterExpression.split(" ");
        if (tokens != null && tokens.length > 0) {
            int i = 0;

            for (String token : tokens) {
                token = token.trim();
                // if token is bracket and operator
                if (token.equals(RuleConstants.LEFT_PAREN) || token.equals(RuleConstants.RIGHT_PAREN)
                        || commaDelimitesString(ExpressionValidationConstants.ARITHMETIC_OPS).indexOf(token) != -1) {
                    expression.append(token).append(" ");

                } else {
                    Parameter parameter = entityDao.find(Parameter.class, Long.parseLong(token));
                    if (parameter != null) {
                        parameter = HibernateUtils.initializeAndUnproxy(parameter);
                        paramVal = buildParameterExpressionToCompile(parameter, map, isStrictMode);
                        if (!(parameter instanceof ConstantParameter || parameter instanceof ReferenceParameter
                                || parameter instanceof NullParameter || parameter instanceof SystemParameter || parameter instanceof CompoundParameter)) {
                            paramVal = addNullCheckParams(tokens, i, paramVal);
                        }
                        BaseLoggers.flowLogger
                                .info("BaseRuleServiceImpl CLass :: Method buildCompoundParameterExpression:: paramVal = "
                                        + paramVal);
                        expression.append(paramVal).append(" ");
                    }
                }

                i++;
            }
        }
        if (expression.length() > 0) {
            return expression.toString();
        }
        return null;

    }

    /**
     * Builds the parameter expression to compile.
     *
     * @param parameter the parameter
     * @param map the map
     * @param isStrictMode the is strict mode
     * @return the string
     */
    protected String buildParameterExpressionToCompile(Parameter parameter, Map<Object, Object> map, boolean isStrictMode) {
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

        if (parameter instanceof ObjectGraphParameter) {
            String obj = ((ObjectGraphParameter) parameter).getObjectGraph();

            if (!isStrictMode) {

                if (((ObjectGraphParameter) parameter).getDataType() == ParameterDataType.PARAMETER_DATA_TYPE_DATE)
                    parameterKey = "( ?" + RulesConverterUtility.getNullSafeObjectGraph(obj)
                            + RuleConstants.RULE_TIME_IN_MILLIS + " )";
                else if(((ObjectGraphParameter) parameter).getDataType() == ParameterDataType.PARAMETER_DATA_TYPE_JAVA_UTIL_DATE) {
                	parameterKey = "( ?" + RulesConverterUtility.getNullSafeObjectGraph(obj)
                    + RuleConstants.RULE_TIME_FOR_JAVA_UTIL_DATE + " )";
                }
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
                            MVEL.eval(queryAttribute.getObjectGraph(), map));
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

        }

        else if (parameter instanceof CompoundParameter) {
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

        }else if(parameter instanceof SQLParameter){
            Map<String, Object> resultMap = sqlRuleExecutor.getParameterValue((SQLParameter)parameter,map);
            Object result = resultMap.get(RuleConstants.SQL_PARAM_RESULT_FOUND);
            if(result!=null){
                map.put(parameterKey,result);
            }
        }


        return parameterKey;
    }

    /**
     * 
     * Method to add null check params
     * @param nullCheckParams
     * @param expression
     * @param currentPos
     * @param paramVal
     */

    protected String addNullCheckParams(String[] expression, int currentPos, String paramVal) {

        boolean ognlChanged = false;

        if (currentPos + 1 < expression.length) {
            if ((")").indexOf(expression[currentPos + 1]) != -1) {
                if (currentPos + 2 < expression.length) {
                    if (commaDelimitesString(ExpressionValidationConstants.ARITHMETIC_OPS_FOR_NULL_SAFE).indexOf(
                            expression[currentPos + 2]) != -1) {
                        paramVal = RuleExpressionMvelEvaluator.createNullSafeParameter(paramVal);
                        ognlChanged = true;
                    }
                }

            } else {
                if (commaDelimitesString(ExpressionValidationConstants.ARITHMETIC_OPS_FOR_NULL_SAFE).indexOf(
                        expression[currentPos + 1]) != -1) {
                    paramVal = RuleExpressionMvelEvaluator.createNullSafeParameter(paramVal);
                    ognlChanged = true;
                }
            }
        }

        if (!ognlChanged) {
            if (currentPos - 1 >= 0) {
                if (("(").indexOf(expression[currentPos - 1]) != -1) {
                    if (currentPos - 2 < expression.length) {
                        if (commaDelimitesString(ExpressionValidationConstants.ARITHMETIC_OPS_FOR_NULL_SAFE).indexOf(
                                expression[currentPos - 2]) != -1) {
                            paramVal = RuleExpressionMvelEvaluator.createNullSafeParameter(paramVal);
                        }
                    }

                } else {
                    if (commaDelimitesString(ExpressionValidationConstants.ARITHMETIC_OPS_FOR_NULL_SAFE).indexOf(
                            expression[currentPos - 1]) != -1) {
                        paramVal = RuleExpressionMvelEvaluator.createNullSafeParameter(paramVal);
                    }
                }

            }
        }

        return paramVal;

    }
    
    /**
     * This method generates Script Rule Evaluator in case it's not found in Cache.
     * 
     * @param scriptRule
     * @return
     * @throws EvalError
     */
    protected ScriptRuleEvaluator generateScriptRuleEvaluator(ScriptRule scriptRule) {
		
		return ruleCacheService.generateScriptRuleEvaluator(scriptRule);
	}
}
