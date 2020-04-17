/**
 * This file and a proportion of its content is copyright of Nucleus Software Exports Limited - © 2012. All rights reserved.
 * Any redistribution or reproduction of part or all of the contents in any form is prohibited other than the following:
 * - you cannot print or download to a local hard disk extract contents either part or full for personal/ commercial/
 * academic or any other use
 * - you may not copy the content to individual/ third parties for any type of use, either as compiled or source format
 * without the knowledge and consent of Nucleus Software
 * - You may not, except with our express written permission, distribute or commercially exploit the content. Nor may you
 * transmit it or store it in any other web site or other form of electronic retrieval system.
 */
package com.nucleus.event;

import java.util.List;
import java.util.Set;

import com.nucleus.core.notification.CommonMailContent;
import com.nucleus.core.notification.Notification;
import com.nucleus.entity.EntityId;

/**
 * Interface to perform some operations to avoid multi threading issues with persistence and transactions.
 *
 * @author Nucleus Software Exports Limited
 *
 */

public interface EventProcessingHelper {

    /**
     * @param event
     * @param userUrisForPopupNotification
     */
    public void processPopupNotificationTask(Event event, Set<String> userUrisForPopupNotification);

    /**
     * @param event
     * @return
     */
    public CommonMailContent getCommonMailContent(Event event);

    /**
     * @param userUris2
     * @param commonMailContent
     */
    public void sendNotificationMail(Set<String> userUris2, CommonMailContent commonMailContent);

    /**
     * @param event
     * @param userUrisForInMail
     */
    public void processInternalMailNotificationTask(Event event, Set<String> userUrisForInMail);

    /**
     * @param genericEvent
     */
    public void saveEvent(Event genericEvent);

    /**
     * @param entityId
     * @return
     */
    public Set<String> getAllWatchersForEntity(EntityId entityId);

    /**
     * @param userUri
     * @param proString
     * @return
     */
    public boolean getUserPreference(String userUri, String proString);

    /**
     * @param event
     * @param userUrisForInMail
     */
    public void processSmtpMailNotificationTask(Event event, Set<String> userUrisForInMail);

    /**
     * @param userUri for fetch notification count
     */
    public Long getUnseenNotificationCountForUser(String userUri);

    /**
     * @param userUri for fetch max number notification to be saved
     */
    public Long getUserPreferenceMaxNotificationCount(String userUri);

    public void processSMSNotificationTask(Event event, Set<String> phonenumbers);

    public void markOldUnseenNotificationAsSeen(Notification notification);

    public List<Long> getNewNotificationByUserByCreationTimestamp(String userUri, Long maxNumberNotifications, List<Integer> applicableEvents);

    public void updateUnseenNotificationByUserByCreationTimestamp(String userUri, List<Long> newNotificationsId, List<Integer> applicableEvents);

    public List<Integer> getApplicableEvents();

    public Long getUserIdFromUserUri(String userUri);

    public String getUserUriFromUserId(Long userId);

}
