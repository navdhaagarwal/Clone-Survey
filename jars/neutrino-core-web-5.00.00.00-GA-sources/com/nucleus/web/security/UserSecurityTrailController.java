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
package com.nucleus.web.security;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;

import net.bull.javamelody.MonitoredWithSpring;

import org.joda.time.DateTime;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.nucleus.event.Event;
import com.nucleus.event.EventService;
import com.nucleus.event.EventTypes;
import com.nucleus.event.UserSecurityTrailEvent;
import com.nucleus.logging.BaseLoggers;
import com.nucleus.web.common.controller.BaseController;

/**
 * @author Nucleus Software Exports Limited This class is to display
 *         login/logout history of users with their ip address,date,time and
 *         other relevant information.
 */
@Controller
@RequestMapping(value = "/securitytrail")
public class UserSecurityTrailController extends BaseController {

    private static final int NUMBER_OF_EVENTS = 10;

    @Inject
    @Named("eventService")
    private EventService     eventService;

    /**
     * This Method retrieves user login logout activity details (only last 10)
     * and return a json object to login history.
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    @RequestMapping(value = "/viewlog")
    public @ResponseBody
    List<Map<String, ?>> viewHistory(ModelMap map) {

        List<Event> logInfo = eventService.getLimtedSecurityEventsByAssociatedUseruri(getUserDetails().getUserEntityId()
                .getUri(), NUMBER_OF_EVENTS);
        List<Map<String, ?>> logInfoDisplay = new ArrayList<Map<String, ?>>();
        if (logInfo != null) {
            Iterator<Event> logInfoItr = logInfo.iterator();
            while (logInfoItr.hasNext()) {
                UserSecurityTrailEvent userSecTrail = (UserSecurityTrailEvent) logInfoItr.next();
                Map renderMap = new HashMap();
                renderMap.put("Username", userSecTrail.getUsername());
                DateTime cal = userSecTrail.getEventTimestamp();
                Date date = cal.toDate();
                DateFormat tformat = new SimpleDateFormat("HH:mm:ss");
                renderMap.put("Date", getFormattedDate(cal));
                renderMap.put("Time", tformat.format(date));
                renderMap.put("RemoteIpAddress", userSecTrail.getRemoteIpAddress());
                renderMap.put("ModuleName",userSecTrail.getModuleNameForEvent());
                Integer eventType = userSecTrail.getEventType();
                if (eventType == EventTypes.USER_SECURITY_TRAIL_LOGIN_SUCCESS) {
                    renderMap.put("Activity", "Login");
                    renderMap.put("Status", "Success");
                } else if (eventType == EventTypes.USER_SECURITY_TRAIL_LOGIN_FAIL) {
                    renderMap.put("Activity", "Login");
                    renderMap.put("Status", "Fail");
                } else if (eventType == EventTypes.USER_SECURITY_TRAIL_LOGOUT) {
                    renderMap.put("Activity", "Logout");
                    renderMap.put("Status", "Success");
                }
                logInfoDisplay.add(renderMap);
            }
        }
        return logInfoDisplay;
    }

    /**
     * This Method retrieves user's last login activity details and return a
     * json object.
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    @RequestMapping(value = "/viewlastlogin")
    @MonitoredWithSpring(name = "USTC_VIEW_LAST_LOGIN_DETAILS")
    public @ResponseBody
    Map viewLastLoginDetails(ModelMap map) {
        Event event = eventService.getLastSuccessLoginEventByAssociatedUseruri(getUserDetails().getUserEntityId().getUri());
        UserSecurityTrailEvent lastLoginInfo = null;
        BaseLoggers.flowLogger.debug("GetLastSuccessLoginEvent " + event);
        if (event != null) {
            lastLoginInfo = (UserSecurityTrailEvent) event;
        }
        Map lastLoginInfoMap = new HashMap();
        if (lastLoginInfo != null) {
            DateTime cal = lastLoginInfo.getEventTimestamp();
            lastLoginInfoMap.put("Date", getFormattedDate(cal));
            lastLoginInfoMap.put("Time", getFormattedTime(cal));
            lastLoginInfoMap.put("RemoteIpAddress", lastLoginInfo.getRemoteIpAddress());
        }
        return lastLoginInfoMap;
    }

}
