package com.nucleus.rules.service;

import com.nucleus.core.event.NeutrinoEvent;
import com.nucleus.core.event.NeutrinoEventWorker;

/**
 *
 * @author Nucleus Software India Pvt Ltd
 */
public class RuleAUditLogEventClass extends NeutrinoEvent {

    public RuleAUditLogEventClass(Object source, String name, NeutrinoEventWorker neutrinoEventWorker) {
        super(source, name, neutrinoEventWorker);
    }

}
