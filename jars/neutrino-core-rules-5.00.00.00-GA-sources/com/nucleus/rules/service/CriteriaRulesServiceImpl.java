package com.nucleus.rules.service;

import java.util.*;
import java.util.regex.Pattern;

import javax.inject.Inject;
import javax.inject.Named;

import com.mongodb.util.Hash;
import com.nucleus.rules.model.*;
import com.nucleus.rules.model.assignmentMatrix.AssignmentCriteriaSet;
import com.nucleus.rules.model.assignmentMatrix.AssignmentMatrixRowData;
import net.bull.javamelody.MonitoredWithSpring;

import com.nucleus.dao.query.JPAQueryExecutor;
import com.nucleus.entity.ApprovalStatus;
import com.nucleus.entity.BaseEntity;
import com.nucleus.entity.Entity;
import com.nucleus.logging.BaseLoggers;
import com.nucleus.master.BaseMasterEntity;
import com.nucleus.service.BaseServiceImpl;
import org.apache.commons.lang3.StringUtils;
import org.apache.xpath.operations.Bool;

import static java.util.stream.Collectors.groupingBy;

@Named(value = "criteriaRules")
@MonitoredWithSpring(name = "criteriaRules_IMPL_")
public class CriteriaRulesServiceImpl extends BaseServiceImpl implements CriteriaRulesService {

    @Inject
    @Named(value = "criteriaExpression")
    private CriteriaExpressionBuilder criteriaExpressionBuilder;

    @Inject
    @Named("ruleExpressionBuilder")
    RuleExpressionBuilder             ruleExpressionBuilder;

    @Override
/*    @MonitoredWithSpring(name = "CRSI_EXECUTE_RULES_CRITERIA")*/
    public List<? extends Entity> executeRulesCriteria(CriteriaRules criteriaRules, Map<Object, Object> map) {
        List<Integer> statusList = new ArrayList<Integer>();
        statusList.add(ApprovalStatus.APPROVED);
        statusList.add(ApprovalStatus.APPROVED_MODIFIED);
        statusList.add(ApprovalStatus.APPROVED_DELETED_IN_PROGRESS);

        if (criteriaRules != null && map != null) {
            if (null != criteriaRules.getEntityType()) {
                EntityType entityType = criteriaRules.getEntityType();
                Class entityName = null;
                List<? extends Entity> result = null;
                boolean isBaseMasterEntity = false;
                try {
                    entityName = Class.forName(entityType.getClassName());
                    Class superClass = BaseMasterEntity.class;
                    isBaseMasterEntity = superClass.isAssignableFrom(entityName);

                    String packageClassName = criteriaRules.getEntityType().getClassName();

                    String className = packageClassName.substring(packageClassName.lastIndexOf(".") + 1,
                            packageClassName.length());

                    StringBuilder queryString = buildQuery(criteriaRules, map, isBaseMasterEntity, packageClassName,
                            className);

                    JPAQueryExecutor<? extends Entity> jpaQueryExecutor = new JPAQueryExecutor<BaseEntity>(
                            queryString.toString());

                    jpaQueryExecutor.addParameter("statusList", statusList);
                    result = entityDao.executeQuery(jpaQueryExecutor);

                } catch (ClassNotFoundException e) {
                    BaseLoggers.exceptionLogger.error("Class not found for criteria rule with id " + criteriaRules.getId()
                            + "and name: " + criteriaRules.getName() + " :" + e.getMessage());
                }
                return result;
            }
        }
        return null;
    }


    @Override
    public List<? extends Entity> executeRulesCriteriaWithConditions(AssignmentMatrixRowData assignmentMatrixRowData, Map<Object, Object> map) {
        CriteriaRules criteriaRules = assignmentMatrixRowData.getCriteriaRules();
        List<Integer> statusList = new ArrayList<>();
        statusList.add(ApprovalStatus.APPROVED);
        statusList.add(ApprovalStatus.APPROVED_MODIFIED);
        statusList.add(ApprovalStatus.APPROVED_DELETED_IN_PROGRESS);

        if (criteriaRules != null && map != null && null != criteriaRules.getEntityType()) {
                List<? extends Entity> result = null;
                try {

                    StringBuilder queryString = buildQueryWithConditions(assignmentMatrixRowData, map);
                    Boolean execute = true;
                    if(map.containsKey(GridCriteriaRuleConstants.LEFT_EXPRESSION_MAP)){
                        Map<String,String> leftExpressionMap= (HashMap)map.get(GridCriteriaRuleConstants.LEFT_EXPRESSION_MAP);
                        for (String key : leftExpressionMap.keySet()) {
                            if(!key.contains(GridCriteriaRuleConstants.CONTEXT_OBJECT_ALLOCATION)){
                                return null;
                            }
                        }
                    }

                    JPAQueryExecutor<? extends Entity> jpaQueryExecutor = new JPAQueryExecutor<>(
                            queryString.toString());
                    if(queryString.toString().contains(GridCriteriaRuleConstants.STATUS_LIST)) {
                        jpaQueryExecutor.addParameter(GridCriteriaRuleConstants.STATUS_LIST, statusList);
                    }
                    result = entityDao.executeQuery(jpaQueryExecutor);
                } catch (ClassNotFoundException e) {
                    BaseLoggers.exceptionLogger.error("Class not found for criteria rule with id " + criteriaRules.getId()
                            + "and name: " + criteriaRules.getName() + " :" + e.getMessage());
                } catch (Exception e){
                    BaseLoggers.exceptionLogger.error("Unable to execute query for criteria rule with id " + criteriaRules.getId()
                            + "and name: " + criteriaRules.getName() + " :" + e.getMessage());
                }
                return result;
        }
        return null;
    }


    /**
     * 
     * Method to build the query
     * 
     * @param criteriaRules
     * @param map
     * @param isBaseMasterEntity
     * @param packageClassName
     * @param className
     * @return
     */
    private StringBuilder buildQuery(CriteriaRules criteriaRules, Map<Object, Object> map, boolean isBaseMasterEntity,
            String packageClassName, String className) {
        StringBuilder queryString = new StringBuilder();

        queryString.append("SELECT " + RuleConstants.CONTEXT_OBJECT + className + " FROM " + packageClassName + " "
                + RuleConstants.CONTEXT_OBJECT + className + " ");

        Map<String, String> joinsMap = new LinkedHashMap<String, String>();

        RuleGroupExpression ruleGroupExpression = ruleExpressionBuilder.parseRuleGroupFlatExpression(criteriaRules
                .getRuleGroup().getRuleGroupExpression());

        String expression = criteriaExpressionBuilder.buildCriteriaRuleQuery(ruleGroupExpression, map, joinsMap);

        String fromClause = buildFromClause(joinsMap);
        queryString.append(fromClause);
        queryString.append(" WHERE (");
        queryString.append(expression);

        if (isBaseMasterEntity) {
            queryString.append(" AND (" + RuleConstants.CONTEXT_OBJECT + className
                    + ".masterLifeCycleData.approvalStatus = 0)");

            queryString.append(" AND (" + RuleConstants.CONTEXT_OBJECT + className
                    + ".masterLifeCycleData.approvalStatus IN (:statusList) )");

            queryString.append(" AND (" + RuleConstants.CONTEXT_OBJECT + className
                    + ".entityLifeCycleData.snapshotRecord IS NULL OR " + RuleConstants.CONTEXT_OBJECT + className
                    + ".entityLifeCycleData.snapshotRecord = false)");

        }

        queryString.append(")");
        return queryString;
    }



    private StringBuilder buildQueryWithConditions(AssignmentMatrixRowData assignmentMatrixRowData, Map<Object, Object> map) throws ClassNotFoundException {
        CriteriaRules criteriaRules = assignmentMatrixRowData.getCriteriaRules();
        StringBuilder queryString = new StringBuilder();

        Map<String, String> joinsMap = new LinkedHashMap<>();
        Set<String> baseContextObjectNameList = new TreeSet<>();
        Map<String,String> leftExpressionMap= new HashMap<>();

        RuleGroupExpression ruleGroupExpression = ruleExpressionBuilder.parseRuleGroupFlatExpression(criteriaRules
                .getRuleGroup().getRuleGroupExpression());
        List<AllocationEntityMapping> allocationEntityMappingList =  (ArrayList)map.get(GridCriteriaRuleConstants.ALLOCATION_ENTITY_MAPPING_LIST);
        for (AllocationEntityMapping allocationEntityMapping : allocationEntityMappingList) {
            baseContextObjectNameList.add(allocationEntityMapping.getBaseContextObjectName());
            baseContextObjectNameList.add(allocationEntityMapping.getAlias());
        }
        map.put(GridCriteriaRuleConstants.LEFT_EXPRESSION_MAP,leftExpressionMap);
        map.put(GridCriteriaRuleConstants.BASE_CONTEXT_OBJECT_NAME_LIST,baseContextObjectNameList);
        String expression = criteriaExpressionBuilder.buildCriteriaRuleQuery(ruleGroupExpression, map, joinsMap);
        StringBuilder statement = generateStatement(map, expression, assignmentMatrixRowData.getAllocationStrategy());
        queryString.append(statement);
        return queryString;
    }

    /**
     * 
     * To build Where clause
     * @param joinsMap
     * @return
     */

    private String buildFromClause(Map<String, String> joinsMap) {
        StringBuilder stringBuilder = new StringBuilder();
        for (Map.Entry<String, String> entry : joinsMap.entrySet()) {
            stringBuilder.append(" join " + entry.getValue() + " " + entry.getKey());
        }
        return stringBuilder.toString();

    }


    private StringBuilder generateStatement( Map<Object, Object> map,String expression, String allocationStrategy) throws ClassNotFoundException {
        boolean isBaseMasterEntity = false;
        Class entityName = null;
        List<AllocationEntityMapping> allocationEntityMappingList =  (ArrayList)map.get(GridCriteriaRuleConstants.ALLOCATION_ENTITY_MAPPING_LIST);
        HashMap<String,String> leftExpressionMap = ((HashMap)map.get(GridCriteriaRuleConstants.LEFT_EXPRESSION_MAP));
        Map<String, List<AllocationEntityMapping>> allocationEntityMap ;
        if(AssignmentCriteriaSet.LEAST_LOADED_TEAM.equalsIgnoreCase(allocationStrategy)) {
            allocationEntityMap = allocationEntityMappingList.stream().filter(entity -> GridCriteriaRuleConstants.TEAM.equalsIgnoreCase(entity.getEntityType())).collect(groupingBy(AllocationEntityMapping::getBaseContextObjectName));
        }else{
            allocationEntityMap = allocationEntityMappingList.stream().filter(entity -> GridCriteriaRuleConstants.USER.equalsIgnoreCase(entity.getEntityType())).collect(groupingBy(AllocationEntityMapping::getBaseContextObjectName));
        }
        StringBuilder queryString = new StringBuilder();
        AllocationEntityMapping allocationEntityMapping  = getAllocationEntityMapping(leftExpressionMap, allocationEntityMap);

        if(allocationEntityMapping!=null) {
            queryString.append(GridCriteriaRuleConstants.SELECT_DISTINCT + allocationEntityMapping.getSelectField() + GridCriteriaRuleConstants.FROM + allocationEntityMapping.getSelectFromEntity() + " "
                    + allocationEntityMapping.getAlias() + " ");
            if(queryString.toString().contains(GridCriteriaRuleConstants.TEAM_FQN)){
                queryString.append(GridCriteriaRuleConstants.JOIN_USER_TO_TEAM);
            }

            queryString = appendJoinStatements(allocationStrategy, leftExpressionMap, allocationEntityMap, queryString, allocationEntityMapping);
            entityName = Class.forName(allocationEntityMapping.getSelectFromEntity());
            Class superClass = BaseMasterEntity.class;
            isBaseMasterEntity = superClass.isAssignableFrom(entityName);
        }

        queryString.append(" WHERE (");
        if(StringUtils.isEmpty(expression)){
            expression = GridCriteriaRuleConstants.DEFAULT_EXPRESSION;
        }
        queryString.append(expression);

        if (isBaseMasterEntity) {

            queryString.append(GridCriteriaRuleConstants.AND + allocationEntityMapping.getAlias()
                    + ".masterLifeCycleData.approvalStatus IN (:statusList) )");

            queryString.append(GridCriteriaRuleConstants.AND + allocationEntityMapping.getAlias()
                    + ".entityLifeCycleData.snapshotRecord IS NULL OR " + allocationEntityMapping.getAlias()
                    + ".entityLifeCycleData.snapshotRecord = false)");

        }

        queryString.append(")");

        return queryString;
    }

    private AllocationEntityMapping getAllocationEntityMapping(HashMap<String, String> leftExpressionMap, Map<String, List<AllocationEntityMapping>> allocationEntityMap) {
        AllocationEntityMapping allocationEntityMapping;
        if(leftExpressionMap.containsKey(GridCriteriaRuleConstants.CONTEXT_OBJECT_ALLOCATION_USER_BRANCH_PRODUCT)){
            allocationEntityMapping = allocationEntityMap.get(GridCriteriaRuleConstants.CONTEXT_OBJECT_ALLOCATION_USER_BRANCH_PRODUCT).get(0);
        }else if(leftExpressionMap.containsKey(GridCriteriaRuleConstants.CONTEXT_OBJECT_ALLOCATION_USER_BRANCH)
                || leftExpressionMap.containsKey(GridCriteriaRuleConstants.CONTEXT_OBJECT_ALLOCATION_USER_BRANCH_SERVED_CITY)
                || leftExpressionMap.containsKey(GridCriteriaRuleConstants.CONTEXT_OBJECT_ALLOCATION_USER_BRANCH_SERVED_VILLAGES)){
            allocationEntityMapping = allocationEntityMap.get(GridCriteriaRuleConstants.CONTEXT_OBJECT_ALLOCATION_USER_BRANCH).get(0);
        }else if(leftExpressionMap.containsKey(GridCriteriaRuleConstants.CONTEXT_OBJECT_ALLOCATION_TEAM)){
            allocationEntityMapping = allocationEntityMap.get(GridCriteriaRuleConstants.CONTEXT_OBJECT_ALLOCATION_TEAM).get(0);
        }else if(leftExpressionMap.containsKey(GridCriteriaRuleConstants.CONTEXT_OBJECT_ALLOCATION_USER)
                || leftExpressionMap.containsKey(GridCriteriaRuleConstants.CONTEXT_OBJECT_ALLOCATION_USER_ROLES)){
            allocationEntityMapping = allocationEntityMap.get(GridCriteriaRuleConstants.CONTEXT_OBJECT_ALLOCATION_USER).get(0);
        }else{
            allocationEntityMapping = allocationEntityMap.get(GridCriteriaRuleConstants.CONTEXT_OBJECT_ALLOCATION_USER).get(0);
        }
        return allocationEntityMapping;
    }

    private StringBuilder appendJoinStatements(String allocationStrategy, HashMap<String, String> leftExpressionMap, Map<String, List<AllocationEntityMapping>> allocationEntityMap, StringBuilder queryString, AllocationEntityMapping allocationEntityMapping) {
        for (Map.Entry<String, String> expressionMapElement : leftExpressionMap.entrySet()) {
            if(allocationEntityMap.containsKey(expressionMapElement.getKey())){
                AllocationEntityMapping keyMapping = allocationEntityMap.get(expressionMapElement.getKey()).get(0);

                if(Objects.nonNull(keyMapping.getJoinStatement()) && StringUtils.isNotBlank(keyMapping.getJoinStatement())
                        && (keyMapping.getBaseContextObjectName()!=allocationEntityMapping.getBaseContextObjectName()
                        || AssignmentCriteriaSet.LEAST_LOADED_TEAM.equalsIgnoreCase(allocationStrategy))) {
                    String statement = keyMapping.getJoinStatement();
                    if(StringUtils.containsIgnoreCase(statement, GridCriteriaRuleConstants.OGNL_FIELD)) {
                        statement =  statement.replaceAll("(?i)"+Pattern.quote(GridCriteriaRuleConstants.OGNL_FIELD), allocationEntityMapping.getOgnlField());
                    }else if(StringUtils.containsIgnoreCase(statement, GridCriteriaRuleConstants.ALIAS_FIELD)) {
                        statement =  statement.replaceAll("(?i)"+Pattern.quote(GridCriteriaRuleConstants.ALIAS_FIELD), allocationEntityMapping.getAlias());
                    }
                    queryString.append(GridCriteriaRuleConstants.INNER_JOIN + statement);
                }
            }
        }
        return queryString;
    }


}
