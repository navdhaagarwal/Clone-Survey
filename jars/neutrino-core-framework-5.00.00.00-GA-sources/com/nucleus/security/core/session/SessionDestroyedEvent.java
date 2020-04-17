package com.nucleus.security.core.session;

public class SessionDestroyedEvent extends AbstractSessionEvent {

    public SessionDestroyedEvent(Object source, NeutrinoMapSession session) {
        super(source, session);
    }

}
