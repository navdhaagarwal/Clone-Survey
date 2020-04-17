/*
 * This file and a proportion of its content is copyright of Nucleus Software Exports Limited - � 2012. All rights reserved.
 * Any redistribution or reproduction of part or all of the contents in any form is prohibited other than the following:
 * - you cannot print or download to a local hard disk extract contents either part or full for personal/ commercial/
 * academic or any other use
 * - you may not copy the content to individual/ third parties for any type of use, either as compiled or source format
 * without the knowledge and consent of Nucleus SOftware
 * - You may not, except with our express written permission, distribute or commercially exploit the content. Nor may you
 * transmit it or store it in any other web site or other form of electronic retrieval system.
 */

package com.nucleus.web.useradministration;

import com.google.gson.Gson;
import com.nucleus.address.Address;
import com.nucleus.address.AddressInitializer;
import com.nucleus.address.AddressTagService;
import com.nucleus.address.Area;
import com.nucleus.address.City;
import com.nucleus.address.State;
import com.nucleus.authenticationToken.AuthenticationTokenService;
import com.nucleus.businessmapping.entity.UserOrgBranchMapping;
import com.nucleus.businessmapping.service.BusinessMappingServiceCore;
import com.nucleus.businessmapping.service.UserBPMappingService;
import com.nucleus.businessmapping.service.UserManagementServiceCore;
import com.nucleus.cas.businessmapping.UserManagementService;
import com.nucleus.cas.businessmapping.UserOrgBranchProdSchemeMapping;
import com.nucleus.cfi.mail.service.MailMessageIntegrationService;
import com.nucleus.config.persisted.enity.Configuration;
import com.nucleus.config.persisted.service.ConfigurationService;
import com.nucleus.config.persisted.vo.ConfigurationVO;
import com.nucleus.config.persisted.vo.ValueType;
import com.nucleus.contact.EMailInfo;
import com.nucleus.contact.SimpleContactInfo;
import com.nucleus.core.genericparameter.service.GenericParameterServiceImpl;
import com.nucleus.core.organization.calendar.BranchCalendar;
import com.nucleus.core.organization.entity.OrganizationBranch;
import com.nucleus.core.organization.entity.OrganizationType;
import com.nucleus.core.organization.entity.SystemName;
import com.nucleus.core.organization.service.OrganizationService;
import com.nucleus.core.role.entity.Role;
import com.nucleus.core.team.entity.Team;
import com.nucleus.core.team.managementService.TeamManagementService;
import com.nucleus.core.team.service.TeamService;
import com.nucleus.core.user.event.UserEvent;
import com.nucleus.core.validation.util.NeutrinoValidator;
import com.nucleus.core.villagemaster.entity.VillageMaster;
import com.nucleus.entity.ApprovalStatus;
import com.nucleus.entity.BaseEntity;
import com.nucleus.entity.EntityId;
import com.nucleus.entity.SystemEntity;
import com.nucleus.event.Event;
import com.nucleus.event.EventBus;
import com.nucleus.event.EventService;
import com.nucleus.event.EventTypes;
import com.nucleus.event.UserSecurityTrailEvent;
import com.nucleus.finnone.pro.base.utility.CoreUtility;
import com.nucleus.finnone.pro.general.util.ValidatorUtils;
import com.nucleus.license.cache.LicenseClientCacheService;
import com.nucleus.license.content.model.LicenseDetail;
import com.nucleus.license.pojo.LicenseMobilityModuleInfo;
import com.nucleus.logging.BaseLoggers;
import com.nucleus.mail.MailService;
import com.nucleus.makerchecker.MakerCheckerService;
import com.nucleus.menu.IMenuService;
import com.nucleus.menu.MenuVO;
import com.nucleus.persistence.HibernateUtils;
import com.nucleus.process.BPMNProcessService;
import com.nucleus.rules.model.SourceProduct;
import com.nucleus.security.oauth.service.RESTfulAuthenticationService;
import com.nucleus.taskCount.TaskCountRequest;
import com.nucleus.taskCount.TaskCountResponse;
import com.nucleus.user.AccessType;
import com.nucleus.user.OrgBranchInfo;
import com.nucleus.user.OrgBranchTree;
import com.nucleus.user.OutOfOfficeDetails;
import com.nucleus.user.RecordComparatorVO;
import com.nucleus.user.User;
import com.nucleus.user.UserAuditLog;
import com.nucleus.user.UserBranchProductService;
import com.nucleus.user.UserCalendar;
import com.nucleus.user.UserCityMapping;
import com.nucleus.user.UserCityVillageMapping;
import com.nucleus.user.UserCityVillageMappingService;
import com.nucleus.user.UserDefaultUrlMapping;
import com.nucleus.user.UserDefaultUrlMappingVO;
import com.nucleus.user.UserDepartment;
import com.nucleus.user.UserInfo;
import com.nucleus.user.UserMobilityInfo;
import com.nucleus.user.UserProfile;
import com.nucleus.user.UserSecurityQuestion;
import com.nucleus.user.UserService;
import com.nucleus.user.UserStatus;
import com.nucleus.user.UserVO;
import com.nucleus.user.UserVillageMapping;
import com.nucleus.web.common.RenderImageUtility;
import com.nucleus.web.master.CommonFileIOMasterGridLoad;
import com.nucleus.web.usermgmt.UserManagementForm;
import flexjson.JSONDeserializer;
import flexjson.JSONSerializer;
import net.bull.javamelody.MonitoredWithSpring;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.Hibernate;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.support.RequestContextUtils;

import javax.inject.Inject;
import javax.inject.Named;
import javax.mail.MessagingException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static com.nucleus.finnone.pro.general.util.ValidatorUtils.notNull;
import static com.nucleus.user.UserConstants.NEUTRINO_SYSTEM_USER;

/**
 * @author Nucleus Software India Pvt Ltd
 * @description This Class is used for User Management This Controller Extends
 *              Master Controller and Override Some Function of Master
 *              Controller to load data in case the bean is not an entity or not
 *              extending to base master entity.
 * 
 * 
 */

@Transactional
@Controller
@RequestMapping(value = "/UserInfo")
public class UserAdminProductMappingController extends UserAdminBaseController {

   // @Inject
    //@Named("templateService")
   // protected TemplateService             templateService;
	@Inject
	@Named("licenseClientCacheService")
	private   LicenseClientCacheService licenseClientCacheService;
    private static final String  CONFIG_APPLICABLE_BPTYPE = "config.user.applicable.businesspartnertype";
    private static final String USER_REGEX_KEY = "username_pattern";
    @Inject
    @Named("addressInitializer")
    private AddressInitializer addressInitializer;
    
    @Inject
    @Named("teamService")
    private TeamService                 teamService;

    @Inject
    @Named("userManagementService")
    private UserManagementService       userManagementService;

    @Inject
    @Named("userManagementServiceCore")
    private UserManagementServiceCore   userManagementServiceCore;


    @Inject
    @Named("userBranchProductService")
    private UserBranchProductService userBranchProductService;

    @Inject
    @Named("oauthauthenticationService")
    private RESTfulAuthenticationService restAuthenticationService;

    //@Inject
    //@Named("userService")
    //private UserService                 userService;

   // @Inject
   // @Named("baseMasterService")
    //private BaseMasterService           baseMasterService;

    @Value("${core.web.user.source.system:db}")
    private String userSourceSystem;

    @Inject
    @Named("businessMappingServiceCore")
    private BusinessMappingServiceCore    businessMappingServiceCore;
    
    @Inject
    @Named("eventService")
    private EventService                  eventService;

    @Inject
    private OrganizationService           organizationService;
    
    @Inject
    @Named("configurationService")
    private ConfigurationService        configurationService;

    @Inject
    @Named("messageSource")
    protected MessageSource               messageSource;

    @Inject
    @Named("mailService")
    private MailService                   mailService;

    @Inject
    @Named("authenticationTokenService")
    private AuthenticationTokenService    authenticationTokenService;

    @Inject
    @Named("mailMessageIntegrationService")
    private MailMessageIntegrationService mailMessageIntegrationService;

    @Inject
    @Named("bpmnProcessService")
    private BPMNProcessService            bpmnProcessService;
    
    @Inject
    @Named("teamManagementService")
    private TeamManagementService         teamManagementService;
    
    @Inject
    @Named("makerCheckerService")
    private MakerCheckerService        makerCheckerService;

    @Autowired
    protected EventBus                    eventBus;

    @Inject
    @Named("userAdminHelper")
    private UserAdminHelper userAdminHelper;
    
    @Inject
    @Named("userGridServiceCore")
    private UserGridServiceImpl         userGridService;
    
    
	@Inject
	@Named("userBPMappingService")
	private UserBPMappingService userBPMappingService;
	

    @Inject
    @Named("customUsernamePasswordAuthenticationFilter")
    private AbstractAuthenticationProcessingFilter customUsernamePasswordAuthenticationFilter;
    
    
    
    @Inject
    @Named("masterXMLDocumentBuilder")
    private CommonFileIOMasterGridLoad commonFileIOMasterGridLoad;
    
    @Inject
    @Named("renderImageUtility")
    private RenderImageUtility renderImageUtility;

    @Inject
    @Named("addressService")
    private AddressTagService addressService;


    @Inject
    @Named("userCityVillageMappingService")
    private UserCityVillageMappingService userCityVillageMappingService;

    @Inject
    @Named("menuService")
    IMenuService menuService;

    @Inject
    @Named("genericParameterService")
    private GenericParameterServiceImpl genericParameterService;

    @Inject
    private UserAdminController adminController;

    @Inject
    @Named("neutrinoRestTemplate")
    private RestTemplate restTemplate;

    @Value("${INTG_BASE_URL}/app/taskCount")
    private String taskCountUrl;

    @Value("${soap.service.trusted.client.id}")
    private String clientID;


    private static final String                  MSG_SUCCESS           = "success";

    private static final String                                masterId              = "UserInfo";
    private static final String           USER_CREATE_EVENT     = "Create";

    
    private static final String           PRODUCT_CONCAT_CHAR     = "P_";

    private static final String STATUS							="status";
    private static final String MESSAGE	 						="message";
	private static final String STATUS_TRUE 					= "true";
	private static final String STATUS_FALSE 					= "false";
    /*
     * Method Added to send current Entity Uri for working of
     * comments,activity,history,notes
     */
    @ModelAttribute("currentEntityClassName")
    public String getEntityClassName() {
        return User.class.getName();
    }

    /**
     * @description This Method Will be used in future for creating a new user
     */
    @PreAuthorize("hasAuthority('MAKER_USER')")
    @RequestMapping(value = "/create")
    public String createNewUser(ModelMap map) {

        UserProfile userProfile = new UserProfile();
        map.put("userProfile", userProfile);
        map.put("masterID", masterId);
        User currentLoggedInUser = getUserDetails().getUserReference();
        if (currentLoggedInUser != null) {
            map.put("isSuperAdmin", userService.isUserSuperAdmin(currentLoggedInUser.getId()));
        }
        setGlobalExpiryDaysInMap(map);
        return "user";
    }

    /**
     * @description This Method is used for editing user configuration i.e
     *              mapping of user with branch,product
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    @PreAuthorize("hasAuthority('MAKER_USER')")
    @RequestMapping(value = "/edit/{id}")
    public String editUser(@PathVariable("id") Long Id, ModelMap map) {
        map.put("edit", true);
        map.put("readonly", false);
        prepareDataForViewAndEdit(Id, map);
        return "userManagement";
    }

    @PreAuthorize("hasAuthority('MAKER_USER-BRANCHES_BULK_MAPPING') or hasAuthority('CHECKER_USER-BRANCHES_BULK_MAPPING') or hasAuthority('VIEW_USER-BRANCHES_BULK_MAPPING')")
    @RequestMapping(value = "/mapping")
    public String userBranchProductMapping(ModelMap map) {
        map.put("user", new User());
        map.put("masterID", masterId);
        return "userBranchProduct";
    }
    
    @RequestMapping(value = "/branchCalendar/{id}")
    @ResponseBody
    public BranchCalendar getDefaultBranchCalendarForUser(@PathVariable("id") Long Id, ModelMap map) {
    	OrganizationBranch orgBranch = organizationService.getOrganizationBranchById(Id);
    	BranchCalendar branchCalendar = null;
    	if(orgBranch!=null){
    		branchCalendar =HibernateUtils.initializeAndUnproxy( organizationService.getDerivedBranchCalendar(orgBranch)); 
        	branchCalendar.setHolidayList(null);	
    	}              
    	return branchCalendar;
    }

	/**
     * This method is to get all branch tree irrespective of logged in user. Will be used in BusinessPartner and Product Masters 
     * */
    @RequestMapping(value = "/branchTree")
    @ResponseBody
    public List<OrgBranchInfo> getBranches(ModelMap map, @RequestParam("systemName") String systemName) {
        List<OrgBranchInfo> topBranches = organizationService.getOrganizationTree(null, systemName);
        List<OrgBranchInfo> branchList = new ArrayList<OrgBranchInfo>();

        for (OrgBranchInfo branch : topBranches) {
            OrgBranchInfo branchTree = new OrgBranchInfo();
            branchTree.setTitle(branch.getOrgName());
            branchTree.setKey((branch.getId()).toString());
            branchTree.setOrganizationType(branch.getOrganizationType());
            if (branch.getChildOrgCount() != 0) {
                branchTree.setLazy(false);

                List<OrganizationBranch> children = organizationService.getAllChildBranches(branch.getId(), systemName);
                List<OrgBranchInfo> childList = new ArrayList<OrgBranchInfo>();
                for (OrganizationBranch child : children) {
                    OrgBranchInfo childTree = new OrgBranchInfo();
                    childTree.setTitle(child.getDisplayName());
                    childTree.setKey(child.getId().toString());
                    childTree.setOrganizationType(child.getOrganizationType().getCode());
                    boolean isChild = true;
                    for (OrgBranchInfo branchParent : topBranches) {
                        if (branchParent.getId().equals(child.getId())) {
                            isChild = false;
                        }
                    }
                    if (isChild) {
                        childList.add(childTree);
                    }
                }
                branchTree.setChildren(childList);
            } else
                branchTree.setLazy(false);

            branchList.add(branchTree);
        }

        return branchList;
    }

    /**
     * This method is to get all branch tree irrespective of logged in user. Will be used in BusinessPartner and Product Masters
     * */
    @RequestMapping(value = "/branchTreeLevelZero")
    @ResponseBody
    public List<OrgBranchTree> getBranchesLevelZero(ModelMap map, @RequestParam("systemName") String systemName) {
        List<OrgBranchInfo> topBranches = organizationService.getOrganizationTree(null, systemName);
        List<OrgBranchTree> branchList = new ArrayList<OrgBranchTree>();

        for (OrgBranchInfo branch : topBranches) {
            OrgBranchTree branchTree = new OrgBranchTree();
            branchTree.setTitle(branch.getOrgName());
            branchTree.setKey((branch.getId()).toString());
            branchTree.setLevel("one");
            branchTree.setUrl("/app/UserInfo/branchTreeLevelOne");
            branchTree.setOrganizationType(branch.getOrganizationType());
            if (branch.getChildOrgCount() != 0) {
                List<OrganizationBranch> children = organizationService.getAllChildBranches(branch.getId(), systemName);
                for (OrganizationBranch child : children) {
                    boolean isChild = true;
                    for (OrgBranchInfo branchParent : topBranches) {
                        if (branchParent.getId().equals(child.getId())) {
                            isChild = false;
                        }
                    }
                    if (isChild) {
                        branchTree.setIsLazy(true);
                    }
                }

            } else
                branchTree.setIsLazy(false);

            branchList.add(branchTree);
        }

        return branchList;
    }


    /**
     * This method is to get all branch tree irrespective of logged in user. Will be used in BusinessPartner and Product Masters
     * */
    @RequestMapping(value = "/branchTreeLevelOne")
    @ResponseBody
    public List<OrgBranchTree> getBranchesLevelOne(ModelMap map, @RequestParam("systemName") String systemName, @RequestParam("key") Long key) {
        List<OrgBranchInfo> topBranches = organizationService.getOrganizationTree(null, systemName);
        List<OrganizationBranch> children = organizationService.getAllChildBranches(key, systemName);

        List<OrgBranchTree> childList = new ArrayList<OrgBranchTree>();
        for (OrganizationBranch child : children) {
            OrgBranchTree childTree = new OrgBranchTree();
            childTree.setTitle(child.getDisplayName());
            childTree.setKey(child.getId().toString());
            childTree.setIsLazy(false);
            childTree.setOrganizationType(child.getOrganizationType().getCode());
            boolean isChild = true;
            for (OrgBranchInfo branchParent : topBranches) {
                if (branchParent.getId().equals(child.getId())) {
                    isChild = false;
                }
            }
            if (isChild) {
                childList.add(childTree);
            }
        }

        return childList;
    }
    /**
     * This method is to get all branches for logged in user. Will be user for User Management Form and Bulk Mapping
     * */
    @RequestMapping(value = "/user/branchTree")
    @ResponseBody
    public List<OrgBranchInfo> getBranchesForCurrentUser(ModelMap map, @RequestParam("systemName") String systemName, @RequestParam(value = "stateName", required = false) String stateName) {
        
     Map<OrganizationBranch, Long> orgBranchChildCountMap  = new HashMap<OrganizationBranch, Long>();
    	if(stateName == null || stateName.trim().isEmpty()){
    	
        orgBranchChildCountMap = userManagementServiceCore.getOrgBranchesWithChildCountUnderCurrentUserByOrganizationType(
                        getUserDetails().getId(), systemName,OrganizationType.ORGANIZATION_TYPE_BRANCH);
    	}
        
    	else{
        orgBranchChildCountMap = userManagementServiceCore.getOrgBranchesWithChildCountUnderCurrentUserByOrganizationTypeWithState(
                getUserDetails().getId(), systemName,OrganizationType.ORGANIZATION_TYPE_BRANCH, stateName);
    	}
        if (ValidatorUtils.notNull(orgBranchChildCountMap)) {
            Set<Long> parentBranchIds = new HashSet<Long>();
            for(OrganizationBranch orgBranch : orgBranchChildCountMap.keySet()){
                String[] parentBranches = null;
                if(ValidatorUtils.notNull(orgBranch.getParentBranchIds())){
                    parentBranches = orgBranch.getParentBranchIds().split(";");
                }
                if(ValidatorUtils.notNull(parentBranches)){
                    for (int i = 0 ; i < parentBranches.length ; i++) {
                    	
                    	
                        if (ValidatorUtils.notNull(parentBranches[i]) && parentBranches[i].contains(systemName)) {
                            parentBranches[i] = StringUtils.stripEnd(parentBranches[i], "_");
                            String[] parentBranchesArray = parentBranches[i].split("_"+systemName);
                            for(int j = 0 ; j < parentBranchesArray.length ; j++){
                                if(StringUtils.isNotBlank(parentBranchesArray[j])){
                                    try {
										parentBranchIds.add(Long.parseLong(parentBranchesArray[j].trim()));
									} catch (NumberFormatException e) {
										BaseLoggers.exceptionLogger.error("Number format error"+parentBranchesArray[j]);
									}
                                }
                            }
                        } 
                    }
                }
            }
            
            List<OrgBranchInfo> topBranchIds = new  ArrayList<OrgBranchInfo>();
            topBranchIds=organizationService.getTopBranchesAmongBranchIds(systemName,new ArrayList<Long>(parentBranchIds));
            if (ValidatorUtils.hasElements(topBranchIds)) {
                return userAdminHelper.formOrganizationBranchTree(topBranchIds,systemName,
                        orgBranchChildCountMap.keySet(),parentBranchIds);
    
            }
        }
        return Collections.emptyList();
    }

    /**
     * This method is to get all branch tree level wise irrespective of logged in user. Will be used in BusinessPartner and Product Masters
     * */
    @RequestMapping(value = "/user/branchTreeLevels")
    @ResponseBody
    public List<OrgBranchTree> getBranchesForLevels(ModelMap map, @RequestParam("systemName") String systemName,@RequestParam("stateName") String stateName,
                                                    @RequestParam("key") Long key, @RequestParam("level") String level) {

        Map<OrganizationBranch, Long> orgBranchChildCountMap  = new HashMap<OrganizationBranch, Long>();
        if(stateName == null || stateName.trim().isEmpty()){

            orgBranchChildCountMap = userManagementServiceCore.getOrgBranchesWithChildCountUnderCurrentUserByOrganizationType(
                    getUserDetails().getId(), systemName,OrganizationType.ORGANIZATION_TYPE_BRANCH);
        }
        else{
            orgBranchChildCountMap = userManagementServiceCore.getOrgBranchesWithChildCountUnderCurrentUserByOrganizationTypeWithState(
                    getUserDetails().getId(), systemName,OrganizationType.ORGANIZATION_TYPE_BRANCH, stateName);
        }

        List<OrgBranchInfo> topBranchIds = new ArrayList<OrgBranchInfo>();
        Set<Long> parentBranchIds = new HashSet<Long>();
        if (ValidatorUtils.notNull(orgBranchChildCountMap)) {

            for(OrganizationBranch orgBranch : orgBranchChildCountMap.keySet()){
                String[] parentBranches = null;
                if(ValidatorUtils.notNull(orgBranch.getParentBranchIds())){
                    parentBranches = orgBranch.getParentBranchIds().split(";");
                }
                if(ValidatorUtils.notNull(parentBranches)){
                    for (int i = 0 ; i < parentBranches.length ; i++) {
                        if (ValidatorUtils.notNull(parentBranches[i]) && parentBranches[i].contains(systemName)) {
                            parentBranches[i] = StringUtils.stripEnd(parentBranches[i], "_");
                            String[] parentBranchesArray = parentBranches[i].split("_"+systemName);
                            for(int j = 0 ; j < parentBranchesArray.length ; j++){
                                if(StringUtils.isNotBlank(parentBranchesArray[j])){
                                    try {
                                        parentBranchIds.add(Long.parseLong(parentBranchesArray[j].trim()));
                                    } catch (NumberFormatException e) {
                                        BaseLoggers.exceptionLogger.error("Number format error"+parentBranchesArray[j]);
                                    }
                                }
                            }
                        }

                    }
                }
            }

            topBranchIds = organizationService.getTopBranchesAmongBranchIds(systemName,new ArrayList<Long>(parentBranchIds));
        }

        int levelVal = Integer.parseInt(level);
        if(levelVal==0)
        {
            if (ValidatorUtils.hasElements(topBranchIds)) {
                return userAdminHelper.formOrganizationBranchTreeTopLevel(topBranchIds,systemName,orgBranchChildCountMap.keySet(),parentBranchIds);

            }

        }
        else
        {
            return userAdminHelper.formLazyBranchTreeNodes(key,systemName,orgBranchChildCountMap.keySet(),parentBranchIds);
        }

        return Collections.emptyList();
    }
    @RequestMapping(value = "/getUser")
    public String getUser(ModelMap map, @RequestParam Long[] branchIds) {
        Map<OrganizationBranch, List<UserOrgBranchMapping>> branchAssociatedUsers = new HashMap<OrganizationBranch, List<UserOrgBranchMapping>>();
        for (int i = 0 ; i < branchIds.length ; i++) {
            String selectedBranch = branchIds[i].toString();
            if (selectedBranch != null && !"".equals(selectedBranch)) {
                List<UserOrgBranchMapping> usersInBranch = userManagementServiceCore.getUserInBranch(Long
                        .parseLong(selectedBranch));
                OrganizationBranch branchName = baseMasterService
                        .getMasterEntityById(OrganizationBranch.class, branchIds[i]);
                branchAssociatedUsers.put(branchName, usersInBranch);
            }

        }
        map.put("branchAssociatedUsers", branchAssociatedUsers);

        updateUserInfo();
        return "userBranchRow";
    }

    @RequestMapping(value = "/transferTeamsToUser/{userId}", method = RequestMethod.POST)
    @ResponseBody
    public String transferTeamsToUser(ModelMap map, @RequestParam(value = "teamId") Long teamId, @PathVariable("userId") Long userId) {

        Set<Team> newUserTeams = new HashSet<Team>();
        Set<Team> finalUserTeams;

        Team team = teamService.getTeamByTeamId(teamId);
        newUserTeams.add(team);

        User user;
        if (userId != null) {
            user = baseMasterService.getMasterEntityById(User.class, userId);
        } else {
            user = new User();
        }

        finalUserTeams = new HashSet<Team>(teamService.getTeamsAssociatedToUserByUserId(user.getId()));
        finalUserTeams.addAll(newUserTeams);
        teamManagementService.allocateTeamsToThisUser(finalUserTeams, userService.getUserById(userId));

        map.put("user", user);
        if (user.getId() != null) {
            map.put("inUser", teamService.getTeamsAssociatedToUserByUserId(user.getId()));
            map.put("notInUser", teamService.getTheEligibleTeamsNotAssociatedToThisUser(user));
            map.put("leaderOfNumberOfTeams", getLeaderOfNumberOfTeams(user));
            map.put("noOfTeamsRepresentedBy", getNoOfTeamsRepresentedBy(user));
        }

        return "userTeams";
    }

    @RequestMapping(value = "/removeTeamsFromUser/{userId}", method = RequestMethod.POST)
    @ResponseBody
    public String removeTeamsFromUser(ModelMap map, @RequestParam(value = "teamId") Long teamId, @PathVariable("userId") Long userId) {

        List<Team> oldUserTeams = new ArrayList<Team>();
        List<Team> newUserTeams;

        oldUserTeams.add(baseMasterService.getMasterEntityById(Team.class, teamId));

        User user;
        if (userId != null)
            user = baseMasterService.getMasterEntityById(User.class, userId);
        else
            user = new User();

        newUserTeams = new ArrayList<Team>(teamService.getTeamsAssociatedToUserByUserId(user.getId()));
        newUserTeams.removeAll(oldUserTeams);
        for (int i = 0 ; i < oldUserTeams.size() ; i++) {
            Team team = oldUserTeams.get(i);
            teamManagementService.removeUserFromThisTeam(user, team);
            if (user == team.getTeamLead())
                team.setTeamLead(null);
        }

        map.put("user", user);
        if (user.getId() != null) {
            map.put("inUser", teamService.getTeamsAssociatedToUserByUserId(user.getId()));
            map.put("notInUser", teamService.getTheEligibleTeamsNotAssociatedToThisUser(user));
            map.put("leaderOfNumberOfTeams", getLeaderOfNumberOfTeams(user));
            map.put("noOfTeamsRepresentedBy", getNoOfTeamsRepresentedBy(user));
        }

        return "userTeams";
    }

    @RequestMapping(value = "/getAllUser")
    public String getAllUserForMapping(ModelMap map, @RequestParam("branchSelected") Object branchSelected) {
        map.put("branchSelected", branchSelected);
        return "assignUserBranch";
    }

    @RequestMapping(value = "/getUserById")
    @ResponseBody
    public String searchUser(ModelMap map, @RequestParam("userCredentials") String userCredentials,
            @RequestParam(value = "branchSelected") String[] branchSelected) {
        List<User> userList = null;
        List<UserInfo> usersInBranch = new ArrayList<UserInfo>();
        Map<String, Object> userMap = new HashMap<String, Object>();
        try {
            if (userCredentials.matches("[0-9]+") && userCredentials.length() > 2) {
                userMap.put("id", Long.parseLong(userCredentials));
                userList = userManagementServiceCore.findUser(userMap);

            } else {
                userMap = new HashMap<String, Object>();
                userMap.put("username", userCredentials);
                userList = userManagementServiceCore.findUser(userMap);

            }
            for (String br : branchSelected) {
                for (Object userinfo : userManagementServiceCore.getUsersInBranch(br)) {
                    UserInfo addUser = (UserInfo) userinfo;
                    usersInBranch.add(addUser);
                }
            }

            List<User> userListExisting = new ArrayList<User>();

            for (int i = 0 ; i < userList.size() ; i++) {
                for (int j = 0 ; j < usersInBranch.size() ; j++) {
                    Object o = usersInBranch.get(j);

                    UserInfo u = (UserInfo) o;

                    if (u.getId().equals(userList.get(i).getId())) {
                        userListExisting.add(userList.get(i));
                    }

                }
            }

            userList.removeAll(userListExisting);

        } catch (NumberFormatException e) {
            BaseLoggers.exceptionLogger.error("Number Format Exception", e);
        }
        JSONSerializer iSerializer = new JSONSerializer();
        return iSerializer.exclude("*.class").include("id", "username").exclude("*").deepSerialize(userList);

    }

    @RequestMapping(value = "/saveImage")
    @ResponseBody
    public String saveImageOfUser(@RequestParam String photoUrl) {
        UserProfile userProfile = userService.getUserProfile(getUserDetails().getUserReference());
        userProfile.setPhotoUrl(photoUrl);
        return new JSONSerializer().serialize("success");
    }
    @PreAuthorize("hasAuthority('MAKER_USER')")
    @RequestMapping(value = "/saveUserToBranch")
    @ResponseBody
    public String saveUserToBranch(ModelMap map, @RequestParam(value = "branchList", required = false) List<Long> branchList,
            @RequestParam(value = "userList", required = false) List<Long> userList,
            @RequestParam("assignUsersToChild") boolean assignUsersToChild, @RequestParam("systemName") String systemName,
            @RequestParam(value = "deleteBranch", required = false) List<Long> deleteList, HttpServletRequest request) {
        String message = null;
        Object[] args = new Object[5];

        Locale loc = RequestContextUtils.getLocale(request);

        if ((branchList != null && !branchList.isEmpty()) && (userList != null && !userList.isEmpty())) {
            List<UserOrgBranchMapping> userOrgBranchMappingList = userManagementServiceCore.addUsersToBranches(userList,
                    branchList, assignUsersToChild, systemName);
            userManagementServiceCore.saveBusinessMappingList(userOrgBranchMappingList);
        }
        List<UserOrgBranchMapping> userDeleteList = new ArrayList<UserOrgBranchMapping>();

        if (deleteList != null && !deleteList.isEmpty()) {
            for (Long id : deleteList) {
                UserOrgBranchMapping userOrg = baseMasterService.getMasterEntityById(UserOrgBranchMapping.class, id);
                userDeleteList.add(userOrg);
            }
            userManagementServiceCore.deleteUserOrgBranchMapping(userDeleteList);
        }

        // Fire event
        if (userList != null && !userList.isEmpty()) {
            for (Long userId : userList) {
                UserInfo user = userService.getUserById(userId);
                UserEvent userEvent = new UserEvent(EventTypes.USER_UPDATED_EVENT, true, getUserDetails().getUserEntityId(),
                        user.getUserReference());
                userEvent.addProperty(UserEvent.USER_EMAIL, user.getMailId());
                userEvent.addProperty(UserEvent.USER_PROFILE, userService.getUserProfile(user.getUserReference()));
                userEvent.setUserName(user.getUsername());
                userEvent.setAssociatedUser(getUserDetails().getUsername());
                eventBus.fireEvent(userEvent);
            }
        }

        if ((deleteList != null && !deleteList.isEmpty()) && (branchList != null && !branchList.isEmpty())
                && (userList != null && !userList.isEmpty())) {
            args[0] = "User Updated";
            message = messageSource.getMessage("label.successfully", args, loc);
            message += "," + MSG_SUCCESS;
        }
        if ((deleteList != null && !deleteList.isEmpty()) && userList.isEmpty()) {
            args[0] = "User Removed from Branch";
            message = messageSource.getMessage("label.successfully", args, loc);
            message += "," + MSG_SUCCESS;
        }

        if (deleteList.isEmpty() && (branchList != null && !branchList.isEmpty())
                && (userList != null && !userList.isEmpty())) {
            args[0] = "User Added in Branch";
            message = messageSource.getMessage("label.successfully", args, loc);
            message += "," + MSG_SUCCESS;
        }
        return message;

    }

   
    @ResponseBody
    @RequestMapping(value = "/CheckUserName", method = RequestMethod.GET)
    public Map<String, String> checkName(@RequestParam String userName, HttpServletRequest request)throws IOException{
    	
    	Map<String, String> map = new HashMap<>();
    	if (!(isThisUserNameValid(userName, request))){
    		map.put(STATUS, STATUS_FALSE);
    		map.put(MESSAGE, messageSource.getMessage("user.name.invalid", null, RequestContextUtils.getLocale(request)));
    	}else if (isThisUserNamePresent(userName)){
    		map.put(STATUS, STATUS_FALSE);
    		map.put(MESSAGE, messageSource.getMessage("user.name.already.exists", null, RequestContextUtils.getLocale(request)));
    	}else{
    		map.put(STATUS, STATUS_TRUE);
    		map.put(MESSAGE, messageSource.getMessage("user.name.available", null, RequestContextUtils.getLocale(request)));
    	}
    	return map;
    }
    
    private boolean isThisUserNameValid(String userName, HttpServletRequest request)
    	    throws IOException
    	  {
    	    String usernamePattern = this.commonFileIOMasterGridLoad.getResourceBundleFileReader(request, USER_REGEX_KEY);

    	    return userName.matches(usernamePattern);
    	  }
    
	@ResponseBody
	@RequestMapping(value = "/isMoreLoginUserNameAllowed")
	@PreAuthorize("hasAuthority('MAKER_USER')")
	public Map<String, String> isMoreLoginUserNameAllowed(UserManagementForm userManagementForm,
			HttpServletRequest request)

	{
		Long[] roleIds=userManagementForm.getRoleIds();
		Long userId=userManagementForm.getAssociatedUser().getId();
		List<Long> userRolesIds=getEffectiveUserRoleIdList(roleIds,userId);
		Map<String, String> map = new HashMap<>();
		if (userRolesIds.isEmpty()) {
			map.put(STATUS, "false");

			map.put("message", messageSource.getMessage("label.role.required", null, getUserLocale()));
			return map;

		}
		List<String> productAssociatedWithUser = userService
				.getProductListFromRoleIds(userRolesIds);
		Map<String, LicenseDetail> anotherModuleCodeLicenseDetailsMap =licenseClientCacheService.getAll();
		String userUUID=userManagementForm.getAssociatedUser().getEntityLifeCycleData().getUuid();
		
		for (String product : productAssociatedWithUser) {
			for (Map.Entry<String, LicenseDetail> entry : anotherModuleCodeLicenseDetailsMap.entrySet()) {
				
				if (product.equals(entry.getKey()) && entry.getValue() != null ) {

					Integer maximumNumberOfUsersAllowed = entry.getValue().getMaxNamedUsers();

					if (!validateNamedUserCreationAllowed(maximumNumberOfUsersAllowed, entry.getKey(), userUUID, map)) {

						return map;
					}
				}
				
				if (entry.getValue() != null  && notNull(entry.getValue().getLicenseMobilityModuleInfoList())) {

					for (LicenseMobilityModuleInfo moblityInfo : entry.getValue().getLicenseMobilityModuleInfoList()) {
						if (product.equals(moblityInfo.getMobilityModuleCode())) {
							Integer maximumNumberOfUsersAllowed = moblityInfo.getNamedUserCount();

							if (!validateNamedUserCreationAllowed(maximumNumberOfUsersAllowed, product, userUUID,
									map)) {

								return map;
							}
						}
					}

				}
			}

		}

		map.put("status", String.valueOf(true));

		return map;

	}
	
    private boolean validateNamedUserCreationAllowed(Integer maximumNumberOfUsersAllowed, String product, String userUUID, Map<String, String> map) {
    	map.put("maxNamedUser", String.valueOf(maximumNumberOfUsersAllowed));
    	boolean creationAllowed = (maximumNumberOfUsersAllowed != -1
				&& maximumNumberOfUsersAllowed > userService.getUsersCountByProductName(product,userUUID))
				|| maximumNumberOfUsersAllowed == -1;
		map.put(STATUS, String.valueOf(creationAllowed));
		if (!creationAllowed) {
			map.put("message",messageSource.getMessage("label.license.named.user.exceeds",new String[] { String.valueOf(maximumNumberOfUsersAllowed),product }, 
							getUserLocale()));
		}
		return creationAllowed;
	}

	private List<Long> getEffectiveUserRoleIdList( Long[] roleIds,Long userId) {
    	List<Long> userRolesIds=new ArrayList<>();
		if(roleIds==null)
		{
			 List<Role> userRoles = userService.getRolesFromUserId(userId);
		     
		        for (Role role : userRoles) {
		        	userRolesIds.add(role.getId());
		        }
			
		}
		else
		{
			 for (Long roleId : roleIds) {
			userRolesIds.add(roleId);
			 }
		}
		return userRolesIds;
	}

	private boolean isThisUserNamePresent(String userName) {
        List<User> users = userService.getAllUser();
        Iterator<User> iter = users.iterator();

        while (iter.hasNext()) {
            User user = iter.next();
            if (StringUtils.isEmpty(userName) || (StringUtils.isNotBlank(user.getUsername()) && user.getUsername().equalsIgnoreCase(userName)))
                return true;
        }

        return false;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @PreAuthorize("hasAuthority('VIEW_USER') or hasAuthority('MAKER_USER') or hasAuthority('CHECKER_USER')")
    @RequestMapping(value = "/view/{id}")
    public String viewUserConfiguration(@PathVariable("id") Long Id, ModelMap map) {
        map.put("viewable", true);
        map.put("readable", true);
        map.put("readonly", true);
        prepareDataForViewAndEdit(Id, map);
        return "userManagement";
    }
    
     
    
    @SuppressWarnings("unchecked")
    private void prepareDataForViewAndEdit(Long Id, ModelMap map){
         Map<String, ?> userDetails = userManagementServiceCore.findUserById(Id);
         User user = (User) userDetails.get("user");

         if(Boolean.TRUE.equals(map.get("edit"))){//Preparing data for edit
             if(UserStatus.STATUS_ACTIVE != user.getUserStatus()){
                 throw new AccessDeniedException("Cannot edit Blocked/Inactive/Deleted User");
             } else if (Boolean.TRUE.equals(user.getEntityLifeCycleData().getSystemModifiableOnly())){
                 throw new AccessDeniedException("Cannot edit System Modifiable Only User");
             }
         }
         
         UserInfo currentUser = getUserDetails();
         user = baseMasterService.getMasterEntityWithActionsById(User.class, user.getId(), currentUser.getUserEntityId()
                 .getUri());
         userGridService.updateUserActions(user, userGridService.getAllLoggedInUsers());
                 
         user.setSysName(HibernateUtils.initializeAndUnproxy(user.getSysName()));
         UserInfo info = new UserInfo(user);
         UserProfile userProfile=(UserProfile)userDetails.get("userProfile");
         if (userProfile != null) {
            info.setDisplayName(userProfile.getFullName());
         }

         if (info.getAccessToAllBranches() != null) {
             String accessToAllBranches = info.getAccessToAllBranches().toString();
             map.put("accessToAllBranches", accessToAllBranches);
         }

         if (info.getAccessToAllProducts() != null) {
             String accessToAllProducts = info.getAccessToAllProducts().toString();
             map.put("accessToAllProducts", accessToAllProducts);
         }
         UserManagementForm userManagementForm = new UserManagementForm();
         userManagementForm.setAssociatedUser(user);
         if (user != null && user.isBusinessPartner()) {
             Long associatedBPId = userBPMappingService.getAssociatedBPIdByUserId(user.getId());
             if (associatedBPId != null) {
                 userManagementForm.setMappedBPid(associatedBPId);
             }
         }
       
         userManagementForm.setUserprofile(userProfile);
         Hibernate.initialize(userManagementForm.getAssociatedUser().getDeviationLevel());
         Hibernate.initialize(userManagementForm.getAssociatedUser().getUserClassification());
         Hibernate.initialize(userManagementForm.getAssociatedUser().getUserCategory());
       
         final DateFormat dformat = new SimpleDateFormat(getUserDateFormat());
         Event event = eventService.getLastSuccessLoginEventByAssociatedUseruri(info.getUserEntityId().getUri());
         UserSecurityTrailEvent lastLoginInfo = null;
         if (event != null) {
             lastLoginInfo = (UserSecurityTrailEvent) event;
         }
         Map lastLoginInfoMap = new HashMap();
         if (lastLoginInfo != null) {
             DateTime cal = lastLoginInfo.getEventTimestamp();
             Date date = cal.toDate();
             DateFormat tformat = new SimpleDateFormat("HH:mm:ss");
             lastLoginInfoMap.put("Date", dformat.format(date));
             lastLoginInfoMap.put("Time", tformat.format(date));
             lastLoginInfoMap.put("RemoteIpAddress", lastLoginInfo.getRemoteIpAddress());
         }

         /* Get current logged in user's mapped branches where he is admin*/
         String sysName = SystemName.SOURCE_PRODUCT_TYPE_CAS;
         if (user.getSysName() != null && user.getSysName().getCode() != null)
             sysName = user.getSysName().getCode();
         Map<OrganizationBranch, Long> userOrgBranchMap = userManagementServiceCore.getOrgBranchesWithChildCountUnderCurrentUserByOrganizationType(
                 getUserDetails().getId(), sysName,OrganizationType.ORGANIZATION_TYPE_BRANCH);
         List<OrganizationBranch> loggedInUserMappedBranchList =null;
         if(ValidatorUtils.notNull(userOrgBranchMap)){
             loggedInUserMappedBranchList = new ArrayList<OrganizationBranch>(userOrgBranchMap.keySet());
         }

       
         /* Branch list of user being viewed */
         List<OrganizationBranch> userBeingViewdBranchList = userManagementServiceCore.getUserOrgBranches(user.getId(), sysName);

         List<OrganizationBranch> orgBranchList = null;

         /*Post only those branches of current user which are under logged in user*/
         if (ValidatorUtils.hasElements(loggedInUserMappedBranchList)) {
             orgBranchList = (List<OrganizationBranch>) CollectionUtils.intersection(loggedInUserMappedBranchList,
                     userBeingViewdBranchList);

             List<Long> orgBranchIds = new ArrayList<Long>();
             for (OrganizationBranch orgBranch : orgBranchList) {
                 orgBranchIds.add(orgBranch.getId());
             }
             if (!orgBranchIds.isEmpty()) {
                 map.put("orgBranchList", orgBranchIds);
             }
         }
         if (user.getTeamLead() != null && user.getTeamLead()) {
             Boolean teamExistsWithUser = teamService.checkTeamOfUser(user);
             map.put("teamExistsWithUser", teamExistsWithUser);
         }

         user.setSysName(HibernateUtils.initializeAndUnproxy(user.getSysName()));
         
         if (HibernateUtils.initializeAndUnproxy(user.getUserDepartment()) != null) {
 			user.setUserDepartment(user.getUserDepartment());
 		}
         
         User currentLoggedInUser = getUserDetails().getUserReference();
         if (currentLoggedInUser != null) {
             map.put("isSuperAdmin", userService.isUserSuperAdmin(currentLoggedInUser.getId()));
         }

         //GET USER CIty village mapping by user id
        HashMap<String,List<String>> selectedCities = new HashMap<>();
        HashMap<String,List<String>> selectedVillages = new HashMap<>();
         UserCityVillageMapping userCityVillageMapping =  userCityVillageMappingService.getCityVillageMappingByUserId(user.getId());
        if(userCityVillageMapping!=null){
			
            List<UserCityMapping> userCityMappingList = userCityVillageMapping.getUserCityMappings();
            List<UserVillageMapping> userVillageMappingList = userCityVillageMapping.getUserVillageMappings();

            for(UserCityMapping userCityMapping : userCityMappingList){
                String key = userCityMapping.getCity().getUri();
                List<String> value = new ArrayList<>();
                for(Area area : userCityMapping.getCityAreaList()){
                    value.add(area.getUri());
                }
                selectedCities.put(key,value);
            }
            for(UserVillageMapping userVillageMapping : userVillageMappingList){
                String key = userVillageMapping.getVillageMaster().getUri();
                List<String> value = new ArrayList<>();
                for(Area area : userVillageMapping.getVillageAreaList()){
                    value.add(area.getUri());
                }
                selectedVillages.put(key,value);
            }


        }
        JSONSerializer iSerializer = new JSONSerializer();


        map.put("selectedCities",iSerializer.exclude("*.class").deepSerialize(selectedCities));
        map.put("selectedVillages",iSerializer.exclude("*.class").deepSerialize(selectedVillages));

             
        Map<String, List<String>> mapOfSchemeList = new HashMap<String, List<String>>();
        List<UserOrgBranchProdSchemeMapping> userProdSchemeMapping = userManagementService.getUserProductSchemeList(Id);
        
        List<String> productMappedWithSchemeList = new ArrayList<String>();
        
        for (UserOrgBranchProdSchemeMapping upsm : userProdSchemeMapping) {
            if (!(productMappedWithSchemeList.contains(upsm.getProductId().toString()))) {
            	productMappedWithSchemeList.add(upsm.getProductId().toString());
            }
        }
        
        if (productMappedWithSchemeList != null && !productMappedWithSchemeList.isEmpty()) {
            for (String prodId : productMappedWithSchemeList) {
                List<String> schemeList = new ArrayList<String>();
                for (UserOrgBranchProdSchemeMapping upsm : userProdSchemeMapping) {
                    if (prodId.equals(upsm.getProductId().toString())) {
                    	schemeList.add(upsm.getSchemeId().toString());
                    }
                }

                mapOfSchemeList.put(PRODUCT_CONCAT_CHAR.concat(prodId), schemeList);
                
            }

        }
        
        List<Object> prodSchememapList = new ArrayList<Object>();
        prodSchememapList.add(mapOfSchemeList);
        
        map.put("userCalendar", false);
        if(userManagementForm.getAssociatedUser().getUserCalendar()!= null){
        	userManagementForm.getAssociatedUser().setUserCalendar(HibernateUtils.initializeAndUnproxy(userManagementForm.getAssociatedUser().getUserCalendar()));
        	map.put("userCalendar", true);
        }
        
        String schemeJsonString = iSerializer.deepSerialize(prodSchememapList);
        map.put("productSchemeMappingJSON", schemeJsonString);

        
        
         map.put("lastLoginInfo", lastLoginInfoMap);
         map.put("userManagementForm", userManagementForm);
         map.put("sysName", sysName);
         map.put("user", user);
         map.put("userId", user.getId());
         if(userProfile != null)
         map.put("userProfileId", userProfile.getId());
         map.put("masterID", masterId);
         setGlobalExpiryDaysInMap(map);
         if (user.getViewProperties() != null) {
             List<String> actions = (ArrayList<String>) user.getViewProperties().get("actions");
             if (actions != null) {
                 for (String act : actions) {
                     String actionString = "act" + act;
                     map.put(actionString.replaceAll(" ", ""), false);
                 }

             }

         }
    }
    
    @PreAuthorize("hasAuthority('MAKER_USERORGBRANCHMAPPING') or hasAuthority('CHECKER_USERORGBRANCHMAPPING') or hasAuthority('VIEW_USER')")
    @RequestMapping(value = "/view/address")
    public String viewAddressInfo(@RequestParam("userId") Long userId, ModelMap map) {
        User user = new User();
        user.setId(userId);
        UserProfile userProfile = userService.getUserProfile(user);
        Address userAddress = new Address();
        if (userProfile != null && userProfile.getSimpleContactInfo() != null) {
            userAddress = userProfile.getSimpleContactInfo().getAddress();
        }
        addressInitializer.initialize(userAddress, AddressInitializer.AddressLazyAttributes.COUNTRY);
        map.put("address", userAddress);
        return "userAddress";
    }
    
  
    @PreAuthorize("hasAuthority('MAKER_USERORGBRANCHMAPPING') or hasAuthority('CHECKER_USERORGBRANCHMAPPING') or hasAuthority('VIEW_USER')")
    @RequestMapping(value = "/view/roles")
    public String viewUserRoles(@RequestParam("userId") Long userId, ModelMap map) {
        List<Role> roleList = baseMasterService.getAllApprovedAndActiveEntities(Role.class);
        List<Role> userRoles = userService.getRolesFromUserId(userId);
        List<Long> roleIds = new ArrayList<Long>();
        for (Role role : userRoles) {
            roleIds.add(role.getId());
        }
        Long[] roleArray = new Long[roleIds.size()];
        
        //start
        String sourceSystem = userService.getSourceSystemForUserId(userId);
                            
        if((UserService.SOURCE_DB).equalsIgnoreCase(sourceSystem)){ //roles can be changed only for db users
            map.put("disable", false);
        }else{
            map.put("disable", true);
        }

        UserManagementForm userForm = new UserManagementForm();
        userForm.setRoleIds(roleIds.toArray(roleArray));
        map.put("userRoles", userRoles);
        map.put("roleList", roleList);
        map.put("userManagementForm", userForm);

        return "userRoles";
    }

    @PreAuthorize("hasAuthority('MAKER_USERORGBRANCHMAPPING') or hasAuthority('CHECKER_USERORGBRANCHMAPPING') or hasAuthority('VIEW_USER')")
    @RequestMapping(value = "/view/branches")
    public String viewUserBranches(@RequestParam("userId") Long userId, ModelMap map) {

        /* Get Primary branches of user being viewed */
        List<OrganizationBranch> userPrimaryBranches = userManagementServiceCore.getUserPrimaryBranch(userId);
        User updatedUser = baseMasterService.findById(User.class, userId);
        User originalUser = (User) baseMasterService.getLastApprovedEntityByUnapprovedEntityId(updatedUser.getEntityId());
        if (CollectionUtils.isEmpty(userPrimaryBranches) && null!=originalUser){
                userPrimaryBranches = userManagementServiceCore.getUserPrimaryBranch(originalUser.getId());
        }
        
        if(ValidatorUtils.notNull(originalUser) || (ValidatorUtils.isNull(originalUser) && updatedUser.getApprovalStatus()!=7)){
            map.put("userAccessBranches", Boolean.TRUE);
        }else{
            map.put("userAccessBranches", Boolean.FALSE);
        }
        UserInfo ui = userService.getUserById(userId);
        if (ui.getAccessToAllBranches() != null) {
            String accessToAllBranches = ui.getAccessToAllBranches().toString();
            map.put("accessToAllBranches", accessToAllBranches);
        }
        /* to check if current logged in user is a branch admin of any Branch */
        User currentLoggedInUser = getUserDetails().getUserReference();
        if (currentLoggedInUser != null) {
            boolean isBranchAdmin = userManagementServiceCore.getBranchAdminFlagForCurrentUser(currentLoggedInUser.getId());
            if (isBranchAdmin) {
                map.put("isBranchAdmin", true);
            } else {
                map.put("isBranchAdmin", false);
            }
            map.put("isSuperAdmin", userService.isUserSuperAdmin(currentLoggedInUser.getId()));
        }
        
        List<OrganizationBranch> orgBranchesForBranchAdmin = organizationService.getBranchesWhereUserIsBranchAdmin(userId);
        map.put("adminBranches", orgBranchesForBranchAdmin);
        
        List<State> stateList = baseMasterService.getAllApprovedAndActiveEntities(State.class);
        map.put("state", stateList);

        // map.put("orgBranchList", finalBranchList);
        map.put("systemName", HibernateUtils.initializeAndUnproxy(ui.getSysName()));
        map.put("primaryBranch", userPrimaryBranches);
       
        return "mapUserBranch";
    }

   

    /*  @PreAuthorize("hasAuthority('MAKER_USERORGBRANCHMAPPING') or hasAuthority('CHECKER_USERORGBRANCHMAPPING')")
      @RequestMapping(value = "/view/products")
      public String viewUserProducts(@RequestParam("userId") Long userId, ModelMap map) {

          List<UserOrgBranchMapping> userBranchMapping = userManagementService.getUserOrgBranchMapping(userId);
          for (UserOrgBranchMapping ubm : userBranchMapping) {
              // Hibernate.initialize(ubm.getLoanProductList());
          }
          String jsonString = iSerializer.exclude("*.class")
                  .include("associatedUser.id", "organizationBranch.id", "loanProductList.id").exclude("*")
                  .deepSerialize(userBranchMapping);
          map.put("userBranchMappingString", jsonString);
          map.put("userBranchMapping", userBranchMapping);
          return "mapUserProduct";
      }

     @RequestMapping(value = "/branchProductTree")
     public @ResponseBody
     List<TreeVO> getBranchesWithProducts(ModelMap map, @RequestParam(value = "branchIds") List<Long> brachIds) {
         // List<LoanProduct> productList = productService.getAllProductsMappedToBranches(brachIds);
         List<TreeVO> branchProductList = new ArrayList<TreeVO>();
         for (Long branchId : brachIds) {
             TreeVO branchNode = new TreeVO();
             Long count = 0l;
             branchNode.setKey(branchId.toString());
             List<TreeVO> childList = new ArrayList<TreeVO>();
               for (LoanProduct product : productList) {
                   OrganizationBranch branch = new OrganizationBranch();
                   branch.setId(branchId);
                   if (product.getOrgBranchList().contains(branch)) {
                       branch = product.getOrgBranchList().get(product.getOrgBranchList().indexOf(branch));
                       branchNode.setTitle(branch.getName());
                       branchNode.setLazy(true);
                       count++;
                       TreeVO productNode = new TreeVO();
                       productNode.setKey(product.getId().toString());
                       productNode.setTitle(product.getProductName());
                       childList.add(productNode);
                   }
               }
             branchNode.setChildCount(count);
             branchNode.setChildren(childList);
             branchProductList.add(branchNode);
         }

         return branchProductList;
     }*/

    @PreAuthorize("hasAuthority('MAKER_USERORGBRANCHMAPPING') or hasAuthority('CHECKER_USERORGBRANCHMAPPING') or hasAuthority('VIEW_USER')")
    @RequestMapping(value = "/view/communicationDetails")
    public String viewUserCommunicationDetails(@RequestParam("userId") Long userId, ModelMap map) {
        User user = new User();
        user.setId(userId);
        UserProfile userProfile = userService.getUserProfile(user);
        if (userProfile != null) {
            if (userProfile.getSimpleContactInfo() != null) {
                Hibernate.initialize(userProfile.getSimpleContactInfo().getPhoneNumber());
                Hibernate.initialize(userProfile.getSimpleContactInfo().getMobileNumber());
                Hibernate.initialize(userProfile.getSimpleContactInfo().getEmail());
            } else {
                userProfile.setSimpleContactInfo(new SimpleContactInfo());
            }
            if (userProfile.getAssociatedUser() != null) {
                Hibernate.initialize(userProfile.getAssociatedUser());
            }
            map.put("userProfile", userProfile);
        } else {
            map.put("userProfile", new UserProfile());
        }

        return "userCommunicationDetails";
    }

    @SuppressWarnings("unchecked")
    @PreAuthorize("hasAuthority('MAKER_USERORGBRANCHMAPPING') or hasAuthority('CHECKER_USERORGBRANCHMAPPING') or hasAuthority('VIEW_USER')")
    @RequestMapping(value = "/view/preferences")
    public String viewUserPreferences(@RequestParam("userId") Long userId, ModelMap map) {

        /*User user = new User();
        user.setId(userId);
        UserProfile userProfile = userService.getUserProfile(user);
        if (userProfile != null) {*/
            Map<String, ?> userDetails = userManagementServiceCore.findUserById(userId);

            Map<String, ConfigurationVO> preferences = (Map<String, ConfigurationVO>) userDetails.get("userPreference");
            if (preferences != null) {
                List<ConfigurationVO> configValues = new ArrayList<ConfigurationVO>();
                for (ConfigurationVO configVal : preferences.values()) {
                    configValues.add(configVal);
                }
                map.put("configVOList", configValues);
            }
        //}

        return "userPreferencesManagement";
    }

    @PreAuthorize("hasAuthority('MAKER_USERORGBRANCHMAPPING') or hasAuthority('CHECKER_USERORGBRANCHMAPPING') or hasAuthority('VIEW_USER')")
    @RequestMapping(value = "/view/teams")
    public String viewUserTeams(@RequestParam("userId") Long userId, ModelMap map) {

        User user = new User();
        user.setId(userId);
        /*        List<Long> teamIds = new ArrayList<Long>();
                for (Long teamId : teamService.getTeamIdAssociatedToUserByUserId(userId)) {
                    teamIds.add(teamId);
                }
                Long[] teamArray = new Long[teamIds.size()];
                map.put("teamIds", teamIds.toArray(teamArray));*/

        List<Long> teams = teamService.getTeamIdAssociatedToUserByUserId(userId);
        long[] ids = new long[teams.size()];
        if (teams != null && !teams.isEmpty()) {
            int i = 0;
            for (long teamIdVal : teams) {
                ids[i] = teamIdVal;
                i++;
            }
        }

        map.put("teamIds", teams);
        if (userId != null) {
            List<Team> allTeams = teamService.getAllTeams();
            map.put("allTeams", allTeams);

            map.put("leaderOfNumberOfTeams", getLeaderOfNumberOfTeams(user));
            map.put("noOfTeamsRepresentedBy", getNoOfTeamsRepresentedBy(user));
        }

        return "userTeams";
    }



    @PreAuthorize("hasAuthority('MAKER_USERORGBRANCHMAPPING') or hasAuthority('CHECKER_USERORGBRANCHMAPPING') or hasAuthority('VIEW_USER')")
    @RequestMapping(value = "/view/personalInfo")
    public String viewUserPersonalInfo(@RequestParam("userId") Long userId, ModelMap map) {
        User user = new User();
        user.setId(userId);
        UserProfile userProfile = userService.getUserProfile(user);
        map.put("userProfile", userProfile);
        return "includingUser";
    }


    @PreAuthorize("hasAuthority('MAKER_USERMOBILITYINFO') or hasAuthority('CHECKER_USERMOBILITYINFO') or hasAuthority('VIEW_USER')")
    @RequestMapping(value = "/view/userMobililyInfo")
    public String viewUserMobilityInfo(@RequestParam("userId") Long userId, ModelMap map) {
       

    	UserMobilityInfo userMobileInfo = userService.getUserMobilityInfo(userId);
        if (userMobileInfo==null) {
        	userMobileInfo = new UserMobilityInfo();
		}
        
        map.put("deviceRowIndex", userMobileInfo.getRegisteredDeviceList().size());
		map.put("userMobilityInfo", userMobileInfo);
		 ConfigurationVO imeiLength = configurationService.getConfigurationPropertyFor(
                 SystemEntity.getSystemEntityId(), Configuration.IMEI_LENGTH);
		 ConfigurationVO meidLength = configurationService.getConfigurationPropertyFor(
                 SystemEntity.getSystemEntityId(), Configuration.MEID_LENGTH);
        	 map.put("imeiLength", imeiLength.getPropertyValue());
        	 map.put("meidLength", meidLength.getPropertyValue());
        	 
        return "userMobilityInfo";
    }

    /*   @RequestMapping(value = "/view/outOfOffice")
       public String viewOutOfOfficeInfo(@RequestParam("userId") Long userId, ModelMap map) {

           return "userOutOfOffice";
       }*/

    private Long getNoOfTeamsRepresentedBy(User thisUser) {
        return teamService.getNoOfTeamsRepresentedByThisUser(thisUser);
    }

    private Long getLeaderOfNumberOfTeams(User thisUser) {
        return teamService.getNumberOfTeamsLedByThisUser(thisUser);
    }
    /**
     * This method is used to create user in the system for first time alonwith basic information  
     * @param userProfile
     * @param result
     * @param map
     * @param createAnotherMaster
     * @return
     */
    @PreAuthorize("hasAuthority('MAKER_USER')")
    @RequestMapping(value = "/saveUser")
    public String saveUser(UserProfile userProfile, BindingResult result, ModelMap map,RedirectAttributes redirectAttributes,
            @RequestParam(value = "createAnotherMaster", required = false) boolean createAnotherMaster, HttpServletRequest request)
    throws IOException{
        BaseLoggers.flowLogger.debug("Saving User Details-->" + userProfile.getLogInfo());
        // server side validation for checking user name
        if (!(isThisUserNameValid(userProfile.getAssociatedUser().getUsername(), request))) {
            map.put("userProfile", userProfile);
            map.put("masterID", this.masterId);
            result.rejectValue("associatedUser.username", "label.name.user.invalid");
            return "user";
          }
        if (isThisUserNamePresent(userProfile.getAssociatedUser().getUsername())) {
            map.put("userProfile", userProfile);
            map.put("masterID", masterId);
            result.rejectValue("associatedUser.username", "label.name.validation.exists");
            return "user";
        }

        User user = userProfile.getAssociatedUser();
        user.setUserStatus(UserStatus.STATUS_ACTIVE);
       

     
        /*setting same as false as a part of auditing */
        user.setTeamLead(Boolean.FALSE);
        /* this below setting needs to be changed */
        user.setSourceSystem(userSourceSystem);
        if(UserService.SOURCE_FEDERATED.equals(userSourceSystem)) {
        	user.setForcePasswordResetOnLogin(false);
        }
        
        user.setMailId(userProfile.getSimpleContactInfo().getEmail().getEmailAddress());
        if (userProfile.getAssociatedUser().getDeviationLevel() != null
                && userProfile.getAssociatedUser().getDeviationLevel().getId() != null) {
            user.setDeviationLevel(userProfile.getAssociatedUser().getDeviationLevel());
        } else {
            user.setDeviationLevel(null);
        }
        
        
        
        if (userProfile.getAssociatedUser().getUserClassification() != null
                && userProfile.getAssociatedUser().getUserClassification().getId() != null) {
            user.setUserClassification(userProfile.getAssociatedUser().getUserClassification());
        } else {
            user.setUserClassification(null);
        }
        
        if (userProfile.getAssociatedUser().getUserCategory() != null
                && userProfile.getAssociatedUser().getUserCategory().getId() != null) {
            user.setUserCategory(userProfile.getAssociatedUser().getUserCategory());
        } else {
            user.setUserCategory(null);
        }
      
        if(!user.isLoginEnabled()){
        	user.setSuperAdmin(false);
        }

        userProfile.setAssociatedUser(user);
        if (user.getOutOfOfficeDetails() == null) {
            OutOfOfficeDetails ood = new OutOfOfficeDetails();
            ood.setOutOfOffice(false);
            user.setOutOfOfficeDetails(ood);
        }
        if (userProfile.getAddressRange().getIpaddress()==null|| userProfile.getAddressRange().getIpaddress().length() == 0) {
            userProfile.getAddressRange().setIpaddress(null);
        }

        else if (userProfile.getAddressRange().getFromIpAddress()==null||userProfile.getAddressRange().getFromIpAddress().length() == 0) {
            userProfile.getAddressRange().setFromIpAddress(null);
            userProfile.getAddressRange().setToIpAddress(null);
        }

        if(userProfile.getUserAccessType()==null || userProfile.getUserAccessType().getId() ==null){
        	userProfile.setUserAccessType(null);
        }
        
        User loggedInUser = getUserDetails().getUserReference();
        if (loggedInUser != null) {
           makerCheckerService.masterEntityChangedByUser(user, loggedInUser);
        }

        EMailInfo email = new EMailInfo();
        email.setEmailAddress(userProfile.getSimpleContactInfo().getEmail().getEmailAddress());
        SimpleContactInfo simpleContactInfo = new SimpleContactInfo();
        simpleContactInfo.setEmail(email);

        userProfile.setSimpleContactInfo(simpleContactInfo);
        
        UserDepartment userDepartment = userProfile.getUserDepartment().isIdNull() ? null
				: userProfile.getUserDepartment();
		userProfile.setUserDepartment(userDepartment);
		user.setUserDepartment(userDepartment);

        if (userProfile != null && userProfile.getId() == null)
            userService.saveNewUserProfile(userProfile);

        else
            userService.saveUserProfile(userProfile);
        /*added to maintain audit log for user */
        UserManagementForm userManagementForm = new UserManagementForm();
        userManagementForm.setUserprofile(userProfile);
        userManagementForm.setAssociatedUser(user);
        
        String userName = userProfile.getAssociatedUser().getUsername();
   
       
        /*added to maintain audit log for user */
        if (createAnotherMaster) {
            UserProfile userProfileObj = new UserProfile();
            map.put("userProfile", userProfileObj);
            map.put("masterID", masterId);
            map.put("savedUser", userName);
            User currentLoggedInUser = getUserDetails().getUserReference();
            if (currentLoggedInUser != null) {
                map.put("isSuperAdmin", userService.isUserSuperAdmin(currentLoggedInUser.getId()));
            }
            setGlobalExpiryDaysInMap(map);
            flushCurrentTransaction();       
            return "user";
        }
        
        redirectAttributes.addFlashAttribute("savedUser", userName);
        flushCurrentTransaction();
        return "redirect:/app/UserInfo/loadColumnConfig";
    }
    /**
     * This method is used to save basic user data along with other 
     * mappings - Branch, Team, Product, Address, Role, Communication, 
     * User preferences etc - but once user is already created in the system 
     * @param userManagementForm
     * @param selectedBranchesList
     * @param originalOrgBranchList
     * @param originalAdminBranches
     * @param result
     * @param map
     * @param createAnotherMaster
     * @param changedBranchList
     * @param changedProductList
     * @return
     */
    @PreAuthorize("hasAuthority('MAKER_USER')")
    @RequestMapping(value = "/save", method = RequestMethod.POST)
    public String save(UserManagementForm userManagementForm,
            @RequestParam("selectedBranchesForUser") List<Long> selectedBranchesList,
            @RequestParam("originalOrgBranchList") List<Long> originalOrgBranchList,
            @RequestParam(value = "orgAdminBranches", required = false) List<Long> originalAdminBranches,
            BindingResult result, ModelMap map,
            @RequestParam(value = "createAnotherMaster", required = false) boolean createAnotherMaster,
            @RequestParam(value = "changedBranchList", required = false) String changedBranchList,
            @RequestParam(value = "changedProductList", required = false) String changedProductList,
            @RequestParam(value = "changedTeamList", required = false) String changedTeamList,
            @RequestParam(value = "cityAreaMap",required = false) String cityAreaMap,
            @RequestParam(value = "villageAreaMap",required = false) String villageAreaMap,
            @RequestParam(value="schemeListId", required=false) String schemeListId
    ) {

        UserVO userVo = prepareUserDataForSaveModified(userManagementForm, changedBranchList, changedProductList, changedTeamList,cityAreaMap,villageAreaMap);
        
        int deviceRowIndex = userManagementForm.getAssociatedUser().getUserMobileInfo()==null?0:userManagementForm.getAssociatedUser().getUserMobileInfo().getRegisteredDeviceList().size();
        map.put("deviceRowIndex", deviceRowIndex);
        
        if(ValidatorUtils.notNull(userVo)){
        	if (userVo.getFormUser().getUserDepartment().isIdNull()) {
				userVo.getFormUser().setUserDepartment(null);
			}
            User updatedUser = userManagementService.updateUserAtMakerStage(userVo);
            
            if ((null != schemeListId) && (!(schemeListId.equals(""))))
                userManagementService.updateUserProductSchemeMappings(schemeListId, updatedUser);
        }
        if (createAnotherMaster) {
            UserProfile userProfileObj = new UserProfile();
            map.put("userProfile", userProfileObj);
            map.put("masterID", masterId);
            setGlobalExpiryDaysInMap(map);
            flushCurrentTransaction();
            return "user";
        }
        flushCurrentTransaction();
        
        return "redirect:/app/UserInfo/loadColumnConfig";

    }

    /* public UserProfile copyUserProfile(UserProfile userProfile, UserProfile formUserProfile) {

    if (userProfile == null) {
    userProfile = new UserProfile();
    }
    userProfile.setPhotoUrl(formUserProfile.getPhotoUrl());
    if (formUserProfile.getSimpleContactInfo() != null) {
    SimpleContactInfo contactInfo = userProfile.getSimpleContactInfo();
    if (contactInfo == null) {
     contactInfo = new SimpleContactInfo();
    }
    if (formUserProfile.getSimpleContactInfo().getAddress() != null) {
     */
    /**To remove the mandatory check for address for a User**/
    /*
    if (formUserProfile.getSimpleContactInfo().getAddress().getCountry() != null
    && formUserProfile.getSimpleContactInfo().getAddress().getCountry().getId() != null) {
    contactInfo.setAddress(formUserProfile.getSimpleContactInfo().getAddress());
    } else {
    contactInfo.setAddress(null);
    }
    }
    if (formUserProfile.getSimpleContactInfo().getMobileNumber() != null) {
    contactInfo.setMobileNumber(formUserProfile.getSimpleContactInfo().getMobileNumber());
    }
    if (formUserProfile.getSimpleContactInfo().getPhoneNumber() != null) {
    contactInfo.setPhoneNumber(formUserProfile.getSimpleContactInfo().getPhoneNumber());
    }
    if (formUserProfile.getSimpleContactInfo().getEmail() != null) {
    contactInfo.setEmail(formUserProfile.getSimpleContactInfo().getEmail());
    }
    userProfile.setSimpleContactInfo(contactInfo);
    }
    if (formUserProfile.getFirstName() != null) {
    userProfile.setSalutation(formUserProfile.getSalutation());
    userProfile.setFirstName(formUserProfile.getFirstName());
    userProfile.setMiddleName(formUserProfile.getMiddleName());
    userProfile.setLastName(formUserProfile.getLastName());
    userProfile.setFourthName(formUserProfile.getFourthName());
    userProfile.setFullName(formUserProfile.getFullName());
    userProfile.setAliasName(formUserProfile.getAliasName());
    userProfile.setAddressRange(formUserProfile.getAddressRange());
    }
    if (userProfile.getAddressRange() != null && userProfile.getAddressRange().getIpaddress() != null
    && userProfile.getAddressRange().getIpaddress().length() == 0) {
    userProfile.getAddressRange().setIpaddress(null);
    } else if (userProfile.getAddressRange() != null && userProfile.getAddressRange().getFromIpAddress() != null
    && userProfile.getAddressRange().getFromIpAddress().length() == 0) {
    userProfile.getAddressRange().setFromIpAddress(null);
    userProfile.getAddressRange().setToIpAddress(null);
    }
    return userProfile;
    }*/

    @RequestMapping(value = "/setloggedinbranch")
    @ResponseBody
    public List<Map<String, Object>> setLoggedinBranch(@RequestParam("branchId") Long branchId,
            @RequestParam("allBranchesFlag") Boolean allBranchesFlag) {

        OrgBranchInfo orgBranchInfo = new OrgBranchInfo();
        UserInfo userInfo = getUserDetails();
        userInfo.setAllBranchesFlag(allBranchesFlag);
        if (allBranchesFlag) {
            orgBranchInfo.setOrgName("All Branches");
            orgBranchInfo.setId(Long.valueOf(-1));
        } else {
            OrganizationBranch organizationBranch = baseMasterService
                    .getMasterEntityById(OrganizationBranch.class, branchId);
            orgBranchInfo.setId(branchId);
            orgBranchInfo.setOrgName(organizationBranch.getName());
        }
        userInfo.setLoggedInBranch(orgBranchInfo);
        CoreUtility.syncSecurityContextHolderInSession(userInfo.getMappedSessionId());
        userBranchProductService.updateUserInfoLoggedInBranchProducts();
        return Collections.emptyList();
    }

    private void updateUserInfo() {
        UserInfo userInfo = getUserDetails();
        String sysName = SystemName.SOURCE_PRODUCT_TYPE_CAS;
        if (getUserDetails().getSysName() != null && getUserDetails().getSysName().getId() != null) {
            sysName = getUserDetails().getSysName().getCode();
        }
        List<OrganizationBranch> orgBranchList = userManagementServiceCore.getUserOrgBranches(userInfo.getId(), sysName);
        List<OrgBranchInfo> orgBranchInfoList = new ArrayList<OrgBranchInfo>();
        for (OrganizationBranch orgBranch : orgBranchList) {
            OrgBranchInfo orgBranchInfo = new OrgBranchInfo();
            orgBranchInfo.setId(orgBranch.getId());
            orgBranchInfo.setOrgName(orgBranch.getName());
            orgBranchInfoList.add(orgBranchInfo);
        }
        userInfo.setUserBranchList(orgBranchInfoList);
    }

    @RequestMapping(value = "/getLoggedInUserPhoto", method = RequestMethod.GET)
    @MonitoredWithSpring(name = "UAC_FETCH_LOGGED_IN_USR_PHOTO")
    @ResponseBody
    public void getLoggedInUserPhoto(ModelMap modelMap, HttpServletResponse response) throws IOException {
        UserInfo ui = getUserDetails();
        String userId = userService.getUserPhotoUrl(ui.getId());
        if(userId !=null && !userId.isEmpty()){
        	renderImageUtility.renderImage(userId,response);
        }
    }

    @SuppressWarnings("unchecked")
    @PreAuthorize("hasAuthority('MAKER_USERORGBRANCHMAPPING') or hasAuthority('CHECKER_USERORGBRANCHMAPPING') or hasAuthority('VIEW_USER')")
    @RequestMapping(value = "/view/branchAdmin")
    public String viewBranchAdmins(@RequestParam("userId") Long userId,
            @RequestParam("branchIds") List<Long> selectedBranchIds, ModelMap map) {
        boolean emptyBranches = false;
        boolean isBranchAdmin = false;
        /* to check if current logged in user is a branch admin of any Branch */
        User currentLoggedInUser = getUserDetails().getUserReference();
        if (currentLoggedInUser != null) {
            isBranchAdmin = userManagementServiceCore.getBranchAdminFlagForCurrentUser(currentLoggedInUser.getId());
            if (isBranchAdmin) {
                isBranchAdmin = true;
                map.put("isBranchAdmin", isBranchAdmin);
            } else {
                isBranchAdmin = false;
                map.put("isBranchAdmin", isBranchAdmin);
            }
        }

        if (selectedBranchIds == null || selectedBranchIds.isEmpty()) {
            emptyBranches = true;
            map.put("emptyBranches", emptyBranches);
            map.put("isBranchAdmin", isBranchAdmin);
            return "mapBranchAdmin";
        }

        /* Get all the branches where user being viewed is branch admin */
        List<OrganizationBranch> orgBranchesForBranchAdmin = organizationService.getBranchesWhereUserIsBranchAdmin(userId);

        List<Long> adminOfBranches = null;
        if (orgBranchesForBranchAdmin != null && !orgBranchesForBranchAdmin.isEmpty()) {
            adminOfBranches = new ArrayList<Long>();
            for (OrganizationBranch organizationBranch : orgBranchesForBranchAdmin) {
                adminOfBranches.add(organizationBranch.getId());
            }

            /*Post only those admin branches of this user which are under current logged in user's marked branches.  */
            adminOfBranches = (List<Long>) CollectionUtils.intersection(adminOfBranches, selectedBranchIds);
        }

        /*Post admin branches of this user if there is any under current logged in user's marked branches*/
        if (adminOfBranches != null && !adminOfBranches.isEmpty()) {
            map.put("adminOfBranches", adminOfBranches);
        }
        map.put("emptyBranches", emptyBranches);
        return "mapBranchAdmin";
    }

    @RequestMapping(value = "/branchAdminTree", method = RequestMethod.POST)
    @ResponseBody
    public List<OrgBranchInfo> getBranchAdminTree(@RequestParam("branchIds") List<Long> selectedBranchIds,
            @RequestParam("sysName") String sysName) {

        List<OrganizationBranch> organizationBranches = null;
        if (selectedBranchIds != null && !selectedBranchIds.isEmpty()) {

            /*Get all OrgBranches using there ids*/
            organizationBranches = new ArrayList<OrganizationBranch>();
            for (Long branchId : selectedBranchIds) {
                organizationBranches.add(organizationService.getOrganizationBranchById(branchId));
            }

            /*Preparing a map of all the selected Organization Branches with their child branches.*/
            Map<OrganizationBranch, List<OrganizationBranch>> orgBranchMap = new HashMap<OrganizationBranch, List<OrganizationBranch>>();
            List<OrganizationBranch> duplicateChildBranchesInMap = new ArrayList<OrganizationBranch>();

            for (OrganizationBranch orgBranchOuter : organizationBranches) {
                List<OrganizationBranch> childOrgBranches = new ArrayList<OrganizationBranch>();
                for (OrganizationBranch orgBranchInner : organizationBranches) {
                    if (!orgBranchOuter.equals(orgBranchInner)) {

                        String parentId = "_" + orgBranchOuter.getId().toString() + "_" + orgBranchInner.getId().toString()
                                + "_";

                        if (orgBranchInner.getParentBranchIds().contains(parentId)) {

                            childOrgBranches.add(orgBranchInner);
                        }
                    }
                }
                orgBranchMap.put(orgBranchOuter, childOrgBranches);
                duplicateChildBranchesInMap.addAll(childOrgBranches);
            }

            /* Remove duplicate child branches from OrgBeanchMap */
            for (OrganizationBranch organizationBranch : duplicateChildBranchesInMap) {
                orgBranchMap.remove(organizationBranch);
            }

            /*Create branch tree*/
            List<OrgBranchInfo> branchList = new ArrayList<OrgBranchInfo>();
            for (Map.Entry<OrganizationBranch, List<OrganizationBranch>> orgBranch : orgBranchMap.entrySet()) {
                OrgBranchInfo branchTree = new OrgBranchInfo();
                branchTree.setTitle(orgBranch.getKey().getName());
                branchTree.setKey((orgBranch.getKey().getId()).toString());
                List<OrganizationBranch> childBranches =orgBranch.getValue();
                if (childBranches != null && !childBranches.isEmpty()) {

                    branchTree.setLazy(false);

                    List<OrgBranchInfo> childList = new ArrayList<OrgBranchInfo>();
                    for (OrganizationBranch child : childBranches) {
                        OrgBranchInfo childTree = new OrgBranchInfo();
                        childTree.setTitle(child.getName());
                        childTree.setKey(child.getId().toString());
                        childList.add(childTree);

                    }
                    branchTree.setChildren(childList);
                } else {
                    branchTree.setLazy(false);
                }

                branchList.add(branchTree);
            }
            Collections.sort(branchList, new Comparator<OrgBranchInfo>() {
                @Override
                public int compare(OrgBranchInfo o1, OrgBranchInfo o2) {

                    int res = String.CASE_INSENSITIVE_ORDER.compare(o1.getTitle(), o2.getTitle());
                    if (res == 0) {
                        res = o1.getTitle().compareTo(o2.getTitle());
                    }
                    return res;
                }
            });
            return branchList;
        }

        return Collections.emptyList();
    }

    @RequestMapping(value = "/askSecurityQuestion")
    public String getSecurityQuestions(@RequestParam("username") String username, ModelMap map) {
        List<UserSecurityQuestion> userSecurityQuestionList = null;
        List<String> questionsList = new ArrayList<String>();

        if (username != null && !"".equals(username)) {
            userSecurityQuestionList = userService.getUserSecurityQuestions(username);
        }

        if (CollectionUtils.isNotEmpty(userSecurityQuestionList)) {
            for (UserSecurityQuestion userSecurityQuestion : userSecurityQuestionList) {
                if (userSecurityQuestion.getName() != null) {
                    questionsList.add(userSecurityQuestion.getName());
                }
            }
            map.put("userSecurityQuestionList", userSecurityQuestionList);
        }
        map.put("username", username);
        return "securityQuestionsPage";
    }

    @RequestMapping(value = "/checkAnswers")
    @ResponseBody
    public String checkAnswers(@RequestParam("answerArray") String[] answerArray, @RequestParam("quesArray") Long[] quesArray,
            @RequestParam("username") String username, ModelMap map) {
        Map<Long, String> questionAnswerMap = userService.getUserQuestionAnswerMap(username);
        String answer = null;
        boolean flag = false;
        for (int i = 0 ; i < quesArray.length ; i++) {
            answer = questionAnswerMap.get(quesArray[i]);
            if (answer.equalsIgnoreCase(answerArray[i])) {
                flag = true;
            } else {
                flag = false;
                break;
            }
        }

        if (flag) {
            // unblock the user
            User user = userService.findUserByUsername(username);
            if(user.isAccountLocked()){
            	 userService.activateUser(user);
                 return "success";
            }
            return "alreadyActive";
        } else {
            return "failure";
        }

    }

    @RequestMapping(value = "/checkSecurityAnswers", method = RequestMethod.POST)
    public String checkSecurityAnswers(@RequestParam("answerArray") String[] answerArray,
            @RequestParam("quesArray") Long[] quesArray, @RequestParam("username") String username,
            @RequestParam("token") String token, ModelMap map) {
        Map<Long, String> questionAnswerMap = userService.getUserQuestionAnswerMap(username);
        String answer = null;
        boolean flag = false;
        String tokenId = token;
        for (int i = 0 ; i < quesArray.length ; i++) {
            answer = questionAnswerMap.get(quesArray[i]);
            if (answer.equalsIgnoreCase(answerArray[i])) {
                flag = true;
            } else {
                flag = false;
                break;
            }
        }
        if (flag) {
            BaseLoggers.flowLogger.debug("Checking the Time bound Token Expiry");
            User user = userService.findUserByPasswordResetTimeToken(tokenId);
            if (user != null) {
                map.put("userId", user.getId());
                if (user.getPasswordResetToken() != null) {
                    map.put("token", tokenId);
                    map.put("tokenized", true);
                    map.put("tokenValid", authenticationTokenService.isTokenValid(user.getId(), tokenId));
                    map.put("username",username);

                } else {
                    map.put("tokenValid", "false");
                }
            }
            return "resetPassword";
        }
        map.put("errorMessage", "Security answer not match");
        return "resetPasswordErrorPage";

    }

    @RequestMapping(value = "/view/accessToBranches")
    public String viewUserAccessToBranches(@RequestParam("userId") Long userId, ModelMap map) {
        List<String> userBranchAccessList = userService.fetchAccessBranchesToCurrentUser(userId);
        map.put("branches", userBranchAccessList);
        return "userAccessBranches";
    }

    /*@ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
    String handleExceptions(Exception be, HttpServletRequest request, HttpServletResponse res) throws IOException {
        BaseLoggers.flowLogger.error(be.toString());
        request.setAttribute("errorMessageObject", be.toString());
        request.setAttribute("errorMessage", "Access Denied");
        return "accessDenied";
    }*/

    @RequestMapping("/validationActiveTasksCheck/{multipleIds}")
    @ResponseBody
    public List<String> getValidationActiveTasksCheck(@PathVariable("multipleIds") String[] uidList, ModelMap map) {
        List<String> userWithActiveTasks = new ArrayList<String>();

        if(!("TRUE".equalsIgnoreCase(configurationService.getPropertyValueByPropertyKey("task.count.service.use", "Configuration.getPropertyValueFromPropertyKey")))){
            for (String uid : uidList) {
                Map<String, ?> userDetails = userManagementServiceCore.findUserById(Long.parseLong(uid));
                User user = (User) userDetails.get("user");
                if (notNull(user) &&  !NEUTRINO_SYSTEM_USER.equalsIgnoreCase(user.getUsername()) && !bpmnProcessService.getAssignedTasksForUser(user.getUri(), null).isEmpty()) {
                        userWithActiveTasks.add(user.getUsername());
                }
            }
        } else {
            List<String> userUri = new ArrayList<>();
            for(String uid : uidList){
                userUri.add(EntityId.getUri(Long.parseLong(uid), User.class));
            }
            TaskCountRequest taskCountRequest = getTaskCountRequest(userUri);
            HttpHeaders headers = getHttpHeaders();
            HttpEntity<Object> entityReq = new HttpEntity<>(taskCountRequest, headers);
            ResponseEntity<String> response = restTemplate.exchange(taskCountUrl, HttpMethod.POST, entityReq, String.class);
            TaskCountResponse taskCountResponse = getTaskCountResponse(response);
            if(taskCountResponse!=null && taskCountResponse.getUriCountMap() != null) {
                Map<String,Long> mapResult = taskCountResponse.getUriCountMap();
                for (Map.Entry<String,Long> entry : mapResult.entrySet()) {
                    Long taskCountForUser = entry.getValue();
                    if (taskCountForUser != null && taskCountForUser > 0) {
                        EntityId entityId = EntityId.fromUri(entry.getKey());
                        if(entityId != null){
                            Long userId = entityId.getLocalId();
                            Map<String, ?> userDetails = userManagementServiceCore.findUserById(userId);
                            User user = (User) userDetails.get("user");
                            userWithActiveTasks.add(user.getUsername());
                        }
                    }
                }
            }
        }
        return userWithActiveTasks;
    }

    private HttpHeaders getHttpHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.set(OAuth2AccessToken.ACCESS_TOKEN ,restAuthenticationService.getSecurityToken(clientID));
        return headers;
    }

    private TaskCountRequest getTaskCountRequest(List<String> userUri) {
        TaskCountRequest taskCountRequest = new TaskCountRequest();
        taskCountRequest.setUserUri(userUri);
        return taskCountRequest;
    }

    private TaskCountResponse getTaskCountResponse(ResponseEntity<String> response) {
        TaskCountResponse taskCountResponse= null;
        if(response != null && StringUtils.isNotEmpty(response.getBody())){
            taskCountResponse=new Gson().fromJson(response.getBody(),TaskCountResponse.class);
        }
        return taskCountResponse;
    }


    @SuppressWarnings({ })
    @RequestMapping(value = "/saveAndSendForApproval", method = RequestMethod.POST)
    public String saveUser(UserManagementForm userManagementForm,
            @RequestParam("selectedBranchesForUser") List<Long> selectedBranchesList,
            @RequestParam("originalOrgBranchList") List<Long> originalOrgBranchList,
            @RequestParam(value = "orgAdminBranches", required = false) List<Long> originalAdminBranches,
            BindingResult result, ModelMap map,
            @RequestParam(value = "createAnotherMaster", required = false) boolean createAnotherMaster,
            @RequestParam(value = "changedBranchList", required = false) String changedBranchList,
            @RequestParam(value = "changedProductList", required = false) String changedProductList,
            @RequestParam(value = "changedTeamList", required = false) String changedTeamList,
            @RequestParam(value = "cityAreaMap",required = false) String cityAreaMap,
            @RequestParam(value = "villageAreaMap",required = false) String villageAreaMap,
            @RequestParam(value="schemeListId", required=false) String schemeListId) {

        UserVO userVo = prepareUserDataForSaveModified(userManagementForm, changedBranchList, changedProductList, changedTeamList,cityAreaMap,villageAreaMap);
        if(ValidatorUtils.notNull(userVo)){
        	if (userVo.getFormUser().getUserDepartment().isIdNull()) {
				userVo.getFormUser().setUserDepartment(null);
			}
            User updatedUser = userManagementService.updateUserAtMakerStageSendForApproval(userVo);
            
            if ((null != schemeListId) && (!(schemeListId.equals(""))))
                userManagementService.updateUserProductSchemeMappings(schemeListId, updatedUser);
        }
        if (createAnotherMaster) {
            UserProfile userProfileObj = new UserProfile();
            map.put("userProfile", userProfileObj);
            map.put("masterID", masterId);
            setGlobalExpiryDaysInMap(map);
            flushCurrentTransaction();
            return "user";
        }
        flushCurrentTransaction();
        
        return "redirect:/app/UserInfo/loadColumnConfig";

    
    
    
    }

    public void saveUserPreferences(UserManagementForm userManagementForm) {
        List<ConfigurationVO> configVOList = new ArrayList<ConfigurationVO>();
        Map<String, ConfigurationVO> userPreferences = configurationService
                .getFinalUserModifiableConfigurationForEntity(userManagementForm.getAssociatedUser().getEntityId());
        String temp = null;

        if (userManagementForm.getConfigVOList() != null && !userManagementForm.getConfigVOList().isEmpty()) {
            for (ConfigurationVO configVO : userManagementForm.getConfigVOList()) {
                ConfigurationVO orgConfig = userPreferences.get(configVO.getPropertyKey());
                orgConfig.setOverride(configVO.isOverride());
                if (orgConfig.getValueType().toString().equalsIgnoreCase(ValueType.NORMAL_TEXT.toString())
                        && (userManagementForm.getMyFavs() != null)
                        && ("config.notifications.myFavorites".equalsIgnoreCase(orgConfig.getPropertyKey()))) {

                    temp = convertListToString(userManagementForm.getMyFavs());
                    orgConfig.setText(temp);
                }

                else if (orgConfig.getValueType().toString().equalsIgnoreCase(ValueType.NORMAL_TEXT.toString())
                        && configVO.getText() != null) {
                    orgConfig.setText(configVO.getText());
                }

                else if ((orgConfig.getValueType().toString().equalsIgnoreCase(ValueType.DATE.toString()) || orgConfig
                        .getValueType().toString().equalsIgnoreCase(ValueType.TIME.toString()))
                        && (configVO.getDate() != null)) {
                    orgConfig.setDate(configVO.getDate());
                } else if ((orgConfig.getValueType().toString().equalsIgnoreCase(ValueType.DATE_RANGE.toString()) || orgConfig
                        .getValueType().toString().equalsIgnoreCase(ValueType.TIME_RANGE.toString()))
                        && (configVO.getFromDate() != null && configVO.getToDate() != null)) {
                    orgConfig.setFromDate(configVO.getFromDate());
                    orgConfig.setToDate(configVO.getToDate());
                } else if ((orgConfig.getValueType().toString().equalsIgnoreCase(ValueType.DAY_OF_WEEK.toString()))
                        && (configVO.getDay() != null)) {
                    orgConfig.setDay(configVO.getDay());
                } else if ((orgConfig.getValueType().toString().equalsIgnoreCase(ValueType.DAYS_OF_WEEK_RANGE.toString()))
                        && (configVO.getFromDay() != null && configVO.getToDay() != null)) {
                    orgConfig.setFromDay(configVO.getFromDay());
                    orgConfig.setToDay(configVO.getToDay());
                } else if ((orgConfig.getValueType().toString().equalsIgnoreCase(ValueType.BOOLEAN_VALUE.toString()))
                        && (configVO.getConfigurable() != null)) {
                    orgConfig.setConfigurable(configVO.isConfigurable());
                }

                configVOList.add(orgConfig);
            }

            configurationService.syncConfiguration(userManagementForm.getAssociatedUser().getEntityId(), configVOList);
        }
    }

    public String convertListToString(List<String> myList) {
        StringBuilder newString = new StringBuilder();
        for (Iterator<String> it = myList.iterator() ; it.hasNext() ;) {
            newString.append(it.next());
            if (it.hasNext()) {
                newString.append(",");
            }
        }
        return newString.toString();
    }

    @RequestMapping(value = "/setloggedinbranchAndProduct")
    @MonitoredWithSpring(name = "UAPMC_SET_LOGGED_IN_BRANCH")
    @ResponseBody
    public List<Map<String, Object>> setLoggedinBranch(@RequestParam("branchId") Long branchId) {

        OrganizationBranch organizationBranch = baseMasterService.getMasterEntityById(OrganizationBranch.class, branchId);
        OrgBranchInfo orgBranchInfo = new OrgBranchInfo();
        orgBranchInfo.setId(organizationBranch.getId());
        orgBranchInfo.setOrgName(organizationBranch.getName());
        UserInfo userInfo = getUserDetails();
        userInfo.setLoggedInBranch(orgBranchInfo);
        CoreUtility.syncSecurityContextHolderInSession(userInfo.getMappedSessionId());
        userBranchProductService.updateUserInfoLoggedInBranchProducts();
        return Collections.emptyList();
    }
  

    @SuppressWarnings("unused")
    private void saveUserAuditLog(UserManagementForm userManagementForm, String event) {
        UserAuditLog userAuditLog = new UserAuditLog();
        userAuditLog.setUserModificationString(populateUserAuditDetails(userManagementForm));
        if (userManagementForm.getAssociatedUser() != null) {
            userAuditLog.setUserId(userManagementForm.getAssociatedUser().getId());
        }
        if (getUserDetails().getUserEntityId() != null) {
            userAuditLog.setUserEntityId(getUserDetails().getUserEntityId().getLocalId());
        }
        if (event.equalsIgnoreCase(USER_CREATE_EVENT)) {
            userAuditLog.setVersion(0);
        } else {
            if (userManagementForm.getAssociatedUser() != null) {
                Integer version = userService.getLatestVersionOfAuditForUser(userManagementForm.getAssociatedUser().getId());
                if (version != null) {
                    version = version + 1;
                    userAuditLog.setVersion(version);
                } else {
                    version = 0;
                    userAuditLog.setVersion(version);
                }
            }
        }
        userAuditLog.setUserEvent(event);
        userService.saveUserAuditLog(userAuditLog);
    }
    
    private UserVO prepareUserDataForSave(UserManagementForm userManagementForm, String changedBranchList,
            String changedProductList, String changedTeamList,String cityAreaMap,String villageAreaMap)
    {






         User formUser = userManagementForm.getAssociatedUser();
         UserVO userVo = new UserVO();
         formUser.setSecurityQuestionAnswers(userService.getUserSecurityQuestionAnswer(formUser.getUsername()));
         /*
          * 1. Get Address from UI 
          */
         UserProfile formUserProfile = userManagementForm.getUserprofile();
         if(formUserProfile!=null && formUserProfile.getUserAccessType()!=null && formUserProfile.getUserAccessType().getId()!=null){
            formUserProfile.setUserAccessType(entityDao.find(AccessType.class, formUserProfile.getUserAccessType().getId()));
         } else{
             formUserProfile.setUserAccessType(null);
         }

        /*
         *2. Get all mapped role Ids 
         */
         
         List<Long> roleIds = new ArrayList<Long>();
       	 if(formUser.isLoginEnabled()){
			if (null != userManagementForm.getRoleIds()) {
				userVo.setRoleMappings(userManagementForm.getRoleIds());
			}
				else {
					List<Role> userRoles = userService.getRolesFromUserId(formUser.getId());
				for (Role role : userRoles) {
					roleIds.add(role.getId());
				}
				 userVo.setRoleMappings(roleIds.toArray(new Long[roleIds.size()]));
			}
		 }
       	 else{
       		 userVo.setRoleMappings(roleIds.toArray(new Long[roleIds.size()]));
       
       	 }
		/*
          * 3. & 4. Get updated branch mapping data in form of map - including branch admin (addition and modification)
          */
         List<Map<String, Object>> updatedUserOrgBranchMappings = userAdminHelper
                .parseUserOrgBranchMappingString(changedBranchList);
         userVo.setUpdatedUserOrgBranchMappings(updatedUserOrgBranchMappings);
         /*
          * 5. Get updated User-Branch-Product mappings 
          */
         Map<Long,List<Map<Long, String>>> updatedUserOrgBranchProductMappings = userAdminHelper
                .parseUserOrgBranchProductMappingString(changedProductList);
         userVo.setUpdatedUserOrgBranchProductMappings(updatedUserOrgBranchProductMappings);
         
         /*
          * 6. Get updated Communication Detail (already part of user profile ) - here update email into user
          */
         userAdminHelper.updateEmailOfUser(formUserProfile, formUser);
         /*
          * 7. Get Updated preferences -  
          */
        if (null != userManagementForm.getConfigVOList()) {
            userVo.setUpdatedUserPreferences(userManagementForm.getConfigVOList());
        } else {
            Map<String, ConfigurationVO> preferences = configurationService
                    .getFinalUserModifiableConfigurationForEntity(formUser.getEntityId());
            if (preferences != null) {
                List<ConfigurationVO> updatedUserPreferences = new ArrayList<ConfigurationVO>();
                for (ConfigurationVO configVal : preferences.values()) {
                    updatedUserPreferences.add(configVal);
                }
                userVo.setUpdatedUserPreferences(updatedUserPreferences);
            }
        }
            userVo.setUserDefaultUrlMappingVOList(userManagementForm.getUserDefaultUrlMappingVOList());
            userVo.setDeletedUserUrlMappings(userManagementForm.getDeletedUserUrlMappings());

        userVo.setMyFavs(userManagementForm.getMyFavs());
         /*
          * 8. Get updated team data
          */
       if(formUser.isLoginEnabled() || formUser.isBusinessPartner()){
    	   if (null != userManagementForm.getTeamIds()) {
			userVo.setTeamMappings(userManagementForm.getTeamIds());
    	   } else {
			List<Long> teamIds = teamService.getTeamIdAssociatedToUserByUserId(formUser.getId());
			userVo.setTeamMappings(teamIds.toArray(new Long[teamIds.size()]));
    	   }
		
		}
		else {
				formUser.setTeamLead(false);
				userVo.setTeamMappings(	new Long[0]);
			}
		
         /*Map<Long, String> teamMappings =  userAdminHelper.parseTeamMappingString(changedTeamList);
         userVo.setTeamMappings(teamMappings);*/
         
         /*
          * 9. updated Module Name
          * 
          */
         if (null == formUser.getSysName()) {
             UserInfo persistedUserInfo = userService.getUserById(formUser.getId());
             formUser.setSysName(persistedUserInfo.getSysName());
         }
         
         /*
          * 10. Update Mobility information in user 
          */        
         


	
		
		UserMobilityInfo userMobilityInfo = userManagementForm.getUserMobilityInfo();
		if(!formUser.isLoginEnabled())	
		{	
			if(userMobilityInfo == null){
				userMobilityInfo = new UserMobilityInfo();
			}
			userMobilityInfo.setIsChallengeEnabled(false);
			userMobilityInfo.setIsMobileEnabled(false);
			userMobilityInfo.setIsDeviceAuthEnabled(false);
			userMobilityInfo.setChallenge(null);
		}
		
			userAdminHelper.updateMobilityInfoInUser(userMobilityInfo, formUser);
		
		
			
//	    	====================Only for POC=========================
	    	
//	    	 Thread.currentThread().stop();
	    	
//	    	ThreadGroup root = Thread.currentThread().getThreadGroup().getParent();
//	        while (root.getParent() != null) {
//	            root = root.getParent();
//	        }
//	        try {
//				visit(root, 0, threadName);
//			} catch (Exception e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//				throw new ServiceInputException("THREAD KILLED ");
//			}
//	    	==============================POC END================	
			
         /*
          * 11. Get user supplied default branch
          */
         Long defaultBranch = userManagementForm.getDefaultBranch();
         userVo.setDefaultBranch(defaultBranch);
         
         /*
          * 12. Get if user is team lead or business partner
          */
        if (formUser.isBusinessPartner()) {
        	
        	
            if (null != userManagementForm.getMappedBPid()) {
                userVo.setMappedBPId(userManagementForm.getMappedBPid());
            } else {
                Long associatedBPId = null;
                associatedBPId = userBPMappingService.getAssociatedBPIdByUserId(formUser.getId());
                if (null != associatedBPId)
                    userVo.setMappedBPId(associatedBPId);
            }
        }
         
        /*
         * 13. Check if non-login and set other properties accordingly
         * 
         */
        if(!formUser.isLoginEnabled()){
        	formUser.setSuperAdmin(false);

        }
        
         /*
          * 14. Update miscellaneous information in user 
          */
         userAdminHelper.updateMiscellaneousInfoInUser(formUser);
		 
		 /*
         * 15. Update user city village mapping
         */
        userVo.setCityVillageMapping(parseUserCityVillageMapping(cityAreaMap, villageAreaMap));
        
         formUserProfile.setAssociatedUser(formUser);
         userVo.setFormUser(formUser);
         userVo.setFormUserProfile(formUserProfile);
        //handle user data for address village and tehsil
        if(userManagementForm != null && userManagementForm.getUserprofile() != null && userManagementForm.getUserprofile().getSimpleContactInfo() != null
                && userManagementForm.getUserprofile().getSimpleContactInfo().getAddress() != null) {
            addressService.handleVillageAndTehsilMaster(userManagementForm.getUserprofile().getSimpleContactInfo().getAddress());
            Address address=userManagementForm.getUserprofile().getSimpleContactInfo().getAddress();
            if(address.getCity()!=null && address.getCity().getId()==null){
            	address.setCity(null);
            }
            if(address.getRegion()!=null && address.getRegion().getId()==null){
            	address.setRegion(null);
            }
            if(address.getDistrict()!=null && address.getDistrict().getId()==null){
            	address.setDistrict(null);
            }
        }
        UserCalendar calendar = userManagementServiceCore.getUserCalendarByUserId(formUser.getId());
        if(formUser.getUserCalendar() == null && calendar != null){        	
        	formUser.setUserCalendar(calendar);
        } else if(formUser.getUserCalendar() != null && formUser.getUserCalendar().getId() == null && calendar != null){
        	formUser.getUserCalendar().setId(calendar.getId());
        }        
        return userVo; 
        
    }

    private UserVO prepareUserDataForSaveModified(UserManagementForm userManagementForm, String changedBranchList,
                                          String changedProductList, String changedTeamList,String cityAreaMap,String villageAreaMap)
    {






        User formUser = userManagementForm.getAssociatedUser();
        UserVO userVo = new UserVO();
        formUser.setSecurityQuestionAnswers(userService.getUserSecurityQuestionAnswer(formUser.getUsername()));
        /*
         * 1. Get Address from UI
         */
        UserProfile formUserProfile = userManagementForm.getUserprofile();
        if(formUserProfile!=null && formUserProfile.getUserAccessType()!=null && formUserProfile.getUserAccessType().getId()!=null){
            formUserProfile.setUserAccessType(entityDao.find(AccessType.class, formUserProfile.getUserAccessType().getId()));
        } else{
            formUserProfile.setUserAccessType(null);
        }

        /*
         *2. Get all mapped role Ids
         */

        List<Long> roleIds = new ArrayList<Long>();
        if(formUser.isLoginEnabled()){
            if (null != userManagementForm.getRoleIds()) {
                userVo.setRoleMappings(userManagementForm.getRoleIds());
            }
            else {
                List<Role> userRoles = userService.getRolesFromUserId(formUser.getId());
                for (Role role : userRoles) {
                    roleIds.add(role.getId());
                }
                userVo.setRoleMappings(roleIds.toArray(new Long[roleIds.size()]));
            }
        }
        else{
            userVo.setRoleMappings(roleIds.toArray(new Long[roleIds.size()]));

        }
        /*
         * 3. & 4. Get updated branch mapping data in form of map - including branch admin (addition and modification)
         */
        List<Map<String, Object>> updatedUserOrgBranchMappings = userAdminHelper
                .parseUserOrgBranchMappingString(changedBranchList);
        userVo.setUpdatedUserOrgBranchMappings(updatedUserOrgBranchMappings);
        /*
         * 5. Get updated User-Branch-Product mappings
         */
        Map<String,String> updatedUserOrgBranchProductMappings = userAdminHelper
                .parseUserOrgBranchProductMappingStringModified(changedProductList);
        userVo.setUpdatedUserOrgBranchProductMappingsModified(updatedUserOrgBranchProductMappings);

        /*
         * 6. Get updated Communication Detail (already part of user profile ) - here update email into user
         */
        userAdminHelper.updateEmailOfUser(formUserProfile, formUser);
        /*
         * 7. Get Updated preferences -
         */
        if (null != userManagementForm.getConfigVOList()) {
            userVo.setUpdatedUserPreferences(userManagementForm.getConfigVOList());
        } else {
            Map<String, ConfigurationVO> preferences = configurationService
                    .getFinalUserModifiableConfigurationForEntity(formUser.getEntityId());
            if (preferences != null) {
                List<ConfigurationVO> updatedUserPreferences = new ArrayList<ConfigurationVO>();
                for (ConfigurationVO configVal : preferences.values()) {
                    updatedUserPreferences.add(configVal);
                }
                userVo.setUpdatedUserPreferences(updatedUserPreferences);
            }
        }
        userVo.setUserDefaultUrlMappingVOList(userManagementForm.getUserDefaultUrlMappingVOList());
        userVo.setDeletedUserUrlMappings(userManagementForm.getDeletedUserUrlMappings());

        userVo.setMyFavs(userManagementForm.getMyFavs());
        /*
         * 8. Get updated team data
         */
        if(formUser.isLoginEnabled() || formUser.isBusinessPartner()){
            if (null != userManagementForm.getTeamIds()) {
                userVo.setTeamMappings(userManagementForm.getTeamIds());
            } else {
                List<Long> teamIds = teamService.getTeamIdAssociatedToUserByUserId(formUser.getId());
                userVo.setTeamMappings(teamIds.toArray(new Long[teamIds.size()]));
            }

        }
        else {
            formUser.setTeamLead(false);
            userVo.setTeamMappings(	new Long[0]);
        }

         /*Map<Long, String> teamMappings =  userAdminHelper.parseTeamMappingString(changedTeamList);
         userVo.setTeamMappings(teamMappings);*/

        /*
         * 9. updated Module Name
         *
         */
        if (null == formUser.getSysName()) {
            UserInfo persistedUserInfo = userService.getUserById(formUser.getId());
            formUser.setSysName(persistedUserInfo.getSysName());
        }

        /*
         * 10. Update Mobility information in user
         */





        UserMobilityInfo userMobilityInfo = userManagementForm.getUserMobilityInfo();
        if(!formUser.isLoginEnabled())
        {
            if(userMobilityInfo == null){
                userMobilityInfo = new UserMobilityInfo();
            }
            userMobilityInfo.setIsChallengeEnabled(false);
            userMobilityInfo.setIsMobileEnabled(false);
            userMobilityInfo.setIsDeviceAuthEnabled(false);
            userMobilityInfo.setChallenge(null);
        }

        userAdminHelper.updateMobilityInfoInUser(userMobilityInfo, formUser);



//	    	====================Only for POC=========================

//	    	 Thread.currentThread().stop();

//	    	ThreadGroup root = Thread.currentThread().getThreadGroup().getParent();
//	        while (root.getParent() != null) {
//	            root = root.getParent();
//	        }
//	        try {
//				visit(root, 0, threadName);
//			} catch (Exception e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//				throw new ServiceInputException("THREAD KILLED ");
//			}
//	    	==============================POC END================

        /*
         * 11. Get user supplied default branch
         */
        Long defaultBranch = userManagementForm.getDefaultBranch();
        userVo.setDefaultBranch(defaultBranch);

        /*
         * 12. Get if user is team lead or business partner
         */
        if (formUser.isBusinessPartner()) {


            if (null != userManagementForm.getMappedBPid()) {
                userVo.setMappedBPId(userManagementForm.getMappedBPid());
            } else {
                Long associatedBPId = null;
                associatedBPId = userBPMappingService.getAssociatedBPIdByUserId(formUser.getId());
                if (null != associatedBPId)
                    userVo.setMappedBPId(associatedBPId);
            }
        }

        /*
         * 13. Check if non-login and set other properties accordingly
         *
         */
        if(!formUser.isLoginEnabled()){
            formUser.setSuperAdmin(false);

        }

        /*
         * 14. Update miscellaneous information in user
         */
        userAdminHelper.updateMiscellaneousInfoInUser(formUser);

        /*
         * 15. Update user city village mapping
         */
        userVo.setCityVillageMapping(parseUserCityVillageMapping(cityAreaMap, villageAreaMap));

        formUserProfile.setAssociatedUser(formUser);
        userVo.setFormUser(formUser);
        userVo.setFormUserProfile(formUserProfile);
        //handle user data for address village and tehsil
        if(userManagementForm != null && userManagementForm.getUserprofile() != null && userManagementForm.getUserprofile().getSimpleContactInfo() != null
                && userManagementForm.getUserprofile().getSimpleContactInfo().getAddress() != null) {
            addressService.handleVillageAndTehsilMaster(userManagementForm.getUserprofile().getSimpleContactInfo().getAddress());
            Address address=userManagementForm.getUserprofile().getSimpleContactInfo().getAddress();
            if(address.getCity()!=null && address.getCity().getId()==null){
                address.setCity(null);
            }
            if(address.getRegion()!=null && address.getRegion().getId()==null){
                address.setRegion(null);
            }
            if(address.getDistrict()!=null && address.getDistrict().getId()==null){
                address.setDistrict(null);
            }
        }
        UserCalendar calendar = userManagementServiceCore.getUserCalendarByUserId(formUser.getId());
        if(formUser.getUserCalendar() == null && calendar != null){
            formUser.setUserCalendar(calendar);
        } else if(formUser.getUserCalendar() != null && formUser.getUserCalendar().getId() == null && calendar != null){
            formUser.getUserCalendar().setId(calendar.getId());
        }
        return userVo;

    }
    
    @RequestMapping(value = "/getUserAuditLog")
    public String retrieveUserAuditLogs(@RequestParam("originalUserId") Long originalUserId,@RequestParam("changedUserId") Long changedUserId,ModelMap map) {
        NeutrinoValidator.notNull(originalUserId, "userAuditLogId Cannot be null");
        NeutrinoValidator.notNull(changedUserId, "userId Cannot be null");
        List<RecordComparatorVO> recordChanges=userManagementService.getUserAuditLog(originalUserId,changedUserId);
        map.put("recordChanges", recordChanges);
        return "userRecordChanges";
    }

    
    @PreAuthorize("hasAuthority('MAKER_USERMOBILITYINFO') or hasAuthority('CHECKER_USERMOBILITYINFO') or hasAuthority('VIEW_USER')")
    @RequestMapping(value = "/newRow")
    public String getNewRowForDeviceMapping(@RequestParam("index") int index, ModelMap map){
    	map.put("index", index);
    	return "newDeviceMappingRow";
    }


     private UserCityVillageMapping parseUserCityVillageMapping(String cityAreaMap,String villageAreaMap){

        JSONDeserializer validateMap = new JSONDeserializer();
        HashMap<String,List<String>> cityMap = new HashMap();
        HashMap<String,List<String>>  villageMap = new HashMap();
        if(cityAreaMap != null && !cityAreaMap.trim().isEmpty()){
        	cityMap = (HashMap<String,List<String>> )validateMap.deserialize(cityAreaMap);
        }
        if(villageAreaMap != null && !villageAreaMap.trim().isEmpty()){
        	villageMap =(HashMap<String,List<String>> )validateMap.deserialize(villageAreaMap);
        }
        List<UserCityMapping> userCityMappingList = new ArrayList<>();
        List<UserVillageMapping> userVillageMappingList = new ArrayList<>();

        for(String key:cityMap.keySet()){
            UserCityMapping userCityMapping = new UserCityMapping();
            userCityMapping.setCity(baseMasterService.getEntityByEntityId(EntityId.fromUri(key)));


            List<Area> areaList = new ArrayList<>();
            for(String areaKey : cityMap.get(key)){
                areaList.add(baseMasterService.getEntityByEntityId(EntityId.fromUri(areaKey)));
            }
            userCityMapping.setCityAreaList(areaList);
            userCityMappingList.add(userCityMapping);
        }

        for(String key:villageMap.keySet()){
            UserVillageMapping userVillageMapping = new UserVillageMapping();
            userVillageMapping.setVillageMaster(baseMasterService.getEntityByEntityId(EntityId.fromUri(key)));

            List<Area> areaList = new ArrayList<>();
            for(String areaKey : villageMap.get(key)){
                areaList.add(baseMasterService.getEntityByEntityId(EntityId.fromUri(areaKey)));
            }
            userVillageMapping.setVillageAreaList(areaList);
            userVillageMappingList.add(userVillageMapping);
        }
        UserCityVillageMapping userCityVillageMapping = new UserCityVillageMapping();
        userCityVillageMapping.setUserCityMappings(userCityMappingList);
        userCityVillageMapping.setUserVillageMappings(userVillageMappingList);

        return userCityVillageMapping;
    }

    @RequestMapping(value = "/resetPasswordSecurityQuestion/forgotSecurityQuestion", method = RequestMethod.POST)
    @ResponseBody
    public String verifySecurityAnswers(@RequestParam("username") String username,@RequestParam("token") String token, HttpServletRequest request) {

        BaseLoggers.flowLogger.debug("Checking the Time bound Token Expiry");
        User user = userService.findUserByPasswordResetTimeToken(token);
        if (user != null) {
            if (user.getUsername().equalsIgnoreCase(username) && authenticationTokenService.isTokenValid(user.getId(), token)) {

                Map<String, String> mapKeys = new HashMap<String, String>();
                mapKeys.put("USER", user.getDisplayName());

                try {
                    adminController.mailhelper(mapKeys, user, token, request);
                    return "success";
                } catch (NullPointerException e) {
                    return "Error,User's E-mail Id not provided. Password can not be reset !!,error";
                } catch (MessagingException e) {
                    return "Error,Mesage Exception Ocurred !!,error";
                } catch (IOException e) {
                    return "Error,IO Exception Ocurred !!,error";
                }

            }
        }
        return "failure";
    }
	
	@RequestMapping(value = "/validatePincodeValue", method = RequestMethod.GET)
    @ResponseBody
    public String validatePincodeValue(@RequestParam("stateId") String stateId, @RequestParam("zipcodeValue") String zipcodeValue) {

        int validationFlag = 0;
        if(stateId.equals("") || zipcodeValue.equals("")){
            return "";
        }
        State stateObj = addressService.getStateAttributes(Long.parseLong(stateId));
        if(stateObj!=null){
            validationFlag = addressService.validateCustomPincodeValue(zipcodeValue,stateObj);
        }
        if(validationFlag == -1){
            return "Invalid Pincode";
        }else{
            return "Valid Pincode";
        }
	}

    @PreAuthorize("hasAuthority('MAKER_USERORGBRANCHMAPPING') or hasAuthority('CHECKER_USERORGBRANCHMAPPING') or hasAuthority('VIEW_USER')")
	@RequestMapping(value = "/view/loadCityVillageMappingDiv")
    public String loadCityVillageMappingDiv(ModelMap map){

        return "mapVillageBranch";
    }

    @RequestMapping(value="/view/orgCitiesForCityVillageMapping")
    @ResponseBody
    public String orgCitiesForCityVillageMapping(@RequestParam("orgbranchIds[]") Long[] orgBranchIds){
        List<TreeInfoVO> servedCityTreeVO = new ArrayList<>();

        HashMap<String,ArrayList<Long>> cityBranchMap = new HashMap<>();

        for(Long orgBranchId : orgBranchIds) {
            OrganizationBranch orgBranch = entityDao.find(OrganizationBranch.class, orgBranchId);
            List<City> servedCities = orgBranch.getServedCities();
            if(CollectionUtils.isNotEmpty(servedCities)){
	            for(City servedCity : servedCities){
	                TreeInfoVO cityTreeVO = new TreeInfoVO();
	                cityTreeVO.setTitle(servedCity.getCityName());
	                cityTreeVO.setKey(servedCity.getUri());
	                cityTreeVO.setIsLazy(true);
	
	                String key = servedCity.getUri();
	                if(!cityBranchMap.containsKey(key)) {
	                    ArrayList<Long> branchList = new ArrayList<>();
	                    branchList.add(orgBranchId);
	                    cityBranchMap.put(key, branchList);
	                }else{
	                    ArrayList<Long> branchList = cityBranchMap.get(key);
	                    branchList.add(orgBranchId);
	                }
	                servedCityTreeVO.add(cityTreeVO);
	            }
	        }
        }



        JSONSerializer iSerializer = new JSONSerializer();

        HashMap<String,Object> responseMap = new HashMap<>();
        responseMap.put("servedCities",servedCityTreeVO);
        responseMap.put("cityBranchMap",cityBranchMap);

        return iSerializer.exclude("*.class").deepSerialize(responseMap);
    }

    @RequestMapping(value="/view/orgVillagesForCityVillageMapping")
    @ResponseBody
    public String orgVillagesForCityVillageMapping(@RequestParam("orgbranchIds[]") Long[] orgBranchIds){

        List<TreeInfoVO> servedVillagesTreeVO = new ArrayList<>();
        HashMap<String,ArrayList<Long>> villageBranchMap = new HashMap<>();
        for(Long orgBranchId : orgBranchIds) {
            OrganizationBranch orgBranch = entityDao.find(OrganizationBranch.class, orgBranchId);
            List<VillageMaster> servedVillages = orgBranch.getServedVillages();
            if(CollectionUtils.isNotEmpty(servedVillages)){
            for(VillageMaster servedVillage : servedVillages){
                TreeInfoVO villageTreeVO = new TreeInfoVO();
                villageTreeVO.setTitle(servedVillage.getName());
                villageTreeVO.setKey(servedVillage.getUri());
                villageTreeVO.setIsLazy(true);

                String key = servedVillage.getUri();
                if(!villageBranchMap.containsKey(key)) {
                    ArrayList<Long> branchList = new ArrayList<>();
                    branchList.add(orgBranchId);
                    villageBranchMap.put(key, branchList);
                }else{
                    ArrayList<Long> branchList = villageBranchMap.get(key);
                    branchList.add(orgBranchId);
                }
                servedVillagesTreeVO.add(villageTreeVO);
            }
        }
        }



        JSONSerializer iSerializer = new JSONSerializer();

        HashMap<String,Object> responseMap = new HashMap<>();
        responseMap.put("servedVillages",servedVillagesTreeVO);
        responseMap.put("villageBranchMap",villageBranchMap);

        return iSerializer.exclude("*.class").deepSerialize(responseMap);
    }

    @RequestMapping(value="/view/areasForCityVillageMapping")
    @ResponseBody
    public String areasForCityVillageMapping(@RequestParam("parentCode") String parentCode, @RequestParam("parentType") String parentType){
        List<Area> areas = new ArrayList<>();
        if(parentType.equals("CITIES")){
            Long cityId = EntityId.fromUri(parentCode).getLocalId();
            areas = userCityVillageMappingService.getAreaFromCity(cityId);
        }
        if(parentType.equals("VILLAGES")){
            Long villageId = EntityId.fromUri(parentCode).getLocalId();
            areas = userCityVillageMappingService.getAreaFromVillage(villageId);
        }

        List<TreeInfoVO> cityAreas = new ArrayList<>();
        if(CollectionUtils.isNotEmpty(areas)){
	        for(Area area : areas){
	            TreeInfoVO areaVO = new TreeInfoVO();
	            areaVO.setTitle(area.getAreaName());
	            areaVO.setKey(area.getUri());
	            areaVO.setIsLazy(false);
	            cityAreas.add(areaVO);
	        }
        }
        JSONSerializer iSerializer = new JSONSerializer();
        return iSerializer.exclude("*.class").deepSerialize(cityAreas);
    }

    @RequestMapping(value = "/view/getUrlMappingPage")
    public String getUserUrlMappingPage(ModelMap map,@RequestParam("userId") Long userId){
        List<UserDefaultUrlMapping> userDefaultUrlMappingList=userManagementService.getAllUrlMappingsOfUser(userId);
        List<UserDefaultUrlMappingVO> userDefaultUrlMappingVOList=userManagementService.userUrlMappingListToVO(userDefaultUrlMappingList);
        map.put("userDefaultUrlMappingVOList",userDefaultUrlMappingVOList);
        if(getUserDetails().getId().equals(userId)) {
            map.put("mappingButtonHidden", true);
            //map.put("showSaveMapping", true);
        }
        return "userDefaultUrlMapping/userUrlMappingPage";

    }

    @RequestMapping(value = "/addMappingRow")
    public String addMappingRow(@RequestParam("totalRows") Integer rowIndex,ModelMap map){
        if(rowIndex==null){
            rowIndex=0;
        }
        map.put("rowIndex",rowIndex);
        return "userDefaultUrlMapping/userUrlMappingRow";
    }

    @RequestMapping(value = "/getMenuOptions/{productId}/{roles}")
    public String getMenuOptions(@PathVariable("productId") Long productId, @PathVariable("roles")Long[] roles, ModelMap map, @RequestParam String value,
                                 @RequestParam String itemVal, @RequestParam String searchCol, @RequestParam String className,
                                 @RequestParam Boolean loadApprovedEntityFlag, @RequestParam String i_label, @RequestParam String idCurr,
                                 @RequestParam String content_id, @RequestParam int page, @RequestParam(required = false) String itemsList,
                                 @RequestParam(required = false) Boolean strictSearchOnitemsList){

        if(productId==null){

        }
        SourceProduct product=genericParameterService.findById(productId,SourceProduct.class);

        List<MenuVO> menuVOList = menuService.getMenuByProductAndRole(product.getCode(),roles);
        String[] searchColumnList = searchCol.split(" ");
        if (strictSearchOnitemsList == null) {
            strictSearchOnitemsList = false;
        }
        if (loadApprovedEntityFlag == null) {
            loadApprovedEntityFlag = false;
        }
        List<Map<String, ?>> list =menuService.searchMenuItemsForString(className, itemVal, searchColumnList, value,
                loadApprovedEntityFlag, itemsList, strictSearchOnitemsList,page,menuVOList);
        paginateAutoComplete(list,map,i_label,page,content_id,idCurr);
        return "autocomplete";
    }


    @RequestMapping(value = "/validateMapping")
    public String validateMapping(){

        return null;
    }

    @ResponseBody
    @RequestMapping(value = "/getRoleIdsOfUser")
    public List<Long> getRoleIdsOfUser(@RequestParam(value="userId" ,required = false) Long userId,
                                       @RequestParam(value="userName",required = false) String userName){
        List<Long> userRoleList=new ArrayList<>();
        if(userId==null && userName==null){
            return userRoleList;
        }

        userRoleList.addAll(userService.getRolesFromUserId(userId != null ? userId : userService.getUserIdByUserName(userName))
                .stream().map(BaseEntity::getId).collect(Collectors.toList()));


        return userRoleList ;
    }

    private void paginateAutoComplete(List<Map<String, ?>> list,ModelMap map,String i_label,int page,String content_id,String idCurr){
        if(list.size() > 0) {
            Map listMap = (Map)list.get(list.size() - 1);
            int sizeList1 = ((Integer)listMap.get("size")).intValue();
            list.remove(list.size() - 1);
            map.put("size", Integer.valueOf(sizeList1));
            map.put("page", Integer.valueOf(page));
        }

        if(i_label != null && i_label.contains(".")) {
            i_label = i_label.replace(".", "");
        }

        map.put("data", list);
        if(idCurr != null && idCurr.trim().length() > 0) {
            idCurr = idCurr.replaceAll("[^\\w\\s\\-_]", "");
        }

        map.put("idCurr", idCurr);
        map.put("i_label", i_label);
        map.put("content_id", content_id);
        return;
    }

    @ResponseBody
    @RequestMapping("/saveUserTargetUrlMapping")
    public String saveUserTargetUrlMapping(ModelMap map,UserManagementForm userManagementForm){

        List<UserDefaultUrlMappingVO> voList= userManagementForm.getUserDefaultUrlMappingVOList();
        userManagementService.removeAllUrlMappingsOfUser(userService.getCurrentUser().getId());
        userManagementService.updateUserUrlMappingAtMakerStage(voList,userService.getCurrentUser().getUserReference(),getUserDetails().getUserReference(),0,null);
        return "Default Target URLs updated successfully";
    }

    @ResponseBody
    @RequestMapping("/countTargetUrlMappings")
    public Long countTargetUrlMappings(@RequestParam(value="userId") Long userId, ModelMap map){
        if(userId==null){
            return 0L;
        }
        Integer userApprovalStatus=userService.getUserById(userId).getApprovalStatus();
        if(!ApprovalStatus.APPROVED_RECORD_STATUS_IN_APPROVED_APPROVED_MODIFIED.contains(userApprovalStatus)){
             User approvedUser=(User)baseMasterService.getLastApprovedEntityByUnapprovedEntityId(userService.getUserById(userId).getUserEntityId());
            if(approvedUser!=null){
                userId=approvedUser.getId();
            }
        }
        return userManagementService.getTargetUrlMappingCount(userId);
    }


    private void setGlobalExpiryDaysInMap(ModelMap map) {
    	ConfigurationVO configVo = configurationService.getConfigurationPropertyFor(SystemEntity.getSystemEntityId(),
                "config.applyGlobalExpiry");
        if(configVo!=null) {
            map.put("globalExpiryEnabled", configVo.getPropertyValue().toLowerCase());

            ConfigurationVO configVo1 = configurationService.getConfigurationPropertyFor(SystemEntity.getSystemEntityId(),
                    "config.globalExpiry.days");
            if (configVo1 != null)
                map.put("globalExpiryDays", configVo1.getPropertyValue());

        }
    }

	
}
