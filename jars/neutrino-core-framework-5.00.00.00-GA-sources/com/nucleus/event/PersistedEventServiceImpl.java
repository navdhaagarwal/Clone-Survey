/**
 * Copyright Nucleus Software India Pvt Ltd. All rights reserved.
 *//*
package com.nucleus.event;

import java.util.List;

import javax.inject.Named;

import org.springframework.beans.factory.annotation.Autowired;

import com.nucleus.core.notification.Notification;
import com.nucleus.logging.BaseLoggers;
import com.nucleus.service.BaseServiceImpl;

@Named("persistedEventService")
public class PersistedEventServiceImpl extends BaseServiceImpl implements PersistedEventService {

    @Autowired
    private EventDao eventDao;

    public void createEventEntry(PersistedEvent persistedEvent) {
        BaseLoggers.eventLogger.debug("Persisting event of type {} into database", persistedEvent.getEventType());
        entityDao.persist(persistedEvent);
    }

    @Override
    public List<PersistedEvent> getAllEventsOfType(int eventType) {
        return eventDao.getAllEventsOfType(eventType);
    }
    
    @Override
	public void createNotificationEntries(List<Notification> notifications) {
    	for (Notification notification : notifications){
    		if (notification.getId() == null) {
    			BaseLoggers.eventLogger.debug("Notification of event type {} saved into database", notification.getPersistedEvent().getEventType());
    			entityDao.persist(notification);
    		} else {
    			BaseLoggers.eventLogger.debug("Notification of event type {} updated into database", notification.getPersistedEvent().getEventType());
    			entityDao.update(notification);
    		}	
    	}
    }

}
*/