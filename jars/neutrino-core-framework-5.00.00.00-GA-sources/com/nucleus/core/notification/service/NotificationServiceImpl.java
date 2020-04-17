package com.nucleus.core.notification.service;

import static com.nucleus.event.GenericEvent.SUCCESS_FLAG;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.Query;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;

import com.nucleus.config.persisted.service.ConfigurationService;
import com.nucleus.core.notification.Notification;
import com.nucleus.core.validation.util.NeutrinoValidator;
import com.nucleus.dao.query.NamedQueryExecutor;
import com.nucleus.entity.EntityId;
import com.nucleus.event.Event;
import com.nucleus.event.EventService;
import com.nucleus.event.FormatType;
import com.nucleus.event.GenericEvent;
import com.nucleus.logging.BaseLoggers;
import com.nucleus.service.BaseServiceImpl;

@Named("notificationService")
public class NotificationServiceImpl extends BaseServiceImpl implements NotificationService {

	protected static final String MAX_PERSISTED_UNSEEN_NOTIFICATIONS = "config.notification.maxPersistedUnseenNotifications";
	protected static final String NOTIFY_EVENT="notifyEvents";
	protected static final String GET_NOTIFICATION_EVENT_QUERY="Configuration.getPropertyValueFromPropertyKey";
	protected static final String COMMA=","; 
	protected static final String QUOTE="'"; 
    protected static final String GET_DTYPE_FROM_GENERIC_EVENT = "SELECT DISTINCT(EVENT_TYPE) FROM GENERIC_EVENT WHERE DTYPE IN  (";

	@Inject
	@Named("eventService")
	private EventService eventService;

	@Inject
	@Named("configurationService")
	private ConfigurationService configurationService;
	
	@Value(value = "#{'${env.notification.key}'}")
    private String                     envNotificationKey;

	@Override
	public void createNotificationEntries(List<Notification> notifications) {
		NeutrinoValidator.notNull(notifications, "Notifications can not be null");
		for (Notification notification : notifications) {
			BaseLoggers.eventLogger.debug("Notification of event type {} saved/updated into database",
					notification.getEvent().getEventType());
			entityDao.saveOrUpdate(notification);
		}
	}

	/**
	 * Creates notifications using base event.
	 *
	 * @return the notification
	 */
	@Override
	public List<Notification> createNotificationsUsingGenericEventForUsers(GenericEvent genericEvent,
			Set<String> userUris) {
		List<Notification> notifications = new ArrayList<Notification>();
		NeutrinoValidator.notNull(userUris, "Notification user set can not be null");

		for (String userUri : userUris) {

			/*List<Notification> notificationAlreadyExistingForUser = getAllNotificationsByUser(userUri, null);*/

			// get value for maximum unseen notifications allowed for a user
			/*Integer maxPersistedUnseenNotificationCount = new Integer(configurationService
					.getConfigurationPropertyFor(EntityId.fromUri(userUri), MAX_PERSISTED_UNSEEN_NOTIFICATIONS)
					.getPropertyValue());*/

			// allows persisting notification for a user if condition gets
			// satisfied
			/*if (notificationAlreadyExistingForUser.size() < maxPersistedUnseenNotificationCount) {*/
				Notification notification = new Notification();
				notification.setGenericEvent(genericEvent);
				notification.setEventType(genericEvent.getEventType());
				notification.setNotificationUserUri(userUri);
				notification.setNotificationType(genericEvent.getPersistentProperty(SUCCESS_FLAG));
				notification.setSeen(false);
				notifications.add(notification);
			/*}*/
		}

		return notifications;
	}

	@Override
	public List<Map<String, Object>> getLocalizedUserNotifications(String userUri, Locale locale,
			Integer notificationsToShow) {
		NeutrinoValidator.notNull(userUri, "User Uri can not be null");
		List<Notification> notifications = getAllNotificationsByUser(userUri, notificationsToShow);
		List<Map<String, Object>> localizedNotificationList = new ArrayList<Map<String, Object>>();
		for (Notification notification : notifications) {
			Map<String, Object> map = new HashMap<String, Object>();
			Event event = notification.getEvent();
			// Update seen flag for fetched notifications
			notification.setSeen(true);
			if(notification.getNotificationType().equalsIgnoreCase("warning") && notification.getEvent()!=null){
				map.put("message", notification.getEvent().getPersistentProperty("WARNING_NOTIFICATION"));
			} else{
			map.put("message", eventService.getEventTypeStringRepresentation(event, locale, FormatType.NOTIFICATION));
			}
			map.put("notificationType", notification.getNotificationType());
			localizedNotificationList.add(map);
		}
		return localizedNotificationList;
	}

	@Override
	public List<Map<String, Object>> getLastLocalizedUserNotifications(String userUri, Locale locale, Integer number) {
		NeutrinoValidator.notNull(userUri, "User Uri can not be null");
		List<Notification> notifications = (number == null) ? getAllNotificationsByUser(userUri, number)
				: getLastNotifications(userUri, number);
		List<Map<String, Object>> localizedLastNotificationList = new ArrayList<Map<String, Object>>();
		for (Notification notification : notifications) {
			Map<String, Object> map = new HashMap<String, Object>();
			Event event = notification.getEvent();
			map.put("message", eventService.getEventTypeStringRepresentation(event, locale, FormatType.NOTIFICATION));
			map.put("notificationType", notification.getNotificationType());
			localizedLastNotificationList.add(map);
		}
		return localizedLastNotificationList;
	}

	@Override
    public List<Notification> getAllNotificationsByUser(String userUri, Integer notificationsToShow) {
    	
	 NamedQueryExecutor<Notification> notificationExecutor = new NamedQueryExecutor<Notification>(
                  "Generic.getUnseenNotificationFromUserUri").addParameter("userUri", userUri).addParameter("notifyEvents",getApplicableEvents());
    	 return (notificationsToShow == null)?entityDao.executeQuery(notificationExecutor):
        		entityDao.executeQuery(notificationExecutor, 0, notificationsToShow);
    }

	@Override
	public Long getUnseenNotificationCountByUser(String userUri) {
		NamedQueryExecutor<Long> notificationExecutor = new NamedQueryExecutor<Long>(
				"Generic.getUnseenNotificationCountFromUserUri").addParameter("userUri", userUri);
		return entityDao.executeQueryForSingleValue(notificationExecutor);

	}
	
	@Override
	public void updateUnseenNotificationByUserByCreationTimestamp(String userUri, List<Long> notificationIds, List<Integer> applicableEvents) {
		if (CollectionUtils.isNotEmpty(notificationIds)) {
			this.entityDao.getEntityManager().createNamedQuery("Generic.updateUnseenNotificationFromUserUriByCreationTimestamp")
					.setParameter("userUri", userUri)
					.setParameter("newNotificationIds", notificationIds)
					.setParameter("notifyEvents", applicableEvents).executeUpdate();
		}
	}

	@Override
	public List<Long> getNewNotificationByUserByCreationTimestamp(String userUri, Long maxUnseenNotifications, List<Integer> applicableEvents) {
		NamedQueryExecutor<Long> executor= new NamedQueryExecutor<Long>("Generic.getNewNotificationFromUserUriByCreationTimestamp")
				.addParameter("userUri", userUri)
                .addParameter("notifyEvents", applicableEvents);
		return this.entityDao.executeQuery(executor, 0, Integer.parseInt(maxUnseenNotifications.toString()));
	}

	@Override
	public List<Notification> getLastNotifications(String userUri, int number) {
		NamedQueryExecutor<Notification> notificationExecutor = new NamedQueryExecutor<Notification>(
				"Generic.getLastNotificationFromUserUri").addParameter("userUri", userUri);
		return entityDao.executeQuery(notificationExecutor, 0, number);

	}
	
	@Override
	public List<Integer> getApplicableEvents(){
        if (envNotificationKey == null || "DEFAULT".equals(envNotificationKey)) {
            envNotificationKey = NOTIFY_EVENT;
        }
        
        String configurationValue=configurationService.getPropertyValueByPropertyKey(envNotificationKey, GET_NOTIFICATION_EVENT_QUERY);
        
        List<Integer> eventTypeList = new ArrayList<Integer>();
        if (StringUtils.isNotBlank(configurationValue)) {
             String[] eventTypes = configurationValue.split(COMMA);
             for (String eventType : eventTypes) {
            	 eventTypeList.add(Integer.parseInt(String.valueOf(eventType)));
             }            
         }
        if (eventTypeList.isEmpty()) {
            eventTypeList.add(0); 
        }        
         return eventTypeList;
    }

	@Override
	public List<Integer> getUserEvents(String eventType){
		if (envNotificationKey == null || "DEFAULT".equals(envNotificationKey)) {
			envNotificationKey = NOTIFY_EVENT;
		}
		String configurationValue=configurationService.getPropertyValueByPropertyKey(envNotificationKey, GET_NOTIFICATION_EVENT_QUERY);
		List<Integer> eventTypeList = new ArrayList<Integer>();
		if (StringUtils.isNotBlank(configurationValue)) {
			String[] eventTypes = configurationValue.split(COMMA);
			if(StringUtils.isNotEmpty(eventType) && eventType.equalsIgnoreCase("mine")){
				eventTypeList = Arrays.stream(eventTypes).filter(e->(!e.equalsIgnoreCase("19") &&
						!e.equalsIgnoreCase("20") && !e.equalsIgnoreCase("21"))).map(e->Integer.parseInt(e)).collect(Collectors.toList());
			}else{
				eventTypeList = Arrays.stream(eventTypes).filter(e->(e.equalsIgnoreCase("19") ||
						e.equalsIgnoreCase("20") || e.equalsIgnoreCase("21"))).map(e->Integer.parseInt(e)).collect(Collectors.toList());
			}
		}
		if (eventTypeList.isEmpty()) {
			eventTypeList.add(0);
		}
		return eventTypeList;
	}
}
