package com.nucleus.cfi.push.service;

import com.nucleus.cfi.push.pojo.PushNotification;
import com.nucleus.cfi.push.pojo.PushNotificationResponsePojo;

public interface PushNotificationIntegrationService {

    PushNotificationResponsePojo sendPushNotification(PushNotification pushNotification);
    
    /**
     * Async call to integration server for pushing notification.
     */
    PushNotificationResponsePojo sendPushNotificationAsynchronously(PushNotification pushNotification);

}
