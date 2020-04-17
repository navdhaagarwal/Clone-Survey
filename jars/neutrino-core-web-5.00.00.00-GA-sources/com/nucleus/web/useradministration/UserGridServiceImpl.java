/**
 * This file and a proportion of its content is copyright of Nucleus Software Exports Limited - \u00a9 2012. All rights reserved.
 * Any redistribution or reproduction of part or all of the contents in any form is prohibited other than the following:
 * - you cannot print or download to a local hard disk extract contents either part or full for personal/ commercial/
 * academic or any other use
 * - you may not copy the content to individual/ third parties for any type of use, either as compiled or source format
 * without the knowledge and consent of Nucleus SOftware
 * - You may not, except with our express written permission, distribute or commercially exploit the content. Nor may you
 * transmit it or store it in any other web site or other form of electronic retrieval system.
 */
package com.nucleus.web.useradministration;

import static com.nucleus.finnone.pro.general.util.ValidatorUtils.notNull;
import static com.nucleus.user.UserConstants.NEUTRINO_SYSTEM_USER;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.collections4.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.security.core.GrantedAuthority;

import com.nucleus.authenticationToken.AuthenticationTokenService;
import com.nucleus.authority.Authority;
import com.nucleus.authority.AuthorityCodes;
import com.nucleus.businessmapping.service.UserApplicationChannelModeService;
import com.nucleus.businessmapping.service.UserManagementServiceCore;
import com.nucleus.core.businesspartner.entity.BusinessPartnerBase;
import com.nucleus.core.exceptions.SystemException;
import com.nucleus.core.genericparameter.service.GenericParameterService;
import com.nucleus.entity.ApprovalStatus;
import com.nucleus.entity.EntityId;
import com.nucleus.finnone.pro.base.utility.CoreUtility;
import com.nucleus.finnone.pro.general.util.ValidatorUtils;
import com.nucleus.logging.BaseLoggers;
import com.nucleus.makerchecker.GridVO;
import com.nucleus.makerchecker.MakerCheckerService;
import com.nucleus.makerchecker.MasterApprovalFlowConstants;
import com.nucleus.makerchecker.MasterConfigurationRegistry;
import com.nucleus.master.BaseMasterEntity;
import com.nucleus.master.BaseMasterService;
import com.nucleus.officer.OfficerBase;
import com.nucleus.persistence.BaseMasterDao;
import com.nucleus.persistence.HibernateUtils;
import com.nucleus.reason.ActiveReason;
import com.nucleus.reason.BlockReason;
import com.nucleus.reason.ReasonVO;
import com.nucleus.security.oauth.service.RESTfulAuthenticationService;
import com.nucleus.service.BaseServiceImpl;
import com.nucleus.user.User;
import com.nucleus.user.UserInfo;
import com.nucleus.user.UserService;
import com.nucleus.user.UserSessionManagerService;
import com.nucleus.user.UserStatus;
import com.nucleus.web.master.MakerCheckerWebUtils;
import com.nucleus.reason.BlockReason;

/**
 * The Class MasterController.
 *
 * @author Nucleus Software Exports Limited
 */
@Named("userGridServiceCore")
public class UserGridServiceImpl extends BaseServiceImpl implements UserGridService {

    @Inject
    @Named("authenticationTokenService")
    private AuthenticationTokenService authenticationTokenService;

    @Inject
    @Named("userManagementServiceCore")
    private UserManagementServiceCore  userManagementService;
    @Inject
    @Named(value = "userSessionManagerService")
    private UserSessionManagerService  userSessionManagerService;
    
	@Value("${soap.service.trusted.client.id}")
	private String clientID;
	
	@Value("${INTG_BASE_URL}/app/restservice/getLoggedInUsersTrustedSourceDetails")
	private String loggedInUsersTrustedSourceUrl;
	
    @Inject
    @Named("baseMasterService")
    private BaseMasterService baseMasterService;
    
    @Inject
    @Named("oauthauthenticationService")
    private RESTfulAuthenticationService oauthauthenticationService;
    
	
    @Inject
    @Named("userService")
    private UserService userService;
    
    
    @Inject
    @Named("coreUtility")
    private CoreUtility coreUtility;
    
    
    @Inject
    @Named("masterConfigurationRegistry")
    private MasterConfigurationRegistry masterConfigurationRegistry;
    
    @Inject
    @Named("baseMasterDao")
    protected BaseMasterDao             baseMasterDao;
    
    @Inject
    @Named("makerCheckerService")
    private MakerCheckerService         makerCheckerService;
    

	@Value(value = "#{'${core.web.config.appChannel.mode}'}")
	private String APP_CHANNEL_MODE;
	
	@Value(value = "#{'${core.web.config.appChannel.mode.service.name}'}")
	private String APP_CHANNEL_MODE_SERVICE;
	
	@Autowired
    protected ApplicationContext           applicationContext;
	
	@Inject
    @Named("userManagementServiceCore")
    protected UserManagementServiceCore  userManagementServiceCore;
	
	@Inject
	@Named(value = "genericParameterService")
	private GenericParameterService genericParameterService;

	public static final String INTERNET_CHANNEL = "I";
	
	private static final String USER_NAME_HTML_TAG_SMALL="<small class='small-display-block'>[" ;
	private static final String USER_NAME_HTML_TAG_END_SMALL="]</small>" ;
    
	private Class<?>					cachedEntityClass;
    @SuppressWarnings("rawtypes")
	public Map<String, Object> loadPaginatedData(Class entityName,
			String userUri, boolean filteredStatus) {
		Map<String, Object> userRecordMap = new HashMap<String, Object>();
		List<Object> userInfoList;
		List<Object> loggedInUserInfoList = userSessionManagerService
				.getAllLoggedInUsers();
		if (filteredStatus) {
			userInfoList = loggedInUserInfoList;
			if (APP_CHANNEL_MODE.equals(INTERNET_CHANNEL)) {
				UserApplicationChannelModeService userApplicationChannelModeService;
				userApplicationChannelModeService = (UserApplicationChannelModeService)applicationContext.getBean(APP_CHANNEL_MODE_SERVICE);
				userInfoList = userApplicationChannelModeService
						.getLoggedInUserByProfile(userInfoList);
			}
		} else {
			userInfoList = userManagementService.getAllActiveUsers();
		}

        for (Object x : userInfoList) {
            ArrayList<String> customizedActions = new ArrayList<String>();
            UserInfo y = (UserInfo) x;
			if (y.isInActive()) {
				customizedActions.add("Inactive Reason");
				y.addProperty("actions", null);
				continue;
			}
            if (loggedInUserInfoList.contains(x)) {
                customizedActions.add("Logout");

            }
            customizedActions.add("Send Message");
            customizedActions.add("Force Reset Password");
            
            if (isAdminActionCapableUser()) {
                if (y.isEnabled()) {
                    customizedActions.add("Inactivate User");
                    customizedActions.add("Block User");
                } else if ((!y.isEnabled()) && (y.isAccountNonLocked())) {
                    customizedActions.add("Activate");
                } else if (!y.isAccountNonLocked() && isUserCanBeUnblock(y)) {
                    customizedActions.add("Unlock User");
                }
            }
            y.addProperty("customizedActions", customizedActions);
            ArrayList<String> actions = new ArrayList<String>();
            actions.add("Edit");
            y.addProperty("actions", actions);

        }
        Integer recordCount = userInfoList.size();
        userRecordMap.put("entityList", userInfoList);
        userRecordMap.put("totalRecordCount", recordCount);
        return userRecordMap;
    }

    private Boolean isAdminUser() {
        Boolean hasAuthority = false;
        for (GrantedAuthority grantedAuthority : getCurrentUser().getAuthorities()) {
            if (grantedAuthority.getAuthority().equalsIgnoreCase("ADMIN_AUTHORITY")) {
                hasAuthority = true;
                break;
            }
        }
        return hasAuthority;
    }
    
    private Boolean isUserCanBeUnblock(User user){
    	return isUserCanBeUnblock(user.getId());
    }
    private Boolean isUserCanBeUnblock(UserInfo user){
    	return isUserCanBeUnblock(user.getId());
    }
    
    private Boolean isUserCanBeUnblock(Long userId){
    	ReasonVO reason = userManagementServiceCore.getReasonByUserId(userId);
    	if(reason!=null && reason.getCode() != null){
    		 BlockReason blockReason = genericParameterService.findByCode(reason.getCode(),BlockReason.class);
             if (blockReason != null && blockReason.getParentCode().equalsIgnoreCase("UNBLOCK_NO")) {
            	return getCurrentUser().getAuthorities().stream().anyMatch((a)->{
       			 return a.getAuthority().equalsIgnoreCase("UNBLOCK_"+blockReason.getCode());});
             }
    	}
    	return true;
    }
    
    private Boolean isUserMarkedForUnlock(Long userId){
    	ReasonVO reason = userManagementServiceCore.getReasonByUserId(userId);
    	if(reason!=null && reason.getCode() != null){
    		 ActiveReason activeReason = genericParameterService.findByCode(reason.getCode(),ActiveReason.class);
             if(activeReason != null) {
            	 return true;
             }
    	}
    	return false;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public Map<String, Object> loadPaginatedData(Class entityName, String userUri, Long parentId, Integer iDisplayStart,
            Integer iDisplayLength, String sortColName, String sortDir, boolean filteredStatus) {
    	GridVO gridVO = new GridVO();
        gridVO.setiDisplayStart(iDisplayStart);
        gridVO.setiDisplayLength(iDisplayLength);
        gridVO.setSortDir(sortDir);
        gridVO.setSortColName(sortColName);
        gridVO.setSearchMap(null);
    	return loadPaginatedData(gridVO, entityName, userUri, parentId, filteredStatus);
    }
    
    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public Map<String, Object> findEntity(Class entityClass, String userUri,Integer iDisplayStart,
            Integer iDisplayLength, Map<String, Object> queryMap) {
        Map<String, Object> searchRecordMap = new HashMap<String, Object>();
        List<Object> loggedInUserInfoList = getAllLoggedInUsers();
        List<Object> searchRecords = baseMasterService.findEntity(entityClass,iDisplayStart,iDisplayLength,queryMap);
        List<Object> recordCount = baseMasterService.findEntity(entityClass,null,null,queryMap);
        Integer totalRecordCount = baseMasterService.getTotalRecordSize(entityClass, userUri);
        String createdBy = null;
        String reviewedBy = null;
        for (Object bm : searchRecords) {
            BaseMasterEntity singleEntity = (BaseMasterEntity) bm;
            if (singleEntity.getEntityLifeCycleData() != null) {
                EntityId createorEntityId = singleEntity.getEntityLifeCycleData().getCreatedByEntityId();
                if (createorEntityId != null)
                    createdBy = userService.getUserNameByUserId(createorEntityId.getLocalId());
                EntityId revieworEntityId = singleEntity.getMasterLifeCycleData().getReviewedByEntityId();
                if (revieworEntityId != null)
                    reviewedBy = userService.getUserNameByUserId(revieworEntityId.getLocalId());
                singleEntity.addProperty("uuid", singleEntity.getEntityLifeCycleData().getUuid());
            }
            if (createdBy != null) {
                singleEntity.addProperty("createdBy", createdBy);
            }
            if (reviewedBy != null) {
                singleEntity.addProperty("reviewedBy", reviewedBy);
            }
            singleEntity.addProperty("approvalStatus",
                    MakerCheckerWebUtils.getApprovalStatus(singleEntity.getApprovalStatus()));
            
            updateUserActions(singleEntity,loggedInUserInfoList);
            
/*
            if (singleEntity.getEntityLifeCycleData().getSystemModifiableOnly() != null
                    && singleEntity.getEntityLifeCycleData().getSystemModifiableOnly()) {
                singleEntity.addProperty("actions", "");
            }*/
        }
        
        searchRecordMap.put("searchRecordList", searchRecords);
        searchRecordMap.put("searchRecordListSize", recordCount.size());
        searchRecordMap.put("totalRecordListSize", totalRecordCount);

        return searchRecordMap;
    }
    
    @SuppressWarnings("unchecked")
    @Override
	public void updateUserActions(BaseMasterEntity singleEntity,List<Object> loggedInUserInfoList){
    	 User user = (User) singleEntity;
    	 if(notNull(user) && isNotBlank(user.getUsername()) && !NEUTRINO_SYSTEM_USER.equalsIgnoreCase(user.getUsername())){
    		 updateCustomizedActionList(singleEntity,loggedInUserInfoList);
    		 updateActionList(singleEntity);
    	 }  	 
    }
    

	@SuppressWarnings("unchecked")
	private void updateActionList(BaseMasterEntity singleEntity) {
		List<String> actionsList = (ArrayList<String>) singleEntity.getViewProperties().get("actions");
			//User is marked for Blocked/UnBlocked/InActivate and Status is WORFLOW_IN_PROGRESS then it should have only two Action 'Accept', 'Reject'
			if(((User) singleEntity).getApprovalStatus() == ApprovalStatus.WORFLOW_IN_PROGRESS) {				
				if (((User) singleEntity).getUserStatus() == UserStatus.STATUS_INACTIVE 
						|| ((User) singleEntity).getUserStatus() == UserStatus.STATUS_LOCKED
						|| ((User) singleEntity).getUserStatus() == UserStatus.STATUS_ACTIVE && isUserMarkedForUnlock(singleEntity.getId())) {
					
					actionsList.remove(MasterApprovalFlowConstants.SEND_BACK);
					singleEntity.addProperty("actions", actionsList);
					return;
				}
			}else {
				if (((User) singleEntity).getUserStatus() == UserStatus.STATUS_INACTIVE) {
					singleEntity.addProperty("actions", null);
					return;
				}
			}
			

			if (actionsList != null) {
				actionsList.remove(MasterApprovalFlowConstants.CLONE);
				actionsList.remove(MasterApprovalFlowConstants.sendForApproval);
			}
			if ((singleEntity.getApprovalStatus() == ApprovalStatus.APPROVED
					|| singleEntity.getApprovalStatus() == ApprovalStatus.APPROVED_MODIFIED)
					&& actionsList != null) {

				actionsList.remove(MasterApprovalFlowConstants.delete);
				singleEntity.addProperty("actions", actionsList);
			}

			if(((User) singleEntity).getUserStatus() == UserStatus.STATUS_LOCKED && actionsList != null ) {
						actionsList.remove(MasterApprovalFlowConstants.edit);
						singleEntity.addProperty("actions", actionsList);
			}
	}

	private void updateCustomizedActionList(BaseMasterEntity singleEntity, List<Object> loggedInUserInfoList) {
		User user = (User) singleEntity;
		List<String> customizedActions = new ArrayList<>();

		// Action will only be visible to Approved Record
		if (((User) singleEntity).getApprovalStatus() == ApprovalStatus.APPROVED) {
			
			// An No Action for InActivated User
			if ((user.getUserStatus() == UserStatus.STATUS_INACTIVE)) {
				customizedActions.add("Inactive Reason");
				singleEntity.addProperty("customizedActions", customizedActions);
				return;
			}

			// If user is Administrator and current record status is Active then only 'Logout',
			// 'Send Message', 'Force Reset Password' should be applicable
			
			if (isAdminUser() && user.isEnabled()) {
				if (loggedInUserInfoList.contains(singleEntity)) {
					customizedActions.add("Logout");
				}
				customizedActions.add("Send Message");
				if (UserService.SOURCE_DB.equalsIgnoreCase(user.getSourceSystem())) {
					customizedActions.add("Force Reset Password");
				}
			}

			if (isAdminActionCapableUser()) {
				if (user.isEnabled()) {
					customizedActions.add("Inactivate User");
					customizedActions.add("Block User");
				} else if ((!user.isEnabled()) && (user.isAccountNonLocked())) {
					customizedActions.add("Activate");
				} else if (!user.isAccountNonLocked()) {
					if (isUserCanBeUnblock(user)){
                        customizedActions.add("Unlock User");
                        customizedActions.add("Block Reason");
                    }
                    List<BlockReason> highPriorityBlockReasons = userService.getHighPriorityBlockReasonExist(user.getId(), "UNBLOCK_NO");
                    if (CollectionUtils.isNotEmpty(highPriorityBlockReasons)) {
                        customizedActions.add("Block User");
                    }
				}
			} else {
				// If User Is Not Admin then he can only view the reasons
				if ((user.getUserStatus() == UserStatus.STATUS_INACTIVE)) {
					customizedActions.add("Inactive Reason");
					singleEntity.addProperty("customizedActions", customizedActions);
					return;
				}else if ((user.getUserStatus() == UserStatus.STATUS_ACTIVE) && isUserMarkedForUnlock(user.getId())) {
					customizedActions.add("Unlock Reason");
					singleEntity.addProperty("customizedActions", customizedActions);
					return;
				}else if ((user.getUserStatus() == UserStatus.STATUS_LOCKED)) {
					customizedActions.add("Block Reason");
					singleEntity.addProperty("customizedActions", customizedActions);
					return;
				}
			}
		} else if(((User) singleEntity).getApprovalStatus() == ApprovalStatus.WORFLOW_IN_PROGRESS){
			// Else only Reasons can be viewed and ideally No Action should be taken on the
			if ((user.getUserStatus() == UserStatus.STATUS_INACTIVE)) {
				customizedActions.add("Inactive Reason");
				singleEntity.addProperty("customizedActions", customizedActions);
				return;
			} else if ((user.getUserStatus() == UserStatus.STATUS_ACTIVE) && isUserMarkedForUnlock(user.getId())) {
				customizedActions.add("Unlock Reason");
				singleEntity.addProperty("customizedActions", customizedActions);
				return;
			} else if ((user.getUserStatus() == UserStatus.STATUS_LOCKED)) {
				customizedActions.add("Block Reason");
				singleEntity.addProperty("customizedActions", customizedActions);
				return;
			}
		}
		singleEntity.addProperty("customizedActions", customizedActions);

	}

	@SuppressWarnings("unchecked")
    @Override
    public <T extends BaseMasterEntity> List<Object> getAllLoggedInUsers(){
    	
    	List<Object> loggedInUserList=new ArrayList<Object>();
    
    	if (cachedEntityClass == null) {
    		cachedEntityClass = getEntityClass("UserInfo");
    	}
    	Class<T> entityClass = (Class<T>) cachedEntityClass;
    	Boolean isAuthorizedMakerForEntity = loggedInUserisAnAuthorizedMakerForEntity(entityClass);

    	List<UserInfo> loggedInUserInfoList=userSessionManagerService.getAllLoggedInUsersAcrossModule();
    	
    	for (UserInfo userInfo : loggedInUserInfoList) {
    		Long userId = userInfo.getId();
    		User user = entityDao.find(User.class, userId);
    		
    		user.addProperty("actions",user.getViewProperties().get("actions"));
            user.addProperty("customizedActions",user.getViewProperties().get("customizedActions"));
            HibernateUtils.initializeAndUnproxy(user.getSysName());
            HibernateUtils.initializeAndUnproxy(user);
            setNonWorkflowEntityAction(user, isAuthorizedMakerForEntity);

            loggedInUserList.add(user);
    	}
    	return loggedInUserList;
	}
    
    private Class<?> getEntityClass(String keyName) {
        Class<?> entityClass;
        String entityPath = masterConfigurationRegistry.getEntityClass(keyName);
        try {
            entityClass = Class.forName(entityPath);
        } catch (ClassNotFoundException e) {
            throw new SystemException(e);
        }
        return entityClass;
    }
    
    @Override
    public <T extends BaseMasterEntity> boolean loggedInUserisAnAuthorizedMakerForEntity(Class<T> entityClass) {
        Set<Authority> authorities = getCurrentUser().getUserAuthorities();
        Boolean isAuthorizedMakerForEntity = false;
        for (Authority authority : authorities) {
            if (authority.getAuthCode().equalsIgnoreCase(
                    AuthorityCodes.MAKER + "_" + entityClass.getSimpleName().toUpperCase())) {
                isAuthorizedMakerForEntity = true;
                break;
            }
        }
        return isAuthorizedMakerForEntity;
    }
    
    @Override
    public <T extends BaseMasterEntity> boolean loggedInUserisAnAuthorizedCheckerForEntity(Class<T> entityClass) {
        Set<Authority> authorities = getCurrentUser().getUserAuthorities();
        Boolean isAuthorizedCheckerForEntity = false;
        for (Authority authority : authorities) {
            if (authority.getAuthCode().equalsIgnoreCase(
                    AuthorityCodes.CHECKER + "_" + entityClass.getSimpleName().toUpperCase())) {
                isAuthorizedCheckerForEntity = true;
                break;
            }
        }
        return isAuthorizedCheckerForEntity;
    }
    
    @Override
    public void setNonWorkflowEntityAction(BaseMasterEntity bma, Boolean isAuthorozedMakerForEntity) {
    	if (bma.getEntityLifeCycleData().getSystemModifiableOnly() == null
                || !(bma.getEntityLifeCycleData().getSystemModifiableOnly())) {
        if (bma.getApprovalStatus() == ApprovalStatus.APPROVED && isAuthorozedMakerForEntity) {
            List<String> actionsList = new ArrayList<String>();
            actionsList.add(MasterApprovalFlowConstants.edit);
            actionsList.add(MasterApprovalFlowConstants.delete);
            if (!(bma instanceof BusinessPartnerBase) && !(bma instanceof OfficerBase)) {
                actionsList.add(MasterApprovalFlowConstants.CLONE);
            }

            bma.addProperty("actions", actionsList);
        } else if ((bma.getApprovalStatus() == ApprovalStatus.UNAPPROVED_MODIFIED || bma.getApprovalStatus() == ApprovalStatus.UNAPPROVED_ADDED)
                && isAuthorozedMakerForEntity) {
            List<String> actionsList = new ArrayList<String>();
            actionsList.add(MasterApprovalFlowConstants.sendForApproval);
            if (masterConfigurationRegistry.getEntityAutoApprovalFlag(bma.getClass())) {
                actionsList.remove(MasterApprovalFlowConstants.sendForApproval);
                actionsList.add(MasterApprovalFlowConstants.autoApproval);
                if (bma instanceof BusinessPartnerBase) {
                    actionsList.remove(MasterApprovalFlowConstants.autoApproval);
                    actionsList.add(MasterApprovalFlowConstants.sendForApproval);
                }
            }
            actionsList.add(MasterApprovalFlowConstants.delete);
            actionsList.add(MasterApprovalFlowConstants.edit);
            if (!(bma instanceof BusinessPartnerBase) && !(bma instanceof OfficerBase)) {
                actionsList.add(MasterApprovalFlowConstants.CLONE);
            }

            bma.addProperty("actions", actionsList);
        } else if (bma.getApprovalStatus() == ApprovalStatus.APPROVED_DELETED && isAuthorozedMakerForEntity) {
            List<String> actionsList = new ArrayList<String>();
            actionsList.add(MasterApprovalFlowConstants.sendForApproval);
            bma.addProperty("actions", actionsList);
        } else if (bma.getApprovalStatus() == ApprovalStatus.CLONED && isAuthorozedMakerForEntity) {
            List<String> actionsList = new ArrayList<String>();
            actionsList.add(MasterApprovalFlowConstants.delete);
            actionsList.add(MasterApprovalFlowConstants.edit);
            if (!(bma instanceof BusinessPartnerBase) && !(bma instanceof OfficerBase)) {
                actionsList.add(MasterApprovalFlowConstants.CLONE);
            }

            bma.addProperty("actions", actionsList);
        }
    	}
    }
    
    @Override
    public <T extends BaseMasterEntity> List<String> getApplicableAssigneeListUri(boolean isAuthorizedMakerForEntity,
            boolean isAuthorizedCheckerForEntity, Class<T> entityClass, String userUri) {
        List<String> authList = new ArrayList<String>(3);
        if (isAuthorizedMakerForEntity) {
            Authority authority = userService.getAuthorityByCode(AuthorityCodes.MAKER + "_"
                    + entityClass.getSimpleName().toUpperCase());
            authList.add(authority.getUri());
        }
        if (isAuthorizedCheckerForEntity) {
            Authority authority = userService.getAuthorityByCode(AuthorityCodes.CHECKER + "_"
                    + entityClass.getSimpleName().toUpperCase());
            authList.add(authority.getUri());
        }
        if (userUri != null) {
            authList.add(userUri);
        }
        return authList;
    }

	@SuppressWarnings("unchecked")
	@Override
	public Map<String, Object> loadPaginatedData(GridVO gridVO, Class entityName, String userUri, Long parentId, boolean filteredStatus) {
        Map<String, Object> returnMap = new HashMap<>();
        List<Object> userInfoList;
        Integer recordCount=0;
        Integer searchRecordCount = 0;
        
        //Below list contains those user who are logged in web modules
        List<Object> loggedInUserInfoList = getAllLoggedInUsers();
        
        if (filteredStatus) {
        	Map<String, List<String>> mapOfTrustedSources = new HashMap<String, List<String>>();
        	
        	//This will be rest call to API manager via NIF to get the list of mobile users
			if (coreUtility.isApiManagerEnabled()) {
				
				mapOfTrustedSources = oauthauthenticationService.getLoggedInUsersTrustedSourceDetails(loggedInUsersTrustedSourceUrl, clientID);
				
				if(mapOfTrustedSources != null && !mapOfTrustedSources.isEmpty()) {
					// update loggedInUserInfoList list with the user who are logged in mobility/trusted source
					for(String usernameKey : mapOfTrustedSources.keySet()) {
						User mobileUser = userService.findUserByUsername(usernameKey);
						mobileUser.addProperty("actions", mobileUser.getViewProperties().get("actions"));
						mobileUser.addProperty("customizedActions", mobileUser.getViewProperties().get("customizedActions"));
						HibernateUtils.initializeAndUnproxy(mobileUser.getSysName());
						HibernateUtils.initializeAndUnproxy(mobileUser);
						setNonWorkflowEntityAction(mobileUser, loggedInUserisAnAuthorizedMakerForEntity(entityName));
						
						if(!loggedInUserInfoList.contains(mobileUser)) {
							loggedInUserInfoList.add(mobileUser);
						}
					}
				}
			}
        	
        	//remove current User from list
        	UserInfo userInfo = getCurrentUser();
        	removeLoggedInUserObjectFromList(loggedInUserInfoList,userInfo);
        	
    		for (Object userObj : loggedInUserInfoList) {
    			//get All loggedIn Modules from web and mobile
                List<String> loggedInModulelist = new ArrayList<>();
                User user = (User)userObj;
                
                //1. web logged in module
                List<String> loggedInWebModulelist = userSessionManagerService.getLoggedInModules(userService.getUserFromUsername(user.getUsername()));
                if(loggedInWebModulelist != null && loggedInWebModulelist.size() > 0) {
                	loggedInModulelist.addAll(loggedInWebModulelist);
                }
                
                //2. trusted sources
                if(mapOfTrustedSources != null) {
                	List<String> loggedInTrustedSourcelist = mapOfTrustedSources.get(user.getUsername());
                    if(loggedInTrustedSourcelist != null && loggedInTrustedSourcelist.size() > 0) {
                    	loggedInModulelist.addAll(loggedInTrustedSourcelist);
                    }
                }
                user.addProperty("loggedInModules",loggedInModulelist);
        	}
    		//Above code is required before searching as now loggedInModule is also part of search criterion.
    		
        	Map<String, Object> searchMap = gridVO.getSearchMap();        	       	
        	if(searchMap !=null && searchMap.size() > 0){
        		userInfoList = getLoggedInUserListBasedOnSearchFilter(loggedInUserInfoList,searchMap);
       		}else{
        		userInfoList = loggedInUserInfoList;
        	}
            recordCount = loggedInUserInfoList.size();
            searchRecordCount= userInfoList.size();              
            Collections.sort(userInfoList, new UserGridDataComparator(gridVO.getSortColName(),gridVO.getSortDir()));            
            userInfoList = loadPaginatedDataFromLoggedInUserList(userInfoList,gridVO);
        } else {
            userInfoList = baseMasterService.loadPaginatedData(gridVO, entityName, userUri);
            recordCount = baseMasterService.getTotalRecordSize(entityName, userUri);
            gridVO.setiDisplayStart(null);
            gridVO.setiDisplayLength(null);
            if (ValidatorUtils.hasNoEntry(gridVO.getSearchMap())) {
            	searchRecordCount = recordCount;
            } else {
            	Long searchRecordsCountLong = baseMasterService.getSearchRecordsCount(gridVO, entityName, userUri);
            	searchRecordCount = searchRecordsCountLong == null ? 0: searchRecordsCountLong.intValue();
            }
        }
        //Render additional properties(eg. "createdBy", "reviewedBy") to grid entities.
        renderAdditionalPropertyToGrid(userInfoList, loggedInUserInfoList);
        returnMap.put("totalRecordCount", recordCount);
        returnMap.put("recordCount", searchRecordCount);
        returnMap.put("entityList", userInfoList);
        return returnMap;
    }
    
	private void renderAdditionalPropertyToGrid(List<Object> userInfoList, List<Object> loggedInUserInfoList) {
		try {
			StringBuilder createdByUserFullName = new StringBuilder();
			StringBuilder reviewedByUserFullName = new StringBuilder();
			StringBuilder createdBy = new StringBuilder();
			StringBuilder reviewedBy = new StringBuilder();
            for (Object o : userInfoList) {
                BaseMasterEntity singleEntity = (BaseMasterEntity) o;
                if (singleEntity.getEntityLifeCycleData() != null) {
                	EntityId creatorEntityId;
                	//if last updated is available then in the data grid created by should be shown as LastUpdatedBy
                	//otherwise created by will be shown as it is 
                	if ( StringUtils.isNotBlank(singleEntity.getEntityLifeCycleData().getLastUpdatedByUri()) )
                		creatorEntityId = EntityId.fromUri(singleEntity.getEntityLifeCycleData().getLastUpdatedByUri());
                	else
                		creatorEntityId = singleEntity.getEntityLifeCycleData().getCreatedByEntityId();
                    if (creatorEntityId != null){
                    	createdBy 				= createdBy.append(userService.getUserNameByUserId(creatorEntityId.getLocalId()));
						createdByUserFullName 	=createdByUserFullName.append(userService.getUserFullNameForUserId(creatorEntityId.getLocalId()));
                    }
                    EntityId revieworEntityId = singleEntity.getMasterLifeCycleData().getReviewedByEntityId();
                    if (revieworEntityId != null){
                    	reviewedBy 				=reviewedBy.append(userService.getUserNameByUserId(revieworEntityId.getLocalId()));
						reviewedByUserFullName 	=reviewedByUserFullName.append(userService.getUserFullNameForUserId(revieworEntityId.getLocalId()));
                    }
                    singleEntity.addProperty("uuid", singleEntity.getEntityLifeCycleData().getUuid());
                }
                singleEntity.addProperty("createdBy",createdBy.length()>0? createdByUserFullName.toString() + USER_NAME_HTML_TAG_SMALL+ createdBy.toString()+ USER_NAME_HTML_TAG_END_SMALL:"");
				singleEntity.addProperty("createdByUserWithFullName", createdBy.toString() + " [" +createdByUserFullName.toString()+ "]");
				singleEntity.addProperty("reviewedBy",reviewedBy.length()>0?reviewedByUserFullName.toString() + USER_NAME_HTML_TAG_SMALL+reviewedBy.toString()+ USER_NAME_HTML_TAG_END_SMALL:"");
				singleEntity.addProperty("reviewedByUserWithFullName", reviewedBy.toString() + " [" +reviewedByUserFullName.toString()+ "]");
				singleEntity.addProperty("approvalStatus",MakerCheckerWebUtils.getApprovalStatus(singleEntity.getApprovalStatus()));
				createdBy.setLength(0);
				reviewedBy.setLength(0);
				createdByUserFullName.setLength(0);
				reviewedByUserFullName.setLength(0);
                updateUserActions(singleEntity,loggedInUserInfoList);
            }
        } catch (Exception e) {
            BaseLoggers.exceptionLogger.error("Exception in rendering data to Grid", e);
        }
	}

	private List<Object> getLoggedInUserListBasedOnSearchFilter(List<Object> loggedInUserInfoList, Map<String, Object> searchMap){

		List<Object> updatedLoggedInUserListBasedOnSearchFilter = new  ArrayList<Object>();
		
		for(Object object : loggedInUserInfoList){
			updateLoggedInUserList(object,updatedLoggedInUserListBasedOnSearchFilter,searchMap);
		}
		return updatedLoggedInUserListBasedOnSearchFilter;
	}

	private void updateLoggedInUserList(Object object,List<Object> updatedLoggedInUserListBasedOnSearchFilter, Map<String, Object> searchMap) {
		if(object instanceof User){
			User user = (User)object;
			for(Map.Entry<String, Object> entry : searchMap.entrySet()){
				String searchedColName = entry.getKey();	
				Object searchedColValue =  getSearchedColumnValueFromUserObject(user,searchedColName);
				String searchedValue  = String.valueOf(searchedColValue);
				if( searchedValue != null && searchedValue.contains(String.valueOf(entry.getValue()))){
					updatedLoggedInUserListBasedOnSearchFilter.add(user);
					break;
				}													
			}
		}	
		
	}
	
	private Object getSearchedColumnValueFromUserObject(User user,String searchedColName) {
		
		Object value = null;
		try {
			value = PropertyUtils.getNestedProperty(user, searchedColName);
		}catch (IllegalAccessException illegalAccessException) {
			throw new SystemException(illegalAccessException);
		}catch (InvocationTargetException invocationTargetException) {
				throw new SystemException(invocationTargetException);
		}catch (NoSuchMethodException noSuchMethodException) {
				throw new SystemException(noSuchMethodException);
		}		
		if(searchedColName.startsWith("viewProperties"))
		{
			Object list = user.getViewProperties().get(searchedColName.substring("viewProperties".length()+1));
			if(list != null) {
				value = list.toString().toLowerCase();
			}
		}
		return value;
	}

	private List<Object> removeLoggedInUserObjectFromList(List<Object> loggedInUserInfoList, UserInfo userInfo) {
		for(Object  object : loggedInUserInfoList){
			if(object instanceof User){
				User user = (User)object;
				if (userInfo != null && userInfo.getUsername() != null && userInfo.getUsername().equals(user.getUsername())) {
	                loggedInUserInfoList.remove(object);
					break;
				}
			}			
		}
		return loggedInUserInfoList;
	}

	private List<Object> loadPaginatedDataFromLoggedInUserList(List<Object> userInfoList, GridVO gridVO) {
		 
		List<Object> paginatedUserInfoList = null ;
			
		 int iDisplayStart = gridVO.getiDisplayStart();
		 int iDisplayLength = gridVO.getiDisplayLength();
		 
		 if(userInfoList.size() < (iDisplayLength+iDisplayStart) ){
			 paginatedUserInfoList = userInfoList.subList(iDisplayStart, userInfoList.size());
		 }else if(userInfoList.size() >= (iDisplayLength + iDisplayStart)){
			 paginatedUserInfoList = userInfoList.subList(iDisplayStart, iDisplayStart+iDisplayLength);
		 }		
		 
		 return paginatedUserInfoList;
	}
	
    private Boolean isAdminActionCapableUser() {
        Boolean hasAuthority = false;
        if(getCurrentUser().getAuthorities().stream().filter(x->x.getAuthority().equalsIgnoreCase("ADMIN_AUTHORITY")).count() > 0
        		&& getCurrentUser().getAuthorities().stream().filter(x->x.getAuthority().equalsIgnoreCase("MAKER_USER")).count() > 0
        		&& getCurrentUser().getAuthorities().stream().filter(x->x.getAuthority().equalsIgnoreCase("VIEW_USER")).count() > 0) {
        	hasAuthority =  true;
        }
        return hasAuthority;
    }
	
   }
