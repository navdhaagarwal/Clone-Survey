package com.nucleus.rules.service;

import java.util.List;
import java.util.Map;

import com.nucleus.entity.Entity;
import com.nucleus.rules.model.CriteriaRules;
import com.nucleus.rules.model.assignmentMatrix.AssignmentMatrixRowData;
import com.nucleus.service.BaseService;

/**
 * 
 * @author Nucleus Software Exports Limited
 * Service added to evaluate rules as a criteria
 */
public interface CriteriaRulesService extends BaseService {

    /**
     * 
     * Execute Criteria Rules
     * @param criteriaRules
     * @param map
     * @return
     */
    public List<? extends Entity> executeRulesCriteria(CriteriaRules criteriaRules, Map<Object, Object> map);

    public List<? extends Entity> executeRulesCriteriaWithConditions(AssignmentMatrixRowData assignmentMatrixRowData, Map<Object, Object> map);
}
