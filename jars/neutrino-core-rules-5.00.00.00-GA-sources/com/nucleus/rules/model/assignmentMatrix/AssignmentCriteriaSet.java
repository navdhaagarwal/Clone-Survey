/**
 * Copyright Nucleus Software India Pvt Ltd. All rights reserved.
 */
package com.nucleus.rules.model.assignmentMatrix;

import javax.persistence.Cacheable;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToOne;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.nucleus.entity.BaseEntity;
import com.nucleus.entity.CloneOptions;
import com.nucleus.master.audit.annotation.EmbedInAuditAsValue;
import com.nucleus.master.audit.annotation.EmbedInAuditAsValueObject;
import com.nucleus.rules.model.CriteriaRules;

/**
 * The Class AssignmentCriteriaRule.
 */
/**
 * @author Nucleus Software India Pvt Ltd 
 */
@Entity
@DynamicUpdate
@DynamicInsert
@Cacheable
public class AssignmentCriteriaSet extends AssignmentSet {

    public static final String LEAST_LOADED_USER = "Least_Loaded_User";
    public static final String LEAST_LOADED_TEAM = "Least_Loaded_Team";

    /** The Constant serialVersionUID. */
    private static final long  serialVersionUID  = 1L;

    /** The criteria rules. */
    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @EmbedInAuditAsValueObject(skipInDisplay=true)
    private CriteriaRules      criteriaRules;

    /** The allocation strategy. */
    @EmbedInAuditAsValue
    private String             allocationStrategy;

    /**
     * Gets the criteria rules.
     *
     * @return the criteria rules
     */
    public CriteriaRules getCriteriaRules() {
        return criteriaRules;
    }

    /**
     * Sets the criteria rules.
     *
     * @param criteriaRules the new criteria rules
     */
    public void setCriteriaRules(CriteriaRules criteriaRules) {
        this.criteriaRules = criteriaRules;
    }

    /**
     * Gets the allocation strategy.
     *
     * @return the allocation strategy
     */
    public String getAllocationStrategy() {
        return allocationStrategy;
    }

    /**
     * Sets the allocation strategy.
     *
     * @param allocationStrategy the new allocation strategy
     */
    public void setAllocationStrategy(String allocationStrategy) {
        this.allocationStrategy = allocationStrategy;
    }

    /* (non-Javadoc) @see com.nucleus.entity.BaseEntity#populate(com.nucleus.entity.BaseEntity, com.nucleus.entity.CloneOptions) */
    @Override
    protected void populate(BaseEntity baseEntity, CloneOptions cloneOptions) {
        AssignmentCriteriaSet assignmentCriteriaRule = (AssignmentCriteriaSet) baseEntity;
        super.populate(assignmentCriteriaRule, cloneOptions);
        assignmentCriteriaRule.setAllocationStrategy(allocationStrategy);
        if (null != criteriaRules) {
            assignmentCriteriaRule.setCriteriaRules((CriteriaRules) this.getCriteriaRules().cloneYourself(cloneOptions));
        }

    }

    /* (non-Javadoc) @see com.nucleus.rules.model.assignmentMatrix.AssignmentSet#populateFrom(com.nucleus.entity.BaseEntity, com.nucleus.entity.CloneOptions) */
    @Override
    protected void populateFrom(BaseEntity baseEntity, CloneOptions cloneOptions) {
        AssignmentCriteriaSet assignmentCriteriaRule = (AssignmentCriteriaSet) baseEntity;
        super.populateFrom(assignmentCriteriaRule, cloneOptions);
        this.setAllocationStrategy(assignmentCriteriaRule.getAllocationStrategy());
        if (assignmentCriteriaRule.getCriteriaRules() != null) {
            this.setCriteriaRules((CriteriaRules) assignmentCriteriaRule.getCriteriaRules().cloneYourself(cloneOptions));

        }

    }

}
