package com.nucleus.rules.service;

import java.util.List;
import java.util.Map;

import org.joda.time.DateTime;

import com.nucleus.makerchecker.UnapprovedEntityData;
import com.nucleus.rules.model.RulesAuditLog;
import com.nucleus.rules.model.RulesAuditLogParametersValues;
import com.nucleus.service.BaseService;

/**
 * 
 * @author Nucleus Software Exports Limited
 * Audit logging of Rules
 */
public interface RulesAuditLogService extends BaseService {
    /**
     * 
     * To do audit logging for Rules
     * @param uuid
     * @param map
     * @param Objectmap
     * @deprecated (Since GA2.5, To Support configurable auditing)
    */
   @Deprecated
    public void ruleInvocationMappingAudit(String uuid, Map<Object, Map<Object, Object>> map, Map<Object, Object> Objectmap,
            String invocationPoint);

    /**
     * @param uuid
     * @param ruleInvocationMappingResults
     * @param objectMap
     * @param invocationPoint
     * @param auditingEnabled
     * @param purgingRequired
     */
    void ruleInvocationMappingAudit(String uuid, Map<Object, Map<Object, Object>> ruleInvocationMappingResults,
            Map<Object, Object> objectMap, String invocationPoint,boolean auditingEnabled,boolean purgingRequired);

    /**
     * To get expression with actual parameter value at the time of execution
     * @param uuid
     * @param RuleId
     * @return
     */
    public String ruleExpressionWithParameterValue(String uuid, Long ruleId);

    /**
     * Get status of all the executed rule
     * @param uuid
     * @return
     */
    public Map<Long, String> getRuleStatus(String uuid);

    /**
     * 
     * Return list of error messages
     * @param uuid
     * @return
     */
    public List<String> getRuleErrorMessages(String uuid);

    /**
     * 
     * Returns list of AuditLogParameters from rule_invocationuuid and Rule Id
     * @param uuid
     * @param ruleId
     * @return
     */

    public List<RulesAuditLogParametersValues> getRulesAuditLogParameters(String uuid, Long ruleId);

    /**
     * 
     * Return list of AuditLog from MapQueryExecutor
     * @param uuid
     * @return
     */
    public List<Map<String, ?>> getRulesAuditLogMap(String uuid);

    /**
     * 
     * Returns Add RuleInvocationPoints from rules_audit_log Table
     * @return
     */
    public List<String> getDistinctRuleInvocationPointsFromRuleAudits();

    /**
     * 
     * Return the string of error messages
     * @param allRulesResult
     * @return
     */

    public List<String> getRulesErrorMessages(Map<Object, Object> allRulesResult, Map<Object,Object> contextMap);
    public List<String> getRulesErrorMessages(Map<Object, Object> allRulesResult);

    /**
     * Return invocation point name on the basic of uuid
     * @param uuid
     * @return
     */
    public String getInvocationPointFromUUID(String uuid);

    /**
     * 
     * Get original rule
     * @param ruleId
     * @param creationTimeStamp
     * @return
     */
    public Long getOriginalRule(Long ruleId, DateTime creationTimeStamp);

    /**
     * 
     * Method to load Rule change state
     * @param uuid
     * @return
     */
    public List<UnapprovedEntityData> getRuleChangeState(String uuid);
    
    /**
     * @param ruleAuditLogList
     */
    void saveRuleAuditLogData(final List<RulesAuditLog> ruleAuditLogList);
}
