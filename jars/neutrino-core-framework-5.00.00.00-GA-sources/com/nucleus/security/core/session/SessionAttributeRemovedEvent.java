package com.nucleus.security.core.session;

public class SessionAttributeRemovedEvent extends AbstractSessionAttributeEvent {

    private String name;

    private Object oldValue;

    public SessionAttributeRemovedEvent(Object source, NeutrinoMapSession session, String name, Object oldValue) {
        super(source, session);
        this.name = name;
        this.oldValue = oldValue;
    }

    public String getName() {
        return name;
    }

    public Object getOldValue() {
        return oldValue;
    }
}
