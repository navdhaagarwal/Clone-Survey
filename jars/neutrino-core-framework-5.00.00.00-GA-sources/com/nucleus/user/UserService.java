package com.nucleus.user;

import java.text.ParseException;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import com.nucleus.menu.MenuEntity;
import com.nucleus.reason.BlockReason;
import com.nucleus.user.ipaddress.IpAddress;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;

import com.nucleus.authority.Authority;
import com.nucleus.contact.PhoneNumber;
import com.nucleus.core.money.entity.Money;
import com.nucleus.core.organization.entity.UserBPMapping;
import com.nucleus.core.role.entity.Role;
import com.nucleus.entity.EntityId;
import com.nucleus.makerchecker.UnapprovedEntityData;
import com.nucleus.service.BaseService;

import javax.servlet.http.HttpServletRequest;

/**
 * The Interface UserService.
 */
public interface UserService extends BaseService {

    public static final String SOURCE_LDAP = "ldap";
    public static final String SOURCE_DB   = "db";
    public static final String SOURCE_FEDERATED = "federated";

    /**
     * Creates the user.
     *
     * @param user the user
     */
    public void createUser(User user);

    /**
     * Save user.
     *
     * @param user the user
     */
    public void saveUser(User user);

    /**
     * Save user profile.
     *
     * @param userProfile the user profile
     */
    public void saveUserProfile(UserProfile userProfile);

    /**
     * Login user.
     *
     * @param username the username
     * @param password the password
     * @return the user info
     */
    public UserInfo loginUser(String username, String password);

    /**
     * Assign Authorities to User.
     *
     * @param userId the user id
     * @param authorities the authorities
     */
    public void assignAuthoritiesToUser(Long userId, Set<Authority> authorities);

    /**
     * Retrive the User by username.
     *
     * @param username the username
     * @return the user from username
     */
    public UserInfo getUserFromUsername(String username);

    /**
     * Gets the random user by authority.
     *
     * @param authorityCode the authority code
     * @return the random user by authority
     */
    public UserInfo getRandomUserByAuthority(String authorityCode);

    /**
     * Temporary Method to find a user with lowest Primary key with with given authority.
     *
     * @param authorityCode the authority code
     * @return UserInfo
     */
    public UserInfo getFirstUserByAuthority(String authorityCode);

    /**
     * Method to find a user with lowest Primary key with with given authority.
     *
     * @param authorityCode the authority code
     * @return UserInfo
     */
    public List<Long> getAllUserWithGivenAuthority(String authorityCode);

    /**
     * Create or update usergroup.
     *
     * @param userGroup the user group
     */
    public void saveUserGroup(UserGroup userGroup);

    /**
     * Create or update role.
     *
     * @param role the role
     */
    public void saveRole(Role role);

    /**
     * Create or update userAuthority.
     *
     * @param userAuthority the user authority
     */
    public void saveUserAuthority(UserAuthority userAuthority);

    /**
     * Get All UserGroups.
     *
     * @return List of UserGroup
     */
    public List<UserGroup> getUserGroups();

    /**
     * Get All Authorities.
     *
     * @return List of Authorities
     */
    public List<Authority> getAuthorities();

    /**
     * Get All Roles.
     *
     * @return List of Roles
     */
    public List<Role> getRoles();

    /**
     * Load User Authorities.
     *
     * @param user the user
     * @return Set of Authorities
     */
    public Set<Authority> loadOnlyUserAuthorities(User user);

    /**
     * Load Role Authorities.
     *
     * @param user the user
     * @return Set of Authorities
     */
    public Set<Authority> loadOnlyRoleAuthorities(User user);

    /**
     * Load UserGroup Authorities.
     *
     * @param user the user
     * @return Set of Authorities
     */
    public Set<Authority> loadOnlyUserGroupAuthorities(User user);

    /**
     * Get Roles From UserId.
     *
     * @param userId the user id
     * @return List of Roles
     */
    public List<Role> getRolesFromUserId(Long userId);

    /**
     * Delete User.
     *
     * @param user the user
     */
    public void deleteUser(User user);

    /**
     * Get UserAuthorities From UserId.
     *
     * @param userId the user id
     * @return UserAuthority
     */
    public UserAuthority getUserAuthoritiesFromUserId(Long userId);

    /**
     * Invalidate User.
     *
     * @param user the user
     */
    public void invalidateUser(User user);

    /**
     * Lock User.
     *
     * @param user the user
     */
    public void lockUser(User user, EntityId byUserEntityId);

    /**
     * Delete Role.
     *
     * @param role the role
     */
    public void deleteRole(Role role);

    /**
     * Delete UserGroup.
     *
     * @param userGroup the user group
     */
    public void deleteUserGroup(UserGroup userGroup);

    /**
     * Get Authority By Code.
     *
     * @param authCode the auth code
     * @return Authority
     */
    public Authority getAuthorityByCode(String authCode);

    /**
     * Get UserGroup By Id.
     *
     * @param userGroupId the user group id
     * @return UserGroup
     */
    public UserGroup getUserGroupById(Long userGroupId);

    /**
     * Get User By Id.
     *
     * @param userId the user id
     * @return UserInfo
     */
    public UserInfo getUserById(Long userId);

    /**
     * Get Role By Id.
     *
     * @param roleId the role id
     * @return Role
     */
    public Role getRoleById(Long roleId);

    /**
     * Get Authority By Id.
     *
     * @param authorityId the authority id
     * @return Authority
     */
    public Authority getAuthorityById(Long authorityId);

    /**
     * Add Users To Role.
     *
     * @param role the role
     * @param users the users
     */
    public void addUsersToRole(Role role, Set<User> users);

    /**
     * Remove All Users From Role.
     *
     * @param role the role
     */
    public void removeAllUsersFromRole(Role role);

    /**
     * Add Users To UserGroup.
     *
     * @param role the role
     * @param users the users
     */
    public void addUsersToUserGroup(UserGroup role, Set<User> users);

    /**
     * Remove All Users From UserGroup.
     *
     * @param role the role
     */
    public void removeAllUsersFromUserGroup(UserGroup role);

    /**
     * Assign Authorities To Role.
     *
     * @param role the role
     * @param authorities the authorities
     */
    public void assignAuthoritiesToRole(Role role, Set<Authority> authorities);

    /**
     * Remove All Authorities From Role.
     *
     * @param role the role
     */
    public void removeAllAuthoritiesFromRole(Role role);

    /**
     * Assign Authorities To UserGroup.
     *
     * @param userGroup the user group
     * @param authorities the authorities
     */
    public void assignAuthoritiesToUserGroup(UserGroup userGroup, Set<Authority> authorities);

    /**
     * Remove All Authorities From UserGroup.
     *
     * @param userGroup the user group
     */
    public void removeAllAuthoritiesFromUserGroup(UserGroup userGroup);

    /**
     * Remove All Authorities From User.
     *
     * @param user the user
     */
    public void removeAllAuthoritiesFromUser(User user);

    /**
     * Get UserGroups From UserId.
     *
     * @param userId the user id
     * @return List of UserGroup
     */
    public List<UserGroup> getUserGroupsFromUserId(Long userId);

    /**
     * Get UserAuthorities From UserRoleNames.
     *
     * @param roleNames the role names
     * @return List of UserAuthority
     */
    public List<Authority> getAuthoritiesByUserRoleNames(List<String> roleNames);

    /**
     * Check User's availability in Forgot Password Functionality.
     *
     * @param userName the user name
     * @param mailId the mail id
     * @return the user
     */
    public User userExistenceInForgotPassword(String userName, String mailId);

    /**
     * To Get All the Users.
     *
     * @return the all user
     */
    public List<User> getAllUser();

    /**
     * to get all user Profile Info
     * 
     * 
     * @return all User Profiles
     */
    public List<UserProfile> getAllUserProfile();

    /**
     * Update user.
     *
     * @param user the user
     */
    public void updateUser(User user);

    /**
     * 
     * @param timeToken
     * @return
     */
    public User findUserByPasswordResetTimeToken(String timeToken);

    /**
     * 
     * @param timeToken
     * @return
     */
    public Object[] findApproveLinkTokenByTokenId(String timeToken);

    /**
     * find User with the UUID.
     *
     * @param uuid the uuid
     * @return the user
     */
    public User findUserByUUID(String uuid);

    /**
     * find User with the username Provided.
     *
     * @param username the username
     * @return the user
     */
    public User findUserByUsername(String username);

    /**
     * find Login Details with the username Provided.
     *
     * @param username the username
     * @return the login details
     */
    public List<User> getLoginDetails(String username);

    /**
     * Gets the user profile.
     *
     * @param user the user
     * @return the user profile
     */
    public UserProfile getUserProfile(User user);

    /**
     * Gets the current user.
     *
     * @return the current user
     */
    public UserInfo getCurrentUser();

    /**
     * Gets the user preferred date format.
     *
     * @return the user preferred date format
     */
    public String getUserPreferredDateFormat();

    public String getUserPreferredTimeZone();

    public String getUserPreferredDateTimeFormat();

    public Locale getUserLocale();

    public boolean isIpAddressInRange(String ip, String fromIp, String toIp);

    public boolean isSecuredIpAddressInMaster(String ip);

    public String validateUserIp(Map<String, String> headers, String ipCameFrom, UserProfile userProfile, Locale locale);

    public String getUserMailById(Long userId);

    public void activateUser(User user);

    public User setUserPasswordExpirationDate(User user);

    public String getUserNameByUserUri(String userUri);

    public String getUserNameByUserId(Long userId);

    public UserBPMapping mapUserToBusinessPartner(Long bpId, User user);

    public UserBPMapping getBPMappedToUser(Long userId);

    public void saveRolesForUser(User user, Long[] roleIds);

    public List<Role> getRolesByRoleName(String roleName);

    public DateTime parseDateTime(String dateTime) throws ParseException;


    public List<User> getAllUserExceptCurrent();

    public List<User> getAllUsersInCurrentBranchExceptCurrent();

    public void updateUserWithNewPassword(User user);

    public String incrementFailedLoginCount(Long userId);

    public void resetFailedLoginCountToZero(Long userId);

    public String incrementFailedPassResetCount(Long userId);

    public void resetFailedPassResetCountToZero(Long userId);

    public UserMobilityInfo getUserMobilityInfo(Long userId);

    public PhoneNumber getUserMobileNumber(Long userId);

    /*
     * 
     * @param userProfile
     * @return save new User in database and persist UserProfile
     */
    public void saveNewUserProfile(UserProfile userProfile);

    String getUserFullNameForUserId(Long userId);

    /*Only username and id will be populated based on Authority Code
     * 
     */
    public List<User> getAuthenticatedUsersNameAndId(String authCode);

    public List<User> getAllUsersInCurrentBranch();

    void lockUser(User user, EntityId byUserEntityId, Integer daysToBlock);

    /**
     * Gets the all user profile name and id.
     *
     * @return the all user profile name and id 
     */
    public List<Map<Long, String>> getAllUserProfileNameAndId();

    public List<Map<Long, String>> getAllUserProfileNameAndIdForBinderList();

    
    /**
     * Gets the deviationLevel of currentUser  
     */
    public DeviationLevel getDeviationLevel(Long userId);

    /**
     * Gets the list of user security questions in case of block
     */
    public List<UserSecurityQuestion> getUserSecurityQuestions(String username);

    /**
     * Gets the security question answer for a particular user
     * @param username
     * 
     */
    public Map<Long, String> getUserQuestionAnswerMap(String username);

    public Money getUserSanctionLimitById(Long userId);

   /**
     * Gets the list of userSecurityQuestionAnswer
     * @param username
     */
    public List<UserSecurityQuestionAnswer> getUserSecurityQuestionAnswer(String username);
    
    /**
     * inactivates all users who have not logged in for a particular number of days.the number 
     * of days are configured using a configuration property.This service is invoked by UserInactivationScheduler
     */
    public void inactivateUsersBasedOnLastLoginTime();
    
    /**
     * block all users who have not logged in for a particular number of days.the number 
     * of days are configured using a configuration property.This service is invoked by UserInactivationScheduler
     */
    public void blockUsersBasedOnLastLoginTime();
    /**
     * updates the list of userSecurityQuestionAnswer
     * @param username
     * @param List of UserSecurityQuestionAnswer
     */
    public void updateUserSecurityQuestionAnswer(String username, List<UserSecurityQuestionAnswer> quesAnsList);

    /**
     * Gets the sourceSystem for a particular user based on Id
     * @param username
     * 
     */
    String getSourceSystemForUserId(Long userId);

    /**
     * Parses the local date.
     *
     * @param dateTime the date time
     * @return the local date
     * @throws ParseException the parse exception
     */
    public LocalDate parseLocalDate(String dateTime) throws ParseException;

    /**
     * Gets the photo url for the passed user's user id.
     * 
     * @param userId
     * @return photoUrl
     * 
     */
    String getUserPhotoUrl(Long userId);

    public List<String> fetchAccessBranchesToCurrentUser(Long userId);

    Boolean getForceResetPassForUserId(Long userId);

    /**
     * 
     * 
     * @return string to get all User Uris in current Branch
     */
    public String getAllUsersUrisInCurrentBranch();

    /**
     * 
     * @param userName
     * @return userId of User with given userName
     */
    public Long getUserIdByUserName(String userName);

    public String getUserUriByUserName(String userName);


    /**
     * @return all users who are marked as super admins.
     */
    public List<User> getAllSuperAdmin();

    /**
     * @return true if user is a super admin, otherwise false.
     */
    public boolean isUserSuperAdmin(Long userId);

    public List<User> getUserByRole(Long role, String authCode);
    
    public List<Long> getIsBranchAccessibleForUser(Long userId, Long branchId);

	public String getUserNameFromDisplayName(String officerName);

    public String getSourceSystemForUserName(String userName);
    
    void saveUserAuditLog(UserAuditLog userAuditLog);

    List<String> getRoleNamesFromUserId(Long userId);

    List<UnapprovedEntityData> fetchAuditLogOfUserByUserUUID(String uuid);

    public Integer getLatestVersionOfAuditForUser(Long id);

    List<String> getBranchCodeFromUserId(Long userId);

    List<String> getTeamNameFromUserId(Long userId);

    String getDefaultBranchCodeFromUserId(Long userId);

    List<String> getBranchCodeWhereUserIsBranchAdmin(Long userId);
    
    public User getUserByUri(String userUri);
   
    List<User> getUserReferenceByUsername(String username);

	List<Map<String, Object>> getUserIdAndApprovalStatusByUUID(String uuid);
	/**
	 * @return List<User>
	 */
	List<User> getAllActiveAndInactiveUser();

    Boolean userHasAuthority(String authCode, Long userId);

    String getUserSourceSystemByUsername(String username);

    IPAddressRange getUserProfileIPAddressRange(Long userId);
    public Integer getUsersCountByProductName(String productName,String uUID) ;
    public List<String> getPasswordExpireInDays();


	/**
	 * get user by user name,it will first look into cache if not found then get it from db.
	 * @param username
	 * @param getApprovedUsers if true then it will all users, else will return approved users
	 * @return
	 */
	public User findUserByUsername(String username, Boolean getApprovedUsers);


	/**
	 * get userInfo by user name,it will first look into cache if not found then get it from db.
	 * @param username
	 * @param getApprovedUsers
	 * @return List of approved/all users
	 */
	public List<User> getUserReferenceByUsername(String username, Boolean isApprovedUsers);

	UserProfile getUserProfileByUserId(Long userId);
	 List<String> getProductListFromRoleIds(List<Long> roleIds);
	 
	 public void markReasonForSystem(User user ,String blockReasonCode);
	 
	public String showUnblockLink(String username);
	
	public BlockReason getUserBlockReasonByUsername(String username);


	public Date parseDate(String text) throws ParseException;

    public List<IpAddress> getIPAddress(String ipAddress,AccessType accessType);
    
    public Boolean getUserLoginTimeValid(UserInfo userInfo);
	
	public Boolean customLoginTimeValidator(UserInfo user, DateTime dateTime);
	
	public List<Authority> getAuthorityByCodeFromDb(String authCode);
	
	public UserProfile getUserProfileByUserIdFromDb(Long userId);
	
	public List<User> getAllUsersByUserName(String username);

    MenuEntity getMappedDefaultMenu(Long userId, Long sourceProductId);

    String returnLandingUrlFromMenu(MenuEntity menuEntity, HttpServletRequest request);
    
    public boolean isBotUser(String userName);
    
    public boolean isUserValidForPasswordReset(User user);

    public BlockReason getCurrentBlockReason(Long userId);

    public List<BlockReason> getHighPriorityBlockReasonExist(Long userId, String parentCodeFilter);

    public boolean checkPriorityOfBlockReason(Long userId, String parentCodeFilter,BlockReason blockReason);

    public int getUserStatusByUserId(Long userId);

    public  List<Long> getUserStatusCountByUserId(List<Long> userIdList);
}
