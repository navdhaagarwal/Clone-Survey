package com.nucleus.adhoc;

import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedHashMap;

import org.joda.time.DateTime;

import com.nucleus.core.team.entity.Team;
import com.nucleus.entity.EntityId;

public class AdhocForm implements Serializable {

    private static final long       serialVersionUID = 240073122508425549L;

    private String                  name;
    private String                  description;
    private String                  owner;
    private String                  assignee;
    private String                  teamUri;
    private String                  teamId;
    private String                  taskId;
    private DateTime                dueDate;
    private Integer                 priority;
    private AdhocTaskType           taskType;
    private AdhocTaskSubType        taskSubType;
    private String                  taskStatus;
    private HashMap<String, Object> viewProperties;

    // To pass process variables
    private HashMap<String, Object> variablesMap;

    public void addProperty(String key, Object value) {
        if (viewProperties == null) {
            this.viewProperties = new LinkedHashMap<String, Object>();
        }
        this.viewProperties.put(key, value);
    }

    /**
     * @return the viewProperties
     */
    public HashMap<String, Object> getViewProperties() {
        return viewProperties;
    }

    /**
     * @param viewProperties
     *            the viewProperties to set
     */
    public void setViewProperties(HashMap<String, Object> viewProperties) {
        this.viewProperties = viewProperties;
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
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

    public String getOwner() {
        return owner;
    }

    public void setOwner(String ownerUri) {
        this.owner = ownerUri;
    }

    public String getAssignee() {
        return assignee;
    }

    public void setAssignee(String assigneeUri) {
        this.assignee = assigneeUri;
    }

    public DateTime getDueDate() {
        return dueDate;
    }

    public void setDueDate(DateTime dueDate) {
        this.dueDate = dueDate;
    }

    public Integer getPriority() {
        return priority;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
    }

    public AdhocTaskSubType getTaskSubType() {
        return taskSubType;
    }

    public void setTaskSubType(AdhocTaskSubType taskSubType) {
        this.taskSubType = taskSubType;
    }

    public AdhocTaskType getTaskType() {
        return taskType;
    }

    public void setTaskType(AdhocTaskType taskType) {
        this.taskType = taskType;
    }

    public String getTaskStatus() {
        return taskStatus;
    }

    public void setTaskStatus(String taskStatus) {
        this.taskStatus = taskStatus;
    }

    public String getTeamUri() {
        return teamUri;
    }

    public void setTeamUri(String teamUri) {
        this.teamUri = teamUri;
        this.teamId = EntityId.fromUri(teamUri).getLocalId().toString();
    }

    public String getTeamId() {
        return teamId;
    }

    public void setTeamId(String teamId) {
        this.teamId = teamId;
        this.teamUri = Team.class.getName() + ":" + teamId;
    }

    public HashMap<String, Object> getVariablesMap() {
        return variablesMap;
    }

    public void setVariablesMap(HashMap<String, Object> variablesMap) {
        this.variablesMap = variablesMap;
    }

}
