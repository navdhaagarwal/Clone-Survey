/**
 * This file and a proportion of its content is copyright of Nucleus Software Exports Limited - © 2012. All rights reserved.
 * Any redistribution or reproduction of part or all of the contents in any form is prohibited other than the following:
 * - you cannot print or download to a local hard disk extract contents either part or full for personal/ commercial/
 * academic or any other use
 * - you may not copy the content to individual/ third parties for any type of use, either as compiled or source format
 * without the knowledge and consent of Nucleus Software
 * - You may not, except with our express written permission, distribute or commercially exploit the content. Nor may you
 * transmit it or store it in any other web site or other form of electronic retrieval system.
 */
package com.nucleus.rules.service;

import javax.inject.Named;

import org.apache.commons.lang3.StringUtils;
import org.mvel2.MVEL;

import com.nucleus.service.BaseServiceImpl;

/**
 * Service To Patch Pattern Of Applilcation And Result String
 * Each character of the Application pattern and results pattern will 
 * be converted to binary character and bitwise operator will be applied on them, 
 * the bitwise operator will also be decided based on the pattern characters. 
 * A binary string will be prepared for all the result patterns and the string with 
 * all the 1s will be considered as match and highest priority of match string will be returned.   
 * @author Nucleus Software Exports Limited
 */
@Named(value = "patternMatchAlgorithmService")
public class PatternMatchAlgorithmServiceImpl extends BaseServiceImpl implements PatternMatchAlgorithmService  {

    
    /**
     * Method to match the application and
     * result pattern
     * @param applicationPattern
     * @param resultPattern
     * @return
     */
    public boolean matchPattern(String applicationPattern, String resultPattern) {
        boolean isMatched = true;
        for (int i = 0 ; i < applicationPattern.length() ; i++) {
            //Application Pattern
            ExpressionOperandAndOperator appPatternOperandAndOp = getApplicationPatternExpressionValues(Character
                    .toString(applicationPattern.charAt(i)));
            //Result Pattern
            ExpressionOperandAndOperator resultPatternOperandAndOp = getResultPatternExpressionValues(Character
                    .toString(resultPattern.charAt(i)));
            String expression = "";
            if (resultPatternOperandAndOp.getOperator() == null) {
                if (appPatternOperandAndOp.getOperator().equalsIgnoreCase("^")) {
                    /*
                     * XNOR GATE (For XNOR GATE : Append "^1" with the expression (a^b) when computing with int values)
                     */
                    expression = "(" + appPatternOperandAndOp.getOperand() + appPatternOperandAndOp.getOperator()
                            + resultPatternOperandAndOp.getOperand() + "^ 1 ) == 1";
                } else {
                    /*
                     * AND GATE
                     */
                    expression = "(" + appPatternOperandAndOp.getOperand() + appPatternOperandAndOp.getOperator()
                            + resultPatternOperandAndOp.getOperand() + ") == 1";
                }
            } else {
                    /*
                     * OR GATE
                     */
                expression = "(" + appPatternOperandAndOp.getOperand() + resultPatternOperandAndOp.getOperator()
                        + resultPatternOperandAndOp.getOperand() + ") == 1";
            }
            /*
             * Evaluate the expression through MVEL
             */
            isMatched = (Boolean) MVEL.eval(expression);
            if (!isMatched) {
                return false;
            }
        }
        return true;
    }
    
    
    /**
     * Method To get Operand and operator
     * of application result string
     * @param character
     * @return
     */
    private ExpressionOperandAndOperator getApplicationPatternExpressionValues(String character) {
        if (character.equals(String.valueOf(RuleConstants.RULE_RESULT_PASS))) {
            ExpressionOperandAndOperator exp = new ExpressionOperandAndOperator();
            exp.setOperator("^");
            exp.setOperand(1);
            return exp;
        } else if (character.equals(String.valueOf(RuleConstants.RULE_RESULT_FAIL))) {
            ExpressionOperandAndOperator exp = new ExpressionOperandAndOperator();
            exp.setOperator("^");
            exp.setOperand(0);
            return exp;
        } else {
            /*
             * It is case of No Value
             */
            ExpressionOperandAndOperator exp = new ExpressionOperandAndOperator();
            exp.setOperator("&");
            exp.setOperand(0);
            return exp;
        }
    }

    /**
     * Method To get Operand And
     * Operator of result pattern string
     * @param character
     * @return
     */
    private ExpressionOperandAndOperator getResultPatternExpressionValues(String character) {
        if (character.equals(String.valueOf(RuleConstants.RULE_RESULT_PASS))) {
            ExpressionOperandAndOperator exp = new ExpressionOperandAndOperator();
            exp.setOperator(null);
            exp.setOperand(1);
            return exp;
        } else if (character.equals(String.valueOf(RuleConstants.RULE_RESULT_FAIL))) {
            ExpressionOperandAndOperator exp = new ExpressionOperandAndOperator();
            exp.setOperator(null);
            exp.setOperand(0);
            return exp;
        } else {
            /*
             * It is case of Wild card 
             */
            ExpressionOperandAndOperator exp = new ExpressionOperandAndOperator();
            exp.setOperator("|");
            exp.setOperand(1);
            return exp;
        }
    }
    
    
    /**
     * Match Pattern With Count
     * Of Characters Of Application String 
     * And Result String
     */
    public boolean matchPatternCount(String applicationPattern, String resultPatternCharacter,
            int countResultPatternOccurence) {
        int countAppPatternOccurence = 0;
        countAppPatternOccurence = StringUtils.countMatches(applicationPattern, resultPatternCharacter);
        if (countResultPatternOccurence == countAppPatternOccurence) {
            return true;
        }
        return false;
    }


    private static class ExpressionOperandAndOperator {
        private int    operand;
        private String operator;

        public int getOperand() {
            return operand;
        }

        public void setOperand(int operand) {
            this.operand = operand;
        }

        public String getOperator() {
            return operator;
        }

        public void setOperator(String operator) {
            this.operator = operator;
        }
    }



}
