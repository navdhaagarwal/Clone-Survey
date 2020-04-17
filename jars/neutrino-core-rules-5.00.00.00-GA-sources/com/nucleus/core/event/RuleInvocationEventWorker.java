/**
 * Copyright Nucleus Software India Pvt Ltd. All rights reserved.
 */
package com.nucleus.core.event;

import java.util.Map;

import com.nucleus.rules.model.RuleGroup;
import com.nucleus.rules.service.RuleInvocationResult;

/**
 * @author Nucleus Software India Pvt Ltd
 */
public class RuleInvocationEventWorker extends NeutrinoEventWorker {

	private String invocationPoint;
	private Map map;
	private RuleInvocationResult ruleInvocationResult;
	private boolean auditingEnabled = true;
	private boolean purgingRequired = false;
    private RuleGroup ruleGroup;
    private String uuid;
    private Boolean isRuleBased = false;   
    
	

	public RuleGroup getRuleGroup() {
		return ruleGroup;
	}

	public void setRuleGroup(RuleGroup ruleGroup) {
		this.ruleGroup = ruleGroup;
	}

	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	public Boolean getIsRuleBased() {
		return isRuleBased;
	}

	public void setIsRuleBased(Boolean isRuleBased) {
		this.isRuleBased = isRuleBased;
	}

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

	public RuleInvocationEventWorker(String description) {
		super(description);
	}

	/**
	 * @return the invocationPoint
	 */
	public String getInvocationPoint() {
		return invocationPoint;
	}

	/**
	 * @param invocationPoint
	 *            the invocationPoint to set
	 */
	public void setInvocationPoint(String invocationPoint) {
		this.invocationPoint = invocationPoint;
	}

	/**
	 * @return the map
	 */
	public Map getMap() {
		return map;
	}

	/**
	 * @param map
	 *            the map to set
	 */
	public void setMap(Map map) {
		this.map = map;
	}

	/**
	 * @return the ruleInvocationResult
	 */
	public RuleInvocationResult getRuleInvocationResult() {
		return ruleInvocationResult;
	}

	/**
	 * @param ruleInvocationResult
	 *            the ruleInvocationResult to set
	 */
	public void setRuleInvocationResult(RuleInvocationResult ruleInvocationResult) {
		this.ruleInvocationResult = ruleInvocationResult;
	}

	public NeutrinoEvent createNeutrinoEvent(NeutrinoEventPublisher publisher) {
		return new RuleInvocationEvent(publisher, description, this);
	}

}
