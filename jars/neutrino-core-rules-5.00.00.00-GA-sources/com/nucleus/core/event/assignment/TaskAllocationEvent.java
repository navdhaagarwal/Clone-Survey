/**
 * Copyright Nucleus Software India Pvt Ltd. All rights reserved.
 */
package com.nucleus.core.event.assignment;

import com.nucleus.core.event.NeutrinoEvent;
import com.nucleus.core.event.NeutrinoEventWorker;

/**
 * 
 * @author Nucleus Software India Pvt Ltd 
 */
public class TaskAllocationEvent extends NeutrinoEvent {

    public TaskAllocationEvent(Object source, String name, NeutrinoEventWorker eventWorker) {
        super(source, name, eventWorker);
    }

}
