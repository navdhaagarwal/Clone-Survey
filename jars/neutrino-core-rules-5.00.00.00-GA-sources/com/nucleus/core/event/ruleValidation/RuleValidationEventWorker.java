/**
 * Copyright Nucleus Software India Pvt Ltd. All rights reserved.
 */
package com.nucleus.core.event.ruleValidation;

import java.util.Map;

import com.nucleus.core.event.NeutrinoEvent;
import com.nucleus.core.event.NeutrinoEventPublisher;
import com.nucleus.core.event.NeutrinoEventWorker;
import com.nucleus.rules.model.RuleGroup;

/**
 * 
 * @author Nucleus Software India Pvt Ltd 
 */
public class RuleValidationEventWorker extends NeutrinoEventWorker {

    /** The map. */
    private Map       map;

    /** The rule group. */
    private RuleGroup ruleGroup;

    /** The result. */
    private Boolean   ruleGroupResult;

    /** The uuid. */
    private String    uuid;
    
    
    private boolean 						   auditingEnabled=true;
    private boolean 						   purgingRequired=false;
    	public boolean isAuditingEnabled() {
    		return auditingEnabled;
    	}

    	public void setAuditingEnabled(boolean auditingEnabled) {
    		this.auditingEnabled = auditingEnabled;
    	}

    	public boolean isPurgingRequired() {
    		return purgingRequired;
    	}

    	public void setPurgingRequired(boolean purgingRequired) {
    		this.purgingRequired = purgingRequired;
    	}
	

    /**
     * Instantiates a new neutrino rule validation event worker.
     *
     * @param name the name
     */
    public RuleValidationEventWorker(String name) {
        super(name);
    }

    /**
     * Gets the map.
     *
     * @return the map
     */
    public Map getMap() {
        return map;
    }

    /**
     * Sets the map.
     *
     * @param map the new map
     */
    public void setMap(Map map) {
        this.map = map;
    }

    /**
     * Gets the rule group.
     *
     * @return the ruleGroup
     */
    public RuleGroup getRuleGroup() {
        return ruleGroup;
    }

    /**
     * Sets the rule group.
     *
     * @param ruleGroup the ruleGroup to set
     */
    public void setRuleGroup(RuleGroup ruleGroup) {
        this.ruleGroup = ruleGroup;
    }

    /**
     * Gets the uuid.
     *
     * @return the uuid
     */
    public String getUuid() {
        return uuid;
    }

    /**
     * Sets the uuid.
     *
     * @param uuid the uuid to set
     */
    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    /**
     * @return the ruleGroupResult
     */
    public Boolean getRuleGroupResult() {
        return ruleGroupResult;
    }

    /**
     * @param ruleGroupResult the ruleGroupResult to set
     */
    public void setRuleGroupResult(Boolean ruleGroupResult) {
        this.ruleGroupResult = ruleGroupResult;
    }

    @Override
    public NeutrinoEvent createNeutrinoEvent(NeutrinoEventPublisher publisher) {
        RuleValidationEvent event = new RuleValidationEvent(publisher, "Invoking  Rule Validation task with RuleGroup"
                + ruleGroup.getName(), this);

        return event;
    }

}
