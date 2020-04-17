package com.nucleussoft.reactive.dummy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;

import com.nucleus.core.notification.Notification;
import com.nucleus.core.notification.service.NotificationServiceImpl;
import com.nucleus.core.validation.util.NeutrinoValidator;
import com.nucleus.dao.query.NamedQueryExecutor;
import com.nucleus.event.Event;
import com.nucleus.event.EventService;
import com.nucleus.event.FormatType;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Named("reactiveNotificationService")
public class ReactiveNotificationServiceImpl extends NotificationServiceImpl{
	
	@Inject
	@Named("reactEntityDao")
	private ReactiveEntityDao entityDao;
	
	@Inject
	@Named("eventService")
	private EventService eventService;

	
	
	public Flux<Map<String, Object>> getLocalizedUserNotificationsReact(String userUri, Locale locale,
			Integer notificationsToShow) {
		NeutrinoValidator.notNull(userUri, "User Uri can not be null");
		Flux<Notification> notifications = getAllNotificationsByUserReact(userUri, notificationsToShow);
		return notifications.map((notification)->{
			Map<String, Object> map = new HashMap<>();
			Event event = notification.getEvent();
			//notification.setSeen(true);
			if(notification.getNotificationType().equalsIgnoreCase("warning") && notification.getEvent()!=null){
				map.put("message", notification.getEvent().getPersistentProperty("WARNING_NOTIFICATION"));
			} else{
			map.put("message", eventService.getEventTypeStringRepresentation(event, locale, FormatType.NOTIFICATION));
			}
			map.put("notificationType", notification.getNotificationType());
			return map;
		});
	}
	
	
	
	
	public Mono<List<Map<String, Object>>> getLocalizedUserNotificationsReactAsync(String userUri, Locale locale,
			Integer notificationsToShow) {
		NeutrinoValidator.notNull(userUri, "User Uri can not be null");
		Mono<List<Notification>> notifications = getAllNotificationsByUserReactAsync(userUri, notificationsToShow);
		return notifications.map((notificationsList)->{
			List<Map<String, Object>> listOfNotResults=new ArrayList<>();
			notificationsList.forEach((notif)->{
				Map<String, Object> map = new HashMap<>();
				Event event = notif.getEvent();
				//notification.setSeen(true);
				if(notif.getNotificationType().equalsIgnoreCase("warning") && notif.getEvent()!=null){
					map.put("message", notif.getEvent().getPersistentProperty("WARNING_NOTIFICATION"));
				} else{
				map.put("message", eventService.getEventTypeStringRepresentation(event, locale, FormatType.NOTIFICATION));
				}
				map.put("notificationType", notif.getNotificationType());
				listOfNotResults.add(map);
			});

			return listOfNotResults;
		});
	/*	return notifications.map((notification)->{
			
			Map<String, Object> map = new HashMap<>();
			Event event = notification.getEvent();
			//notification.setSeen(true);
			if(notification.getNotificationType().equalsIgnoreCase("warning") && notification.getEvent()!=null){
				map.put("message", notification.getEvent().getPersistentProperty("WARNING_NOTIFICATION"));
			} else{
			map.put("message", eventService.getEventTypeStringRepresentation(event, locale, FormatType.NOTIFICATION));
			}
			map.put("notificationType", notification.getNotificationType());
			return map;
		});
*/	}

    public Mono<List<Notification>> getAllNotificationsByUserReactAsync(String userUri, Integer notificationsToShow) {
    	
	 NamedQueryExecutor<Notification> notificationExecutor = new NamedQueryExecutor<Notification>(
                  "Generic.getUnseenNotificationFromUserUri").addParameter("userUri", userUri).addParameter("notifyEvents",getApplicableEvents());
    	 return (notificationsToShow == null)?entityDao.executeQueryAsync(notificationExecutor):
        		entityDao.executeQueryAsync(notificationExecutor, 0, notificationsToShow);
    }

	
    public Flux<Notification> getAllNotificationsByUserReact(String userUri, Integer notificationsToShow) {
    	
	 NamedQueryExecutor<Notification> notificationExecutor = new NamedQueryExecutor<Notification>(
                  "Generic.getUnseenNotificationFromUserUri").addParameter("userUri", userUri).addParameter("notifyEvents",getApplicableEvents());
    	 return (notificationsToShow == null)?entityDao.executeQuery(notificationExecutor):
        		entityDao.executeQuery(notificationExecutor, 0, notificationsToShow);
    }

}
