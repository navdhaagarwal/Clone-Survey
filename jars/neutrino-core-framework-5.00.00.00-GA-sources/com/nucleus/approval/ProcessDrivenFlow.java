package com.nucleus.approval;

import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;

import com.nucleus.entity.BaseEntity;

@MappedSuperclass
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
public abstract class ProcessDrivenFlow extends BaseEntity {

    @Transient
    private static final long serialVersionUID = -8657526404702243156L;

    private int               currentState     = ProcessDrivenFlowStates.UNKNOWN;

    private String            workflowId;

    public String getWorkflowId() {
        return workflowId;
    }

    public void setWorkflowId(String workflowId) {
        this.workflowId = workflowId;
    }

    /**
     * See {@code ProcessDrivenFlowStates} to understand the states.
     */
    public int getCurrentState() {
        return currentState;
    }

    /**
     * See {@code ProcessDrivenFlowStates} to understand the states.
     */
    public void setCurrentState(int currentState) {
        this.currentState = currentState;
    }

}