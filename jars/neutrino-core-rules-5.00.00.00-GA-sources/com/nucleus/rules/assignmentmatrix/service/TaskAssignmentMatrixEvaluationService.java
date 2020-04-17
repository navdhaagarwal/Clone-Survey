package com.nucleus.rules.assignmentmatrix.service;

import java.util.Map;

import com.nucleus.rules.model.assignmentMatrix.TaskAssignmentMaster;

/**
 * 
 * @author Nucleus Software Exports Limited
 * This class is required to execute the assignment Matrix
 */

public interface TaskAssignmentMatrixEvaluationService {

    /**
     * 
     * Method to execute the Task Assignment Matrix Master
     * this return the values
     * @param taskAssignmentMaster
     * @param map
     * @return
     */

    public Map<Object, Object> executeTaskAssignMatrix(TaskAssignmentMaster taskAssignmentMaster, Map map);

}
