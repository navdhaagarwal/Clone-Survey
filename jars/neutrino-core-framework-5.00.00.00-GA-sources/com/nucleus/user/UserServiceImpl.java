package com.nucleus.user;

import static com.nucleus.event.EventTypes.USER_ACTIVATED_EVENT;
import static com.nucleus.event.EventTypes.USER_BLOCKED_EVENT;
import static com.nucleus.event.EventTypes.USER_CREATED_EVENT;
import static com.nucleus.event.EventTypes.USER_INACTIVATED_EVENT;
import static com.nucleus.event.EventTypes.USER_UPDATED_EVENT;
import static com.nucleus.user.AccessType.BOTH;
import static com.nucleus.user.AccessType.INTERNET;
import static com.nucleus.user.AccessType.INTRANET;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.Days;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.MessageSource;

import com.nucleus.authenticationToken.AuthenticationTokenService;
import com.nucleus.authenticationToken.PasswordResetToken;
import com.nucleus.authority.Authority;
import com.nucleus.businessmapping.service.UserManagementServiceCore;
import com.nucleus.config.persisted.service.ConfigurationService;
import com.nucleus.config.persisted.vo.ConfigurationVO;
import com.nucleus.contact.PhoneNumber;
import com.nucleus.core.exceptions.SystemException;
import com.nucleus.core.genericparameter.service.GenericParameterService;
import com.nucleus.core.initialization.ProductInformationLoader;
import com.nucleus.core.misc.util.DateUtils;
import com.nucleus.core.money.entity.Money;
import com.nucleus.core.organization.calendar.DailySchedule;
import com.nucleus.core.organization.entity.OrganizationBranch;
import com.nucleus.core.organization.entity.UserBPMapping;
import com.nucleus.core.role.entity.Role;
import com.nucleus.core.user.event.UserEvent;
import com.nucleus.core.validation.util.NeutrinoValidator;
import com.nucleus.dao.query.NamedQueryExecutor;
import com.nucleus.entity.ApprovalStatus;
import com.nucleus.entity.EntityId;
import com.nucleus.entity.SystemEntity;
import com.nucleus.event.Event;
import com.nucleus.event.EventService;
import com.nucleus.event.UserSecurityTrailEvent;
import com.nucleus.finnone.pro.cache.common.NeutrinoCachePopulator;
import com.nucleus.finnone.pro.general.util.ValidatorUtils;
import com.nucleus.license.metrics.Measure;
import com.nucleus.logging.BaseLoggers;
import com.nucleus.makerchecker.IdComparator;
import com.nucleus.makerchecker.MakerCheckerService;
import com.nucleus.makerchecker.UnapprovedEntityData;
import com.nucleus.master.BaseMasterService;
import com.nucleus.menu.MenuEntity;
import com.nucleus.query.constants.QueryHint;
import com.nucleus.reason.BlockReason;
import com.nucleus.reason.ReasonVO;
import com.nucleus.rules.model.SourceProduct;
import com.nucleus.service.BaseServiceImpl;
import com.nucleus.user.cache.IUserCacheService;
import com.nucleus.user.ipaddress.IpAddress;
import com.nucleus.userotpcount.service.UserOtpCountService;

import net.bull.javamelody.MonitoredWithSpring;

@Named(value = "userService")
@MonitoredWithSpring(name = "USER_SERVIE_IMPL_")
public class UserServiceImpl extends BaseServiceImpl implements UserService, ApplicationContextAware {

	private static final String AUTH_CODE = "authCode";

	private static final String LABEL_PROXY_CHECK_8 = "label.proxyCheck.8";

	private static final String LABEL_PROXY_CHECK_7 = "label.proxyCheck.7";

	private static final String LABEL_PROXY_CHECK_6 = "label.proxyCheck.6";

	private static final String LABEL_PROXY_CHECK_5 = "label.proxyCheck.5";

	private static final String LABEL_PROXY_CHECK_4 = "label.proxyCheck.4";

	private static final String LABEL_PROXY_CHECK_3 = "label.proxyCheck.3";

	private static final String LABEL_PROXY_CHECK_2 = "label.proxyCheck.2";

	private static final String LABEL_PROXY_CHECK_1 = "label.proxyCheck.1";

    private static final String LABEL_PROXY_CHECK_13 = "label.proxyCheck.13";

    private static final String LABEL_PROXY_CHECK_14 = "label.proxyCheck.14";

    private static final String LABEL_PROXY_CHECK_15 = "label.proxyCheck.15";

    private static final String LABEL_PROXY_CHECK_16 = "label.proxyCheck.16";

    private static final String LABEL_PROXY_CHECK_17 = "label.proxyCheck.17";

    private static final String LABEL_PROXY_CHECK_18 = "label.proxyCheck.18";

    private static final String LABEL_PROXY_CHECK_19 = "label.proxyCheck.19";

    private static final String LABEL_PROXY_CHECK_20 = "label.proxyCheck.20";

    private static final String LABEL_PROXY_CHECK_21 = "label.proxyCheck.21";

    private static final String LABEL_PROXY_CHECK_22 = "label.proxyCheck.22";

    private static final String LABEL_PROXY_CHECK_23 = "label.proxyCheck.23";



	private static final String USERS_IN_CURRENT_BRANCH = "Users.InCurrentBranch";

	@Inject
	@Named("usernameUsersIdCachePopulator")
	private NeutrinoCachePopulator usernameUsersIdCachePopulator;
	
	@Inject
	@Named("userIdUserProfileIdCachePopulator")
	private NeutrinoCachePopulator userIdUserProfileIdCachePopulator;
	
	@Inject
	@Named("authCodeAuthorityIdCachePopulator")
	private NeutrinoCachePopulator authCodeAuthorityIdCachePopulator;
	
    @Autowired
    private UserDao                      userDao;

    private ApplicationContext           applicationContext;

    @Inject
    @Named("frameworkMessageSource")
    protected MessageSource              messageSource;

    @Inject
    @Named("configurationService")
    protected ConfigurationService       configurationService;

    @Inject
    @Named("authenticationTokenService")
    protected AuthenticationTokenService authenticationTokenService;

    @Inject
    @Named("userManagementServiceCore")
    protected UserManagementServiceCore  userManagementServiceCore;

    @Inject
    @Named("userSessionManagerService")
    protected UserSessionManagerService  userSessionManagerService;

    @Inject
    @Named("baseMasterService")
    protected BaseMasterService          baseMasterService;

    @Inject
    @Named(value = "userSecurityService")
    protected UserSecurityService        securityService;

    @Inject
    @Named("eventService")
    private EventService                 eventService;

    @Inject
    @Named("makerCheckerService")
    private MakerCheckerService          makerCheckerService;
    
    @Inject
    @Named("userCacheService")
    private IUserCacheService userCacheService;

    @Inject
    @Named("genericParameterService")
    private GenericParameterService genericParameterService;
    
    @Value("#{'${password.expire.in.days}'.split(',')}")
    private List<String> passwordExpireInDaysNewList;
    @Inject
    @Named("userOtpCountService")
    private UserOtpCountService userOtpCountService;
    
    public static final String IS_CUSTOM_CACHE_ENABLED = "user.service.custom.cache.enabled";
        
    public static final String ORGANIZATION_BRANCH="organizationBranch";
    
    public static final String USER_NAME="username";
    
    public static final String USERS_ALLUSERSBYUSERNAME= "Users.allUsersByUsername";
    
    public static final String USERS_USER_BY_USERNAME_QUERY="Users.userByUsername";
    
    public static final String ROLE_NOT_NULL_ERROR="role cannot be null";
    
    public static final String STATUS_LIST="statusList";
    
    public static final String USER_ID="userId";
    
    public static final String USER_NAME_NOT_NULL_ERR="username cannot be null";
    
    public static final String USER_ID_CANT_NULL="userId cannot be null";




    @Override
    public void createUser(User user) {
        user.setUserStatus(UserStatus.STATUS_ACTIVE);
        entityDao.persist(user);
    }

    @Override
    public void saveUserProfile(UserProfile userProfile) {
    	NeutrinoValidator.notNull(userProfile);
        userProfile.setAssociatedUser(setUserPasswordExpirationDate(userProfile.getAssociatedUser()));
        BaseLoggers.flowLogger.info("auditing user Record");
        if (userProfile.getId() == null) {
            entityDao.persist(userProfile);
        } else {
            entityDao.update(userProfile);
        }
    }

    private void saveUserInDB(User user) {
        BaseLoggers.flowLogger.info("Saving User Profile in database");
        String userPwd = user.getPassword();
        entityDao.persist(user);
        UserEvent userEvent = new UserEvent(USER_CREATED_EVENT, true, getCurrentUser().getUserEntityId(), user);
        userEvent.addProperty(UserEvent.USER_PSWD, userPwd);
        userEvent.setUserName(user.getUsername());
        userEvent.setAssociatedUser(getCurrentUser().getDisplayName());
        eventBus.fireEvent(userEvent);
    }

    @Override
    @Measure(key = "named.user.created", eventType = "LICENSE_NAMED_USER_CREATED")
    public void saveNewUserProfile(UserProfile userProfile) {
        entityDao.persist(userProfile);
    }

    @Override
    public void saveUser(User user) {
    	NeutrinoValidator.notNull(user);
        if (user.getId() == null) {
            saveUserInDB(user);
        } else {
            entityDao.update(user);
            UserEvent userEvent = new UserEvent(USER_UPDATED_EVENT, true, getCurrentUser().getUserEntityId(), user);
            userEvent.addProperty(UserEvent.USER_PROFILE, getUserProfile(user));
            userEvent.setUserName(user.getUsername());
            userEvent.setAssociatedUser(getCurrentUser().getDisplayName());
            eventBus.fireEvent(userEvent);
        }

    }

    @Override
    public UserInfo loginUser(String username, String password) {
        return null;
    }

    @Override
    public void assignAuthoritiesToUser(Long userId, Set<Authority> authorities) {

        UserAuthority persistedUserAuthority = getUserAuthoritiesFromUserId(userId);
        if (persistedUserAuthority != null && persistedUserAuthority.getAssociatedUser() != null)
            persistedUserAuthority.getAuthorities().addAll(authorities);
        else if (persistedUserAuthority != null) {
            persistedUserAuthority.setAuthorities(authorities);
        } else {
            User user = new User();
            user.setId(userId);
            UserAuthority userAuthority = new UserAuthority();
            userAuthority.setAssociatedUser(user);
            userAuthority.setAuthorities(authorities);

            saveUserAuthority(userAuthority);
        }

    }

    
	@Override
    public UserInfo getUserFromUsername(String username) {
		NeutrinoValidator.notNull(username, USER_NAME_NOT_NULL_ERR);
		List<User> userList=getUserReferenceByUsername(username);
		if (ValidatorUtils.hasElements(userList))
		{
			return populateUserInfo(userList.get(0));
		}
		return null;
	}

    
	private boolean isUserNotDeleted(User user) {
		return user.getUserStatus() == UserStatus.STATUS_ACTIVE || user.getUserStatus() == UserStatus.STATUS_LOCKED
				|| user.getUserStatus() == UserStatus.STATUS_INACTIVE;
	}

	/**
	 * Get list of users by userName approved
	 */
	@Override
	public List<User> getUserReferenceByUsername(String username) {
		NeutrinoValidator.notNull(username, USER_NAME_NOT_NULL_ERR);
		return getUserReferenceByUsername(username, Boolean.TRUE);
	}
	
	
	/**
	 * Get list of users by userName all/approved
	 *  Query-->  Select u from User u where u.username = :username AND (u.userStatus in (0, 1, 2)) and 
	 *          (u.masterLifeCycleData.approvalStatus in (0,3))
	 */
    @SuppressWarnings("unchecked")
	@Override
	public List<User> getUserReferenceByUsername(String username,
			Boolean isApprovedUsers) {
		NeutrinoValidator.notNull(username, USER_NAME_NOT_NULL_ERR);

		
		List<User> userList = new ArrayList<>();
		Set<Long> userIdSet = (Set<Long>) usernameUsersIdCachePopulator.get(username.toLowerCase());
		if (ValidatorUtils.hasElements(userIdSet)) {
			if (isApprovedUsers) {
				return getApprovedUsersFromUserSet(userIdSet);
			} else {
				for (Long userId : userIdSet) {
					userList.add(entityDao.find(User.class, userId));
				}
			}
		}
		return userList;
	}
    
    /**
     * get all/approved users fromDB
     * @param username
     * @param getApprovedUsers
     * @return never returns null 
     */
    private List<User> getUserListByUserNameFromDb(String username, Boolean getApprovedUsers) {
    	if (getApprovedUsers) {
			NamedQueryExecutor<User> userExecutor = new NamedQueryExecutor<User>(USERS_USER_BY_USERNAME_QUERY)
					.addParameter(USER_NAME, username.toLowerCase())
					.addParameter("userStatus", UserStatus.ALL_STATUSES_EXCLUDING_DELETED)
	        		.addParameter("approvalStatusList", ApprovalStatus.APPROVED_RECORD_STATUS_IN_APPROVED_APPROVED_MODIFIED)
					.addQueryHint(QueryHint.QUERY_HINT_CACHEABLE, Boolean.TRUE);
			return userDao.executeQuery(userExecutor);
		} else {
			return getAllUsersByUserName(username);
		}
    }

	@Override
    public UserInfo getRandomUserByAuthority(String authorityCode) {
        NamedQueryExecutor<User> userExecutor = new NamedQueryExecutor<User>("Users.userByAuthorityOnRoles")
        		.addParameter(AUTH_CODE, authorityCode)
        		.addParameter("approvalStatusList", ApprovalStatus.APPROVED_RECORD_STATUS_IN_APPROVED_APPROVED_MODIFIED)
        		.addQueryHint(QueryHint.QUERY_HINT_CACHEABLE, Boolean.TRUE);
        List<User> users = userDao.executeQuery(userExecutor);
        if (ValidatorUtils.hasElements(users)) {
            Collections.shuffle(users);
            return populateUserInfo(users.get(0));
        } else {
            return null;
        }
    }

    @Override
    public List<UserGroup> getUserGroups() {
        NamedQueryExecutor<UserGroup> userGroupsExecutor = new NamedQueryExecutor<>("Users.allUserGroups");
        return userDao.executeQuery(userGroupsExecutor);
    }

	@Override
    public List<Authority> getAuthorities() {
    	NamedQueryExecutor<Authority> authoritiesExecutor = new NamedQueryExecutor<>("Users.allAuthorities");
        return userDao.executeQuery(authoritiesExecutor);
	}

	@Override
	public List<Role> getRoles() {
		NamedQueryExecutor<Role> rolesExecutor = new NamedQueryExecutor<Role>("Users.allRoles")
				.addParameter("productDescriminator", ProductInformationLoader.getProductName());
		return userDao.executeQuery(rolesExecutor);
	}
	
	@Override
	public UserInfo getUserById(Long userId) {
		NeutrinoValidator.notNull(userId, USER_ID_CANT_NULL);
		UserInfo userInfo = null;
		User user = userDao.find(User.class, userId);
		if (user != null) {
			userInfo = populateUserInfo(user);
		}
		return userInfo;
	}
	


    @Override
    public String getUserMailById(Long userId) {
        NeutrinoValidator.notNull(userId, USER_ID_CANT_NULL);
        
        User user = entityDao.find(User.class,userId);
        if(ValidatorUtils.notNull(user)){
        	return user.getMailId();
        }
        
        return null;
    }

    @Override
	public String getUserNameByUserUri(String userUri) {
		NeutrinoValidator.notNull(userUri, "userUri cannot be null");
		User user = getUserByUri(userUri);

		if (ValidatorUtils.notNull(user)) {
			return user.getUsername();
		}
		return null;
	}

    @Override
    public String getUserNameByUserId(Long userId) {
        NeutrinoValidator.notNull(userId, USER_ID_CANT_NULL);
        
        User user = entityDao.find(User.class,userId);
        if(ValidatorUtils.notNull(user)){
        	return user.getUsername();
        }
        
        return "";
    }

    @Override
    public Role getRoleById(Long roleId) {
        NeutrinoValidator.notNull(roleId, "roleId cannot be null");
        return userDao.find(Role.class, roleId);
    }

    @Override
    public Authority getAuthorityById(Long authorityId) {
        NeutrinoValidator.notNull(authorityId, "authorityId cannot be null");
        return userDao.find(Authority.class, authorityId);
    }

    @Override
    public UserGroup getUserGroupById(Long userGroupId) {
        NeutrinoValidator.notNull(userGroupId, "userGroupId cannot be null");
        return userDao.find(UserGroup.class, userGroupId);
    }

    
    /**
     * Query-->
     *           Select au FROM Authority au WHERE au.authCode=:authCode
     */
	@Override
	public Authority getAuthorityByCode(String authCode) {
		NeutrinoValidator.notNull(authCode, "authCode cannot be null");
		Authority authority=null;
		Long authorityId = (Long) authCodeAuthorityIdCachePopulator.get(authCode);
		if (ValidatorUtils.notNull(authorityId)) {
			authority=entityDao.find(Authority.class, authorityId);			
		}
		return authority;
	}

    public List<Authority> getAuthorityByCodeFromDb(String authCode) {
		NamedQueryExecutor<Authority> authorityExecutor = new NamedQueryExecutor<Authority>("Users.getAuthority")
				.addParameter(AUTH_CODE, authCode);
		return userDao.executeQuery(authorityExecutor);
	}

	@Override
    public void saveUserGroup(UserGroup userGroup) {
        NeutrinoValidator.notNull(userGroup, "userGroup cannot be null");
        entityDao.persist(userGroup);
    }

    @Override
    public void saveRole(Role role) {
        NeutrinoValidator.notNull(role);
        if (role.getId() == null) {
            entityDao.persist(role);
        } else {
            entityDao.update(role);
        }
    }

    @Override
    public void saveUserAuthority(UserAuthority userAuthority) {
        NeutrinoValidator.notNull(userAuthority, "userAuthority cannot be null");
        entityDao.persist(userAuthority);
    }

	@Override
    @MonitoredWithSpring(name = "USI_FETCH_ROLES_FOR_USRID")
	public List<Role> getRolesFromUserId(Long userId) {
		NeutrinoValidator.notNull(userId, USER_ID_CANT_NULL);
		
		NeutrinoValidator.notNull(userId, "userId cannot be null");
		NamedQueryExecutor<Role> rolesExecutor = new NamedQueryExecutor<Role>("Users.getRoleFromUser")
				.addParameter("userId", userId)
				.addParameter("userStatus", UserStatus.ALL_STATUSES_EXCLUDING_DELETED)				
				.addQueryHint(QueryHint.QUERY_HINT_CACHEABLE, Boolean.TRUE);
		return userDao.executeQuery(rolesExecutor);
	}

	@Override
    public void saveRolesForUser(User user, Long[] roleIds) {
        if (user != null && roleIds != null && (roleIds.length > 0 || !user.isLoginEnabled())) {
            List<Role> roleList = getRolesFromUserId(user.getId());

            for (int i = 0 ; i < roleIds.length ; i++) {
            	saveRoleWithId(roleList,roleIds[i],user);
            }
            if (roleList != null) {
                for (Role userRole : roleList) {
                	userRole.getUsers().remove(user);
                    saveRole(userRole);
            }
           }
        }
    }

    private void saveRoleWithId(List<Role> roleList, Long roleIds, User user) {
        Role role = new Role();
        role.setId(roleIds);
        if (roleList != null && roleList.contains(role)) {
            roleList.remove(role);
        } 
        else 
        {
			role = getRoleById(role.getId());

			if (role.getUsers() == null) {
				Set<User> userSet = new HashSet<>();
				role.setUsers(userSet);
			}
			role.getUsers().add(user);
			saveRole(role);

        }
		
	}

	@Override
    public List<UserGroup> getUserGroupsFromUserId(Long userId) {
        NeutrinoValidator.notNull(userId, USER_ID_CANT_NULL);
        NamedQueryExecutor<UserGroup> rolesExecutor = new NamedQueryExecutor<UserGroup>("Users.allUserGroupsFromUser")
                .addParameter(USER_ID, userId)
        		.addParameter("userStatus", Arrays.asList(UserStatus.STATUS_ACTIVE,UserStatus.STATUS_LOCKED));
        return userDao.executeQuery(rolesExecutor);
    }

    @Override
    public UserAuthority getUserAuthoritiesFromUserId(Long userId) {
        NeutrinoValidator.notNull(userId, USER_ID_CANT_NULL);
        NamedQueryExecutor<UserAuthority> userAuthoritiesExecutor = new NamedQueryExecutor<UserAuthority>(
                "Users.allUserAuthoritiesFromUser").addParameter(USER_ID, userId)
        		.addParameter("userStatus", Arrays.asList(UserStatus.STATUS_ACTIVE,UserStatus.STATUS_LOCKED));
        List<UserAuthority> uaList = userDao.executeQuery(userAuthoritiesExecutor);
        if (ValidatorUtils.hasElements(uaList)) {
            return uaList.get(0);
        } else
            return null;
    }

    @Override
    public void deleteUser(User user) {
        NeutrinoValidator.notNull(user);
        changeUserStatus(user, UserStatus.STATUS_DELETED);
    }

    @Override
    public void deleteRole(Role role) {
        NeutrinoValidator.notNull(role, ROLE_NOT_NULL_ERROR);
        entityDao.delete(role);
    }

    @Override
    public void deleteUserGroup(UserGroup userGroup) {
        NeutrinoValidator.notNull(userGroup, "userGroup cannot be null");
        entityDao.delete(userGroup);
    }

    @Override
    public Set<Authority> loadOnlyUserAuthorities(User user) {
        NeutrinoValidator.notNull(user);
        NamedQueryExecutor<Authority> userAuthoritiesExecutor = new NamedQueryExecutor<Authority>("Users.userAuthorities")
                .addParameter(USER_ID, user.getId())
                .addParameter("userStatus", Arrays.asList(UserStatus.STATUS_ACTIVE,UserStatus.STATUS_LOCKED));
        List<Authority> authorities = userDao.executeQuery(userAuthoritiesExecutor);
        Set<Authority> userAuthorities = new HashSet<>();
        if (authorities != null) {
            userAuthorities.addAll(authorities);
        }

        return userAuthorities;
    }

    @Override
    public Set<Authority> loadOnlyRoleAuthorities(User user) {
        NeutrinoValidator.notNull(user);
        NamedQueryExecutor<Authority> roleAuthoritiesExecutor = new NamedQueryExecutor<Authority>(
                "Users.authoritiesByUserRoles").addParameter(USER_ID, user.getId())
        		.addParameter("activeFlag", true)
        		.addParameter("userStatus", Arrays.asList(UserStatus.STATUS_ACTIVE,UserStatus.STATUS_LOCKED))
        		.addQueryHint(QueryHint.QUERY_HINT_CACHEABLE, Boolean.TRUE);
        List<Authority> roleAuthorities = userDao.executeQuery(roleAuthoritiesExecutor);
        Set<Authority> userAuthorities = new HashSet<>();
        if (roleAuthorities != null) {
            userAuthorities.addAll(roleAuthorities);
        }

        return userAuthorities;
    }

    @Override
    public Set<Authority> loadOnlyUserGroupAuthorities(User user) {
        NeutrinoValidator.notNull(user);

        Set<Authority> userAuthorities = new HashSet<>();
        NamedQueryExecutor<Authority> groupAuthoritiesExecutor = new NamedQueryExecutor<Authority>("Users.authoritiesByUserGroups")
        		.addParameter(USER_ID, user.getId())
        		.addParameter("activeFlag", true)
        		.addParameter("userStatus", Arrays.asList(UserStatus.STATUS_ACTIVE,UserStatus.STATUS_LOCKED));;
        List<Authority> groupAuthorities = userDao.executeQuery(groupAuthoritiesExecutor);
        if (groupAuthorities != null) {
            userAuthorities.addAll(groupAuthorities);
        }

        return userAuthorities;
    }

    @Override
    public void activateUser(User user) {
        NeutrinoValidator.notNull(user);

        User usr = userDao.get(user.getEntityId());

        if(usr.isAccountLocked()){
            if(usr.getApprovalStatus() != ApprovalStatus.APPROVED){
                throw new SystemException(messageSource.getMessage(UserConstants.PFA_USER_BEING_UNBLOCKED,null,getUserLocale()));
            }
            user.unlock();
            user.setNumberOfFailedLoginAttempts(0);
            user.setNumberOfFailedPassResetAttempts(0);
            user.setDaysToBlock(0);
            userOtpCountService.resetUserOtpCount(usr.getUsername());
        }else{
            throw new SystemException("User is not marked locked");
        }

        UserInfo currentUser = getCurrentUser();
        if (currentUser == null){
            currentUser = securityService.getCompleteUserFromUsername(UserSecurityService.NEUTRINO_SYSTEM_USER);
        }

        entityDao.saveOrUpdate(user);
        
        UserEvent userEvent = new UserEvent(USER_ACTIVATED_EVENT, true, currentUser.getUserEntityId(), user);
        userEvent.setUserName(user.getUsername());
        userEvent.setAssociatedUser(currentUser.getDisplayName());
        eventBus.fireEvent(userEvent);
    }

    @Override
    public void invalidateUser(User user) {
        NeutrinoValidator.notNull(user);
        changeUserStatus(user, UserStatus.STATUS_INACTIVE);
        UserEvent userEvent = new UserEvent(USER_INACTIVATED_EVENT, true, getCurrentUser().getUserEntityId(), user);
        userEvent.setUserName(user.getUsername());
        userEvent.setAssociatedUser(getCurrentUser().getDisplayName());
        eventBus.fireEvent(userEvent);
    }

    @Override
    public void lockUser(User user, EntityId byUserEntityId) {
        lockUser(user, byUserEntityId, null);
    }

    @Override
    public void lockUser(User user, EntityId byUserEntityId, Integer daysToBlock) {
        NeutrinoValidator.notNull(user);
        changeUserStatus(user, UserStatus.STATUS_LOCKED);
        user.setLastLockedDate(DateUtils.getCurrentUTCTime());
        if (daysToBlock != null && daysToBlock > 0) {
            user.setDaysToBlock(daysToBlock);
        }
        PasswordResetToken passwordResetToken = user.getPasswordResetToken();
        user.setPasswordResetToken(null);
        UserEvent userEvent = new UserEvent(USER_BLOCKED_EVENT, true, byUserEntityId, user);
        userEvent.setUserName(user.getUsername());
        eventBus.fireEvent(userEvent);
        authenticationTokenService.deleteOldToken(user.getId(), passwordResetToken);

    }

   
    @Override
    public void markReasonForSystem(User user ,String blockReasonCode) {

        UserAuditLog userAuditLog = new UserAuditLog();

        userAuditLog.setUserId(user.getId());

        Integer version = getLatestVersionOfAuditForUser(user.getId());
        if (version != null) {
            version = version + 1;
            userAuditLog.setVersion(version);
        } else {
            version = 0;
            userAuditLog.setVersion(version);
        }

        userAuditLog.setUserEvent(UserConstants.USER_BLOCK_EVENT);
        //update reason

        String reasonRemarks = "Blocked by System";

        BlockReason blockReason = genericParameterService.findByCode(blockReasonCode,BlockReason.class);

        if(blockReason!=null){
            userAuditLog.setBlockReason(blockReason);
        }else{
             throw new SystemException("BlockReason not found for Id = "+ blockReasonCode);
        }

        userAuditLog.setReasonRemarks(reasonRemarks);
        saveUserAuditLog(userAuditLog);
    }

    @Override
    public void addUsersToRole(Role role, Set<User> users) {
    	NeutrinoValidator.notNull(role, ROLE_NOT_NULL_ERROR);
    	Role persistedRole = userDao.find(Role.class, role.getId());

    	if(ValidatorUtils.notNull(persistedRole)){
			if (persistedRole.getUsers() != null)
				persistedRole.getUsers().addAll(users);
			else {
				persistedRole.setUsers(users);
			}
    	}
    }

    @Override
    public void removeAllUsersFromRole(Role role) {
    	NeutrinoValidator.notNull(role, ROLE_NOT_NULL_ERROR);
    	
    	Role persistedRole = userDao.find(Role.class, role.getId());
    	
        if (persistedRole != null && persistedRole.getUsers() != null && !persistedRole.getUsers().isEmpty()) {
            persistedRole.getUsers().clear();
        }

    }

    @Override
    public void addUsersToUserGroup(UserGroup userGroup, Set<User> users) {

        UserGroup persistedUserGroup = getUserGroupById(userGroup.getId());
        if (persistedUserGroup == null)
        {
        	return;
        }
        if (persistedUserGroup.getUsers() != null)
        {
            persistedUserGroup.getUsers().addAll(users);
        }
        else {
            persistedUserGroup.setUsers(users);
        }

    }

    @Override
    public void removeAllUsersFromUserGroup(UserGroup userGroup) {
        UserGroup persistedUserGroup = getUserGroupById(userGroup.getId());
        persistedUserGroup.getUsers().clear();
    }

	@Override
	public void assignAuthoritiesToRole(Role role, Set<Authority> authorities) {

		Role persistedRole = userDao.find(Role.class, role.getId());

		if (ValidatorUtils.notNull(persistedRole)) {

			if (ValidatorUtils.notNull(persistedRole.getAuthorities())) {
				persistedRole.getAuthorities().addAll(authorities);
			} else {
				persistedRole.setAuthorities(authorities);
			}
		}

	}

	@Override
	public void removeAllAuthoritiesFromRole(Role role) {

		Role persistedRole = userDao.find(Role.class, role.getId());

		if (ValidatorUtils.notNull(persistedRole) && ValidatorUtils.notNull(persistedRole.getAuthorities())) {
			persistedRole.getAuthorities().clear();
		}
	}

    @Override
    public void assignAuthoritiesToUserGroup(UserGroup userGroup, Set<Authority> authorities) {

        UserGroup persistedUserGroup = getUserGroupById(userGroup.getId());

        if(persistedUserGroup==null)
        {
        	return;
        }
        if (persistedUserGroup.getUsers() != null)
        {
            persistedUserGroup.getAuthorities().addAll(authorities);
        }
        else {
            persistedUserGroup.setAuthorities(authorities);
        }

    }

    @Override
    public void removeAllAuthoritiesFromUserGroup(UserGroup userGroup) {
        UserGroup persistedUserGroup = getUserGroupById(userGroup.getId());
        if (persistedUserGroup != null && persistedUserGroup.getAuthorities() != null
                && ValidatorUtils.hasElements(persistedUserGroup.getAuthorities())) {
            persistedUserGroup.getAuthorities().clear();
        }

    }

    @Override
    public void removeAllAuthoritiesFromUser(User user) {
        UserAuthority userAuthority = getUserAuthoritiesFromUserId(user.getId());
        if (userAuthority != null && userAuthority.getAuthorities() != null && ValidatorUtils.hasElements(userAuthority.getAuthorities())) {
            userAuthority.getAuthorities().clear();
        }
    }

    private void changeUserStatus(User user, int status) {
        User usr = userDao.get(user.getEntityId());
        usr.setUserStatus(status);
    }

    /**
     * Get userInfo from user Object
     * @param user
     * @return
     */
	private UserInfo populateUserInfo(User user) {
		UserInfo userInfo = new UserInfo(user);
		// populating display Name from UserProfile for a user
		UserProfile profile = getUserProfile(user);
		if (profile != null) {
			if (profile.getSalutation() != null) {
				if (StringUtils.isEmpty(profile.getSalutation().getName()))
					userInfo.setDisplayName(profile.getFullName());
				else{
					String displayName=profile.getFullName().replaceFirst(
							profile.getSalutation().getName(), "");
					userInfo.setDisplayName(displayName.trim());
				}
			} else
				userInfo.setDisplayName(profile.getFullName());
		}
		return userInfo;
	}

    /**
     * Temporary Method to find a user with lowest Primary key with with given
     * authority. Query can be improved in future : Amit Parashar
     * 
     * @param authorityCode
     * @return UserInfo
     */
    @Override
    public UserInfo getFirstUserByAuthority(String authorityCode) {
        String[] activeProfiles = applicationContext.getEnvironment().getActiveProfiles();
        String allActiveProfiles = Arrays.toString(activeProfiles);
        String sourceSystem = null;
        if (allActiveProfiles.contains(UserService.SOURCE_DB)) {
            sourceSystem = UserService.SOURCE_DB;
        } else {
            sourceSystem = UserService.SOURCE_LDAP;
        }
        NamedQueryExecutor<User> userExecutor = new NamedQueryExecutor<User>("Users.userByAuthorityOnRoles")
                .addParameter(AUTH_CODE, authorityCode).addParameter("srcSystem", sourceSystem)
                .addParameter("approvalStatusList", ApprovalStatus.APPROVED_RECORD_STATUS_IN_APPROVED_APPROVED_MODIFIED)
                .addQueryHint(QueryHint.QUERY_HINT_CACHEABLE, Boolean.TRUE);
        List<User> users = userDao.executeQuery(userExecutor);
        if (ValidatorUtils.hasElements(users)) {
            Collections.min(users, new IdComparator());
            return populateUserInfo(users.get(0));
        } else {
            return null;
        }
    }

    @Override
    public List<Long> getAllUserWithGivenAuthority(String authorityCode) {
        NamedQueryExecutor<Long> userExecutor = new NamedQueryExecutor<Long>("Users.userIdsByAuthorityOnRoles")
                .addParameter(AUTH_CODE, authorityCode)
                .addParameter("activeFlag", true)
                .addParameter("approvalStatusList", ApprovalStatus.APPROVED_RECORD_STATUS_IN_APPROVED_APPROVED_MODIFIED)
                .addQueryHint(QueryHint.QUERY_HINT_CACHEABLE, Boolean.TRUE);
        return userDao.executeQuery(userExecutor);
    }

    @Override
    public List<Authority> getAuthoritiesByUserRoleNames(List<String> roleNames) {
        NamedQueryExecutor<Authority> authExecutor = new NamedQueryExecutor<Authority>("Users.authoritiesByUserRoleNames")
                .addParameter("roleNames", roleNames);
        return userDao.executeQuery(authExecutor);
    }

	@Override
	public User userExistenceInForgotPassword(String userName, String mailId) {
		NeutrinoValidator.notNull(userName, USER_NAME_NOT_NULL_ERR);
		NeutrinoValidator.notNull(mailId, "mailId cannot be null");
		List<User> listUser=getUserFromDbByMailIdAndUserName(userName,mailId);
		if (ValidatorUtils.hasElements(listUser))
		{
			return listUser.get(0);
		}
		return null;
	}

	private List<User> getUserFromDbByMailIdAndUserName(String userName, String mailId) {
		NamedQueryExecutor<User> userByUsernameAndMailIdExecutor = new NamedQueryExecutor<User>(
				"Users.userByUsernameAndMailId").addParameter(USER_NAME, userName.toLowerCase())
						.addParameter("mailId", mailId.toLowerCase())
						.addParameter("approvalStatusList", ApprovalStatus.APPROVED_RECORD_STATUS_IN_APPROVED_APPROVED_MODIFIED)
						.addQueryHint(QueryHint.QUERY_HINT_CACHEABLE, Boolean.TRUE);
		return entityDao.executeQuery(userByUsernameAndMailIdExecutor);
	}

	@Override
	public List<User> getAllUser() {
        List<Integer> statusList = new ArrayList<>();
        statusList.add(ApprovalStatus.UNAPPROVED_ADDED);
        statusList.add(ApprovalStatus.WORFLOW_IN_PROGRESS);
        statusList.add(ApprovalStatus.APPROVED);
        statusList.add(ApprovalStatus.APPROVED_MODIFIED);
        statusList.add(ApprovalStatus.UNAPPROVED_MODIFIED);
        statusList.add(ApprovalStatus.APPROVED_DELETED);
        statusList.add(ApprovalStatus.APPROVED_DELETED_IN_PROGRESS);
        NamedQueryExecutor<User> userList = new NamedQueryExecutor<User>("Users.allUsers").addParameter("statusList", statusList);
        return entityDao.executeQuery(userList);
	}
	
    @Override
    public List<User> getAllActiveAndInactiveUser() {
        return entityDao.findAll(User.class);
    }
    
    @Override
    public void updateUser(User user) {
        entityDao.update(user);
    }

    @Override
    public User findUserByPasswordResetTimeToken(String timeToken) {
        NamedQueryExecutor<User> userBytimeTokenExecutor = new NamedQueryExecutor<User>("Users.userByPasswordResetTimeToken")
                .addParameter("timeToken", authenticationTokenService.getEncryptedToken(timeToken));
        List<User> listUser = entityDao.executeQuery(userBytimeTokenExecutor);

        if (ValidatorUtils.hasElements(listUser))
            return listUser.get(0);
        else
        {
            NamedQueryExecutor<User> userBytimeTokenExecutorMD5 = new NamedQueryExecutor<User>("Users.userByPasswordResetTimeToken")
                    .addParameter("timeToken", authenticationTokenService.getEncryptedTokenMD5(timeToken));
            List<User> listUserMD5 = entityDao.executeQuery(userBytimeTokenExecutorMD5);

            if (ValidatorUtils.hasElements(listUserMD5))
                return listUserMD5.get(0);
            else
                return null;
        }
    }

    @Override
    public Object[] findApproveLinkTokenByTokenId(String timeToken) {
        NamedQueryExecutor<Object[]> userBytimeTokenExecutor = new NamedQueryExecutor<Object[]>(
                "Users.userByApproveLinkTimeToken").addParameter("timeToken",
                authenticationTokenService.getEncryptedToken(timeToken));
        List<Object[]> objects = entityDao.executeQuery(userBytimeTokenExecutor);

        if (ValidatorUtils.hasElements(objects))
            return objects.get(0);
        else
        {
            NamedQueryExecutor<Object[]> userBytimeTokenExecutorMD5 = new NamedQueryExecutor<Object[]>(
                    "Users.userByApproveLinkTimeToken").addParameter("timeToken",
                    authenticationTokenService.getEncryptedTokenMD5(timeToken));
            List<Object[]> objectsMD5 = entityDao.executeQuery(userBytimeTokenExecutorMD5);

            if (ValidatorUtils.hasElements(objectsMD5))
                return objectsMD5.get(0);
            else
                return null;
        }

    }

    
	@Override
	public User findUserByUsername(String username) {
		NeutrinoValidator.notNull(username, USER_NAME_NOT_NULL_ERR);
		return findUserByUsername(username, Boolean.TRUE);
	}
    
    @Override
	public User findUserByUsername(String username, Boolean getApprovedUsers) {
		NeutrinoValidator.notNull(username, USER_NAME_NOT_NULL_ERR);
		List<User> userList=getUserReferenceByUsername(username, getApprovedUsers);
		return userList.isEmpty() ? null : userList.get(0);
	}

    private List<User> getApprovedUsersFromUserSet(Set<Long> allUserIdSet) {
    	
    	List<User> userList  = new ArrayList<>();
    	for (Long userId : allUserIdSet) {
			User user=entityDao.find(User.class, userId);
			if ((isUserNotDeleted(user))
					&& (user.getApprovalStatus() == ApprovalStatus.APPROVED
							|| user.getApprovalStatus() == ApprovalStatus.APPROVED_MODIFIED)) {
				userList.add(user);
			}
		}
		return userList;
	}


	/**
	 * Get all users by username irrespective of approval status from DB
	 * @param username
	 * @return returns all users for username.
	 */
	public List<User> getAllUsersByUserName(String username) {
		NamedQueryExecutor<User> userExecutor = new NamedQueryExecutor<User>(USERS_ALLUSERSBYUSERNAME)
				.addParameter(USER_NAME, username.toLowerCase());
		
		return entityDao.executeQuery(userExecutor);
	}

	@Override
    public User findUserByUUID(String uuid) {
        NamedQueryExecutor<User> userExecutor = new NamedQueryExecutor<User>("Users.userByUUid").addParameter("uuid", uuid)
                .addQueryHint(QueryHint.QUERY_HINT_CACHEABLE, Boolean.TRUE);
        List<User> userList = userDao.executeQuery(userExecutor);
        User user = null;
        if (ValidatorUtils.hasElements(userList)) {
            user = userList.get(0);
            return user;
        } else {
            return null;
        }
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Override
    public List<User> getLoginDetails(String username) {
        NamedQueryExecutor<User> userExecutor = new NamedQueryExecutor<User>("Users.loginDetails").addParameter(USER_NAME,
                username);
        return userDao.executeQuery(userExecutor);
    }

	@Override
	@MonitoredWithSpring(name = "USI_FETCH_USR_PROFILE")
	public UserProfile getUserProfile(User user) {
		NeutrinoValidator.notNull(user);
		NeutrinoValidator.notNull(user.getId(), USER_ID_CANT_NULL);
		return getUserProfileByUserId(user.getId());
    }

	/**
	 * get UserProfile by user id from db/cache
	 */
	@Override
	public UserProfile getUserProfileByUserId(Long userId) {
		NeutrinoValidator.notNull(userId, USER_ID_CANT_NULL);
		Long userProfileId = (Long) userIdUserProfileIdCachePopulator.get(userId);
		if (userProfileId != null) {
			return entityDao.find(UserProfile.class, userProfileId);
		}
		return null;
	}

    public UserProfile getUserProfileByUserIdFromDb(Long userId)
    {
    	NamedQueryExecutor<UserProfile> userExecutor = new NamedQueryExecutor<UserProfile>("Users.getUserProfileFromUserId")
                .addParameter("userId",userId);

        return entityDao.executeQueryForSingleValue(userExecutor);
    }
	
	@Override
    @MonitoredWithSpring(name = "USI_FETCH_USR_PROFILE_PHOTO")
	public String getUserPhotoUrl(Long userId) {
		NeutrinoValidator.notNull(userId, USER_ID_CANT_NULL);

		UserProfile userProfile = getUserProfileByUserId(userId);
		if(ValidatorUtils.notNull(userProfile)){
			return userProfile.getPhotoUrl();
		}
		
		return null;
		
	}

    @Override
    public boolean isIpAddressInRange(String ip, String fromIp, String toIp) {

        long ipVal = ipToLong(ip);
        long fromIpVal = ipToLong(fromIp);
        long toIpVal = ipToLong(toIp);
        return ipVal >= fromIpVal && ipVal <= toIpVal;
    }

    public boolean isSecuredIpAddressInMaster(String ip){
        NamedQueryExecutor<IpAddress> executor = new NamedQueryExecutor<IpAddress>("Users.findSecuredIpInMaster")
                .addParameter("ip", ip)
                .addParameter("approvalStatusList", ApprovalStatus.APPROVED_RECORD_STATUS_IN_APPROVED_APPROVED_MODIFIED);
        List<IpAddress> ipAddress = entityDao.executeQuery(executor);
        if(ipAddress!=null && !ipAddress.isEmpty()){
            return true;
        }

        return false;


    }


    private long ipToLong(String ipAddress) {
        InetAddress ip;
        long result = 0;
        try {
            ip = InetAddress.getByName(ipAddress);
            byte[] octets = ip.getAddress();

            for (byte octet : octets) {
                result <<= 8;
                result |= octet & 0xff;
            }
        } catch (Exception e) {
            throw new SystemException("Exception while converting ip address to Long", e);
        }

        return result;
    }
    @Override
    public IPAddressRange getUserProfileIPAddressRange(Long userId){
        NamedQueryExecutor<IPAddressRange> userExecutor = new NamedQueryExecutor<IPAddressRange>("Users.getUserProfileIPAddressRange")
                .addParameter(USER_ID,userId).addQueryHint(QueryHint.QUERY_HINT_CACHEABLE, true);
       return entityDao.executeQueryForSingleValue(userExecutor);
  }

    @Override
    public String validateUserIp(Map<String, String> headers, String ipCameFrom, UserProfile userProfile, Locale userLocale) {

    	Locale locale=userLocale;
        if (locale == null) {
            locale = Locale.getDefault();
        }
        IPAddressRange ipAddressRange;
        String proxyMessage = null;
        String ipAddress = null;
        String fromIpAddress = null;
        String toIpAddress = null;
        Boolean securedIp=null;
        String ipavailableInDatabase = null;
        if (userProfile != null) {
            ipAddressRange = userProfile.getAddressRange();
            if (ipAddressRange != null) {
                ipAddress = ipAddressRange.getIpaddress();
                fromIpAddress = ipAddressRange.getFromIpAddress();
                toIpAddress = ipAddressRange.getToIpAddress();
                securedIp = ipAddressRange.getSecuredIp();
            }
        }
        if (ipAddress != null) {
            ipavailableInDatabase = "singleIP";
        }
        if (fromIpAddress != null && (fromIpAddress.equals("")) && toIpAddress != null && !(toIpAddress.equals(""))) {
            ipavailableInDatabase = ipavailableInDatabase!=null ? ipavailableInDatabase : "" + "rangeIp";
        }
        if(securedIp!=null && securedIp){
            ipavailableInDatabase = ipavailableInDatabase!=null ? ipavailableInDatabase : "" + "securedIp";
        }

        String via = null;
        String forwarded = null;
        String xff = null;
        String clientIP = null;
        if (ipavailableInDatabase != null) {

            via = headers.get("Via");
            forwarded = headers.get("Forwarded");
            xff = headers.get("X-Forwarded-For");
            clientIP = headers.get("Client-ip");

            messageSource.getMessage(LABEL_PROXY_CHECK_1, null, locale);

            if (ipavailableInDatabase.contains("singleIP")) {

                if (via != null) {
                    if (ipCameFrom.equals(ipAddress)) {
                        proxyMessage = messageSource.getMessage(LABEL_PROXY_CHECK_1, null, locale);
                    } else {
                        if (forwarded != null) {
                            if (forwarded.equals(ipAddress)) {
                                proxyMessage = messageSource.getMessage(LABEL_PROXY_CHECK_2, null, locale);
                            } else {
                                proxyMessage = messageSource.getMessage(LABEL_PROXY_CHECK_3, null, locale);
                            }
                        } else if (xff != null) {
                            if (xff.equals(ipAddress)) {
                                proxyMessage = messageSource.getMessage(LABEL_PROXY_CHECK_4, null, locale);
                            } else {
                                proxyMessage = messageSource.getMessage(LABEL_PROXY_CHECK_5, null, locale);
                            }
                        } else if (clientIP != null) {
                            if (clientIP.equals(ipAddress)) {
                                proxyMessage = messageSource.getMessage(LABEL_PROXY_CHECK_6, null, locale);
                            } else {
                                proxyMessage = messageSource.getMessage(LABEL_PROXY_CHECK_7, null, locale);
                            }
                        } else {
                            if (!(ipCameFrom.equals(ipAddress))) {
                                proxyMessage = messageSource.getMessage(LABEL_PROXY_CHECK_8, null, locale);
                            }
                        }
                    }
                } else {

                    if (ipCameFrom.equals(ipAddress) && forwarded == null && xff == null && clientIP == null) {
                        proxyMessage = messageSource.getMessage("label.proxyCheck.9", null, locale);
                    } else {

                        if (ipCameFrom.equals(ipAddress)) {
                            // "Via" header is missing
                            proxyMessage = messageSource.getMessage("label.proxyCheck.10", null, locale);
                        }

                        else if (forwarded != null) {
                            if (forwarded.equals(ipAddress)) {
                                proxyMessage = messageSource.getMessage(LABEL_PROXY_CHECK_2, null, locale);
                            } else {
                                proxyMessage = messageSource.getMessage(LABEL_PROXY_CHECK_3, null, locale);
                            }
                        } else if (xff != null) {
                            if (xff.equals(ipAddress)) {
                                proxyMessage = messageSource.getMessage(LABEL_PROXY_CHECK_4, null, locale);
                            } else {
                                proxyMessage = messageSource.getMessage(LABEL_PROXY_CHECK_5, null, locale);
                            }
                        } else if (clientIP != null) {
                            if (clientIP.equals(ipAddress)) {
                                proxyMessage = messageSource.getMessage(LABEL_PROXY_CHECK_6, null, locale);
                            } else {
                                proxyMessage = messageSource.getMessage(LABEL_PROXY_CHECK_7, null, locale);
                            }
                        } else {
                            if (!(ipCameFrom.equals(ipAddress))) {
                                proxyMessage = messageSource.getMessage("label.proxyCheck.11", null, locale);
                            }
                        }
                    }
                }

            }
            if (ipavailableInDatabase.contains("rangeIp") && proxyMessage != null && !proxyMessage.contains("{success}")) {

                if (via != null) {
                    if (this.isIpAddressInRange(ipCameFrom, fromIpAddress, toIpAddress)) {
                        proxyMessage = messageSource.getMessage(LABEL_PROXY_CHECK_1, null, locale);
                    } else {
                        if (forwarded != null) {
                            if (this.isIpAddressInRange(forwarded, fromIpAddress, toIpAddress)) {
                                proxyMessage = messageSource.getMessage(LABEL_PROXY_CHECK_2, null, locale);
                            } else {
                                proxyMessage = messageSource.getMessage(LABEL_PROXY_CHECK_3, null, locale);
                            }
                        } else if (xff != null) {
                            if (this.isIpAddressInRange(xff, fromIpAddress, toIpAddress)) {
                                proxyMessage = messageSource.getMessage(LABEL_PROXY_CHECK_4, null, locale);
                            } else {
                                proxyMessage = messageSource.getMessage(LABEL_PROXY_CHECK_5, null, locale);
                            }
                        } else if (clientIP != null) {
                            if (this.isIpAddressInRange(clientIP, fromIpAddress, toIpAddress)) {
                                proxyMessage = messageSource.getMessage(LABEL_PROXY_CHECK_6, null, locale);
                            } else {
                                proxyMessage = messageSource.getMessage(LABEL_PROXY_CHECK_7, null, locale);
                            }
                        } else {
                            if (!(this.isIpAddressInRange(ipCameFrom, fromIpAddress, toIpAddress))) {
                                proxyMessage = messageSource.getMessage(LABEL_PROXY_CHECK_8, null, locale);
                            }
                        }
                    }
                } else {

                    if (this.isIpAddressInRange(ipCameFrom, fromIpAddress, toIpAddress) && forwarded == null && xff == null
                            && clientIP == null) {
                        proxyMessage = messageSource.getMessage("label.proxyCheck.9", null, locale);
                    } else {

                        if (this.isIpAddressInRange(ipCameFrom, fromIpAddress, toIpAddress)) {
                            // scenario when Via header is missing
                            proxyMessage = messageSource.getMessage("label.proxyCheck.10", null, locale);
                        }

                        else if (forwarded != null) {
                            if (this.isIpAddressInRange(forwarded, fromIpAddress, toIpAddress)) {
                                proxyMessage = messageSource.getMessage(LABEL_PROXY_CHECK_2, null, locale);
                            } else {
                                proxyMessage = messageSource.getMessage(LABEL_PROXY_CHECK_3, null, locale);
                            }
                        } else if (xff != null) {
                            if (this.isIpAddressInRange(xff, fromIpAddress, toIpAddress)) {
                                proxyMessage = messageSource.getMessage(LABEL_PROXY_CHECK_4, null, locale);
                            } else {
                                proxyMessage = messageSource.getMessage(LABEL_PROXY_CHECK_5, null, locale);
                            }
                        } else if (clientIP != null) {
                            if (this.isIpAddressInRange(clientIP, fromIpAddress, toIpAddress)) {
                                proxyMessage = messageSource.getMessage(LABEL_PROXY_CHECK_6, null, locale);
                            } else {
                                proxyMessage = messageSource.getMessage(LABEL_PROXY_CHECK_7, null, locale);
                            }
                        } else {
                            if (!(this.isIpAddressInRange(ipCameFrom, fromIpAddress, toIpAddress))) {
                                proxyMessage = messageSource.getMessage("label.proxyCheck.11", null, locale);
                            }
                        }
                    }
                }

            }

            //Secured IP Address
            if (ipavailableInDatabase.contains("securedIp") && ( proxyMessage==null || (proxyMessage != null && !proxyMessage.contains("{success}")))) {

                if (via != null) {
                    if (isSecuredIpAddressInMaster(ipCameFrom)) {
                        proxyMessage = messageSource.getMessage(LABEL_PROXY_CHECK_13, null, locale);
                    } else {
                        if (forwarded != null) {
                            if (isSecuredIpAddressInMaster(forwarded)) {
                                proxyMessage = messageSource.getMessage(LABEL_PROXY_CHECK_14, null, locale);
                            } else {
                                proxyMessage = messageSource.getMessage(LABEL_PROXY_CHECK_15, null, locale);
                            }
                        } else if (xff != null) {
                            if (isSecuredIpAddressInMaster(xff)) {
                                proxyMessage = messageSource.getMessage(LABEL_PROXY_CHECK_16, null, locale);
                            } else {
                                proxyMessage = messageSource.getMessage(LABEL_PROXY_CHECK_17, null, locale);
                            }
                        } else if (clientIP != null) {
                            if (isSecuredIpAddressInMaster(clientIP)) {
                                proxyMessage = messageSource.getMessage(LABEL_PROXY_CHECK_18, null, locale);
                            } else {
                                proxyMessage = messageSource.getMessage(LABEL_PROXY_CHECK_19, null, locale);
                            }
                        } else {
                            if (!(isSecuredIpAddressInMaster(ipCameFrom))) {
                                proxyMessage = messageSource.getMessage(LABEL_PROXY_CHECK_20, null, locale);
                            }
                        }
                    }
                } else {

                    if (isSecuredIpAddressInMaster(ipCameFrom) && forwarded == null && xff == null && clientIP == null) {
                        proxyMessage = messageSource.getMessage(LABEL_PROXY_CHECK_21, null, locale);
                    } else {

                        if (isSecuredIpAddressInMaster(ipCameFrom)) {
                            // "Via" header is missing
                            proxyMessage = messageSource.getMessage(LABEL_PROXY_CHECK_22, null, locale);
                        }

                        else if (forwarded != null) {
                            if (isSecuredIpAddressInMaster(forwarded)) {
                                proxyMessage = messageSource.getMessage(LABEL_PROXY_CHECK_14, null, locale);
                            } else {
                                proxyMessage = messageSource.getMessage(LABEL_PROXY_CHECK_15, null, locale);
                            }
                        } else if (xff != null) {
                            if (isSecuredIpAddressInMaster(xff)) {
                                proxyMessage = messageSource.getMessage(LABEL_PROXY_CHECK_16, null, locale);
                            } else {
                                proxyMessage = messageSource.getMessage(LABEL_PROXY_CHECK_17, null, locale);
                            }
                        } else if (clientIP != null) {
                            if (isSecuredIpAddressInMaster(clientIP)) {
                                proxyMessage = messageSource.getMessage(LABEL_PROXY_CHECK_18, null, locale);
                            } else {
                                proxyMessage = messageSource.getMessage(LABEL_PROXY_CHECK_19, null, locale);
                            }
                        } else {
                            if (!(isSecuredIpAddressInMaster(ipCameFrom))) {
                                proxyMessage = messageSource.getMessage(LABEL_PROXY_CHECK_23, null, locale);
                            }
                        }
                    }
                }

            }



        } else {
            proxyMessage = messageSource.getMessage("label.proxyCheck.12", null, locale);
        }

        if(proxyMessage==null)
        {
        	return proxyMessage;
        }
        
        proxyMessage = proxyMessage.replace("{ipCameFrom}", ipCameFrom);
        proxyMessage = proxyMessage.replace("{success}", "success");
        proxyMessage = proxyMessage.replace("{error}", "error");
        proxyMessage = proxyMessage.replace("{warning}", "warning");
        if (ipAddress != null) {
            proxyMessage = proxyMessage.replace("{ipAddress}", ipAddress);
        }
        if (forwarded != null) {
            proxyMessage = proxyMessage.replace("{forwarded}", forwarded);
        }
        if (xff != null) {
            proxyMessage = proxyMessage.replace("{xff}", xff);
        }
        if (clientIP != null) {
            proxyMessage = proxyMessage.replace("{clientIP}", clientIP);
        }
        return proxyMessage;

    }

    @Override
    public User setUserPasswordExpirationDate(User user) {
        DateTime currentDate = DateUtils.getCurrentUTCTime();
        DateTime passwordExpirationDate = null;
        String expiringDays = user.getPasswordExpiresInDays();
        if (StringUtils.isNotBlank(expiringDays) && !expiringDays.equalsIgnoreCase(User.PSWD_NEVER_EXPIRES)) {
            int numberOfDays = Integer.parseInt(expiringDays);
            passwordExpirationDate = currentDate.plusDays(numberOfDays);
        }
        user.setPasswordExpirationDate(passwordExpirationDate);
        user.setLastPasswordResetDate(currentDate.toDateTime());
        return user;
    }

    @Override
    public UserBPMapping mapUserToBusinessPartner(Long bpId, User user) {

        NeutrinoValidator.notNull(bpId, "Business partner Id cannot be null");
        NeutrinoValidator.notNull(user, "User To be mapped to business Partner  cannot be null");

        UserBPMapping oldUserBPMapping = getBPMappedToUser(user.getId());
        if (oldUserBPMapping != null) {
            entityDao.delete(oldUserBPMapping);
        }

        UserBPMapping userBPMapping = new UserBPMapping();
        userBPMapping.setBusinessPartnerId(bpId);
        userBPMapping.setAssociatedUser(user);
        entityDao.persist(userBPMapping);
        return userBPMapping;
    }

    @Override
    public UserBPMapping getBPMappedToUser(Long userId) {
        NeutrinoValidator.notNull(userId, USER_ID_CANT_NULL);
        NamedQueryExecutor<UserBPMapping> userBPMappingExecutor = new NamedQueryExecutor<UserBPMapping>(
                "Users.getMappedBusinessPartners").addParameter(USER_ID, userId).addQueryHint(
                QueryHint.QUERY_HINT_CACHEABLE, Boolean.TRUE);
        List<UserBPMapping> ubpmList = userDao.executeQuery(userBPMappingExecutor);
        if (ubpmList != null && !ubpmList.isEmpty()) {
            return ubpmList.get(0);
        }
        return null;
    }

	
	@Override
	public List<Role> getRolesByRoleName(String roleName) {
		NeutrinoValidator.notNull(roleName, "roleName cannot be null");
		NamedQueryExecutor<Role> rolesExecutor = new NamedQueryExecutor<Role>("Roles.getRolesByRoleName")
				.addParameter("roleName", roleName);
		return entityDao.executeQuery(rolesExecutor);
	}

    @Override
    public List<User> getAllUsersInCurrentBranchExceptCurrent() {
        OrgBranchInfo branch = getCurrentUser().getLoggedInBranch();
        NamedQueryExecutor<User> userInCurrentBranch = new NamedQueryExecutor<User>(USERS_IN_CURRENT_BRANCH)
        		.addParameter(ORGANIZATION_BRANCH, branch.getId())
        		.addParameter("approvalStatusList", ApprovalStatus.APPROVED_RECORD_STATUS_IN_APPROVED_APPROVED_MODIFIED);
        List<User> userList = userDao.executeQuery(userInCurrentBranch);
        UserInfo user = getCurrentUser();
        if (userList.contains(user.getUserReference())) {
            userList.remove(user.getUserReference());
        }
        return userList;
    }

    @Override
    public List<User> getAllUsersInCurrentBranch() {
        OrgBranchInfo branch = getCurrentUser().getLoggedInBranch();
        NamedQueryExecutor<User> userInCurrentBranch = new NamedQueryExecutor<User>(USERS_IN_CURRENT_BRANCH)
        		.addParameter(ORGANIZATION_BRANCH, branch.getId())
        		.addParameter("approvalStatusList", ApprovalStatus.APPROVED_RECORD_STATUS_IN_APPROVED_APPROVED_MODIFIED);
        return userDao.executeQuery(userInCurrentBranch);
    }

    @Override
    public String getAllUsersUrisInCurrentBranch() {
        List<String> uris = new ArrayList<>();
        List<String> uris1 = null;
        OrgBranchInfo branch = getCurrentUser().getLoggedInBranch();
        NamedQueryExecutor<User> userInCurrentBranch = new NamedQueryExecutor<User>(USERS_IN_CURRENT_BRANCH)
        		.addParameter(ORGANIZATION_BRANCH, branch.getId())
        		.addParameter("approvalStatusList", ApprovalStatus.APPROVED_RECORD_STATUS_IN_APPROVED_APPROVED_MODIFIED);
        List<User> userList = userDao.executeQuery(userInCurrentBranch);
        for (User user : userList) {
            uris.add(user.getUri());
        }
        uris1 = new ArrayList<>(new LinkedHashSet<String>(uris));

        StringBuilder commaSepValueBuilder = new StringBuilder();
        for (int i = 0 ; i < uris1.size() ; i++) {
            commaSepValueBuilder.append("'");
            commaSepValueBuilder.append(uris1.get(i));
            commaSepValueBuilder.append("'");

            if (i != uris1.size() - 1) {
                commaSepValueBuilder.append(",");
            }

        }

        return commaSepValueBuilder.toString();
    }

    /*
     * (non-Javadoc) @see
     * com.nucleus.user.UserService#updateUserPassword(java.lang.String)
     */
    @Override
    public void updateUserWithNewPassword(User user) {
        if (user != null && user.getId() != null) {
            entityDao.update(user);
        }
    }

    @Override
    public String incrementFailedLoginCount(Long userId) {

        User user = baseMasterService.getMasterEntityById(User.class, userId);
        Map<String, ConfigurationVO> conf = configurationService.getFinalConfigurationForEntity(SystemEntity.getSystemEntityId());
        int failedLoginCount = Integer.parseInt(conf.get("config.user.allowed.failedLoginAttempts").getPropertyValue());

        user.setNumberOfFailedLoginAttempts(user.getNumberOfFailedLoginAttempts() + 1);
        entityDao.update(user);
        String uid = String.valueOf(userId);
        // 3 is at present hard coded . This value will be taken from the System
        // Parameter
        if (user.getNumberOfFailedLoginAttempts() >= failedLoginCount) {
            userSessionManagerService.invalidateUserSession(userManagementServiceCore.blockUser(uid, baseMasterService
                    .getMasterEntityById(User.class, getUserFromUsername("system").getId()).getEntityId()));

            markReasonForSystem(user, BlockReason.Failed_password_attempts);
            flushCurrentTransaction();
            return "blocked";
        }
        flushCurrentTransaction();
        return "incremented";
    }

    @Override
    public void resetFailedLoginCountToZero(Long userId) {
        User user = baseMasterService.getMasterEntityById(User.class, userId);
        user.setNumberOfFailedLoginAttempts(0);
        entityDao.update(user);
        
        flushCurrentTransaction();
    }

    @Override
    public String incrementFailedPassResetCount(Long userId) {

        User user = baseMasterService.getMasterEntityById(User.class, userId);
        Map<String, ConfigurationVO> conf = configurationService.getFinalConfigurationForEntity(SystemEntity.getSystemEntityId());
        int failedPassResetCount = Integer.parseInt(conf.get("config.user.allowed.failedPasswordResetAttempts")
                .getPropertyValue());

        user.setNumberOfFailedPassResetAttempts(user.getNumberOfFailedPassResetAttempts() + 1);
        entityDao.update(user);
        String uid = String.valueOf(userId);
        // 3 is at present hard coded . This value will be taken from the System
        // Parameter
        if (user.getNumberOfFailedPassResetAttempts() >= failedPassResetCount) {
            userSessionManagerService.invalidateUserSession(userManagementServiceCore.blockUser(uid, baseMasterService
                    .getMasterEntityById(User.class, getUserFromUsername("system").getId()).getEntityId()));

            markReasonForSystem(user, BlockReason.Failed_reset_password_attempts);

            return "blocked";
        }
        return "incremented";
    }

    @Override
    public void resetFailedPassResetCountToZero(Long userId) {
        User user = baseMasterService.getMasterEntityById(User.class, userId);
        user.setNumberOfFailedPassResetAttempts(0);
        entityDao.update(user);
    }

    @Override
    public UserMobilityInfo getUserMobilityInfo(Long userId) {
        
    	User user = entityDao.find(User.class, userId);
    	if(ValidatorUtils.notNull(user)){
    		UserMobilityInfo userMobilityInfo=	user.getUserMobileInfo();
    		if(userMobilityInfo!=null)
    		{
    		userMobilityInfo.getIsMobileEnabled();
    		}
    		return userMobilityInfo;
    	}
    	
    	return null;
     }

    @Override
    public PhoneNumber getUserMobileNumber(Long userId) {
        NeutrinoValidator.notNull(userId, USER_ID_CANT_NULL);
        
        UserProfile userProfile = getUserProfileByUserId(userId);
		if(ValidatorUtils.notNull(userProfile) && ValidatorUtils.notNull(userProfile.getSimpleContactInfo())){
			return userProfile.getSimpleContactInfo().getMobileNumber();
		}
		
		return null;
    }

    @Override
    public List<UserProfile> getAllUserProfile() {
        NamedQueryExecutor<UserProfile> userProfileList = new NamedQueryExecutor<>("Users.allUserProfiles");
        userProfileList.addParameter("userStatus", 0)
        .addParameter("approvalStatusList", ApprovalStatus.APPROVED_RECORD_STATUS_IN_APPROVED_APPROVED_MODIFIED);
        return entityDao.executeQuery(userProfileList);
    }

    @Override
	public String getUserFullNameForUserId(Long userId) {
		NeutrinoValidator.notNull(userId, USER_ID_CANT_NULL);
		UserProfile userProfile = getUserProfileByUserId(userId);
		if (ValidatorUtils.notNull(userProfile)) {
			return userProfile.getFullName();
		}
		return null;
	}

 
    @Override
    public List<User> getAuthenticatedUsersNameAndId(String authCode) {
      NamedQueryExecutor<User> users = new NamedQueryExecutor<User>("Users.userNameByAuthorityOnRoles")
    		  .addParameter(AUTH_CODE, authCode)
    		  .addParameter("activeFlag", true)
    		  .addParameter("approvalStatusList", ApprovalStatus.APPROVED_RECORD_STATUS_IN_APPROVED_APPROVED_MODIFIED);
      return entityDao.executeQuery(users);

    }

    @Override
    public List<Map<Long, String>> getAllUserProfileNameAndId() {
        NamedQueryExecutor<Map<Long, String>> excecutor = new NamedQueryExecutor<>(
                "UserProfile.getAllNameAndId");
        return entityDao.executeQuery(excecutor);

    }
    
    @Override
    public List<Map<Long, String>> getAllUserProfileNameAndIdForBinderList() {
        NamedQueryExecutor<Map<Long, String>> excecutor = new NamedQueryExecutor<>(
                "UserProfile.getAllNameAndIdForBinderList");
        return entityDao.executeQuery(excecutor);

    }

    @Override
    public DeviationLevel getDeviationLevel(Long userId) {
        NeutrinoValidator.notNull(userId, USER_ID_CANT_NULL);
        NamedQueryExecutor<DeviationLevel> executor = new NamedQueryExecutor<DeviationLevel>("user.getDeviationLevel")
                .addParameter(USER_ID, userId).addQueryHint(QueryHint.QUERY_HINT_CACHEABLE, Boolean.TRUE);
        return entityDao.executeQueryForSingleValue(executor);
    }

    @Override
    public List<UserSecurityQuestion> getUserSecurityQuestions(String username) {
        NeutrinoValidator.notNull(username, USER_NAME_NOT_NULL_ERR);
        NamedQueryExecutor<UserSecurityQuestion> userSecurityExecutor = new NamedQueryExecutor<UserSecurityQuestion>(
                "user.getUserSecurityQuestions").addParameter(USER_NAME, username);
        return entityDao.executeQuery(userSecurityExecutor);
    }

    @SuppressWarnings("rawtypes")
    @Override
    public Map<Long, String> getUserQuestionAnswerMap(String username) {
        NeutrinoValidator.notNull(username, USER_NAME_NOT_NULL_ERR);
        NamedQueryExecutor<Map> userSecurityExecutor = new NamedQueryExecutor<Map>("user.getUserSecurityQuestionAnswers")
                .addParameter(USER_NAME, username.toLowerCase())
                .addParameter("approvalStatusList", ApprovalStatus.APPROVED_RECORD_STATUS_IN_APPROVED_APPROVED_MODIFIED);
        List<Map> securityQuestionAnswersList = entityDao.executeQuery(userSecurityExecutor);
        Map<Long, String> securityQuestionAnswerMap = new HashMap<>();
        Long questionId = null;
        String answer = null;

        if (CollectionUtils.isNotEmpty(securityQuestionAnswersList)) {
            for (Map quesAnsMap : securityQuestionAnswersList) {
                questionId = (Long) quesAnsMap.get("quesId");
                answer = (String) quesAnsMap.get("answer");
                securityQuestionAnswerMap.put(questionId, answer);
            }
            return securityQuestionAnswerMap;
        }
        return null;
    }

    @Override
    public Money getUserSanctionLimitById(Long userId) {
        NeutrinoValidator.notNull(userId, USER_ID_CANT_NULL);
        NamedQueryExecutor<Money> executor = new NamedQueryExecutor<Money>("user.getSanctionLimit").addParameter(USER_ID,
                userId).addQueryHint(QueryHint.QUERY_HINT_CACHEABLE, Boolean.TRUE);
        return entityDao.executeQueryForSingleValue(executor);
    }

	public List<User> getAllActiveUsers() {
        NamedQueryExecutor<User> userIdExecutor = new NamedQueryExecutor<>("user.getAllActiveUsersId");
        userIdExecutor.addParameter("userStatus", UserStatus.STATUS_ACTIVE)
        .addParameter("approvalStatusList", ApprovalStatus.APPROVED_RECORD_STATUS_IN_APPROVED_APPROVED_MODIFIED);
        return entityDao.executeQuery(userIdExecutor);
    }

    @Override
    public void inactivateUsersBasedOnLastLoginTime() {
        invalidateOrBlockUser(UserStatus.STATUS_INACTIVE);
    }
    
    
    public void  dumpAllStackTraces ()
    {
        for (Map.Entry <Thread, StackTraceElement []> entry: 
            Thread.getAllStackTraces().entrySet ())
        {
            BaseLoggers.exceptionLogger.error(entry.getKey ().getName () + ":");
            for (StackTraceElement element: entry.getValue ())
            	BaseLoggers.exceptionLogger.error ("\t" + element);
        }
    }
    
    @Override
    public void blockUsersBasedOnLastLoginTime() {
		invalidateOrBlockUser(UserStatus.STATUS_LOCKED);
	}
    
	private void invalidateOrBlockUser(int changeUserStatusTo) {
		BaseLoggers.exceptionLogger.error("Started taking stacktrace for invalidateOrBlockUser--->"+changeUserStatusTo);
		dumpAllStackTraces();
		BaseLoggers.exceptionLogger.error("Ended taking stacktrace for invalidateOrBlockUser");
		
        List<User> activeUsersList = getAllActiveAndBlockedUsers();
        int count = 0;
        if (CollectionUtils.isNotEmpty(activeUsersList)) {
            // configuration property for number of days after which user should
            // be blocked/inactivated through scheduler

            for (User user : activeUsersList) {
                Event event = eventService.getLastSuccessLoginEventByAssociatedUseruri(user.getUri());
                Event logoutEvent = eventService.getLastLogoutEventByAssociatedUseruri(user.getUri());

                UserSecurityTrailEvent lastLoginInfo = null;
                UserSecurityTrailEvent lastLogoutInfo = null;
                if (event != null || logoutEvent!=null) {
                    ConfigurationVO configVo = configurationService.getConfigurationPropertyFor(SystemEntity.getSystemEntityId(),
                            "config.autoInactivateUser.days");
                    if (configVo != null) {
                        int daysToBlockAfter = Integer.parseInt(configVo.getPropertyValue());
                        lastLoginInfo = (UserSecurityTrailEvent) event;
                        DateTime lastLoginTime = null;

                        lastLoginTime = lastLoginInfo!=null?lastLoginInfo.getEventTimestamp():null;

                        if(lastLoginTime==null) {
                            lastLogoutInfo = (UserSecurityTrailEvent) logoutEvent;
                            lastLoginTime = logoutEvent!=null?lastLogoutInfo.getEventTimestamp():null;
                        }
                        if (lastLoginTime != null) {
                            // The difference between dates is taken from
                            // current time
                            int daysSinceLastLogin = Days.daysBetween(lastLoginTime, DateUtils.getCurrentUTCTime())
                                    .getDays();
                            // int
                            if (daysSinceLastLogin >= daysToBlockAfter) {
                                if (UserStatus.STATUS_INACTIVE == changeUserStatusTo) {
                                    invalidateUser(user);
                                } else {
                                    boolean doBlock = false;
                                    if (UserStatus.STATUS_ACTIVE == user.getUserStatus()) {
                                        doBlock = true;
                                    } else {
                                        BlockReason blockReason = genericParameterService.findByCode(BlockReason.Dormancy, BlockReason.class);
                                        Boolean isHighPriority = checkPriorityOfBlockReason(user.getId(), null, blockReason);
                                        if (isHighPriority) {
                                            doBlock = true;
                                        }
                                    }
                                    if (doBlock) {
                                        lockUser(user, getCurrentUser().getUserEntityId());
                                        markReasonForSystem(user, BlockReason.Dormancy);
                                    } else {
                                        count--;
                                    }
                                }
                                count++;
                            }
                        }
                    }
                }else if(event==null && logoutEvent==null){
                    /*
                     * if user successful login event is not found then user
                     * creation date is checked and if number of days since
                     * user creation exceeds daysToBlockAfter user is
                     * blocked/inactivated
                     */
                    ConfigurationVO configVo = configurationService.getConfigurationPropertyFor(SystemEntity.getSystemEntityId(),
                            "config.initialDormancy.days");
                    if (configVo != null) {
                        int daysToBlockAfter = Integer.parseInt(configVo.getPropertyValue());
                        if (user.getEntityLifeCycleData() != null
                                && user.getEntityLifeCycleData().getCreationTimeStamp() != null) {
                            DateTime userCreatedTime = user.getEntityLifeCycleData().getCreationTimeStamp();
                            int daysSinceUserCreated = Days.daysBetween(userCreatedTime, DateUtils.getCurrentUTCTime())
                                    .getDays();
                            if (daysSinceUserCreated >= daysToBlockAfter) {
                                if (UserStatus.STATUS_INACTIVE == changeUserStatusTo) {
                                    invalidateUser(user);
                                } else {
                                    boolean doBlock = false;
                                    if (UserStatus.STATUS_ACTIVE == user.getUserStatus()) {
                                        doBlock = true;
                                    } else {
                                        BlockReason blockReason = genericParameterService.findByCode(BlockReason.Initial_Dormancy, BlockReason.class);
                                        Boolean isHighPriority = checkPriorityOfBlockReason(user.getId(), null, blockReason);
                                        if (isHighPriority) {
                                            doBlock = true;
                                        }
                                    }
                                    if (doBlock) {
                                        lockUser(user, getCurrentUser().getUserEntityId());
                                        markReasonForSystem(user, BlockReason.Initial_Dormancy);
                                    } else {
                                        count--;
                                    }
                                }
                                count++;
                            }
                        }
                    }
                }
            }
            BaseLoggers.flowLogger.info("Number of users blocked/inactivated: {}", count);

        }
    }

    @Override
    public List<String> fetchAccessBranchesToCurrentUser(Long userId) {
        NeutrinoValidator.notNull(userId, "User Id cannot be null");
        List<Integer> statusList = new ArrayList<>();
        statusList.add(ApprovalStatus.APPROVED);
        statusList.add(ApprovalStatus.APPROVED_MODIFIED);
        statusList.add(ApprovalStatus.APPROVED_DELETED_IN_PROGRESS);
        List<String> branchNameList = new ArrayList<>();
        NamedQueryExecutor<OrganizationBranch> executor = new NamedQueryExecutor<OrganizationBranch>("User.getAccessToAllBranchesForUser")
                .addParameter(USER_ID, userId).addParameter(STATUS_LIST, statusList)
                .addQueryHint(QueryHint.QUERY_HINT_CACHEABLE, Boolean.TRUE);
        Set<OrganizationBranch> branchesAccessToCurrentUserList = new HashSet<>(entityDao.executeQuery(executor));
        for(OrganizationBranch org : branchesAccessToCurrentUserList){
            branchNameList.add(org.getName()+" ("+org.getBranchCode()+")");
        }
        return branchNameList;
    }

    @Override
	public Boolean getForceResetPassForUserId(Long userId) {
		NeutrinoValidator.notNull(userId, USER_NAME_NOT_NULL_ERR);
		NeutrinoValidator.notNull(userId, USER_ID_CANT_NULL);

		User user = entityDao.find(User.class,userId);
		if (ValidatorUtils.notNull(user)) {
			return user.isForcePasswordResetOnLogin();
		}

		return false;
	}

    @Override
	public Long getUserIdByUserName(String userName) {
		NeutrinoValidator.notNull(userName, USER_NAME_NOT_NULL_ERR);
		List<User> userList = getUserReferenceByUsername(userName);
		if (ValidatorUtils.notNull(userList)) {
			for (User user : userList) {
				if ((user.getUserStatus() == 0)
						&& (ValidatorUtils.isNull(user.getEntityLifeCycleData().getSnapshotRecord())
								|| user.getEntityLifeCycleData().getSnapshotRecord() == Boolean.FALSE)) {
					return user.getId();
				}
			}
		}

		return null;
	}

    @Override
    public String getUserUriByUserName(String userName) {
        NeutrinoValidator.notNull(userName, USER_NAME_NOT_NULL_ERR);
        Long entityId = getUserIdByUserName(userName);
        return User.class.getName() + ":" + entityId;
    }

   
	@Override
	public List<User> getAllSuperAdmin() {        
    	NamedQueryExecutor<User> executor = new NamedQueryExecutor<>("User.getAllSuperAdmin");
    	executor.addParameter("isSuperAdmin",true)
    	.addParameter("userStatus",UserStatus.STATUS_ACTIVE)
    	.addParameter("approvalStatusList", ApprovalStatus.APPROVED_RECORD_STATUS_IN_APPROVED_APPROVED_MODIFIED);
    	return entityDao.executeQuery(executor);
    }


	@Override
    public boolean isUserSuperAdmin(Long userId) {
        NeutrinoValidator.notNull(userId, "User Id Cannot be null");
        
        User user =entityDao.find(User.class, userId);
        if(ValidatorUtils.notNull(user) && user.getUserStatus()==0){
        		return user.isSuperAdmin();
        }
		return false;        
    }

	@Override
	public List<User> getUserByRole(Long roleId, String authCode) {
		NeutrinoValidator.notNull(roleId, ROLE_NOT_NULL_ERROR);
		NeutrinoValidator.notNull(authCode, "authCode Cannot be null");
		
		NamedQueryExecutor<User> users = new NamedQueryExecutor<User>("Users.userNameByAuthorityOnRolesAndLevel")
				.addParameter("role", roleId).addParameter(AUTH_CODE, authCode)
				.addParameter("activeFlag", true)
				.addParameter("approvalStatusList", ApprovalStatus.APPROVED_RECORD_STATUS_IN_APPROVED_APPROVED_MODIFIED);
		return entityDao.executeQuery(users);

	}

    @Override
    public List<Long> getIsBranchAccessibleForUser(Long userId, Long branchId) {

        List<Integer> statusList = new ArrayList<>();
        statusList.add(ApprovalStatus.APPROVED);
        statusList.add(ApprovalStatus.APPROVED_MODIFIED);
        statusList.add(ApprovalStatus.APPROVED_DELETED_IN_PROGRESS);
        NamedQueryExecutor<Long> userOrgBranchExecutor = new NamedQueryExecutor<Long>("User.getIsBranchAccessibleForUser")
                .addParameter(USER_ID, userId).addParameter("branchId", branchId).addParameter(STATUS_LIST, statusList);
        return userDao.executeQuery(userOrgBranchExecutor);
    }

    @Override
    public String getUserNameFromDisplayName(String displayName) {
        NeutrinoValidator.notNull(displayName, "Display Name cannot be null");
        NamedQueryExecutor<String> userName = new NamedQueryExecutor<String>("Users.getUserFromDisplayName").addParameter(
                "displayName", displayName);
        return entityDao.executeQueryForSingleValue(userName);
    }

    @Override
    public void saveUserAuditLog(UserAuditLog userAuditLog) {
        entityDao.persist(userAuditLog);
    }

	@Override
	public List<String> getRoleNamesFromUserId(Long userId) {        
		NeutrinoValidator.notNull(userId, "userId cannot be null");
		NamedQueryExecutor<String> rolesExecutor = new NamedQueryExecutor<String>("Users.getRoleNameFromUser")
				.addParameter("userId", userId)
				.addParameter("userStatus", Arrays.asList(UserStatus.STATUS_ACTIVE,UserStatus.STATUS_LOCKED))
				.addParameter("activeFlag", true)
				.addQueryHint(QueryHint.QUERY_HINT_CACHEABLE, Boolean.TRUE);
		return userDao.executeQuery(rolesExecutor);
	}

    @Override
    public List<String> getBranchCodeFromUserId(Long userId) {
        NeutrinoValidator.notNull(userId, USER_ID_CANT_NULL);
        NamedQueryExecutor<String> branchExecutor = new NamedQueryExecutor<String>("Organization.getOrgBranchesCodeOfUser")
                .addParameter(USER_ID, userId)
                .addParameter("activeFlag",true)
                .addParameter("approvalStatus", ApprovalStatus.APPROVED)
                .addQueryHint(QueryHint.QUERY_HINT_CACHEABLE, Boolean.TRUE);
        return userDao.executeQuery(branchExecutor);
    }


    @Override
    public String getDefaultBranchCodeFromUserId(Long userId) {
        NeutrinoValidator.notNull(userId, "User Id Cannot be null");
        String defaultBranchId = null;
        NamedQueryExecutor<String> executor = new NamedQueryExecutor<String>("User.primaryOrganizationBranchCodebyUserId")
                .addParameter(USER_ID, userId);
        List<String> defaultBranchList = entityDao.executeQuery(executor);
        if (CollectionUtils.isNotEmpty(defaultBranchList)) {
            defaultBranchId = defaultBranchList.get(0);
        }
        return defaultBranchId;
    }

    @Override
    public List<String> getTeamNameFromUserId(Long userId) {
        NeutrinoValidator.notNull(userId, USER_ID_CANT_NULL);
        NamedQueryExecutor<String> teamExecutor = new NamedQueryExecutor<String>("Team.getTeamNameByUserId").addParameter(
                USER_ID, userId).addQueryHint(QueryHint.QUERY_HINT_CACHEABLE, Boolean.TRUE);
        return userDao.executeQuery(teamExecutor);
    }

    @Override
    public List<String> getBranchCodeWhereUserIsBranchAdmin(Long userId) {
        NeutrinoValidator.notNull(userId, USER_ID_CANT_NULL);
        NamedQueryExecutor<String> branchAdminExecutor = new NamedQueryExecutor<String>("Organization.getOrgBranchesCodeWhereUserIsBranchAdmin")
        		.addParameter(USER_ID, userId)
        		.addParameter("isBranchAdmin", true)
        		.addParameter("approvalStatus", ApprovalStatus.APPROVED)
        		.addParameter("activeFlag",Boolean.TRUE)
        		.addQueryHint(QueryHint.QUERY_HINT_CACHEABLE, Boolean.TRUE);
        return userDao.executeQuery(branchAdminExecutor);
    }

    @Override
    public List<UnapprovedEntityData> fetchAuditLogOfUserByUserUUID(String uuid) {
        NeutrinoValidator.notNull(uuid, USER_ID_CANT_NULL);
        NamedQueryExecutor<UnapprovedEntityData> executor = new NamedQueryExecutor<UnapprovedEntityData>("User.getUserAuditLogOfUser")
                .addParameter("uuid", uuid).addQueryHint(QueryHint.QUERY_HINT_CACHEABLE, Boolean.TRUE);
        return userDao.executeQuery(executor);
    }


    @Override
    public Integer getLatestVersionOfAuditForUser(Long userId) {
        NeutrinoValidator.notNull(userId, "User Id cannot be null");
        NamedQueryExecutor<Integer> executor = new NamedQueryExecutor<Integer>("User.getLatestVersionOfUserAuditLog")
                .addParameter(USER_ID, userId).addQueryHint(QueryHint.QUERY_HINT_CACHEABLE, Boolean.TRUE);
        return userDao.executeQueryForSingleValue(executor);
    }

    @Override
	public User getUserByUri(String userUri) {
		NeutrinoValidator.notNull(userUri, "userUri cannot be null");

		if (StringUtils.isBlank(userUri)) {
			return null;
		}
		return entityDao.find(User.class,(EntityId.fromUri(userUri).getLocalId()));

	}
    
    @Override
	public List<UserSecurityQuestionAnswer> getUserSecurityQuestionAnswer(String username) {
        NeutrinoValidator.notNull(username, USER_NAME_NOT_NULL_ERR);
        
        NamedQueryExecutor<UserSecurityQuestionAnswer> userSecurityQAExecutor = new NamedQueryExecutor<UserSecurityQuestionAnswer>(
                "user.getUserSecurityQuestionAnswersList").addParameter(USER_NAME, username.toLowerCase());
        return entityDao.executeQuery(userSecurityQAExecutor);
    }

	@Override
	public void updateUserSecurityQuestionAnswer(String username, List<UserSecurityQuestionAnswer> quesAnsList) {
        User user = findUserByUsername(username);
        if (user != null && CollectionUtils.isNotEmpty(quesAnsList)) {
            for (UserSecurityQuestionAnswer usqa : quesAnsList) {
                if (usqa.getId() == null) {
                    entityDao.persist(usqa);
                    user.getSecurityQuestionAnswers().add(usqa);
                } else {
                    int modIndex = user.getSecurityQuestionAnswers().indexOf(usqa);
                    user.getSecurityQuestionAnswers().set(modIndex, entityDao.saveOrUpdate(usqa));
                }
            }
          }
    }

	@Override
	public String getSourceSystemForUserId(Long userId) {
		NeutrinoValidator.notNull(userId, USER_ID_CANT_NULL);
		User user = entityDao.find(User.class, userId);
		if (ValidatorUtils.notNull(user)) {
			return user.getSourceSystem();
		}
		return null;
	}

	/**
	 *  Query-->   Select u.sourceSystem from User u where u.username = :userName and (u.masterLifeCycleData.approvalStatus
	 *   in (0,3))
	 */
	@Override
	public String getSourceSystemForUserName(String userName) {
    	NeutrinoValidator.notNull(userName, USER_NAME_NOT_NULL_ERR);
    	List<User> userList = getUserReferenceByUsername(userName,Boolean.FALSE);
    	if(ValidatorUtils.notNull(userList)){
    		for(User user : userList){
    			if(user.getApprovalStatus() == ApprovalStatus.APPROVED || user.getApprovalStatus() == ApprovalStatus.APPROVED_MODIFIED){
    				return user.getSourceSystem();
    			}
    		}
    	}
        return null;
	}
	
	@Override
	public List<Map<String, Object>> getUserIdAndApprovalStatusByUUID(String uuid) {
        NamedQueryExecutor<Map<String, Object>> userExecutor = new NamedQueryExecutor<Map<String, Object>>("Users.UserIdAndApprovalStatusByUUid")
        		.addParameter("uuid", uuid)
        		.addParameter("userStatus", UserStatus.STATUS_ACTIVE);
        return  entityDao.executeQuery(userExecutor);
    }
 
	
	/**
	 * Query -->
	 * select role.id from Role role inner join role.users users inner join
	 * role.authorities auth where auth.authCode = :authCode AND users.id=
	 * :userId
	 */
	@Override
	public Boolean userHasAuthority(String authCode, Long userId) {
		NeutrinoValidator.notNull(authCode, "authCode cannot be null");
		NeutrinoValidator.notNull(userId, USER_ID_CANT_NULL);
		Boolean hasAuthority = Boolean.FALSE;
		NamedQueryExecutor<Long> userExecutor = new NamedQueryExecutor<Long>("User.hasAuthorityByAuthCode")
				.addParameter(USER_ID, userId).addParameter(AUTH_CODE, authCode).addQueryHint(QueryHint.QUERY_HINT_CACHEABLE, Boolean.TRUE);
		List<Long> roleIds = entityDao.executeQuery(userExecutor);
		if (CollectionUtils.isNotEmpty(roleIds)) {
			hasAuthority = Boolean.TRUE;
		}
		return hasAuthority;
	}

	@Override
	public String getUserSourceSystemByUsername(String username) {
		NeutrinoValidator.notNull(username, USER_NAME_NOT_NULL_ERR);
		User user = findUserByUsername(username);
		if (ValidatorUtils.notNull(user)) {
			return user.getSourceSystem();
		}
		return null;    
	}
	
	@Override
	public Integer getUsersCountByProductName(String productName,String uUID) {
        // To get id of product from GenericParameter so that cartesian join with GenericParameter can be avoided
        SourceProduct sp = genericParameterService.findByCode(productName, SourceProduct.class);
        NamedQueryExecutor<Long> userExecutor = new NamedQueryExecutor<Long>("Users.usersCountByProductId").addParameter(
                "sourceProductId", sp.getId()).addParameter("uuid", uUID)
        		.addParameter("userStatus", UserStatus.ACTIVE_AND_LOCKED)
        		.addParameter("approvalStatusList", ApprovalStatus.APPROVED_RECORD_STATUS_IN_APPROVED_APPROVED_MODIFIED)
        		.addParameter("isLoginEnabled",Boolean.TRUE)
        		.addParameter("activeFlag", true)
        		.addQueryHint(QueryHint.QUERY_HINT_CACHEABLE, true);
      
        	 Long count=userDao.executeQueryForSingleValue(userExecutor);
        return count.intValue();  
	}


	@Override
    public List<String> getPasswordExpireInDays() {

        return passwordExpireInDaysNewList;
    }

	@Override
	public List<User> getAllUserExceptCurrent() {
        UserInfo user = getCurrentUser();

        List<User> userList = getAllUser();
        if (userList.contains(user.getUserReference())) {
            userList.remove(user.getUserReference());
        }
        return userList;
	}
	@Override
	public List<String>   getProductListFromRoleIds(List<Long> roleIds)
    {
    	 NamedQueryExecutor<String> userExecutor = new NamedQueryExecutor<String>("Users.systemNamesByRolesList").addParameter(
                 "roleIds", roleIds).addQueryHint(QueryHint.QUERY_HINT_CACHEABLE, true);
         return userDao.executeQuery(userExecutor); 
    	 
    	
    }

	@Override
	public String showUnblockLink(String username) {
		
		if(username == null){
			return "NO";
		}
		
		UserInfo userInfo = this.getUserFromUsername(username);

        if(userInfo!=null){

            ReasonVO reasonVO = userManagementServiceCore.getReasonByUserId(userInfo.getId());
            if(reasonVO!=null){

                BlockReason blockReason = genericParameterService.findByCode(reasonVO.getCode(),BlockReason.class);

                if (blockReason != null && blockReason.getParentCode().equalsIgnoreCase("UNBLOCK_NO")) {
                  return "NO";
                }
            }

        }
		
		return "YES";
	}
	
	

	@Override
	public BlockReason getUserBlockReasonByUsername(String username) {

		if (username == null || username.isEmpty()) {
			return null;
		}

		UserInfo userInfo = this.getUserFromUsername(username);

		if (userInfo != null) {
			ReasonVO reasonVO = userManagementServiceCore.getBlockInactiveReasonByUserId(userInfo.getId());
			if (reasonVO != null) {
				return genericParameterService.findByCode(reasonVO.getCode(), BlockReason.class);
			}
		}

		return null;
	}

	@Override
    public List<IpAddress> getIPAddress(String ipAddress,AccessType accessType) {

        List<IpAddress> ipAddresses=null;
        List<String> accessTypeCodes=getAccessTypes(accessType);

        NamedQueryExecutor<IpAddress> executor = new NamedQueryExecutor<IpAddress>("Users.findIpAddressInMaster")
                .addParameter("accessType", accessTypeCodes)
                .addParameter("approvalStatus",ApprovalStatus.APPROVED_RECORD_STATUS_LIST);
        ipAddresses = entityDao.executeQuery(executor);

        return ipAddresses;
    }

    private List<String> getAccessTypes(AccessType accessType){
        List<String> accessTypesToBeSearched = new ArrayList<>();
        if (accessType.getCode().equals(BOTH)) {
            accessTypesToBeSearched.add(INTERNET);
            accessTypesToBeSearched.add(INTRANET);
        }else if(accessType.getCode().equals(INTERNET) || accessType.getCode().equals(INTRANET)){
            accessTypesToBeSearched.add(accessType.getCode());
        }
        return accessTypesToBeSearched;
    }

    @Override
	public Boolean getUserLoginTimeValid(UserInfo userInfo){
		Boolean toReturn = customLoginTimeValidator(userInfo, new DateTime());
		
		return toReturn;
	}
	
	@Override
	public Boolean customLoginTimeValidator(UserInfo user, DateTime currentTime) {
		UserCalendar userCalendar = user.getUserReference().getUserCalendar();
		// CAS-40443 -- null check handling for userCalendar is done.
		DailySchedule dailySchedule = null;
		if (userCalendar != null)
			dailySchedule = userCalendar.getSchedule(currentTime.getDayOfWeek());



		Boolean workingDay = null;
		if (dailySchedule != null) {
			workingDay = dailySchedule.isWorkingDay();
		} else {
			workingDay = Boolean.FALSE;
		}
		BaseLoggers.flowLogger.info("workingDay = " + workingDay);
		if (!workingDay) {
			BaseLoggers.flowLogger.info("User is not authorized to login today");
			return false;
		}
		BaseLoggers.flowLogger.info("currentTime = " + currentTime.getMillisOfDay());
		if (dailySchedule.getOpeningTime() != null) {
			BaseLoggers.flowLogger.info("getOpeningTime = " + dailySchedule.getOpeningTime().getMillisOfDay());
		}
		if (dailySchedule.getClosingTime() != null) {
			BaseLoggers.flowLogger.info("getClosingTime = " + dailySchedule.getClosingTime().getMillisOfDay());
		}
		if ((dailySchedule.getOpeningTime() != null
				&& currentTime.getMillisOfDay() < dailySchedule.getOpeningTime().getMillisOfDay())
				|| (dailySchedule.getClosingTime() != null
						&& currentTime.getMillisOfDay() > dailySchedule.getClosingTime().getMillisOfDay())) {
			return false;
		} 
		return true;
		
	}

    @Override
    public MenuEntity getMappedDefaultMenu(Long userId, Long sourceProductId){
        NeutrinoValidator.notNull(userId,USER_ID_CANT_NULL);
        if(sourceProductId==null){
            return null;
        }
        NamedQueryExecutor<MenuEntity> executor= new NamedQueryExecutor<MenuEntity>("User.getMappedMenuForUserAndProduct").addParameter("userId",userId).addParameter("sourceProductId",sourceProductId);
        List<MenuEntity> mappedUrlList=userDao.executeQuery(executor);
        if(CollectionUtils.isEmpty(mappedUrlList)){
            return null;
        }else{
            if(mappedUrlList.size()>1){
                BaseLoggers.exceptionLogger.error("Multiple default urls mapped to userId: {}",userId);
            }
            return mappedUrlList.get(0);
        }
    }

    public String returnLandingUrlFromMenu(MenuEntity menuEntity, HttpServletRequest request){
        if(menuEntity==null){
            return "";
        }
        String linkedUrl="";
        if(StringUtils.isEmpty(linkedUrl) && StringUtils.isNotEmpty(menuEntity.getUrl())){
            linkedUrl=menuEntity.getUrl().substring(menuEntity.getUrl().indexOf("/"));
        }else if(StringUtils.isEmpty(linkedUrl) && StringUtils.isNotEmpty(menuEntity.getLinkedFunction())) {
            linkedUrl="/app/UserInfo/userMappedDefaultRedirect";
            request.setAttribute("linkedTargetFunction",menuEntity.getLinkedFunction());
        }
        return linkedUrl;
    }

	@Override
	public boolean isBotUser(String userName) {
		NamedQueryExecutor<Boolean> userExecutor = new NamedQueryExecutor<Boolean>("User.isBotUserByUserName")
				.addParameter(USER_NAME, userName.toLowerCase())
        		.addParameter("approvalStatus", ApprovalStatus.APPROVED_RECORD_STATUS_LIST);
		Boolean result = userDao.executeQueryForSingleValue(userExecutor);
		if(result!=null && result){
			return true;
		}		
		return false;
	}
	
	@Override
	public boolean isUserValidForPasswordReset(User user) {
		
		return user!=null && user.getUserStatus()==UserStatus.STATUS_ACTIVE;
	}

    @Override
    public List<BlockReason> getHighPriorityBlockReasonExist(Long userId, String parentCodeFilter) {
        List<BlockReason> highPriorityBlockReasonList = null;
        List<BlockReason> blockReasonList = genericParameterService.retrieveTypes(BlockReason.class);

        BlockReason currentBlockReason = getCurrentBlockReason(userId);
        if (null != currentBlockReason && null != blockReasonList) {
            if (StringUtils.isNotEmpty(parentCodeFilter)) {
                highPriorityBlockReasonList = blockReasonList.stream().filter(b -> (b.getLevelInHierarchy() > currentBlockReason.getLevelInHierarchy()) && (parentCodeFilter.equalsIgnoreCase(b.getParentCode())) && !currentBlockReason.getCode().equalsIgnoreCase(b.getCode())).collect(Collectors.toList());
            } else {
                highPriorityBlockReasonList = blockReasonList.stream().filter(b -> (b.getLevelInHierarchy() > currentBlockReason.getLevelInHierarchy() && !currentBlockReason.getCode().equalsIgnoreCase(b.getCode()))).collect(Collectors.toList());
            }
        }else {
            if (StringUtils.isNotEmpty(parentCodeFilter)) {
                return blockReasonList.stream().filter(b -> (parentCodeFilter.equalsIgnoreCase(b.getParentCode()))).collect(Collectors.toList());
            }else {
                return blockReasonList;
            }
        }
        return highPriorityBlockReasonList;
    }

    @Override
    public BlockReason getCurrentBlockReason(Long userId) {
        NeutrinoValidator.notNull(userId, "User Id cannot be null");
        NamedQueryExecutor<BlockReason> executor = new NamedQueryExecutor<BlockReason>("User.getCurrentBlockReason")
                .addParameter(USER_ID, userId).addQueryHint(QueryHint.QUERY_HINT_CACHEABLE, Boolean.TRUE);
        return userDao.executeQueryForSingleValue(executor);
    }

    @Override
    public boolean checkPriorityOfBlockReason(Long userId, String parentCodeFilter, BlockReason blockReason) {
        BlockReason existingBlockReason = getCurrentBlockReason(userId);
        if (existingBlockReason != null && blockReason != null){
            if (blockReason.getLevelInHierarchy()>existingBlockReason.getLevelInHierarchy()){
                return true;
            }
        }
        return false;
    }

    public List<User> getAllActiveAndBlockedUsers() {
        NamedQueryExecutor<User> userIdExecutor = new NamedQueryExecutor<>("user.getAllActiveAndBlockedUsers");
        userIdExecutor.addParameter("userStatusList", UserStatus.ACTIVE_AND_LOCKED)
                .addParameter("approvalStatusList", ApprovalStatus.APPROVED_RECORD_STATUS_IN_APPROVED_APPROVED_MODIFIED);
        return entityDao.executeQuery(userIdExecutor);
    }

    @Override
    public int getUserStatusByUserId(Long userId) {
        NamedQueryExecutor<Integer> executor = new NamedQueryExecutor<Integer>("User.getUserStatusByUserId")
                .addParameter("userId", userId);
        return entityDao.executeQueryForSingleValue(executor);
    }

    @Override
    public List<Long> getUserStatusCountByUserId(List<Long> userIdList) {
        if (CollectionUtils.isNotEmpty(userIdList)) {
            NamedQueryExecutor<Long> executor = new NamedQueryExecutor<Long>("User.getUserStatusCountByUserId")
                    .addParameter("userIdList", userIdList).addParameter(
                            "userStatus", UserStatus.STATUS_LOCKED);
            return entityDao.executeQuery(executor);
        }else {
            return null;
        }
    }

}