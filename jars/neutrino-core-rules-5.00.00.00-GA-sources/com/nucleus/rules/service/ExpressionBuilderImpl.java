package com.nucleus.rules.service;

import javax.inject.Named;

import org.apache.commons.lang3.StringUtils;

import com.nucleus.rules.model.Condition;
import com.nucleus.rules.model.Parameter;

/**
 * 
 * @author Nucleus Software Exports Limited
 * Expression Builder
 */

@Named(value = "expressionBuilder")
public class ExpressionBuilderImpl extends BaseRuleServiceImpl implements ExpressionBuilder {

    @Override
    public String buildParameterExpression(String parameterExpression) {
        StringBuilder expression = new StringBuilder();
        if (StringUtils.isNotBlank(parameterExpression)) {

            String[] tokens = parameterExpression.split(" ");
            if (tokens != null && tokens.length > 0) {
                for (String token : tokens) {
                    token = token.trim();
                    // if token is bracket and operator
                    if (token.equals(RuleConstants.LEFT_PAREN) || token.equals(RuleConstants.RIGHT_PAREN)
                            || commaDelimitesString(ExpressionValidationConstants.ARITHMETIC_OPS).indexOf(token) != -1) {
                        expression.append(token).append(" ");
                    } else {
                        Parameter parameter = entityDao.find(Parameter.class, Long.parseLong(token));
                        if (parameter != null) {
                            expression.append(
                                    RulesConverterUtility.replaceSpace(parameter.getName())
                                            + RuleConstants.PARAMETER_NAME_ID + parameter.getId()).append(" ");
                        }
                    }
                }
            }
            if (expression.length() > 0) {
                return expression.toString();
            }
        }
        return "";
    }

    @Override
    public String buildConditionExpression(String conditionExpression) {
        StringBuilder expression = new StringBuilder();

        if (StringUtils.isNotBlank(conditionExpression)) {
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
                            expression.append(
                                    RulesConverterUtility.replaceSpace(parameter.getName())
                                            + RuleConstants.PARAMETER_NAME_ID + parameter.getId()).append(" ");
                        }
                    }
                }
            }
            if (expression.length() > 0) {
                return expression.toString();
            }
        }
        return "";
    }

    @Override
    public String buildConditionLevelRuleExpression(String ruleExpression) {
        StringBuilder expression = new StringBuilder();
        if (StringUtils.isNotBlank(ruleExpression)) {
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
                                    RulesConverterUtility.replaceSpace(condition.getName())
                                            + RuleConstants.PARAMETER_NAME_ID + condition.getId()).append(" ");
                        }
                    }
                }
            }
            if (expression.length() > 0) {
                return expression.toString();
            }
        }
        return "";
    }
}
