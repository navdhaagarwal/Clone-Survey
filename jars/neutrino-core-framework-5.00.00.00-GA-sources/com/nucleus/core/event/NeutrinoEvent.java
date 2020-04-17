/**
 * Copyright Nucleus Software India Pvt Ltd. All rights reserved.
 */
package com.nucleus.core.event;

import org.springframework.context.ApplicationEvent;
/**
 * 
 * @author Nucleus Software India Pvt Ltd 
 */
public class NeutrinoEvent extends ApplicationEvent {
    
    String name;
    private NeutrinoEventWorker eventWorker;
    
    public NeutrinoEvent(Object source, String name, NeutrinoEventWorker neutrinoEventWorker ){
        super(source);
        this.name = name;
        this.eventWorker = neutrinoEventWorker;
    }

    /**
     * @return the eventWorker
     */
    public NeutrinoEventWorker getEventWorker() {
        return eventWorker;
    }
    
}
