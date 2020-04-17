/**
 * Copyright Nucleus Software India Pvt Ltd. All rights reserved.
 */
package com.nucleus.core.event;


/**
 * 
 * @author Nucleus Software India Pvt Ltd 
 */
public class RuleMatrixEvent extends NeutrinoEvent {

    public RuleMatrixEvent(Object source, String name, NeutrinoEventWorker eventWorker) {
        super(source, name, eventWorker);
    }

}

