package com.nucleus.rules.model;


/**
 * 
 * @author Nucleus Software Exports Limited
 * Represents a Condition Expression in the system.
 */

public class ConditionExpression extends ExpressionTree {

    // ~ Instance fields
    // ============================================================================

    private Parameter parameter;

    /**
     * 
     * getter for parameter
     * @return
     */

    public Parameter getParameter() {
        return parameter;
    }

    /**
     * 
     * setter for parameter
     * @param parameter
     */

    public void setParameter(Parameter parameter) {
        this.parameter = parameter;
    }

    /**
     * @param op
     * @param parameter
     * @param leftExp
     * @param rightExp
     */

    public ConditionExpression(String op, Parameter parameter, ExpressionTree leftExp, ExpressionTree rightExp) {
        super(op, leftExp, rightExp);
        this.parameter = parameter;
    }

    /**
     * 
     * Default constructor
     */

    public ConditionExpression() {

        super();

    }

}