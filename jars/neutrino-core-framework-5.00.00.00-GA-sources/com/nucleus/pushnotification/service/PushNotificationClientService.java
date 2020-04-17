package com.nucleus.pushnotification.service;

import java.util.List;

import com.nucleus.pushnotification.vo.PushNoticationsClient;
import com.nucleus.user.UserInfo;
import com.nucleus.ws.core.entities.PushNotificationClientDetail;

public interface PushNotificationClientService {

	PushNotificationClientDetail unregisterPushNotification(String notificationClientId);

	void registerPushNotification(UserInfo user, String trustedSourceName, PushNoticationsClient pushNoticationsClient);

	List<PushNotificationClientDetail> findActivePushNotificationClientDetailByNotificationClientIds(List<String> notificationClientIds);

	List<PushNotificationClientDetail> findActivePushNotificationClientDetailByUserIds(List<Long> userIds);

	List<PushNotificationClientDetail> findActivePushNotificationClientDetailByUserIdsAndTrustedSourceModules(List<Long> userIds,List<String> trustedSourceNames);

}
