/**
 * Copyright Nucleus Software India Pvt Ltd. All rights reserved.
 */
package com.nucleus.core.event.assignment;

import java.util.Map;

import com.nucleus.core.event.NeutrinoEvent;
import com.nucleus.core.event.NeutrinoEventPublisher;
import com.nucleus.core.event.NeutrinoEventWorker;
import com.nucleus.rules.model.RuleGroup;
import com.nucleus.rules.model.assignmentMatrix.TaskAssignmentMaster;

/**
 * 
 * @author Nucleus Software India Pvt Ltd 
 */
public class TaskAllocationEventWorker extends NeutrinoEventWorker {

    private TaskAssignmentMaster taskAssignmentMaster;
    private Map<Object, Object>  contextMap;
    private Map<Object, Object>  resultMap;
    private RuleGroup ruleGroup;
    private String uuid;
    private Boolean auditingEnabled = true;
    private Boolean purgingRequired = false;
    private Boolean isRuleBased = false;

    
    

    public RuleGroup getRuleGroup() {
		return ruleGroup;
	}

	public void setRuleGroup(RuleGroup ruleGroup) {
		this.ruleGroup = ruleGroup;
	}

	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	public Boolean getAuditingEnabled() {
		return auditingEnabled;
	}

	public void setAuditingEnabled(Boolean auditingEnabled) {
		this.auditingEnabled = auditingEnabled;
	}

	public Boolean getPurgingRequired() {
		return purgingRequired;
	}

	public void setPurgingRequired(Boolean purgingRequired) {
		this.purgingRequired = purgingRequired;
	}

	public Boolean getIsRuleBased() {
		return isRuleBased;
	}

	public void setIsRuleBased(Boolean isRuleBased) {
		this.isRuleBased = isRuleBased;
	}

	public TaskAllocationEventWorker(String description) {
        super(description);
    }

    public Map<Object, Object> getContextMap() {
        return contextMap;
    }

    public TaskAssignmentMaster getTaskAssignmentMaster() {
        return taskAssignmentMaster;
    }

    public void setTaskAssignmentMaster(TaskAssignmentMaster taskAssignmentMaster) {
        this.taskAssignmentMaster = taskAssignmentMaster;
    }

    public void setContextMap(Map<Object, Object> contextMap) {
        this.contextMap = contextMap;
    }

    public Map<Object, Object> getResultMap() {
        return resultMap;
    }

    public void setResultMap(Map<Object, Object> resultMap) {
        this.resultMap = resultMap;
    }

    public NeutrinoEvent createNeutrinoEvent(NeutrinoEventPublisher publisher) {
        TaskAllocationEvent event = new TaskAllocationEvent(publisher, "Invoking  Task Assignment Master"
                + taskAssignmentMaster.getName(), this);

        return event;
    }

}
