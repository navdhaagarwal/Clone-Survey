package com.nucleus.rules.model.assignmentMatrix;

import javax.persistence.Entity;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.nucleus.entity.BaseEntity;
import com.nucleus.entity.CloneOptions;
import com.nucleus.master.audit.annotation.EmbedInAuditAsValue;
import com.nucleus.master.audit.annotation.NeutrinoAuditableMaster;

/**
 * 
 * @author Nucleus Software Exports Limited
 * Master class for assignment master
 */

@Entity
@DynamicUpdate
@DynamicInsert
@NeutrinoAuditableMaster(identifierColumn="code")
public class AssignmentMaster extends BaseAssignmentMaster {

    private static final long serialVersionUID = 1L;

    @EmbedInAuditAsValue(displayKey="label.executeAll")
    private Boolean           executeAll;

    @EmbedInAuditAsValue(displayKey = "label.assignment.master.saveResult")
    private Boolean           saveResult;


    
    /**
     * @return the executeAll
     */
    public Boolean getExecuteAll() {
        return executeAll;
    }

    /**
     * @param executeAll the executeAll to set
     */
    public void setExecuteAll(Boolean executeAll) {
        this.executeAll = executeAll;
    }

    public Boolean getSaveResult() {
        return saveResult;
    }

    public void setSaveResult(Boolean saveResult) {
        this.saveResult = saveResult;
    }



    @Override
    protected void populate(BaseEntity baseEntity, CloneOptions cloneOptions) {
        AssignmentMaster assignmentMaster = (AssignmentMaster) baseEntity;
        super.populate(assignmentMaster, cloneOptions);
        assignmentMaster.setExecuteAll(executeAll);
        assignmentMaster.setSaveResult(saveResult);

    }

    @Override
    protected void populateFrom(BaseEntity baseEntity, CloneOptions cloneOptions) {
        AssignmentMaster assignmentMaster = (AssignmentMaster) baseEntity;
        super.populateFrom(assignmentMaster, cloneOptions);
        this.setExecuteAll(assignmentMaster.getExecuteAll());
        this.setSaveResult(assignmentMaster.getSaveResult());


    }

}