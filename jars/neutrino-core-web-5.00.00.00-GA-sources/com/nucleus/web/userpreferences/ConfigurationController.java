package com.nucleus.web.userpreferences;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import com.nucleus.core.genericparameter.service.GenericParameterService;
import com.nucleus.core.initialization.ProductInformationLoader;
import com.nucleus.core.locale.LanguageInfoReader;

import com.nucleus.event.*;
import com.nucleus.finnone.pro.base.utility.CoreUtility;
import com.nucleus.event.UserPreferencesEvent;
import net.bull.javamelody.MonitoredWithSpring;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.reflections.Reflections;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.nucleus.config.persisted.enity.Configuration;
import com.nucleus.config.persisted.service.ConfigurationService;
import com.nucleus.config.persisted.vo.ConfigurationVO;
import com.nucleus.config.persisted.vo.MyFavorites;
import com.nucleus.config.persisted.vo.ValueType;
import com.nucleus.core.team.entity.Team;
import com.nucleus.core.team.service.TeamService;
import com.nucleus.logging.BaseLoggers;
import com.nucleus.master.BaseMasterService;
import com.nucleus.user.*;
import com.nucleus.web.common.controller.BaseController;

import flexjson.JSONSerializer;
import flexjson.transformer.AbstractTransformer;
import flexjson.transformer.Transformer;

@Controller
//@SessionAttributes("preferences")
public class ConfigurationController extends BaseController {

    @Inject
    @Named("userSessionManagerService")
    private UserSessionManagerService userSessionManagerService;

    @Inject
    @Named("configurationService")
    protected ConfigurationService    configurationService;

    @Inject
    @Named("teamService")
    protected TeamService             teamService;

    @Inject
    @Named("userService")
    protected UserService             userService;

    @Inject
    @Named("baseMasterService")
    private BaseMasterService         baseMasterService;

    @Inject
    @Named("genericParameterService")
    private GenericParameterService genericParameterService;
    
    @Inject
    @Named("languageInfoReader")
    private LanguageInfoReader languageInfoReader;

    @Inject
    @Named("eventBus")
    private EventBus eventBus;

    @Autowired
    private EventService eventService;

    private final Transformer         dateTransformer = new AbstractTransformer() {
        @Override
        public void transform(Object object) {
            getContext().write(
                    "\"" + getFormattedDate((DateTime) object) + "\"");
        }
    };

    public static final String CONFIGURATION_QUERY = "Configuration.getPropertyValueFromPropertyKey";


    @RequestMapping(value = "/configuration/load", method = RequestMethod.POST)
    public String loadPreferences(ModelMap modelMap,HttpServletRequest request) {
        Map<String, ConfigurationVO> userPreferencesMap = configurationService
                .getFinalUserModifiableConfigurationForEntity(getUserDetails().getUserEntityId());
        PreferenceFormBean userPreferences = (PreferenceFormBean)request.getSession().getAttribute(Configuration.PREFERENCES);
        if (userPreferences == null) {
            userPreferences = new PreferenceFormBean();
        }
        Map<String, ConfigurationVO> sortedUserPreferencesMap = new TreeMap<String, ConfigurationVO>(userPreferencesMap);

        userPreferences.setConfigVOList(new ArrayList<ConfigurationVO>(sortedUserPreferencesMap.values()));
        List<ConfigurationVO> configCAVOList =  new ArrayList<ConfigurationVO>();
        for(ConfigurationVO configVO :userPreferences.getConfigVOList()){
            if(configVO.getPropertyKey().startsWith("config.CAStage")){
                configCAVOList.add(configVO);
            }
        }
        if(CollectionUtils.isNotEmpty(userPreferences.getConfigVOList())){
            userPreferences.getConfigVOList().removeAll(configCAVOList);
        }
        userPreferences.setConfigVOCAList(configCAVOList);
        request.getSession().setAttribute(Configuration.PREFERENCES,userPreferences);
        modelMap.put(Configuration.PREFERENCES, userPreferences);
        List<String> supportedDateFormats = Arrays.asList(configurationService.getPropertyValueByPropertyKey("config.supportedDateFormats",CONFIGURATION_QUERY).split(","));
        modelMap.put("supportedDateFormats",supportedDateFormats);
        Map<String, List<String>> gridsForDefaultTabPref = new HashMap<>();
        gridsForDefaultTabPref = defaultGridTabs();
        modelMap.put("gridsForDefaultTabPref", gridsForDefaultTabPref);

        return "userPreferences";
    }

    @RequestMapping(value = "/configuration/save", method = RequestMethod.POST)
    @ResponseBody
    public String savePreferences(@RequestParam(value = "preferredTheme", required = false) String preferredTheme,
                                  PreferenceFormBean preferences,
                                  @ModelAttribute(value = "preferences") PreferenceFormBean userPreferencesSession, ModelMap modelMap, HttpServletRequest request) {
        return savePref(preferredTheme, preferences, userPreferencesSession, modelMap, request);
    }

    private String savePref(String preferredTheme,
                            PreferenceFormBean preferences, PreferenceFormBean userPreferencesSession, ModelMap modelMap, HttpServletRequest request) {
        String msg = "Error Saving Preferences";
        List<ConfigurationVO> configVOList = new ArrayList<ConfigurationVO>();
        String originalConfigPropertyKey;
        if(preferences == null){
            return "No Preference To Save";
        }
        UserInfo userInfo = getUserDetails();
        Map<String, ConfigurationVO> userPreferences = configurationService
                .getFinalUserModifiableConfigurationForEntity(userInfo.getUserEntityId());
        if(CollectionUtils.isNotEmpty(preferences.getConfigVOCAList()) && preferences.getConfigVOList()!= null){
            preferences.getConfigVOList().addAll(preferences.getConfigVOCAList());
        }
        if(CollectionUtils.isNotEmpty(preferences.getConfigVOList())){
            for (ConfigurationVO configVO : preferences.getConfigVOList()) {
                ConfigurationVO orgConfig = userPreferences.get(configVO.getPropertyKey());
                originalConfigPropertyKey =  orgConfig.getPropertyKey();
                if (orgConfig.getValueType().toString().equalsIgnoreCase(ValueType.NORMAL_TEXT.toString())){
                    updateNormalTextConfigPreference(orgConfig, configVO, originalConfigPropertyKey, preferredTheme);
                }
                else if (orgConfig.getValueType().toString().equalsIgnoreCase(ValueType.BOOLEAN_VALUE.toString())){
                    updateBooleanConfigPreference(orgConfig, configVO, originalConfigPropertyKey);
                }
                else if (orgConfig.getValueType().toString().equalsIgnoreCase(ValueType.DASHBOARD.toString())){
                    if("config.dashboard.commentWidget".equalsIgnoreCase(configVO.getPropertyKey())){
                        configVO.getPropertyValue();
                    }
                    updateDashboardConfigPreference(orgConfig, configVO, originalConfigPropertyKey);
                }
                else{
                    updateDateAndTimeConfigPreference(orgConfig, configVO);
                }
                configVOList.add(orgConfig);
            }
        }
        configurationService.syncConfiguration(userInfo.getUserEntityId(), configVOList);
        Map<String, ConfigurationVO> updatedPreferences = configurationService
                .getFinalUserModifiableConfigurationForEntity(userInfo.getUserEntityId());

        userInfo.setUserPreferences(updatedPreferences);
        CoreUtility.syncSecurityContextHolderInSession(userInfo.getMappedSessionId());
        if (userPreferencesSession != null) {
            List<ConfigurationVO> configCAVOList = new ArrayList<ConfigurationVO>();
            if(CollectionUtils.isNotEmpty(preferences.getConfigVOCAList())){
                for(ConfigurationVO configCAVO : preferences.getConfigVOCAList()){
                    for(ConfigurationVO orgConfigVO : configVOList){
                        if(orgConfigVO.getPropertyKey().equals(configCAVO.getPropertyKey())){
                            configCAVOList.add(orgConfigVO);
                        }
                    }
                }
            }
            configVOList.removeAll(configCAVOList);
            userPreferencesSession.setConfigVOList(configVOList);
            userPreferencesSession.setConfigVOCAList(configCAVOList);
        }
        modelMap.put(Configuration.PREFERENCES, userPreferencesSession);
        msg = "Data Saved Sucessfully";
        UserPreferencesEvent event = new UserPreferencesEvent(EventTypes.USER_PREFERENCES_UPDATED_EVENT);
        HttpSession session = request.getSession(false);
        event.setSessionId(session.getId());
        event.setUsername(userInfo.getUsername());
        event.setRemoteIpAddress(
                ((WebAuthenticationDetails) ((SecurityContextImpl) session.getAttribute("SPRING_SECURITY_CONTEXT")).getAuthentication().getDetails())
                        .getRemoteAddress());
        event.setModuleNameForEvent(ProductInformationLoader.getProductName());
        eventService.createEventEntry(event);
        eventBus.fireEvent(event);
        return msg;
    }

    private void updateNormalTextConfigPreference(ConfigurationVO orgConfig,
                                                  ConfigurationVO configVO, String originalConfigPropertyKey, String preferredTheme) {
        if (StringUtils.isNotBlank(preferredTheme)
                && Configuration.THEME_PREFERRED_THEME.equalsIgnoreCase(originalConfigPropertyKey)) {
            orgConfig.setText(preferredTheme);
        }
        else if (Configuration.NORMAL_TEXT_CONFIG_LIST.contains(originalConfigPropertyKey)) {
            orgConfig.setText(configVO.getText());
        }
        else if (Configuration.REQ_ALL_FIELDS_PREF.contains(originalConfigPropertyKey)) {
            orgConfig.setText(configVO.getText());
        }
    }

    private void updateDashboardConfigPreference(ConfigurationVO orgConfig,
                                                 ConfigurationVO configVO, String originalConfigPropertyKey) {
        if (Configuration.DASHBOARD_COMMENT_WIDGET.equalsIgnoreCase(originalConfigPropertyKey)) {
            orgConfig.setCommentWidget(configVO.getCommentWidget());
        } else if (Configuration.DASHBOARD_STREAM_WIDGET.equalsIgnoreCase(originalConfigPropertyKey)) {
            orgConfig.setStreamWidget(configVO.getStreamWidget());
        } else if (Configuration.DASHBOARD_APP_COUNT_BY_PRODUCT_TYPE_WIDGET.equalsIgnoreCase(originalConfigPropertyKey)) {
            orgConfig.setAppCountByProductTypeWidget(configVO.getAppCountByProductTypeWidget());
        } else if (Configuration.DASHBOARD_APP_COUNT_BY_STAGE_WIDGET.equalsIgnoreCase(originalConfigPropertyKey)) {
            orgConfig.setAppCountByStageWidget(configVO.getAppCountByStageWidget());
        } else if (Configuration.DASHBOARD_NOTES_WIDGET.equalsIgnoreCase(originalConfigPropertyKey)) {
            orgConfig.setNotesWidget(configVO.getNotesWidget());
        } else if (Configuration.DASHBOARD_LEAD_COUNT_BY_CITY_WIDGET.equalsIgnoreCase(originalConfigPropertyKey)) {
            orgConfig.setLeadCountByCityWidget(configVO.getLeadCountByCityWidget());
        } else if (Configuration.DASHBOARD_LEAD_COUNT_BY_CONVERSION_WIDGET.equalsIgnoreCase(originalConfigPropertyKey)) {
            orgConfig.setLeadCountByConversionWidget(configVO.getLeadCountByConversionWidget());
        } else if (Configuration.DASHBOARD_LEAD_COUNT_BY_TAT_WIDGET.equalsIgnoreCase(originalConfigPropertyKey)) {
            orgConfig.setLeadCountByTatWidget(configVO.getLeadCountByTatWidget());
        } else if (Configuration.DASHBOARD_LEAD_COUNT_BY_DUE_TODAY_WIDGET.equalsIgnoreCase(originalConfigPropertyKey)) {
            orgConfig.setLeadCountByDueTodayWidget(configVO.getLeadCountByDueTodayWidget());
        } else if (Configuration.DASHBOARD_LEAD_COUNT_BY_STATUS_WIDGET.equalsIgnoreCase(originalConfigPropertyKey)) {
            orgConfig.setLeadCountByStatusWidget(configVO.getLeadCountByStatusWidget());
        } else if (Configuration.DASHBOARD_RECENT_MAILS.equalsIgnoreCase(originalConfigPropertyKey)) {
            orgConfig.setRecentMails(configVO.getRecentMails());
        }
    }

    private void updateBooleanConfigPreference(ConfigurationVO orgConfig,
                                               ConfigurationVO configVO, String originalConfigPropertyKey) {
        if (Configuration.BOOLEAN_VALUE_CONFIG_LIST.contains(originalConfigPropertyKey)) {
            orgConfig.setConfigurable(configVO.getConfigurable());
        } else {
            orgConfig.setConfigurable(configVO.isConfigurable());
        }
    }

    private void updateDateAndTimeConfigPreference(ConfigurationVO orgConfig, ConfigurationVO configVO) {
        if (orgConfig.getValueType().toString().equalsIgnoreCase(ValueType.DATE.toString())
                || orgConfig.getValueType().toString().equalsIgnoreCase(ValueType.TIME.toString())) {
            orgConfig.setDate(configVO.getDate());
        } else if (orgConfig.getValueType().toString().equalsIgnoreCase(ValueType.DATE_RANGE.toString())
                || orgConfig.getValueType().toString().equalsIgnoreCase(ValueType.TIME_RANGE.toString())) {
            orgConfig.setFromDate(configVO.getFromDate());
            orgConfig.setToDate(configVO.getToDate());
        } else if (orgConfig.getValueType().toString().equalsIgnoreCase(ValueType.DAY_OF_WEEK.toString())) {
            orgConfig.setDay(configVO.getDay());
        } else if (orgConfig.getValueType().toString().equalsIgnoreCase(ValueType.DAYS_OF_WEEK_RANGE.toString())) {
            orgConfig.setFromDay(configVO.getFromDay());
            orgConfig.setToDay(configVO.getToDay());
        }
    }

    @RequestMapping(value = "/myFav", method = RequestMethod.GET)
    @MonitoredWithSpring(name = "CC_LOAD_USER_FAV_CONFIG")
    public String loadMyFav(ModelMap map) {
        List<MyFavorites> myFavoritesSelect = baseMasterService.getAllApprovedAndActiveEntities(MyFavorites.class);
        BaseLoggers.flowLogger.debug("MyFavoritesList " + myFavoritesSelect);
        map.put("myFavoritesSelect", myFavoritesSelect);
        return "myFavData";
    }

    @PreAuthorize("hasAuthority('CHAT_ENABLED')")
    @RequestMapping(value = "/loadLoggedinUsers", method = RequestMethod.GET)
    @MonitoredWithSpring(name = "CC_LOAD_LOGGED_IN_USRS")
    public String loadAllLoggedinUsers(ModelMap map) {
        List<UserInfo> userList = userSessionManagerService.getAllLoggedInUsers();
        // Remove current user from user list to be shown in Contacts SideBar..
        userList.remove(getUserDetails());

        List<Boolean> userChatEnabled = new ArrayList<Boolean>();
        for (UserInfo user : userList) {
            if (user.isChatEnabled()) {
                userChatEnabled.add(true);
            } else
                userChatEnabled.add(false);
        }
        BaseLoggers.flowLogger.debug("UserList " + userList);
        map.put(Configuration.USER_LIST, userList);
        map.put("userChatEnabledList", userChatEnabled);
        return Configuration.LOGGED_IN_USERS;
    }

    @PreAuthorize("hasAuthority('CHAT_ENABLED')")
    @RequestMapping(value = "/addRecipient/{user}", method = RequestMethod.GET)
    public String addRecipient(ModelMap map, @PathVariable("user") String username) {

        return "redirect:/app/email/sendMail/" + username;
    }

    @RequestMapping(value = "/configuration/getConfig", method = RequestMethod.GET)
    @MonitoredWithSpring(name = "CC_FETCH_CONFIGS")
    @ResponseBody
    public String getConfigurations(ModelMap modelMap) {

        PreferenceFormBean userPreferences = new PreferenceFormBean();
        Map<String, ConfigurationVO> preferences = getUserDetails().getUserPreferences();
        List<ConfigurationVO> configValues = new ArrayList<ConfigurationVO>();
        if (preferences != null) {

            for (ConfigurationVO configVal : preferences.values()) {
                configValues.add(configVal);
            }
            userPreferences.setConfigVOList(configValues);
            modelMap.put(Configuration.PREFERENCES, userPreferences);

        }
        String jsonString = new JSONSerializer().exclude("*.class").transform(dateTransformer, DateTime.class)
                .deepSerialize(configValues);
        return jsonString;
    }

    @RequestMapping(value = "/configuration/setConfig", method = RequestMethod.POST)
    @ResponseBody
    public String setTooltipPreference(@RequestParam("key") String propertyKey, @RequestParam("value") Boolean propertyValue,
                                       HttpServletRequest request) {
        String msg = "Error Saving Preferences";
        List<ConfigurationVO> configVOList = new ArrayList<ConfigurationVO>();
        UserInfo userInfo = getUserDetails();
        Map<String, ConfigurationVO> userPreferencesMap = configurationService
                .getFinalUserModifiableConfigurationForEntity(userInfo.getUserEntityId());
        for (String key : userPreferencesMap.keySet()) {
            ConfigurationVO configVO = userPreferencesMap.get(key);
            if (propertyKey.equalsIgnoreCase(key)) {
                configVO.setConfigurable(propertyValue);
                userPreferencesMap.put(key, configVO);
                userInfo.setUserPreferences(userPreferencesMap);
            }
            configVOList.add(configVO);
        }
        PreferenceFormBean userPreferences = (PreferenceFormBean)request.getSession().getAttribute(Configuration.PREFERENCES);
        if (userPreferences == null) {
            userPreferences = new PreferenceFormBean();
        }
        userPreferences.setConfigVOList(configVOList);

        configurationService.syncConfiguration(userInfo.getUserEntityId(), configVOList);
        CoreUtility.syncSecurityContextHolderInSession(userInfo.getMappedSessionId());
        request.getSession().setAttribute(Configuration.PREFERENCES,userPreferences);
        msg = "Data Saved Sucessfully";
        return msg;
    }

    /**
     * @description to return all logged in Users filtered by user Teams
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    @PreAuthorize("hasAuthority('CHAT_ENABLED')")
    @RequestMapping(value = "/loadLoggedinUsersByTeam", method = RequestMethod.GET)
    public String loadAllLoggedinUsersByTeam(ModelMap map) {
        List<UserInfo> loggedInUsersList = userSessionManagerService.getAllLoggedInUsers();
        Set<UserInfo> allLoggedInUsers = new HashSet<UserInfo>(loggedInUsersList);
        List<Team> teamAssociatedWithUser = teamService.getTeamsOfUserInLoggedInBranch(getUserDetails());
        Set<UserInfo> allUsersOfTeams = new HashSet<UserInfo>();
        for (Team team : teamAssociatedWithUser) {
            allUsersOfTeams.addAll(teamService.getAssociatedUsersOfTeamByTeamId(team.getId()));
        }
        List<UserInfo> userList = new ArrayList(CollectionUtils.intersection(allLoggedInUsers, allUsersOfTeams));
        // Remove current user from user list to be shown in Contacts SideBar..
        userList.remove(getUserDetails());

        map.put(Configuration.USER_LIST, userList);
        return Configuration.LOGGED_IN_USERS;
    }

    /**
     * @description to return all logged in Users filtered by user logged in branch
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    @PreAuthorize("hasAuthority('CHAT_ENABLED')")
    @RequestMapping(value = "/loadLoggedinUsersByBranch", method = RequestMethod.GET)
    public String loadAllLoggedinUsersByBranch(ModelMap map) {
        List<UserInfo> loggedInUsersList = userSessionManagerService.getAllLoggedInUsers();
        Set<UserInfo> allLoggedInUsers = new HashSet<UserInfo>(loggedInUsersList);
        List<Team> teamAssociatedWithUser = teamService.getAllTeamsOfLoggedInBranchOfThisUser(getUserDetails());
        Set<UserInfo> allUsersOfTeams = new HashSet<UserInfo>();
        for (Team team : teamAssociatedWithUser) {
            allUsersOfTeams.addAll(teamService.getAssociatedUsersOfTeamByTeamId(team.getId()));
        }

        List<UserInfo> userList = new ArrayList(CollectionUtils.intersection(allLoggedInUsers, allUsersOfTeams));
        // Remove current user from user list to be shown in Contacts SideBar..
        userList.remove(getUserDetails());

        map.put(Configuration.USER_LIST, userList);
        return Configuration.LOGGED_IN_USERS;
    }

    @RequestMapping(value = "/configuration/saveEvent", method = RequestMethod.POST)
    @ResponseBody
    public String saveConfigurableEvent(ModelMap map, HttpServletRequest request,
                                        @RequestParam("selectedEvents")String selectedEvents) throws Exception {

        Configuration configuration = configurationService
                .getPropertyObjectByPropertyKey();

        if (configuration == null) {
            configuration = new Configuration();
            configuration.setPropertyKey(Configuration.NOTIFY_EVENT);
        }
        if(StringUtils.isNotBlank(selectedEvents))
            configuration.setPropertyValue(selectedEvents);
        else
            configuration.setPropertyValue(null);
        configurationService.saveConfiguration(configuration);
        JSONSerializer iSerializer = new JSONSerializer();
        List<Map<String, Object>> recordList = new ArrayList<Map<String, Object>>();
        Map<String, Object> hm = new HashMap<String, Object>();
        hm.put("success", Configuration.NOTIFY_EVENT_FOR_LIST+" "+selectedEvents);
        recordList.add(hm);
        String jsonString = iSerializer.deepSerialize(recordList);

        return jsonString;

    }

    @RequestMapping(value = "/configuration/getEventChoice/{showEdit}", method = RequestMethod.GET)
    public String getEventChoiceJspPage(ModelMap modelMap,@PathVariable("showEdit") int showEdit) {
        List<EventName> eventList=new ArrayList<EventName>();
        int size=0;
        List<String> selectedEventList=new ArrayList<String>();
        Configuration configuration=configurationService.getPropertyObjectByPropertyKey();
        if(configuration!=null )
        {
            if(configuration.getPropertyValue()!=null)
            {
                String ar[]=configuration.getPropertyValue().split(",");
                selectedEventList = Arrays.asList(ar);
                size=selectedEventList.size();
            }
            if(showEdit==1)
            {
                modelMap.put("editButton", true);
                modelMap.put("viewable", true);
            }
            else
                modelMap.put("viewable", false);
        }

        String[] selectEvent=new String[size];
        Reflections reflections = new Reflections(Configuration.PACKAGE_NAME);
        Set<Class<? extends GenericEvent>> subTypes =reflections.getSubTypesOf(GenericEvent.class);
        Iterator itr =subTypes.iterator();
        int i=0;
        while(itr.hasNext())
        {
            String classNameAndPkg=(itr.next().toString());
            String className=classNameAndPkg.substring(classNameAndPkg.lastIndexOf(".")+1);
            EventName eventObj=new EventName();
            eventObj.setEventName(className);
            if(configuration!=null)
            {
                if(selectedEventList.contains(className))
                {
                    selectEvent[i]=className;
                    i++;
                }
            }
            eventList.add(eventObj);
        }
        if(configuration!=null)
        {
            configuration.setEventsValue(selectEvent);
        }
        else
        {
            configuration=new Configuration();
            modelMap.put("editButton", false);
        }
        modelMap.put("eventList", eventList);
        modelMap.put("configuration", configuration);
        return "notificationChoice";
    }

    @RequestMapping(value = "/configuration/getMobileValidationConfig", method = {RequestMethod.GET,RequestMethod.POST})
    @ResponseBody
    public String getMobileValidationConfiguration(ModelMap modelMap) {
        String mobileValidatorConfig = configurationService.getPropertyValueByPropertyKey(Configuration.CUSTOM_MOBILE_VALIDATION, Configuration.GET_NOTIFICATION_EVENT_QUERY);
        return mobileValidatorConfig;
    }

    @RequestMapping(value = "/configuration/validateMobileNumber", method = {RequestMethod.GET,RequestMethod.POST})
    @ResponseBody
    public Boolean validateMobileNumber(@RequestParam(value = "countryCode", required = true) String ISDCode, @RequestParam(value = "mobileNumber", required = true) String mobileNumber) {
        return configurationService.validateMobileNumber(ISDCode,mobileNumber);
    }

    @RequestMapping(value = "/configuration/allowInvalidNumber", method = {RequestMethod.GET,RequestMethod.POST})
    @ResponseBody
    public String allowInvalidNumber(ModelMap modelMap) {
        String invalidNumberConfig = configurationService.getPropertyValueByPropertyKey(Configuration.ALLOW_INVALID_PHONE_NUMBER, Configuration.GET_NOTIFICATION_EVENT_QUERY);
        return invalidNumberConfig;
    }

    @RequestMapping(value = "/configuration/loadPreferencesJson", method = RequestMethod.GET)
    public @ResponseBody Map<String,Object> loadPreferencesJson(HttpServletRequest request) {
        Map<String,Object> prefMap = new HashMap<String,Object>();
        String preferredTheme = "";
        Map<String, ConfigurationVO> userPreferencesMap = configurationService
                .getFinalUserModifiableConfigurationForEntity(getUserDetails().getUserEntityId());
        PreferenceFormBean userPreferences = (PreferenceFormBean)request.getSession().getAttribute(Configuration.PREFERENCES);
        if (userPreferences == null) {
            userPreferences = new PreferenceFormBean();
        }
        Map<String, ConfigurationVO> sortedUserPreferencesMap = new TreeMap<String, ConfigurationVO>(userPreferencesMap);

        userPreferences.setConfigVOList(new ArrayList<ConfigurationVO>(sortedUserPreferencesMap.values()));
        List<ConfigurationVO> configNotificationVOList =  new ArrayList<ConfigurationVO>();
        List<ConfigurationVO> configCAVOList =  new ArrayList<ConfigurationVO>();
        List<ConfigurationVO> dashboardWidgetsList =  new ArrayList<ConfigurationVO>();
        List<String> dashboardWidgets = new ArrayList<String>();
        Locale locale = userService.getUserLocale();
        for(ConfigurationVO configVO :userPreferences.getConfigVOList()){
            configVO.setLabel(messageSource.getMessage(configVO.getPropertyKey()+".label.key", null, userService.getUserLocale()));
            if(configVO.getPropertyKey().startsWith("config.CAStage")){
                //remove null elements from json
                /*ConfigurationVO configurationVO = new ConfigurationVO();
                configurationVO.setId(configVO.getId());
                configurationVO.setConfigurable(configVO.getConfigurable());
                configurationVO.setPropertyKey(configVO.getPropertyKey());
                configurationVO.setPropertyValue(configVO.getPropertyValue());
                configurationVO.setValueType(configVO.getValueType());*/
                configCAVOList.add(configVO);
            }if(configVO.getPropertyKey().startsWith("config.notifications")){
                //remove null elements from json
                /*ConfigurationVO configurationVO = new ConfigurationVO();
                configurationVO.setId(configVO.getId());
                configurationVO.setConfigurable(configVO.getConfigurable());
                configurationVO.setPropertyKey(configVO.getPropertyKey());
                configurationVO.setPropertyValue(configVO.getPropertyValue());
                configurationVO.setValueType(configVO.getValueType());*/
                configNotificationVOList.add(configVO);
            }else if(configVO.getPropertyKey().startsWith("config.dashboard.")){
                if(configVO.getPropertyValue().equalsIgnoreCase("enable")){
                    dashboardWidgets.add(configVO.getPropertyKey().split("\\.")[2]);
                }
                dashboardWidgetsList.add(configVO);
            }else if(configVO.getPropertyKey().equalsIgnoreCase("config.theme.preferredTheme")) {
                preferredTheme = configVO.getText();
                if(preferredTheme == "" || preferredTheme == null || preferredTheme == "null"){
                    preferredTheme="bootstrap";
                }
            }
        }
        if(CollectionUtils.isNotEmpty(userPreferences.getConfigVOList())){
            userPreferences.getConfigVOList().removeAll(configCAVOList);
            if(CollectionUtils.isNotEmpty(configNotificationVOList))
                userPreferences.getConfigVOList().removeAll(configNotificationVOList);
            if(CollectionUtils.isNotEmpty(dashboardWidgetsList))
                userPreferences.getConfigVOList().removeAll(dashboardWidgetsList);
        }
        userPreferences.setConfigVOCAList(configCAVOList);
        request.getSession().setAttribute(Configuration.PREFERENCES,userPreferences);
        prefMap.put(Configuration.PREFERENCES, userPreferences);
        List<String> supportedDateFormats = Arrays.asList(configurationService.getPropertyValueByPropertyKey("config.supportedDateFormats",CONFIGURATION_QUERY).split(","));
        prefMap.put("supportedDateFormats",supportedDateFormats);
        prefMap.put("gridsForDefaultTabPref",defaultGridTabs());
        prefMap.put("timeZones",new ArrayList(DateTimeZone.getAvailableIDs()));
        prefMap.put("preferredTheme",preferredTheme);
        prefMap.put("dashboardWidgets",dashboardWidgets);
        prefMap.put("dashboardWidgetsList",dashboardWidgetsList);
        prefMap.put("configNotificationVOList",configNotificationVOList);
        prefMap.put("supportedUserLocales",languageInfoReader.getAvailableLanguageInfo());
        prefMap.put("userLoanProductList",getUserDetails().getLoanProductInfoList());
        return prefMap;
    }


    @RequestMapping(value = "/configuration/saveJson", method = RequestMethod.POST,consumes = "application/json")
    @ResponseBody
    public Map<String,String> savePreferencesJson(@RequestParam(value = "preferredTheme", required = false) String preferredTheme,
                                                  @RequestBody PreferenceFormBean preferences,
                                                  @ModelAttribute(value = "preferences") PreferenceFormBean userPreferencesSession, ModelMap modelMap, HttpServletRequest request) {
        Map<String,String> resMap = new HashMap<>();
        resMap.put("result","failure");
        String res = savePref(preferredTheme, preferences, userPreferencesSession, modelMap, request);
        if(StringUtils.isNotEmpty(res)){
            resMap.put("result","success");
            request.getSession().setAttribute("updateUserPreferences", "true");
        }
        return resMap;
    }


    private Map defaultGridTabs(){
        List<String> listOfTabs = new ArrayList<>();
        List<UserGridPref> userGridPref = genericParameterService.retrieveTypes(UserGridPref.class);
        Map<String, List<String>> gridsForDefaultTabPref = new HashMap<>();
        for(UserGridPref u : userGridPref){

            listOfTabs.addAll(getTabsFromGridName(u.getCode()));
            gridsForDefaultTabPref.put(u.getCode(), listOfTabs);
            listOfTabs = new ArrayList<>();

        }

        return gridsForDefaultTabPref;

    }


    private List getTabsFromGridName(String gridName) {
        Map<String, List<String>> gridTabMap = new HashMap<>();
        List<String> listOfTabs = new ArrayList<>();
        List<String> gridTabNamesList = new ArrayList<>();
        for(GridTabNames gridTabName : genericParameterService.retrieveTypes(GridTabNames.class)){
            listOfTabs.addAll(Arrays.asList(gridTabName.getParentCode().split(",")));
            if(listOfTabs.contains(gridName)) {
                gridTabNamesList.add(gridTabName.getName());
            }
            //gridTabMap.put(gridTabNames.getName(), listOfTabs);
            listOfTabs = new ArrayList<>();
        }
        return gridTabNamesList;
    }


}