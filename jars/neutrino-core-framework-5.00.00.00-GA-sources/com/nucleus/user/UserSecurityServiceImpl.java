package com.nucleus.user;

import static com.nucleus.finnone.pro.general.util.ValidatorUtils.hasElements;
import static com.nucleus.finnone.pro.general.util.ValidatorUtils.notNull;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.collections4.CollectionUtils;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;

import com.nucleus.authority.Authority;
import com.nucleus.cas.delegation.service.DelegationLoginService;
import com.nucleus.config.persisted.service.ConfigurationService;
import com.nucleus.config.persisted.vo.ConfigurationVO;
import com.nucleus.core.NeutrinoSpringAppContextUtil;
import com.nucleus.core.initialization.ProductInformationLoader;
import com.nucleus.core.misc.util.DateUtils;
import com.nucleus.core.mutitenancy.service.MultiTenantService;
import com.nucleus.core.role.entity.Role;
import com.nucleus.core.validation.util.NeutrinoValidator;
import com.nucleus.dao.query.NamedQueryExecutor;
import com.nucleus.entity.ApprovalStatus;
import com.nucleus.entity.SystemEntity;
import com.nucleus.event.Event;
import com.nucleus.event.EventService;
import com.nucleus.event.UserSecurityTrailEvent;
import com.nucleus.finnone.pro.general.util.ValidatorUtils;
import com.nucleus.logging.BaseLoggers;
import com.nucleus.query.constants.QueryHint;
import com.nucleus.security.core.session.NeutrinoSessionRegistry;
import com.nucleus.service.BaseServiceImpl;
import com.nucleus.web.ldap.LdapService;

import net.bull.javamelody.MonitoredWithSpring;

@Named(value = "userSecurityService")
public class UserSecurityServiceImpl extends BaseServiceImpl implements UserSecurityService {

    @Autowired
    private UserDao              userDao;

    @Inject
    @Named("sessionRegistry")
    private NeutrinoSessionRegistry sessionRegistry;
    
    @Inject
    @Named("configurationService")
    private ConfigurationService configurationService;

    @Inject
    @Named("ldapService")
    private LdapService ldapService;
   
    @Inject
    @Named("eventService")
    private EventService         eventService;

    @Inject
    @Named("userService")
    private UserService          userService;

    @Inject
    @Named("forbiddenAuthorityProvider")
    private IForbiddenAuthorityProvider forbiddenAuthorityProvider;
    
    @Inject
	@Named("multiTenantService")
	private MultiTenantService multiTenantService;

    @Value(value = "#{'${core.web.config.ldap.default.role}'}")
    private String               defaultLdapRole;
    @Value(value = "#{'${core.web.config.ldap.default.role.enable}'}")
    private Boolean              defaultLdapRoleEnable;
    @Value(value = "#{'${core.web.config.create.update.user.enable}'}")
    private boolean createUpdateUserEnable;

    @Override
    @MonitoredWithSpring(name = "USSI_FETCH_USR_FROM_USRNAME")
    @Transactional(readOnly=true)
    public UserInfo getCompleteUserFromUsername(String username) {
        UserInfo userInfo=null;
        NamedQueryExecutor<User> userExecutor = new NamedQueryExecutor<User>("Users.userByUsername")
        		.addParameter("username", username.toLowerCase())
        		.addParameter("userStatus", UserStatus.ALL_STATUSES_EXCLUDING_DELETED)
        		.addParameter("approvalStatusList", ApprovalStatus.APPROVED_RECORD_STATUS_IN_APPROVED_APPROVED_MODIFIED);
        User userObj = userDao.executeQueryForSingleValue(userExecutor);
        if (notNull(userObj)) {
            userInfo = createUserInfo(userObj);         
            Event event = eventService.getLastSuccessLoginEventByAssociatedUseruri(userInfo.getUserEntityId().getUri());
            UserSecurityTrailEvent lastLoginInfo = null;
            if (event != null) {
                lastLoginInfo = (UserSecurityTrailEvent) event;
            }
            if (lastLoginInfo != null) {

                DateTime cal = lastLoginInfo.getEventTimestamp();
                String pattern = "dd/MM/yyyy";
                if (userInfo != null && userInfo.getUserPreferences() != null) {
                    if(userInfo.getUserPreferences().containsKey("config.date.formats")){
                        ConfigurationVO confVO = userInfo.getUserPreferences().get("config.date.formats");
                        if (confVO != null && !confVO.getText().isEmpty()) {
                            pattern = confVO.getText();
                        }
                    }
                }
                DateTimeFormatter timeFormat = DateTimeFormat.forPattern(DateUtils.DEFAULT_TIME_FORMAT);
                userInfo.setLastLoginDate(DateUtils.getFormattedDate(cal, pattern));
                userInfo.setLastLoginTime(timeFormat.print(cal));
                userInfo.setLastLoginRemoteIpAddress(lastLoginInfo.getRemoteIpAddress());
               
            }
        }

        return userInfo;
    }

    
    @Override
    @Transactional(readOnly=true)
    public UserInfo getUserFromUsernameWithOutLoginDtl(String username) {
        UserInfo userInfo=null;
        NamedQueryExecutor<User> userExecutor = new NamedQueryExecutor<User>("Users.userByUsername")
        		.addParameter("username", username.toLowerCase())
        		.addParameter("userStatus", UserStatus.ALL_STATUSES_EXCLUDING_DELETED)
        		.addParameter("approvalStatusList", ApprovalStatus.APPROVED_RECORD_STATUS_IN_APPROVED_APPROVED_MODIFIED);
        User userObj = userDao.executeQueryForSingleValue(userExecutor);
        if (notNull(userObj)) {
            userInfo = createUserInfo(userObj);
        }
        return userInfo;
    }
    
    @Override
    public UserInfo populateUserFromLDAP(String username, List<String> userRoleNamesinAD, DirContextOperations ctx,Boolean ldapUserisAtLogin) {

        Map<String, Object> map = ldapService.convertDirectContextToMap(ctx);
        return checkAndUpdateUserIfAlreadyExists(username, userRoleNamesinAD,map,ldapUserisAtLogin);
    }

    /**
     * Specific use after authentication from Active directory.  
     */
    @Override
    public UserInfo populateUserFromUsername(String username, List<String> userRoleNamesinAD) {
        NamedQueryExecutor<User> userExecutor = new NamedQueryExecutor<User>("Users.userByUsername")
        		.addParameter("username", username.toLowerCase())
        		.addParameter("userStatus", UserStatus.ALL_STATUSES_EXCLUDING_DELETED)
        		.addParameter("approvalStatusList", ApprovalStatus.APPROVED_RECORD_STATUS_IN_APPROVED_APPROVED_MODIFIED);
        List<User> userList = userDao.executeQuery(userExecutor);
        User user = null;
        if (!userList.isEmpty()) {
            user = userList.get(0);
        } else {
            user = new User();
            user.setUsername(username);
            user.setSourceSystem(UserSecurityService.SOURCE_LDAP);
            createUser(user);
        }
        Set<Authority> authSet = syncUserRoles(user, userRoleNamesinAD);
        return createUserInfo(user, authSet);

    }
    
    public UserInfo checkAndUpdateUserIfAlreadyExists(String username, List<String> userRoleNamesinAD, Map<String, Object> map,Boolean ldapUserisAtLogin){
    	if (ldapUserisAtLogin) {
			User existingUser = null;
			List<User> userList = userService.getUserReferenceByUsername(username);
			if (userList != null && !userList.isEmpty()) {
				existingUser = userList.get(0);
			}
			if (existingUser != null) {
				return populateUserFromMap(username, userRoleNamesinAD, map, ldapUserisAtLogin);
			}
		}    	    	
    	return null;
    }

/**
     * Specific use after authentication from Active directory. 
     * Load UserInfo and update user based on attributes from ldap.
     * Note: No test case, as it requires integration with ldap services and ldap authentication. Hence, can only be tested from application UI.
     */

    public UserInfo populateUserFromMap(String username, List<String> userRoleNamesinAD, Map<String, Object> map, Boolean ldapUserisAtLogin) {
        // if create user config property enabled
        if (createUpdateUserEnable) {
        	Map<String,Object> userDetails = ldapService.createUpdateUserFromLdap(username, map);
        	User user = (User)userDetails.get("user");
            Set<Authority> authList = syncUserRoles(user, userRoleNamesinAD, ldapUserisAtLogin);            
            return createUserInfo(user, authList);
        } else { // create/update configuration is off
            return getCompleteUserFromUsername(username);
        }
    }

	

    private UserInfo createUserInfo(User user) {
        Set<Authority> authSet = loadUserAuthorities(user);
        UserInfo userInfo = createUserInfo(user, authSet);
        /*ConfigurationVO configVo = configurationService.getConfigurationPropertyFor(user.getEntityId(),
                MINIMUM_YEAR_FOR_DATE_PROPERTY);
        if (configVo != null && configVo.getText() != null) {
            userInfo.setMinYearForDate(configVo.getText());
        }*/
        return userInfo; 
    }

    private UserInfo createUserInfo(User user, Set<Authority> authSet) {
        UserInfo userInfo;
        userInfo = new UserInfo(user);
        // populating display Name from UserProfile for a user
        userInfo.setDisplayName(userService.getUserFullNameForUserId(user.getId()));
        userInfo.setAuthorities(authSet);
        userInfo.setSanctionedLimit(user.getSanctionedLimit());
        userInfo.setLoggedInModule(sessionRegistry.getModuleCode());
        if (user.getOutOfOfficeDetails() != null) {
            DateTime fromDate = user.getOutOfOfficeDetails().getFromDate();
            DateTime toDate = user.getOutOfOfficeDetails().getToDate();
            if (null != fromDate && null != toDate) {
                 if ((fromDate.toDate().after(new DateTime().toDate()) && (new DateTime()
                         .toDate().before(toDate.toDate())))
                         || (fromDate.withTimeAtStartOfDay().equals(new DateTime().withTimeAtStartOfDay()))
                         || (toDate.withTimeAtStartOfDay().equals(new DateTime().withTimeAtStartOfDay()))) {
                     userInfo.setOutOfOffice(true);
                 }
                if ((toDate.toDate().after(new DateTime().toDate()) && (fromDate.toDate().before(new DateTime().toDate())))
                        || (fromDate.withTimeAtStartOfDay().equals(new DateTime().withTimeAtStartOfDay()))
                        || (toDate.withTimeAtStartOfDay().equals(new DateTime().withTimeAtStartOfDay()))) {
                    userInfo.setOutOfOffice(true);
                } else if (new DateTime().toDate().after(toDate.toDate())) {
                    userInfo.setOutOfOffice(false);
                    User userObj = userDao.find(User.class, userInfo.getId());
                    userObj.getOutOfOfficeDetails().setFromDate(null);
                    userObj.getOutOfOfficeDetails().setToDate(null);
                    userObj.getOutOfOfficeDetails().setDelegatedToUserId(null);
                    userObj.getOutOfOfficeDetails().setOutOfOffice(false);
                    userDao.update(userObj);

                } else {
                    userInfo.setOutOfOffice(false);
                }
            } else {
                userInfo.setOutOfOffice(false);
            }

        }
        Map<String, ConfigurationVO> configurationMap = configurationService
                .getFinalUserModifiableConfigurationForEntity(user.getEntityId());

        // If there is no user preference, it means that this is a new user
        if (configurationMap.size() == 0) {
            // Add an entry in configuration group for this new user
            configurationService.populateConfigurationForNewEntity(SystemEntity.getSystemEntityId(), user.getEntityId());
            configurationMap = configurationService.getFinalUserModifiableConfigurationForEntity(user.getEntityId());
        }
      
        userInfo.setUserPreferences(configurationMap);
        
        userInfo.setBaseTenant(multiTenantService.getDefaultTenant());
      
        if(CollectionUtils.isNotEmpty(user.getUserRoles())) {
        	userInfo.setUserRoleIds(user.getUserRoles().stream().map(Role::getId).collect(Collectors.toSet()));
        }

        DelegationLoginService delegationLoginService = null;
        try {
            delegationLoginService = NeutrinoSpringAppContextUtil.getBeanByName("delegationService", DelegationLoginService.class);
        } catch (NoSuchBeanDefinitionException e) {
            BaseLoggers.exceptionLogger.error("No implementation is available for interface DelegationLoginService, moving ahead.");
        }
        if (delegationLoginService != null) {
            delegationLoginService.setDelegatedFromUserAndTeamUriSet(userInfo);
            delegationLoginService.getAuthoritiesDelegatedToUser(userInfo, authSet);
        }

        return userInfo;
    }

    @Override
    public Set<Authority> loadUserAuthorities(User user) {
        NeutrinoValidator.notNull(user, "User cannot be null");
        Set<Authority> finalAuthorties = new HashSet<Authority>();
        ForbiddenAuthorityVO forbiddenAuthorityVO=forbiddenAuthorityProvider.getForbiddenAuthorityVO();
        int degreeOfAccess=forbiddenAuthorityVO.getDegreeOfAcess();
        NamedQueryExecutor<Authority> roleAuthoritiesExecutor;
        if(degreeOfAccess!=-1)
        {
        	BaseLoggers.flowLogger.debug("Revocking Authorities acoording to degree of access= "+degreeOfAccess);
        	roleAuthoritiesExecutor=new NamedQueryExecutor<Authority>(
                    "Users.authoritiesByUserRolesDegreeOfAccess").addParameter("userId", user.getId())
        			.addParameter("degreeOfAccess", degreeOfAccess)
        			.addParameter("activeFlag", true)
        			.addParameter("currentDate", new DateTime().withTimeAtStartOfDay())
            		.addParameter("userStatus", Arrays.asList(UserStatus.STATUS_ACTIVE,UserStatus.STATUS_LOCKED))
        			.addQueryHint(QueryHint.QUERY_HINT_CACHEABLE, Boolean.TRUE);

        }
        else
        {
        	roleAuthoritiesExecutor=new NamedQueryExecutor<Authority>(
                    "Users.authoritiesByUserRolesActive").addParameter("userId", user.getId())
        			.addParameter("activeFlag", true)
        			.addParameter("currentDate", new DateTime().withTimeAtStartOfDay())
            		.addParameter("userStatus", Arrays.asList(UserStatus.STATUS_ACTIVE,UserStatus.STATUS_LOCKED))
        			.addQueryHint(QueryHint.QUERY_HINT_CACHEABLE, Boolean.TRUE);

        }
        List<Authority> roleAuthorities = userDao.executeQuery(roleAuthoritiesExecutor);
        removeForbiddenAuthorities(roleAuthorities,forbiddenAuthorityVO);
        if (hasElements(roleAuthorities)) {
            finalAuthorties.addAll(roleAuthorities);
        }
        return finalAuthorties;
    }

    private void removeForbiddenAuthorities(List<Authority> roleAuthorities,
			ForbiddenAuthorityVO forbiddenAuthorityVO) {
    		List<Authority> forbiddenAuthorities=forbiddenAuthorityVO.getForbiddenAuthorities();
    		if(ValidatorUtils.hasElements(forbiddenAuthorities))
    		{
    			roleAuthorities.removeAll(forbiddenAuthorities);
    		}
    		
	}

	public Set<Authority> syncUserRoles(User user, List<String> userRoleNamesinAD) {
        return syncUserRoles(user, userRoleNamesinAD, false);
    }
	
	public Set<Authority> syncUserRoles(User user, List<String> userRoleNamesinAD, Boolean ldapUserisAtLogin) {
		if (!ldapUserisAtLogin && defaultLdapRoleEnable && defaultLdapRole != null && !defaultLdapRole.contains("{")
				&& !userRoleNamesinAD.contains(defaultLdapRole)) {
			userRoleNamesinAD.add(defaultLdapRole);
		}
		List<Role> persistedRoles = userBasedRoles(user.getId(), userRoleNamesinAD);
		Set<Authority> authorities = new HashSet<>();

		for (Role persistedRole : persistedRoles) {
			filterRolesAndAddAuthorities(user, userRoleNamesinAD, persistedRole, ldapUserisAtLogin, authorities);
		}
		return authorities;
	}
	
	private void filterRolesAndAddAuthorities(User user, List<String> userRoleNamesinAD, Role persistedRole,
			Boolean ldapUserisAtLogin, Set<Authority> authorities) {
		if (userRoleNamesinAD.contains(persistedRole.getName())) {
			if (!persistedRole.getUsers().contains(user)) {
				persistedRole.getUsers().add(user);
			}
			authorities.addAll(persistedRole.getAuthorities());
		} else if (!ldapUserisAtLogin && createUpdateUserEnable) {
			persistedRole.getUsers().remove(user);
		} else if (persistedRole.getUsers().contains(user)) {
			authorities.addAll(persistedRole.getAuthorities());
		}
	}

    private List<Role> userBasedRoles(Long userId, List<String> role) {
        NamedQueryExecutor<Role> rolesExecutor = new NamedQueryExecutor<Role>("Users.userbasedRoles")
                .addParameter("userId", userId).addParameter("roleNames", role);
        return userDao.executeQuery(rolesExecutor);
    }

    private List<Role> getRoles() {
        NamedQueryExecutor<Role> rolesExecutor = new NamedQueryExecutor<Role>("Users.allRoles").addParameter(
                "productDescriminator", ProductInformationLoader.getProductName());
        return userDao.executeQuery(rolesExecutor);
    }

    private List<Authority> getAuthoritiesByUserRoleNames(List<String> roleNames) {
        NamedQueryExecutor<Authority> authExecutor = new NamedQueryExecutor<Authority>("Users.authoritiesByUserRoleNames")
                .addParameter("roleNames", roleNames);
        List<Authority> authorities = userDao.executeQuery(authExecutor);
        return authorities;
    }

    public void createUser(User user) {
        user.setUserStatus(UserStatus.STATUS_ACTIVE);
        entityDao.persist(user);
    }

    /* (non-Javadoc) @see com.nucleus.user.UserSecurityService#loadContextWithSystemUser()
     * This method can be used to populate the session context with System user in case of jobs.
     * Remember to clear the context manually after calling this message or it may cause problems.
     *  */
    @Override
    public void loadContextWithSystemUser() {

        UserInfo user = getCompleteUserFromUsername(NEUTRINO_SYSTEM_USER);
        UsernamePasswordAuthenticationToken result = new UsernamePasswordAuthenticationToken(user, null,
                user.getAuthorities());
        SecurityContext emptyContext = SecurityContextHolder.createEmptyContext();
        emptyContext.setAuthentication(result);
        SecurityContextHolder.setContext(emptyContext);
    }

    public Boolean getDefaultLdapRoleEnable() {
        return defaultLdapRoleEnable;
    }

    public void setDefaultLdapRoleEnable(Boolean defaultLdapRoleEnable) {
        this.defaultLdapRoleEnable = defaultLdapRoleEnable;
    }

 @Override
    public Set<Authority> syncUserRolesforCards(User user, List<String> userRoleNamesinAD) {

        if (defaultLdapRoleEnable && defaultLdapRole != null && !defaultLdapRole.contains("{")
                && !userRoleNamesinAD.contains(defaultLdapRole)) {
            userRoleNamesinAD.add(defaultLdapRole);
        }

        List<Role> allPersistedRoles = getRoles();
        Set<Authority> authorities = new HashSet<Authority>();

        for (Role persistedRole : allPersistedRoles) {
            if (userRoleNamesinAD.contains(persistedRole.getName())) {
                if (!persistedRole.getUsers().contains(user)) {
                    persistedRole.getUsers().add(user);
                }
                authorities.addAll(persistedRole.getAuthorities());
            }
        }
        return authorities;
    }
}
