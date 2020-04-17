package com.nucleus.rules.model.ruleMatrixMaster.pojo;

import com.nucleus.rules.model.Rule;

import java.io.Serializable;
import java.util.*;

import javax.persistence.ManyToOne;


public class AssignmentSetVO implements Serializable{

    private String   index;
    private String   assignmentSetName;
    private Integer  assignmentPriority;
    private Rule     assignmentSetRule;
    private Boolean    executeAll;
    private Boolean    defaultSet;
    private Date        effectiveFrom;
    private Date        efffectiveTill;
    private Integer     bufferDays;

    public String getIndex() {
        return index;
    }

    public void setIndex(String index) {
        this.index = index;
    }

    public String getAssignmentSetName() {
        return assignmentSetName;
    }

    public void setAssignmentSetName(String assignmentSetName) {
        this.assignmentSetName = assignmentSetName;
    }

    public Integer getAssignmentPriority() {
        return assignmentPriority;
    }

    public void setAssignmentPriority(Integer assignmentPriority) {
        this.assignmentPriority = assignmentPriority;
    }

    public Rule getAssignmentSetRule() {
        return assignmentSetRule;
    }

    public void setAssignmentSetRule(Rule assignmentSetRule) {
        this.assignmentSetRule = assignmentSetRule;
    }

    public Boolean getExecuteAll() {
        return executeAll;
    }

    public void setExecuteAll(Boolean executeAll) {
        this.executeAll = executeAll;
    }

    public Boolean getDefaultSet() {
        return defaultSet;
    }

    public void setDefaultSet(Boolean defaultSet) {
        this.defaultSet = defaultSet;
    }

    public Date getEffectiveFrom() {
        return effectiveFrom;
    }

    public void setEffectiveFrom(Date effectiveFrom) {
        this.effectiveFrom = effectiveFrom;
    }

    public Date getEfffectiveTill() {
        return efffectiveTill;
    }

    public void setEfffectiveTill(Date efffectiveTill) {
        this.efffectiveTill = efffectiveTill;
    }

    public Integer getBufferDays() {
        return bufferDays;
    }

    public void setBufferDays(Integer bufferDays) {
        this.bufferDays = bufferDays;
    }
}
