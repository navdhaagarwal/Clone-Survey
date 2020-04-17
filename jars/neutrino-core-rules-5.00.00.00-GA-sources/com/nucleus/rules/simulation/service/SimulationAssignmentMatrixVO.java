/**
 * This file and a proportion of its content is copyright of Nucleus Software Exports Limited - © 2014. All rights reserved.
 * Any redistribution or reproduction of part or all of the contents in any form is prohibited other than the following:
 * - you cannot print or download to a local hard disk extract contents either part or full for personal/ commercial/
 * academic or any other use
 * - you may not copy the content to individual/ third parties for any type of use, either as compiled or source format
 * without the knowledge and consent of Nucleus Software
 * - You may not, except with our express written permission, distribute or commercially exploit the content. Nor may you
 * transmit it or store it in any other web site or other form of electronic retrieval system.
 */
package com.nucleus.rules.simulation.service;

import java.io.Serializable;
import java.util.List;

/**
 * @author Nucleus Software Exports Limited
 */
public class SimulationAssignmentMatrixVO implements Serializable {

    private static final long       serialVersionUID = -1342992348625931348L;

    private String                  assignmentMatrixName;

    private List<AssignmentSetPojo> assignmentSetPojoList;

    /**
     * @return the assignmentSetPojoList
     */
    public List<AssignmentSetPojo> getAssignmentSetPojoList() {
        return assignmentSetPojoList;
    }

    /**
     * @param assignmentSetPojoList the assignmentSetPojoList to set
     */
    public void setAssignmentSetPojoList(List<AssignmentSetPojo> assignmentSetPojoList) {
        this.assignmentSetPojoList = assignmentSetPojoList;
    }

    /**
     * @return the assignmentMatrixName
     */
    public String getAssignmentMatrixName() {
        return assignmentMatrixName;
    }

    /**
     * @param assignmentMatrixName the assignmentMatrixName to set
     */
    public void setAssignmentMatrixName(String assignmentMatrixName) {
        this.assignmentMatrixName = assignmentMatrixName;
    }

}
