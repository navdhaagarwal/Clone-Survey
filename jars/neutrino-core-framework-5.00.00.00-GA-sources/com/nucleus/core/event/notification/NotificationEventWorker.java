/**
 * Copyright Nucleus Software India Pvt Ltd. All rights reserved.
 */
package com.nucleus.core.event.notification;

import java.util.Map;

import com.nucleus.core.event.NeutrinoEvent;
import com.nucleus.core.event.NeutrinoEventPublisher;
import com.nucleus.core.event.NeutrinoEventWorker;
import com.nucleus.notificationMaster.NotificationMaster;

import fr.opensagres.xdocreport.template.formatter.FieldsMetadata;

/**
 * 
 * @author Nucleus Software India Pvt Ltd 
 */
public class NotificationEventWorker extends NeutrinoEventWorker {

    private NotificationMaster notificationMaster;
    private Map                map;
    private FieldsMetadata     metadata;

    public NotificationEventWorker(String description) {
        super(description);
    }

    public NeutrinoEvent createNeutrinoEvent(NeutrinoEventPublisher publisher) {
        NotificationEvent event = new NotificationEvent(publisher, "", this);

        return event;
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

}
