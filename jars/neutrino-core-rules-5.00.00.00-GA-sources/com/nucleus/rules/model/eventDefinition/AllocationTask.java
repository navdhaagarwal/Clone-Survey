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
import com.nucleus.rules.model.assignmentMatrix.TaskAssignmentMaster;
import com.nucleus.rules.service.RuleBasedEventTask;

/**
 * @author Nucleus Software India Pvt Ltd 
 */
@Entity
@DynamicUpdate
@DynamicInsert
@Cacheable
public class AllocationTask extends EventTask implements RuleBasedEventTask {

    /** The Constant serialVersionUID. */
    private static final long    serialVersionUID = 1L;

    /** The assignment task. */
    @OneToOne
    private TaskAssignmentMaster taskAssignment;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private RuleGroup          ruleGroup;
    
    
    

    public RuleGroup getRuleGroup() {
		return ruleGroup;
	}

	public void setRuleGroup(RuleGroup ruleGroup) {
		this.ruleGroup = ruleGroup;
	}

	/**
     * Gets the task assignment.
     *
     * @return the task assignment
     */
    public TaskAssignmentMaster getTaskAssignment() {
        return taskAssignment;
    }

    /**
     * Sets the task assignment.
     *
     * @param taskAssignment the new task assignment
     */
    public void setTaskAssignment(TaskAssignmentMaster taskAssignment) {
        this.taskAssignment = taskAssignment;
    }

    @Override
    protected void populate(BaseEntity baseEntity, CloneOptions cloneOptions) {
        AllocationTask allocationTask = (AllocationTask) baseEntity;
        super.populate(allocationTask, cloneOptions);
        allocationTask.setTaskAssignment(taskAssignment);
        if (null != ruleGroup) {
        	allocationTask.setRuleGroup((RuleGroup) this.getRuleGroup().cloneYourself(cloneOptions));
        }
    }

    @Override
    protected void populateFrom(BaseEntity baseEntity, CloneOptions cloneOptions) {

        AllocationTask allocationTask = (AllocationTask) baseEntity;
        super.populateFrom(allocationTask, cloneOptions);
        this.setTaskAssignment(allocationTask.getTaskAssignment());
        if (allocationTask.getRuleGroup() != null) {
            this.setRuleGroup((RuleGroup) allocationTask.getRuleGroup().cloneYourself(cloneOptions));
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
