package com.nucleus.rules.model;

import javax.persistence.Cacheable;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.nucleus.core.annotations.Synonym;
import com.nucleus.entity.BaseEntity;
import com.nucleus.entity.CloneOptions;
import com.nucleus.master.BaseMasterEntity;

/**
 * 
 * @author Nucleus Software Exports Limited
 * Rule Action Mapping class
 */

@Entity
@DynamicUpdate
@DynamicInsert
@Cacheable
@Synonym(grant="ALL")
public class RuleActionMapping extends BaseMasterEntity {

    private static final long serialVersionUID = -5948738088121107400L;

    @ManyToOne
    @JoinColumn(name = "re_rule")
    private Rule              rule;

    @ManyToOne
    private RuleAction        thenAction;

    @ManyToOne
    private RuleAction        elseAction;

    /**
     * @return the rule
     */
    public Rule getRule() {
        return rule;
    }

    /**
     * @param rule the rule to set
     */
    public void setRule(Rule rule) {
        this.rule = rule;
    }

    /**
     * @return the thenAction
     */
    public RuleAction getThenAction() {
        return thenAction;
    }

    /**
     * @param thenAction the thenAction to set
     */
    public void setThenAction(RuleAction thenAction) {
        this.thenAction = thenAction;
    }

    /**
     * @return the elseAction
     */
    public RuleAction getElseAction() {
        return elseAction;
    }

    /**
     * @param elseAction the elseAction to set
     */
    public void setElseAction(RuleAction elseAction) {
        this.elseAction = elseAction;
    }

    /**
     * @param populate
     */
    @Override
    protected void populate(BaseEntity baseEntity, CloneOptions cloneOptions) {
        RuleActionMapping ruleActionMapping = (RuleActionMapping) baseEntity;
        super.populate(ruleActionMapping, cloneOptions);
        ruleActionMapping.setElseAction(elseAction);
        ruleActionMapping.setThenAction(thenAction);
        ruleActionMapping.setRule(rule);
    }
}
