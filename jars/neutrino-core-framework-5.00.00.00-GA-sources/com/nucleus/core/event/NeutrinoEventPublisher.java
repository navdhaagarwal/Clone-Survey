/**
 * Copyright Nucleus Software India Pvt Ltd. All rights reserved.
 */
package com.nucleus.core.event;

import javax.inject.Named;

import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
/**
 * @author Nucleus Software India Pvt Ltd 
 */
@Named(value = "neutrinoEventPublisher")
public class NeutrinoEventPublisher implements ApplicationEventPublisherAware {

    private ApplicationEventPublisher publisher;

    @Override
    public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        publisher = applicationEventPublisher;
    }

    public void publish(NeutrinoEventWorker worker) {
        publisher.publishEvent(worker.createNeutrinoEvent(this));
    }
    
    public void publish(ApplicationEvent event) {
        publisher.publishEvent(event);
    }  
    
}
