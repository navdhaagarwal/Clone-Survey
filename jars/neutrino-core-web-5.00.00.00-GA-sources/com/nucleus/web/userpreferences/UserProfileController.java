package com.nucleus.web.userpreferences;

import static com.nucleus.finnone.pro.base.utility.CoreUtility.getUserUri;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.nucleus.finnone.pro.base.utility.CoreUtility;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.Hibernate;
import org.joda.time.DateTime;
import org.joda.time.Days;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.SessionAttributes;

import com.csvreader.CsvWriter;
import com.nucleus.businessmapping.service.UserManagementServiceCore;
import com.nucleus.config.persisted.service.ConfigurationService;
import com.nucleus.config.persisted.vo.ConfigurationVO;
import com.nucleus.core.genericparameter.service.GenericParameterService;
import com.nucleus.core.misc.util.DateUtils;
import com.nucleus.core.role.entity.Role;
import com.nucleus.core.team.entity.Team;
import com.nucleus.core.team.service.TeamService;
import com.nucleus.entity.EntityId;
import com.nucleus.event.Event;
import com.nucleus.event.EventService;
import com.nucleus.event.FormatType;
import com.nucleus.event.UserSecurityTrailEvent;
import com.nucleus.user.OutOfOfficeDetails;
import com.nucleus.user.User;
import com.nucleus.user.UserInfo;
import com.nucleus.user.UserProfile;
import com.nucleus.user.UserSecurityQuestion;
import com.nucleus.user.UserSecurityQuestionAnswer;
import com.nucleus.user.UserService;
import com.nucleus.web.common.controller.BaseController;

//import com.nucleus.user.OutOfOfficeDetailsVO;
import flexjson.JSONSerializer;

@Controller
@SessionAttributes("preferences")
@RequestMapping("User")
public class UserProfileController extends BaseController {

    @Inject
    @Named("configurationService")
    private ConfigurationService      configurationService;

    @Inject
    @Named("eventService")
    private EventService              eventService;
    @Inject
    @Named("userService")
    private UserService               userService;

    @Inject
    @Named("teamService")
    private TeamService               teamService;

    @Inject
    @Named("userManagementServiceCore")
    private UserManagementServiceCore userManagementService;

    @Inject
    @Named("genericParameterService")
    protected GenericParameterService genericParameterService;

    @Value("${core.web.config.commonsMultipartResolver.maxUserProfileImgSize}")
    private String maxUserProfileImgSize;

    private static final String loginLogoutEvent = "loginLogout";
    private static final String mineEvent = "mine";

    @RequestMapping(value = "/userProfile", method = RequestMethod.GET)
    public String loadPreferences(ModelMap modelMap) {
    	prepareUserProfileData(modelMap);
        return "userProfile";
    }

    /**
     * This method is used to show user activity on profile page in a paginated
     * view. Fetch all events where user is a watcher of event's owner entity or
     * himself is directly associated with event.
     * 
     * @param map
     * @param locale
     * @param startIndex
     * @param pageSize
     * @param lastGroupName
     * @return
     */
    @RequestMapping(value = "/paginatedUserActivity/{startIndex}/{pageSize}")
    public String retrievePagenatedActivityForUser(ModelMap map, Locale locale, @PathVariable("startIndex") int startIndex,
            @PathVariable("pageSize") int pageSize, @RequestParam("lastGroupName") String lastGroupName) {
        List<Map<String, Object>> activityInfoList = new ArrayList<Map<String, Object>>();
        String currentUserUri = getUserDetails().getUserEntityId().getUri();
        if (!currentUserUri.equals("")) {
            List<Event> eventList = eventService.getPaginatedEventsForUser(currentUserUri, startIndex, pageSize);
            for (Event genericEvent : eventList) {

                String classForJsp = "";

                // we are adding watched class by default
                classForJsp = "watched ";

                if (genericEvent.getAssociatedUserUri().equals(getUserDetails().getUserEntityId().getUri())) {
                    classForJsp += "mine ";
                }

                if (genericEvent.getEventType() == 19 || genericEvent.getEventType() == 20
                        || genericEvent.getEventType() == 21) {
                    classForJsp += "loginLogout ";
                }

                Map<String, Object> singleActivityInfo = new HashMap<String, Object>();
                long userId = EntityId.fromUri(genericEvent.getAssociatedUserUri()).getLocalId();
                String activityMessage = eventService.getEventTypeStringRepresentation(genericEvent, locale,
                        FormatType.USER_PROFILE);
                DateTime cal = genericEvent.getEventTimestamp();
                singleActivityInfo.put("userId", userId);
                singleActivityInfo.put("activityMessage", activityMessage);
                singleActivityInfo.put("dateTime", cal);
                singleActivityInfo.put("groupName", assignGroup(cal));
                singleActivityInfo.put("classForJsp", classForJsp);

                activityInfoList.add(singleActivityInfo);
            }
            map.put("activityInfoList", activityInfoList);
            map.put("lastGroupName", lastGroupName);
        }
        return "activitypageforprofile";
    }

    private String assignGroup(DateTime cal) {
        DateFormat dformat = new SimpleDateFormat("EEE, MMM d");
        String date = dformat.format(cal.toDate());
        if (date.equalsIgnoreCase(dformat.format(DateUtils.getCurrentUTCTime().toDate())))
            return "Today";
        else
            return date;
    }

    @RequestMapping(value = "/loadOutOfOffice")
    public String viewOutOfOfficeInfo(@RequestParam("userId") Long userId, ModelMap map) {
        UserInfo userInfo = userService.getUserById(userId);
        User userObj = userService.findUserByUsername(userInfo.getUsername());
        OutOfOfficeDetails outOffOffice = userObj.getOutOfOfficeDetails();
        if (outOffOffice != null) {
            map.put("outOfOfficeDetails", outOffOffice);
        } else {
            map.put("outOfOfficeDetails", new OutOfOfficeDetails());
        }
        return "userOutOfOffice";
    }

    @RequestMapping(value = "/saveOutOfOfficeDetails")
    public String saveOutOfOfficeDetails(OutOfOfficeDetails outOfOfficeDetails,
            ModelMap map) {
    	UserInfo userInfo = getUserDetails();
		User userObj = userService.findUserByUsername(userInfo.getUsername());
		DateTime fromDate = outOfOfficeDetails.getFromDate();
		DateTime toDate = outOfOfficeDetails.getToDate();
		if (null != fromDate && null != toDate) {
			if ((fromDate.withTimeAtStartOfDay().equals(new DateTime()
					.withTimeAtStartOfDay()))) {
				outOfOfficeDetails.setOutOfOffice(true);
			}
		} else {
			outOfOfficeDetails.setOutOfOffice(false);
		}
		
		outOfOfficeDetails.setAssignedTo(outOfOfficeDetails.getAssignedTo());
		outOfOfficeDetails.setDelegatedToUserId(outOfOfficeDetails.getDelegatedToUserId());
		userObj.setOutOfOfficeDetails(outOfOfficeDetails);
		userService.updateUser(userObj);
		flushCurrentTransaction();
		
		return "userOutOfOffice";
    }

    @RequestMapping(value = "/inOffice")
    public String markInOffice(@RequestParam("userId") Long userId) {
        UserInfo userInfo = userService.getUserById(userId);
        User userObj = userService.findUserByUsername(userInfo.getUsername());
        if (userObj.getOutOfOfficeDetails() != null) {
            userObj.getOutOfOfficeDetails().setOutOfOffice(false);
            userObj.getOutOfOfficeDetails().setFromDate(null);
            userObj.getOutOfOfficeDetails().setToDate(null);
            userObj.getOutOfOfficeDetails().setDelegatedToUserId(null);
            userService.updateUser(userObj);
            UserInfo userDetail = (UserInfo) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            userDetail.setOutOfOffice(false);
            CoreUtility.syncSecurityContextHolderInSession(userInfo.getMappedSessionId());

            /* UsernamePasswordAuthenticationToken userToken = new UsernamePasswordAuthenticationToken(userDetail,
                     userDetail.getPassword());
             SecurityContextHolder.getContext().setAuthentication(userToken);*/

            return "redirect:/app/dashboard";
        } else {
            return "outOfOfficeLogout";
        }

    }

    @RequestMapping(value = "/outOfOffice")
    public String outOfOffice() {
        return "outOfOfficeLogout";
    }

    /**
     * Creates a downloadable CSV for User Activity Stream
     * @param locale 
     * @param request
     * @param response
     * @throws IOException
     */
    @RequestMapping(value = "/csvView", method = RequestMethod.POST)
    public void createCSV( Locale locale, HttpServletRequest request,
            HttpServletResponse response,@RequestParam("userUri")String userUri) throws IOException {

        UserInfo userInfo = getUserDetails();
        Long id = userInfo.getId();
        String currentUserUri = userInfo.getUserEntityId().getUri();

        String fullName = userService.getUserFullNameForUserId(id);
        if(!StringUtils.isEmpty(fullName)){
            fullName = fullName.replace(" ", "_");
        }

        if(!EntityId.isUriOfClass(userUri, User.class)) {
            throw new AccessDeniedException("User uri: " + userUri + " is invalid");
        }
        if(!currentUserUri.equals(userUri)&&!userInfo.hasAuthority("ADMIN_AUTHORITY"))
        {
            throw new AccessDeniedException("User does not hold a required authority ");
        }
        // setting content type to CSV
        response.setContentType("text/csv;charset=utf-8");

        // todays date in yyyy-MM-dd HH:mm:ss format
        SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date now = new Date();
        String strDate = sdfDate.format(now);

        // defining the filename
        // TODO: give a proper file name
        response.setHeader("Content-Disposition", "attachment; filename=\"" + "User_" + fullName + "_"+ id + "_user-data_" + strDate
                + ".csv\"");

        if (!userUri.equals("")) {
            List<Event> eventList = eventService.getEventsForUserExcludingLoginLogout(userUri);

            OutputStream resOs = null;
            OutputStream buffOs = null;
            OutputStreamWriter outputwriter = null;
            CsvWriter writer = null;
            try {
                resOs = response.getOutputStream();
                buffOs = new BufferedOutputStream(resOs);
                outputwriter = new OutputStreamWriter(buffOs);

                writer = new CsvWriter(outputwriter, ',');

                writer.write("User ID");
                writer.write("Activity");
                writer.write("Activity Date");
                writer.write("Activity Time");
                writer.endRecord();
                DateTimeFormatter format = DateTimeFormat.forPattern(getAppTimeFormat());
                String userPrefferredDateFormat = getUserDateFormat();
                for (Event genericEvent : eventList) {

                    long userId = EntityId.fromUri(genericEvent.getAssociatedUserUri()).getLocalId();

                    String activityMessage = eventService.getEventTypeStringRepresentation(genericEvent, locale,
                            FormatType.USER_PROFILE);

                    DateTime timestamp = genericEvent.getEventTimestamp();

                    // writer records for all current User events
                    writer.write("" + userId);
                    writer.write("" + activityMessage);
					writer.write("" + DateUtils.getFormattedDate(timestamp, userPrefferredDateFormat));
                    writer.write("" + format.print(timestamp));

                    // end the current record
                    writer.endRecord();

                }
                outputwriter.flush();
            } finally {
                writer.close();
                outputwriter.close();
                buffOs.close();
                resOs.close();

            }
        }

    }
    
    @RequestMapping(value = "/csvViewLoginLogout", method = RequestMethod.POST)
    public void createCSVLoginLogout( Locale locale, HttpServletRequest request,
            HttpServletResponse response,@RequestParam("userUri")String userUri) throws IOException {

        UserInfo userInfo = getUserDetails();
        Long id = userInfo.getId();
        String currentUserUri = userInfo.getUserEntityId().getUri();

        String fullName = userService.getUserFullNameForUserId(id);
        if(!StringUtils.isEmpty(fullName)){
            fullName = fullName.replace(" ", "_");
        }

        if(!EntityId.isUriOfClass(userUri, User.class)) {
            throw new AccessDeniedException("User uri: " + userUri + " is invalid");
        }
        if(!currentUserUri.equals(userUri)&&!userInfo.hasAuthority("ADMIN_AUTHORITY"))
        {
            throw new AccessDeniedException("User does not hold a required authority ");
        }
        // setting content type to CSV
        response.setContentType("text/csv;charset=utf-8");

        // todays date in yyyy-MM-dd HH:mm:ss format
        SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date now = new Date();
        String strDate = sdfDate.format(now);

        // defining the filename
        // TODO: give a proper file name
        response.setHeader("Content-Disposition", "attachment; filename=\"" + "User_" + fullName + "_"+ id + "_user-data_" + strDate
                + ".csv\"");

        if (!userUri.equals("")) {
            List<Event> eventList = eventService.getAllSecurityEventsByAssociatedUseruri(userUri);
            
            OutputStream resOs = null;
            OutputStream buffOs = null;
            OutputStreamWriter outputwriter = null;
            CsvWriter writer = null;
            try {
                resOs = response.getOutputStream();
                buffOs = new BufferedOutputStream(resOs);
                outputwriter = new OutputStreamWriter(buffOs);

                writer = new CsvWriter(outputwriter, ',');

                writer.write("User ID");
                writer.write("Activity");
                writer.write("Activity Date");
                writer.write("Activity Time");
                writer.endRecord();
                DateTimeFormatter format = DateTimeFormat.forPattern(getAppTimeFormat());
                String userPrefferredDateFormat = getUserDateFormat();
                for (Event genericEvent : eventList) {
                    long userId = EntityId.fromUri(genericEvent.getAssociatedUserUri()).getLocalId();

                    String activityMessage = eventService.getEventTypeStringRepresentation(genericEvent, locale,
                            FormatType.USER_PROFILE);

                    DateTime timestamp = genericEvent.getEventTimestamp();

                    // writer records for all current User events
                    writer.write("" + userId);
                    
                    if(genericEvent instanceof UserSecurityTrailEvent) {
                    	UserSecurityTrailEvent loginLogoutEvent = (UserSecurityTrailEvent) genericEvent;
                    	activityMessage = activityMessage + "(" + loginLogoutEvent.getModuleNameForEvent() + ")";
                    }

                    writer.write("" + activityMessage);
					writer.write("" + DateUtils.getFormattedDate(timestamp, userPrefferredDateFormat));
                    writer.write("" + format.print(timestamp));

                    // end the current record
                    writer.endRecord();

                }
                outputwriter.flush();
            } finally {
                writer.close();
                outputwriter.close();
                buffOs.close();
                resOs.close();

            }
        }

    }
    
	/**
     * Hard deletes entries in Users activity stream before specified days
     * @param days days before which entries need to be hard deleted
     * @param userUri user's uri whose event needs to be deleted
     * @return page
     */
    @RequestMapping(value = "/deleteBefore", method = RequestMethod.POST)
    public String deleteBefore(@RequestParam("days") int days) {
    	String userUri= getUserUri();
        // get the date 'days' ago
        DateTime dateTime = new DateTime();
        DateTime dateBeforeDays = dateTime.minusDays(days);

        // delete events for userUri
        eventService.deleteUserEventsBeforeDate(userUri, dateBeforeDays);
        return "userProfile";
    }

    @RequestMapping(value = "/changeSecurityQuestions")
    public String changeSecurityQuestions(ModelMap modelMap) {
        
    	String username = getUserDetails().getUsername();
    	
        List<UserSecurityQuestionAnswer> userSecurityQuesAnsList = userService.getUserSecurityQuestionAnswer(username);
        UserSecurityQuestionAnswerVo userSecurityQuestionAnswerVo = new UserSecurityQuestionAnswerVo();
        userSecurityQuestionAnswerVo.setSecurityQuestionAnswerList(userSecurityQuesAnsList);
        modelMap.put("userSecurityQuestionAnswerVo", userSecurityQuestionAnswerVo);
        return "editUserSecurityQuestions";
    }

    @RequestMapping(value = "/saveSecurityQuestions", method=RequestMethod.POST)
    public @ResponseBody
    String saveSecurityQuestions(@ModelAttribute UserSecurityQuestionAnswerVo userSecurityQuestionAnswerVo) {

    	String username = getUserDetails().getUsername();
    	
    	List<UserSecurityQuestionAnswer> quesAnsList = userSecurityQuestionAnswerVo.getSecurityQuestionAnswerList();

        if (CollectionUtils.isNotEmpty(quesAnsList) && quesAnsList.size()==2) {
            userService.updateUserSecurityQuestionAnswer(username, quesAnsList);
            return "success";
        }
        return null;
    }

    private void prepareUserProfileData(ModelMap modelMap) {
		UserInfo ui = getUserDetails();
		PreferenceFormBean userPreferences = new PreferenceFormBean();
		Map<String, ConfigurationVO> preferences = configurationService
				.getFinalUserModifiableConfigurationForEntity(ui
						.getUserEntityId());

		if (preferences != null) {
			List<ConfigurationVO> configValues = new ArrayList<ConfigurationVO>();
			for (ConfigurationVO configVal : preferences.values()) {
				configValues.add(configVal);
			}
			userPreferences.setConfigVOList(configValues);

			modelMap.put("preferences", userPreferences);

		}
		String mailId = userService.getUserMailById(ui.getId());
		String uuid = ui.getUuid();
		Map<String, ?> userDetails = userManagementService.findUserByUUID(uuid);
		User user = (User) userDetails.get("user");
		UserProfile userProfile = userService.getUserProfile(user);
		if (userProfile != null) {
			modelMap.put("fullName", userProfile.getFullName());
			modelMap.put("photoUrl", userProfile.getPhotoUrl());
		}
		modelMap.put("sourceSystem", user.getSourceSystem());
		modelMap.put("lastResetDatePassword", user.getLastPasswordResetDate());
		List<Team> userTeamList = teamService
				.getTeamsAssociatedToUserByUserId(user.getId());
		List<Role> userRoles = userService.getRolesFromUserId(user.getId());

		modelMap.put("userTeamList", userTeamList);
		modelMap.put("userRoleList", userRoles);
		// User uri
		modelMap.put("userUri", user.getUri());
		String daysLeftForExpire = null;
		List<String> userAccessToBranchesList = userService
				.fetchAccessBranchesToCurrentUser(user.getId());
		if (user.getLastPasswordResetDate() != null
				&& user.getPasswordExpiresInDays() != null) {
			try {
				int numberOfDaysOfExpiringPassword = Days.daysBetween(
						user.getLastPasswordResetDate(),
						DateUtils.getCurrentUTCTime()).getDays();
				int numberOfDaysLeftToExpirePassword = (Integer.parseInt(user
						.getPasswordExpiresInDays()) - numberOfDaysOfExpiringPassword);
				daysLeftForExpire = Integer
						.toString(numberOfDaysLeftToExpirePassword);
			} catch (NumberFormatException e) {
				// nothing to do
			}
		}
		modelMap.put("passwordExpiresAfter", daysLeftForExpire);
		modelMap.put("mailId", mailId);
		modelMap.put("outOfOffice", user.getOutOfOfficeDetails()
				.isOutOfOffice());
		modelMap.put("userAccessToBranchesList", userAccessToBranchesList);
        modelMap.put("maxUserProfileImgSize",maxUserProfileImgSize);
	}
	
	    @RequestMapping(value = "/userProfileMap", method = RequestMethod.GET)
    @ResponseBody
    public Map<String,Object> loadPreferencesMap() {
        return prepareUserProfileData();
    }
	
	
	@RequestMapping(value = "/paginatedUserActivityJson/{startIndex}/{pageSize}", method = RequestMethod.GET)
    public @ResponseBody String retrievePagenatedJsonActivityForUser(ModelMap map, Locale locale, @PathVariable("startIndex") int startIndex,
                                                   @PathVariable("pageSize") int pageSize, @RequestParam("lastGroupName") String lastGroupName,
                                                   @RequestParam("eventType") String eventType) {
        map = new ModelMap();
        List<Map<String, Object>> loginLogoutActivityInfoList = new ArrayList<Map<String, Object>>();
        List<Map<String, Object>> mineActivityInfoList = new ArrayList<Map<String, Object>>();
        JSONSerializer serializer = new JSONSerializer();
        String currentUserUri = getUserDetails().getUserEntityId().getUri();
        if (!currentUserUri.equals("")) {
            if(StringUtils.isNotEmpty(eventType) && eventType.equalsIgnoreCase(loginLogoutEvent)){
                List<Event> loginLogoutEventList = eventService.getPaginatedLoginLogoutAndUserEvents(currentUserUri, startIndex, pageSize,loginLogoutEvent);
                loginLogoutEventList.forEach(genericEvent -> {
                    updateActivityinfo(locale, loginLogoutActivityInfoList, genericEvent, loginLogoutEvent);
                });
                map.put("loginLogoutActivityInfoList", loginLogoutActivityInfoList);
            }else if(StringUtils.isNotEmpty(eventType) && eventType.equalsIgnoreCase(mineEvent)) {
                List<Event> myEventList = eventService.getPaginatedLoginLogoutAndUserEvents(currentUserUri, startIndex, pageSize, mineEvent);
                myEventList.forEach(genericEvent -> {
                    updateActivityinfo(locale, mineActivityInfoList, genericEvent, mineEvent);
                });
                map.put("mineActivityInfoList", mineActivityInfoList);
            }
            map.put("lastGroupName", lastGroupName);
            map.put("currentUserDateFormat", userService.getUserPreferredDateFormat());
        }
        return serializer.deepSerialize(map);
    }

    private void updateActivityinfo(Locale locale, List<Map<String, Object>> activityInfoList, Event genericEvent, String classForJsp) {
        Map<String, Object> singleActivityInfo = new HashMap<String, Object>();
        long userId = EntityId.fromUri(genericEvent.getAssociatedUserUri()).getLocalId();
        String activityMessage = eventService.getEventTypeStringRepresentation(genericEvent, locale,
                FormatType.USER_PROFILE);
        DateTime cal = genericEvent.getEventTimestamp();
        singleActivityInfo.put("userId", userId);

        if(genericEvent instanceof UserSecurityTrailEvent) {
            UserSecurityTrailEvent loginLogoutEvent = (UserSecurityTrailEvent) genericEvent;
            activityMessage = activityMessage + "(" + loginLogoutEvent.getModuleNameForEvent() + ")";
        }
                
        singleActivityInfo.put("activityMessage", activityMessage);
        singleActivityInfo.put("dateTime", cal.toString());
        singleActivityInfo.put("groupName", assignGroup(cal));
        singleActivityInfo.put("classForJsp", classForJsp);
        activityInfoList.add(singleActivityInfo);
    }

	
	
	 /*@RequestMapping(value = "/saveOutOfOfficeDetailsFromJson", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
    public @ResponseBody String saveOutOfOfficeDetailsFromJson(@RequestBody OutOfOfficeDetailsVO outOfOfficeDetailsVo) {
        UserInfo userInfo = getUserDetails();
        User userObj = userService.findUserByUsername(userInfo.getUsername());
        DateTimeFormatter f = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");
       // DateTime dateTime = f.parseDateTime("2012-01-10 23:13:26");
        DateTime fromDate = f.parseDateTime(outOfOfficeDetailsVo.getFromDate());
        DateTime toDate = f.parseDateTime(outOfOfficeDetailsVo.getToDate());
        if (null != fromDate && null != toDate) {
            if ((fromDate.withTimeAtStartOfDay().equals(new DateTime()
                    .withTimeAtStartOfDay()))) {
                outOfOfficeDetailsVo.setOutOfOffice(true);
            }
        } else {
            outOfOfficeDetailsVo.setOutOfOffice(false);
        }

        outOfOfficeDetailsVo.setAssignedTo(outOfOfficeDetailsVo.getAssignedTo());
        outOfOfficeDetailsVo.setDelegatedToUserId(outOfOfficeDetailsVo.getDelegatedToUserId());

        OutOfOfficeDetails outOfOfficeDetails = new OutOfOfficeDetails();
        outOfOfficeDetails.setAssignedTo(outOfOfficeDetailsVo.getAssignedTo());
        outOfOfficeDetails.setDelegatedToUserId(outOfOfficeDetailsVo.getDelegatedToUserId());
        outOfOfficeDetails.setOutOfOffice(outOfOfficeDetailsVo.isOutOfOffice());
        outOfOfficeDetails.setFromDate(fromDate);
        outOfOfficeDetails.setToDate(toDate);
        userObj.setOutOfOfficeDetails(outOfOfficeDetails);
        userService.updateUser(userObj);
        flushCurrentTransaction();

        return "userOutOfOffice";
    }*/
	
	 @RequestMapping(value = "/getChangeSecurityQuestions", method = RequestMethod.GET)
    public @ResponseBody UserSecurityQuestionAnswerVo changeSecurityQuestions() {
        String username = getUserDetails().getUsername();
        List<UserSecurityQuestionAnswer> userSecurityQuesAnsList = userService.getUserSecurityQuestionAnswer(username);
        UserSecurityQuestionAnswerVo userSecurityQuestionAnswerVo = new UserSecurityQuestionAnswerVo();
        userSecurityQuestionAnswerVo.setSecurityQuestionAnswerList(userSecurityQuesAnsList);
        return userSecurityQuestionAnswerVo;
    }

	
	 private Map<String,Object> prepareUserProfileData() {
        Map<String,Object> userProfileMap = new HashMap<String,Object>();
        UserInfo ui = getUserDetails();
        String mailId = userService.getUserMailById(ui.getId());
        String uuid = ui.getUuid();
        Map<String, ?> userDetails = userManagementService.findUserByUUID(uuid);
        User user = getUser();
        userProfileMap.put("userName", user.getUsername());
        UserProfile userProfile = userService.getUserProfile(user);
        if (userProfile != null) {
            userProfileMap.put("fullName", userProfile.getFullName());
            if(userProfile.getSalutation()!=null)
                userProfileMap.put("salutation", userProfile.getSalutation().getCode());
            userProfileMap.put("photoUrl", userProfile.getPhotoUrl());
        }
        userProfileMap.put("sourceSystem", user.getSourceSystem());
        userProfileMap.put("lastResetDatePassword", user.getLastPasswordResetDate());
        List<Team> userTeamList = teamService
                .getTeamsAssociatedToUserByUserId(user.getId());
        List<Role> userRoles = userService.getRolesFromUserId(user.getId());

        List<String> userTeamNames = new ArrayList<String>();
        for(Team eachTeam : userTeamList){
            userTeamNames.add(eachTeam.getName());
        }
        List<String> userRoleNames = new ArrayList<String>();
        for(Role eachRole : userRoles){
            userRoleNames.add(eachRole.getName());
        }
        //userProfileMap.put("userTeamList", userTeamList);
        userProfileMap.put("userTeamList", new HashSet<>(userTeamNames));
        //userProfileMap.put("userRoleList", userRoles);
        userProfileMap.put("userRoleList", new HashSet<>(userRoleNames));
        // User uri
        userProfileMap.put("userUri", user.getUri());
        String daysLeftForExpire = null;
        List<String> userAccessToBranchesList = userService
                .fetchAccessBranchesToCurrentUser(user.getId());
        if (user.getLastPasswordResetDate() != null
                && user.getPasswordExpiresInDays() != null) {
            try {
                int numberOfDaysOfExpiringPassword = Days.daysBetween(
                        user.getLastPasswordResetDate(),
                        DateUtils.getCurrentUTCTime()).getDays();
                int numberOfDaysLeftToExpirePassword = (Integer.parseInt(user
                        .getPasswordExpiresInDays()) - numberOfDaysOfExpiringPassword);
                daysLeftForExpire = Integer
                        .toString(numberOfDaysLeftToExpirePassword);
            } catch (NumberFormatException e) {
                // nothing to do
            }
        }
        userProfileMap.put("passwordExpiresAfter", daysLeftForExpire);
        userProfileMap.put("mailId", mailId);
        userProfileMap.put("outOfOffice", user.getOutOfOfficeDetails()
                .isOutOfOffice());
        userProfileMap.put("userAccessToBranchesList", new HashSet<>(userAccessToBranchesList));
        userProfileMap.put("currentUserDateFormat", userService.getUserPreferredDateFormat());

         //update sec question
         Hibernate.initialize(user.getSecurityQuestionAnswers());
         if(CollectionUtils.isNotEmpty(user.getSecurityQuestionAnswers())){
             List<Map<String,String>> quesList = new ArrayList<>();
             for(UserSecurityQuestionAnswer each : user.getSecurityQuestionAnswers()){
                 Hibernate.initialize(each.getQuestion().getAuthorities());
                 Map<String,String> quesMap = new HashMap<String,String>();
                 quesMap.put("name",each.getQuestion().getName());
                 quesMap.put("code",each.getQuestion().getCode());
                 quesMap.put("desc",each.getQuestion().getDescription());
                 quesMap.put("answer",each.getAnswer());
                 quesList.add(quesMap);
             }
             userProfileMap.put("userQuesAns", quesList);
         }

         //userSecQuesList
         List<UserSecurityQuestion> userSecurityQuestionList = genericParameterService.retrieveTypes(UserSecurityQuestion.class);
         if(CollectionUtils.isNotEmpty(userSecurityQuestionList)){
             List<Map<String,String>> allSecurityQuestions = new ArrayList<>();
             for(UserSecurityQuestion each : userSecurityQuestionList){
                 Map<String,String> quesMap = new HashMap<String,String>();
                 quesMap.put("name",each.getName());
                 quesMap.put("code",each.getCode());
                 quesMap.put("desc",each.getDescription());
                 allSecurityQuestions.add(quesMap);
             }
             userProfileMap.put("allSecurityQuestions", allSecurityQuestions);
         }
         userProfileMap.put("maxUserProfileImgSize", this.maxUserProfileImgSize);
         return userProfileMap;
    }


    @RequestMapping(value = "/deleteBeforeByAjax", method = RequestMethod.GET)
    public @ResponseBody Map<String,String> deleteBeforeByAjax(@RequestParam("days") int days) {
        Map<String,String> resultMap = new HashMap<String,String>();
        String userUri= getUserUri();
        DateTime dateTime = new DateTime();
        DateTime dateBeforeDays = dateTime.minusDays(days);
        eventService.deleteUserEventsBeforeDate(userUri, dateBeforeDays);
        resultMap.put("result","success");
        return resultMap;
    }

    @RequestMapping(value = "/saveUserSecurityQuestionsJson",method=RequestMethod.POST,consumes = "application/json")
    public @ResponseBody Map<String,String>  saveUserSecurityQuestions(@RequestBody List<UserSecurityQuestionAnswerVo> userSecurityQuestionAnswerVoList) {
        Map<String,String> resultMap = new HashMap<String,String>();
        resultMap.put("result","fail");
        if(CollectionUtils.isNotEmpty(userSecurityQuestionAnswerVoList)){
            List<UserSecurityQuestionAnswerVo> userSecQuesAnsVoList = userSecurityQuestionAnswerVoList.stream().filter( e -> StringUtils.isNotEmpty(e.getAnswer())
                    && StringUtils.isNotEmpty(e.getQuestionCode())).collect(Collectors.toList());
            List<UserSecurityQuestionAnswer> userSecQuesAnsList = new ArrayList<UserSecurityQuestionAnswer>();
            if(userSecQuesAnsVoList.size()!=2) {
            	return resultMap;
            }
            userSecQuesAnsVoList.forEach( e -> {
                UserSecurityQuestion userSecurityQuestion = genericParameterService.findByCode(e.getQuestionCode(),UserSecurityQuestion.class);
                if(userSecurityQuestion != null){
                    UserSecurityQuestionAnswer userSecurityQuestionAnswer = new UserSecurityQuestionAnswer();
                    userSecurityQuestionAnswer.setQuestion(userSecurityQuestion);
                    userSecurityQuestionAnswer.setAnswer(e.getAnswer());
                    userSecQuesAnsList.add(userSecurityQuestionAnswer);
                }
            });
            if(userSecQuesAnsList.size()==2){
                User user = getUser();
                user.setSecurityQuestionAnswers(userSecQuesAnsList);
                resultMap.put("result","success");
            } 
        }

        return resultMap;
    }

    private User getUser(){
        UserInfo ui = getUserDetails();
        String uuid = ui.getUuid();
        Map<String, ?> userDetails = userManagementService.findUserByUUID(uuid);
        return (User) userDetails.get("user");
    }
}
