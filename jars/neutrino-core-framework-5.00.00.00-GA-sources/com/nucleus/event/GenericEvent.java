package com.nucleus.event;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Index;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.joda.time.DateTime;
import org.springframework.util.StringUtils;
import com.nucleus.html.util.HtmlUtils;
import com.nucleus.core.annotations.Synonym;
import com.nucleus.core.validation.util.NeutrinoValidator;
import com.nucleus.entity.BaseEntity;
import com.nucleus.entity.EntityId;
import com.nucleus.finnone.pro.lmsbase.utility.GenericUtilityConstants;
import com.nucleus.metadata.Metadata;

/**
 * Abstract class for all events in the framework.
 * The API of event framework supports fluent api upto one level.
 * e.g. You can write something like following:
 * new UserCommentEvent(UserCommentEvent.COMMENT_DELETED).setOwnerEntityUri(comment.getOwnerEntityUri())
 */
@Entity
@DynamicUpdate
@DynamicInsert
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@Table(indexes={@Index(name="event_Type_index",columnList="eventType"), @Index(name="associatedUserUri_index",columnList="associatedUserUri")})
@Synonym(grant = "ALL")
public class GenericEvent extends BaseEntity implements Event, Serializable {

    private static final long     serialVersionUID                        = -5128197546627517788L;

    @Transient
    protected Map<String, Object> eventContext                            = new LinkedHashMap<String, Object>();

    // A map key to hold user uris for users who are non watchers but notification should be sent to them.
    // use getContextProperty(NON_WATCHER_NOTIFICATION_USERS_URI_LIST) to get that list
    private static final String   NON_WATCHER_NOTIFICATION_USERS_URI_LIST = "nonWatcherUsersUrisToNotify";

    public static final String    EVENT_TYPE                              = "EVENT_TYPE";
    public static final String    ENTITY_NAME                             = "ENTITY_NAME";
    public static final String    ENTITY_DISPLAY_NAME                     = "ENTITY_DISPLAY_NAME";
    public static final String    SUCCESS_FLAG                            = "SUCCESS_FLAG";
    public static final String    MOBILE_NUMBERS_TO_SMS_AS_CSV            = "MOBILE_NUMBERS_TO_SMS_AS_CSV";

    private int                   eventType;
    private String                associatedUserUri;

    // field if true will override user preferences to send mail,notifications etc.
    private boolean               notificationMandatory;

    @ManyToOne(cascade = { CascadeType.PERSIST })
    private final Metadata        eventMetadata                           = new Metadata();

    public Metadata getEventMetadata() {
        return eventMetadata;
    }

    /**
     * Instantiates a new generic event.
     */
    protected GenericEvent() {
    }

    /**
     * Instantiates a new generic event.
     *
     * @param eventType the event type
     */
    public GenericEvent(int eventType) {
        NeutrinoValidator.isTrue(eventType > 0, "Unknown event type: ", eventType);
        this.eventType = eventType;
        addContextProperty(EVENT_TYPE, eventType);
    }

    public GenericEvent(GenericEvent aGenericEvent) {
        NeutrinoValidator.notNull(aGenericEvent, "GenericEvent cannot be null");
        this.eventType = aGenericEvent.getEventType();
        setAssociatedUserUri(aGenericEvent.getAssociatedUserUri());
        if (aGenericEvent.getEventMetadata() != null && aGenericEvent.getEventMetadata().getKeys() != null) {
            for (String propertyKey : aGenericEvent.getEventMetadata().getKeys()) {
                eventMetadata.createOrUpdate(propertyKey, aGenericEvent.getEventMetadata().getValue(propertyKey));
            }
        }

    }

    @Override
    public <T extends BaseEntity> void setStandardEventPropertiesUsingEntity(T entity) {
        setEntityName(entity.getClass().getSimpleName());
        setOwnerEntityId(entity.getEntityId());
        if (entity.getDisplayName() != null) {
            setEntityDisplayName(entity.getDisplayName());
        } else {
            setEntityDisplayName("");
        }
    }

    @Override
    public <T extends BaseEntity> void setStandardEventPropertiesUsingEntity(EntityId entityId) {
        setEntityName(entityId.getClass().getSimpleName());
        setOwnerEntityId(entityId);
        if (entityId.getUri() != null) {
            setEntityDisplayName(entityId.getUri());
        } else {
            setEntityDisplayName("");
        }
    }

    /**
     * Sets the entity name.
     *
     * @param entityName the new entity name
     */
    public void setEntityName(String entityName) {
        addPersistentProperty(ENTITY_NAME, entityName);
    }

    /**
     * Gets the entity name.
     *
     * @return the entity name
     */
    public String getEntityName() {
        return getPersistentProperty(ENTITY_NAME);
    }

    /**
     * Sets the entity display name.
     *
     * @param entityDisplayName the new entity display name
     */
    public void setEntityDisplayName(String entityDisplayName) {
        addPersistentProperty(ENTITY_DISPLAY_NAME, entityDisplayName);
    }

    /**
     * Gets the entity display name.
     *
     * @return the entity display name
     */
    public String getEntityDisplayName() {
        return getPersistentProperty(ENTITY_DISPLAY_NAME);
    }

    @Override
    public void addContextProperty(String propertyName, Object propertyValue) {
        eventContext.put(propertyName, propertyValue);
    }

    @Override
    public Set<String> getContextKeys() {
        return eventContext.keySet();
    }

    @Override
    public Object getContextProperty(String propertyKey) {
        return eventContext.get(propertyKey);
    }

    @Override
    public void addPersistentProperty(String propertyName, String propertyValue) {
        eventMetadata.createOrUpdate(propertyName, propertyValue);
    }

    @Override
    public Set<String> getPersistentPropertyKeys() {
        return eventMetadata.getKeys();
    }

    @Override
    public String getPersistentProperty(String propertyKey) {
        return eventMetadata.getValue(propertyKey);
    }

    @Override
    public Map<String, String> getPersistentPropertyMap() {
        Map<String, String> persistentPropertyMap = new HashMap<String, String>();
        Set<String> propertyKeys = getPersistentPropertyKeys();
        for (String propertyKey : propertyKeys) {
            persistentPropertyMap.put(propertyKey, HtmlUtils.htmlEscape(getPersistentProperty(propertyKey),GenericUtilityConstants.CHARSET_UTF8));
        }
        return persistentPropertyMap;
    }

    @Override
    public EntityId getOwnerEntityId() {
        return eventMetadata.getOwnerEntityId();
    }

    /**
     * Sets the owner entity id.
     *
     * @param ownerEntityId the new owner entity id
     */
    public void setOwnerEntityId(EntityId ownerEntityId) {
        NeutrinoValidator.isTrue(ownerEntityId.getLocalId() != null,
                "Please save the entity first before passing it to fire event");
        eventMetadata.setOwnerEntityId(ownerEntityId);
    }

    /**
     * Gets the event timestamp.
     *
     * @return the event timestamp
     */
    @Override
    public DateTime getEventTimestamp() {
        return getEntityLifeCycleData().getCreationTimeStamp();
    }

    @Override
    public int getEventType() {
        return eventType;
    }

    @Override
    public void setEventType(String eventType) {
        this.eventType = Integer.valueOf(eventType);
        addContextProperty(EVENT_TYPE, eventType);
    }

    /**
     * @return the notificationMandatory
     */
    @Override
    public boolean isNotificationMandatory() {
        return notificationMandatory;
    }

    /**
     * @param notificationMandatory the notificationMandatory to set
     */
    public void setNotificationMandatory(boolean notificationMandatory) {
        this.notificationMandatory = notificationMandatory;
    }

    /**
     * @return the associatedUserUri
     */
    @Override
    public String getAssociatedUserUri() {
        return associatedUserUri;
    }

    /**
     * @param associatedUserUri the associatedUserUri to set
     */
    public void setAssociatedUserUri(String associatedUserUri) {
        this.associatedUserUri = associatedUserUri;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Set<String> getNonWatchersToNotify() {
        if (eventContext.containsKey(NON_WATCHER_NOTIFICATION_USERS_URI_LIST))
            return (Set<String>) eventContext.get(NON_WATCHER_NOTIFICATION_USERS_URI_LIST);
        else
            return Collections.emptySet();
    }

    @SuppressWarnings("unchecked")
    public void addNonWatchersToNotify(Set<String> nonWatchersToNotify) {

        if (eventContext.containsKey(NON_WATCHER_NOTIFICATION_USERS_URI_LIST)) {
            ((Set<String>) eventContext.get(NON_WATCHER_NOTIFICATION_USERS_URI_LIST)).addAll(nonWatchersToNotify);

        } else {
            eventContext.put(NON_WATCHER_NOTIFICATION_USERS_URI_LIST, nonWatchersToNotify);
        }
    }

    @SuppressWarnings("unchecked")
    public void addNonWatcherToNotify(String nonWatcherToNotify) {

        if (eventContext.containsKey(NON_WATCHER_NOTIFICATION_USERS_URI_LIST)) {
            ((Set<String>) eventContext.get(NON_WATCHER_NOTIFICATION_USERS_URI_LIST)).add(nonWatcherToNotify);

        } else {
            Set<String> nonWatchersToNotify = new HashSet<String>();
            nonWatchersToNotify.add(nonWatcherToNotify);
            eventContext.put(NON_WATCHER_NOTIFICATION_USERS_URI_LIST, nonWatchersToNotify);
        }
    }

    @Override
    public void setMobileNumbersToSms(String... numberToSms) {
        addPersistentProperty(MOBILE_NUMBERS_TO_SMS_AS_CSV, StringUtils.arrayToCommaDelimitedString(numberToSms));
    }

    @Override
    public Set<String> getMobileNumbersToSms() {

        String mobileNumbersToSmsAsCsv = getPersistentProperty(MOBILE_NUMBERS_TO_SMS_AS_CSV);
        return StringUtils.commaDelimitedListToSet(mobileNumbersToSmsAsCsv);
    }

}