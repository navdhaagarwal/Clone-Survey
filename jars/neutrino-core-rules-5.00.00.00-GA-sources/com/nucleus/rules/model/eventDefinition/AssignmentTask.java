/**
 * Copyright Nucleus Software India Pvt Ltd. All rights reserved.
 */package com.nucleus.rules.model.eventDefinition;

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
import com.nucleus.rules.model.assignmentMatrix.AssignmentMaster;
import com.nucleus.rules.service.RuleBasedEventTask;

/**
 * @author Nucleus Software India Pvt Ltd 
 */
@Entity
@DynamicUpdate
@DynamicInsert
@Cacheable
public class AssignmentTask extends EventTask implements RuleBasedEventTask{

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /** The assignment task. */
    @OneToOne
    private AssignmentMaster  assignment;
    
    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private RuleGroup ruleGroup;
    
    
    
    public RuleGroup getRuleGroup() {
		return ruleGroup;
	}

	public void setRuleGroup(RuleGroup ruleGroup) {
		this.ruleGroup = ruleGroup;
	}

	/**
     * Gets the assignment.
     *
     * @return the assignment
     */
    public AssignmentMaster getAssignment() {
        return assignment;
    }

    /**
     * Sets the assignment.
     *
     * @param assignment the new assignment
     */
    public void setAssignment(AssignmentMaster assignment) {
        this.assignment = assignment;
    }

    @Override
    protected void populate(BaseEntity baseEntity, CloneOptions cloneOptions) {
        AssignmentTask assignmentTask = (AssignmentTask) baseEntity;
        super.populate(assignmentTask, cloneOptions);
        assignmentTask.setAssignment(assignment);
        if (null != ruleGroup) {
        	assignmentTask.setRuleGroup((RuleGroup) this.getRuleGroup().cloneYourself(cloneOptions));
        }

    }

    @Override
    protected void populateFrom(BaseEntity baseEntity, CloneOptions cloneOptions) {
        AssignmentTask assignmentTask = (AssignmentTask) baseEntity;
        super.populateFrom(assignmentTask, cloneOptions);
        this.setAssignment(assignmentTask.getAssignment());  
        if (assignmentTask.getRuleGroup() != null) {
            this.setRuleGroup((RuleGroup) assignmentTask.getRuleGroup().cloneYourself(cloneOptions));
        } 
    }

	@Override
	public RuleGroup fetchRuleGroup() {	
		return this.getRuleGroup();
	}

	@Override
	public void setRuleGroupInEventTask(RuleGroup ruleGroup) {
		this.setRuleGroup(ruleGroup);
		
	}

	@Override
	public Boolean isEventRuleBased() {
		return this.getIsRuleBased();
	}

}
