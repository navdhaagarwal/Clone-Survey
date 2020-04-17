package com.nucleus.rules.assignmentmatrix.service;

import java.util.Map;

import com.nucleus.rules.model.assignmentMatrix.AssignmentMaster;
import com.nucleus.rules.model.ruleMatrixMaster.RuleMatrixMaster;
import com.nucleus.rules.simulation.service.SimulationAssignmentMatrixVO;

/**
 * 
 * @author Nucleus Software Exports Limited
 * This class is required to execute the assignment Matrix
 */

public interface AssignmentMatrixExecutionService {

    /**
     * 
     * Method to execute the Assignment Matrix Master
     * This also evaluates to Assignment Action
     * @param assignmentMaster
     * @param map
     */

    public void executeAssignMatrix(AssignmentMaster assignmentMaster, Map map);

    /**
     * Execute rule matrix.
     *
     * @param ruleMatrixMaster the rule matrix master
     * @param map the map
     */
    public void executeRuleMatrix(RuleMatrixMaster ruleMatrixMaster, Map map);

    void loadSimulationAssignmentMatrixVO(AssignmentMaster assignmentMaster,SimulationAssignmentMatrixVO assignmentMatrixVO, Map<Object, Object> contextMap);

    void loadSimulationRuleMatrixVO(RuleMatrixMaster ruleMatrixMaster,SimulationAssignmentMatrixVO assignmentMatrixVO, Map<Object, Object> contextMap);

}
