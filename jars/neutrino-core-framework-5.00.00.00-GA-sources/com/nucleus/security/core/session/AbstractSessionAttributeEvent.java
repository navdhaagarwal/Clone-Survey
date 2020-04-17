package com.nucleus.security.core.session;

public abstract class AbstractSessionAttributeEvent extends AbstractSessionEvent {
    public AbstractSessionAttributeEvent(Object source, NeutrinoMapSession session) {
        super(source, session);
    }
}
