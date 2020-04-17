/**
get * Copyright Nucleus Software India Pvt Ltd. All rights reserved.
 */
package com.nucleus.web.dashboard;

import static com.nucleus.finnone.pro.general.util.ValidatorUtils.notNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.hibernate.Hibernate;
import org.joda.time.DateTime;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.support.RequestContextUtils;

import com.nucleus.businessmapping.service.UserManagementServiceCore;
import com.nucleus.config.persisted.service.ConfigurationService;
import com.nucleus.config.persisted.vo.ConfigurationVO;
import com.nucleus.core.comment.entity.Comment;
import com.nucleus.core.comment.service.CommentService;
import com.nucleus.core.dashboard.Panel;
import com.nucleus.core.organization.entity.OrganizationBranch;
import com.nucleus.core.organization.entity.SystemName;
import com.nucleus.core.role.entity.Role;
import com.nucleus.core.team.entity.Team;
import com.nucleus.core.team.service.TeamService;
import com.nucleus.entity.EntityId;
import com.nucleus.entity.SystemEntity;
import com.nucleus.event.Event;
import com.nucleus.event.EventService;
import com.nucleus.event.EventTypes;
import com.nucleus.event.FormatType;
import com.nucleus.user.User;
import com.nucleus.user.UserInfo;
import com.nucleus.user.UserProfile;
import com.nucleus.user.UserService;
import com.nucleus.web.common.controller.BaseController;

import flexjson.JSONSerializer;
import net.bull.javamelody.MonitoredWithSpring;

/**
 * @author taru.agarwal
 */
@Controller
public class DashboardController extends BaseController {

    @Inject
    @Named("userService")
    private UserService                 userService;

    @Inject
    @Named("userManagementServiceCore")
    private UserManagementServiceCore   userManagementService;


    @Inject
    @Named("messageSource")
    protected MessageSource             messageSource;

    @Inject
    @Named("teamService")
    private TeamService                 teamService;

    // to exclude login logout events
    private static final List<Integer>  LOGIN_LOGOUT_EVENT_TYPES_LIST = Collections.unmodifiableList(Arrays.asList(
                                                                              EventTypes.USER_SECURITY_TRAIL_LOGIN_FAIL,
                                                                              EventTypes.USER_SECURITY_TRAIL_LOGIN_SUCCESS,
                                                                              EventTypes.USER_SECURITY_TRAIL_LOGOUT));

    private static final String         CAS_BRANCH_ADMIN              = "CAS_BRANCH_ADMIN";
    private static final String         CAS_ADMIN                     = "CAS_ADMIN";
    private static final String         SYSTEM_RIGHTCLICK_DISABLED    = "config.system.rightclick.disabled";
    private static final String         SYSTEM_AUTOFILL_ENABLED       = "config.system.autofill.enabled";
	public static final String Proxy_Message = "proxy_message";

    @Inject
    @Named("eventService")
    private EventService                eventService;

    @Inject
    @Named(value = "commentService")
    private CommentService              commentService;


    @Inject
    @Named(value = "configurationService")
    private ConfigurationService        configurationService;

    /**
     * @param
     * @return String
     * @throws IOException 
     * @throws JsonMappingException 
     * @throws JsonGenerationException 
     * @throws
     * @description Action method to load common dashboard jsp for handling
     *              country master ,task and state CRUD
     */
    /*For PDDEV-23142 : Once code clean up for Session Attribute is completed across modules
     * We MAY remove the requestParams 'errorOccurred' & 'errCode'  and corresponding usages within the method*/
    @RequestMapping(value = "/dashboard", method = RequestMethod.GET)
    @MonitoredWithSpring(name = "DC_LOAD_DASHBOARD_GRID")
    public String getGridDashboard(@RequestParam(value = "errorOccurred", required = false) boolean errorOccurred, @RequestParam(value = "errCode", required = false) String errorCode,
                                   @RequestParam(value = QUERYACCESSDENIED, required = false) boolean resourceAccessDeniedFlag,
                                   ModelMap modelMap, @RequestHeader Map<String, String> headers, HttpServletRequest request, HttpSession session)
            throws JsonGenerationException, JsonMappingException, IOException {
    	
    	if(resourceAccessDeniedFlag){
            modelMap.put("resourceAccessDeniedFlag", resourceAccessDeniedFlag);
        }
    	if(errorOccurred){
            modelMap.put("errorOccurred", errorOccurred);
        }
    	Locale locale = RequestContextUtils.getLocale(request);
        if(StringUtils.isNotEmpty(errorCode)){
            modelMap.put("errorMessage", messageSource.getMessage(errorCode, null, locale));
        }

        String proxyMessage = null;
        UserInfo ui = getUserDetails();
        String uuid = ui.getUuid();
        Map<String, ?> userDetails = userManagementService.findUserByUUID(uuid);
        User userObj = (User) userDetails.get("user");
        UserProfile userProfile = userService.getUserProfile(userObj);
        String ipCameFrom = request.getRemoteAddr();

        proxyMessage =  (String) session.getAttribute(Proxy_Message);
        
        
        if(proxyMessage==null)
        {
        
        proxyMessage = userService.validateUserIp(headers, ipCameFrom,userProfile , locale);
        session.setAttribute(Proxy_Message,proxyMessage);
        
        StringTokenizer token = new StringTokenizer(proxyMessage, ":");

        modelMap.put("proxyMessage", token.nextToken().trim());
        modelMap.put("success", token.nextToken().trim());

        
        }
        
        
        
        /* List<Panel> panelList = dashboardService.getUserTasksForDashboard(ui);*/
        List<Panel> panelList = new ArrayList<Panel>();
        modelMap.put("panelList", panelList);

        // modelMap.put("panelListJson", OBJECT_MAPPER.writeValueAsString(panelList));
        modelMap.put("panelListJson", "");
        Map<String, ConfigurationVO> preferences = getUserDetails().getUserPreferences();
        ConfigurationVO stream = preferences.get("config.dashboard.streamWidget");
        //ConfigurationVO notes = preferences.get("config.dashboard.notesWidget");
        ConfigurationVO comment = preferences.get("config.dashboard.commentWidget");
        ConfigurationVO recentMails = preferences.get("config.dashboard.recentMails");
        if(notNull(stream))
        {
        modelMap.put("stream", stream.getPropertyValue());
        }
/*        if(notNull(notes))
        {
        modelMap.put("notes", notes.getPropertyValue());
        }*/
        if(notNull(comment))
        {
        modelMap.put("comment", comment.getPropertyValue());
        }
        if(notNull(recentMails))
        {
        modelMap.put("recentMails", recentMails.getPropertyValue());
        }

        return "dashboard";
    }

    @RequestMapping(value = "/getTeams", method = RequestMethod.GET)
    @ResponseBody
    public String getTeams() {

        boolean isBranchAdmin = false;
        boolean isALLBranchAdmin = false;
        List<Team> teamList = null;

        UserInfo userInfo = getUserDetails();
        Map<Long, String> teamMap = new HashMap<Long, String>();
        List<Role> roles = userService.getRolesFromUserId(userInfo.getId());
        for (Role role : roles) {
            if (role.getName().equals(CAS_BRANCH_ADMIN)) {
                isBranchAdmin = true;
                break;
            } else if (role.getName().equals(CAS_ADMIN)) {
                isALLBranchAdmin = true;
                break;
            }
        }

        // For Performance T

        /*if (isBranchAdmin) {
            teamList = teamService.getAllTeamsOfLoggedInBranchOfThisUser(userInfo);
        } else if (isALLBranchAdmin) {
            teamList = teamService.getAllTeams();
        } else if (!isBranchAdmin && !isALLBranchAdmin) {
            teamList = teamService.getTeamsLedByThisUserInLoggedInBranch(userInfo);
        }
        for (Team team : teamList) {
            teamMap.put(team.getId(), team.getName());
        }*/
        List<Object[]> teamid_name_list = null;

        if (isBranchAdmin) {
            teamid_name_list = teamService.getAllTeamIdsAndNamesOfLoggedInBranchOfThisUser(userInfo);

        } else if (isALLBranchAdmin) {
            teamid_name_list = teamService.getAllTeamIdsAndNames();

        } else if (!isBranchAdmin && !isALLBranchAdmin) {
            teamid_name_list = teamService.getTeamIdsAndNamesLedByThisUserInLoggedInBranch(userInfo);
        }

        for (Object[] team : teamid_name_list) {
            teamMap.put(Long.parseLong(String.valueOf(team[0])), String.valueOf(team[1]));
        }
        // End For Performance T

        JSONSerializer iSerializer = new JSONSerializer();
        String jsonString = iSerializer.deepSerialize(teamMap);
        return jsonString;

    }

    @RequestMapping(value = "/getTeamUsers", method = RequestMethod.GET)
    @ResponseBody
    public String getTeamUsers(@RequestParam(value = "teamID") Long teamID) {

        Map<Long, String> userInfoMap = new HashMap<Long, String>();

        Set<UserInfo> userInfoList = teamService.getAssociatedUsersOfTeamByTeamId(teamID);

        for (UserInfo userInfo : userInfoList) {
            userInfoMap.put(userInfo.getId(), userInfo.getDisplayName());
        }

        JSONSerializer iSerializer = new JSONSerializer();
        String jsonString = iSerializer.deepSerialize(userInfoMap);
        return jsonString;

    }

    /**
     * Method To retrieve live feeds for some dashboard widgets which  show live data feeds like
     * recent activity,recent comments etc. 
     * @param map
     * @param locale
     * @param pageSize
     * @param latestEventId
     * @param latestCommentId
     * @return
     */
    @RequestMapping(value = "/getDashboardLiveFeeds/{pageSize}")
    @MonitoredWithSpring(name = "DC_FETCH_DASHBOARD_WIDGETS")
    public String retrieveDashboardLiveStreamWidgetsFeed(ModelMap map, Locale locale,
            @PathVariable("pageSize") int pageSize,
            @RequestParam(value = "latestEventId", required = false) Long latestEventId,
            @RequestParam(value = "latestCommentId", required = false) Long latestCommentId,
            @RequestParam(value = "latestMailId", required = false) Long latestMailId,
            @RequestParam(value = "latestNoteId", required = false) Long latestNoteId) {

        // 1.FOR Recent Activity Widget
        List<Map<String, Object>> activityInfoList = retrieveRecentActivityWidgetFeed(pageSize, latestEventId, locale);
        map.put("activityInfoList", activityInfoList);
        if (activityInfoList != null && !activityInfoList.isEmpty()) {
            map.put("latestEventId", activityInfoList.get(0).get("eventId"));
        }
        // 2.FOR Recent Comment Widget
        List<Map<String, Object>> commentInfoList = retrieveRecentCommentWidgetFeed(pageSize, latestCommentId);
        map.put("commentInfoList", commentInfoList);
        if (commentInfoList != null && !commentInfoList.isEmpty()) {
            map.put("latestCommentId", commentInfoList.get(0).get("commentId"));
        }
      /*  // 3. For Recent Mails
        List<UserMailNotificationVO> mailInfoList = retrieveRecentMailWidgetFeed(pageSize, latestMailId);
        map.put("mailInfoList", mailInfoList);
        if (mailInfoList != null && !mailInfoList.isEmpty()) {
            map.put("latestMailId", mailInfoList.get(0).getNotificationID());
        }
*/
/*        // 4. For Recent Notes
        List<Map<String, Object>> noteInfoList = retrieveRecentNotesWidgetFeed(pageSize, latestNoteId);
        map.put("noteInfoList", noteInfoList);
        if (noteInfoList != null && !noteInfoList.isEmpty()) {
            map.put("latestNoteId", noteInfoList.get(0).get("noteId"));
        }*/

        return "activitypageforDashboard";
    }

    // 1.FOR Recent Activity Widget Feed
    private List<Map<String, Object>> retrieveRecentActivityWidgetFeed(int pageSize, Long latestEventId, Locale locale) {

        List<Map<String, Object>> activityInfoList = new ArrayList<Map<String, Object>>();
        String currentUserUri = getUserDetails().getUserEntityId().getUri();
        if (!currentUserUri.equals("")) {
            List<Event> eventList = (latestEventId != null && latestEventId != -1) ? eventService
                    .getPaginatedEventsForUserWithExcludeAfterEvent(currentUserUri, 0, pageSize,
                            LOGIN_LOGOUT_EVENT_TYPES_LIST, latestEventId) : eventService
                    .getPaginatedEventsForUserWithExclude(currentUserUri, 0, pageSize, LOGIN_LOGOUT_EVENT_TYPES_LIST);
            for (Event genericEvent : eventList) {

                String cssClassForEvent = genericEvent.getAssociatedUserUri().equals(
                        getUserDetails().getUserEntityId().getUri()) ? "mine" : "watched";

                Map<String, Object> singleActivityInfo = new HashMap<String, Object>();
                long userId = EntityId.fromUri(genericEvent.getAssociatedUserUri()).getLocalId();
                String activityMessage = eventService.getEventTypeStringRepresentation(genericEvent, locale,
                        FormatType.ACTIVITY_STREAM);
                DateTime cal = genericEvent.getEventTimestamp();
                singleActivityInfo.put("userId", userId);
                singleActivityInfo.put("activityMessage", activityMessage);
                singleActivityInfo.put("dateTime", cal);
                singleActivityInfo.put("cssClassForEvent", cssClassForEvent);
                singleActivityInfo.put("eventId", genericEvent.getId());
                activityInfoList.add(singleActivityInfo);
            }

        }
        return activityInfoList;
    }

    // 2.FOR Recent Comments Widget Feed
    private List<Map<String, Object>> retrieveRecentCommentWidgetFeed(int pageSize, Long lastCommentId) {

        List<Map<String, Object>> commentInfoList = new ArrayList<Map<String, Object>>();
        String currentUserUri = getUserDetails().getUserEntityId().getUri();
        if (!currentUserUri.equals("")) {

            List<Comment> commentList = (lastCommentId != null && lastCommentId != -1) ? commentService
                    .getCommentsForEntitiesWatchedByUserAfterComment(currentUserUri, pageSize, lastCommentId)
                    : commentService.getCommentsForEntitiesWatchedByUser(currentUserUri, pageSize);

            for (Comment comment : commentList) {

                Map<String, Object> singleCommentInfo = new HashMap<String, Object>();
                String entityName = comment.getOwnerEntityUri().getEntityClass().getSimpleName();
                long userId = comment.getAddedBy().getLocalId();
                String commentText = comment.getText();
                DateTime cal = comment.getAddTimestamp();
                singleCommentInfo.put("entityName", entityName);
                singleCommentInfo.put("userId", userId);
                singleCommentInfo.put("commentText", commentText);
                singleCommentInfo.put("dateTime", cal);
                singleCommentInfo.put("commentId", comment.getId());
                commentInfoList.add(singleCommentInfo);
            }
        }
        return commentInfoList;
    }

   /* // 3. For Recent Mails
    private List<UserMailNotificationVO> retrieveRecentMailWidgetFeed(int pageSize, Long latestMailId) {
        User user = getUserDetails().getUserReference();
        EntityId entityId = EntityId.fromUri(user.toString());
        List<String> statList = new ArrayList<String>();
        statList.add(UserMailNotificationType.USER_MAIL_NEW);

        List<Map<String, ?>> notifications = (latestMailId != null && latestMailId != -1) ? userMailNotificationService
                .getUserNotifications(entityId.getUri(), statList, "recentInInbox", latestMailId, pageSize)
                : userMailNotificationService.getUserNotifications(entityId.getUri(), statList, "inbox", null, pageSize);

        // Get List Of Inbox

        List<UserMailNotificationVO> mailList = new ArrayList<UserMailNotificationVO>();
        for (Map<String, ?> notification : notifications) {
            UserMailNotificationVO mailNotify = new UserMailNotificationVO();
            CommonMailContent cmc = (CommonMailContent) notification.get("commonMailContent");
            UserInfo createdBy = userService.getUserById(EntityId.fromUri(
                    cmc.getFromUserUri() == null ? "" : cmc.getFromUserUri().toString()).getLocalId());
            mailNotify.setFromUser(createdBy.getDisplayName() == null ? "" : createdBy.getDisplayName());
            mailNotify.setNotificationID((Long) notification.get("id"));
            mailNotify.setSubject(cmc.getSubject() == null ? "" : cmc.getSubject().toString());
            if (cmc.getMsgSentTimeStamp() != null) {
                DateTime calendar = cmc.getMsgSentTimeStamp();
                mailNotify.setMsgTimeStamp(calendar);
            }
            mailList.add(mailNotify);

        }

        return mailList;

    }*/
/*
    // 4.FOR Recent Notes Widget Feed
    private List<Map<String, Object>> retrieveRecentNotesWidgetFeed(int pageSize, Long lastNoteId) {

        List<Map<String, Object>> noteInfoList = new ArrayList<Map<String, Object>>();
        String currentUserUri = getUserDetails().getUserEntityId().getUri();
        if (!currentUserUri.equals("")) {

            List<Note> noteList = (lastNoteId != null && lastNoteId != -1) ? noteService
                    .getNotesForEntitiesWatchedByUserAfterNote(currentUserUri, pageSize, lastNoteId) : noteService
                    .getNotesForEntitiesWatchedByUser(currentUserUri, pageSize);

            for (Note note : noteList) {

                Map<String, Object> singleNoteInfo = new HashMap<String, Object>();
                String entityName = note.getOwnerEntityUri().getEntityClass().getSimpleName();
                long userId = note.getAddedBy().getLocalId();
                String noteText = note.getText();
                String noteTitle = note.getTitle();
                DateTime cal = note.getAddTimestamp();
                singleNoteInfo.put("entityName", entityName);
                singleNoteInfo.put("userId", userId);
                singleNoteInfo.put("noteText", noteText);
                singleNoteInfo.put("noteTitle", noteTitle);
                singleNoteInfo.put("dateTime", cal);
                singleNoteInfo.put("noteId", note.getId());
                noteInfoList.add(singleNoteInfo);
            }
        }
        return noteInfoList;
    }*/

    @RequestMapping(value = "/getLeadConversion", method = RequestMethod.GET)
    @MonitoredWithSpring(name = "DC_FETCH_LEAD_CONVERSION")
    @ResponseBody
    public String getLeadConversion(HttpServletRequest request, @RequestParam(value = "criteria") String criteria,
            @RequestParam(value = "teamID", required = false) Long teamID,
            @RequestParam(value = "startDate", required = false) DateTime startDate,
            @RequestParam(value = "endDate", required = false) DateTime endDate) {

        List<Object> resultList = new ArrayList<Object>();
        List<String> leadConversionStagesList = new ArrayList<String>();
        leadConversionStagesList.add("OPEN");
        leadConversionStagesList.add("QUICKLEAD");
        leadConversionStagesList.add("REJECTED");
        leadConversionStagesList.add("HOLD");
        leadConversionStagesList.add("QUALIFIEDLEAD");
        leadConversionStagesList.add("CLOSED");

        // change key names to user friendly name
        Locale loc = RequestContextUtils.getLocale(request);
        List<Object[]> newResultList = new ArrayList<Object[]>();
        for (Object x : resultList) {
            Object[] ar = (Object[]) x;
            String statusCode = (String) ar[0];

            if (statusCode.equalsIgnoreCase("OPEN")) {
                ar[0] = messageSource.getMessage("label.dashboard.leadsConversion.open", null, loc);
            } else if (statusCode.equalsIgnoreCase("QUICKLEAD")) {
                ar[0] = messageSource.getMessage("label.dashboard.leadsConversion.quickLead", null, loc);
            } else if (statusCode.equalsIgnoreCase("REJECTED")) {
                ar[0] = messageSource.getMessage("label.dashboard.leadsConversion.rejected", null, loc);
            } else if (statusCode.equalsIgnoreCase("HOLD")) {
                ar[0] = messageSource.getMessage("label.dashboard.leadsConversion.hold", null, loc);
            } else if (statusCode.equalsIgnoreCase("QUALIFIEDLEAD")) {
                ar[0] = messageSource.getMessage("label.dashboard.leadsConversion.qualifiedLead", null, loc);
            } else if (statusCode.equalsIgnoreCase("CLOSED")) {
                ar[0] = messageSource.getMessage("label.dashboard.leadsConversion.closed", null, loc);
            }

            newResultList.add(ar);

        }

        JSONSerializer iSerializer = new JSONSerializer();
        String jsonString = iSerializer.deepSerialize(newResultList);
        return jsonString;

    }

    @RequestMapping(value = "/getUserDisplayCard/{id}", method = RequestMethod.GET)
    public String getUserDisplayCard(ModelMap map, Locale locale, @PathVariable("id") Long userID) {

        List<Team> teamList = teamService.getTeamsAssociatedToUserByUserId(userID);
        String userEmailID = userService.getUserMailById(userID);
        UserInfo userInfo = userService.getUserById(userID);
        // OrgBranch: Branch View defaulted to CAS
        List<OrganizationBranch> orgBranchList = userManagementService.getUserOrgBranches(userInfo.getId(),
                SystemName.SOURCE_PRODUCT_TYPE_CAS);
        for (Team team : teamList) {
            Hibernate.initialize(team.getTeamLead());
        }

        map.put("userDispName", userInfo.getDisplayName());
        map.put("userEmailID", userEmailID);
        map.put("teamList", teamList);
        map.put("orgBranchList", orgBranchList);

        return "userDisplayCard";

    }

    @RequestMapping(value = "/rightClickStatus", method = RequestMethod.POST)
    @ResponseBody
    public boolean findRightClickStatus() {
        boolean rightClickStatus = Boolean.valueOf(configurationService.getConfigurationPropertyFor(
                SystemEntity.getSystemEntityId(), SYSTEM_RIGHTCLICK_DISABLED).getPropertyValue());
        return rightClickStatus;
    }

    @RequestMapping(value = "/autoFillStatus", method = RequestMethod.POST)
    @ResponseBody
    public boolean findautoFillStatus() {
        boolean autoFillStatus = Boolean.valueOf(configurationService.getConfigurationPropertyFor(
                SystemEntity.getSystemEntityId(), SYSTEM_AUTOFILL_ENABLED).getPropertyValue());
        return autoFillStatus;
    }
}
