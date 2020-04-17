package com.nucleus.core.notification.service;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import com.nucleus.core.notification.Notification;
import com.nucleus.event.GenericEvent;

/**
 * The Interface NotificationService.
 */
public interface NotificationService {

    /**
     * Creates the notification entries.
     *
     * @param notifications the notifications
     */
    void createNotificationEntries(List<Notification> notifications);

    /**
     * Creates the notifications using generic event for users.
     *
     * @param genericEvent the generic event
     * @param userUris the user uris
     * @return the list
     */
    List<Notification> createNotificationsUsingGenericEventForUsers(GenericEvent genericEvent, Set<String> userUris);

    /**
     * Gets the localized user notifications.
     *
     * @param userUri the user uri
     * @param locale the locale
     * @param notificationsToShow is the number of notifications to show
     * @return the localized user notifications
     */
    List<Map<String, Object>> getLocalizedUserNotifications(String userUri, Locale locale, Integer notificationsToShow);

    /**
     * Gets the last localized user notifications.
     *
     * @param userUri the user uri
     * @param locale the locale
     * @param number the number
     * @return the last localized user notifications
     */
    List<Map<String, Object>> getLastLocalizedUserNotifications(String userUri, Locale locale, Integer number);

    
    /**
     * Gets all {@link GenericEvent} object from database for passed event type or limited number
     * of unseen notifications if notificationsToShow is not null
     * @param userUri the user uri
     *  @param notificationsToShow is the number of notifications to show
     * @return the all notifications by user
     */
    List<Notification> getAllNotificationsByUser(String userUri, Integer notificationsToShow);

    /**
     * Gets the last notifications.
     *
     * @param userUri the user uri
     * @param number the number
     * @return the last notifications
     */
    List<Notification> getLastNotifications(String userUri, int number);
    
    /**
     * Gets the number of unseen notifications for a user
     *
     * @param userUri the user uri
     * @return count of unseen notifications
     */
    public Long getUnseenNotificationCountByUser(String userUri);
    /**
     * Updates old unseen notifications for the applicable events list for a user.
     *
     * @param userUri the user uri
     * @param notificationIds ids of new notification
     */
    public void updateUnseenNotificationByUserByCreationTimestamp(String userUri, List<Long> notificationIds, List<Integer> applicableEvents);
    /**
     * Gets the applicable events list for a user.
     *
     * @param userUri the user uri
     *  @param maxNumberNotifications count of newUnseenNotification
     * @return list of Ids of new unseen notifications
     */
    public List<Long> getNewNotificationByUserByCreationTimestamp(String userUri, Long maxNumberNotifications, List<Integer> applicableEvents);

	List<Integer> getApplicableEvents();
    List<Integer> getUserEvents(String eventType);
    
}
