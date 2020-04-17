/**
 * Copyright Nucleus Software India Pvt Ltd. All rights reserved.
 *//*
package com.nucleus.event;

import java.util.List;

import com.nucleus.core.notification.Notification;

*//**
 * Service for PersistedEvent business operations.
 * @author Nucleus Software India Pvt Ltd
 *//*
public interface PersistedEventService {

    *//**
     * Saves the {@link PersistedEvent} object into database.
     *//*
    public void createEventEntry(PersistedEvent persistedEvent);

    *//**
     * Gets all {@link PersistedEvent} object from database for passed event type.
     *//*
    public List<PersistedEvent> getAllEventsOfType(int eventType);

	void createNotificationEntries(List<Notification> notifications);

}
*/