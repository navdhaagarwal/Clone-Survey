/**
 * Copyright Nucleus Software India Pvt Ltd. All rights reserved.
 */package com.nucleus.core.event;
/**
 * @author Nucleus Software India Pvt Ltd 
 */
public class NeutrinoEventWorker {
    
    protected String description; 
    
    public NeutrinoEventWorker(String name){
        this.description = name;
    }
    
    /**
     * @return the name
     */
    public String getName() {
        return description;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.description = name;
    }

    public NeutrinoEvent createNeutrinoEvent(NeutrinoEventPublisher publisher){
        NeutrinoEvent event = new NeutrinoEvent(publisher, description, this);
        return event; 
    }
    
    
}
