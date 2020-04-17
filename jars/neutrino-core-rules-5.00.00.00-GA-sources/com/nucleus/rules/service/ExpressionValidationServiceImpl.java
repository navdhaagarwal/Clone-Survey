package com.nucleus.rules.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import javax.inject.Inject;
import javax.inject.Named;

import com.nucleus.logging.BaseLoggers;
import com.nucleus.rules.exception.RuleValidationException;
import com.nucleus.rules.model.Condition;
import com.nucleus.rules.model.ConstantParameter;
import com.nucleus.rules.model.NullParameter;
import com.nucleus.rules.model.Parameter;
import com.nucleus.rules.model.ParameterDataType;
import com.nucleus.rules.model.Rule;

@Named(value = "expressionValidation")
public class ExpressionValidationServiceImpl extends BaseRuleServiceImpl implements ExpressionValidationService {

    @Inject
    @Named("ruleService")
    RuleService ruleService;

    @Override
    public List<ValidationError> validateCompoundParameterExpression(String expression, int dataType) {
        List<ValidationError> validationErrorsList = new ArrayList<ValidationError>();

        validationErrorsList = validateExpressionBrackets(expression, validationErrorsList);

        if (validationErrorsList.size() == 0) {

            String[] input = expression.split(" ");
            
            List<String> output = infixToRPN(input);

            try {
            	if (commaDelimitesString(ExpressionValidationConstants.ARITHMETIC_OPS).indexOf(input[0]) != -1 || 
            			commaDelimitesString(ExpressionValidationConstants.ARITHMETIC_OPS).indexOf(input[input.length-1]) != -1) {
            		 return setValidationErrors("label.invalid.expression.parameter", "", validationErrorsList);
            	}
                
                if (null != output && output.size() > 1) {

                    Parameter rightParameter = null;
                    Parameter leftParameter = null;
                    int i = 0;
                    Stack<Object> treeStack = new Stack<Object>();

                    int rightDataType = -1;
                    int leftDataType = -1;

                    boolean isDataTypeSupport = false;

                    for (i = 0 ; i < output.size() ; i++) {
                        String token = output.get(i);
                        if (commaDelimitesString(ExpressionValidationConstants.ARITHMETIC_OPS).indexOf(token) != -1) {

                            // If the token is of operator type

                            rightParameter = (Parameter) treeStack.pop();
                            leftParameter = (Parameter) treeStack.pop();

                            rightDataType = rightParameter.getDataType();
                            leftDataType = leftParameter.getDataType();

                            isDataTypeSupport = isDataTypeSupported(rightDataType, leftDataType, token);

                            if (isDataTypeSupport) {

                                // paramDataType is the resultant dataType that has to be used

                                try {
                                    Parameter resultantParameter = getResultantParameter(rightDataType, leftDataType, token);
                                    treeStack.push(resultantParameter);
                                } catch (RuleValidationException e) {
                                    BaseLoggers.exceptionLogger
                                            .debug("Mismatch of Selected operator and Parameter DataType " + e);
                                    return setValidationErrors("label.invalid.expression.operator", "", validationErrorsList);
                                }
                            } else {
                                return setValidationErrors("label.invalid.expression.operator.mismatch", "",
                                        validationErrorsList);
                            }

                        } else {
                            // If the token is of parameter type
                            treeStack.push(getParameter(token));
                        }

                    }
                    if (null != treeStack && treeStack.size() == 1) {
                        Parameter resultParam = (Parameter) treeStack.pop();
                        if (resultParam.getDataType() == dataType) {
                            return validationErrorsList;
                        } else {
                            return setValidationErrors("label.invalid.expression.selectedDataType.error", "",
                                    validationErrorsList);
                        }

                    } else {
                        return setValidationErrors("label.invalid.expression.parameter", "", validationErrorsList);
                    	}
                	}
            	else {
                    return setValidationErrors("label.invalid.expression.parameter", "", validationErrorsList);
                }
           } catch (Exception e) {
                BaseLoggers.exceptionLogger.debug("Invalid Parameter Expression" + e);
                return setValidationErrors("label.invalid.expression.parameter", "", validationErrorsList);
            }
        } else {
            return validationErrorsList;
        }
    }

    @Override
    public List<ValidationError> validateConditionExpression(String expression) {

        List<ValidationError> validationErrorsList = new ArrayList<ValidationError>();

        validationErrorsList = validateExpressionBrackets(expression, validationErrorsList);

        if (validationErrorsList.size() == 0) {

            String[] input = expression.split(" ");
            List<String> output = infixToRPN(input);

            try {
                if (null != output && output.size() > 1) {
                    Parameter rightParameter = null;
                    Parameter leftParameter = null;
                    int i = 0;

                    int rightDataType = -1;
                    int leftDataType = -1;
                    boolean isDataTypeSupport = false;

                    int resultantParamDataType = -1;

                    Stack<Object> treeStack = new Stack<Object>();
                    for (i = 0 ; i < output.size() ; i++) {
                        String token = output.get(i);
                        if (commaDelimitesString(ExpressionValidationConstants.ARITHMETIC_OPS).indexOf(token) != -1
                                || commaDelimitesString(ExpressionValidationConstants.REL_OPS).indexOf(token) != -1) {

                            rightParameter = (Parameter) treeStack.pop();
                            leftParameter = (Parameter) treeStack.pop();

                            rightDataType = rightParameter.getDataType();
                            leftDataType = leftParameter.getDataType();

                            /**
                             * Logic to check
                             * Null Parameter is not allowed in the following cases
                             *          If the other Parameter is COntsnat nad its data type is boolean
                             *          If the resultant data type is boolean
                             *          Should be allowed when it is field parameter of Boolean type
                             *          Should be allowed when it is field parameter of any other data type
                             */
                            if (rightParameter instanceof NullParameter || leftParameter instanceof NullParameter) {
                                if ((rightParameter instanceof NullParameter && (leftParameter.getDataType() == ParameterDataType.PARAMETER_DATA_TYPE_BOOLEAN
                                        && leftParameter.getParamType() == null || (leftParameter instanceof ConstantParameter && leftParameter
                                        .getDataType() == ParameterDataType.PARAMETER_DATA_TYPE_BOOLEAN)))
                                        || (leftParameter instanceof NullParameter
                                                && (rightParameter.getDataType() == ParameterDataType.PARAMETER_DATA_TYPE_BOOLEAN && rightParameter
                                                        .getParamType() == null) || (rightParameter instanceof ConstantParameter && rightParameter
                                                .getDataType() == ParameterDataType.PARAMETER_DATA_TYPE_BOOLEAN))) {
                                    return setValidationErrors("label.invalid.expression.condition", "",
                                            validationErrorsList);

                                } else {
                                    if (commaDelimitesString(ExpressionValidationConstants.NULL_CONDITION_OPERATORS)
                                            .indexOf(token) != -1) {
                                        resultantParamDataType = ParameterDataType.PARAMETER_DATA_TYPE_BOOLEAN;
                                    } else {
                                        return setValidationErrors("label.invalid.expression.condition", "",
                                                validationErrorsList);
                                    }
                                }
                            } else {
                                isDataTypeSupport = isDataTypeSupported(rightDataType, leftDataType, token);
                                if (isDataTypeSupport) {
                                    try {
                                        resultantParamDataType = getResultantParamDataType(leftDataType, rightDataType,
                                                token);
                                    } catch (RuleValidationException e) {
                                        BaseLoggers.exceptionLogger.debug("Invalid Operator used" + e);
                                        return setValidationErrors("label.invalid.expression.operator", "",
                                                validationErrorsList);
                                    }
                                } else {
                                    return setValidationErrors("label.invalid.expression.operator.mismatch", "",
                                            validationErrorsList);
                                }
                            }

                            Parameter resultantParameter = new Parameter();
                            resultantParameter.setDataType(resultantParamDataType);
                            treeStack.push(resultantParameter);
                        } else {
                            treeStack.push(getParameter(token));
                        }
                    }

                    if (null != treeStack && treeStack.size() == 1) {
                        if (((Parameter) treeStack.peek()).getDataType() == ParameterDataType.PARAMETER_DATA_TYPE_BOOLEAN) {
                            return validationErrorsList;
                        } else {
                            return setValidationErrors("label.invalid.expression.condition.result", "", validationErrorsList);
                        }
                    } else {
                        return setValidationErrors("label.invalid.expression.condition", "", validationErrorsList);
                    }

                } else {
                    return setValidationErrors("label.invalid.expression.condition", "", validationErrorsList);
                }
            } catch (Exception e) {
                BaseLoggers.exceptionLogger.debug("Invalid Condition Expression" + e);
                return setValidationErrors("label.invalid.expression.condition", "", validationErrorsList);
            }
        } else {
            return validationErrorsList;
        }

    }

    @Override
    public List<ValidationError> validateRule(String expression) {

        List<ValidationError> validationErrorsList = new ArrayList<ValidationError>();

        try {

            validationErrorsList = validateExpressionBrackets(expression, validationErrorsList);

            if (validationErrorsList.size() == 0) {
                validationErrorsList = verifyRuleOperators(expression, validationErrorsList);
            } else {
                return validationErrorsList;
            }

        } catch (Exception e) {
            BaseLoggers.exceptionLogger.debug("Invalid Rule Expression" + e);
            return setValidationErrors("label.invalid.expression.rule", "", validationErrorsList);
        }
        return validationErrorsList;
    }

    @Override
    public List<ValidationError> validateRuleGroup(String expression) {
        List<ValidationError> validationErrorsList = new ArrayList<ValidationError>();
        try {
            validationErrorsList = validateExpressionBrackets(expression, validationErrorsList);

            if (validationErrorsList.size() == 0) {
                validationErrorsList = verifyRuleGroupOperators(expression, validationErrorsList);
            } else {
                return validationErrorsList;
            }

        } catch (Exception e) {
            BaseLoggers.exceptionLogger.debug("Invalid Rule Expression" + e);
            return setValidationErrors("label.invalid.expression.rule", "", validationErrorsList);
        }
        return validationErrorsList;
    }

    /**
     * 
     * Function to get the 
     *      Resultant data type
     *  @param leftDataType
     *  @param rightDataType
     *  @param token
     *  @return
     */
    private int getResultantParamDataType(int leftDataType, int rightDataType, String token) {

        int resultDataType = -1;

        if (ExpressionValidationConstants.REL_OPERATORS.containsKey(token)) {
            resultDataType = ExpressionValidationConstants.REL_OPERATORS.get(token);

        } else if (ExpressionValidationConstants.operatorsDataTypeResultMap.containsKey(token)) {
            Map<Integer, Integer> resultDataTypeMap = ExpressionValidationConstants.operatorsDataTypeResultMap.get(token);
            if (null != resultDataTypeMap) {
                resultDataType = resultDataTypeMap.get(leftDataType);
            }
    
        } 
        else if(ExpressionValidationConstants.LOGICAL_OPERATORS.containsKey(token))
        {
            resultDataType = ExpressionValidationConstants.LOGICAL_OPERATORS.get(token);
        }
        
        else {
            throw new RuleValidationException("Invalid Operator");
        }
        return resultDataType;

    }

    /**
     * 
     * select parameter based on id
     * @param token
     * @return
     */

    private Parameter getParameter(String token) {
        return getParameter(Long.valueOf(token));
    }

    /**
     * Parse a Rule .
     * 
     * @param exp
     *         The Rule to be parsed .  
     * @return RuleExpression object.
     */
    private List<ValidationError> verifyRuleOperators(String exp, List<ValidationError> validationErrorsList) {

        String[] input = exp.split(" ");
        List<String> output = infixToRPN(input);

        Stack<Condition> treeStack = new Stack<Condition>();
        Condition rightCondition = null;
        Condition leftCondition = null;

        int i = 0;
        for (i = 0 ; i < output.size() ; i++) {
            String token = output.get(i);
            if (commaDelimitesString(ExpressionValidationConstants.RULE_OPERATORS).indexOf(token) != -1) {

                rightCondition = treeStack.pop();
                leftCondition = treeStack.pop();

                treeStack.push(new Condition());
            } else {
                treeStack.push(new Condition());
            }
            continue;
        }

        if (null != treeStack && treeStack.size() == 1) {
            return validationErrorsList;
        } else {
            return setValidationErrors("label.invalid.expression.rule", "", validationErrorsList);
        }
    }

    /**
     * 
     * Verify Rule Group
     * @param exp
     * @return
     */
    private List<ValidationError> verifyRuleGroupOperators(String exp, List<ValidationError> validationErrorsList) {

        String[] input = exp.split(" ");
        List<String> output = RulesConverterUtility.infixToRPN(input);

        Stack<Rule> treeStack = new Stack<Rule>();
        Rule rightRule = null;
        Rule leftRule = null;

        int i = 0;
        for (i = 0 ; i < output.size() ; i++) {
            String token = output.get(i);
            if (RulesConverterUtility.commaDelimitesString(ExpressionValidationConstants.RULE_OPERATORS).indexOf(token) != -1) {

                rightRule = treeStack.pop();
                leftRule = treeStack.pop();

                treeStack.push(new Rule());
            } else {
                treeStack.push(new Rule());
            }

            continue;
        }

        if (null != treeStack && treeStack.size() == 1) {
            return validationErrorsList;
        } else {
            return setValidationErrors("label.invalid.expression.rule", "", validationErrorsList);
        }
    }

    @Override
    public List<ValidationError> validateMvelScriptParameter(String expression) {

        List<ValidationError> validationErrorsList = new ArrayList<ValidationError>();

        validationErrorsList = validateExpressionBrackets(expression, validationErrorsList);

        if (validationErrorsList.size() == 0) {

            String[] input = expression.split(" ");
            List<String> output = infixToRPN(input);

            try {
                if (null != output && output.size() > 1) {

                    Parameter rightParameter = null;
                    Parameter leftParameter = null;
                    int i = 0;
                    Stack<Object> treeStack = new Stack<Object>();

                    int rightDataType = -1;
                    int leftDataType = -1;

                    boolean isDataTypeSupport = false;

                    for (i = 0 ; i < output.size() ; i++) {
                        String token = output.get(i);
                        if (token == null || token.isEmpty()) {
                            continue;
                        }
                        if (commaDelimitesString(ExpressionValidationConstants.REL_OPS).indexOf(token) != -1) {

                            // If the token is of operator type

                            rightParameter = (Parameter) treeStack.pop();
                            leftParameter = (Parameter) treeStack.pop();

                            rightDataType = rightParameter.getDataType();
                            leftDataType = leftParameter.getDataType();

                            isDataTypeSupport = isDataTypeSupported(rightDataType, leftDataType, token);

                            if (isDataTypeSupport) {

                                // paramDataType is the resultant dataType that has to be used

                                try {
                                    Parameter resultantParameter = getResultantParameter(rightDataType, leftDataType, token);
                                    treeStack.push(resultantParameter);
                                } catch (RuleValidationException e) {
                                    BaseLoggers.exceptionLogger
                                            .debug("Mismatch of Selected operator and Parameter DataType " + e);
                                    return setValidationErrors("label.invalid.expression.operator", "", validationErrorsList);
                                }
                            } else {
                                return setValidationErrors("label.invalid.expression.operator.mismatch", "",
                                        validationErrorsList);
                            }

                        } else if (commaDelimitesString(
                                ExpressionValidationConstants.SUPPORTED_CONDITION_JOIN_OPERATORS_MVEL_SCRIPT).indexOf(token) != -1) {
                            rightParameter = (Parameter) treeStack.pop();
                            leftParameter = (Parameter) treeStack.pop();

                            rightDataType = rightParameter.getDataType();
                            leftDataType = leftParameter.getDataType();
                            if (rightDataType == ParameterDataType.PARAMETER_DATA_TYPE_BOOLEAN
                                    && leftDataType == ParameterDataType.PARAMETER_DATA_TYPE_BOOLEAN) {
                                Parameter resultantParameter = getResultantParameter(rightDataType, leftDataType, token);
                                treeStack.push(resultantParameter);

                            } else {
                                return setValidationErrors("label.invalid.expression.parameter.joinoperator", "",
                                        validationErrorsList);
                            }
                        } else {
                            // If the token is of parameter type
                            treeStack.push(ruleService.getParameter(Long.parseLong(token)));
                        }

                    }
                    if (null != treeStack && treeStack.size() == 1) {
                        Parameter resultParam = (Parameter) treeStack.pop();
                        if (resultParam.getDataType() == ParameterDataType.PARAMETER_DATA_TYPE_BOOLEAN) {
                            return validationErrorsList;
                        } else {
                            return setValidationErrors("label.invalid.expression.selectedDataType.error", "",
                                    validationErrorsList);
                        }

                    } else {
                        return setValidationErrors("label.invalid.expression.parameter", "", validationErrorsList);
                    }
                } else {
                    return setValidationErrors("label.invalid.expression.parameter", "", validationErrorsList);
                }
            } catch (Exception e) {
                BaseLoggers.exceptionLogger.debug("Invalid Parameter Expression" + e);
                return setValidationErrors("label.invalid.expression.parameter", "", validationErrorsList);
            }
        } else {
            return validationErrorsList;
        }
    }

    private Parameter getResultantParameter(int rightDataType, int leftDataType, String token) {
        int paramDataType = getResultantParamDataType(leftDataType, rightDataType, token);
        Parameter resultantParameter = new Parameter();
        resultantParameter.setDataType(paramDataType);
        return resultantParameter;
    }

}
