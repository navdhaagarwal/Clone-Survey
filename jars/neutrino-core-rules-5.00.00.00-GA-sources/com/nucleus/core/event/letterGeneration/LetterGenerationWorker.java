package com.nucleus.core.event.letterGeneration;


import com.nucleus.core.event.NeutrinoEvent;
import com.nucleus.core.event.NeutrinoEventPublisher;
import com.nucleus.core.event.NeutrinoEventWorker;
import com.nucleus.letterMaster.LetterType;
import com.nucleus.rules.model.RuleGroup;

import java.util.Map;

public class LetterGenerationWorker extends NeutrinoEventWorker{

    private LetterType letterType;
    private Map  contextMap;
    private RuleGroup ruleGroup;
    private String             uuid;
    private Boolean auditingEnabled=true;
    private Boolean purgingRequired=false;
    private Boolean isRuleBased=false;

    public LetterGenerationWorker(String name){super(name);}

    public LetterType getLetterType() {
        return letterType;
    }

    public void setLetterType(LetterType letterType) {
        this.letterType = letterType;
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
        LetterGenerationEvent event = new LetterGenerationEvent(publisher, "Starting letter generation for "
                + letterType.getName(), this);

        return event;
    }
}
