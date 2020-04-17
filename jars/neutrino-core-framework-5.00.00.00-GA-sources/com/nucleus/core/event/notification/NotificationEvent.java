/**
 * Copyright Nucleus Software India Pvt Ltd. All rights reserved.
 */
package com.nucleus.core.event.notification;

import com.nucleus.core.event.NeutrinoEvent;
import com.nucleus.core.event.NeutrinoEventWorker;
/**
 * 
 * @author Nucleus Software India Pvt Ltd 
 */
public class NotificationEvent extends NeutrinoEvent {

    public NotificationEvent(Object source, String name, NeutrinoEventWorker eventWorker) {
        super(source, name, eventWorker);
    }

}
