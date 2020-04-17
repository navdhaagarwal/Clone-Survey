package com.nucleus.security.core.session;

public class SessionAttributeReplacedEvent extends AbstractSessionAttributeEvent {

    private String name;

    private Object value;

    private Object oldValue;

    private boolean ignoreEqualsCheck;

    public SessionAttributeReplacedEvent(Object source, NeutrinoMapSession session, String name, Object value, Object oldValue, Boolean ignoreEqualsCheck) {
        super(source, session);
        this.name = name;
        this.value = value;
        this.oldValue = oldValue;
        this.ignoreEqualsCheck = ignoreEqualsCheck;
    }

    public String getName() {
        return name;
    }

    public Object getValue() {
        return value;
    }

    public Object getOldValue() {
        return oldValue;
    }

    public boolean isIgnoreEqualsCheck() {
        return ignoreEqualsCheck;
    }
}
