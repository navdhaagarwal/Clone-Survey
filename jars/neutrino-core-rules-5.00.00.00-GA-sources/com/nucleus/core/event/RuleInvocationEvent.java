/**
 * Copyright Nucleus Software India Pvt Ltd. All rights reserved.
 */package com.nucleus.core.event;

 /**
  * @author Nucleus Software India Pvt Ltd 
  */
public class RuleInvocationEvent extends NeutrinoEvent {
    
    
    public RuleInvocationEvent(Object source, String name, NeutrinoEventWorker eventWorker){
        super(source, name, eventWorker);
    }

}
