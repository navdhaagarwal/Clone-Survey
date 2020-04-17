package com.nucleus.rules.service;

import javax.inject.Named;

import org.apache.commons.lang3.StringUtils;

import com.nucleus.rules.model.CompoundParameter;
import com.nucleus.rules.model.Condition;
import com.nucleus.rules.model.Parameter;

/**
 * 
 * @author Nucleus Software Exports Limited
 */
@Named(value = "ruleSentenceBuilderService")
public class RuleSentenceBuilderServiceImpl extends BaseRuleServiceImpl implements RuleSentenceBuilderService {

    /**
     * 
     * Method to convert the given operators to easy understandable form 
     * @param operator
     * @return
     */
    public static String simpleRuleSentence(String operator) {

        if (RuleConstants.operatorToEnglish.get(operator) != null) {

            return RuleConstants.operatorToEnglish.get(operator);

        } else {

            return operator;
        }
    }

    @Override
    public String buildCompoundSentence(String parameterExpression) {
        StringBuilder expression = new StringBuilder();

        if (StringUtils.isNotBlank(parameterExpression)) {
            String[] tokens = parameterExpression.split(" ");
            if (tokens != null && tokens.length > 0) {
                for (String token : tokens) {
                    token = token.trim();
                    // if token is bracket and operator
                    if (token.equals(RuleConstants.LEFT_PAREN) || token.equals(RuleConstants.RIGHT_PAREN)) {
                        expression.append(token).append(" ");

                    } else if (commaDelimitesString(ExpressionValidationConstants.ARITHMETIC_OPS).indexOf(token) != -1
                            || commaDelimitesString(ExpressionValidationConstants.REL_OPS).indexOf(token) != -1) {

                        expression.append(simpleRuleSentence(token)).append(" ");

                    } else {
                        Parameter parameter = entityDao.find(Parameter.class, Long.parseLong(token));

                        if (parameter instanceof CompoundParameter) {

                            expression.append(" "
                                    + buildCompoundSentence(((CompoundParameter) parameter).getParameterExpression()));
                        } else {
                            expression.append("  <b>" + parameter.getName() + "</b> ");
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
    public String buildConditionSentence(String conditionExpression) {
        StringBuilder expression = new StringBuilder();

        if (StringUtils.isNotBlank(conditionExpression)) {

            String[] tokens = conditionExpression.split(" ");
            if (tokens != null && tokens.length > 0) {
                for (String token : tokens) {
                    token = token.trim();
                    // if token is bracket and operator
                    if (token.equals(RuleConstants.LEFT_PAREN) || token.equals(RuleConstants.RIGHT_PAREN)) {
                        expression.append(token).append(" ");

                    } else if (commaDelimitesString(ExpressionValidationConstants.ARITHMETIC_OPS).indexOf(token) != -1
                            || commaDelimitesString(ExpressionValidationConstants.REL_OPS).indexOf(token) != -1) {

                        expression.append(simpleRuleSentence(token)).append(" ");

                    } else {
                        Parameter parameter = entityDao.find(Parameter.class, Long.parseLong(token));
                        expression.append("  <b>" + parameter.getName() + "</b> ");
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
    public String buildRuleSentence(String ruleExpression) {
        StringBuilder expression = new StringBuilder();

        if (StringUtils.isNotBlank(ruleExpression)) {
            String[] tokens = ruleExpression.split(" ");
            if (tokens != null && tokens.length > 0) {
                for (String token : tokens) {
                    token = token.trim();
                    // if token is bracket and operator
                    if (token.equals(RuleConstants.LEFT_PAREN) || token.equals(RuleConstants.RIGHT_PAREN)) {
                        expression.append(token).append(" ");

                    } else if (commaDelimitesString(ExpressionValidationConstants.RULE_OPERATORS).indexOf(token) != -1) {

                        expression.append(simpleRuleSentence(token)).append(" <br/> ");

                    } else {
                        Condition condition = entityDao.find(Condition.class, Long.parseLong(token));
                        if (condition != null) {
                            expression.append(buildConditionSentence(condition.getConditionExpression())).append(" ");
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
