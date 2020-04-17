package com.nucleus.rules.simulation.service;

import java.io.Serializable;
import java.util.List;

public class AssignmentSetPojo implements Serializable {

    private static final long                 serialVersionUID = -6691346687522021892L;

    private String                            assignmentSetName;

    private List<AssignmentMatrixRowDataPojo> assignmentMatrixRowDataPojoList;

    /**
     * @return the assignmentSetName
     */
    public String getAssignmentSetName() {
        return assignmentSetName;
    }

    /**
     * @param assignmentSetName the assignmentSetName to set
     */
    public void setAssignmentSetName(String assignmentSetName) {
        this.assignmentSetName = assignmentSetName;
    }

    /**
     * @return the assignmentMatrixRowDataPojoList
     */
    public List<AssignmentMatrixRowDataPojo> getAssignmentMatrixRowDataPojoList() {
        return assignmentMatrixRowDataPojoList;
    }

    /**
     * @param assignmentMatrixRowDataPojoList the assignmentMatrixRowDataPojoList to set
     */
    public void setAssignmentMatrixRowDataPojoList(List<AssignmentMatrixRowDataPojo> assignmentMatrixRowDataPojoList) {
        this.assignmentMatrixRowDataPojoList = assignmentMatrixRowDataPojoList;
    }

}
