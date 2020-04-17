package com.nucleus.rules.service;

import java.util.List;
import java.util.Stack;

import javax.inject.Named;

import com.nucleus.entity.CloneOptionConstants;
import com.nucleus.rules.model.CompoundParameter;
import com.nucleus.rules.model.Condition;
import com.nucleus.rules.model.ConditionExpression;
import com.nucleus.rules.model.Parameter;
import com.nucleus.rules.model.ParameterExpression;
import com.nucleus.rules.model.Rule;
import com.nucleus.rules.model.RuleExpression;
import com.nucleus.rules.model.RuleGroupExpression;

/**
 * 
 * @author Nucleus Software Exports Limited
 * This class will be used to build the flat expressions
 */

@Named("ruleExpressionBuilder")
public class RuleExpressionBuilderImpl extends RuleServiceImpl implements RuleExpressionBuilder {

    @Override
    public String buildRuleLevelRuleExpression(String ruleGroupExpression) {
        // assuming rulegroupExpression can have only bracket, and or operator and rule id.

        if (null != ruleGroupExpression) {

            StringBuilder expression = new StringBuilder();
            String[] tokens = ruleGroupExpression.split("\\s+");
            if (tokens != null && tokens.length > 0) {
                for (String token : tokens) {
                    token = token.trim();
                    if (token.equals(RuleConstants.LEFT_PAREN) || token.equals(RuleConstants.RIGHT_PAREN)
                            || RuleConstants.LOG_OPERATORS.containsKey(token)) {
                        expression.append(token).append(" ");
                    } else {
                        Rule rule = entityDao.find(Rule.class, Long.parseLong(token));
                        if (rule != null) {
                            expression.append(
                                    RulesConverterUtility.replaceSpace(rule.getName()) + RuleConstants.PARAMETER_NAME_ID
                                            + rule.getId()).append(" ");
                        }
                    }
                }
            }
            if (expression.length() > 0) {
                return expression.toString();
            }
        }
        return null;
    }

    @Override
    public RuleGroupExpression parseRuleGroupFlatExpression(String exp) {
        String[] input = exp.split("\\s+");
        List<String> output = infixToRPN(input);

        Stack<RuleGroupExpression> treeStack = new Stack<RuleGroupExpression>();
        RuleGroupExpression rtExp = null;
        RuleGroupExpression ltExp = null;

        int i = 0;
        for (i = 0 ; i < output.size() ; i++) {
            String token = output.get(i);
            if (RuleConstants.LOG_OPERATORS.containsKey(token)) {

                rtExp = treeStack.pop();
                ltExp = treeStack.pop();

                treeStack.push(new RuleGroupExpression(token, null, ltExp, rtExp));
            }

            else {
                // setting rule expression before pushing into stack
                Rule rule = getRule(Long.parseLong(output.get(i).toString()));
                Rule tempRule = new Rule();
                tempRule.setExpression(parseRuleExpression(rule.getRuleExpression()));
                treeStack.push(new RuleGroupExpression(null, tempRule, null, null));
            }

            continue;
        }
        return treeStack.peek();
    }

    private ConditionExpression parseConditionExpression(String exp) {

        String[] input = exp.split("\\s+");
        List<String> output = infixToRPN(input);

        ConditionExpression rtExp = null;
        ConditionExpression ltExp = null;
        int i = 0;
        Stack<Object> treeStack = new Stack<Object>();
        for (i = 0 ; i < output.size() ; i++) {
            String token = output.get(i);
            if (RuleConstants.ARITH_OPERATORS.containsKey(token) || RuleConstants.REL_OPERATORS.containsKey(token)) {

                rtExp = (ConditionExpression) treeStack.pop();
                ltExp = (ConditionExpression) treeStack.pop();

                treeStack.push(new ConditionExpression(token, null, ltExp, rtExp));

            } else {
                Parameter parameter = getParameter(Long.parseLong(output.get(i).toString()));
                Parameter tempParameter = (Parameter) parameter.cloneYourself(CloneOptionConstants.COPY_CLONING_OPTION);
                if (parameter instanceof CompoundParameter) {
                    ((CompoundParameter) tempParameter)
                            .setExpression(parseParameterExpression(((CompoundParameter) parameter).getParameterExpression()));
                }
                treeStack.push(new ConditionExpression(null, tempParameter, null, null));
            }

        }
        return (ConditionExpression) treeStack.peek();
    }

    /**
     * Parse a Rule .
     * 
     * @param exp
     *         The Rule to be parsed .  
     * @return RuleExpression object.
     */

    private RuleExpression parseRuleExpression(String exp) {

        String[] input = exp.split("\\s+");
        List<String> output = infixToRPN(input);

        Stack<RuleExpression> treeStack = new Stack<RuleExpression>();
        RuleExpression rtExp = null;
        RuleExpression ltExp = null;

        int i = 0;
        for (i = 0 ; i < output.size() ; i++) {
            String token = output.get(i);
            if (RuleConstants.LOG_OPERATORS.containsKey(token)) {

                rtExp = treeStack.pop();
                ltExp = treeStack.pop();

                treeStack.push(new RuleExpression(token, null, ltExp, rtExp));
            }

            else {
                Condition condition = getCondition(Long.parseLong(output.get(i).toString()));
                Condition tempCondition = new Condition();
                tempCondition.setExpression(parseConditionExpression(condition.getConditionExpression()));
                treeStack.push(new RuleExpression(null, tempCondition, null, null));
            }

            continue;
        }
        return treeStack.peek();

    }

    /**
     * Parse a CondiParametertion .
     * 
     * @param exp
     *            The Parameter to be parsed .  
     * @return ParameterExpression object.
     */

    private ParameterExpression parseParameterExpression(String exp) {

        String[] input = exp.split("\\s+");
        List<String> output = infixToRPN(input);

        ParameterExpression rtExp = null;
        ParameterExpression ltExp = null;
        int i = 0;
        Stack<Object> treeStack = new Stack<Object>();
        for (i = 0 ; i < output.size() ; i++) {
            String token = output.get(i);
            if (RuleConstants.ARITH_OPERATORS.containsKey(token) || RuleConstants.REL_OPERATORS.containsKey(token)) {

                rtExp = (ParameterExpression) treeStack.pop();
                ltExp = (ParameterExpression) treeStack.pop();

                treeStack.push(new ParameterExpression(token, null, ltExp, rtExp));

            } else {
                Parameter parameter = getParameter(Long.parseLong(output.get(i).toString()));
                Parameter tempParameter = (Parameter) parameter.cloneYourself(CloneOptionConstants.COPY_CLONING_OPTION);
                if (parameter instanceof CompoundParameter) {
                    ((CompoundParameter) tempParameter)
                            .setExpression(parseParameterExpression(((CompoundParameter) parameter).getParameterExpression()));
                }
                treeStack.push(new ParameterExpression(null, tempParameter, null, null));
            }

        }
        return (ParameterExpression) treeStack.peek();

    }

}
