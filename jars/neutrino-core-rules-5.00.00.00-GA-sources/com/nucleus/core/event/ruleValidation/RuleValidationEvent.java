/**
 * Copyright Nucleus Software India Pvt Ltd. All rights reserved.
 */
package com.nucleus.core.event.ruleValidation;

import com.nucleus.core.event.NeutrinoEvent;
import com.nucleus.core.event.NeutrinoEventWorker;
/**
 * 
 * @author Nucleus Software India Pvt Ltd 
 */
public class RuleValidationEvent extends NeutrinoEvent {

    public RuleValidationEvent(Object source, String name, NeutrinoEventWorker neutrinoEventWorker) {
        super(source, name, neutrinoEventWorker);
    }

}
