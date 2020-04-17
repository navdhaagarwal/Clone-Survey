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
public class SimulationResultVO implements Serializable {

    private static final long      serialVersionUID = -6401202458332227890L;

    private String                 ruleInvocationPoint;

    private List<SimulationRuleVO> rules;

    /**
     * @return the ruleInvocationPoint
     */
    public String getRuleInvocationPoint() {
        return ruleInvocationPoint;
    }

    /**
     * @param ruleInvocationPoint the ruleInvocationPoint to set
     */
    public void setRuleInvocationPoint(String ruleInvocationPoint) {
        this.ruleInvocationPoint = ruleInvocationPoint;
    }

    /**
     * @return the rules
     */
    public List<SimulationRuleVO> getRules() {
        return rules;
    }

    /**
     * @param rules the rules to set
     */
    public void setRules(List<SimulationRuleVO> rules) {
        this.rules = rules;
    }

}
