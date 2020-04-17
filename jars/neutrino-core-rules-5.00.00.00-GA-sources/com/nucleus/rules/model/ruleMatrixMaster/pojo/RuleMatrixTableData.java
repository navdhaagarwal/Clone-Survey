package com.nucleus.rules.model.ruleMatrixMaster.pojo;

import java.io.Serializable;
import java.util.List;

import com.nucleus.rules.model.ModuleName;
import com.nucleus.rules.model.ruleMatrixMaster.RuleMatrixGridData;


public class RuleMatrixTableData implements Serializable{

    private List<RuleMatrixRowData> ruleMatrixRowDataList;

    private List<RuleMatrixGridData> ifTableGridData;

    private List<RuleMatrixGridData> thenTableGridData;

    private String sourceProductTableData;

    private ModuleName moduleNameTableData;

    private AssignmentSetVO assignmentSet;

    private Integer assignmentIndex;

    public List<RuleMatrixRowData> getRuleMatrixRowDataList() {
        return ruleMatrixRowDataList;
    }

    public void setRuleMatrixRowDataList(List<RuleMatrixRowData> ruleMatrixRowDataList) {
        this.ruleMatrixRowDataList = ruleMatrixRowDataList;
    }

    public List<RuleMatrixGridData> getIfTableGridData() {
        return ifTableGridData;
    }

    public void setIfTableGridData(List<RuleMatrixGridData> ifTableGridData) {
        this.ifTableGridData = ifTableGridData;
    }

    public List<RuleMatrixGridData> getThenTableGridData() {
        return thenTableGridData;
    }

    public void setThenTableGridData(List<RuleMatrixGridData> thenTableGridData) {
        this.thenTableGridData = thenTableGridData;
    }

    public String getSourceProductTableData() {
        return sourceProductTableData;
    }

    public void setSourceProductTableData(String sourceProductTableData) {
        this.sourceProductTableData = sourceProductTableData;
    }

    public ModuleName getModuleNameTableData() {
        return moduleNameTableData;
    }

    public void setModuleNameTableData(ModuleName moduleNameTableData) {
        this.moduleNameTableData = moduleNameTableData;
    }

    public AssignmentSetVO getAssignmentSet() {
        return assignmentSet;
    }

    public void setAssignmentSet(AssignmentSetVO assignmentSet) {
        this.assignmentSet = assignmentSet;
    }

    public Integer getAssignmentIndex() {
        return assignmentIndex;
    }

    public void setAssignmentIndex(Integer assignmentIndex) {
        this.assignmentIndex = assignmentIndex;
    }
}
