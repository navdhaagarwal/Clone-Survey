package com.nucleus.security.core.session;


public class SessionCreatedEvent extends AbstractSessionEvent {

    public SessionCreatedEvent(Object source, NeutrinoMapSession session) {
        super(source, session);
    }

}
