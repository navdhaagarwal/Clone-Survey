package com.nucleus.rules.model;

public class ParameterExpression extends ExpressionTree {

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
     * 
     * @param op
     * @param parameter
     * @param leftExp
     * @param rightExp
     */

    public ParameterExpression(String op, Parameter parameter, ExpressionTree leftExp, ExpressionTree rightExp) {
        super(op, leftExp, rightExp);
        this.parameter = parameter;
    }

    /**
     * 
     * default constructor
     */

    public ParameterExpression() {
        super();
    }

}
