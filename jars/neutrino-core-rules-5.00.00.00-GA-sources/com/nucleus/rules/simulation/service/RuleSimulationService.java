/**
 * This file and a proportion of its content is copyright of Nucleus Software Exports Limited - © 2012. All rights reserved.
 * Any redistribution or reproduction of part or all of the contents in any form is prohibited other than the following:
 * - you cannot print or download to a local hard disk extract contents either part or full for personal/ commercial/
 * academic or any other use
 * - you may not copy the content to individual/ third parties for any type of use, either as compiled or source format
 * without the knowledge and consent of Nucleus Software
 * - You may not, except with our express written permission, distribute or commercially exploit the content. Nor may you
 * transmit it or store it in any other web site or other form of electronic retrieval system.
 */
package com.nucleus.rules.simulation.service;

import java.util.List;
import java.util.Map;

import com.nucleus.entity.BaseEntity;
import com.nucleus.rules.model.EntityType;
import com.nucleus.rules.model.Parameter;
import com.nucleus.rules.model.Rule;
import com.nucleus.rules.model.ScriptRule;
import com.nucleus.service.BaseService;

/**
 * The Interface RuleSimulationService.
 *
 * @author Nucleus Software Exports Limited
 */
public interface RuleSimulationService extends BaseService {

    /**
     * Simulate Rules For Loan Application.
     *
     * @param rule the rule
     * @param contextMap the context map
     * @return the simulation rule vo
     */
    /* public SimulationResultVO ruleSimulationAudit(Map<Object, Map<Object, Object>> ruleSimulationAudit,
             Map<Object, Object> Objectmap, String invocationPoint);*/

    public SimulationRuleVO ruleSimulationParametersForSingleRule(Rule rule, Map<Object, Object> contextMap);

    /**
     * Evaluate script rule.
     *
     * @param scriptRule the script rule
     * @param contextMap the context map
     * @param isStrictEvaluation the is strict evaluation
     * @return the char
     */
    public char evaluateScriptRule(ScriptRule scriptRule, Map contextMap, boolean isStrictEvaluation);

    /* public SimulationRuleVO ruleSimulationForScriptRule(Rule rule, char result);*/

    /**
     * Find.
     *
     * @param id the id
     * @param className the class name
     * @return the object
     */
    public Object find(Long id, Class className);

    /**
     * Find all.
     *
     * @param className the class name
     * @return the list
     */
    public List findAll(Class className);

    /**
     * It will load the Rules/Rule Sets mapped with the invocation point,
     * Evaluate the Rules/Rule Set and executes the actions on the basis of the Basis of the action mapped to the Rules/Rule Set.
     * It will return the result map of rule execution
     *
     * @param invocationPoint the invocation point
     * @param map the map
     * @param simulationResultVO the simulation result vo
     * @return the simulation result vo
     */

    public SimulationResultVO invokeRuleForSimulation(String invocationPoint, Map<Object, Object> map,
            List<SimulationResultVO> simulationResultVO);

    /**
     * Encrypt script rule.
     *
     * @param scriptRuleExp the script rule exp
     * @return the string
     */
    public String encryptScriptRule(String scriptRuleExp);

    /**
     * Populate context object.
     *
     * @param <T> the generic type
     * @param baseEntity the base entity
     * @param entityClass the entity class
     * @return the map
     */
    public <T extends BaseEntity> Map<Object, Object> populateContextObject(T baseEntity, Class<T> entityClass);

    /**
     * List entity process.
     *
     * @param entityType the entity type
     * @param entityTypeFilterCriteria the entity type filter criteria
     * @return the list
     * @throws ClassNotFoundException the class not found exception
     */
    public List<BaseEntity> listEntityProcess(EntityType entityType, EntityTypeFilterCriteria entityTypeFilterCriteria)
            throws ClassNotFoundException;

    /**
     * Simulate paramter.
     *
     * @param parameter the parameter
     * @param contextMap the context map
     * @return the list
     */
    public List<SimulationParameterVO> simulateParamter(Parameter parameter, Map<Object, Object> contextMap);

}
