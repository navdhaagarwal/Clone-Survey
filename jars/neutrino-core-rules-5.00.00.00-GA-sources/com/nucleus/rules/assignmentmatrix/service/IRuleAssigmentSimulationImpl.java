package com.nucleus.rules.assignmentmatrix.service;

import com.nucleus.entity.BaseEntity;
import com.nucleus.master.BaseMasterService;
import com.nucleus.rules.model.ObjectGraphTypes;
import com.nucleus.rules.model.Parameter;
import com.nucleus.rules.model.ParameterDataType;
import com.nucleus.rules.model.ScriptRule;
import com.nucleus.rules.model.assignmentMatrix.*;
import com.nucleus.rules.model.ruleMatrixMaster.RuleMatrixMaster;
import com.nucleus.rules.service.ExpressionEvaluatorImpl;
import com.nucleus.rules.service.RuleConstants;
import com.nucleus.rules.service.RuleService;
import com.nucleus.rules.simulation.service.AssignmentMatrixRowDataPojo;
import com.nucleus.rules.simulation.service.AssignmentSetPojo;
import com.nucleus.rules.simulation.service.SimulationAssignmentMatrixVO;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections4.MapIterator;
import org.apache.commons.collections4.iterators.EntrySetMapIterator;
import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.springframework.ui.ModelMap;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Named("defaultAssigmentSimulation")
public class IRuleAssigmentSimulationImpl implements IRuleAssginmentSimulation {

    @Inject
    @Named(value = "assignmentMatrixService")
    private AssignmentMatrixService assignmentMatrixService;

    @Inject
    @Named("assignmentMatrixExecutionService")
    private AssignmentMatrixExecutionService assignmentMatrixExecutionService;

    @Inject
    @Named("expressionEvaluator")
    private ExpressionEvaluatorImpl expressionEvaluator;

    @Inject
    @Named("ruleService")
    private RuleService ruleService;

    @Inject
    @Named("baseMasterService")
    private BaseMasterService baseMasterService;

    @Override
    public Map<String, List<SimulationAssignmentMatrixVO>> handleAssignmentSimulation(BaseEntity baseEntity, Map<Object, Object> contextMap, ModelMap map, Long[] assignmentMatrixIds, String entityType) {
        List<SimulationAssignmentMatrixVO> assignmentMatrixVOs = new ArrayList<>();
        Map<String, List<SimulationAssignmentMatrixVO>> finalMatricesSimulationResult = new HashMap<>();
        AssignmentMaster assignmentMaster;
        for (long id : assignmentMatrixIds) {
            assignmentMaster = assignmentMatrixService.getAssignmentMatrixById(id);
            if (null != assignmentMaster) {
                assignmentMatrixExecutionService.executeAssignMatrix(assignmentMaster, contextMap);
                SimulationAssignmentMatrixVO assignmentMatrixVO = new SimulationAssignmentMatrixVO();
                assignmentMatrixExecutionService.loadSimulationAssignmentMatrixVO(assignmentMaster, assignmentMatrixVO, contextMap);
                assignmentMatrixVOs.add(assignmentMatrixVO);
            }
        }
        finalMatricesSimulationResult.put(baseEntity.getDisplayName(), assignmentMatrixVOs);
        return finalMatricesSimulationResult;
    }

    @Override
    public Map<String, List<SimulationAssignmentMatrixVO>> handleRuleSimulationOperation(BaseEntity baseEntity, Map<Object, Object> contextMap, ModelMap map, Long[] ruleMatrixIds, String entityType) {
        List<SimulationAssignmentMatrixVO> assignmentMatrixVOs = new ArrayList<SimulationAssignmentMatrixVO>();
        RuleMatrixMaster ruleMatrixMaster = null;
        Map<String, List<SimulationAssignmentMatrixVO>> finalMatricesSimulationResult = new HashMap<String, List<SimulationAssignmentMatrixVO>>();
        for (long id : ruleMatrixIds) {
            ruleMatrixMaster = baseMasterService.findById(RuleMatrixMaster.class, id);
            if (null != ruleMatrixMaster) {
                assignmentMatrixExecutionService.executeRuleMatrix(ruleMatrixMaster, contextMap);
                SimulationAssignmentMatrixVO assignmentMatrixVO = new SimulationAssignmentMatrixVO();
                assignmentMatrixExecutionService.loadSimulationRuleMatrixVO(ruleMatrixMaster, assignmentMatrixVO, contextMap);
                assignmentMatrixVOs.add(assignmentMatrixVO);
            }
        }
        finalMatricesSimulationResult.put(baseEntity.getDisplayName(), assignmentMatrixVOs);
        return finalMatricesSimulationResult;
    }
}
