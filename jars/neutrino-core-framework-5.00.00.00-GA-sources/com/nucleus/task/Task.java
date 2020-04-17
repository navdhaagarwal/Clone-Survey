package com.nucleus.task;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Index;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.Type;
import org.joda.time.DateTime;

import com.nucleus.core.annotations.Synonym;
import com.nucleus.core.misc.util.DateUtils;
import com.nucleus.entity.BaseEntity;
import com.nucleus.user.User;

@Entity
@DynamicUpdate
@DynamicInsert
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@Synonym(grant="ALL")
@Table(indexes={@Index(name="taskStatus_index",columnList="taskStatus"),@Index(name="priority_index",columnList="priority")})
public class Task extends BaseEntity {

    @Transient
    private static final long serialVersionUID = 1L;

    @ManyToOne(fetch = FetchType.LAZY)
    private User              assignee;

    private int               taskStatus       = TaskStatus.PENDING;

    private int               priority         = TaskPriority.NORMAL;

    @Column
    @Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
    private DateTime          createDate       = DateUtils.getCurrentUTCTime();

    @Column
    @Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
    private DateTime          dueDate;

    @Column
    @Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
    private DateTime          completionDate;

    @Column
    @Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
    private DateTime          lastUserAssignmentTimestamp;

    private String            name;

    private String            description;

    private String            refUUId;

    public User getAssignee() {
        return assignee;
    }

    public void setAssignee(User assignee) {
        this.assignee = assignee;
    }

    public DateTime getLastUserAssignmentTimestamp() {
        return lastUserAssignmentTimestamp;
    }

    public void setLastUserAssignmentTimestamp(DateTime userAssignmentChangeTimestamp) {
        this.lastUserAssignmentTimestamp = userAssignmentChangeTimestamp;
    }

    public int getTaskStatus() {
        return taskStatus;
    }

    public void setTaskStatus(int status) {
        this.taskStatus = status;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public DateTime getDueDate() {
        return dueDate;
    }

    public void setDueDate(DateTime dueDate) {
        this.dueDate = dueDate;
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

    public DateTime getCreateDate() {
        return createDate;
    }

    public void setCreateDate(DateTime createDate) {
        this.createDate = createDate;
    }

    public DateTime getCompletionDate() {
        return completionDate;
    }

    public void setCompletionDate(DateTime completionDate) {
        this.completionDate = completionDate;
    }

    public String getRefUUId() {
        return refUUId;
    }

    public void setRefUUId(String refUUId) {
        this.refUUId = refUUId;
    }

}