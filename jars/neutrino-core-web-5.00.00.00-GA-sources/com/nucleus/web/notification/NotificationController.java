package com.nucleus.web.notification;

import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.nucleus.core.notification.service.NotificationService;
import com.nucleus.web.common.controller.BaseController;

import flexjson.JSONSerializer;

@Controller
public class NotificationController extends BaseController {

    @Inject
    @Named("notificationService")
    private NotificationService notificationService;

    @RequestMapping(value = "/getNotifications", method = RequestMethod.GET)
    public @ResponseBody
    String getNotifications(Locale locale, @RequestParam("number") Integer notificationsToShow) {
        String currentUserUri = getCurrentUserUri();
        List<Map<String, Object>> localizedNotificationList = null;
        if (currentUserUri != null) {
            localizedNotificationList = notificationService.getLocalizedUserNotifications(currentUserUri, locale,
                    notificationsToShow);
        }
        JSONSerializer serializer = new JSONSerializer();
        return serializer.serialize(localizedNotificationList);
    }

    @RequestMapping(value = "/getLastNotifications", method = RequestMethod.POST)
    public @ResponseBody
    String getLastTenNotifications(Locale locale, @RequestParam("number") int number) {
        String currentUserUri = getCurrentUserUri();
        List<Map<String, Object>> localizedNotificationList = null;
        if (currentUserUri != null) {
            localizedNotificationList = notificationService
                    .getLastLocalizedUserNotifications(currentUserUri, locale, number);
        }
        JSONSerializer serializer = new JSONSerializer();
        return serializer.serialize(localizedNotificationList);
    }

    private String getCurrentUserUri() {
        if (getUserDetails() != null && getUserDetails().getUserEntityId() != null) {
            return getUserDetails().getUserEntityId().getUri();
        } else
            return null;
    }
}
