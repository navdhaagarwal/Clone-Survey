/**
 * Copyright Nucleus Software India Pvt Ltd. All rights reserved.
 */
package com.nucleus.core.event.adhocTask;

import java.util.Date;

import com.nucleus.adhoc.AdhocTaskSubType;
import com.nucleus.adhoc.AdhocTaskType;
import com.nucleus.core.event.NeutrinoEvent;
import com.nucleus.core.event.NeutrinoEventPublisher;
import com.nucleus.core.event.NeutrinoEventWorker;

/**
 * 
 * @author Nucleus Software India Pvt Ltd 
 */

public class AdhocTaskEventWorker extends NeutrinoEventWorker {

    private String           owner;

    private String           assignee;

    private String           teamUri;

    private Date             dueDate;

    private Integer          priority;

    private AdhocTaskType    taskType;

    private AdhocTaskSubType taskSubType;

    private String           title;

    private String           description;

    public AdhocTaskEventWorker(String name) {
        super(name);

    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getAssignee() {
        return assignee;
    }

    public void setAssignee(String assignee) {
        this.assignee = assignee;
    }

    public String getTeamUri() {
        return teamUri;
    }

    public void setTeamUri(String teamUri) {
        this.teamUri = teamUri;
    }

    public Date getDueDate() {
        return dueDate;
    }

    public void setDueDate(Date dueDate) {
        this.dueDate = dueDate;
    }

    public Integer getPriority() {
        return priority;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
    }

    public AdhocTaskType getTaskType() {
        return taskType;
    }

    public void setTaskType(AdhocTaskType taskType) {
        this.taskType = taskType;
    }

    public AdhocTaskSubType getTaskSubType() {
        return taskSubType;
    }

    public void setTaskSubType(AdhocTaskSubType taskSubType) {
        this.taskSubType = taskSubType;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public NeutrinoEvent createNeutrinoEvent(NeutrinoEventPublisher publisher) {
        AdhocTaskEvent event = new AdhocTaskEvent(publisher, "Invoking Adhoc Task " + title, this);

        return event;
    }

}
