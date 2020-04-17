/**
 * This file and a proportion of its content is copyright of Nucleus Software Exports Limited - � 2012. All rights reserved.
 * Any redistribution or reproduction of part or all of the contents in any form is prohibited other than the following:
 * - you cannot print or download to a local hard disk extract contents either part or full for personal/ commercial/
 * academic or any other use
 * - you may not copy the content to individual/ third parties for any type of use, either as compiled or source format
 * without the knowledge and consent of Nucleus SOftware
 * - You may not, except with our express written permission, distribute or commercially exploit the content. Nor may you
 * transmit it or store it in any other web site or other form of electronic retrieval system.
 */
package com.nucleus.core.notification;

import static com.nucleus.finnone.pro.general.util.ValidatorUtils.notNull;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Index;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.nucleus.entity.BaseEntity;
import com.nucleus.event.Event;
import com.nucleus.event.GenericEvent;

/**
 * The Class Notification.
 *
 * @author Nucleus Software Exports Limited
 */
@Entity
@DynamicUpdate
@DynamicInsert
@Table(indexes={@Index(name="notificationUserUri_index",columnList="notificationUserUri"), @Index(name="NOTIF_IDX1",columnList="eventType, seen, notificationUserUri")})
public class Notification extends BaseEntity {

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = -17357698607146244L;

    /** The persisted event. */
    @ManyToOne(fetch = FetchType.LAZY)
    private GenericEvent      event;

    private int      eventType;
    
    /** The notification user uri. */
    private String            notificationUserUri;

    /** The seen. */
    // @Index(name = "seen_index")
    private Boolean           seen;
   
    private Boolean           markedSeenBySystem=Boolean.FALSE;

    private String            notificationType;

    /**
     * Gets the persisted event.
     *
     * @return the persistedEvent
     */
    public Event getEvent() {
        return event;
    }

    /**
     * Sets the persisted event.
     *
     * @param persistedEvent the persistedEvent to set
     */
    public void setGenericEvent(GenericEvent genericEvent) {
        this.event = genericEvent;
    }

    /**
     * Gets the notification user uri.
     *
     * @return the notificationUserUri
     */
    public String getNotificationUserUri() {
        return notificationUserUri;
    }

    /**
     * Sets the notification user uri.
     *
     * @param notificationUserUri the notificationUserUri to set
     */
    public void setNotificationUserUri(String notificationUserUri) {
        this.notificationUserUri = notificationUserUri;
    }

    /**
     * Gets the seen flag.
     *
     * @return the seen
     */
    public Boolean getSeen() {
        return seen;
    }

    /**
     * Sets the seen flag.
     *
     * @param seen the seen to set
     */
    public void setSeen(Boolean seen) {
        this.seen = seen;
    }

    public Boolean getMarkedSeenBySystem() {
        return markedSeenBySystem;
    }

    public void setMarkedSeenBySystem(Boolean markedSeenBySystem) {
        this.markedSeenBySystem=notNull(markedSeenBySystem)?markedSeenBySystem:Boolean.FALSE;
        
    }

    public String getNotificationType() {
        return notificationType;
    }

    public void setNotificationType(String notificationType) {
        this.notificationType = notificationType;
    }

	public int getEventType() {
		return eventType;
	}

	public void setEventType(int eventType) {
		this.eventType = eventType;
	}

}
