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

import java.io.Serializable;
import java.util.List;

/**
 * @author Nucleus Software Exports Limited
 */
public class SimulationRuleVO implements Serializable {

    private static final long serialVersionUID = 7106722166980177873L;

    private String                      ruleResult;

    private List<SimulationParameterVO> auditParameters;

    private String                      ruleName;

    private String                      ruleEnglishExpression;

    /**
     * @return the ruleResult
     */
    public String getRuleResult() {
        return ruleResult;
    }

    /**
     * @return the auditParameters
     */
    public List<SimulationParameterVO> getAuditParameters() {
        return auditParameters;
    }

    /**
     * @param ruleResult the ruleResult to set
     */
    public void setRuleResult(String ruleResult) {
        this.ruleResult = ruleResult;
    }

    /**
     * @param auditParameters the auditParameters to set
     */
    public void setAuditParameters(List<SimulationParameterVO> auditParameters) {
        this.auditParameters = auditParameters;
    }

    /**
     * @return the ruleName
     */
    public String getRuleName() {
        return ruleName;
    }

    /**
     * @param ruleName the ruleName to set
     */
    public void setRuleName(String ruleName) {
        this.ruleName = ruleName;
    }

    /**
     * @return the ruleEnglishExpression
     */
    public String getRuleEnglishExpression() {
        return ruleEnglishExpression;
    }

    /**
     * @param ruleEnglishExpression the ruleEnglishExpression to set
     */
    public void setRuleEnglishExpression(String ruleEnglishExpression) {
        this.ruleEnglishExpression = ruleEnglishExpression;
    }

}
