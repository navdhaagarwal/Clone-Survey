package com.nucleus.core.event.interfaceRun;


import com.nucleus.eventInterfaceCode.EventInterfaceCode;
import com.nucleus.core.event.NeutrinoEvent;
import com.nucleus.core.event.NeutrinoEventPublisher;
import com.nucleus.core.event.NeutrinoEventWorker;

import com.nucleus.rules.model.RuleGroup;

import java.util.Map;

public class InterfaceWorker extends NeutrinoEventWorker{

    private EventInterfaceCode eventInterfaceCode;
    private Map  contextMap;
    private RuleGroup ruleGroup;
    private String             uuid;
    private Boolean auditingEnabled=true;
    private Boolean purgingRequired=false;
    private Boolean isRuleBased=false;

    public InterfaceWorker(String name){super(name);}

    public EventInterfaceCode getEventInterfaceCode() {
        return eventInterfaceCode;
    }

    public void setEventInterfaceCode(EventInterfaceCode eventInterfaceCode) {
        this.eventInterfaceCode = eventInterfaceCode;
    }

    public Map getContextMap() {
        return contextMap;
    }

    public void setContextMap(Map contextMap) {
        this.contextMap = contextMap;
    }

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


    public Boolean isAuditingEnabled() {
        return auditingEnabled;
    }

    public void setAuditingEnabled(Boolean auditingEnabled) {
        this.auditingEnabled = auditingEnabled;
    }

    public Boolean isPurgingRequired() {
        return purgingRequired;
    }

    public void setPurgingRequired(Boolean purgingRequired) {
        this.purgingRequired = purgingRequired;
    }

    public Boolean isRuleBased() {
        return isRuleBased;
    }

    public void setRuleBased(Boolean ruleBased) {
        isRuleBased = ruleBased;
    }

    public NeutrinoEvent createNeutrinoEvent(NeutrinoEventPublisher publisher) {
        InterfaceEvent event = new InterfaceEvent(publisher, "Starting Interface Execution for "
                + eventInterfaceCode.getName(), this);

        return event;
    }
}
