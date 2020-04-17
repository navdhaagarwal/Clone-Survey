/**
 * Copyright Nucleus Software India Pvt Ltd. All rights reserved.
 */
package com.nucleus.rules.model.eventDefinition;

import javax.persistence.Cacheable;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToOne;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.nucleus.core.event.EventTask;
import com.nucleus.entity.BaseEntity;
import com.nucleus.entity.CloneOptions;
import com.nucleus.rules.model.RuleGroup;
import com.nucleus.rules.model.RuleInvocationMapping;
import com.nucleus.rules.service.RuleBasedEventTask;

/**
 * @author Nucleus Software India Pvt Ltd 
 */
@Entity
@DynamicUpdate
@DynamicInsert
@Cacheable
public class RuleInvocationMappingTask extends EventTask implements RuleBasedEventTask{

    /** The Constant serialVersionUID. */
    private static final long     serialVersionUID = 1L;

    /** The assignment task. */
    @OneToOne
    private RuleInvocationMapping ruleInvocationMapping;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private RuleGroup ruleGroup;

    public RuleGroup getRuleGroup() {
        return ruleGroup;
    }

    public void setRuleGroup(RuleGroup ruleGroup) {
        this.ruleGroup = ruleGroup;
    }

    /**
     * Gets the rule invocation mapping.
     *
     * @return the rule invocation mapping
     */
    public RuleInvocationMapping getRuleInvocationMapping() {
        return ruleInvocationMapping;
    }

    /**
     * Sets the rule invocation mapping.
     *
     * @param ruleInvocationMapping the new rule invocation mapping
     */
    public void setRuleInvocationMapping(RuleInvocationMapping ruleInvocationMapping) {
        this.ruleInvocationMapping = ruleInvocationMapping;
    }

    /* (non-Javadoc) @see com.nucleus.entity.BaseEntity#populate(com.nucleus.entity.BaseEntity, com.nucleus.entity.CloneOptions) */
    @Override
    protected void populate(BaseEntity baseEntity, CloneOptions cloneOptions) {
        RuleInvocationMappingTask ruleInvocationMappingTask = (RuleInvocationMappingTask) baseEntity;
        super.populate(ruleInvocationMappingTask, cloneOptions);
        ruleInvocationMappingTask.setRuleInvocationMapping(ruleInvocationMapping);
        if (null != ruleGroup) {
        	ruleInvocationMappingTask.setRuleGroup((RuleGroup) this.getRuleGroup().cloneYourself(cloneOptions));
        }
    }

    @Override
    protected void populateFrom(BaseEntity baseEntity, CloneOptions cloneOptions) {
        RuleInvocationMappingTask ruleInvocationMappingTask = (RuleInvocationMappingTask) baseEntity;
        super.populateFrom(ruleInvocationMappingTask, cloneOptions);
        this.setRuleInvocationMapping(ruleInvocationMappingTask.getRuleInvocationMapping());
        if (ruleInvocationMappingTask.getRuleGroup() != null) {
            this.setRuleGroup((RuleGroup) ruleInvocationMappingTask.getRuleGroup().cloneYourself(cloneOptions));
        } 

    }

	@Override
	public RuleGroup fetchRuleGroup() {
		// TODO Auto-generated method stub
		return this.getRuleGroup();
	}

	@Override
	public void setRuleGroupInEventTask(RuleGroup ruleGroup) {
		this.setRuleGroup(ruleGroup);
		
	}

	@Override
	public Boolean isEventRuleBased() {
		// TODO Auto-generated method stub
		return this.getIsRuleBased();
	}

}
