/**
 * This file and a proportion of its content is copyright of Nucleus Software Exports Limited - � 2012. All rights reserved.
 * Any redistribution or reproduction of part or all of the contents in any form is prohibited other than the following:
 * - you cannot print or download to a local hard disk extract contents either part or full for personal/ commercial/
 * academic or any other use
 * - you may not copy the content to individual/ third parties for any type of use, either as compiled or source format
 * without the knowledge and consent of Nucleus SOftware
 * - You may not, except with our express written permission, distribute or commercially exploit the content. Nor may you
 * transmit it or store it in any other web site or other form of electronic retrieval system.
 */
package com.nucleus.businessmapping.service;

import java.util.List;
import java.util.Map;

import com.nucleus.businessmapping.entity.UserOrgBranchMapping;
import com.nucleus.core.organization.entity.OrganizationBranch;
import com.nucleus.entity.EntityId;
import com.nucleus.reason.ReasonVO;
import com.nucleus.service.BaseService;
import com.nucleus.user.User;
import com.nucleus.user.UserAuditLog;
import com.nucleus.user.UserCalendar;
import com.nucleus.user.UserInfo;
import com.nucleus.user.UserProfile;


public interface UserManagementServiceCore extends BaseService {

    String USER_TO_ORGANIZATION    = "userToOrg";

    String USER_TO_ORG_TO_PRODUCTS = "userToOrgToProducts";

    public List<UserOrgBranchMapping> saveBusinessMappingList(List<UserOrgBranchMapping> casBusinessMappingList);

    public void saveUserManagementConfigurations(UserProfile userDetails);

    public List<Object> getAllUsers();

    public List<OrganizationBranch> getUserOrgBranches(Long userID, String systemName);

    /**
     * find User with the UUID
     *
     * @param UUID
     * @return
     */
    public Map<String, Object> findUserByUUID(String uuid);

    public Map<String, Object> findUserById(Long userId);

    public List<Object> getUsersInBranch(String branchId);

    public List<User> findUser(Map<String, Object> queryMap);

    public List<UserOrgBranchMapping> addUsersToBranches(List<Long> userId, List<Long> branchId, boolean assignUsersToChild,
                                                         String systemName);

    public boolean deleteUserOrgBranchMapping(List<UserOrgBranchMapping> userOrgBranchMappingsList);

    public Long blockUser(String userid, EntityId byUserEntityId);

    public List<UserOrgBranchMapping> getUserInBranch(Long branchId);

    public void activateBlockedUsers();

    public String updateUserPassword(UserInfo userInfo, String oldPassword, String newPassword, String token,
                                     Boolean forceResetEnable, Boolean licenseAccepted);

    public String forceResetPassword(String uuid);

    public String sendNotificationToUser(String uuid, String message);

    public void saveBranchesToUser(User user, List<Long> selectedBranchIds, List<Long> branchAdminIds, Long defaultBranch);



    public List<UserOrgBranchMapping> getUserOrgBranchMapping(Long userID);

    public List<OrganizationBranch> getUserPrimaryBranch(Long userID);

    public List<OrganizationBranch> getUserOrganizationBranchObject(Long userID);

    public UserProfile copyUserProfile(UserProfile userProfile, UserProfile formUserProfile);

    public OrganizationBranch getUserPrimaryOrganizationBranch(Long userId) ;

    public OrganizationBranch getBPPrimaryOrganizationBranch(Long bpId) ;

    public Map<OrganizationBranch, Long> getOrgBranchesWithChildCountUnderCurrentUser(Long userId, String systemName);

    public UserOrgBranchMapping getUserBranchMappingForPrimaryBranch(Long userId);

    public List<UserOrgBranchMapping> getUserOrgBranchMappingsForBranches(List<Long> orgBranchIds, Long userId);

    public User getUserByBusinessPartnerId(Long bpId);

    public OrganizationBranch getBPPrimaryOrganizationBranchWithoutSysBranchCheck(Long bpId);

    Long blockUser(String uuid, EntityId byUserEntityId, Integer daysToBlock);

    public List<Long> getUserOrgBranchesIds(Long userID, String systemName);

    public boolean getBranchAdminFlagForCurrentUser(Long userId);


    public OrganizationBranch getUserprimaryOrganizationBranchBPbyUserId(Long userId) ;

    public List<Object> getAllActiveUsers();

    /**
     * @param userProfile
     * @param formUserProfile
     * @return
     */
    UserProfile prepareUserProfileFromExistingUserProfile(UserProfile userProfile, UserProfile formUserProfile);

    Map<Long, OrganizationBranch> getUserOrgBranchMappings(Long userId, String systemName);

    List<UserOrgBranchMapping> getUserOrgBranchMappingList(Long userID);

    List<Map<String, ?>> getUserOrganizationBranch(Long userID);
    
    Map<OrganizationBranch, Long> getOrgBranchesWithChildCountUnderCurrentUserByOrganizationType(Long userId,
            String systemName,String organizationType);


    Map<OrganizationBranch, Long> getOrgBranchesWithChildCountUnderCurrentUserByOrganizationTypeWithState(Long userId,
                                                                                                 String systemName,String organizationType, String stateName);

    void notifyNewUser(String link, User user);

    Map<String,String> getAllReasons();

    ReasonVO getReasonByUserId(Long userId);

    ReasonVO getBlockInactiveReasonByUserId(Long userId);

    UserAuditLog updateReason(UserAuditLog userAuditLog, Long blockReasonId, String type);
    
    UserCalendar getUserCalendarByUserId(Long userId);

   public List<UserAuditLog> getUserAuditLogListByUserId(Long userId);

    List<Long> getOrgBranchesWithCurrentUserByOrganizationType(Long userId,String organizationType);

	String validatePassowrd(String passwordEntered, String username, String passwordPattern);
}
