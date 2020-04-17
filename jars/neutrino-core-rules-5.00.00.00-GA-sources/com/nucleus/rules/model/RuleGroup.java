package com.nucleus.rules.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.Cacheable;
import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.Lob;
import javax.persistence.Transient;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.nucleus.entity.ApprovalStatus;
import com.nucleus.entity.BaseEntity;
import com.nucleus.entity.CloneOptions;

/**
 * 
 * @author Nucleus Software Exports Limited
 * Class to create Rule Group
 */
@Entity
@DynamicUpdate
@DynamicInsert
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@Cacheable
public class RuleGroup extends RuleSet {

    private static final long         serialVersionUID = 3032950161687619591L;

    @Transient
    private List<RuleGroupExpression> expression;

    @Lob
    private String                    ruleGroupExpression;

    @Lob
    private Serializable              ruleLevelCompiledExpr;

    /**
     * @return expression
     */

    public RuleGroupExpression getExpression() {
        if (expression == null || expression.size() == 0) {
            return null;
        }
        return expression.get(0);
    }

    public String getRuleGroupExpression() {
        return ruleGroupExpression;
    }

    public void setRuleGroupExpression(String ruleGroupExpression) {
        this.ruleGroupExpression = ruleGroupExpression;
    }

    /**
     * 
     * @param name
     * @param expression
     */

    public RuleGroup(String name, RuleGroupExpression expression) {
        super();
        this.expression = new ArrayList<RuleGroupExpression>();
        this.expression.add(expression);
    }

    /**
     * 
     * Default constructor
     */

    public RuleGroup() {

    }

    /**
     * @return the ruleLevelCompiledExpr
     */
    public Serializable getRuleLevelCompiledExpr() {
        return ruleLevelCompiledExpr;
    }

    /**
     * @param ruleLevelCompiledExpr the ruleLevelCompiledExpr to set
     */
    public void setRuleLevelCompiledExpr(Serializable ruleLevelCompiledExpr) {
        this.ruleLevelCompiledExpr = ruleLevelCompiledExpr;
    }

    @Override
    protected void populate(BaseEntity baseEntity, CloneOptions cloneOptions) {
        RuleGroup ruleGroup = (RuleGroup) baseEntity;
        super.populate(ruleGroup, cloneOptions);
        ruleGroup.setRuleGroupExpression(ruleGroupExpression);
        ruleGroup.setApprovalStatus(ApprovalStatus.UNAPPROVED);
        ruleGroup.setRuleGroupExpression(ruleGroupExpression);
        ruleGroup.setRuleLevelCompiledExpr(ruleLevelCompiledExpr);
    }
}
