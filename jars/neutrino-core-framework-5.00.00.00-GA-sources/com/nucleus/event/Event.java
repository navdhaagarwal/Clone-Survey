package com.nucleus.event;

import java.util.Map;
import java.util.Set;

import org.joda.time.DateTime;

import com.nucleus.entity.BaseEntity;
import com.nucleus.entity.Entity;
import com.nucleus.entity.EntityId;

/**
 * Interface to denote all events in the framework
 */
public interface Event extends Entity {

    int getEventType();

    <T extends BaseEntity> void setStandardEventPropertiesUsingEntity(T entity);

    <T extends BaseEntity> void setStandardEventPropertiesUsingEntity(EntityId entityId);

    EntityId getOwnerEntityId();

    DateTime getEventTimestamp();

    String getPersistentProperty(String propertyKey);

    Set<String> getPersistentPropertyKeys();

    void addPersistentProperty(String propertyName, String propertyValue);

    Object getContextProperty(String propertyKey);

    Set<String> getContextKeys();

    void addContextProperty(String propertyName, Object propertyValue);

    Map<String, String> getPersistentPropertyMap();

    void setEventType(String eventType);

    boolean isNotificationMandatory();

    String getAssociatedUserUri();

    Set<String> getNonWatchersToNotify();

    void setMobileNumbersToSms(String... numberToSms);

    Set<String> getMobileNumbersToSms();

}
