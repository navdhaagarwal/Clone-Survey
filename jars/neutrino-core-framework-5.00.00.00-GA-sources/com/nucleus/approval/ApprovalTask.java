/**
 * Copyright Nucleus Software India Pvt Ltd. All rights reserved.
 */
package com.nucleus.approval;

import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQuery;
import javax.persistence.Transient;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.nucleus.task.Task;

/**
* @author Nucleus Software India Pvt Ltd
*/

@Entity
@DynamicUpdate
@DynamicInsert
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@NamedQuery(name="getApprovalTaskIdbyRefUUID",
		query="select id from ApprovalTask t where t.refUUId = :uuid order by entityLifeCycleData.creationTimeStamp desc")
public class ApprovalTask extends Task {

    @Transient
    private static final long serialVersionUID = 1L;

    @ManyToOne
    private ApprovalFlow      approvalFlowReference;

    // possible actions suggested by process
    private String            actions;
    private String            actionTaken;
    private String            workflowUserTaskId;

    
    /** This field is used to store the uri to whom the maker checker task needs to be assigned . This uri can be of either of two entity types -:
     * Either:
     * 1. Authority - If default maker checker assignment strategy is followed , then authority uri will be used
     * Or:
     * 2. User - If task assignment master execution needs to be used  , then user uri will be saved in this field
     * 
     */
    
    private String makerCheckerAssigneeUri;
 
    public String getActionTaken() {
        return actionTaken;
    }

    public void setActionTaken(String actionTaken) {
        this.actionTaken = actionTaken;
    }

    public String getActions() {
        return actions;
    }

    public void setActions(String actions) {
        this.actions = actions;
    }

    public ApprovalFlow getApprovalFlowReference() {
        return approvalFlowReference;
    }

    public void setApprovalFlowReference(ApprovalFlow approval) {
        this.approvalFlowReference = approval;
    }

    public String getWorkflowUserTaskId() {
        return workflowUserTaskId;
    }

    public void setWorkflowUserTaskId(String approvalTaskId) {
        this.workflowUserTaskId = approvalTaskId;
    }

    /**
     * @return the makerCheckerAssigneeUri
     */
    public String getMakerCheckerAssigneeUri() {
        return makerCheckerAssigneeUri;
    }

    /**
     * @param makerCheckerAssigneeUri the makerCheckerAssigneeUri to set
     */
    public void setMakerCheckerAssigneeUri(String makerCheckerAssigneeUri) {
        this.makerCheckerAssigneeUri = makerCheckerAssigneeUri;
    }

  


 

}