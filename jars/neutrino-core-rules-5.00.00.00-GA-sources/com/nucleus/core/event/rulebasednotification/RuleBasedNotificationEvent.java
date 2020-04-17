/**
 * Copyright Nucleus Software India Pvt Ltd. All rights reserved.
 */
package com.nucleus.core.event.rulebasednotification;

import com.nucleus.core.event.NeutrinoEvent;
import com.nucleus.core.event.NeutrinoEventWorker;
/**
 * 
 * @author Nucleus Software India Pvt Ltd 
 */
public class RuleBasedNotificationEvent extends NeutrinoEvent {

    public RuleBasedNotificationEvent(Object source, String name, NeutrinoEventWorker neutrinoEventWorker) {
        super(source, name, neutrinoEventWorker);
    }

}
