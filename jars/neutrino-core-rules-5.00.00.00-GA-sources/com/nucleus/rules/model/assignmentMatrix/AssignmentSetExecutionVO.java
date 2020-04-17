package com.nucleus.rules.model.assignmentMatrix;

import java.util.*;

public class AssignmentSetExecutionVO {

    private String assignmentSetName;
    private String priority;
    private Date effectiveFrom;
    private Date effectiveTill;
    private Integer bufferDays;
    private Map<Object, Object> result;

    public String getAssignmentSetName() {
        return assignmentSetName;
    }

    public void setAssignmentSetName(String assignmentSetName) {
        this.assignmentSetName = assignmentSetName;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public Date getEffectiveFrom() {
        return effectiveFrom;
    }

    public void setEffectiveFrom(Date effectiveFrom) {
        this.effectiveFrom = effectiveFrom;
    }

    public Date getEffectiveTill() {
        return effectiveTill;
    }

    public void setEffectiveTill(Date effectiveTill) {
        this.effectiveTill = effectiveTill;
    }

    public Integer getBufferDays() {
        return bufferDays;
    }

    public void setBufferDays(Integer bufferDays) {
        this.bufferDays = bufferDays;
    }

    public Map<Object, Object> getResult() {
        return result;
    }

    public void setResult(Map<Object, Object> result) {
        this.result = result;
    }
}
