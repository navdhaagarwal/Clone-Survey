package com.nucleus.security.core.session;

import com.nucleus.security.core.session.NeutrinoMapSession;
import org.springframework.context.ApplicationEvent;

public abstract class AbstractSessionEvent extends ApplicationEvent {

    private final String sessionId;

    private final NeutrinoMapSession session;

    AbstractSessionEvent(Object source, NeutrinoMapSession session) {
        super(source);
        this.session = session;
        this.sessionId = session.getId();
    }


    public NeutrinoMapSession getSession() {
        return this.session;
    }

    public String getSessionId() {
        return this.sessionId;
    }

}
