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
import com.nucleus.rules.service.RuleBasedEventTask;

/**
 * @author Nucleus Software India Pvt Ltd 
 */

@Entity
@DynamicUpdate
@DynamicInsert
@Cacheable
public class RuleValidationTask extends EventTask implements RuleBasedEventTask{
	
	public RuleValidationTask(){		
		this.setIsRuleBased(true);
	}
    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /** The rule group. */
    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private RuleGroup         ruleGroup;

    /**
     * Gets the rule group.
     *
     * @return the rule group
     */
    public RuleGroup getRuleGroup() {
        return ruleGroup;
    }

    /**
     * Sets the rule group.
     *
     * @param ruleGroup the new rule group
     */
    public void setRuleGroup(RuleGroup ruleGroup) {
        this.ruleGroup = ruleGroup;
    }

    /* (non-Javadoc) @see com.nucleus.entity.BaseEntity#populate(com.nucleus.entity.BaseEntity, com.nucleus.entity.CloneOptions) */
    @Override
    protected void populate(BaseEntity baseEntity, CloneOptions cloneOptions) {
        RuleValidationTask ruleValidationTask = (RuleValidationTask) baseEntity;
        super.populate(ruleValidationTask, cloneOptions);
        if (null != ruleGroup) {
            ruleValidationTask.setRuleGroup((RuleGroup) this.getRuleGroup().cloneYourself(cloneOptions));
        }

    }

    @Override
    protected void populateFrom(BaseEntity baseEntity, CloneOptions cloneOptions) {
        RuleValidationTask ruleValidationTask = (RuleValidationTask) baseEntity;
        super.populateFrom(ruleValidationTask, cloneOptions);
        if (ruleValidationTask.getRuleGroup() != null) {
            this.setRuleGroup((RuleGroup) ruleValidationTask.getRuleGroup().cloneYourself(cloneOptions));
        } 
    }

	@Override
	public RuleGroup fetchRuleGroup() {
		// TODO Auto-generated method stub
		return this.getRuleGroup();
	}

	@Override
	public void setRuleGroupInEventTask(RuleGroup ruleGroup) {
		// TODO Auto-generated method stub
		this.setRuleGroup(ruleGroup);
		
	}

	@Override
	public Boolean isEventRuleBased() {
		// TODO Auto-generated method stub
		return this.getIsRuleBased();
	}
}
