package com.nucleus.security.core.session;

import org.springframework.session.Session;

import java.io.Serializable;
import java.time.Duration;
import java.time.Instant;

public final class NeutrinoMapSessionVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private String id;
    private String originalId;
    private Instant creationTime;
    private Instant lastAccessedTime;
    private Duration maxInactiveInterval;
    private String serverNodeId;

    public NeutrinoMapSessionVO(NeutrinoMapSession session, String serverNodeId) {
        super();
        this.id = session.getId();
        this.originalId = session.getOriginalId();
        this.creationTime = session.getCreationTime();
        this.lastAccessedTime = session.getLastAccessedTime();
        this.maxInactiveInterval = session.getMaxInactiveInterval();
        this.serverNodeId = serverNodeId;
    }


    public Instant getCreationTime() {
        return this.creationTime;
    }

    public String getId() {
        return this.id;
    }

    public String getOriginalId() {
        return this.originalId;
    }

    public Instant getLastAccessedTime() {
        return this.lastAccessedTime;
    }

    public Duration getMaxInactiveInterval() {
        return this.maxInactiveInterval;
    }

    public String getServerNodeId() {
        return this.serverNodeId;
    }

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
    public boolean equals(Object obj) {
        return obj instanceof NeutrinoMapSessionVO && this.id.equals(((Session) obj).getId());
    }

    @Override
    public int hashCode() {
        return this.id.hashCode();
    }


}
