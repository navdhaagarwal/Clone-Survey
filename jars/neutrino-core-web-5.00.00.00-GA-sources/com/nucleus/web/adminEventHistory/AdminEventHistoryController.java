package com.nucleus.web.adminEventHistory;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;

import org.joda.time.DateTime;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.nucleus.core.misc.util.DateUtils;
import com.nucleus.entity.EntityId;
import com.nucleus.event.Event;
import com.nucleus.event.EventService;
import com.nucleus.event.FormatType;
import com.nucleus.user.User;
import com.nucleus.user.UserService;
import com.nucleus.web.common.controller.BaseController;

@Controller
@RequestMapping("adminEventHistory")
public class AdminEventHistoryController extends BaseController {

    private static final String NO_RECORD = "message.record.notFound";

    @Inject
    @Named("eventService")
    private EventService        eventService;

    @Inject
    @Named("userService")
    private UserService         userService;

    @PreAuthorize("hasAuthority('VIEW_ADMIN_EVENTHISTORY')")
    @RequestMapping(value = "/loadPage")
    public String loagPage(ModelMap map) {
        map.put("users", retrieveUsers());
        return "adminEventHistory";
    }

    @RequestMapping(value = "/paginatedSingleUserActivity/{startIndex}/{pageSize}")
    public String retrievePagenatedActivityForSingleUser(ModelMap map, Locale locale,
            @PathVariable("startIndex") int startIndex, @PathVariable("pageSize") int pageSize,
            @RequestParam("toUser") String userSelected) {

        List<Map<String, Object>> activityInfoList = new ArrayList<Map<String, Object>>();
        String userSelectedUri = userService.getUserFromUsername(userSelected).getUserEntityId().getUri();
        if (!userSelectedUri.equals("")) {
            List<Event> eventList = eventService.getPaginatedEventsForUser(userSelectedUri, startIndex, pageSize);
            for (Event genericEvent : eventList) {

                String classForJsp = "";

                // we are adding watched class by default
                classForJsp = "watched ";

                if (genericEvent.getAssociatedUserUri().equals(userSelectedUri)) {
                    classForJsp += "mine ";
                }

                if (genericEvent.getEventType() == 19 || genericEvent.getEventType() == 20
                        || genericEvent.getEventType() == 21) {
                    classForJsp += "loginLogout ";
                }

                Map<String, Object> singleActivityInfo = new HashMap<String, Object>();
                String userName = userService
                        .getUserById(EntityId.fromUri(genericEvent.getAssociatedUserUri()).getLocalId()).getUsername();
                String activityMessage = eventService.getEventTypeStringRepresentation(genericEvent, locale,
                        FormatType.USER_PROFILE);
                DateTime cal = genericEvent.getEventTimestamp();
                // singleActivityInfo.put("userName", userName);
                singleActivityInfo.put("userId", EntityId.fromUri(genericEvent.getAssociatedUserUri()).getLocalId());
                singleActivityInfo.put("activityMessage", activityMessage);
                singleActivityInfo.put("dateTime", cal);
                singleActivityInfo.put("groupName", assignGroup(cal));
                singleActivityInfo.put("classForJsp", classForJsp);
                activityInfoList.add(singleActivityInfo);
                map.put("activityInfoList", activityInfoList);
                map.put("lastGroupName", "");

            }
        }
        return "activity/activitypageforprofile";
    }

    @RequestMapping(value = "/paginatedUserActivity/{startIndex}/{pageSize}")
    public String retrievePagenatedActivityForUser(ModelMap map1, Locale locale, @PathVariable("startIndex") int startIndex,
            @PathVariable("pageSize") int pageSize, @RequestParam("toUsers") HashSet<Long> usersSelected) {

        List<Map<String, Object>> consolidatedList = new ArrayList<Map<String, Object>>();

        for (Long userSelected : usersSelected) {
        	if(userSelected!=null)	{
            Map<String, Object> map = new HashMap<String, Object>();
            List<Map<String, Object>> activityInfoList = new ArrayList<Map<String, Object>>();
            String userSelectedUri = userService.getUserById(userSelected).getUserEntityId().getUri();
            if (!userSelectedUri.equals("")) {
                List<Event> eventList = eventService.getPaginatedEventsForUser(userSelectedUri, startIndex, pageSize);
                if (eventList.size() > 0) {
                    for (Event genericEvent : eventList) {

                        String classForJsp = "";

                        // we are adding watched class by default
                        classForJsp = "watched ";

                        if (genericEvent.getAssociatedUserUri().equals(userSelectedUri)) {
                            classForJsp += "mine ";
                        }

                        if (genericEvent.getEventType() == 19 || genericEvent.getEventType() == 20
                                || genericEvent.getEventType() == 21) {
                            classForJsp += "loginLogout ";
                        }

                        Map<String, Object> singleActivityInfo = new HashMap<String, Object>();
                        String userName = userService.getUserById(
                                EntityId.fromUri(genericEvent.getAssociatedUserUri()).getLocalId()).getUsername();
                        String activityMessage = eventService.getEventTypeStringRepresentation(genericEvent, locale,
                                FormatType.USER_PROFILE);
                        DateTime cal = genericEvent.getEventTimestamp();
                        singleActivityInfo.put("userName", userName);
                        singleActivityInfo.put("activityMessage", activityMessage);
                        singleActivityInfo.put("dateTime", cal);
                        singleActivityInfo.put("groupName", assignGroup(cal));
                        singleActivityInfo.put("classForJsp", classForJsp);
                        
                        
                        activityInfoList.add(singleActivityInfo);
                    }
                } else {
                    String userName = userService.getUserById(userSelected).getUsername();
                    Map<String, Object> singleActivityInfo = new HashMap<String, Object>();
                    singleActivityInfo.put("userName", userName);
                    singleActivityInfo.put("activityMessage", NO_RECORD);
                    activityInfoList.add(singleActivityInfo);
                }
                //User uri
                map.put("userUri", userSelectedUri);
                map.put("activityInfoList", activityInfoList);
                map.put("lastGroupName", "");
                consolidatedList.add(map);

            }
        }
        }
        map1.put("consolidatedList", consolidatedList);
        return "activityMultiUser";
    }

    
    
    @RequestMapping(value = " /paginatedUserActivityforTree/{startIndex}/{pageSize}")
    public String retrievePagenatedActivityForUser(ModelMap map1, Locale locale, @PathVariable("startIndex") int startIndex,
            @PathVariable("pageSize") int pageSize, @RequestParam("userSelectedName") String usersSelectedName) {

        List<Map<String, Object>> consolidatedList = new ArrayList<Map<String, Object>>();

      //  for (Long userSelected : usersSelected) {
        	if(usersSelectedName!=null)	{
            Map<String, Object> map = new HashMap<String, Object>();
            List<Map<String, Object>> activityInfoList = new ArrayList<Map<String, Object>>();
            String userSelectedUri = userService.getUserFromUsername(usersSelectedName).getUserEntityId().getUri();
            if (!userSelectedUri.equals("")) {
                List<Event> eventList = eventService.getPaginatedEventsForUser(userSelectedUri, startIndex, pageSize);
                if (eventList.size() > 0) {
                    for (Event genericEvent : eventList) {

                        String classForJsp = "";

                        // we are adding watched class by default
                        classForJsp = "watched ";

                        if (genericEvent.getAssociatedUserUri().equals(userSelectedUri)) {
                            classForJsp += "mine ";
                        }

                        if (genericEvent.getEventType() == 19 || genericEvent.getEventType() == 20
                                || genericEvent.getEventType() == 21) {
                            classForJsp += "loginLogout ";
                        }

                        Map<String, Object> singleActivityInfo = new HashMap<String, Object>();
                        String userName = userService.getUserById(
                                EntityId.fromUri(genericEvent.getAssociatedUserUri()).getLocalId()).getUsername();
                        String activityMessage = eventService.getEventTypeStringRepresentation(genericEvent, locale,
                                FormatType.USER_PROFILE);
                        DateTime cal = genericEvent.getEventTimestamp();
                        singleActivityInfo.put("userName", userName);
                        singleActivityInfo.put("activityMessage", activityMessage);
                        singleActivityInfo.put("dateTime", cal);
                        singleActivityInfo.put("groupName", assignGroup(cal));
                        singleActivityInfo.put("classForJsp", classForJsp);
                        
                        
                        activityInfoList.add(singleActivityInfo);
                    }
                } else {
                    String userName = userService.getUserFromUsername(usersSelectedName).getUsername();
                    Map<String, Object> singleActivityInfo = new HashMap<String, Object>();
                    singleActivityInfo.put("userName", userName);
                    singleActivityInfo.put("activityMessage", NO_RECORD);
                    activityInfoList.add(singleActivityInfo);
                }
                //User uri
                map.put("userUri", userSelectedUri);
                map.put("activityInfoList", activityInfoList);
                map.put("lastGroupName", "");
                consolidatedList.add(map);

            }
        }
        //}
        map1.put("consolidatedList", consolidatedList);
        return "activityMultiUser";
    }

   
    
    
    private String assignGroup(DateTime cal) {
        DateFormat dformat = new SimpleDateFormat("EEE, MMM d");
        String date = dformat.format(cal.toDate());
        if (date.equalsIgnoreCase(dformat.format(DateUtils.getCurrentUTCTime().toDate())))
            return "Today";
        else
            return date;
    }

    public List<User> retrieveUsers() {

        // // Team team = entityDao.find(Team.class, Long.valueOf(teamId));
        // List<User> userList = new ArrayList<User>();
        //
        //
        //
        // userList.addAll(userService.getAllUser());
        // List<Map<String, ?>> par = new ArrayList<Map<String, ?>>();
        // Map consolidateMap = new HashMap();
        //
        // for (User user : userList) {
        // Map<String, Object> valueMap = new HashMap<String, Object>();
        // valueMap.put("id", String.valueOf(user.getId()));
        // valueMap.put("username", user.getUsername());
        // par.add(valueMap);
        // }
        //
        // consolidateMap = ComboBoxAdapter.listOfMapsToSingleMap(par, "username", "id");

        return userService.getAllUser();

    }
    
    
    /**
     * Hard deletes entries in Users activity stream before specified days
     * @param days days before which entries need to be hard deleted
     * @return page
     */

    @RequestMapping(value = "/deleteBefore", method = RequestMethod.POST)
    public String deleteBefore(ModelMap map,@RequestParam("days") int days,@RequestParam("userUri")String userUri){
    	
    	//get the date 'days' ago
    	DateTime dateTime=new DateTime();
    	DateTime dateBeforeDays=dateTime.minusDays(days);
    	
    	//delete events for userUri
    	eventService.deleteUserEventsBeforeDate(userUri, dateBeforeDays);
    	return loagPage(map);
    }

}
