package com.nucleus.rules.model;

import javax.persistence.Cacheable;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.nucleus.entity.BaseEntity;
import com.nucleus.entity.CloneOptions;

/**
 * 
 * @author Nucleus Software Exports Limited
 * This class represents the Then part of the Rule
 */

@Entity
@DynamicUpdate
@DynamicInsert
@Cacheable
public class AssignmentAction extends RuleAction {

    private static final long serialVersionUID = 1L;

    private String            leftValue;

    @ManyToOne
    private Parameter         rightValue;

    /**
     * 
     * Getter for leftValue property
     * @return
     */
    public String getLeftValue() {
        return leftValue;
    }

    /**
     * 
     * Setter for leftValue property
     * @param leftValue
     */
    public void setLeftValue(String leftValue) {
        this.leftValue = leftValue;
    }

    /**
     * 
     * Getter for rightValue property
     * @return
     */
    public Parameter getRightValue() {
        return rightValue;
    }

    /**
     * 
     * Getter for rightValue property
     * @param rightValue
     */
    public void setRightValue(Parameter rightValue) {
        this.rightValue = rightValue;
    }

    @Override
    public String getDisplayName() {
        return getName();
    }

    @Override
    protected void populate(BaseEntity baseEntity, CloneOptions cloneOptions) {
        AssignmentAction assignmentAction = (AssignmentAction) baseEntity;
        super.populate(assignmentAction, cloneOptions);
        assignmentAction.setLeftValue(leftValue);
        assignmentAction.setRightValue(rightValue);
    }
    
    @Override
    protected void populateFrom(BaseEntity baseEntity, CloneOptions cloneOptions) {
    	AssignmentAction assignmentAction = (AssignmentAction) baseEntity;
        super.populateFrom(assignmentAction, cloneOptions);
        this.setLeftValue(assignmentAction.getLeftValue());
        this.setRightValue(assignmentAction.getRightValue());
    }

}
