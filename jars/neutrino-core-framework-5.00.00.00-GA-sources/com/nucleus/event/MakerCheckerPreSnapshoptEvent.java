package com.nucleus.event;

import com.nucleus.entity.BaseEntity;

public class MakerCheckerPreSnapshoptEvent extends GenericEvent {

    private static final long serialVersionUID = -673775868277965306L;

    public static final String ENTITY_CLASS = "ENTITY_CLASS";
    
    public static final String ENTITY_FOR_SNAPSHOT = "ENTITY_FOR_SNAPSHOT";
    
    public <T extends BaseEntity> MakerCheckerPreSnapshoptEvent(T entity) {
        addContextProperty(ENTITY_CLASS, entity.getClass());
        addContextProperty(ENTITY_FOR_SNAPSHOT, entity);
    }
}
