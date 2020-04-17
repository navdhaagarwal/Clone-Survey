/**
 * Copyright Nucleus Software India Pvt Ltd. All rights reserved.
 */

package com.nucleus.core.event;

import java.util.Map;

import com.nucleus.rules.model.RuleGroup;
import com.nucleus.rules.model.ruleMatrixMaster.RuleMatrixMaster;

/**
 * 
 * @author Nucleus Software India Pvt Ltd 
 */
public class RuleMatrixEventWorker extends NeutrinoEventWorker {

    private RuleMatrixMaster ruleMatrixMaster;
    private Map              contextmap;
    private RuleGroup ruleGroup;
    private String uuid;
    private Boolean auditingEnabled = true;
    private Boolean purgingRequired = false;
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

    public Boolean getAuditingEnabled() {
        return auditingEnabled;
    }

    public void setAuditingEnabled(Boolean auditingEnabled) {
        this.auditingEnabled = auditingEnabled;
    }

    public Boolean getPurgingRequired() {
        return purgingRequired;
    }

    public void setPurgingRequired(Boolean purgingRequired) {
        this.purgingRequired = purgingRequired;
    }



    public Boolean getIsRuleBased() {
		return isRuleBased;
	}

	public void setIsRuleBased(Boolean isRuleBased) {
		this.isRuleBased = isRuleBased;
	}

	public RuleMatrixEventWorker(String name) {
        super(name);
    }

    public RuleMatrixMaster getRuleMatrixMaster() {
        return ruleMatrixMaster;
    }

    public void setRuleMatrixMaster(RuleMatrixMaster ruleMatrixMaster) {
        this.ruleMatrixMaster = ruleMatrixMaster;
    }

    /**
     * @return the contextmap
     */
    public Map getContextmap() {
        return contextmap;
    }

    /**
     * @param contextmap the contextmap to set
     */
    public void setContextmap(Map contextmap) {
        this.contextmap = contextmap;
    }

    public NeutrinoEvent createNeutrinoEvent(NeutrinoEventPublisher publisher) {
        RuleMatrixEvent event = new RuleMatrixEvent(publisher, "Invoking Assignment Master" + ruleMatrixMaster.getName(),
                this);

        return event;
    }

}
