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
import com.nucleus.notificationMaster.NotificationMaster;
import com.nucleus.rules.model.RuleGroup;
import com.nucleus.rules.service.RuleBasedEventTask;

/**
 * @author Nucleus Software India Pvt Ltd 
 */
@Entity
@DynamicUpdate
@DynamicInsert
@Cacheable
public class NotificationTask extends EventTask implements RuleBasedEventTask{

    /** The Constant serialVersionUID. */
    private static final long  serialVersionUID = 1L;

    /** The notification master. */
    @OneToOne
    private NotificationMaster notificationMaster;

    /** The rule group. */
    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private RuleGroup          ruleGroup;


    /**
     * Gets the notification master.
     *
     * @return the notification master
     */
    public NotificationMaster getNotificationMaster() {
        return notificationMaster;
    }

    /**
     * Sets the notification master.
     *
     * @param notificationMaster the new notification master
     */
    public void setNotificationMaster(NotificationMaster notificationMaster) {
        this.notificationMaster = notificationMaster;
    }


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
        NotificationTask notificationTask = (NotificationTask) baseEntity;
        super.populate(notificationTask, cloneOptions);
        notificationTask.setNotificationMaster(notificationMaster);
        if (null != ruleGroup) {
            notificationTask.setRuleGroup((RuleGroup) this.getRuleGroup().cloneYourself(cloneOptions));
        }

    }

    @Override
    protected void populateFrom(BaseEntity baseEntity, CloneOptions cloneOptions) {
        NotificationTask notificationTask = (NotificationTask) baseEntity;
        super.populateFrom(notificationTask, cloneOptions);
        this.setNotificationMaster(notificationTask.getNotificationMaster());
        if (notificationTask.getRuleGroup() != null) {
            this.setRuleGroup((RuleGroup) notificationTask.getRuleGroup().cloneYourself(cloneOptions));

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
