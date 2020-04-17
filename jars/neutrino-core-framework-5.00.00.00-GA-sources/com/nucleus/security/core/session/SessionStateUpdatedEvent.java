package com.nucleus.security.core.session;


public class SessionStateUpdatedEvent extends AbstractSessionEvent {

    public SessionStateUpdatedEvent(Object source, NeutrinoMapSession session) {
        super(source, session);
    }

}
