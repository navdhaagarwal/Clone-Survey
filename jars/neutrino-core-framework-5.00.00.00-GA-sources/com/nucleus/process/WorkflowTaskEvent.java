/**
 * Copyright Nucleus Software India Pvt Ltd. All rights reserved.
 */
package com.nucleus.process;

import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.nucleus.event.EventTypes;
import com.nucleus.event.GenericEvent;

/**
 * @author Nucleus Software India Pvt Ltd
 * This class holds the event data for workflow human task.
 */
@Entity
@DynamicUpdate
@DynamicInsert
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
public class WorkflowTaskEvent extends GenericEvent {

    private static final long serialVersionUID = 5022556829404732858L;

    public WorkflowTaskEvent() {
        super(EventTypes.WORKFLOW_TASK_EVENT);
    }

    public WorkflowTaskEvent setWorkflowTaskId(String taskId) {
        eventContext.put("WORKFLOW_TASK_ID", taskId);
        return this;
    }

    public String getWorkflowTaskId() {
        return (String) eventContext.get("WORKFLOW_TASK_ID");
    }

    public WorkflowTaskEvent setWorkflowProcessDefinitionId(String workflowProcessDefinitionId) {
        eventContext.put("WORKFLOW_PROCESS_DEFINITON_ID", workflowProcessDefinitionId);
        return this;
    }

    public String getWorkflowProcessDefinitionId() {
        return (String) eventContext.get("WORKFLOW_PROCESS_DEFINITON_ID");
    }

    public WorkflowTaskEvent setWorkflowTaskInstance(Object taskInstance) {
        eventContext.put("WORKFLOW_TASK_INSTANCE", taskInstance);
        return this;
    }

    public Object getWorkflowTaskInstance() {
        return eventContext.get("WORKFLOW_TASK_INSTANCE");
    }

}
