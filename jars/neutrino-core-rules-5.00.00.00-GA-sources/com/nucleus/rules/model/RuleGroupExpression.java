package com.nucleus.rules.model;


/**
 * @author Nucleus Software Exports Limited
 * Represents a Rule Group Expression in the system.
 */

public class RuleGroupExpression extends ExpressionTree {

    // ~ Instance fields ============================================================================

    private Rule rules;

    /**
     * Get rules
     * @return
     */

    public Rule getRules() {
        return rules;
    }

    /**
     * Set Rules
     * @param rules
     */

    public void setRules(Rule rules) {
        this.rules = rules;
    }

    /**
     * 
     * @param op
     * @param rule
     * @param leftExp
     * @param rightExp
     */

    public RuleGroupExpression(String op, Rule rule, ExpressionTree leftExp, ExpressionTree rightExp) {
        super(op, leftExp, rightExp);
        this.rules = rule;
    }

    /**
     * default constructor
     */

    public RuleGroupExpression() {

        super();

    }

}