package com.nucleus.rules.model.ruleMatrixMaster.pojo;

import java.io.Serializable;
import java.util.List;


public class RuleMatrixRowData implements Serializable{

    private Integer index;
    private String priority;
    private List<RuleMatrixColumnData> ifColumnData;
    private List<RuleMatrixColumnData> thenColumnData;

    public Integer getIndex() {
        return index;
    }

    public void setIndex(Integer index) {
        this.index = index;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public List<RuleMatrixColumnData> getIfColumnData() {
        return ifColumnData;
    }

    public void setIfColumnData(List<RuleMatrixColumnData> ifColumnData) {
        this.ifColumnData = ifColumnData;
    }

    public List<RuleMatrixColumnData> getThenColumnData() {
        return thenColumnData;
    }

    public void setThenColumnData(List<RuleMatrixColumnData> thenColumnData) {
        this.thenColumnData = thenColumnData;
    }
}
