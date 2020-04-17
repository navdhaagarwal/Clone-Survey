/**
 * Copyright Nucleus Software India Pvt Ltd. All rights reserved.
 */
package com.nucleus.rules.assignmentmatrix.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Named;

import com.nucleus.rules.model.AllocationEntityMapping;
import com.nucleus.rules.service.GridCriteriaRuleConstants;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.Hibernate;

import com.nucleus.businessmapping.entity.UserOrgBranchMapping;
import com.nucleus.core.team.entity.Team;
import com.nucleus.core.team.service.TeamService;
import com.nucleus.core.validation.util.NeutrinoValidator;
import com.nucleus.entity.Entity;
import com.nucleus.logging.BaseLoggers;
import com.nucleus.persistence.EntityDao;
import com.nucleus.rules.model.Rule;
import com.nucleus.rules.model.assignmentMatrix.AssignmentConstants;
import com.nucleus.rules.model.assignmentMatrix.AssignmentCriteriaSet;
import com.nucleus.rules.model.assignmentMatrix.AssignmentMatrixAction;
import com.nucleus.rules.model.assignmentMatrix.AssignmentMatrixRowData;
import com.nucleus.rules.model.assignmentMatrix.AssignmentSet;
import com.nucleus.rules.model.assignmentMatrix.TaskAssignmentMaster;
import com.nucleus.rules.service.CompiledExpressionBuilder;
import com.nucleus.rules.service.CriteriaRulesService;
import com.nucleus.rules.service.RuleConstants;
import com.nucleus.rules.taskAssignmentMaster.ColumnDataService;
import com.nucleus.user.User;

/**
 * @author Nucleus Software India Pvt Ltd 
 */

@Named(value = "assignmentMatrixEvaluateService")
public class TaskAssignmentMatrixEvaluationServiceImpl implements TaskAssignmentMatrixEvaluationService {

    @Inject
    @Named("compiledExpressionBuilder")
    private CompiledExpressionBuilder compiledExpressionBuilder;

    @Inject
    @Named(value = "entityDao")
    private EntityDao                 entityDao;

    @Inject
    @Named(value = "columnDataService")
    private ColumnDataService         columnDataService;

    @Inject
    @Named("criteriaRules")
    private CriteriaRulesService      criteriaRulesService;

    @Inject
    @Named("teamService")
    private TeamService               teamService;

    @Inject
    @Named("userAssignmentStrategy")
    private AssignmentStrategy        userAssignmentStrategy;

    @Inject
    @Named("teamAssignmntStrategy")
    private AssignmentStrategy        teamAssignmntStrategy;

    @SuppressWarnings("rawtypes")
    @Override
    public Map<Object, Object> executeTaskAssignMatrix(TaskAssignmentMaster taskAssignmentMaster, Map contextMap) {

        if (null == taskAssignmentMaster) {
            return null;
        }

        List<AssignmentSet> assignmentSetsList = taskAssignmentMaster.getAssignmentSet();
        NeutrinoValidator.notEmpty(assignmentSetsList, "No Assignment Set is present in Assignment Master :"
                + taskAssignmentMaster.getCode());

        /*
         * Sort On the Basis Of priority
        */

        List<AssignmentSet> tempAssignmentSet = new ArrayList<AssignmentSet>();
        for (AssignmentSet assignmentSet : assignmentSetsList) {
            tempAssignmentSet.add(assignmentSet);
        }

        sortAssignmentSetByPriority(tempAssignmentSet);

        /*
         * Execute assignmentSets by rules
         */
        try {
            return executeAssignmentSetsRules(tempAssignmentSet, contextMap);
        } catch (ClassNotFoundException e) {
            BaseLoggers.exceptionLogger.error("Exception occured while finding the class" + e.getMessage());
        } catch (Exception e) {
        	BaseLoggers.exceptionLogger.error("Exception occured while executing task assignment" , e);
        	throw e;
		}

        return null;
    }

    /**
     * 
     * Sort On the Basis Of priority
     * @param assignmentSetsList
     */

    private void sortAssignmentSetByPriority(List<AssignmentSet> assignmentSetsList) {
        Collections.sort(assignmentSetsList, new Comparator<AssignmentSet>() {
            @Override
            public int compare(AssignmentSet o1, AssignmentSet o2) {

                if (o1.getPriority() == o2.getPriority()) {
                    return 0;
                }

                return o1.getPriority() < o2.getPriority() ? -1 : 1;
            }
        });
    }

    /**
     * 
     * Execute Task Assignment Row rules and assignment actions
     * @param assignmentSetsList
     * @param map
     * @throws ClassNotFoundException 
     */

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private Map<Object, Object> executeAssignmentSetsRules(List<AssignmentSet> assignmentSetsList, Map contextMap)
            throws ClassNotFoundException {

        Rule rule = null;
        Boolean ruleResult = null;
        char res;

        Map<Object, Object> resultMap = new HashMap<Object, Object>();

        if (assignmentSetsList == null || assignmentSetsList.size() == 0) {
            return resultMap;
        }

        for (AssignmentSet assignmentSet : assignmentSetsList) {

            Hibernate.initialize(assignmentSet);

            if (assignmentSet instanceof AssignmentCriteriaSet) {
                resultMap = evaluateCriteriaSet(contextMap, (AssignmentCriteriaSet) assignmentSet);
                if (resultMap != null && resultMap.size() > 0) {
                    return resultMap;
                }
            }

            // Execute AssignmentSet

            List<AssignmentMatrixRowData> assignmentMatrixRowDataList = assignmentSet.getAssignmentMatrixRowData();

            if (null != assignmentMatrixRowDataList && assignmentMatrixRowDataList.size() > 0) {

                for (AssignmentMatrixRowData assignmentMatrixRowData : assignmentMatrixRowDataList) {

                    Hibernate.initialize(assignmentMatrixRowData);
                    if(assignmentMatrixRowData.getCriteriaRules()!=null){
                        Hibernate.initialize(assignmentMatrixRowData.getCriteriaRules());
                    }

                    if (assignmentMatrixRowData.getRule() == null) {
                        continue;
                    }
                    Hibernate.initialize(assignmentMatrixRowData.getRule());

                    rule = assignmentMatrixRowData.getRule();

                    res = compiledExpressionBuilder.evaluateRule(rule.getId(), contextMap);
                    ruleResult = (res == RuleConstants.RULE_RESULT_PASS) ? true : false;
                    if(null !=assignmentMatrixRowData.getRuleExp()){
                    	String ruleExp=assignmentMatrixRowData.getRuleExp();
                        String[] ruleArray = ruleExp.split(",");
                        for(int i=0;i<ruleArray.length;i++){
                            String[]  ruleData = ruleArray[i].split(":");
                            Long ruleId=Long.valueOf(ruleData[0].split("_")[0]);
                            char ruleRes=compiledExpressionBuilder.evaluateRule(ruleId, contextMap);
                            Boolean ruleR= (ruleRes == RuleConstants.RULE_RESULT_PASS) ? true : false;
                            if(((!("*".equalsIgnoreCase(ruleData[1]))) && !ruleR && !("F".equalsIgnoreCase(ruleData[1])))||(ruleR && ("F".equalsIgnoreCase(ruleData[1])))){
                                ruleResult=Boolean.FALSE;
                            }
                        }
                    }
                    if (ruleResult) {
                        if(assignmentMatrixRowData.getCriteriaRules()!=null && assignmentMatrixRowData.getCriteriaRules().getRuleGroup()!=null){

                            List<AllocationEntityMapping> allocationEntityMappingList = entityDao.findAll(AllocationEntityMapping.class);
                            contextMap.put(GridCriteriaRuleConstants.ALLOCATION_ENTITY_MAPPING_LIST,allocationEntityMappingList );
                            resultMap  = evaluateCriteriaSetWithConditions(contextMap, assignmentMatrixRowData);
                            if (resultMap != null && resultMap.size() > 0) {
                                return resultMap;
                            }
                        }else{
                            Hibernate.initialize(assignmentMatrixRowData.getAssignmentMatrixAction());
                            return getTaskAssignmentMatrixValues(assignmentMatrixRowData.getAssignmentMatrixAction(), resultMap,
                                    contextMap,assignmentMatrixRowData.getHoldFlag());
                        }

                    }

                }
            }
        }

        return resultMap;
    }

    /**
     * Method to evaluate Task Assignment Matrix Action and return the values.
     *
     * @param assignmentMatrixAction the assignment matrix action
     * @param resultMap the result map
     * @return the task assignment matrix values
     * @throws ClassNotFoundException the class not found exception
     */

    private Map<Object, Object> getTaskAssignmentMatrixValues(AssignmentMatrixAction assignmentMatrixAction,
            Map<Object, Object> resultMap, Map contextMap,Boolean holdFlag) throws ClassNotFoundException {

        if (null != assignmentMatrixAction) {

            // converted json string to map for one row for assignment actions
            Map<String, Object> fieldKeyValueMap = AssignmentMatrixMasterUtility.convertJsonToMap(assignmentMatrixAction
                    .getAssignActionValues());

            Map<String, Object> finalMap = columnDataService.populateResultMap(fieldKeyValueMap, contextMap);
            Object userIdObj = finalMap.get(AssignmentConstants.User.getName());
            Object teamIdObj = finalMap.get(AssignmentConstants.Team.getName());
            String userId = userIdObj == null ? null : String.valueOf(userIdObj);
            String teamId = teamIdObj == null ? null : String.valueOf(teamIdObj);

            if (StringUtils.isNotBlank(userId)) {
                resultMap.put(
                        AssignmentConstants.USER_URI,
                        (AssignmentConstants.User.getName()).concat(":").concat(
                                (String.valueOf(finalMap.get(AssignmentConstants.User.getName())))));
            }

            if (StringUtils.isNotBlank(teamId)) {
                resultMap.put(
                        AssignmentConstants.TEAM_URI,
                        (AssignmentConstants.Team.getName()).concat(":").concat(
                                (String.valueOf(finalMap.get(AssignmentConstants.Team.getName())))));
            }
            if(null != holdFlag && holdFlag){
            	resultMap.put(AssignmentConstants.HOLD_FLAG,holdFlag);
            }

        }
        return resultMap;
    }

    /**
     * Evaluate criteria rules.
     *
     * @param contextObjectMap the context object map
     * @param assignmentCriteriaSet the assignment criteria rule
     * @return the map
     */
    private Map<Object, Object> evaluateCriteriaSet(Map<Object, Object> contextObjectMap,
            AssignmentCriteriaSet assignmentCriteriaSet) {

        Map<Object, Object> criteriaResult = new HashMap<Object, Object>();
        // Evaluate Criteria Rules
        if (null == assignmentCriteriaSet.getCriteriaRules()) {
            return criteriaResult;
        }

        List<? extends Entity> entitiesList = criteriaRulesService.executeRulesCriteria(
                assignmentCriteriaSet.getCriteriaRules(), contextObjectMap);

        if (entitiesList != null && entitiesList.size() > 0) {
            if (UserOrgBranchMapping.class.isAssignableFrom(entitiesList.get(0).getClass())) {

                if (assignmentCriteriaSet.getAllocationStrategy().equals(AssignmentCriteriaSet.LEAST_LOADED_USER)) {
                    List<User> userEntList = new ArrayList<User>();
                    for (Entity entity : entitiesList) {
                        UserOrgBranchMapping userMapping = (UserOrgBranchMapping) entity;
                        if (userMapping.getAssociatedUser() != null) {
                            userEntList.add(userMapping.getAssociatedUser());
                        }
                    }
                    criteriaResult = userAssignmentStrategy.handleEntityList(userEntList);
                }

                else {
                    Set<Team> teamList = new HashSet<Team>();
                    for (Entity entity : entitiesList) {
                        UserOrgBranchMapping userMapping = (UserOrgBranchMapping) entity;
                        User user = userMapping.getAssociatedUser();
                        if (user != null) {
                            teamList.addAll(teamService.getTeamsAssociatedToUserByUserId(user.getId()));

                        }
                    }
                    criteriaResult = teamAssignmntStrategy.handleEntityList(new ArrayList<Team>(teamList));
                }

            } else if (User.class.isAssignableFrom(entitiesList.get(0).getClass())) {

                criteriaResult = userAssignmentStrategy.handleEntityList(entitiesList);
            } else if (Team.class.isAssignableFrom(entitiesList.get(0).getClass())) {

                criteriaResult = teamAssignmntStrategy.handleEntityList(entitiesList);
            }
        }

        return criteriaResult;

    }


    /**
     * Evaluate criteria rules.
     *
     * @param contextObjectMap the context object map
     * @param assignmentCriteriaSet the assignment criteria rule
     * @return the map
     */
    private Map<Object, Object> evaluateCriteriaSetWithConditions(Map<Object, Object> contextObjectMap,
                                                    AssignmentMatrixRowData assignmentMatrixRowData) {

        Map<Object, Object> criteriaResult = new HashMap<>();
        // Evaluate Criteria Rules
        if (null == assignmentMatrixRowData.getCriteriaRules()) {
            return criteriaResult;
        }

        List<? extends Entity> entitiesList = criteriaRulesService.executeRulesCriteriaWithConditions(
                assignmentMatrixRowData, contextObjectMap);

        if (entitiesList != null && !entitiesList.isEmpty()) {
            if (assignmentMatrixRowData.getAllocationStrategy().equals(AssignmentCriteriaSet.LEAST_LOADED_USER)) {
                criteriaResult = userAssignmentStrategy.findLeastLoadedEntity(entitiesList);
            } else if (assignmentMatrixRowData.getAllocationStrategy().equals(AssignmentCriteriaSet.LEAST_LOADED_TEAM)) {
                criteriaResult = teamAssignmntStrategy.findLeastLoadedEntity(entitiesList);
            }
        }

        return criteriaResult;

    }

}
