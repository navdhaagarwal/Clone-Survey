package com.nucleus.security.core.session;

import org.springframework.session.Session;

import java.io.Serializable;

public final class SessionAttributeTopicVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private String id;
    private String serverNodeId;
    private SessionAttributeActionEnum attributeAction;
    private String attributeKey;
    private Object attributeValue;

    public SessionAttributeTopicVO(String sessionId, String serverNodeId, SessionAttributeActionEnum attributeAction, String attributeKey, Object attributeValue) {
        super();
        this.id = sessionId;
        this.serverNodeId = serverNodeId;
        this.attributeAction = attributeAction;
        this.attributeKey = attributeKey;
        this.attributeValue = attributeValue;
    }

    public String getId() {
        return id;
    }

    public String getServerNodeId() {
        return serverNodeId;
    }

    public SessionAttributeActionEnum getAttributeAction() {
        return attributeAction;
    }

    public String getAttributeKey() {
        return attributeKey;
    }

    public void setAttributeKey(String attributeKey) {
        this.attributeKey = attributeKey;
    }

    public Object getAttributeValue() {
        return attributeValue;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof SessionAttributeTopicVO && this.id.equals(((Session) obj).getId());
    }

    @Override
    public int hashCode() {
        return this.id.hashCode();
    }


}
