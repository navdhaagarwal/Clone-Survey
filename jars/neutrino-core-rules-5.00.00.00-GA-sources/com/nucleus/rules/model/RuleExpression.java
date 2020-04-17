package com.nucleus.rules.model;


/**
 * Represents a Rule Expression in the system.
 */
public class RuleExpression extends ExpressionTree {

    // ~ Instance fields ============================================================================

    private Condition conditions;

    /**
     * @return conditions
     */
    public Condition getConditions() {
        return conditions;
    }

    /**
     * 
     * set conditions
     * @param conditions
     */

    public void setConditions(Condition conditions) {
        this.conditions = conditions;
    }

    /**
     * 
     * @param op
     * @param conditions
     * @param leftExp
     * @param rightExp
     */
    public RuleExpression(String op, Condition conditions, ExpressionTree leftExp, ExpressionTree rightExp) {
        super(op, leftExp, rightExp);
        this.conditions = conditions;
    }

    /**
     * default constructor
     */

    public RuleExpression() {
        super();

    }

}