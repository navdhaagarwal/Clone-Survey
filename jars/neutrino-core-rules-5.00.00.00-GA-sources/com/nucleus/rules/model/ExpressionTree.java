package com.nucleus.rules.model;


/**
 * 
 * @author Nucleus Software Exports Limited
 * Represents a Expression in the system.
 */

public class ExpressionTree {

    // ~ Instance fields
    // ============================================================================

    private String         operator;

    private ExpressionTree leftExpression;

    private ExpressionTree rightExpression;

    /**
     * 
     * default constructor
     */

    public ExpressionTree() {

    }

    /**
     * 
     * getter for operator
     * @return
     */

    public String getOperator() {
        return operator;
    }

    /**
     * 
     * setter for operator
     * @param operator
     */

    public void setOperator(String operator) {
        this.operator = operator;
    }

    /**
     * 
     * getter for leftExpression
     * @return
     */

    public ExpressionTree getLeftExpression() {
        return leftExpression;
    }

    /**
     * 
     * setter for leftExpression
     * @param leftExpression
     */

    public void setLeftExpression(ExpressionTree leftExpression) {
        this.leftExpression = leftExpression;
    }

    /**
     * 
     * getter for rightExpression
     * @return
     */

    public ExpressionTree getRightExpression() {
        return rightExpression;
    }

    /**
     * 
     * setter for rightExpression
     * @param rightExpression
     */

    public void setRightExpression(ExpressionTree rightExpression) {
        this.rightExpression = rightExpression;
    }

    /**
     * 
     * @param op
     * @param leftExp
     * @param rightExp
     */

    public ExpressionTree(String op, ExpressionTree leftExp, ExpressionTree rightExp) {
        this.operator = op;
        this.leftExpression = leftExp;
        this.rightExpression = rightExp;

    }
}