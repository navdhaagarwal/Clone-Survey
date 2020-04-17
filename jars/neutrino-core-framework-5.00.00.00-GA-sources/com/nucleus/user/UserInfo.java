package com.nucleus.user;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.Transient;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.nucleus.authority.Authority;
import com.nucleus.businessmapping.service.UserInfoOrgBranchMappingService;
import com.nucleus.cas.ModuleDetails;
import com.nucleus.config.persisted.enity.Configuration;
import com.nucleus.config.persisted.service.ConfigurationService;
import com.nucleus.config.persisted.vo.ConfigurationVO;
import com.nucleus.core.NeutrinoSpringAppContextUtil;
import io.swagger.annotations.ApiModelProperty;
import com.nucleus.core.initialization.ProductInformationLoader;
import com.nucleus.core.money.entity.Money;
//import com.nucleus.core.notification.service.UserMailNotificationService;
import com.nucleus.core.organization.entity.SystemName;
import com.nucleus.core.validation.util.NeutrinoValidator;
import com.nucleus.entity.BaseTenant;
import com.nucleus.entity.EntityId;
import com.nucleus.entity.SystemEntity;
import com.nucleus.event.UserMailNotificationType;
import com.nucleus.finnone.pro.base.constants.CoreConstant;
import com.nucleus.finnone.pro.general.util.CoreDateUtility;
import com.nucleus.persistence.HibernateUtils;
import com.nucleus.security.core.session.SessionModuleServiceImpl;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * VO To hold logged-in user information
 */
public class UserInfo implements UserDetails {

	private static final long            serialVersionUID = -7602127825185978392L;

	private final Long                   id;
	private final String                 username;
	private String                       displayName;
	
	@ApiModelProperty(hidden=true)
	private final String                 password;
	private Set<Authority>               authorities;
	
	@ApiModelProperty(hidden=true)
	private Set<GrantedAuthority>        springSecurityAuthorities;
	public final int                     userStatus;
	private DateTime               		 passwordExpiryDate;
	private boolean                      authoritiesHaveBeenSet;
	private final EntityId               userEntityId;
	private final User                   userShallowReference;
	private final String                 hashKey;
	/**
	 * Toggle for Login Enable
	 * 
	 */
	private boolean 					 isLoginEnabled;


	private String                       createdBy;
	private DateTime                     creationTimeStamp;
	private String                       uuid;
	private Map<String, ConfigurationVO> userPreferences;
	private OrgBranchInfo                loggedInBranch   = new OrgBranchInfo();
	private List<OrgBranchInfo>          userBranchList;
	private List<LoanProductInfo>        loanProductInfoList;
	private boolean                      isBusinessPartner;
	private String                       passwordExpiresInDays;
	private DateTime                     lastPasswordResetDate;
	private boolean                      outOfOffice;
	private String                       lastLoginDate;
	private String                       lastLoginTime;
	private String                       lastLoginRemoteIpAddress;
	private String                       mailId;
	private Character                    accessToAllBranches;
	private Character                    accessToAllProducts;
	private boolean                      allBranchesFlag;
	private boolean                      isRelationshipOfficer;
	private boolean                      isSupervisor;
	private String                       fullName;
	private String                       minYearForDate;
	private String                       maxYearForDate;
	private SystemName                   sysName;
	private List<ModuleDetails>          allowedModules;
	private String 						 loggedInModule;
	private List<UserDeviceMapping>      registeredDevices;
	private boolean                      isDeviceAuthEnabled;
	private Set<Long>          			 userRoleIds;
	private boolean						 isChatEnabled;
	private long						 emailCount;

	public String getLoggedInModule() {
		return loggedInModule;
	}

	public void setLoggedInModule(String loggedInModule) {
		this.loggedInModule = loggedInModule;
	}

	public List<ModuleDetails> getAllowedModules() {
		return allowedModules;
	}

	public void setAllowedModules(List<ModuleDetails> allowedModules) {
		this.allowedModules = allowedModules;
	}

	private String                       reviewedByUri;
	private Integer                      approvalStatus;
	private Boolean                      licenseAccepted;
	private Map<String, String>          userAuthoritiesMap = new HashMap<>();
	private Set<String>         		 lowPriorityUserModulesSet = new HashSet<>();
	private List<OrgBranchInfo>          approvedAndActiveUserBranchList;
	
	@ApiModelProperty(hidden=true)
	private transient UserInfoOrgBranchMappingService userInfoOrgBranchMappingService;

	/**
	 * This object would contain the primary organization branch info  by default for a logged in user.
	 * If user switches the organization branch he/she wishes to work with , then this object needs to be updated.    
	 */
	private OrgBranchInfo                primaryOrgBranchInfo;

	private Money                        sanctionedLimit;

	private BaseTenant 					 baseTenant;

	@Transient
	@ApiModelProperty(hidden=true)
	public HashMap<String, Object>      viewProperties;

	private boolean isMobileLoginAllowed;

	private Set<String> delegatedFromUserUri;

	private Set<String> delegatedFromTeamUri;

	private String mappedSessionId;

	public String getMappedSessionId() {
		return mappedSessionId;
	}

	public void setMappedSessionId(String mappedSessionId) {
		this.mappedSessionId = mappedSessionId;
	}

	/**
	 * @return the baseTenant
	 */
	public BaseTenant getBaseTenant() {
		return baseTenant;
	}

	/**
	 * @param baseTenant the baseTenant to set
	 */
	public void setBaseTenant(BaseTenant baseTenant) {
		this.baseTenant = baseTenant;
	}

	public boolean isMobileLoginAllowed() {
		return isMobileLoginAllowed;
	}

	public void setMobileLoginAllowed(boolean isMobileLoginAllowed) {
		this.isMobileLoginAllowed = isMobileLoginAllowed;
	}

	public DateTime getPasswordExpiryDate() {
		return passwordExpiryDate;

	}

	public String getFullName() {
		return fullName;
	}

	public void setFullName(String fullName) {
		this.fullName = fullName;
	}



	public void addProperty(String key, Object value) {
		if (viewProperties == null) {
			this.viewProperties = new LinkedHashMap<String, Object>();
		}
		this.viewProperties.put(key, value);
	}

	/**
	 * @return the viewProperties
	 */
	public HashMap<String, Object> getViewProperties() {
		return viewProperties;
	}

	/**
	 * @param viewProperties the viewProperties to set
	 */
	public void setViewProperties(HashMap<String, Object> viewProperties) {
		this.viewProperties = viewProperties;
	}

	/**
	 * @return the userPreferences
	 */
	public Map<String, ConfigurationVO> getUserPreferences() {
		return userPreferences;
	}

	/**
	 * @param userPreferences the userPreferences to set
	 */
	public void setUserPreferences(Map<String, ConfigurationVO> userPreferences) {
		this.userPreferences = userPreferences;
	}

	/**
	 * @return the hashKey
	 */
	public String getHashKey() {
		return hashKey;
	}

	public UserInfo(User user) {
		this.id = user.getId();
		this.username = user.getUsername();
		this.userStatus = user.getUserStatus();
		this.password = user.getPassword();
		this.passwordExpiryDate = user.getPasswordExpirationDate();
		this.passwordExpiresInDays = user.getPasswordExpiresInDays();
		this.userEntityId = user.getEntityId();
		this.userShallowReference = this.userEntityId.toInstance(User.class);
		this.userShallowReference.setMaintainLoginDays(user.getMaintainLoginDays());
		if(user.getMaintainLoginDays() != null && user.getMaintainLoginDays()){
			this.userShallowReference.setUserCalendar(HibernateUtils.initializeAndUnproxy(user.getUserCalendar()));
		}
		this.userShallowReference.setForcePasswordResetOnLogin(user.isForcePasswordResetOnLogin());
		this.hashKey = user.getHashKey();
		this.isBusinessPartner = user.isBusinessPartner();
		this.lastPasswordResetDate = user.getLastPasswordResetDate();
		if (user.getEntityLifeCycleData() != null) {
			this.uuid = user.getEntityLifeCycleData().getUuid();
			this.createdBy = user.getEntityLifeCycleData().getCreatedByUri();
		}
		if (user.getMasterLifeCycleData() != null) {
			this.reviewedByUri = user.getMasterLifeCycleData().getReviewedByUri();
		}
		this.mailId = user.getMailId();
		this.accessToAllBranches = user.getAccessToAllBranches();
		this.accessToAllProducts = user.getAccessToAllProducts();
		this.isRelationshipOfficer = user.isRelationshipOfficer();
		this.isSupervisor = user.isSupervisor();
		this.sysName = HibernateUtils.initializeAndUnproxy(user.getSysName());
		this.approvalStatus=user.getApprovalStatus();
		addProperty("actions",user.getViewProperties().get("actions"));
		addProperty("customizedActions", user.getViewProperties().get("customizedActions"));
		addProperty("loggedInModules", user.getViewProperties().get("loggedInModules"));
		this.licenseAccepted = user.isLicenseAccepted();
		if(user.getUserMobileInfo() != null){
			
				this.isMobileLoginAllowed = user.getUserMobileInfo().getIsMobileEnabled();
				if(user.getUserMobileInfo().getIsDeviceAuthEnabled()!=null){
					this.registeredDevices = user.getUserMobileInfo().getRegisteredDeviceList();
					this.isDeviceAuthEnabled = user.getUserMobileInfo().getIsDeviceAuthEnabled();
				}
				
			
		}
		else{
			this.isMobileLoginAllowed = false;
		}
		this.setLoginEnabled(user.isLoginEnabled());
	}
	
	public UserInfo(UserInfo userInfo) {
		this.id = userInfo.getId();
		this.mappedSessionId = userInfo.getMappedSessionId();
		this.username = userInfo.getUsername();
		this.password = null;
		this.userEntityId = userInfo.getUserEntityId();
		this.loggedInModule = userInfo.getLoggedInModule();
		this.userShallowReference = null;
		this.hashKey = null;
		this.userStatus = userInfo.userStatus;
		this.lowPriorityUserModulesSet = userInfo.getLowPriorityUserModulesSet();
		this.isChatEnabled = userInfo.isChatEnabled();

		this.emailCount = getEmailCount(userInfo.getUserReference().getUri());
		userInfo.setEmailCount(this.emailCount);
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	@Override
	public boolean isAccountNonLocked() {
		return userStatus != UserStatus.STATUS_LOCKED;
	}

	@Override
	public boolean isCredentialsNonExpired() {
		if (this.getPasswordExpiryDate() != null && this.getPasswordExpiresInDays() != null) {
			if (!(this.getPasswordExpiresInDays().equalsIgnoreCase(User.PSWD_NEVER_EXPIRES))
					&& !expiryDateisAfterToday() ) {
				return false;
			}
		}

		return true;
	}

	private boolean expiryDateisAfterToday()
	{
		return CoreDateUtility.formatDate(this.getPasswordExpiryDate(), CoreConstant.DATE_FORMAT).isAfter(CoreDateUtility.formatDate(new DateTime(new Date()), CoreConstant.DATE_FORMAT));
	}


	@Override
	public boolean isEnabled() {
		return userStatus == UserStatus.STATUS_ACTIVE;
	}
	
	
	public boolean isInActive() {
		return userStatus == UserStatus.STATUS_INACTIVE;
	}

	@Override
	public boolean isAccountNonExpired() {
		/* User is allowed to login even after password expiration 
		 * but is forced to change the password at its first login
		 * after password expiration
		 * */
		return true;
		// return passwordExpiryDate.getTime().after(new Date());
	}

	@Override
	public Set<GrantedAuthority> getAuthorities() {
		return springSecurityAuthorities;
	}

	public Set<Authority> getUserAuthorities() {
		return authorities;
	}

	public Long getId() {
		return id;
	}

	/**
	 * Sets the authorities for this user. Once set, the authorities cannot be modified for this UserInfo object
	 * @param authorities The derived final list of authorities for this user.
	 */
	public void setAuthorities(Set<Authority> authorities) {
		if (authorities == null || authorities.isEmpty()) {
			return;
		}
		NeutrinoValidator.isTrue(!authoritiesHaveBeenSet,
				"Authorities are already set. The object is locked for any authority changes");
		this.authorities = Collections.unmodifiableSet(authorities);
		this.springSecurityAuthorities = new LinkedHashSet<>();
		for (Authority authority : authorities) {
			String authCode=authority.getAuthority();
			springSecurityAuthorities.add(new SpringSecurityAuthorityAdapter(authCode));


		}
		this.springSecurityAuthorities = Collections.unmodifiableSet(this.springSecurityAuthorities);
		authoritiesHaveBeenSet = true;
		if ( userAuthoritiesMap.isEmpty() && springSecurityAuthorities != null) {
			for (GrantedAuthority grantedAuthority : springSecurityAuthorities) {
				userAuthoritiesMap.put(grantedAuthority.getAuthority(), grantedAuthority.getAuthority());
			}
		}
		if(!userAuthoritiesMap.isEmpty()) {
			String module = StringUtils.capitalize(ProductInformationLoader.getProductName());
			if(userAuthoritiesMap.containsKey(SessionModuleServiceImpl.LOW_PRIORITY_USER+module)) {
				lowPriorityUserModulesSet.add(module);
			}
			if(userAuthoritiesMap.containsKey(SessionModuleServiceImpl.LOW_PRIORITY_USER+"SSO")) {
				lowPriorityUserModulesSet.add("SSO");
			}
			if(userAuthoritiesMap.containsKey(Configuration.CHAT_ENABLED)) {
				this.isChatEnabled=true;
			} 
		}
	}
	@Override
	public String getPassword() {
		return password;
	}

	@Override
	public String getUsername() {
		return username;
	}

	public String getDisplayName() {
		return displayName;
	}

	public EntityId getUserEntityId() {
		return userEntityId;
	}

	/**
	 * Returns a shallow reference of the user object.</br> 
	 * <b>NOTE (Very Important): </b> The returned {@link User} object only contains the id of user and is only intended 
	 * to be set as non-cascading reference in entities where User object is required. 
	 * @return Shallow user reference.
	 */
	public User getUserReference() {
		return userShallowReference;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((userEntityId == null) ? 0 : userEntityId.hashCode()) + ((loggedInModule == null) ? 0 : loggedInModule.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		UserInfo other = (UserInfo) obj;
		if (this.userEntityId == null) {
			if (other.userEntityId != null)
				return false;
		} else if (!this.userEntityId.equals(other.userEntityId))
			return false;

		if (this.loggedInModule == null) {
			if (other.loggedInModule != null) {
				return false;
			}
		} else if (!this.loggedInModule.equalsIgnoreCase(other.loggedInModule)) {
			return false;
		}
		return true;
	}

	public OrgBranchInfo getPrimaryOrgBranchInfo() {
		if (primaryOrgBranchInfo == null) {
			getUserInfoOrgBranchService().updateOrgBranchInfo(this);
		}
		return primaryOrgBranchInfo;
	}

	public void setPrimaryOrgBranchInfo(OrgBranchInfo primaryOrgBranchInfo) {
		this.primaryOrgBranchInfo = primaryOrgBranchInfo;
	}

	public String getCreatedBy() {
		return createdBy;
	}

	public String getReviewedByUri() {
		return reviewedByUri;
	}

	public Integer getApprovalStatus() {
		return approvalStatus;
	}

	public DateTime getCreationTimeStamp() {
		return creationTimeStamp;
	}

	public String getUuid() {
		return uuid;
	}

	public OrgBranchInfo getLoggedInBranch() {
		if (loggedInBranch == null) {
			getUserInfoOrgBranchService().updateOrgBranchInfo(this);
		}
		return loggedInBranch;
	}

	public void setLoggedInBranch(OrgBranchInfo loggedInBranch) {
		this.loggedInBranch = loggedInBranch;
	}




	public boolean isLoginEnabled() {
		return isLoginEnabled;
	}

	public void setLoginEnabled(boolean isLoginEnabled) {
		this.isLoginEnabled = isLoginEnabled;
	}
	public List<OrgBranchInfo> getUserBranchList() {
		if (userBranchList == null) {
			getUserInfoOrgBranchService().updateOrgBranchInfo(this);
		}
		return userBranchList;
	}

	public void setUserBranchList(List<OrgBranchInfo> userBranchList) {
		this.userBranchList = userBranchList;
	}

	public List<LoanProductInfo> getLoanProductInfoList() {
		return loanProductInfoList;
	}

	public void setLoanProductInfoList(List<LoanProductInfo> loanProductInfoList) {
		this.loanProductInfoList = loanProductInfoList;
	}

	public boolean isBusinessPartner() {
		return isBusinessPartner;
	}

	public void setBusinessPartner(boolean isBusinessPartner) {
		this.isBusinessPartner = isBusinessPartner;
	}

	public String getPasswordExpiresInDays() {
		ConfigurationService configurationService=NeutrinoSpringAppContextUtil.getBeanByName(
				"configurationService", ConfigurationService.class);
		ConfigurationVO configVo = configurationService.getConfigurationPropertyFor(SystemEntity.getSystemEntityId(),
				"config.applyGlobalExpiry");
		if(configVo!=null && configVo.getPropertyValue().equalsIgnoreCase("true")) {

			ConfigurationVO configVo1 = configurationService.getConfigurationPropertyFor(SystemEntity.getSystemEntityId(),
					"config.globalExpiry.days");
			if (configVo1 != null) {
				this.passwordExpiresInDays = configVo1.getPropertyValue();
			}
		}

		return passwordExpiresInDays;
	}

	public long getEmailCount(String userUri){
		/*UserMailNotificationService userMailNotificationService=NeutrinoSpringAppContextUtil.getBeanByName(
				"userMailNotificationService", UserMailNotificationService.class);
		List<String> STAT_LIST = Collections.unmodifiableList(Arrays.asList(
				UserMailNotificationType.USER_MAIL_NEW, UserMailNotificationType.USER_MAIL_DELETED_FROM_OUTBOX,
				UserMailNotificationType.USER_MAIL_DELETED_FROM_OUTBOX_TRASH));
		return userMailNotificationService.getUserNotificationsCount(userUri, STAT_LIST,"inbox");*/
		return 1l;
	}

	public void setPasswordExpiresInDays(String passwordExpiresInDays) {
		this.passwordExpiresInDays = passwordExpiresInDays;
	}

	public DateTime getLastPasswordResetDate() {
		return lastPasswordResetDate;
	}

	public void setLastPasswordResetDate(DateTime lastPasswordResetDate) {
		this.lastPasswordResetDate = lastPasswordResetDate;
	}

	public boolean isOutOfOffice() {
		return outOfOffice;
	}

	public void setOutOfOffice(boolean outOfOffice) {
		this.outOfOffice = outOfOffice;
	}

	public String getLastLoginDate() {
		return lastLoginDate;
	}

	public void setLastLoginDate(String lastLoginDate) {
		this.lastLoginDate = lastLoginDate;
	}

	public String getLastLoginTime() {
		return lastLoginTime;
	}

	public void setLastLoginTime(String lastLoginTime) {
		this.lastLoginTime = lastLoginTime;
	}

	public String getLastLoginRemoteIpAddress() {
		return lastLoginRemoteIpAddress;
	}

	public void setLastLoginRemoteIpAddress(String lastLoginRemoteIpAddress) {
		this.lastLoginRemoteIpAddress = lastLoginRemoteIpAddress;
	}

	public String getMailId() {
		return mailId;
	}

	public void setMailId(String mailId) {
		this.mailId = mailId;
	}

	public Character getAccessToAllBranches() {
		return accessToAllBranches;
	}

	public void setAccessToAllBranches(Character accessToAllBranches) {
		this.accessToAllBranches = accessToAllBranches;
	}

	public Character getAccessToAllProducts() {
		return accessToAllProducts;
	}

	public void setAccessToAllProducts(Character accessToAllProducts) {
		this.accessToAllProducts = accessToAllProducts;
	}

	public boolean isAllBranchesFlag() {
		return allBranchesFlag;
	}

	public void setAllBranchesFlag(boolean allBranchesFlag) {
		this.allBranchesFlag = allBranchesFlag;
	}

	public boolean isRelationshipOfficer() {
		return isRelationshipOfficer;
	}

	public void setRelationshipOfficer(boolean isRelationshipOfficer) {
		this.isRelationshipOfficer = isRelationshipOfficer;
	}

	public boolean isSupervisor() {
		return isSupervisor;
	}

	public void setSupervisor(boolean isSupervisor) {
		this.isSupervisor = isSupervisor;
	}

	public Money getSanctionedLimit() {
		return sanctionedLimit;
	}

	public void setSanctionedLimit(Money sanctionedLimit) {
		this.sanctionedLimit = sanctionedLimit;
	}

	public String getMinYearForDate() {
		return minYearForDate;
	}

	public void setMinYearForDate(String minYearForDate) {
		this.minYearForDate = minYearForDate;
	}

	public void setCreatedByLoggedInUsersByName(String username) {
		this.createdBy = username;
	}

	public SystemName getSysName() {
		return sysName;
	}

	public void setSysName(SystemName sysName) {
		this.sysName = sysName;
	}

	public String getMaxYearForDate() {
		return maxYearForDate;
	}

	public void setMaxYearForDate(String maxYearForDate) {
		this.maxYearForDate = maxYearForDate;
	}

	public Boolean getLicenseAccepted() {
		return licenseAccepted;
	}

	public void setLicenseAccepted(Boolean licenseAccepted) {
		this.licenseAccepted = licenseAccepted;
	}

	public boolean hasAuthority(String authority) {

		return (userAuthoritiesMap.get(authority) != null);
	}

	public List<OrgBranchInfo> getApprovedAndActiveUserBranchList() {
		if (approvedAndActiveUserBranchList == null) {
			getUserInfoOrgBranchService().updateOrgBranchInfo(this);
		}
		return approvedAndActiveUserBranchList;
	}

	public void setApprovedAndActiveUserBranchList(
			List<OrgBranchInfo> approvedAndActiveUserBranchList) {
		this.approvedAndActiveUserBranchList = approvedAndActiveUserBranchList;
	}

	public List<UserDeviceMapping> getRegisteredDevices() {
		return registeredDevices;
	}

	public void setRegisteredDevices(List<UserDeviceMapping> registeredDevices) {
		this.registeredDevices = registeredDevices;
	}

	public boolean isDeviceAuthEnabled() {
		return isDeviceAuthEnabled;
	}

	public void setDeviceAuthEnabled(boolean isDeviceAuthEnabled) {
		this.isDeviceAuthEnabled = isDeviceAuthEnabled;
	}

	public Set<Long> getUserRoleIds() {
		return userRoleIds;
	}

	public void setUserRoleIds(Set<Long> userRoleIds) {
		this.userRoleIds = userRoleIds;
	}

	public boolean isChatEnabled() {
		return isChatEnabled;
	}
	
	public Set<String> getLowPriorityUserModulesSet() {
		return lowPriorityUserModulesSet;
	}

	public long getEmailCount() {
		return emailCount;
	}

	public void setEmailCount(long emailCount) {
		this.emailCount = emailCount;
	}

	public Set<String> getDelegatedFromUserUri() {
		return delegatedFromUserUri;
	}

	public void setDelegatedFromUserUri(Set<String> delegatedFromUserUri) {
		this.delegatedFromUserUri = delegatedFromUserUri;
	}

	public Set<String> getDelegatedFromTeamUri() {
		return delegatedFromTeamUri;
	}

	public void setDelegatedFromTeamUri(Set<String> delegatedFromTeamUri) {
		this.delegatedFromTeamUri = delegatedFromTeamUri;
	}
	
	private UserInfoOrgBranchMappingService getUserInfoOrgBranchService() {
		if (userInfoOrgBranchMappingService == null) {
			userInfoOrgBranchMappingService = NeutrinoSpringAppContextUtil
					.getBeanByName("userInfoOrgBranchMappingService", UserInfoOrgBranchMappingService.class);
		}
		return userInfoOrgBranchMappingService;
	}
}