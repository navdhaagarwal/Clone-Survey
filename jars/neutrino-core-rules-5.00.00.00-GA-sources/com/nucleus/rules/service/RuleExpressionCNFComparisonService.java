package com.nucleus.rules.service;

import com.nucleus.rules.model.*;

import java.util.*;

public interface RuleExpressionCNFComparisonService {
    List<RuleExpressionCNFMetaData> compareRuleExpression(String ruleExp, String ruleCode);

    void createCnfForAllRules();

    void deleteFromCnfMetaDataCache(String ruleExp, String ruleCode, Long ruleId);

    void updateFromCnfMetaDataCache(String ruleExp, String ruleCode, Long ruleId, Integer approvalStatus);

    public Map<String,Object> preProcessCnf(String cnf);
}
