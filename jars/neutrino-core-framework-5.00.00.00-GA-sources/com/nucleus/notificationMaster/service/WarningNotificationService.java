package com.nucleus.notificationMaster.service;

import java.util.Map;

import com.nucleus.event.GenericEvent;

public interface WarningNotificationService {
	
	public void processWarningNotificationTask(String userIdForPopupNotification, String body,GenericEvent eventObj,Map contextmap);

}
