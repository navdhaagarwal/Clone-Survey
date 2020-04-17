package com.nucleus.rules.service;

import com.nucleus.rules.model.RuleExceptionLoggingVO;
import com.nucleus.service.BaseService;

import java.util.List;
import java.util.Map;

public interface RuleExceptionLoggingService extends BaseService {

    public void saveRuleErrorLogs(RuleExceptionLoggingVO ruleExceptionLoggingVO);
    public List getRuleExceptionTypeForAudit(String uuid);
    public List<Map<String, Object>> getRuleExceptionForAudit(List<String> uuid);
    public Map<String,Object> getRuleExceptionMessageForAuditByUuidCode(String uuid,String uuidCode);
}
