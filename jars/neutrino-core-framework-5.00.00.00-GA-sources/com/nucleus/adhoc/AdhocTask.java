package com.nucleus.adhoc;

import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.OneToOne;
import javax.persistence.Transient;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.nucleus.core.annotations.Synonym;
import com.nucleus.entity.BaseEntity;

@Entity
@DynamicUpdate
@DynamicInsert
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@Synonym(grant="ALL")
public class AdhocTask extends BaseEntity {

    @Transient
    private static final long serialVersionUID = 1L;

    private String            title;

    @OneToOne
    private AdhocTaskType     taskType;

    @OneToOne
    private AdhocTaskSubType  taskSubType;

    private String            taskStatus;

    private String            taskId;

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
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

    public String getTaskStatus() {
        return taskStatus;
    }

    public void setTaskStatus(String taskStatus) {
        this.taskStatus = taskStatus;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

}