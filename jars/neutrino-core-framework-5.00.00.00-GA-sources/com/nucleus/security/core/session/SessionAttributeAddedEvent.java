package com.nucleus.security.core.session;

public class SessionAttributeAddedEvent extends AbstractSessionAttributeEvent {

    private String name;

    private Object value;

    public SessionAttributeAddedEvent(Object source, NeutrinoMapSession session, String name, Object value) {
        super(source, session);
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public Object getValue() {
        return value;
    }
}
