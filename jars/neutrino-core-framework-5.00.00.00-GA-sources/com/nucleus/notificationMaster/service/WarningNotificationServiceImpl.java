package com.nucleus.notificationMaster.service;

import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;

import com.nucleus.core.notification.Notification;
import com.nucleus.core.validation.util.NeutrinoValidator;
import com.nucleus.event.GenericEvent;
import com.nucleus.logging.BaseLoggers;
import com.nucleus.persistence.EntityDao;
import com.nucleus.user.UserService;

@Named("warningNotificationService")
public class WarningNotificationServiceImpl implements WarningNotificationService{
	
	 @Inject
     @Named("userService")
	 private UserService                    userService;
	
	 @Inject
	 @Named("entityDao")
	 private EntityDao                      entityDao;


	@Override
	public void processWarningNotificationTask(String userIdForPopupNotification, String body, GenericEvent eventObj,
			Map contextmap) {

		
        if (userIdForPopupNotification != null && !userIdForPopupNotification.isEmpty()) {
                String userUri = userService.getUserById(Long.parseLong(userIdForPopupNotification)).getUserEntityId().getUri();

            NeutrinoValidator.notNull(userUri, "Notification user set can not be null");
                Notification notification = new Notification();
                notification.setNotificationUserUri(userUri);
                notification.setNotificationType("warning");
                notification.setSeen(false);
                notification.setGenericEvent(eventObj);
                notification.setEventType(eventObj.getEventType());
                entityDao.saveOrUpdate(notification);
        }else{
            BaseLoggers.exceptionLogger.error("No user found to send Warning Notification");
            }

    
		
	}

}
