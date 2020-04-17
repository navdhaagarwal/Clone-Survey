package com.nucleus.rules.assignmentmatrix.service;

import java.io.IOException;
import java.io.Serializable;
import java.util.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Arrays;

import javax.inject.Inject;
import javax.inject.Named;

import com.nucleus.core.transaction.TransactionPostCommitWorker;
import com.nucleus.dao.query.NamedQueryExecutor;
import com.nucleus.entity.Entity;
import com.nucleus.entity.EntityId;
import com.nucleus.entity.EntityLifeCycleData;
import com.nucleus.rules.assignmentmatrix.assignmentExecutionResult.AssignmentMatrixExecutionResult;
import com.nucleus.rules.model.*;

import com.nucleus.rules.model.assignmentMatrix.*;
import com.nucleus.rules.service.*;
import com.nucleus.rules.simulation.service.AssignmentMatrixRowDataElementsPojo;
import com.nucleus.rules.simulation.service.AssignmentMatrixRowDataPojo;
import com.nucleus.rules.simulation.service.AssignmentSetPojo;
import com.nucleus.rules.simulation.service.SimulationAssignmentMatrixVO;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections4.MapIterator;
import org.apache.commons.collections4.iterators.EntrySetMapIterator;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.codehaus.jettison.json.JSONObject;
import org.joda.time.DateTime;
import org.mvel2.MVEL;
import org.springframework.beans.factory.annotation.Autowired;

import net.bull.javamelody.MonitoredWithSpring;

import java.math.BigDecimal;

import com.nucleus.assignmentmaster.addondataprovider.AddOnDataProviderForAssignmentTask;
import com.nucleus.core.NeutrinoSpringAppContextUtil;
import com.nucleus.core.validation.util.NeutrinoValidator;
import com.nucleus.logging.BaseLoggers;
import com.nucleus.notificationMaster.service.AddOnDataProviderForNotificationGeneration;
import com.nucleus.persistence.EntityDao;
import com.nucleus.rules.model.ruleMatrixMaster.RuleMatrixMaster;

import flexjson.JSONSerializer;
import org.springframework.context.annotation.Lazy;
import org.springframework.transaction.annotation.Transactional;

@Named(value = "assignmentMatrixExecutionService")
@MonitoredWithSpring(name = "AssignmentMatrixExecution_Service_IMPL_")
public class AssignmentMatrixExecutionServiceImpl implements AssignmentMatrixExecutionService {

    @Inject
    @Named("compiledExpressionBuilder")
    CompiledExpressionBuilder compiledExpressionBuilder;

    @Inject
    @Named("ruleService")
    private RuleService ruleService;

    @Inject
    @Named("expressionEvaluator")
    protected ExpressionEvaluatorImpl expressionEvaluator;

    @Inject
    @Named("entityDao")
    protected EntityDao entityDao;

    @Inject
    @Named("ruleExceptionLoggingPostCommitWorker")
    private RuleExceptionLoggingPostCommitWorker ruleErrorLoggingPostCommitWorker;

    @Lazy
    @Inject
    @Named("ruleExceptionLoggingServiceImpl")
    private RuleExceptionLoggingService ruleExceptionLoggingService;

    @Autowired
    private AddOnDataProviderForAssignmentTask addOnDataProviderForNotification;

    private final static String ASSIGN_STRING_1 = " Assignment Save Data String 3";
    private final static String ASSIGN_STRING_2 = "Assignment Save Data String 4" ;
    private final static String ASSIGN_STRING_3 = "Assignment Save Data String 5" ;
    private final static String ASSIGN_STRING_4 = "Assignment Save Data String 6" ;
    private final static String ASSIGN_STRING_5 = "Assignment Save Data String 2" ;
    private final static String ASSIGN_STRING_6 = "Assignment Save Data String 1" ;
    private final static String ASSIGN_STRING_7 = "Assignment Save Data String 7" ;
    private final static String ASSIGN_STRING_8 = "Assignment Save Data String 8" ;
    private final static String ASSIGN_STRING_9 = "Assignment Save Data String 9" ;
    private final static String ASSIGN_STRING_10 = "Assignment Save Data String 10" ;
    private final static String ASSIGN_STRING_11 = "Assignment Save Data String 11" ;
    private final static String ASSIGN_STRING_12 = "Assignment Save Data String 12" ;
    private final static String ASSIGN_STRING_13 = "Assignment Save Data String 13" ;
    private final static String ASSIGN_STRING_14 = "Assignment Save Data String 14" ;
    private final static String ASSIGN_STRING_15 = "Assignment Save Data String 15" ;
    private final static String ASSIGN_NUMBER_1 = "Assignment Save Data Number 1" ;
    private final static String ASSIGN_NUMBER_2 = "Assignment Save Data Number 2" ;
    private final static String ASSIGN_NUMBER_3 = "Assignment Save Data Number 3" ;
    private final static String ASSIGN_NUMBER_4 = "Assignment Save Data Number 4" ;
    private final static String ASSIGN_NUMBER_5 = "Assignment Save Data Number 5" ;
    private final static String ASSIGN_NUMBER_6 = "Assignment Save Data Number 6" ;
    private final static String ASSIGN_NUMBER_7 = "Assignment Save Data Number 7" ;
    private final static String ASSIGN_NUMBER_8 = "Assignment Save Data Number 8" ;
    private final static String ASSIGN_NUMBER_9 = "Assignment Save Data Number 9" ;
    private final static String ASSIGN_NUMBER_10 = "Assignment Save Data Number 10" ;
    private final static String ASSIGN_INTEGER_1 = "Assignment Save Data Integer 1" ;
    private final static String ASSIGN_INTEGER_2 = "Assignment Save Data Integer 2" ;
    private final static String ASSIGN_INTEGER_3 = "Assignment Save Data Integer 3" ;
    private final static String ASSIGN_INTEGER_4 = "Assignment Save Data Integer 4" ;
    private final static String ASSIGN_INTEGER_5 = "Assignment Save Data Integer 5" ;
    private final static String ASSIGN_INTEGER_6 = "Assignment Save Data Integer 6" ;
    private final static String ASSIGN_INTEGER_7 = "Assignment Save Data Integer 7" ;
    private final static String ASSIGN_INTEGER_8 = "Assignment Save Data Integer 8" ;
    private final static String ASSIGN_INTEGER_9 = "Assignment Save Data Integer 9" ;
    private final static String ASSIGN_INTEGER_10 = "Assignment Save Data Integer 10" ;
    private final static String ASSIGN_BOOLEAN_1 = "Assignment Save Data Boolean 1" ;
    private final static String ASSIGN_BOOLEAN_2 = "Assignment Save Data Boolean 2" ;
    private final static String ASSIGN_BOOLEAN_3 = "Assignment Save Data Boolean 3" ;

    @Override
    public void executeAssignMatrix(AssignmentMaster assignmentMaster, Map map) {
        try {
            if (null != assignmentMaster) {
                if (addOnDataProviderForNotification != null) {
                    assignmentMaster = addOnDataProviderForNotification.reInitializeAssignmentMaster(AssignmentMaster.class, assignmentMaster.getId());
                }
                if(!map.containsKey(AssignmentConstants.ASSIGNMENT_RESULT_CONTEXT)){
                    map.put(AssignmentConstants.ASSIGNMENT_RESULT_CONTEXT,new AssignmentMatrixExecutionResult());
                }
                List<AssignmentSet> assignmentSetsList = assignmentMaster.getAssignmentSet();
                NeutrinoValidator.notEmpty(assignmentSetsList, "No Assignment Set is present in Assignment Master :"
                        + assignmentMaster.getCode());

                /*
                 * Sort On the Basis Of priority
                 */

                List<AssignmentSet> tempAssignmentSet = new ArrayList<AssignmentSet>();
                if (map.get(AssignmentConstants.APPLICATION_STAMPING_DATE) != null) {
                    for (AssignmentSet assignmentSet : assignmentSetsList) {


                        boolean isValid = isAssignmentSetValidWithDateConstraints(assignmentSet, (Date) map.get(AssignmentConstants.APPLICATION_STAMPING_DATE));
                        if (isValid)
                            tempAssignmentSet.add(assignmentSet);

                    }
                }
                if (CollectionUtils.isEmpty(tempAssignmentSet)) {
                    //find assignment matrix with current date between from date and end date
                    tempAssignmentSet = assignmentSetsWithValidCurrentDateConstraints(assignmentSetsList);
                }

                sortAssignmentSetByPriority(tempAssignmentSet);

                /*
                 * Execute assignmentSets by rules
                 */
                map.put(AssignmentConstants.IS_ASSIGNMENT_MATRIX, true);
                executeAssignmentSetsRules(tempAssignmentSet, map, assignmentMaster.getExecuteAll());
                if(assignmentMaster.getSaveResult()!=null && assignmentMaster.getSaveResult()) {
                    AssignmentMatrixExecutionResult assignmentMatrixExecutionResult = saveAssignmentMatrixExecutionData(map,assignmentMaster);
                    updateContextMapForAssignmentMatrixData(map,assignmentMatrixExecutionResult);
                }
            }
        } catch (Exception e) {
            BaseLoggers.exceptionLogger.error("Error in executing assignment matrix :" + assignmentMaster.getName(), e);
            throw e;
        }
    }

    @Transactional
    private AssignmentMatrixExecutionResult saveAssignmentMatrixExecutionData(Map map,AssignmentMaster assignmentMaster) {
        AssignmentMatrixExecutionResult assignmentMatrixExecutionResult =(AssignmentMatrixExecutionResult) map.get(AssignmentConstants.ASSIGNMENT_RESULT_CONTEXT);
        if(assignmentMatrixExecutionResult!=null) {
            assignmentMatrixExecutionResult.setAssignmentMatrixName(assignmentMaster.getName());
            EntityLifeCycleData entityLifeCycleData= assignmentMatrixExecutionResult.getEntityLifeCycleData();
            entityLifeCycleData.setCreationTimeStamp(DateTime.now());
            assignmentMatrixExecutionResult.setEntityLifeCycleData(entityLifeCycleData);
            String uri = (String) map.get("contextObjectLoanApplicationUri");
            if (uri != null && !uri.isEmpty()) {
                EntityId entityId = EntityId.fromUri(uri);
                if (entityId != null && entityId.getLocalId() != null) {
                    Long id = entityId.getLocalId();
                    if (id != null) {
                        assignmentMatrixExecutionResult.setApplicationId(id);
                    }
                    entityDao.saveOrUpdate(assignmentMatrixExecutionResult);
                }
            }
            return assignmentMatrixExecutionResult;
        }
        return null;
    }

    @Transactional
    private void updateContextMapForAssignmentMatrixData(Map contextMap, AssignmentMatrixExecutionResult assignmentMatrixExecutionResult) {
        if (assignmentMatrixExecutionResult != null) {
            Map<String, Map<String, Object>> assignmentResultMap = new HashMap<>();
            if (assignmentMatrixExecutionResult.getAssignmentMatrixName() != null && !assignmentMatrixExecutionResult.getAssignmentMatrixName().isEmpty()) {
                Map<String, Object> nameValueMap = new HashMap<>();
                setNameValueMap(nameValueMap, assignmentMatrixExecutionResult);
                assignmentResultMap.put(assignmentMatrixExecutionResult.getAssignmentMatrixName(), nameValueMap);
            }
            contextMap.put(AssignmentConstants.ASSIGNMENT_RESULT_MAP_CONTEXT, assignmentResultMap);
        }
    }




    private static void setNameValueMap(Map<String,Object> map,AssignmentMatrixExecutionResult assignmentMatrixExecutionResult){
        if(StringUtils.isNotEmpty(assignmentMatrixExecutionResult.getContextObjectAssignmentMatrixResultBoolean1())){
            map.put(ASSIGN_BOOLEAN_1,assignmentMatrixExecutionResult.getContextObjectAssignmentMatrixResultBoolean1());
        }
        if(StringUtils.isNotEmpty(assignmentMatrixExecutionResult.getContextObjectAssignmentMatrixResultBoolean2())){
            map.put(ASSIGN_BOOLEAN_2,assignmentMatrixExecutionResult.getContextObjectAssignmentMatrixResultBoolean2());

        }
        if(StringUtils.isNotEmpty(assignmentMatrixExecutionResult.getContextObjectAssignmentMatrixResultBoolean3())){
            map.put(ASSIGN_BOOLEAN_3,assignmentMatrixExecutionResult.getContextObjectAssignmentMatrixResultBoolean3());

        }
        if(StringUtils.isNotEmpty(assignmentMatrixExecutionResult.getContextObjectAssignmentMatrixResultInteger1())){
            map.put(ASSIGN_INTEGER_1,assignmentMatrixExecutionResult.getContextObjectAssignmentMatrixResultInteger1());

        }
        if(StringUtils.isNotEmpty(assignmentMatrixExecutionResult.getContextObjectAssignmentMatrixResultInteger2())){
            map.put(ASSIGN_INTEGER_2,assignmentMatrixExecutionResult.getContextObjectAssignmentMatrixResultInteger2());

        }
        if(StringUtils.isNotEmpty(assignmentMatrixExecutionResult.getContextObjectAssignmentMatrixResultInteger3())){
            map.put(ASSIGN_INTEGER_3,assignmentMatrixExecutionResult.getContextObjectAssignmentMatrixResultInteger3());

        }
        if(StringUtils.isNotEmpty(assignmentMatrixExecutionResult.getContextObjectAssignmentMatrixResultInteger4())){
            map.put(ASSIGN_INTEGER_4,assignmentMatrixExecutionResult.getContextObjectAssignmentMatrixResultInteger4());

        }
        if(StringUtils.isNotEmpty(assignmentMatrixExecutionResult.getContextObjectAssignmentMatrixResultInteger5())){
            map.put(ASSIGN_INTEGER_5,assignmentMatrixExecutionResult.getContextObjectAssignmentMatrixResultInteger5());

        }
        if(StringUtils.isNotEmpty(assignmentMatrixExecutionResult.getContextObjectAssignmentMatrixResultInteger6())){
            map.put(ASSIGN_INTEGER_6,assignmentMatrixExecutionResult.getContextObjectAssignmentMatrixResultInteger6());

        }
        if(StringUtils.isNotEmpty(assignmentMatrixExecutionResult.getContextObjectAssignmentMatrixResultInteger7())){
            map.put(ASSIGN_INTEGER_7,assignmentMatrixExecutionResult.getContextObjectAssignmentMatrixResultInteger7());

        }
        if(StringUtils.isNotEmpty(assignmentMatrixExecutionResult.getContextObjectAssignmentMatrixResultInteger8())){
            map.put(ASSIGN_INTEGER_8,assignmentMatrixExecutionResult.getContextObjectAssignmentMatrixResultInteger8());

        }
        if(StringUtils.isNotEmpty(assignmentMatrixExecutionResult.getContextObjectAssignmentMatrixResultInteger9())){
            map.put(ASSIGN_INTEGER_9,assignmentMatrixExecutionResult.getContextObjectAssignmentMatrixResultInteger9());

        }
        if(StringUtils.isNotEmpty(assignmentMatrixExecutionResult.getContextObjectAssignmentMatrixResultInteger10())){
            map.put(ASSIGN_INTEGER_10,assignmentMatrixExecutionResult.getContextObjectAssignmentMatrixResultInteger10());

        }
        if(StringUtils.isNotEmpty(assignmentMatrixExecutionResult.getContextObjectAssignmentMatrixResultNumber1())){
            map.put(ASSIGN_NUMBER_1,assignmentMatrixExecutionResult.getContextObjectAssignmentMatrixResultNumber1());

        }
        if(StringUtils.isNotEmpty(assignmentMatrixExecutionResult.getContextObjectAssignmentMatrixResultNumber2())){
            map.put(ASSIGN_NUMBER_2,assignmentMatrixExecutionResult.getContextObjectAssignmentMatrixResultNumber2());

        }
        if(StringUtils.isNotEmpty(assignmentMatrixExecutionResult.getContextObjectAssignmentMatrixResultNumber3())){
            map.put(ASSIGN_NUMBER_3,assignmentMatrixExecutionResult.getContextObjectAssignmentMatrixResultNumber3());

        }
        if(StringUtils.isNotEmpty(assignmentMatrixExecutionResult.getContextObjectAssignmentMatrixResultNumber4())){
            map.put(ASSIGN_NUMBER_4,assignmentMatrixExecutionResult.getContextObjectAssignmentMatrixResultNumber4());

        }
        if(StringUtils.isNotEmpty(assignmentMatrixExecutionResult.getContextObjectAssignmentMatrixResultNumber5())){
            map.put(ASSIGN_NUMBER_5,assignmentMatrixExecutionResult.getContextObjectAssignmentMatrixResultNumber5());

        }
        if(StringUtils.isNotEmpty(assignmentMatrixExecutionResult.getContextObjectAssignmentMatrixResultNumber6())){
            map.put(ASSIGN_NUMBER_6,assignmentMatrixExecutionResult.getContextObjectAssignmentMatrixResultNumber6());

        }
        if(StringUtils.isNotEmpty(assignmentMatrixExecutionResult.getContextObjectAssignmentMatrixResultNumber7())){
            map.put(ASSIGN_NUMBER_7,assignmentMatrixExecutionResult.getContextObjectAssignmentMatrixResultNumber7());

        }
        if(StringUtils.isNotEmpty(assignmentMatrixExecutionResult.getContextObjectAssignmentMatrixResultNumber8())){
            map.put(ASSIGN_NUMBER_8,assignmentMatrixExecutionResult.getContextObjectAssignmentMatrixResultNumber8());

        }
        if(StringUtils.isNotEmpty(assignmentMatrixExecutionResult.getContextObjectAssignmentMatrixResultNumber9())){
            map.put(ASSIGN_NUMBER_9,assignmentMatrixExecutionResult.getContextObjectAssignmentMatrixResultNumber9());

        }
        if(StringUtils.isNotEmpty(assignmentMatrixExecutionResult.getContextObjectAssignmentMatrixResultNumber10())){
            map.put(ASSIGN_NUMBER_10,assignmentMatrixExecutionResult.getContextObjectAssignmentMatrixResultNumber10());

        }
        if(StringUtils.isNotEmpty(assignmentMatrixExecutionResult.getContextObjectAssignmentMatrixResultString1())){
            map.put(ASSIGN_STRING_1,assignmentMatrixExecutionResult.getContextObjectAssignmentMatrixResultString1());

        }
        if(StringUtils.isNotEmpty(assignmentMatrixExecutionResult.getContextObjectAssignmentMatrixResultString2())){
            map.put(ASSIGN_STRING_2,assignmentMatrixExecutionResult.getContextObjectAssignmentMatrixResultString2());

        }
        if(StringUtils.isNotEmpty(assignmentMatrixExecutionResult.getContextObjectAssignmentMatrixResultString3())){
            map.put(ASSIGN_STRING_3,assignmentMatrixExecutionResult.getContextObjectAssignmentMatrixResultString3());

        }
        if(StringUtils.isNotEmpty(assignmentMatrixExecutionResult.getContextObjectAssignmentMatrixResultString4())){
            map.put(ASSIGN_STRING_4,assignmentMatrixExecutionResult.getContextObjectAssignmentMatrixResultString4());

        }
        if(StringUtils.isNotEmpty(assignmentMatrixExecutionResult.getContextObjectAssignmentMatrixResultString5())){
            map.put(ASSIGN_STRING_5,assignmentMatrixExecutionResult.getContextObjectAssignmentMatrixResultString5());

        }
        if(StringUtils.isNotEmpty(assignmentMatrixExecutionResult.getContextObjectAssignmentMatrixResultString6())){
            map.put(ASSIGN_STRING_6,assignmentMatrixExecutionResult.getContextObjectAssignmentMatrixResultString6());

        }
        if(StringUtils.isNotEmpty(assignmentMatrixExecutionResult.getContextObjectAssignmentMatrixResultString7())){
            map.put(ASSIGN_STRING_7,assignmentMatrixExecutionResult.getContextObjectAssignmentMatrixResultString7());

        }
        if(StringUtils.isNotEmpty(assignmentMatrixExecutionResult.getContextObjectAssignmentMatrixResultString8())){
            map.put(ASSIGN_STRING_8,assignmentMatrixExecutionResult.getContextObjectAssignmentMatrixResultString8());

        }
        if(StringUtils.isNotEmpty(assignmentMatrixExecutionResult.getContextObjectAssignmentMatrixResultString9())){
            map.put(ASSIGN_STRING_9,assignmentMatrixExecutionResult.getContextObjectAssignmentMatrixResultString9());

        }
        if(StringUtils.isNotEmpty(assignmentMatrixExecutionResult.getContextObjectAssignmentMatrixResultString10())){
            map.put(ASSIGN_STRING_10,assignmentMatrixExecutionResult.getContextObjectAssignmentMatrixResultString10());

        }
        if(StringUtils.isNotEmpty(assignmentMatrixExecutionResult.getContextObjectAssignmentMatrixResultString11())){
            map.put(ASSIGN_STRING_11,assignmentMatrixExecutionResult.getContextObjectAssignmentMatrixResultString11());

        }
        if(StringUtils.isNotEmpty(assignmentMatrixExecutionResult.getContextObjectAssignmentMatrixResultString12())){
            map.put(ASSIGN_STRING_12,assignmentMatrixExecutionResult.getContextObjectAssignmentMatrixResultString12());

        }
        if(StringUtils.isNotEmpty(assignmentMatrixExecutionResult.getContextObjectAssignmentMatrixResultString13())){
            map.put(ASSIGN_STRING_13,assignmentMatrixExecutionResult.getContextObjectAssignmentMatrixResultString13());

        }
        if(StringUtils.isNotEmpty(assignmentMatrixExecutionResult.getContextObjectAssignmentMatrixResultString14())){
            map.put(ASSIGN_STRING_14,assignmentMatrixExecutionResult.getContextObjectAssignmentMatrixResultString14());

        }
        if(StringUtils.isNotEmpty(assignmentMatrixExecutionResult.getContextObjectAssignmentMatrixResultString15())){
            map.put(ASSIGN_STRING_15,assignmentMatrixExecutionResult.getContextObjectAssignmentMatrixResultString15());

        }
    }

    private List<AssignmentMatrixExecutionResult> getAssignmentExecutionResultBasedOnAppIdAndMatrixName(Long appId,String matrixName) {
        NamedQueryExecutor<AssignmentMatrixExecutionResult> namedQueryExecutor =  new NamedQueryExecutor<>("AssignmentResult.getAssignmentResultBasedOnAppIdAndMatrixName");
        namedQueryExecutor.addParameter("applicationId",appId);
        namedQueryExecutor.addParameter("matrixName",matrixName);
        return entityDao.executeQuery(namedQueryExecutor);
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

                if (o1.getPriority() == null && o2.getPriority() == null) {
                    return 0;
                }

                else if (o1.getPriority() == null) {
                    return 1;
                }

                else if (o2.getPriority() == null) {
                    return -1;
                }

                else if (o1.getPriority() == o2.getPriority()) {
                    return 0;
                }

                return o1.getPriority() < o2.getPriority() ? -1 : 1;
            }
        });
    }

    /**
     *
     * Sort the assignment matrix row data on basis of priority
     * @param assignmentMatrixRowDatas
     */

    private void sortAssignmentMatrixRowDataByPriority(List<AssignmentMatrixRowData> assignmentMatrixRowDatas) {
        Collections.sort(assignmentMatrixRowDatas, new Comparator<AssignmentMatrixRowData>() {
            @Override
            public int compare(AssignmentMatrixRowData o1, AssignmentMatrixRowData o2) {

                if (o1.getPriority() == null && o2.getPriority() == null) {
                    return 0;
                }

                else if (o1.getPriority() == null) {
                    return 1;
                }

                else if (o2.getPriority() == null) {
                    return -1;
                }

                else if (o1.getPriority() == o2.getPriority()) {
                    return 0;
                }

                return o1.getPriority() < o2.getPriority() ? -1 : 1;
            }
        });
    }

    /**
     *
     * Execute Assignmnet Row rules and assignment actions
     * @param assignmentSetsList
     * @param map
     */

    private void executeAssignmentSetsRules(List<AssignmentSet> assignmentSetsList, Map map, Boolean executeAll) {

        Rule rule = null;
        Boolean ruleResult = null;
        char res;
        char assignmentSetRes;
        Boolean assignmentSetRuleResult = null;
        Boolean assignmentSetExecuteAll = null;
        Boolean defaultSet = null;
        AssignmentMatrixAction assignmentMatrixAction = null;
        JSONSerializer serializer = new JSONSerializer();
        Set<AssignmentSetExecutionVO> assignmentSetExecutionVOList = new HashSet<>();
        int index_value = 0;
        boolean multipleResults =map.get(AssignmentConstants.MULTIPLE_RESULTS)!=null ? (Boolean) map.get(AssignmentConstants.MULTIPLE_RESULTS) : false;
        if(multipleResults) {
            map.put(AssignmentConstants.INDEX_REPLACEMENT, index_value);
            map.put(AssignmentConstants.MULTIPLE_RESULT_CONTEXT,new ArrayList<>());
        }


        for (AssignmentSet assignmentSet : assignmentSetsList) {
            Map assignmentSetExecutionResultMap = new HashMap();
            map.put(AssignmentConstants.ASSIGNMENT_SET_EXECUTION_RESULT_MAP, assignmentSetExecutionResultMap);
            BaseLoggers.flowLogger.debug("Execution for AssignmentSet :: = " + assignmentSet.getAssignmentSetName()
                    + " :: begins");

            // Execute AssignmentSet
            assignmentSetExecuteAll = assignmentSet.getExecuteAll();

            // default assignment set
            defaultSet = assignmentSet.getDefaultSet();

            if (assignmentSet.getAssignmentSetRule() != null) {

                BaseLoggers.flowLogger.debug("Execution for Rule on AssignmentSet :: =  "
                        + assignmentSet.getAssignmentSetName() + " :: begins");

                rule = assignmentSet.getAssignmentSetRule();

                assignmentSetRes = compiledExpressionBuilder.evaluateRule(rule.getId(), map);
                assignmentSetRuleResult = (assignmentSetRes == RuleConstants.RULE_RESULT_PASS) ? true : false;

                BaseLoggers.flowLogger.debug("Execution Result for Rule :: " + rule.getId() + "+ On AssignmentSet :: "
                        + assignmentSet.getAssignmentSetName() + " :: Rule Result is = " + assignmentSetRuleResult);

            } else {

                assignmentSetRuleResult = true;
            }

            if (assignmentSetRuleResult) {
                List<AssignmentMatrixRowData> assignmentMatrixRowDataList = assignmentSet.getAssignmentMatrixRowData();
                AssignmentSetExecutionVO assignmentSetExecutionVO = new AssignmentSetExecutionVO();


                assignmentSetExecutionVO.setAssignmentSetName(assignmentSet.getAssignmentSetName());
                assignmentSetExecutionVO.setPriority(String.valueOf(assignmentSet.getPriority()));
                assignmentSetExecutionVO.setBufferDays(assignmentSet.getBufferDays());
                assignmentSetExecutionVO.setEffectiveFrom(assignmentSet.getEffectiveFrom());
                assignmentSetExecutionVO.setEffectiveTill(assignmentSet.getEffectiveTill());


                if (CollectionUtils.isNotEmpty(assignmentMatrixRowDataList)) {

                    List<AssignmentMatrixRowData> tempAssignmentMatrixRowDataList = new ArrayList<AssignmentMatrixRowData>();
                    for (AssignmentMatrixRowData assignmentMatrixRowData : assignmentMatrixRowDataList) {
                        tempAssignmentMatrixRowDataList.add(assignmentMatrixRowData);
                    }

                    sortAssignmentMatrixRowDataByPriority(tempAssignmentMatrixRowDataList);
                    for (AssignmentMatrixRowData assignmentMatrixRowData : tempAssignmentMatrixRowDataList) {

                        rule = assignmentMatrixRowData.getRule();
                        try {

                            BaseLoggers.flowLogger.debug("Execution for AssignmentMatrixRowData with id begins :: = "
                                    + assignmentMatrixRowData.getId());

                            res = compiledExpressionBuilder.evaluateRule(rule.getId(), map);
                            ruleResult = (res == RuleConstants.RULE_RESULT_PASS) ? true : false;
	                        if(null != assignmentMatrixRowData.getRuleExp()){
                                String ruleExp=assignmentMatrixRowData.getRuleExp();
                                String[] ruleArray = ruleExp.split(",");
                                for(int i=0;i<ruleArray.length;i++){
                                    String[]  ruleData = ruleArray[i].split(":");
                                    Long ruleId=Long.valueOf(ruleData[0].split("_")[0]);
                                    char ruleRes=compiledExpressionBuilder.evaluateRule(ruleId, map);
                                    Boolean ruleR= (ruleRes == RuleConstants.RULE_RESULT_PASS) ? true : false;
                                    map.put(assignmentMatrixRowData.getId() + "_" + ruleId+"_actual",  ruleR);
                                    if((!("*".equalsIgnoreCase(ruleData[1]))) && (!ruleR && !("F".equalsIgnoreCase(ruleData[1])))||(ruleR && ("F".equalsIgnoreCase(ruleData[1])))){
                                        ruleResult=Boolean.FALSE;
                                        map.put(assignmentMatrixRowData.getId() + "_" + ruleId, Boolean.FALSE);
                                    }else {
                                        map.put(assignmentMatrixRowData.getId() + "_" + ruleId,  Boolean.TRUE);
                                    }
                                }
                            }
                            BaseLoggers.flowLogger.debug("Rule Execution result for AssignmentMatrixRowData with id :: = "
                                    + assignmentMatrixRowData.getId() + " and rule id is :: = " + rule.getId()
                                    + " and rule result is = " + ruleResult);

                            map.put(rule.getId(), ruleResult);
                            if (ruleResult) {
                                // Evaluate Assignment Action
                                assignmentMatrixAction = assignmentMatrixRowData.getAssignmentMatrixAction();
                                if (assignmentMatrixAction.getAssignActionValues().contains(AssignmentConstants.MULTI_VALUE_SEPARATOR))
                                    createParameterListForAggregateFunctions(assignmentSet, assignmentMatrixAction, map);


                                BaseLoggers.flowLogger
                                        .debug("Rule Result is true , so executing assignment action with id :: = "
                                                + assignmentMatrixAction.getId());

                                compiledExpressionBuilder.executeAssignmentAction(assignmentMatrixAction, map);
                                if(multipleResults) {
                                    map.put(AssignmentConstants.INDEX_REPLACEMENT,++index_value);
                                }

                                Map thenActionMap = new HashMap();
                                Boolean isAssignmentMatrix = (Boolean) map.get(AssignmentConstants.IS_ASSIGNMENT_MATRIX);

                                if (null != isAssignmentMatrix && isAssignmentMatrix)
                                    thenActionMap = getAssignmentValueMap(assignmentMatrixRowData.getAssignmentMatrixAction(), map);
                                else if (null != isAssignmentMatrix && !isAssignmentMatrix)
                                    thenActionMap = getRuleMatrixAssignmentValueMap(assignmentMatrixRowData.getAssignmentMatrixAction(), map);

                                assignmentSetExecutionResultMap.put(AssignmentConstants.ASSIGNMENT_OBJECT_EXPRESSION_RESULT, thenActionMap);
                                assignmentSetExecutionVO.setResult((Map) map.get(AssignmentConstants.ASSIGNMENT_SET_EXECUTION_RESULT_MAP));
                                assignmentSetExecutionVOList.add(assignmentSetExecutionVO);


                                if ((null == assignmentSetExecuteAll || !assignmentSetExecuteAll)) {
                                    BaseLoggers.flowLogger.debug("Rule Result is true and assignmentSetExecuteAll is = "
                                            + assignmentSetExecuteAll + " so breaking from ARMD");
                                    break;
                                }
                            }
                        } catch (Exception e) {
                            BaseLoggers.exceptionLogger.error("Error in Row evalution :: Expression " + assignmentMatrixRowData.getExpression(), e);
                            RuleExceptionLoggingVO ruleExceptionLoggingVO = new RuleExceptionLoggingVO();
                            ruleExceptionLoggingVO.setContextMap(map);
                            ruleExceptionLoggingVO.setE(e);
                            ruleExceptionLoggingVO.setRule(rule);
                            ruleExceptionLoggingVO.setExceptionOwner(RuleConstants.RULE_ACTION_EXCEPTION);
                            ruleExceptionLoggingService.saveRuleErrorLogs(ruleExceptionLoggingVO);
                            throw e;
                        }

                    }


                    if (ruleResult && (null == executeAll || !executeAll) && (null == defaultSet || !defaultSet)) {
                        BaseLoggers.flowLogger.debug("Rule Result is true and executeAll is = " + executeAll
                                + " so returning from AM");

                        map.put(AssignmentConstants.ASSIGNMENT_SET_EXECUTION_VO_LIST, new ArrayList<>(assignmentSetExecutionVOList));

                        return;
                    }
                }



            }
        }
        if(multipleResults){
            List<String> promoCodes = (List<String>)map.get(AssignmentConstants.MULTIPLE_RESULT_CONTEXT);
            promoCodes.removeIf(s -> s.equalsIgnoreCase(AssignmentConstants.JUNK_VALUE));
        }
        map.put(AssignmentConstants.ASSIGNMENT_SET_EXECUTION_VO_LIST, new ArrayList<>(assignmentSetExecutionVOList));
    }

    @Override
    public void executeRuleMatrix(RuleMatrixMaster ruleMatrixMaster, Map map) {

        if (null != ruleMatrixMaster) {
            if (addOnDataProviderForNotification != null) {
                ruleMatrixMaster = addOnDataProviderForNotification.reInitializeAssignmentMaster(RuleMatrixMaster.class, ruleMatrixMaster.getId());
            }
            boolean multipleResults = false;
            if(ruleMatrixMaster.getMultipleResults()!=null && ruleMatrixMaster.getMultipleResults())
                multipleResults = true;
            List<AssignmentSet> assignmentSetsList = ruleMatrixMaster.getAssignmentSet();
            NeutrinoValidator.notEmpty(assignmentSetsList, "No Rule Matrix Set is present in Rule Matrix Master :"
                    + ruleMatrixMaster.getCode());

            /*
             * Sort On the Basis Of priority
             */
            List<AssignmentSet> tempAssignmentSet = new ArrayList<AssignmentSet>();
            if (map.get(AssignmentConstants.APPLICATION_STAMPING_DATE) != null) {
                for (AssignmentSet assignmentSet : assignmentSetsList) {
                    boolean isValid = isAssignmentSetValidWithDateConstraints(assignmentSet, (Date) map.get(AssignmentConstants.APPLICATION_STAMPING_DATE));
                    if (isValid)
                        tempAssignmentSet.add(assignmentSet);
                }
            }
            if (CollectionUtils.isEmpty(tempAssignmentSet)) {
                //find assignment matrix with current date between from date and end date
                tempAssignmentSet = assignmentSetsWithValidCurrentDateConstraints(assignmentSetsList);
            }

            sortAssignmentSetByPriority(tempAssignmentSet);

            /*
             * Execute rule matrix sets by rules
             */
            map.put(AssignmentConstants.IS_ASSIGNMENT_MATRIX, false);
            map.put(AssignmentConstants.MULTIPLE_RESULTS,multipleResults);
            executeAssignmentSetsRules(tempAssignmentSet, map, ruleMatrixMaster.getExecuteAll());

        }
    }

    private void prepareParameterList(String[] multiValues,
                                      Map<String, Object> parmeterValueAndId,
                                      List<BigDecimal> paramValueList, Map map)

    {
        BigDecimal tempVariable;
        for (int i = 0; i < multiValues.length; i++) {
            Object paramValue = expressionEvaluator.executeParameter(
                    ruleService.getParameter(Long.valueOf(multiValues[i])),
                    map, false);
            if (null != paramValue) {
                if (paramValue instanceof Double) {
                    tempVariable = new BigDecimal((Double) paramValue);
                    paramValueList.add(tempVariable);
                    parmeterValueAndId.put(tempVariable.toString(),
                            multiValues[i]);
                } else if (paramValue instanceof Long) {
                    tempVariable = new BigDecimal((Long) paramValue);
                    paramValueList.add(tempVariable);
                    parmeterValueAndId.put(tempVariable.toString(),
                            multiValues[i]);
                } else if (paramValue instanceof Integer) {
                    tempVariable = new BigDecimal((Integer) paramValue);
                    paramValueList.add(tempVariable);
                    parmeterValueAndId.put(tempVariable.toString(),
                            multiValues[i]);
                } else {
                    tempVariable = (BigDecimal) paramValue;
                    paramValueList.add(tempVariable);
                    parmeterValueAndId.put(tempVariable.toString(),
                            multiValues[i]);
                }
            } else {
                paramValueList.add(BigDecimal.ZERO);
                parmeterValueAndId.put(BigDecimal.ZERO.toString(),
                        multiValues[i]);
            }

        }

    }

    private void createParameterListForAggregateFunctions(
            AssignmentSet assignmentSet,
            AssignmentMatrixAction assignmentMatrixAction, Map map) {

        Map<String, Object> fieldKeyValueMap;
        Map<String, Object> parmeterValueAndId = new HashMap<String, Object>();
        fieldKeyValueMap = AssignmentMatrixMasterUtility
                .convertJsonToMap(assignmentMatrixAction
                        .getAssignActionValues());

        String[] multiValues = ((String) fieldKeyValueMap.get(assignmentSet
                .getAssignmentActionFieldMetaDataList().get(0).getIndexId()))
                .split(AssignmentConstants.MULTI_VALUE_SEPARATOR);
        List<BigDecimal> paramValueList = new ArrayList<BigDecimal>();

        if (null != multiValues && multiValues.length > 0) {

            prepareParameterList(multiValues, parmeterValueAndId,
                    paramValueList, map);

        }

        map.put("contextObjectParamListForAggregateFunction", paramValueList);

    }

    @Override
    public void loadSimulationAssignmentMatrixVO(AssignmentMaster assignmentMaster, SimulationAssignmentMatrixVO assignmentMatrixVO, Map<Object, Object> contextMap) {
        AssignmentSetPojo assignmentSetPojo = null;
        List<AssignmentMatrixRowDataPojo> assignmentMatrixRowDataPojoList = null;
        List<AssignmentSetPojo> assignmentSetPojoList = new ArrayList<>();

        AssignmentMatrixRowDataPojo assignmentMatrixRowDataPojo = null;

        Map<String, String> defaultMatrixAssignmentValues = new HashMap<>();
        defaultMatrixAssignmentValues.put("N.A", null);

        for (AssignmentSet assignmentSet : assignmentMaster.getAssignmentSet()) {
            assignmentSetPojo = new AssignmentSetPojo();
            assignmentSetPojo.setAssignmentSetName(assignmentSet.getAssignmentSetName());
            assignmentMatrixRowDataPojoList = new ArrayList<>();

            sortAssignmentMatrixRowDataByPriority(assignmentSet.getAssignmentMatrixRowData());
            for (AssignmentMatrixRowData assignmentMatrixRowData : assignmentSet.getAssignmentMatrixRowData()) {
                assignmentMatrixRowDataPojo = new AssignmentMatrixRowDataPojo();
                assignmentMatrixRowDataPojo.setAssignmentMatrixRowDataName(assignmentMatrixRowData.getRule()
                        .getDisplayName());
                assignmentMatrixRowDataPojo.setPriority(assignmentMatrixRowData.getPriority());
                // In case when rule id = null or rule result = false
                assignmentMatrixRowDataPojo.setAssignmentValueMap(defaultMatrixAssignmentValues);

                if (contextMap.get(assignmentMatrixRowData.getRule().getId()) != null) {
                    assignmentMatrixRowDataPojo.setRuleResults(contextMap.get(assignmentMatrixRowData.getRule().getId())
                            .toString());

                    assignmentMatrixRowDataPojo.setRuleExpression(expressionEvaluator.getRuleExpressionWithValues(
                            ((ScriptRule) assignmentMatrixRowData.getRule()).getScriptCode(), contextMap,
                            getOGNLwithNameMap(assignmentMatrixRowData.getRowMapValues(), contextMap)));

                    if ((Boolean) contextMap.get(assignmentMatrixRowData.getRule().getId()) == true) {
                        assignmentMatrixRowDataPojo.setAssignmentValueMap(getAssignmentValueMap(
                                assignmentMatrixRowData.getAssignmentMatrixAction(), contextMap));
                    }
                } else {
                    assignmentMatrixRowDataPojo.setRuleResults("N.A");
                    assignmentMatrixRowDataPojo.setRuleExpression("N.A");
                }


                updateRowDataElemetsMap(assignmentMatrixRowDataPojo,((ScriptRule) assignmentMatrixRowData.getRule()).getScriptCode(),contextMap,getOGNLwithNameMap(assignmentMatrixRowData.getRowMapValues(), contextMap),assignmentMatrixRowData);
                assignmentMatrixRowDataPojoList.add(assignmentMatrixRowDataPojo);
            }
            assignmentSetPojo.setAssignmentMatrixRowDataPojoList(assignmentMatrixRowDataPojoList);
            assignmentSetPojoList.add(assignmentSetPojo);
        }
        assignmentMatrixVO.setAssignmentSetPojoList(assignmentSetPojoList);
        assignmentMatrixVO.setAssignmentMatrixName(assignmentMaster.getName());
    }

    @Override
    public void loadSimulationRuleMatrixVO(RuleMatrixMaster ruleMatrixMaster, SimulationAssignmentMatrixVO assignmentMatrixVO, Map<Object, Object> contextMap) {
        AssignmentSetPojo assignmentSetPojo = null;
        List<AssignmentMatrixRowDataPojo> assignmentMatrixRowDataPojoList = null;
        List<AssignmentSetPojo> assignmentSetPojoList = new ArrayList<AssignmentSetPojo>();

        AssignmentMatrixRowDataPojo assignmentMatrixRowDataPojo = null;

        Map<String, String> defaultMatrixAssignmentValues = new HashMap<String, String>();
        defaultMatrixAssignmentValues.put("N.A", null);

        for (AssignmentSet assignmentSet : ruleMatrixMaster.getAssignmentSet()) {
            assignmentSetPojo = new AssignmentSetPojo();
            assignmentSetPojo.setAssignmentSetName(assignmentSet.getAssignmentSetName());
            assignmentMatrixRowDataPojoList = new ArrayList<AssignmentMatrixRowDataPojo>();

            sortAssignmentMatrixRowDataByPriority(assignmentSet.getAssignmentMatrixRowData());
            for (AssignmentMatrixRowData assignmentMatrixRowData : assignmentSet.getAssignmentMatrixRowData()) {
                assignmentMatrixRowDataPojo = new AssignmentMatrixRowDataPojo();
                assignmentMatrixRowDataPojo.setAssignmentMatrixRowDataName(assignmentMatrixRowData.getRule()
                        .getDisplayName());
                assignmentMatrixRowDataPojo.setPriority(assignmentMatrixRowData.getPriority());
                // In case when rule id = null or rule result = false
                assignmentMatrixRowDataPojo.setAssignmentValueMap(defaultMatrixAssignmentValues);

                if (contextMap.get(assignmentMatrixRowData.getRule().getId()) != null) {
                    assignmentMatrixRowDataPojo.setRuleResults(contextMap.get(assignmentMatrixRowData.getRule().getId())
                            .toString());
                    contextMap.put("MetadataList",((AssignmentGrid) assignmentSet).getAssignmentFieldMetaDataList());
                    assignmentMatrixRowDataPojo.setRuleExpression(expressionEvaluator.getRuleExpressionWithValues(
                            ((ScriptRule) assignmentMatrixRowData.getRule()).getScriptCode(), contextMap,
                            getOGNLwithNameMap(assignmentMatrixRowData.getRowMapValues(), contextMap)));

                    if ((Boolean) contextMap.get(assignmentMatrixRowData.getRule().getId()) == true) {
                        assignmentMatrixRowDataPojo.setAssignmentValueMap(getRuleMatrixAssignmentValueMap(
                                assignmentMatrixRowData.getAssignmentMatrixAction(), contextMap));
                    }
                } else {
                    assignmentMatrixRowDataPojo.setRuleResults("N.A");
                    assignmentMatrixRowDataPojo.setRuleExpression("N.A");

                }
                updateRowDataElemetsMap(assignmentMatrixRowDataPojo,((ScriptRule) assignmentMatrixRowData.getRule()).getScriptCode(),contextMap,getOGNLwithNameMap(assignmentMatrixRowData.getRowMapValues(), contextMap),assignmentMatrixRowData);

                assignmentMatrixRowDataPojoList.add(assignmentMatrixRowDataPojo);
            }
            assignmentSetPojo.setAssignmentMatrixRowDataPojoList(assignmentMatrixRowDataPojoList);
            assignmentSetPojoList.add(assignmentSetPojo);
        }
        assignmentMatrixVO.setAssignmentSetPojoList(assignmentSetPojoList);
        assignmentMatrixVO.setAssignmentMatrixName(ruleMatrixMaster.getName());
    }

    private void updateRowDataElemetsMap(AssignmentMatrixRowDataPojo assignmentMatrixRowDataPojo, String scriptCode, Map<Object, Object> contextMap, Map<String, String> ognLwithNameMap,AssignmentMatrixRowData assignmentMatrixRowData) {
        String[] ruleArray=null;
        if(null !=assignmentMatrixRowData.getRuleExp()){
            String ruleExp=assignmentMatrixRowData.getRuleExp();
            ruleArray = ruleExp.split(",");
        }
        String[] scriptCodeElements = new String[0];
        if (StringUtils.isNotEmpty(scriptCode)) {
            scriptCodeElements = scriptCode.split("&&");
        }

        Integer index = 0;
        Integer ruleIndex = 0;
        Map<Integer, AssignmentMatrixRowDataElementsPojo> elementsPojoHashMap = new HashMap<>();
        for (String scriptCodeElement : scriptCodeElements) {
            if(null != scriptCodeElement && "true".equalsIgnoreCase(scriptCodeElement.trim()) && null !=ruleArray && ruleArray.length>0){
                String rule=ruleArray[ruleIndex];
                String[] ruleIdR=rule.split(":");
                String[] ruleId=ruleIdR[0].split("_");
                if(rule!=null) {
                    AssignmentMatrixRowDataElementsPojo assignmentMatrixRowDataElementsPojo = new AssignmentMatrixRowDataElementsPojo();
                    if("P".equalsIgnoreCase(ruleIdR[1])){
                        assignmentMatrixRowDataElementsPojo.setExpectedValue("PASS");
                    }else if("F".equalsIgnoreCase(ruleIdR[1])){
                        assignmentMatrixRowDataElementsPojo.setExpectedValue("FAIL");
                    }else if("*".equalsIgnoreCase(ruleIdR[1])){
                        assignmentMatrixRowDataElementsPojo.setExpectedValue("ANYTHING");
                    }
                    assignmentMatrixRowDataElementsPojo.setOperation("IS EQUAL TO");
                    Rule rule1=ruleService.getRule(Long.valueOf(ruleId[0]));
                    assignmentMatrixRowDataElementsPojo.setTokenKey(rule1.getName());
                    Boolean result =(Boolean) contextMap.get(assignmentMatrixRowData.getId()+"_"+ruleId[0]);
                    if (!assignmentMatrixRowDataPojo.getRuleResults().equals("N.A")) {
                        Boolean actResult= (Boolean) contextMap.get(assignmentMatrixRowData.getId() + "_" + ruleId[0]+"_actual");
                        assignmentMatrixRowDataElementsPojo.setActualValue((actResult) ? "PASS" : "FAIL");
                        assignmentMatrixRowDataElementsPojo.setResult((result) ? "true" : "false");
                    } else {
                        assignmentMatrixRowDataElementsPojo.setResult("[ignored]");
                        assignmentMatrixRowDataElementsPojo.setActualValue("[ignored]");
                    }
                    elementsPojoHashMap.put(index, assignmentMatrixRowDataElementsPojo);
                    index++;
                    ruleIndex++;
                    continue;
                }
            }
            Map<String, String> tokenMap = expressionEvaluator.getRuleExpressionKeyElementsForSimulation(scriptCodeElement, contextMap, ognLwithNameMap);
            if (tokenMap.containsKey("operator")) {
                AssignmentMatrixRowDataElementsPojo assignmentMatrixRowDataElementsPojo = new AssignmentMatrixRowDataElementsPojo();
                assignmentMatrixRowDataElementsPojo.setActualValue(String.valueOf(tokenMap.get("actualValue")));
                assignmentMatrixRowDataElementsPojo.setExpectedValue(String.valueOf(tokenMap.get("expectedValue")));
                convertOperatorToString(tokenMap);
                assignmentMatrixRowDataElementsPojo.setOperation(String.valueOf(tokenMap.get("operator")));
                assignmentMatrixRowDataElementsPojo.setTokenKey(String.valueOf(tokenMap.get("tokenName")));
                if (!assignmentMatrixRowDataPojo.getRuleResults().equals("N.A")) {
                    int leftParanthesis = StringUtils.countMatches(scriptCodeElement, "(");
                    int rightParenthesis = StringUtils.countMatches(scriptCodeElement, ")");
                    if (leftParanthesis > rightParenthesis) {
                        scriptCodeElement = StringUtils.replaceOnce(scriptCodeElement, "(", "");
                    }
                    else if(rightParenthesis > leftParanthesis){
                        int lastIndex = scriptCodeElement.lastIndexOf(')');
                        scriptCodeElement = scriptCodeElement.substring(0, lastIndex - 1) + scriptCodeElement.substring(lastIndex + 1, scriptCodeElement.length());
                    }
                    Serializable compiledExpression = MVEL.compileExpression(scriptCodeElement);
                    Object evaluationResult = RuleExpressionMvelEvaluator.evaluateCompiledExpression(compiledExpression, contextMap);
                    if (evaluationResult instanceof Boolean) {
                        assignmentMatrixRowDataElementsPojo.setResult(String.valueOf(evaluationResult));
                    } else {
                        assignmentMatrixRowDataElementsPojo.setResult("[ignored]");
                    }
                } else {
                    assignmentMatrixRowDataElementsPojo.setResult("[ignored]");
                }

                elementsPojoHashMap.put(index, assignmentMatrixRowDataElementsPojo);
                index++;
            }
        }

        assignmentMatrixRowDataPojo.setRowDataElementsPojoMap(elementsPojoHashMap);
    }

    private Map<String, String> convertOperatorToString(Map<String, String> tokenMap) {
        String operator = String.valueOf(tokenMap.get("operator"));
        if (StringUtils.isNotEmpty(operator)) {
            operator = RuleConstants.operatorToEnglish.get(operator).toLowerCase();
            tokenMap.put("operator", operator);
        }
        return tokenMap;
    }

    private Map<String, String> getOGNLwithNameMap(String rowMapValues, Map<Object, Object> contextMap) {
        Map<String, String> ognlNameMapping = new HashMap<>();

        Map<String, String> map = new HashMap<>();

        if (StringUtils.isNotBlank(rowMapValues)) {

            ObjectMapper mapper = new ObjectMapper();
            try {
                map = mapper.readValue(rowMapValues, new TypeReference<HashMap<String, String>>() {
                });
            } catch (JsonParseException | JsonMappingException e) {
                map = null;
            } catch (IOException e) {
                map = null;
            }
        }

        if (map != null && !map.isEmpty()) {
            MapIterator<String, String> entrySetMapIterator = new EntrySetMapIterator<String, String>(map);
            while (entrySetMapIterator.hasNext()) {
                entrySetMapIterator.next();

                String entrySetMapValue = entrySetMapIterator.getValue();

                if (StringUtils.isNotBlank(entrySetMapValue)) {

                    List<Long> parameterIds = new ArrayList<Long>();

                    if (entrySetMapValue.contains(AssignmentConstants.MULTI_VALUE_SEPARATOR)) {
                        String[] splittedParameterIds = entrySetMapValue.split("#");
                        for (String id : splittedParameterIds) {
                            parameterIds.add(Long.parseLong(id));
                        }
                    } else {
                        if(!("P".equalsIgnoreCase(entrySetMapValue) || "F".equalsIgnoreCase(entrySetMapValue)|| "*".equalsIgnoreCase(entrySetMapValue))) {
                            if(NumberUtils.isNumber(entrySetMapValue) && !entrySetMapValue.contains(".")){
                                parameterIds.add(Long.parseLong(entrySetMapValue));
                            }
                    }
                    }

                    if (CollectionUtils.isNotEmpty(parameterIds)) {
                        for (Long parameterId : parameterIds) {
                            Parameter parameter = ruleService.getParameter(parameterId);
                            if (parameter != null) {
                                Boolean notReferanceType = true;
                                if(null != contextMap.get("MetadataList")) {
                                    List<AssignmentFieldMetaData> assignmentFieldMetaData = (List<AssignmentFieldMetaData>) contextMap.get("MetadataList");
                                    Iterator<AssignmentFieldMetaData> iterator = assignmentFieldMetaData.iterator();
                                    while (iterator.hasNext()) {
                                        AssignmentFieldMetaData assignmentFieldMetaData1 = iterator.next();
                                        if (6 == assignmentFieldMetaData1.getDataType()) {
                                            if (assignmentFieldMetaData1.getIndexId().equalsIgnoreCase(entrySetMapIterator.getKey()))
                                                notReferanceType= false;
                                        }
                                    }
                                }
                                if(notReferanceType) {
                                    expressionEvaluator.getOgnlParamMapForParameter(parameter, ognlNameMapping, contextMap);
                                }
                            }
                        }
                    }
                }

                ObjectGraphTypes graphType = ruleService.getObjectGraphTypes(Long.parseLong(entrySetMapIterator.getKey()
                        .split("_")[0]));
                if (graphType != null) {
                    String objectGraph = graphType.getObjectGraph();
                    if(null!=objectGraph){
                        objectGraph = objectGraph.trim();
                    }

                    if (Integer.parseInt(graphType.getDataType().getCode()) == ParameterDataType.PARAMETER_DATA_TYPE_DATE) {
                        objectGraph = objectGraph + RuleConstants.RULE_TIME_IN_MILLIS;
                    } else if (Integer.parseInt(graphType.getDataType().getCode()) == ParameterDataType.PARAMETER_DATA_TYPE_REFERENCE) {
                        objectGraph = objectGraph + RuleConstants.RULE_IDS;
                    }
                    ognlNameMapping.put(objectGraph, graphType.getDisplayName());
                }
            }
        }
        return ognlNameMapping;
    }

    private Map<String, String> getRuleMatrixAssignmentValueMap(AssignmentMatrixAction ruleAction, Map contextMap) {

        String actionValues = ruleAction.getAssignActionValues();
        Map<String, String> matrixAssignmentValues = new HashMap<String, String>();

        Map<Long, Object> map = new HashMap<Long, Object>();

        if (StringUtils.isNotBlank(actionValues)) {

            ObjectMapper mapper = new ObjectMapper();
            try {
                map = mapper.readValue(actionValues, new TypeReference<HashMap<Long, Object>>() {
                });
            } catch (JsonParseException | JsonMappingException e) {
                map = null;
            } catch (IOException e) {
                map = null;
            }
        }

        if (map != null && !map.isEmpty()) {
            MapIterator<Long, Object> entrySetMapIterator = new EntrySetMapIterator<Long, Object>(map);
            while (entrySetMapIterator.hasNext()) {
                entrySetMapIterator.next();
                ObjectGraphTypes key = ruleService.getObjectGraphTypes(entrySetMapIterator.getKey());

                Object paramValue = entrySetMapIterator.getValue();
                if (paramValue == null) {
                    matrixAssignmentValues.put(key.getDisplayName() + " = ", null);
                } else if (paramValue instanceof BigDecimal) {
                    paramValue = ((BigDecimal) paramValue).setScale(3, BigDecimal.ROUND_HALF_DOWN);
                    matrixAssignmentValues.put(key.getDisplayName() + " = ", paramValue.toString());
                } else {
                    matrixAssignmentValues.put(key.getDisplayName() + " = ", paramValue.toString());
                }

            }
        }
        return matrixAssignmentValues;
    }

    private Map<String, String> getAssignmentValueMap(AssignmentMatrixAction ruleAction, Map contextMap) {

        String actionValues = ruleAction.getAssignActionValues();
        Map<String, String> matrixAssignmentValues = new HashMap<String, String>();

        Map<Long, Long> map = new HashMap<Long, Long>();

        if (StringUtils.isNotBlank(actionValues)) {

            ObjectMapper mapper = new ObjectMapper();
            try {
                map = mapper.readValue(actionValues, new TypeReference<HashMap<Long, Long>>() {
                });
            } catch (JsonParseException | JsonMappingException e) {
                map = null;
            } catch (IOException e) {
                map = null;
            }
        }

        if (map != null && !map.isEmpty()) {
            MapIterator<Long, Long> entrySetMapIterator = new EntrySetMapIterator<Long, Long>(map);
            while (entrySetMapIterator.hasNext()) {
                entrySetMapIterator.next();
                ObjectGraphTypes key = ruleService.getObjectGraphTypes(entrySetMapIterator.getKey());
                Parameter valueParameter = ruleService.getParameter(entrySetMapIterator.getValue());
                if(null ==valueParameter ){
                    Object paramValue = entrySetMapIterator.getValue();
                    if (paramValue  == null) {
                        matrixAssignmentValues.put(key.getDisplayName() + " = ", null);
                    } else if (paramValue instanceof BigDecimal) {
                        paramValue = ((BigDecimal) paramValue).setScale(3, BigDecimal.ROUND_HALF_DOWN);
                        matrixAssignmentValues.put(key.getDisplayName() + " = ", paramValue.toString());
                    } else {
                        matrixAssignmentValues.put(key.getDisplayName() + " = ", paramValue.toString());
                    }
                }else {
                    Object paramValue = expressionEvaluator.executeParameter(valueParameter, contextMap, false);
                    if (paramValue == null) {
                        matrixAssignmentValues.put(key.getDisplayName() + " = ", null);
                    } else if (paramValue instanceof BigDecimal) {
                        paramValue = ((BigDecimal) paramValue).setScale(3, BigDecimal.ROUND_HALF_DOWN);
                        matrixAssignmentValues.put(key.getDisplayName() + " = ", paramValue.toString());
                    } else {
                        matrixAssignmentValues.put(key.getDisplayName() + " = ", paramValue.toString());
                    }
                }

            }
        }

        return matrixAssignmentValues;

    }


    private boolean isAssignmentSetValidWithDateConstraints(AssignmentSet assignmentSet, Date applicationTimeStamp) {
        boolean isValid = true;
        if (assignmentSet != null) {
            Date currentDate = new Date();
            Integer bufferDays = assignmentSet.getBufferDays() != null ? assignmentSet.getBufferDays() : 0;
            Date startDate = assignmentSet.getEffectiveFrom();
            Date endDate = assignmentSet.getEffectiveTill();
            if (startDate != null && endDate != null) {
                //if application time stamp is  between start date, end date AND
                // current date is between start date , end date + bufferDays then that assignment set is valid
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(endDate);
                calendar.add(Calendar.DATE, bufferDays);
                Date endDatePlusBufferDays = calendar.getTime();
                if ((applicationTimeStamp.after(startDate) && applicationTimeStamp.before(endDate))
                        && (currentDate.after(startDate) && currentDate.before(endDatePlusBufferDays)))
                    isValid = true;
                else
                    isValid = false;

            }
        }else {
            isValid = false;
        }
        return isValid;
    }

    private List<AssignmentSet> assignmentSetsWithValidCurrentDateConstraints(List<AssignmentSet> assignmentSetsList) {
        List<AssignmentSet> tempAssignmentList = new ArrayList<>();
        for (AssignmentSet assignmentSet : assignmentSetsList) {
            Date currentDate = new Date();
            // if current date is between start date and end date then add assignment set else skip
            if (assignmentSet.getEffectiveFrom() != null && assignmentSet.getEffectiveTill() != null &&
                    currentDate.after(assignmentSet.getEffectiveFrom()) && currentDate.before(assignmentSet.getEffectiveTill()))
                tempAssignmentList.add(assignmentSet);
            if(assignmentSet.getEffectiveFrom()==null || assignmentSet.getEffectiveTill()==null)
                tempAssignmentList.add(assignmentSet);
        }
        return tempAssignmentList;
    }
}
