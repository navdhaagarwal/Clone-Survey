/**
 * Copyright Nucleus Software India Pvt Ltd. All rights reserved.
 */
package com.nucleus.rules.service;

import java.util.HashMap;

import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;

import com.nucleus.logging.BaseLoggers;
import com.nucleus.rules.model.RuleGroup;
import com.nucleus.rules.model.RuleInvocationPoint;
import static com.nucleus.finnone.pro.general.util.ValidatorUtils.notNull;
/**
 * @author Nucleus Software India Pvt Ltd 
 */
@Named(value = "ruleGroupEvaluationService")
public class RuleGroupEvaluationServiceImpl implements RuleGroupEvaluationService {

    @Inject
    @Named("ruleSetExecutorService")
    private RuleSetExecutorService ruleSetExecutorService;

    @Inject
    @Named("rulesAuditLogService")
    private RulesAuditLogService   rulesAuditLogService;
    
    
    /**
     * @deprecated (Since GA2.5, To Support configurable auditing)
     */
    @Deprecated
    @Override
    public Boolean executeRuleGroup(RuleGroup ruleGroup, Map<Object, Object> contextObjectMap, String uuid, String eventName) {
    	return executeRuleGroup(ruleGroup, contextObjectMap, uuid, eventName, true, false);
    }

    @Override
    public Boolean executeRuleGroup(RuleGroup ruleGroup, Map<Object, Object> contextObjectMap, String uuid, String eventName,boolean auditingEnabled, boolean purgingRequired) {

        Boolean result = null;
        contextObjectMap.put("Uuid_Audit_Excep",uuid);
        try {
            if (null != ruleGroup && notNull(ruleGroup.getRuleGroupExpression())) {
                Map<Object, Object> relationMap = ruleSetExecutorService.evaluateRuleGroup(ruleGroup, contextObjectMap);

                Map<String, Object> resultMap = new HashMap<String, Object>();
                Map<Object, Object> ruleGroupResults = new HashMap<Object, Object>();
                Map<Object, Map<Object, Object>> ruleAuditMap = new HashMap<Object, Map<Object, Object>>();

                ruleGroupResults.put(ruleGroup, relationMap.get(RuleConstants.RULE_GROUP_PATTERN_KEY));

                result = (Boolean) relationMap.get(RuleConstants.RULE_GROUP_RESULT_KEY);
                resultMap.put(RuleInvocationPoint.RULE_INVOCATION_RESULT_VALUE, result);

                ruleAuditMap.put(RuleConstants.RULEGROUP_KEY, ruleGroupResults);

                // Rule Auditing

                rulesAuditLogService.ruleInvocationMappingAudit(uuid, ruleAuditMap, relationMap, eventName ,auditingEnabled,purgingRequired);
            }

            return result;

        } catch (Exception e) {
            // exceptionLoggingService.saveExceptionDataInCouch(getCurrentUser(), e);
            BaseLoggers.exceptionLogger.error("Exception Occured in executeRuleGroup" , e);
            return null;
        }
    }

}
