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
import com.nucleus.rules.model.ruleMatrixMaster.RuleMatrixMaster;
import com.nucleus.rules.service.RuleBasedEventTask;

/**
 * @author Nucleus Software India Pvt Ltd 
 */
@Entity
@DynamicUpdate
@DynamicInsert
@Cacheable
public class RuleMatrixTask extends EventTask implements RuleBasedEventTask{

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /** The rule matrix. */
    @OneToOne
    private RuleMatrixMaster  ruleMatrix;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private RuleGroup ruleGroup;  
    

    public RuleGroup getRuleGroup() {
		return ruleGroup;
	}

	public void setRuleGroup(RuleGroup ruleGroup) {
		this.ruleGroup = ruleGroup;
	}

	public RuleMatrixMaster getRuleMatrix() {
        return ruleMatrix;
    }

    public void setRuleMatrix(RuleMatrixMaster ruleMatrix) {
        this.ruleMatrix = ruleMatrix;
    }

    @Override
    protected void populate(BaseEntity baseEntity, CloneOptions cloneOptions) {
        RuleMatrixTask ruleMatrixTask = (RuleMatrixTask) baseEntity;
        super.populate(ruleMatrixTask, cloneOptions);
        ruleMatrixTask.setRuleMatrix(ruleMatrix);
        if (null != ruleGroup) {
        	ruleMatrixTask.setRuleGroup((RuleGroup) this.getRuleGroup().cloneYourself(cloneOptions));
        }
        
    }

    @Override
    protected void populateFrom(BaseEntity baseEntity, CloneOptions cloneOptions) {
        RuleMatrixTask ruleMatrixTask = (RuleMatrixTask) baseEntity;
        super.populateFrom(ruleMatrixTask, cloneOptions);
        this.setRuleMatrix(ruleMatrixTask.getRuleMatrix());
        if (ruleMatrixTask.getRuleGroup() != null) {
            this.setRuleGroup((RuleGroup) ruleMatrixTask.getRuleGroup().cloneYourself(cloneOptions));
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
