package com.nucleus.notificationMaster.service;

import java.util.Set;

import com.nucleus.core.notification.CommonMailContent;
import com.nucleus.service.BaseService;

public interface InAppMailHelper extends BaseService {

    public void sendNotificationAndCorporateEmails(Set<String> userUris2, CommonMailContent commonMailContent,
            boolean filteringEnabled, String notificationPriority);

    boolean checkUserEnabled(String userUri);

}
