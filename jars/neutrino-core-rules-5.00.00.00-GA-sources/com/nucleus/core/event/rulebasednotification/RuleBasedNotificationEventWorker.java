/**
 * Copyright Nucleus Software India Pvt Ltd. All rights reserved.
 */
package com.nucleus.core.event.rulebasednotification;

import java.util.Map;

import com.nucleus.core.event.NeutrinoEvent;
import com.nucleus.core.event.NeutrinoEventPublisher;
import com.nucleus.core.event.NeutrinoEventWorker;
import com.nucleus.notificationMaster.NotificationMaster;
import com.nucleus.rules.model.RuleGroup;

import fr.opensagres.xdocreport.template.formatter.FieldsMetadata;

/**
 * 
 * @author Nucleus Software India Pvt Ltd 
 */
public class RuleBasedNotificationEventWorker extends NeutrinoEventWorker {

    private RuleGroup          ruleGroup;
    private Map                map;
    private NotificationMaster notificationMaster;
    private String             uuid;
    private FieldsMetadata     metadata;
    
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
	

    public RuleBasedNotificationEventWorker(String name) {
        super(name);
    }

    /**
     * @return the notificationMaster
     */
    public NotificationMaster getNotificationMaster() {
        return notificationMaster;
    }

    /**
     * @param notificationMaster the notificationMaster to set
     */
    public void setNotificationMaster(NotificationMaster notificationMaster) {
        this.notificationMaster = notificationMaster;
    }

    /**
     * @return the map
     */
    public Map getMap() {
        return map;
    }

    /**
     * @param map the map to set
     */
    public void setMap(Map map) {
        this.map = map;
    }

    /**
     * @return the ruleGroup
     */
    public RuleGroup getRuleGroup() {
        return ruleGroup;
    }

    /**
     * @param ruleGroup the ruleGroup to set
     */
    public void setRuleGroup(RuleGroup ruleGroup) {
        this.ruleGroup = ruleGroup;
    }

    /**
     * @return the uuid
     */
    public String getUuid() {
        return uuid;
    }

    /**
     * @param uuid the uuid to set
     */
    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    /**
     * @return the metadata
     */
    public FieldsMetadata getMetadata() {
        return metadata;
    }

    /**
     * @param metadata the metadata to set
     */
    public void setMetadata(FieldsMetadata metadata) {
        this.metadata = metadata;
    }

    public NeutrinoEvent createNeutrinoEvent(NeutrinoEventPublisher publisher) {
        RuleBasedNotificationEvent event = new RuleBasedNotificationEvent(publisher, description, this);

        return event;
    }

}
