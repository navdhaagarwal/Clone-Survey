package com.nucleus.rules.usage;

import java.util.List;

public class RuleUsageDataService {

    public List<RuleUsageDataExtractor> ruleUsageDataExtractorList;

    /**
     * 
     * @return
     */

    public List<RuleUsageDataExtractor> getRuleUsageDataExtractorList() {
        return ruleUsageDataExtractorList;
    }

    /**
     * 
     * @param ruleUsageDataExtractorList
     */

    public void setRuleUsageDataExtractorList(List<RuleUsageDataExtractor> ruleUsageDataExtractorList) {
        this.ruleUsageDataExtractorList = ruleUsageDataExtractorList;
    }
}
