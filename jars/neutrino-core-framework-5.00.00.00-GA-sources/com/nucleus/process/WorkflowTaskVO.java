/**
 * This file and a proportion of its content is copyright of Nucleus Software Exports Limited - © 2012. All rights reserved.
 * Any redistribution or reproduction of part or all of the contents in any form is prohibited other than the following:
 * - you cannot print or download to a local hard disk extract contents either part or full for personal/ commercial/
 * academic or any other use
 * - you may not copy the content to individual/ third parties for any type of use, either as compiled or source format
 * without the knowledge and consent of Nucleus SOftware
 * - You may not, except with our express written permission, distribute or commercially exploit the content. Nor may you
 * transmit it or store it in any other web site or other form of electronic retrieval system.
 */
package com.nucleus.process;

import java.io.Serializable;

import org.joda.time.DateTime;

/**
 * @author Nucleus Software Exports Limited
 */
public class WorkflowTaskVO implements Serializable {

    private static final long serialVersionUID = 5667512550959960080L;

    private String            workflowTaskId;
    private String            associatedStepName;
    private String            associatedStageName;
    private DateTime          creationDate;
    private DateTime          dueBy;
    private String            processKey;
    private boolean           canReassign;
    private boolean           currentState;
    private String            ownerUri;
    private String            assigneeUri;
    private String            candidateGroupsUri;
    private boolean           currentlyUnassigned;
    private boolean           sendToPoolPossible;
    private Integer           level;
    private String            name;
    private String            description;
    private int               priority;
    private String            assigneeName;
    private String            leadStatus;
    /** captures the loan processing stage. 0=Unknown 1=Lead 2=Application*/
    private int               loanStage;
    private String            workflowConfigurationTypeUri;
    private Long              teamBranchId;
    private String            tat;
    
    /** Captures dashboard related fields */
    private int 			taskcompleteOnTime;
    private int 			taskCompletedBeforeTime;
    private int 			taskCompletedAfterTime;
    private int 			taskNotCompleteAfterTime;
    private int 			taskNotCompleteBeforeTime;
    private int 			taskNotCompleteOnTime;
    private String            communicationStatus;
    private String gridColorCode;

    public String getGridColorCode() {
        return gridColorCode;
    }

    public void setGridColorCode(String gridColorCode) {
        this.gridColorCode = gridColorCode;
    }

    public String getTat() {
        return tat;
    }

    public void setTat(String tat) {
        this.tat = tat;
    }

    public DateTime getCreationDate() {
        return creationDate;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public DateTime getDueBy() {
        return dueBy;
    }

    public String getWorkflowTaskId() {
        return workflowTaskId;
    }

    public String getAssociatedStepName() {
        return associatedStepName;
    }

    public void setAssociatedStepName(String stepName) {
        this.associatedStepName = stepName;
    }

    public String getProcessKey() {
        return processKey;
    }

    public void setWorkflowTaskId(String workflowTaskId) {
        this.workflowTaskId = workflowTaskId;
    }

    public void setCreationDate(DateTime creationDate) {
        this.creationDate = creationDate;
    }

    public void setDueBy(DateTime dueDate) {
        this.dueBy = dueDate;
    }

    public void setProcessKey(String processKey) {
        this.processKey = processKey;
    }

    public boolean isCanReassign() {
        return canReassign;
    }

    public void setCanReassign(boolean canReassign) {
        this.canReassign = canReassign;
    }

    public boolean isCurrentState() {
        return currentState;
    }

    public void setCurrentState(boolean currentState) {
        this.currentState = currentState;
    }

    public String getOwnerUri() {
        return ownerUri;
    }

    public void setOwnerUri(String ownerUri) {
        this.ownerUri = ownerUri;
    }

    public String getCandidateGroupsUri() {
        return candidateGroupsUri;
    }

    public void setCandidateGroupsUri(String candidateGroupsUri) {
        this.candidateGroupsUri = candidateGroupsUri;
    }

    public boolean isCurrentlyUnassigned() {
        return currentlyUnassigned;
    }

    public void setUnassigned(boolean unassigned) {
        this.currentlyUnassigned = unassigned;
    }

    public boolean isSendToPoolPossible() {
        return sendToPoolPossible;
    }

    public void setCanSendToPool(boolean canSendToPool) {
        this.sendToPoolPossible = canSendToPool;
    }

    public void setCurrentlyUnassigned(boolean currentlyUnassigned) {
        this.currentlyUnassigned = currentlyUnassigned;
    }

    public void setSendToPoolPossible(boolean sendToPoolPossible) {
        this.sendToPoolPossible = sendToPoolPossible;
    }

    public String getAssigneeUri() {
        return assigneeUri;
    }

    public void setAssigneeUri(String assigneeUri) {
        this.assigneeUri = assigneeUri;
    }

    public Integer getLevel() {
        return level;
    }

    public void setLevel(Integer level) {
        this.level = level;
    }

    public String getAssigneeName() {
        return assigneeName;
    }

    public void setAssigneeName(String assigneeName) {
        this.assigneeName = assigneeName;
    }

    /**
     * @return the leadStatus
     */
    public String getLeadStatus() {
        return leadStatus;
    }

    /**
     * @param leadStatus the leadStatus to set
     */
    public void setLeadStatus(String leadStatus) {
        this.leadStatus = leadStatus;
    }

    /**
     * @return the associatedStageName
     */
    public String getAssociatedStageName() {
        return associatedStageName;
    }

    /**
     * @param associatedStageName the associatedStageName to set
     */
    public void setAssociatedStageName(String associatedStageName) {
        this.associatedStageName = associatedStageName;
    }

    public int getLoanStage() {
        return loanStage;
    }

    public void setLoanStage(int loanStage) {
        this.loanStage = loanStage;
    }

    public String getWorkflowConfigurationTypeUri() {
        return workflowConfigurationTypeUri;
    }

    public void setWorkflowConfigurationTypeUri(String workflowConfigurationTypeUri) {
        this.workflowConfigurationTypeUri = workflowConfigurationTypeUri;
    }

    public Long getTeamBranchId() {
        return teamBranchId;
    }

    public void setTeamBranchId(Long teamBranchId) {
        this.teamBranchId = teamBranchId;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((assigneeName == null) ? 0 : assigneeName.hashCode());
        result = prime * result + ((assigneeUri == null) ? 0 : assigneeUri.hashCode());
        result = prime * result + ((associatedStageName == null) ? 0 : associatedStageName.hashCode());
        result = prime * result + ((associatedStepName == null) ? 0 : associatedStepName.hashCode());
        result = prime * result + (canReassign ? 1231 : 1237);
        result = prime * result + ((candidateGroupsUri == null) ? 0 : candidateGroupsUri.hashCode());
        result = prime * result + ((creationDate == null) ? 0 : creationDate.hashCode());
        result = prime * result + (currentState ? 1231 : 1237);
        result = prime * result + (currentlyUnassigned ? 1231 : 1237);
        result = prime * result + ((description == null) ? 0 : description.hashCode());
        result = prime * result + ((dueBy == null) ? 0 : dueBy.hashCode());
        result = prime * result + ((leadStatus == null) ? 0 : leadStatus.hashCode());
        result = prime * result + ((level == null) ? 0 : level.hashCode());
        result = prime * result + loanStage;
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + ((ownerUri == null) ? 0 : ownerUri.hashCode());
        result = prime * result + priority;
        result = prime * result + ((processKey == null) ? 0 : processKey.hashCode());
        result = prime * result + (sendToPoolPossible ? 1231 : 1237);
        result = prime * result + ((tat == null) ? 0 : tat.hashCode());
        result = prime * result + ((teamBranchId == null) ? 0 : teamBranchId.hashCode());
        result = prime * result + ((workflowConfigurationTypeUri == null) ? 0 : workflowConfigurationTypeUri.hashCode());
        result = prime * result + ((workflowTaskId == null) ? 0 : workflowTaskId.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        WorkflowTaskVO other = (WorkflowTaskVO) obj;
        if (assigneeName == null) {
            if (other.assigneeName != null)
                return false;
        } else if (!assigneeName.equals(other.assigneeName))
            return false;
        if (assigneeUri == null) {
            if (other.assigneeUri != null)
                return false;
        } else if (!assigneeUri.equals(other.assigneeUri))
            return false;
        if (associatedStageName == null) {
            if (other.associatedStageName != null)
                return false;
        } else if (!associatedStageName.equals(other.associatedStageName))
            return false;
        if (associatedStepName == null) {
            if (other.associatedStepName != null)
                return false;
        } else if (!associatedStepName.equals(other.associatedStepName))
            return false;
        if (canReassign != other.canReassign)
            return false;
        if (candidateGroupsUri == null) {
            if (other.candidateGroupsUri != null)
                return false;
        } else if (!candidateGroupsUri.equals(other.candidateGroupsUri))
            return false;
        if (creationDate == null) {
            if (other.creationDate != null)
                return false;
        } else if (!creationDate.equals(other.creationDate))
            return false;
        if (currentState != other.currentState)
            return false;
        if (currentlyUnassigned != other.currentlyUnassigned)
            return false;
        if (description == null) {
            if (other.description != null)
                return false;
        } else if (!description.equals(other.description))
            return false;
        if (dueBy == null) {
            if (other.dueBy != null)
                return false;
        } else if (!dueBy.equals(other.dueBy))
            return false;
        if (leadStatus == null) {
            if (other.leadStatus != null)
                return false;
        } else if (!leadStatus.equals(other.leadStatus))
            return false;
        if (level == null) {
            if (other.level != null)
                return false;
        } else if (!level.equals(other.level))
            return false;
        if (loanStage != other.loanStage)
            return false;
        if (name == null) {
            if (other.name != null)
                return false;
        } else if (!name.equals(other.name))
            return false;
        if (ownerUri == null) {
            if (other.ownerUri != null)
                return false;
        } else if (!ownerUri.equals(other.ownerUri))
            return false;
        if (priority != other.priority)
            return false;
        if (processKey == null) {
            if (other.processKey != null)
                return false;
        } else if (!processKey.equals(other.processKey))
            return false;
        if (sendToPoolPossible != other.sendToPoolPossible)
            return false;
        if (tat == null) {
            if (other.tat != null)
                return false;
        } else if (!tat.equals(other.tat))
            return false;
        if (teamBranchId == null) {
            if (other.teamBranchId != null)
                return false;
        } else if (!teamBranchId.equals(other.teamBranchId))
            return false;
        if (workflowConfigurationTypeUri == null) {
            if (other.workflowConfigurationTypeUri != null)
                return false;
        } else if (!workflowConfigurationTypeUri.equals(other.workflowConfigurationTypeUri))
            return false;
        if (workflowTaskId == null) {
            if (other.workflowTaskId != null)
                return false;
        } else if (!workflowTaskId.equals(other.workflowTaskId))
            return false;
        return true;
    }

	public int getTaskcompleteOnTime() {
		return taskcompleteOnTime;
	}

	public void setTaskcompleteOnTime(int taskcompleteOnTime) {
		this.taskcompleteOnTime = taskcompleteOnTime;
	}

	public int getTaskCompletedBeforeTime() {
		return taskCompletedBeforeTime;
	}

	public void setTaskCompletedBeforeTime(int taskCompletedBeforeTime) {
		this.taskCompletedBeforeTime = taskCompletedBeforeTime;
	}

	public int getTaskCompletedAfterTime() {
		return taskCompletedAfterTime;
	}

	public void setTaskCompletedAfterTime(int taskCompletedAfterTime) {
		this.taskCompletedAfterTime = taskCompletedAfterTime;
	}

	public int getTaskNotCompleteAfterTime() {
		return taskNotCompleteAfterTime;
	}

	public void setTaskNotCompleteAfterTime(int taskNotCompleteAfterTime) {
		this.taskNotCompleteAfterTime = taskNotCompleteAfterTime;
	}

	public int getTaskNotCompleteBeforeTime() {
		return taskNotCompleteBeforeTime;
	}

	public void setTaskNotCompleteBeforeTime(int taskNotCompleteBeforeTime) {
		this.taskNotCompleteBeforeTime = taskNotCompleteBeforeTime;
	}

	public int getTaskNotCompleteOnTime() {
		return taskNotCompleteOnTime;
	}

	public void setTaskNotCompleteOnTime(int taskNotCompleteOnTime) {
		this.taskNotCompleteOnTime = taskNotCompleteOnTime;
	}

    public String getCommunicationStatus() {
        return communicationStatus;
    }

    public void setCommunicationStatus(String communicationStatus) {
        this.communicationStatus = communicationStatus;
    }

}