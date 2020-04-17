/**
 * Copyright Nucleus Software India Pvt Ltd. All rights reserved.
 */
package com.nucleus.core.event.adhocTask;

import com.nucleus.core.event.NeutrinoEvent;
import com.nucleus.core.event.NeutrinoEventWorker;

/**
 * 
 * @author Nucleus Software India Pvt Ltd 
 */

public class AdhocTaskEvent extends NeutrinoEvent {

    public AdhocTaskEvent(Object source, String name, NeutrinoEventWorker neutrinoEventWorker) {
        super(source, name, neutrinoEventWorker);

    }

}
