package com.nucleus.rules.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Named;

import net.bull.javamelody.MonitoredWithSpring;

import com.nucleus.core.misc.util.DateUtils;
import com.nucleus.dao.query.NamedQueryExecutor;
import com.nucleus.entity.ApprovalStatus;
import com.nucleus.event.EventTypes;
import com.nucleus.event.RuleInvocationEvent;
import com.nucleus.master.BaseMasterService;
import com.nucleus.rules.model.Rule;
import com.nucleus.rules.model.RuleActionMapping;
import com.nucleus.rules.model.RuleGroup;
import com.nucleus.rules.model.RuleInvocationMapping;
import com.nucleus.rules.model.RuleInvocationPoint;
import com.nucleus.rules.model.RuleSet;
import com.nucleus.rules.model.RuleSetActionMapping;
import com.nucleus.rules.model.RuleSetPatternActionMapping;
import com.nucleus.service.BaseServiceImpl;
import com.nucleus.user.User;
import com.nucleus.user.UserInfo;

/**
 * Implementation class for RuleInvocationService 
 * 
 * @author Nucleus Software Exports Limited
 */
@Named(value = "ruleInvocationService")
@MonitoredWithSpring(name = "RuleInvocation_Service_IMPL_")
public class RuleInvocationServiceImpl extends BaseServiceImpl implements RuleInvocationService {

    @Inject
    @Named(value = "expressionEvaluator")
    private ExpressionEvaluator          expressionEvaluator;

    @Inject
    @Named("ruleSetExecutorService")
    private RuleSetExecutorService       ruleSetExecutorService;

    @Inject
    @Named(value = "expressionBuilder")
    private ExpressionBuilder            expressionBuilder;

    @Inject
    @Named("ruleExpressionBuilder")
    private RuleExpressionBuilder        ruleExpressionBuilder;

    @Inject
    @Named("baseMasterService")
    BaseMasterService                    baseMasterService;

    @Inject
    @Named("rulesAuditLogService")
    RulesAuditLogService                 rulesAuditLogService;

    @Inject
    @Named("compiledExpressionBuilder")
    CompiledExpressionBuilder            compiledExpressionBuilder;

    @Inject
    @Named("criteriaRules")
    CriteriaRulesService                 criteriaRulesService;

    @Inject
    @Named("patternMatchAlgorithmService")
    private PatternMatchAlgorithmService patternMatchAlgorithmService;
    
    /**
     * @deprecated (Since GA2.5, To Support configurable auditing)
     */
    @Deprecated
    @Override
    public RuleInvocationResult invokeRule(String invocationPoint, final Map<Object, Object> contextObjectMap) {
    	return invokeRule(invocationPoint,  contextObjectMap,  true, false);	
    }
   
    @Override
    @MonitoredWithSpring(name = "RISI_INVOKE_RULES")
    public RuleInvocationResult invokeRule(String invocationPoint, final Map<Object, Object> contextObjectMap, boolean auditingEnabled, boolean purgingRequired) {

        Map<String, Object> resultMap = new HashMap<String, Object>();
       
        RuleInvocationMapping ruleInvocationMapping = getRuleMapping(invocationPoint);
        if (ruleInvocationMapping == null) {
            return null;
        }

        Map<Object, Object> ruleResults = new HashMap<Object, Object>();
        Map<Object, Object> ruleSetResults = new HashMap<Object, Object>();
        Map<Object, Object> ruleGroupResults = new HashMap<Object, Object>();
        Map<Object, Object> criteriaResult = new HashMap<Object, Object>();

        final Map<Object, Map<Object, Object>> ruleInvocationMappingResults = new HashMap<Object, Map<Object, Object>>();

        //This UUID used for rule exception logging also
        final String uuid = UUID.randomUUID().toString();
        //UUID passed to rule execution via contextObjectMap
        contextObjectMap.put("Uuid_Audit_Excep",uuid);
        // getting start time

        /**
         *  Evaluate Rule from RuleAction Mapping -- Start
         */

        evaluateRuleActions(contextObjectMap, ruleInvocationMapping, ruleResults);

        // //////////////////////////////////////////////////////////////////////////////////////////////////////

        /**
         *  Evaluate Rule from RuleSet -- Start
         */

        evaluateRuleSet(contextObjectMap, ruleInvocationMapping, ruleSetResults);

        // //////////////////////////////////////////////////////////////////////////////////////////////////////

        /**
         *  Evaluate Rule from RuleGroup -- Start
         */

        evaluateRuleGroup(contextObjectMap, resultMap, ruleInvocationMapping, ruleGroupResults);

        // //////////////////////////////////////////////////////////////////////////////////////////////////////

        /**
         *  Evaluate Rule from Criteria Rules -- Start
         */

        evaluateCriteriaRules(contextObjectMap, resultMap, ruleInvocationMapping, criteriaResult);

        // //////////////////////////////////////////////////////////////////////////////////////////////////////

        resultMap.put(RuleInvocationPoint.RULE_INVOCATION_RESULT_TRANSACTION_ID, uuid);

        ruleInvocationMappingResults.put(RuleConstants.RULE_KEY, ruleResults);
        ruleInvocationMappingResults.put(RuleConstants.RULEGROUP_KEY, ruleGroupResults);
        ruleInvocationMappingResults.put(RuleConstants.RULESET_KEY, ruleSetResults);
        ruleInvocationMappingResults.put(RuleConstants.CRITERIA_RULES_RESULT_KEY, criteriaResult);

        rulesAuditLogService.ruleInvocationMappingAudit(uuid, ruleInvocationMappingResults, contextObjectMap, invocationPoint, auditingEnabled, purgingRequired);

        Map<Object, Object> allRulesResult = getRuleExecutionMap(ruleInvocationMappingResults);

        resultMap.put(RuleConstants.ALL_RULES_RESULT, allRulesResult);

        RuleInvocationResult ruleInvocationResult = new RuleInvocationResult(resultMap);

        /**
         * Fire an event into event bus
         */

        UserInfo userInfo = getCurrentUser();
        User user = null;

        if (null != userInfo) {
            user = userInfo.getUserReference();

        } else {
            user = baseMasterService
                    .getMasterEntityById(User.class, Long.parseLong(RuleConstants.SYSTEM_USER.split(":")[1]));
        }

        RuleInvocationEvent event = new RuleInvocationEvent(EventTypes.RULE_INVOCATION_EVENT, invocationPoint,
                user.getEntityId());
        eventBus.fireEvent(event);

        return ruleInvocationResult;
    }

    /**
     * 
     * method evaluateCriteriaRules
     * @param contextObjectMap
     * @param resultMap
     * @param ruleInvocationMapping
     * @param criteriaResult
     */

    private void evaluateCriteriaRules(final Map<Object, Object> contextObjectMap, Map<String, Object> resultMap,
            RuleInvocationMapping ruleInvocationMapping, Map<Object, Object> criteriaResult) {
        // Evaluate Criteria Rules
        if (null != ruleInvocationMapping.getCriteriaRules()) {

            criteriaResult.put(ruleInvocationMapping.getCriteriaRules().getId(),
                    criteriaRulesService.executeRulesCriteria(ruleInvocationMapping.getCriteriaRules(), contextObjectMap));
            resultMap.put(RuleInvocationPoint.RULE_INVOCATION_CRITERIA_RULE_RESULT_VALUE, criteriaResult);
        }
    }

    /**
     * 
     * method to evaluateRuleGroup
     * @param contextObjectMap
     * @param resultMap
     * @param ruleInvocationMapping
     * @param ruleGroupResults
     */

    private void evaluateRuleGroup(final Map<Object, Object> contextObjectMap, Map<String, Object> resultMap,
            RuleInvocationMapping ruleInvocationMapping, Map<Object, Object> ruleGroupResults) {
        // Evaluate Rule Group
        if (null != ruleInvocationMapping.getRuleGroup()) {
            Map<Object, Object> relationMap = ruleSetExecutorService.evaluateRuleGroup(ruleInvocationMapping.getRuleGroup(),
                    contextObjectMap);

            ruleGroupResults
                    .put(ruleInvocationMapping.getRuleGroup(), relationMap.get(RuleConstants.RULE_GROUP_PATTERN_KEY));

            boolean result = (Boolean) relationMap.get(RuleConstants.RULE_GROUP_RESULT_KEY);
            resultMap.put(RuleInvocationPoint.RULE_INVOCATION_RESULT_VALUE, result);
        }
    }

    /**
     * 
     * method to evaluateRuleSet
     * @param contextObjectMap
     * @param ruleInvocationMapping
     * @param ruleSetResults
     */

    private void evaluateRuleSet(final Map<Object, Object> contextObjectMap, RuleInvocationMapping ruleInvocationMapping,
            Map<Object, Object> ruleSetResults) {
        // Evaluate Rule Set
        List<RuleSetActionMapping> ruleSetActionMappings = ruleInvocationMapping.getRulesetMapping();

        for (RuleSetActionMapping ruleSetActionMapping : ruleSetActionMappings) {
            char[] result = ruleSetExecutorService.evaluateRuleSet(ruleSetActionMapping.getRuleSet(), contextObjectMap);
            ruleSetResults.put(ruleSetActionMapping.getRuleSet(), result);

            /*
             * build the String from the char array of pattern
             * and send the string pattern to Pattern Matching Algorithm Service
             * to match the pattern with result Set Pattern 
             */
            StringBuilder ruleSetPattern = new StringBuilder();
            for (int i = 0 ; i < result.length ; i++) {
                ruleSetPattern.append(result[i]);
            }

            for (RuleSetPatternActionMapping ruleSetPatternActionMapping : ruleSetActionMapping.getPatternMappings()) {
                if (patternMatchAlgorithmService.matchPattern(ruleSetPattern.toString(),
                        ruleSetPatternActionMapping.getPattern())) {
                    expressionEvaluator.executeRuleAction(ruleSetPatternActionMapping.getRuleAction(), contextObjectMap);

                    if (null != ruleSetPatternActionMapping.getRuleAction()
                            && ruleSetPatternActionMapping.getRuleAction().getCompiledExpression() == null) {
                        expressionEvaluator.executeRuleAction(ruleSetPatternActionMapping.getRuleAction(), contextObjectMap);

                    } else {
                        compiledExpressionBuilder.executeAssignmentAction(ruleSetPatternActionMapping.getRuleAction(),
                                contextObjectMap);
                    }

                }
            }
        }
    }

    /**
     * 
     * method to evaluateRuleActions
     * @param contextObjectMap
     * @param ruleInvocationMapping
     * @param ruleResults
     */

    private void evaluateRuleActions(final Map<Object, Object> contextObjectMap,
            RuleInvocationMapping ruleInvocationMapping, Map<Object, Object> ruleResults) {
        // Evaluate Rule
        List<RuleActionMapping> ruleActionMappings = ruleInvocationMapping.getRuleMapping();

        if (null != ruleActionMappings && ruleActionMappings.size() > 0) {
            for (RuleActionMapping ruleActionMapping : ruleActionMappings) {                

				char res = compiledExpressionBuilder.evaluateRule(ruleActionMapping.getRule().getId(), contextObjectMap);
                boolean result = res == RuleConstants.RULE_RESULT_PASS ? true : false;

                ruleResults.put(ruleActionMapping.getRule(), result);

                if (result) {

                    if (null != ruleActionMapping.getThenAction()
                            && ruleActionMapping.getThenAction().getCompiledExpression() == null) {

                        expressionEvaluator.executeRuleAction(ruleActionMapping.getThenAction(), contextObjectMap);

                    } else {
                        compiledExpressionBuilder.executeAssignmentAction(ruleActionMapping.getThenAction(),
                                contextObjectMap);
                    }

                } else {
                    if (ruleActionMapping.getElseAction() != null) {
                        if (ruleActionMapping.getElseAction().getCompiledExpression() == null) {

                            expressionEvaluator.executeRuleAction(ruleActionMapping.getElseAction(), contextObjectMap);

                        } else {

                            compiledExpressionBuilder.executeAssignmentAction(ruleActionMapping.getElseAction(),
                                    contextObjectMap);
                        }
                    }

                }
            }
        }
    }

    /**
     * 
     * Return map of Rules with there Results
     * @param ruleInvocationMappingResults
     * @return
     */

    private Map<Object, Object> getRuleExecutionMap(Map<Object, Map<Object, Object>> ruleInvocationMappingResults) {

        Map<Object, Object> allRulesResult = new HashMap<Object, Object>();

        Map<Object, Object> ruleResults = ruleInvocationMappingResults.get(RuleConstants.RULE_KEY);
        Map<Object, Object> ruleSetResults = ruleInvocationMappingResults.get(RuleConstants.RULESET_KEY);
        Map<Object, Object> ruleGroupResults = ruleInvocationMappingResults.get(RuleConstants.RULEGROUP_KEY);

        // For Rule Action
        if (null != ruleResults && ruleResults.size() > 0) {
            for (Map.Entry<Object, Object> entry : ruleResults.entrySet()) {
                allRulesResult.put(entry.getKey(), String.valueOf(entry.getValue()));
            }
        }

        // For Rule Set
        if (null != ruleSetResults && ruleSetResults.size() > 0) {
            for (Map.Entry<Object, Object> entry : ruleSetResults.entrySet()) {
                char[] result = (char[]) entry.getValue();
                RuleSet ruleSet = (RuleSet) entry.getKey();
                List<Rule> ruleList = ruleSet.getRules();
                for (int i = 0 ; i < result.length ; i++) {
                    allRulesResult.put(ruleList.get(i), String.valueOf(getRuleResult(result[i])));
                }
            }
        }

        // For Rule Group
        if (null != ruleGroupResults && ruleGroupResults.size() > 0) {
            for (Map.Entry<Object, Object> entry : ruleGroupResults.entrySet()) {
                char[] result = (char[]) entry.getValue();
                RuleGroup ruleGroup = (RuleGroup) entry.getKey();
                List<Rule> ruleList = ruleGroup.getRules();
                for (int i = 0 ; i < result.length ; i++) {
                    allRulesResult.put(ruleList.get(i), String.valueOf(getRuleResult(result[i])));
                }
            }
        }

        return allRulesResult;
    }

    private boolean getRuleResult(char result) {
        if (result == RuleConstants.RULE_RESULT_PASS) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * To get the  Rule mapping
     * @param invocationPoint
     * @return
     */
    @Override
    public RuleInvocationMapping getRuleMapping(String invocationPoint) {

        NamedQueryExecutor<RuleInvocationMapping> ruleInvocationMapping = new NamedQueryExecutor<RuleInvocationMapping>(
                "RuleInvocationMapping.getRuleMappingByInvocationPoint").addParameter("invocationPoint", invocationPoint)
                .addParameter("approvalStatus", ApprovalStatus.APPROVED);
        return entityDao.executeQueryForSingleValue(ruleInvocationMapping);

    }

    @Override
    public List<Rule> getRuleSet(String ruleGroupExpression) {

        String[] allElements = ruleGroupExpression.split(" ");

        List<Long> ruleIds = new ArrayList<Long>();

        int i = 0;
        for (String ruleParam : allElements) {
            if (!(RuleConstants.LOG_OPERATORS.containsKey(ruleParam) || ruleParam.equals("(") || ruleParam.equals(")"))) {
                String[] ruleId = ruleParam.split("\\" + RuleConstants.PARAMETER_NAME_ID);
                ruleIds.add(Long.parseLong(ruleId[1]));
                i++;
            }
        }
        List<Rule> ruleList = new ArrayList<Rule>();

        for (long ruleId : ruleIds) {
            Rule rule = baseMasterService.getMasterEntityById(Rule.class, ruleId);
            ruleList.add(rule);
        }

        return ruleList;

    }

    /**
         * To save the  Rule mapping
         * @param RuleInvocationMapping object
         * @return void
         */
    @Override
    public void saveRuleInvocationMapping(RuleInvocationMapping ruleInvocation, User user) {
        ruleInvocation.getEntityLifeCycleData().setCreatedByUri(user.getUri());
        ruleInvocation.getEntityLifeCycleData().setCreationTimeStamp(DateUtils.getCurrentUTCTime());
        if (ruleInvocation.getId() == null) {
            entityDao.persist(ruleInvocation);
        } else {
            entityDao.update(ruleInvocation);
        }
    }

    @Override
    public List<RuleInvocationPoint> getAllUnEditedInvocationPoint() {
        NamedQueryExecutor<RuleInvocationPoint> ruleInvocationPoint = new NamedQueryExecutor<RuleInvocationPoint>(
                "RuleInvocationMapping.getNotApprovedRuleInvocationPoint")
        		.addParameter("statusList", Arrays.asList(ApprovalStatus.UNAPPROVED,ApprovalStatus.DELETED_APPROVED_IN_HISTORY,ApprovalStatus.UNAPPROVED_HISTORY));
        return entityDao.executeQuery(ruleInvocationPoint);
    }

}
