package com.nucleus.rules.simulation.service;

import java.io.Serializable;
import java.util.Map;

public class AssignmentMatrixRowDataPojo implements Serializable {

    private static final long   serialVersionUID = 8138092206966107876L;

    private String              assignmentMatrixRowDataName;

    private String              ruleResults;

    private Map<String, String> assignmentValueMap;

    private String              ruleExpression;

    private Integer             priority;

    private Map<Integer, AssignmentMatrixRowDataElementsPojo> rowDataElementsPojoMap;

    /**
     * @return the assignmentMatrixRowDataName
     */
    public String getAssignmentMatrixRowDataName() {
        return assignmentMatrixRowDataName;
    }

    /**
     * @param assignmentMatrixRowDataName
     *            the assignmentMatrixRowDataName to set
     */
    public void setAssignmentMatrixRowDataName(String assignmentMatrixRowDataName) {
        this.assignmentMatrixRowDataName = assignmentMatrixRowDataName;
    }

    /**
     * @return the ruleResults
     */
    public String getRuleResults() {
        return ruleResults;
    }

    /**
     * @param ruleResults
     *            the ruleResults to set
     */
    public void setRuleResults(String ruleResults) {
        this.ruleResults = ruleResults;
    }

    /**
     * @return the assignmentValueMap
     */
    public Map<String, String> getAssignmentValueMap() {
        return assignmentValueMap;
    }

    /**
     * @param assignmentValueMap the assignmentValueMap to set
     */
    public void setAssignmentValueMap(Map<String, String> assignmentValueMap) {
        this.assignmentValueMap = assignmentValueMap;
    }

    /**
     * @return the ruleExpression
     */
    public String getRuleExpression() {
        return ruleExpression;
    }

    /**
     * @param ruleExpression the ruleExpression to set
     */
    public void setRuleExpression(String ruleExpression) {
        this.ruleExpression = ruleExpression;
    }


    public Map<Integer, AssignmentMatrixRowDataElementsPojo> getRowDataElementsPojoMap() {
        return rowDataElementsPojoMap;
    }

    public void setRowDataElementsPojoMap(Map<Integer, AssignmentMatrixRowDataElementsPojo> rowDataElementsPojoMap) {
        this.rowDataElementsPojoMap = rowDataElementsPojoMap;
    }

    public Integer getPriority() {
        return priority;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
    }
}
