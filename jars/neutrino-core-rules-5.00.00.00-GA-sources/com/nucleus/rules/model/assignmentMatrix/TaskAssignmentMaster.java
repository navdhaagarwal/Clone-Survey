package com.nucleus.rules.model.assignmentMatrix;

import javax.persistence.Entity;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.nucleus.entity.BaseEntity;
import com.nucleus.entity.CloneOptions;
import com.nucleus.master.audit.annotation.NeutrinoAuditableMaster;

@Entity
@DynamicUpdate
@DynamicInsert
@NeutrinoAuditableMaster(identifierColumn="code")
public class TaskAssignmentMaster extends BaseAssignmentMaster {

    private static final long serialVersionUID = 1L;

    @Override
    protected void populate(BaseEntity baseEntity, CloneOptions cloneOptions) {
        TaskAssignmentMaster taskAssignmentMaster = (TaskAssignmentMaster) baseEntity;
        super.populate(taskAssignmentMaster, cloneOptions);
    }

    @Override
    protected void populateFrom(BaseEntity baseEntity, CloneOptions cloneOptions) {
        TaskAssignmentMaster taskAssignmentMaster = (TaskAssignmentMaster) baseEntity;
        super.populateFrom(taskAssignmentMaster, cloneOptions);

    }

}
