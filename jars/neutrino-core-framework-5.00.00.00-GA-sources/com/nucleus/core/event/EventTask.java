/**
 * Copyright Nucleus Software India Pvt Ltd. All rights reserved.
 */
package com.nucleus.core.event;

import javax.persistence.Cacheable;
import javax.persistence.Entity;
import javax.persistence.Index;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.Table;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.nucleus.core.annotations.Synonym;
import com.nucleus.entity.BaseEntity;
import com.nucleus.entity.CloneOptions;

/**
 * @author Nucleus Software India Pvt Ltd
 */
@Entity
@DynamicUpdate
@DynamicInsert
@Cacheable
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@Synonym(grant="ALL")
@Table(indexes={@Index(name="fk_evnt_defntion_index",columnList="fk_evnt_defntion")})
public class EventTask extends BaseEntity {

	// All Possible Tasks
	public static final String ASSIGNMENT_TASK = "Assignment Task";
	public static final String ALLOCATION_TASK = "Allocation Task";
	public static final String NOTIFICATION_TASK = "Notification Task";
	public static final String VALIDATION_TASK = "Validation Task";
	public static final String RULE_INVOCATION_TASK = "Rule Invocation Task";
	public static final String ADHOC_TASK = "Adhoc Task";
	public static final String RULE_MATRIX_TASK = "Rule Matrix Task";
	public static final String LETTER_GENERATION_TASK = "Letter Generation Task";
	public static final String INTERFACE_TASK = "Interface Task";
	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;

	/** The event task description. */
	private String description;

	/** The event task code. */
	private String code;

	/** The event task type. */
	private String type;

	private Integer taskSequence;
	
    /** The is rule based. */
    private Boolean            isRuleBased = false;




	public Boolean getIsRuleBased() {
		return null == isRuleBased ? false : isRuleBased;
	}

	public void setIsRuleBased(Boolean isRuleBased) {
		this.isRuleBased = isRuleBased;
	}

	public Integer getTaskSequence() {
		return taskSequence;
	}

	public void setTaskSequence(Integer taskSequence) {
		this.taskSequence = taskSequence;
	}

	/**
	 * Gets the description.
	 * 
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * Sets the description.
	 * 
	 * @param description
	 *            the new description
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * Gets the code.
	 * 
	 * @return the code
	 */
	public String getCode() {
		return code;
	}

	/**
	 * Sets the code.
	 * 
	 * @param code
	 *            the new code
	 */
	public void setCode(String code) {
		this.code = code;
	}

	/**
	 * Gets the type.
	 * 
	 * @return the type
	 */
	public String getType() {
		return type;
	}

	/**
	 * Sets the type.
	 * 
	 * @param type
	 *            the new type
	 */
	public void setType(String type) {
		this.type = type;
	}

	@Override
	protected void populate(BaseEntity baseEntity, CloneOptions cloneOptions) {
		EventTask eventTask = (EventTask) baseEntity;
		super.populate(eventTask, cloneOptions);
		eventTask.setCode(code);
		eventTask.setDescription(description);
		eventTask.setType(type);
 		eventTask.setTaskSequence(taskSequence);
 		eventTask.setIsRuleBased(isRuleBased);
	}

	@Override
	protected void populateFrom(BaseEntity baseEntity, CloneOptions cloneOptions) {
		EventTask eventTask = (EventTask) baseEntity;
		super.populateFrom(eventTask, cloneOptions);
		this.setCode(eventTask.getCode());
		this.setDescription(eventTask.getDescription());
		this.setType(eventTask.getType());
		this.setTaskSequence(eventTask.getTaskSequence());
		this.setIsRuleBased(eventTask.getIsRuleBased());
	}

}
