package com.nucleus.rules.assignmentmatrix.service;

import com.nucleus.entity.BaseEntity;
import com.nucleus.rules.simulation.service.SimulationAssignmentMatrixVO;
import org.springframework.ui.ModelMap;

import java.util.List;
import java.util.Map;

public interface IRuleAssginmentSimulation {

    Map<String, List<SimulationAssignmentMatrixVO>> handleAssignmentSimulation(BaseEntity baseEntity, Map<Object, Object> contextMap, ModelMap map, Long[] assignmentMatrixIds, String entityType);

    Map<String, List<SimulationAssignmentMatrixVO>> handleRuleSimulationOperation(BaseEntity baseEntity, Map<Object, Object> contextMap, ModelMap map, Long[] ruleMatrixIds, String entityType);
}
