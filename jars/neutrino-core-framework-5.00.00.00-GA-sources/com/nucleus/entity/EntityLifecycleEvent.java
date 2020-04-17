/**
 * Copyright Nucleus Software India Pvt Ltd. All rights reserved.
 */
package com.nucleus.entity;

import com.nucleus.event.GenericEvent;

/**
 * Event to denote entity lifecycle events (not through ORM framework but through product framework). This event will hold 
 * the type & id of entity and other useful information.
 * 
 * @author Nucleus Software India Pvt Ltd
 */
public class EntityLifecycleEvent extends GenericEvent {

    private static final long serialVersionUID = 2273236634103346391L;

    public EntityLifecycleEvent(int eventType) {
        super(eventType);
    }

}
