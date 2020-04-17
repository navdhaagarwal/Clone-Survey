package com.nucleus.rules.service;

/**
 * Copyright Nucleus Software India Pvt Ltd. All rights reserved.
 */

import java.util.List;
import java.util.Map;

import com.nucleus.core.event.NeutrinoEvent;
import com.nucleus.core.event.NeutrinoEventPublisher;
import com.nucleus.core.event.NeutrinoEventWorker;
import com.nucleus.rules.model.RuleGroup;
import com.nucleus.rules.model.RulesAuditLog;

/**
 *
 * @author Nucleus Software India Pvt Ltd
 */
public class RuleAUditLogEventClassWorker extends NeutrinoEventWorker {





    /** The result. */
    private List<RulesAuditLog> ruleAuditLogList;








    /**
     * Instantiates a new neutrino rule validation event worker.
     *
     * @param name the name
     */
    public RuleAUditLogEventClassWorker(String name) {
        super(name);
    }









    @Override
    public NeutrinoEvent createNeutrinoEvent(NeutrinoEventPublisher publisher) {
        RuleAUditLogEventClass event = new RuleAUditLogEventClass(publisher, "Invoking  Rule Audit Log  task ", this);

        return event;
    }

    public List<RulesAuditLog> getRuleAuditLogList() {
        return ruleAuditLogList;
    }

    public void setRuleAuditLogList(List<RulesAuditLog> ruleAuditLogList) {
        this.ruleAuditLogList = ruleAuditLogList;
    }
}
