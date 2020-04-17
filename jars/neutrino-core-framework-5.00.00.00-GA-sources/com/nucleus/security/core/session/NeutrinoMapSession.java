package com.nucleus.security.core.session;

import com.nucleus.logging.BaseLoggers;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.session.Session;

import java.io.Serializable;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public final class NeutrinoMapSession implements Session, Serializable {

    private static final long serialVersionUID = 1L;

    public static final int DEFAULT_MAX_INACTIVE_INTERVAL_SECONDS = 1800;
    public static final String FROM_REMOTE_STORE_ATTR = "FROM_REMOTE_STORE";

    private static ApplicationEventPublisher applicationEventPublisher;

    private String id;
    private String originalId;
    private Map<String, Object> sessionAttributes = new HashMap<>();
    private Instant creationTime = Instant.now();
    private Instant lastAccessedTime = this.creationTime;
//    private boolean fromRemoteStore;

    private Duration maxInactiveInterval = Duration.ofSeconds(DEFAULT_MAX_INACTIVE_INTERVAL_SECONDS);

    public static void initialize(ApplicationEventPublisher applicationEventPublisher) {
        if (NeutrinoMapSession.applicationEventPublisher == null) {
            NeutrinoMapSession.applicationEventPublisher = applicationEventPublisher;
        }
    }

    public NeutrinoMapSession() {
        this(generateId());
        this.setFromRemoteStore(false);
    }

    /**
     * Creates a new instance with the specified id. This is preferred to the
     * default constructor when the id is known to prevent unnecessary consumption
     * on entropy which can be slow.
     *
     * @param id the identifier to use
     */
    public NeutrinoMapSession(String id) {
        this.id = id;
        this.originalId = id;
    }

    public boolean isFromRemoteStore() {
        //This is temporary for till code clean up for session attribute usage across modules is done
        //Actually it should be 'return fromRemoteStore;'
        return (boolean) sessionAttributes.get(FROM_REMOTE_STORE_ATTR);
    }

    void setFromRemoteStore(boolean fromRemoteStore) {
        //This is temporary for till code clean up for session attribute usage across modules is done
        //Actually it should be 'this.fromRemoteStore = fromRemoteStore;'
        sessionAttributes.put(FROM_REMOTE_STORE_ATTR, fromRemoteStore);
    }

    @Override
    public void setLastAccessedTime(Instant lastAccessedTime) {
        this.lastAccessedTime = lastAccessedTime;
    }

    @Override
    public Instant getCreationTime() {
        return this.creationTime;
    }

    @Override
    public String getId() {
        return this.id;
    }

    public String getOriginalId() {
        return this.originalId;
    }

    void setOriginalId(String originalId) {
        this.originalId = originalId;
    }

    @Override
    public String changeSessionId() {
        String changedId = generateId();
        setId(changedId);
        return changedId;
    }

    @Override
    public Instant getLastAccessedTime() {
        return this.lastAccessedTime;
    }

    @Override
    public void setMaxInactiveInterval(Duration interval) {
        this.maxInactiveInterval = interval;
    }

    @Override
    public Duration getMaxInactiveInterval() {
        return this.maxInactiveInterval;
    }

    @Override
    public boolean isExpired() {
        return isExpired(Instant.now());
    }

    boolean isExpired(Instant now) {
        if (this.maxInactiveInterval.isNegative()) {
            return false;
        }
        return now.minus(this.maxInactiveInterval).compareTo(this.lastAccessedTime) >= 0;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getAttribute(String attributeName) {
        return (T) this.sessionAttributes.get(attributeName);
    }

    @Override
    public Set<String> getAttributeNames() {
        return this.sessionAttributes.keySet();
    }

    @Override
    public void setAttribute(String attributeName, Object attributeValue) {
        this.setAttribute(attributeName,attributeValue,false);
    }

    protected void setAttributesFromPubSub(Map<String, Object> map) {
        this.sessionAttributes.putAll(map);
    }

    protected void setAttribute(String attributeName, Object attributeValue, Boolean ignoreEqualsCheck) {
        if (attributeValue == null) {
            removeAttribute(attributeName);
        } else {
            Object oldValue = this.sessionAttributes.put(attributeName, attributeValue);
            if (oldValue == null) {
                publishEvent(new SessionAttributeAddedEvent(this, this, attributeName, attributeValue));
            } else {
                publishEvent(new SessionAttributeReplacedEvent(this, this, attributeName, attributeValue, oldValue, ignoreEqualsCheck));
            }
        }
    }

    @Override
    public void removeAttribute(String attributeName) {
        Object oldValue = this.sessionAttributes.remove(attributeName);
        publishEvent(new SessionAttributeRemovedEvent(this, this, attributeName, oldValue));
    }

    protected void removeAttributeFromPubSub(String attributeName) {
       this.sessionAttributes.remove(attributeName);
    }

    /**
     * Sets the time that this {@link Session} was created. The default is when the
     * {@link Session} was instantiated.
     *
     * @param creationTime the time that this {@link Session} was created.
     */
    public void setCreationTime(Instant creationTime) {
        this.creationTime = creationTime;
    }

    /**
     * Sets the identifier for this {@link Session}. The id should be a secure
     * random generated value to prevent malicious users from guessing this value.
     * The default is a secure random generated identifier.
     *
     * @param id the identifier for this session.
     */
    public void setId(String id) {
        this.id = id;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Session && this.id.equals(((Session) obj).getId());
    }

    @Override
    public int hashCode() {
        return this.id.hashCode();
    }

    private static String generateId() {
        return UUID.randomUUID().toString();
    }

    private void publishEvent(AbstractSessionAttributeEvent event) {
        try {
            applicationEventPublisher.publishEvent(event);
        } catch (Exception e) {
            BaseLoggers.flowLogger.error(e.getMessage(), e);
        }
    }

}
