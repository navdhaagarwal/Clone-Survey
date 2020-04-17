/**
 * Copyright Nucleus Software India Pvt Ltd. All rights reserved.
 */
package com.nucleus.rules.model.eventDefinition;

import javax.persistence.*;

import com.nucleus.rules.model.RuleGroup;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.Type;
import org.joda.time.DateTime;
import com.nucleus.rules.service.RuleBasedEventTask;
import com.nucleus.adhoc.AdhocTaskSubType;
import com.nucleus.adhoc.AdhocTaskType;
import com.nucleus.core.event.EventTask;
import com.nucleus.entity.BaseEntity;
import com.nucleus.entity.CloneOptions;

/**
 * The Class EventAdhocTask.
 *
 * @author Nucleus Software India Pvt Ltd
 */

@Entity
@DynamicUpdate
@DynamicInsert
@Cacheable
public class EventAdhocTask extends EventTask implements RuleBasedEventTask{

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /** The owner. */
    private String            owner;

    /** The assignee. */
    private String            assignee;

    /** The team uri. */
    private String            teamUri;

    /** The due date. */
    @Column
    @Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
    private DateTime          dueDate;

    /** The priority. */
    private Integer           priority;

    /** The task type. */
    @OneToOne
    private AdhocTaskType     taskType;

    /** The task sub type. */
    @OneToOne
    private AdhocTaskSubType  taskSubType;

    /** The name. */
    private String            title;


    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private RuleGroup ruleGroup;

    public RuleGroup getRuleGroup() {
        return ruleGroup;
    }

    public void setRuleGroup(RuleGroup ruleGroup) {
        this.ruleGroup = ruleGroup;
    }

    /**
     * Gets the owner.
     *
     * @return the owner
     */
    public String getOwner() {
        return owner;
    }

    /**
     * Sets the owner.
     *
     * @param owner the new owner
     */
    public void setOwner(String owner) {
        this.owner = owner;
    }

    /**
     * Gets the assignee.
     *
     * @return the assignee
     */
    public String getAssignee() {
        return assignee;
    }

    /**
     * Sets the assignee.
     *
     * @param assignee the new assignee
     */
    public void setAssignee(String assignee) {
        this.assignee = assignee;
    }

    /**
     * Gets the due date.
     *
     * @return the due date
     */
    public DateTime getDueDate() {
        return dueDate;
    }

    /**
     * Sets the due date.
     *
     * @param dueDate the new due date
     */
    public void setDueDate(DateTime dueDate) {
        this.dueDate = dueDate;
    }

    /**
     * Gets the priority.
     *
     * @return the priority
     */
    public Integer getPriority() {
        return priority;
    }

    /**
     * Sets the priority.
     *
     * @param priority the new priority
     */
    public void setPriority(Integer priority) {
        this.priority = priority;
    }

    /**
     * Gets the team uri.
     *
     * @return the team uri
     */
    public String getTeamUri() {
        return teamUri;
    }

    /**
     * Sets the team uri.
     *
     * @param teamUri the new team uri
     */
    public void setTeamUri(String teamUri) {
        this.teamUri = teamUri;
    }

    /**
     * Gets the task type.
     *
     * @return the task type
     */
    public AdhocTaskType getTaskType() {
        return taskType;
    }

    /**
     * Sets the task type.
     *
     * @param taskType the new task type
     */
    public void setTaskType(AdhocTaskType taskType) {
        this.taskType = taskType;
    }

    /**
     * Gets the task sub type.
     *
     * @return the task sub type
     */
    public AdhocTaskSubType getTaskSubType() {
        return taskSubType;
    }

    /**
     * Sets the task sub type.
     *
     * @param taskSubType the new task sub type
     */
    public void setTaskSubType(AdhocTaskSubType taskSubType) {
        this.taskSubType = taskSubType;
    }

    /**
     * Gets the title.
     *
     * @return the title
     */
    public String getTitle() {
        return title;
    }

    /**
     * Sets the title.
     *
     * @param title the new title
     */
    public void setTitle(String title) {
        this.title = title;
    }


    /* (non-Javadoc)
     * @see com.nucleus.rules.model.eventDefinition.EventTask#populate(com.nucleus.entity.BaseEntity, com.nucleus.entity.CloneOptions)
     */
    @Override
    protected void populate(BaseEntity baseEntity, CloneOptions cloneOptions) {
        EventAdhocTask eventAdhocTask = (EventAdhocTask) baseEntity;
        super.populate(eventAdhocTask, cloneOptions);

        eventAdhocTask.setOwner(owner);
        eventAdhocTask.setAssignee(assignee);
        eventAdhocTask.setTeamUri(teamUri);
        eventAdhocTask.setDueDate(dueDate);
        eventAdhocTask.setPriority(priority);
        eventAdhocTask.setTaskType(taskType);
        eventAdhocTask.setTaskSubType(taskSubType);
        eventAdhocTask.setTitle(title);
        if (null != ruleGroup) {
        	eventAdhocTask.setRuleGroup((RuleGroup) this.getRuleGroup().cloneYourself(cloneOptions));
        }

    }

    /* (non-Javadoc)
     * @see com.nucleus.rules.model.eventDefinition.EventTask#populateFrom(com.nucleus.entity.BaseEntity, com.nucleus.entity.CloneOptions)
     */
    @Override
    protected void populateFrom(BaseEntity baseEntity, CloneOptions cloneOptions) {
        EventAdhocTask eventAdhocTask = (EventAdhocTask) baseEntity;
        super.populateFrom(eventAdhocTask, cloneOptions);

        this.setOwner(eventAdhocTask.getOwner());
        this.setAssignee(eventAdhocTask.getAssignee());
        this.setTeamUri(eventAdhocTask.getTeamUri());
        this.setDueDate(eventAdhocTask.getDueDate());
        this.setPriority(eventAdhocTask.getPriority());
        this.setTaskType(eventAdhocTask.getTaskType());
        this.setTaskSubType(eventAdhocTask.getTaskSubType());
        this.setTitle(eventAdhocTask.getTitle());
        if (eventAdhocTask.getRuleGroup() != null) {
            this.setRuleGroup((RuleGroup) eventAdhocTask.getRuleGroup().cloneYourself(cloneOptions));
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
