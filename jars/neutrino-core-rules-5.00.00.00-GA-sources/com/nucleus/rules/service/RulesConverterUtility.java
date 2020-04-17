package com.nucleus.rules.service;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Stack;

import com.nucleus.core.exceptions.InvalidDataException;
import com.nucleus.rules.exception.RuleException;
import com.nucleus.rules.model.ParameterDataType;

public class RulesConverterUtility {

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
    public static int cmpPrecedence(String token1, String token2) {
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

    public static boolean isOperator(String token) {
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
    public static boolean isAssociative(String token, int type) {
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
    public static ArrayList<String> infixToRPN(String[] inputTokens) {
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

    public static String commaDelimitesString(String[] list) {
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
     * Method to convert space to _ in parameter name, condition name or rule name
     * @param name
     * @return
     */

    public static String replaceSpace(String name) {

        if (!(name.equals(""))) {
            String newParameterName = "";

            if ((RuleConstants.NUMERIC_CHARACTERS).indexOf(name.charAt(0)) != -1) {
                newParameterName = RuleConstants.REPLACE_SPACE_IN_NAME + name;
            } else {
                newParameterName = name;
            }
            newParameterName = newParameterName.replaceAll("-", RuleConstants.REPLACE_SPACE_IN_NAME);
            newParameterName = newParameterName.trim().replaceAll("\\s", RuleConstants.REPLACE_SPACE_IN_NAME);
            return newParameterName;
        } else {
            return "";
        }
    }

    public static Long getIdFromName(Object name) {
        String parametername = (String) name;
        int beginIndex = parametername.indexOf(RuleConstants.PARAMETER_NAME_ID);
        int endIndex = parametername.length();
        String Id = parametername.substring(beginIndex + 1, endIndex);
        return Long.parseLong(Id);
    }

    public static String getQueryRuleOperator(String operator) {
        if (operator.equals(RuleConstants.AND_OPERATOR)) {
            return RuleConstants.AND_OPERATOR_ENGLISH;
        } else if (operator.equals(RuleConstants.OR_OPERATOR)) {
            return RuleConstants.OR_OPERATOR_ENGLISH;
        }
        return operator;
    }

    public static String getNullSafeObjectGraph(String objectGraph) {

        if (null != objectGraph) {
            return objectGraph.replaceAll("\\.", ".?");
        }

        return objectGraph;
    }

    public static Object convertValuetoActualObject(String value, int dataType) {

        try {
            if (dataType == ParameterDataType.PARAMETER_DATA_TYPE_BOOLEAN) {
                return Boolean.parseBoolean(value);

            } else if (dataType == ParameterDataType.PARAMETER_DATA_TYPE_DATE 
            		|| dataType ==  ParameterDataType.PARAMETER_DATA_TYPE_JAVA_UTIL_DATE) {
                DateFormat formatter;
                Date date;
                formatter = new SimpleDateFormat(RuleConstants.DATE_PATTERN);
                date = formatter.parse(value);
                return date.getTime();

            } else if (dataType == ParameterDataType.PARAMETER_DATA_TYPE_INTEGER
                    || dataType == ParameterDataType.PARAMETER_DATA_TYPE_NUMBER) {
                return new BigDecimal(value).doubleValue();

            } else if (dataType == ParameterDataType.PARAMETER_DATA_TYPE_STRING) {
                return value;

            } else if (dataType == ParameterDataType.PARAMETER_DATA_TYPE_REFERENCE) {
                return Long.parseLong(value);

            }
        } catch (Exception e) {
            throw new RuleException("Cannot evaluate value :: " + value + " with datatype ::" + dataType, e);
        }

        return null;
    }
}
