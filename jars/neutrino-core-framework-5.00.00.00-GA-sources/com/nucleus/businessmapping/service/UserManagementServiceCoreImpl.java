/**
 * This file and a proportion of its content is copyright of Nucleus Software Exports Limited - ï¿½ 2012. All rights reserved.
 * Any redistribution or reproduction of part or all of the contents in any form is prohibited other than the following:
 * - you cannot print or download to a local hard disk extract contents either part or full for personal/ commercial/
 * academic or any other use
 * - you may not copy the content to individual/ third parties for any type of use, either as compiled or source format
 * without the knowledge and consent of Nucleus SOftware
 * - You may not, except with our express written permission, distribute or commercially exploit the content. Nor may you
 * transmit it or store it in any other web site or other form of electronic retrieval system.
 */
package com.nucleus.businessmapping.service;

import com.nucleus.address.Address;
import com.nucleus.address.AddressInitializer;
import com.nucleus.authenticationToken.AuthenticationTokenService;
import com.nucleus.authenticationToken.PasswordResetToken;
import com.nucleus.businessmapping.entity.UserOrgBranchMapping;
import com.nucleus.businessmapping.entity.UserPasswordHistory;
import com.nucleus.cfi.mail.service.MailMessageIntegrationService;
import com.nucleus.config.persisted.service.ConfigurationService;
import com.nucleus.config.persisted.vo.ConfigurationVO;
import com.nucleus.contact.SimpleContactInfo;
import com.nucleus.core.SelectiveMapping;
import com.nucleus.core.exceptions.InvalidDataException;
import com.nucleus.core.exceptions.SystemException;
import com.nucleus.core.genericparameter.service.GenericParameterService;
import com.nucleus.core.misc.util.DateUtils;
import com.nucleus.core.organization.entity.OrganizationBranch;
import com.nucleus.core.organization.entity.OrganizationType;
import com.nucleus.core.organization.service.OrganizationService;
import com.nucleus.core.user.event.UserEvent;
import com.nucleus.core.validation.util.NeutrinoValidator;
import com.nucleus.dao.query.CriteriaQueryExecutor;
import com.nucleus.dao.query.JPAQueryExecutor;
import com.nucleus.dao.query.NamedQueryExecutor;
import com.nucleus.entity.ApprovalStatus;
import com.nucleus.entity.EntityId;
import com.nucleus.entity.SystemEntity;
import com.nucleus.event.EventTypes;
import com.nucleus.event.GenericEvent;
import com.nucleus.finnone.pro.base.Message;
import com.nucleus.finnone.pro.general.util.ValidatorUtils;
import com.nucleus.logging.BaseLoggers;
import com.nucleus.mail.MailService;
import com.nucleus.mail.MimeMailMessageBuilder;
import com.nucleus.master.BaseMasterService;
import com.nucleus.password.reset.ResetPasswordEmailHelper;
import com.nucleus.passwordpolicy.PasswordCreationConfigurationFactory;
import com.nucleus.passwordpolicy.PasswordCreationPolicy;
import com.nucleus.passwordpolicy.PasswordPolicyDictWords;
import com.nucleus.passwordpolicy.PasswordPolicySpecChars;
import com.nucleus.passwordpolicy.passwordvalidations.AbstractPasswordValidation;
import com.nucleus.passwordpolicy.service.PasswordValidationService;
import com.nucleus.query.constants.QueryHint;
import com.nucleus.reason.ActiveReason;
import com.nucleus.reason.BlockReason;
import com.nucleus.reason.InactiveReason;
import com.nucleus.reason.ReasonVO;
import com.nucleus.security.core.session.NeutrinoSessionRegistryImpl;
import com.nucleus.service.BaseServiceImpl;
import com.nucleus.systemSetup.service.SystemSetupService;
import com.nucleus.template.TemplateService;
import com.nucleus.user.*;
import net.bull.javamelody.MonitoredWithSpring;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.common.util.RandomValueStringGenerator;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.inject.Inject;
import javax.inject.Named;
import javax.mail.MessagingException;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.security.SecureRandom;
import java.util.*;
import java.util.Map.Entry;

import static com.nucleus.event.EventTypes.USER_PASSWORD_RESET_EVENT;
import static com.nucleus.finnone.pro.general.util.ValidatorUtils.notNull;
import static com.nucleus.user.UserConstants.NEUTRINO_SYSTEM_USER;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

/**
 * @author Nucleus Software Exports Limited
 */
@Named("userManagementServiceCore")
public class UserManagementServiceCoreImpl extends BaseServiceImpl implements UserManagementServiceCore {

	@Inject
    @Named("addressInitializer")
    private AddressInitializer addressInitializer;

    @Inject
    @Named("userManagementDaoCore")
    private UserManagementDao          userManagementDao;

    @Inject
    @Named("organizationService")
    private OrganizationService        organizationService;

    @Inject
    @Named("configurationService")
    private ConfigurationService       configurationService;

    @Inject
    @Named("userService")
    private UserService                userService;

    @Inject
    @Named("resetPasswordEmailHelper")
    private ResetPasswordEmailHelper resetPasswordEmailHelper;

    @Inject
    @Named("authenticationTokenService")
    private AuthenticationTokenService authenticationTokenService;

    @Inject
    @Named("userSessionManagerService")
    private UserSessionManagerService  userSessionManagerService;

    @Inject
    @Named("messageSource")
    protected MessageSource           messageSource;

    @Inject
    @Named("baseMasterService")
    private BaseMasterService          baseMasterService;

    @Inject
    @Named("genericParameterService")
    private GenericParameterService genericParameterService;
    @Inject
	@Named("templateService")
    protected TemplateService templateService;

    @Inject
    @Named("systemSetupService")
    private SystemSetupService systemSetupService;

    @Inject
    @Named("mailService")
    private MailService mailService;

    @Inject
    @Named("mailMessageIntegrationService")
    private MailMessageIntegrationService mailMessageIntegrationService;


    @Inject
    @Named("sessionRegistry")
    private NeutrinoSessionRegistryImpl neutrinoSessionRegistry;


    @Value(value = "#{'${internetchannel.mail.from}'}")
    private String                       from;


    @Value(value = "#{'${core.web.config.SSO.ticketvalidator.url.value}'}")
	private String ssoUrl;
    
    @Inject
    @Named(value="passwordValidationService")
    private PasswordValidationService passwordValidationService;

    
	@Inject
	@Named("neutrinoPasswordEncoder")
	private PasswordEncoder passwordEncoder;


    private static final String        QUERY_FOR_FETCHING_USER_BRANCHES           = "UserManagement.getUserOrgBranches";
    private static final String        QUERY_FOR_FETCHING_USER_BRANCHES_SELECTIVE = "UserManagement.getUserBranches";
    private static final String        QUERY_FOR_FETCHING_USER_BRANCHES_OBJECT    = "UserManagement.getUserBranchesObject";
    private static final String        QUERY_FOR_FETCHING_PRIMARY_BRANCHES        = "User.primaryOrganizationBranchbyUserId";
    private static final String        QUERY_FOR_FETCHING_APPROVED_USER_BRANCHES  = "UserManagement.getApprovedUserOrgBranches";
    private static final String        QUERY_FOR_FETCHING_USER_AUDIT_LOGS         = "User.getUserAuditLogOfUserWithReason";
    private static final String        QUERY_FOR_FETCHING_USER_CALENDAR         = "User.getUserCalendarById";
    private static final String        QUERY_FOR_FETCHING_DAYS_TO_BLOCK_OF_USER   = "User.getDaysToBlockByUserId";
    private static final String         PASSWORD_FOR_LDAP_USERS =  "Please use your domain password";
    private static final String           configErrorCode       = "msg.8000001";

    private final String  pswdKey = "password_pattren";
    private final String  pswdPatternDescription = "label.password_pattern_description";

    @Override
    @MonitoredWithSpring(name = "UMSCI_FETCH_USR_ORG_BRANCH")
    public List<OrganizationBranch> getUserOrgBranches(Long userID, String systemName) {
        User user = entityDao.find(User.class, userID);
        List<OrganizationBranch> userBranchesList = new ArrayList<OrganizationBranch>();
        Map<Long, OrganizationBranch> userBranchesMap = new HashMap<Long, OrganizationBranch>();

        if (user.getApprovalStatus() == ApprovalStatus.APPROVED
                || user.getApprovalStatus() == ApprovalStatus.APPROVED_MODIFIED) {
            userBranchesMap = getUserOrgBranchMappings(userID, systemName);
        } else {
            User originalUser = (User) baseMasterService.getLastApprovedEntityByUnapprovedEntityId(user.getEntityId());
            if (null != originalUser) {
                userBranchesMap = getUserOrgBranchMappings(originalUser.getId(), systemName);
            }
            List<UserOrgBranchMapping> userOrgBranchMappingList = getUserOrgBranchMappingList(userID);
            for (UserOrgBranchMapping userOrgBranchMapping : userOrgBranchMappingList) {
                if (SelectiveMapping.DELETION_OPERATION.equalsIgnoreCase(userOrgBranchMapping.getOperationType())) {
                    userBranchesMap.remove(userOrgBranchMapping.getOrganizationBranchId());
                } else if (SelectiveMapping.ADDITION_OPERATION.equalsIgnoreCase(userOrgBranchMapping.getOperationType())) {
                    if (userOrgBranchMapping.isIncludesSubBranches()) {
                        userBranchesMap.put(userOrgBranchMapping.getOrganizationBranchId(),
                                userOrgBranchMapping.getOrganizationBranch());
                        List<OrganizationBranch> organizationBranchList = organizationService.getAllChildBranches(
                                userOrgBranchMapping.getOrganizationBranch().getId(), systemName);
                        for (OrganizationBranch organizationBranch : organizationBranchList) {
                            userBranchesMap.put(organizationBranch.getId(), organizationBranch);
                        }
                    } else {
                        userBranchesMap.put(userOrgBranchMapping.getOrganizationBranchId(),
                                userOrgBranchMapping.getOrganizationBranch());
                    }
                }
            }
        }

        for (Entry<Long, OrganizationBranch> entry : userBranchesMap.entrySet()) {
            userBranchesList.add(entry.getValue());
        }
        return userBranchesList;
    }

    /**
     * @param userId
     * @param systemName
     * @return
     */
    @Override
    public Map<Long, OrganizationBranch> getUserOrgBranchMappings(Long userId, String systemName) {
        Map<Long, OrganizationBranch> userBranchMap = new HashMap<Long, OrganizationBranch>();
        List<UserOrgBranchMapping> userOrgBranchMappingList = getUserOrgBranchMappingList(userId);
        for (UserOrgBranchMapping userOrgBranchMapping : userOrgBranchMappingList) {
            if (userOrgBranchMapping.isIncludesSubBranches()) {
                userBranchMap.put(userOrgBranchMapping.getOrganizationBranchId(),
                        userOrgBranchMapping.getOrganizationBranch());
                List<OrganizationBranch> organizationBranchList = organizationService.getAllChildBranches(
                        userOrgBranchMapping.getOrganizationBranch().getId(), systemName);
                for (OrganizationBranch organizationBranch : organizationBranchList) {
                    userBranchMap.put(organizationBranch.getId(), organizationBranch);
                }
            } else {
                userBranchMap.put(userOrgBranchMapping.getOrganizationBranch().getId(),
                        userOrgBranchMapping.getOrganizationBranch());
            }
        }

        return userBranchMap;
    }

    @Override
    public List<UserOrgBranchMapping> getUserOrgBranchMappingList(Long userID) {
        NamedQueryExecutor<UserOrgBranchMapping> executorUserOrgBranchMappings = new NamedQueryExecutor<UserOrgBranchMapping>(
                QUERY_FOR_FETCHING_APPROVED_USER_BRANCHES).addParameter("userID", userID).addQueryHint(
                QueryHint.QUERY_HINT_CACHEABLE, Boolean.TRUE);
        List<UserOrgBranchMapping> userOrgBranchMappingList = entityDao.executeQuery(executorUserOrgBranchMappings);
        return userOrgBranchMappingList;
    }

    @Override
    public List<Long> getUserOrgBranchesIds(Long userID, String systemName) {
        List<UserOrgBranchMapping> userOrgBranchMappingList = getUserOrgBranchMappingList(userID);
        List<Long> userBranchesListIds = new ArrayList<Long>();
        for (UserOrgBranchMapping userOrgBranchMapping : userOrgBranchMappingList) {
            if (userOrgBranchMapping.isIncludesSubBranches()) {
                userBranchesListIds.add(userOrgBranchMapping.getOrganizationBranch().getId());
                List<OrganizationBranch> organizationBranchList = organizationService.getAllChildBranches(
                        userOrgBranchMapping.getOrganizationBranch().getId(), systemName);
                if (CollectionUtils.isNotEmpty(organizationBranchList)) {
                    Iterator<OrganizationBranch> itr = organizationBranchList.iterator();
                    while (itr.hasNext()) {
                        OrganizationBranch childBranch = itr.next();
                        userBranchesListIds.add(childBranch.getId());
                    }
                }
            } else {
                userBranchesListIds.add(userOrgBranchMapping.getOrganizationBranch().getId());
            }
        }
        return userBranchesListIds;
    }

    @Override
    public List<UserOrgBranchMapping> getUserOrgBranchMapping(Long userID) {
        NamedQueryExecutor<UserOrgBranchMapping> executorUserOrgBranchMappings = new NamedQueryExecutor<UserOrgBranchMapping>(
                QUERY_FOR_FETCHING_USER_BRANCHES).addParameter("userID", userID);

        List<UserOrgBranchMapping> userOrgBranchMappingList = entityDao.executeQuery(executorUserOrgBranchMappings);
        return userOrgBranchMappingList;
    }

    @Override
    public List<UserOrgBranchMapping> saveBusinessMappingList(List<UserOrgBranchMapping> casBusinessMappingList) {
        for (UserOrgBranchMapping casBusinessMapping : casBusinessMappingList) {
            if (casBusinessMapping.getId() == null) {
                entityDao.persist(casBusinessMapping);
            } else {
                entityDao.update(casBusinessMapping);
            }
        }
        return casBusinessMappingList;
    }

    @Override
    public List<Object> getAllUsers() {
        return userManagementDao.getAllUsers();
    }

    @Override
    @MonitoredWithSpring(name = "UMSCI_FETCH_USR_BY_UUID")
    public Map<String, Object> findUserByUUID(String uuid) {
        Map<String, Object> userManagementMap = new HashMap<String, Object>();
        NamedQueryExecutor<User> userExecutor = new NamedQueryExecutor<User>("Users.userByUUid").addParameter("uuid", uuid)
                .addQueryHint(QueryHint.QUERY_HINT_CACHEABLE, Boolean.TRUE);
        List<User> userList = userManagementDao.executeQuery(userExecutor);

        User user = null;
        if (userList.size() > 0) {
            user = userList.get(0);
            userManagementMap.put("user", user);
            userManagementMap.put("userPreference",
                    configurationService.getFinalUserModifiableConfigurationForEntity(user.getEntityId()));
        }
        return userManagementMap;
    }

    @Override
    public void saveUserManagementConfigurations(UserProfile userDetails) {
        if (userDetails.getId() == null) {
            entityDao.persist(userDetails);
        } else {
            entityDao.update(userDetails);
        }
    }

    @Override
    public List<Object> getUsersInBranch(String branchId) {
        return userManagementDao.getAllUsersInBranch(branchId);
    }

    @Override
    public List<UserOrgBranchMapping> addUsersToBranches(List<Long> userId, List<Long> branchId, boolean assignUsersToChild,
                                                         String systemName) {
        List<UserOrgBranchMapping> userOrgBranchMappingList = new ArrayList<UserOrgBranchMapping>();
        List<Long> branchIdList = new ArrayList<Long>(branchId);
        if (assignUsersToChild) {
            for (Long branch : branchId) {
                List<OrganizationBranch> children = organizationService.getAllChildBranches(branch, systemName);
                for (OrganizationBranch child : children) {
                    branchIdList.add(child.getId());
                }
            }
        }

        for (Long branch : branchIdList) {
            for (Long user : userId) {
                UserOrgBranchMapping userOrgBranchMapping = new UserOrgBranchMapping();
                OrganizationBranch orgBranch = new OrganizationBranch();
                User userToAdd = new User(user);
                userOrgBranchMapping.setAssociatedUser(userToAdd);
                orgBranch.setId(branch);
                userOrgBranchMapping.setOrganizationBranch(orgBranch);
                userOrgBranchMappingList.add(userOrgBranchMapping);
            }
        }
        return userOrgBranchMappingList;
    }

    @Override
    public List<User> findUser(Map<String, Object> queryMap) {
        CriteriaQueryExecutor<User> criteriaQueryExecutor = new CriteriaQueryExecutor<User>(User.class);
        for (Map.Entry<String, Object> entry : queryMap.entrySet()) {
            if (entry.getValue().toString().matches("[0-9]+")) {
                criteriaQueryExecutor.addOrClause(entry.getKey(), CriteriaQueryExecutor.LIKE_OPERATOR,
                        Long.parseLong(entry.getValue().toString()));
            } else {
                criteriaQueryExecutor.addOrClause(entry.getKey(), CriteriaQueryExecutor.LIKE_OPERATOR, entry.getValue());
            }
        }
        List<User> userList = userManagementDao.executeQuery(criteriaQueryExecutor);
        return userList;
    }

    @Override
    public Long blockUser(String uid, EntityId byUserEntityId) {
        Map<String, Object> userMap = findUserById(Long.parseLong(uid));
        User user = (User) userMap.get("user");

        userService.lockUser(user, byUserEntityId);

        return user.getId();
    }

    @Override
    public Long blockUser(String uid, EntityId byUserEntityId, Integer daysToBlock) {
        Map<String, Object> userMap = findUserById(Long.parseLong(uid));
        User user = (User) userMap.get("user");
        if(notNull(user) && isNotBlank(user.getUsername()) && !(user.getUsername().equalsIgnoreCase(NEUTRINO_SYSTEM_USER))){
            userService.lockUser(user, byUserEntityId, daysToBlock);
            return user.getId();
        }


        return null;
    }

    @Override
    public boolean deleteUserOrgBranchMapping(List<UserOrgBranchMapping> userOrgBranchMappingsList) {
        boolean result = false;
        for (UserOrgBranchMapping userOrgBranchMapping : userOrgBranchMappingsList) {
            if (userOrgBranchMapping.getId() == null) {
                throw new InvalidDataException("The 'user org branch mapping ID' in the delete request is null");
            }
            userManagementDao.delete(userOrgBranchMapping);
            result = true;
        }
        return result;
    }

    @Override
    public List<UserOrgBranchMapping> getUserInBranch(Long organizationBranch) {
        NamedQueryExecutor<UserOrgBranchMapping> userExecutor = new NamedQueryExecutor<UserOrgBranchMapping>(
                "Users.userInBranch").addParameter("organizationBranch", organizationBranch);
        List<UserOrgBranchMapping> userOrgBranchMappingList = userManagementDao.executeQuery(userExecutor);
        return userOrgBranchMappingList;
    }

    @Override
    public void activateBlockedUsers() {
        NamedQueryExecutor<User> userExecutor = new NamedQueryExecutor<User>("Users.getAllUsersByUserStatus").addParameter(
                "userStatus", UserStatus.STATUS_LOCKED)
        		.addParameter("approvalStatusList", ApprovalStatus.APPROVED_RECORD_STATUS_IN_APPROVED_APPROVED_MODIFIED)
        		.addQueryHint(QueryHint.QUERY_HINT_CACHEABLE, Boolean.TRUE);
        ;
        List<User> blockedUsers = userManagementDao.executeQuery(userExecutor);
        if (blockedUsers==null || blockedUsers.isEmpty()) {
            return;
        }
        int intUnlockAfterMinutes = 10;
        try {
            String unlockAfterMinutes = configurationService.getConfigurationPropertyFor(SystemEntity.getSystemEntityId(),
                    "config.userLock.coolOffHours").getText();
            intUnlockAfterMinutes = Integer.parseInt(unlockAfterMinutes);
        } catch (Exception e) {
            BaseLoggers.exceptionLogger.info("Exception occured while retreving config.userLock.coolOffHours "
                    + e.getMessage());
        }
        DateTime now = DateUtils.getCurrentUTCTime();
        if (blockedUsers != null && !blockedUsers.isEmpty()) {
            for (User user : blockedUsers) {
                Boolean unblockUser = true;
                if (user.getUserStatus() == UserStatus.STATUS_LOCKED) {
                    DateTime unblockingTime = user.getLastLockedDate();
                    Integer daysToBlock = user.getDaysToBlock();
                    if (daysToBlock != null && daysToBlock != 0) {
                        unblockingTime = unblockingTime.plusDays(daysToBlock);
                    } else {
                        unblockingTime = unblockingTime.plusMinutes(intUnlockAfterMinutes);
                        ReasonVO reasonVO = getBlockInactiveReasonByUserId(user.getId());
                        if (reasonVO != null) {
                            BlockReason blockReason = genericParameterService.findByCode(reasonVO.getCode(), BlockReason.class);
                            if (blockReason != null && (blockReason.getParentCode().equalsIgnoreCase("UNBLOCK_NO")||blockReason.getParentCode().equalsIgnoreCase(UserConstants.UNBLOCK_MFA_ENABLED))) {
                                unblockUser = false;
                            }
                        }
                    }
                    if (unblockUser && unblockingTime.compareTo(now) <= 0) {
                        userService.activateUser(user);
                    }
                }
            }
        }
    }

    @Override
    public String updateUserPassword(UserInfo userInfo, String oldPassword, String newPassword, String token,
                                     Boolean forceResetEnable, Boolean licenseAccepted) {
        User user = userManagementDao.find(User.class, userInfo.getUserReference().getId());
        Map<String, ConfigurationVO> conf = configurationService.getFinalConfigurationForEntity(SystemEntity.getSystemEntityId());
        int LIMIT_PASS_RESET = Integer.parseInt(conf.get("config.user.allowed.failedPasswordResetAttempts")
                .getPropertyValue());
        String msg = "";
        // Updating password on Change Password
        boolean isTokenValid=false;
        if (StringUtils.isNotBlank(oldPassword) || StringUtils.isNotBlank(token)) {
            String encryptedPswd = "";
            if (StringUtils.isNotBlank(token)) {
                if ((user.getPasswordResetToken().getTokenId().equals(authenticationTokenService.getEncryptedToken(token))
                        || user.getPasswordResetToken().getTokenId().equals(authenticationTokenService.getEncryptedTokenMD5(token)))
                        && user.getPasswordResetToken().getTokenValidity().isAfter(DateTime.now())) {
                    isTokenValid = true;
                    encryptedPswd = user.getPassword();
                } else {
                    return "Token invalid or Expired .";
                }
            } else {
                encryptedPswd = passwordEncoder.encode(oldPassword);
            }
            if(user.isAccountLocked()){
            	return "Your attempts to reset password have failed "
                        + LIMIT_PASS_RESET
                        + " times,"
                        + " hence account has been LOCKED per organization policy. Please contact the administrator.";
            }
            String encryptedPswdNew = passwordEncoder.encode(newPassword);

            int LIMIT = Integer.parseInt(conf.get("config.user.allowed.passwordHistoryCount").getPropertyValue());
            String regExp = messageSource.getMessage(pswdKey, null, getUserLocale());
            String regExpDesc = messageSource.getMessage(pswdPatternDescription, null, getUserLocale());


            // We take from (0)th to (Limit-1)th - This makes last (Limit-1) passwords . And 1 is the current password
            List<UserPasswordHistory> latestPasswordHistories = new ArrayList<UserPasswordHistory>();
            int noOfSavedPassword = user.getUserPasswordHistories().size();

            if (noOfSavedPassword > (LIMIT - 2)) {
                latestPasswordHistories.addAll(user.getUserPasswordHistories().subList(1, LIMIT - 1));
            } else {
                latestPasswordHistories.addAll(user.getUserPasswordHistories());
            }

            List<String> latestPasswords = new ArrayList<String>();
            if (noOfSavedPassword > 0) {
                latestPasswords.add(user.getUserPasswordHistories().get(0).getPassword());
            }
            for (UserPasswordHistory uPH : latestPasswordHistories) {
                latestPasswords.add(uPH.getPassword());
            }
            if (isTokenValid || passwordEncoder.matches(oldPassword, user.getPassword())) {
                if (isPasswordReused(encryptedPswd,latestPasswords,newPassword)) {
                    msg = userService.incrementFailedPassResetCount(user.getId());
                    if (msg.equals("blocked")) {
                        return "New Password matches the recent passwords list .  Your attempts to reset password have failed "
                                + LIMIT_PASS_RESET
                                + " times,"
                                + " hence account has been LOCKED per organization policy. Please contact the administrator.";
                    }
                    return "New Password matches the recent passwords list . Please set another password .";
                } else {
                        String message = this.validatePassowrd(newPassword, userInfo.getUsername(), regExp);
                        if(!message.equals("")) {
                        msg = userService.incrementFailedPassResetCount(user.getId());
                        if (msg.equals("blocked")) {
                            return " Your attempts to reset password have failed "
                                    + LIMIT_PASS_RESET
                                    + " times,"
                                    + " hence account has been LOCKED per organization policy. Please contact the administrator.";
                        }
                        return message;
                        }
                    }

                UserPasswordHistory uPH = new UserPasswordHistory();
                uPH.setPasswordChangedDate(DateTime.now());
                uPH.setPassword(encryptedPswd);

                latestPasswordHistories.add(uPH);

                user.setPassword(encryptedPswdNew);
                user.getUserPasswordHistories().clear();
                user.getUserPasswordHistories().addAll(latestPasswordHistories);
                PasswordResetToken passwordResetToken = user.getPasswordResetToken();
                user.setPasswordResetToken(null);
                user = userService.setUserPasswordExpirationDate(user);
                authenticationTokenService.deleteOldToken(user.getId(), passwordResetToken);
                // this function only updates the user object .
                // entityDao.saveOrUpdate(user);

                UserEvent userEvent = new UserEvent(USER_PASSWORD_RESET_EVENT, true, userInfo.getUserEntityId(), user);
                userEvent.setUserName(user.getUsername());
                userEvent.setAssociatedUser(user.getDisplayName());
                eventBus.fireEvent(userEvent);
                BaseLoggers.flowLogger.debug("Password has been reset successfully by " + user.getDisplayName() + " on "
                        + user.getLastPasswordResetDate());

                userService.resetFailedPassResetCountToZero(user.getId());
                user.setForcePasswordResetOnLogin(forceResetEnable);
                user.setLicenseAccepted(licenseAccepted);
                if(user.getUserStatus() == UserStatus.STATUS_LOCKED && (user.getDaysToBlock() == null || user.getDaysToBlock() == 0)){
                	//Earlier user status was set to active on update password, now the status remains the same as before after updating the password
                   //user.setUserStatus(UserStatus.STATUS_ACTIVE);
                    user.setNumberOfFailedLoginAttempts(0);
                    user.setNumberOfFailedPassResetAttempts(0);
                }
                return "success";
            } else {
                msg = userService.incrementFailedPassResetCount(user.getId());
                if (msg.equals("blocked")) {
                    return "Wrong Old Password .  Your attempts to reset password have failed " + LIMIT_PASS_RESET
                            + " times,"
                            + " hence account has been LOCKED per organization policy. Please contact the administrator.";
                }
                return "Wrong Old Password";
            }
        }
        msg = userService.incrementFailedPassResetCount(user.getId());
        if (msg.equals("blocked")) {
            return "Failed to Reset Password .  Your attempts to reset password have failed " + LIMIT_PASS_RESET + " times,"
                    + " hence account has been LOCKED per organization policy. Please contact the administrator.";
        }
        return "failure";
    }

    private boolean isPasswordReused(String encryptedPswd, List<String> latestPasswords, String newPassword) {
		if(passwordEncoder.matches(newPassword, encryptedPswd)) {
			return true;
		}
		if(latestPasswords!=null &&! latestPasswords.isEmpty()) {
			return latestPasswords.stream()
					.anyMatch(encodedOldPassword-> passwordEncoder.matches(newPassword, encodedOldPassword));
		}
		return false;
	}

	@Override
    public String forceResetPassword(String uid) {
        Map<String, Object> userMap = findUserById(Long.parseLong(uid));
        User user = (User) userMap.get("user");
        if(notNull(user) && isNotBlank(user.getUsername()) && !UserConstants.NEUTRINO_SYSTEM_USER.equalsIgnoreCase(user.getUsername()) ){
            user.setPasswordExpirationDate(DateUtils.getCurrentUTCTime());
            user.setForcePasswordResetOnLogin(true);
            UserEvent userEvent = new UserEvent(USER_PASSWORD_RESET_EVENT, true, getCurrentUser().getUserEntityId(), user);
            userEvent.setUserName(user.getUsername());
            userEvent.setAssociatedUser(getCurrentUser().getDisplayName());
            eventBus.fireEvent(userEvent);
            userService.saveUser(user);
            userSessionManagerService.invalidateUserSession(user.getId());
            Map<String, ConfigurationVO> conf = configurationService.getFinalConfigurationForEntity(SystemEntity.getSystemEntityId());
            String tokenValidityTimeInMillis = conf.get("config.user.passwordResetToken.duration").getPropertyValue();
            String token = authenticationTokenService.generatePasswordResetTokenForUser(user, tokenValidityTimeInMillis);
            if (StringUtils.isNotBlank(token) && user.getPasswordResetToken() != null
                    && user.getPasswordResetToken().getTokenId() != null) {
                return token;
            }
            return "success";
        }
        return "error";
    }

    @Override
    public String sendNotificationToUser(String uid, String message) {

        Map<String, Object> userMap = findUserById(Long.parseLong(uid));
        User user = (User) userMap.get("user");
        if(notNull(user) && isNotBlank(user.getUsername()) && !UserConstants.NEUTRINO_SYSTEM_USER.equalsIgnoreCase(user.getUsername()) ){
            String usersToNotify = user.getUri();
            GenericEvent event = new GenericEvent(EventTypes.USER_ADMIN_SEND_NOTIFICATION);
            event.addPersistentProperty("ADMIN_NOTIFICATION", message);
            event.addNonWatcherToNotify(usersToNotify);
            event.addPersistentProperty(GenericEvent.SUCCESS_FLAG, "success");
            eventBus.fireEvent(event);
            return "success";
        }

        return "error";
    }

    @SuppressWarnings("unchecked")
    @Override
    public void saveBranchesToUser(User user, List<Long> branchList, List<Long> branchAdminIds, Long defaultBranch) {

        NeutrinoValidator.notNull(branchList);
        NeutrinoValidator.notNull(user);
        List<UserOrgBranchMapping> userOrgBranchMappings = getUserOrgBranchMapping(user.getId());
        for (UserOrgBranchMapping orgBranchMapping : userOrgBranchMappings) {
            // orgBranchMapping.getLoanProductList().removeAll(orgBranchMapping.getLoanProductList());
            userManagementDao.delete(orgBranchMapping);
        }

        /*It may happen that user unchecks a branch but its "branch admin mapping" is still there. So removing all such admin branches */
        if (branchAdminIds != null && branchAdminIds.size() > 0 && branchList.size() > 0) {
            branchAdminIds = (List<Long>) CollectionUtils.intersection(branchAdminIds, branchList);
        }

        for (Long branchKey : branchList) {

            OrganizationBranch branch = new OrganizationBranch();
            branch.setId(branchKey);

            UserOrgBranchMapping userOrgBrnchMapping = new UserOrgBranchMapping();
            userOrgBrnchMapping.setAssociatedUser(user);
            userOrgBrnchMapping.setOrganizationBranch(branch);
            userOrgBrnchMapping.setApprovalStatus(ApprovalStatus.APPROVED);
            /*  List<LoanProduct> productList = new ArrayList<LoanProduct>();
              for (String productId : branchPrpoductMap.get(key)) {
                  LoanProduct product = entityDao.find(LoanProduct.class, Long.parseLong(productId));
                  product.setId(Long.parseLong(productId));
                  productList.add(product);
              }
              userOrgBrnchMapping.setLoanProductList(productList);*/
            if (branch.getId().equals(defaultBranch)) {
                userOrgBrnchMapping.setPrimaryBranch(true);
            }
            if (branchAdminIds != null && branchAdminIds.size() > 0) {
                if (branchAdminIds.contains(branch.getId())) {
                    userOrgBrnchMapping.setBranchAdmin(true);
                }
            }
            userManagementDao.persist(userOrgBrnchMapping);

        }

    }



    @Override
    public List<OrganizationBranch> getUserPrimaryBranch(Long userID) {
        NamedQueryExecutor<OrganizationBranch> userPrimaryBranch = new NamedQueryExecutor<OrganizationBranch>(
                QUERY_FOR_FETCHING_PRIMARY_BRANCHES).addParameter("associatedUserId", userID);

        List<OrganizationBranch> userPrimaryBranchMapping = entityDao.executeQuery(userPrimaryBranch);
        return userPrimaryBranchMapping;
    }

    @Override
    public List<Map<String, ?>> getUserOrganizationBranch(Long userID) {
        NamedQueryExecutor<Map<String, ?>> userOrganizationBranch = new NamedQueryExecutor<Map<String, ?>>(
                QUERY_FOR_FETCHING_USER_BRANCHES_SELECTIVE).addParameter("userID", userID).addParameter("orgType",
                OrganizationType.ORGANIZATION_TYPE_BRANCH);

        List<Map<String, ?>> userOrgBranchList = entityDao.executeQuery(userOrganizationBranch);
        return userOrgBranchList;
    }

    @Override
    public List<OrganizationBranch> getUserOrganizationBranchObject(Long userID) {
        NamedQueryExecutor<OrganizationBranch> userOrganizationBranch = new NamedQueryExecutor<OrganizationBranch>(
                QUERY_FOR_FETCHING_USER_BRANCHES_OBJECT).addParameter("userID", userID).addParameter("orgType",
                OrganizationType.ORGANIZATION_TYPE_BRANCH);

        List<OrganizationBranch> userOrgBranchList = entityDao.executeQuery(userOrganizationBranch);
        return userOrgBranchList;
    }

    @Override
    public UserProfile copyUserProfile(UserProfile userProfile, UserProfile formUserProfile) {

        if (userProfile == null) {
            userProfile = new UserProfile();
        }
        prepareUserProfile(userProfile, formUserProfile);
        return userProfile;
    }

    @Override
    public UserProfile prepareUserProfileFromExistingUserProfile(UserProfile userProfile, UserProfile formUserProfile) {

        if (userProfile == null) {
            userProfile = new UserProfile();
        } else {
        	if(userProfile.getSimpleContactInfo() != null) {
        		if(userProfile.getSimpleContactInfo().getPhoneNumber() != null) {
        			userProfile.getSimpleContactInfo().getPhoneNumber().getId();		
        		}
        		if(userProfile.getSimpleContactInfo().getMobileNumber() != null) {
        			userProfile.getSimpleContactInfo().getMobileNumber().getId();		
        		}
        		if(userProfile.getSimpleContactInfo().getEmail() != null) {
        			userProfile.getSimpleContactInfo().getEmail().getId();		
        		}
        		if(userProfile.getSimpleContactInfo().getAddress() != null) {
        			addressInitializer.initialize(userProfile.getSimpleContactInfo().getAddress(), AddressInitializer.AddressLazyAttributes.ALL);		
        		}
        	}
        	entityDao.detach(userProfile);
        }

        // before return clean ids from attached entities
        if (null != userProfile.getId()) {
            userProfile.clearId();
            if (null != userProfile.getSimpleContactInfo() && null != userProfile.getSimpleContactInfo().getId()) {

                if (null != userProfile.getSimpleContactInfo().getAddress()
                        && null != userProfile.getSimpleContactInfo().getAddress().getId()) {
                	
                    entityDao.detach(userProfile.getSimpleContactInfo().getAddress());
                    userProfile.getSimpleContactInfo().getAddress().clearId();
                }
                if (null != userProfile.getSimpleContactInfo().getEmail()
                        && null != userProfile.getSimpleContactInfo().getEmail().getId()) {
                	
                    entityDao.detach(userProfile.getSimpleContactInfo().getEmail());
                    userProfile.getSimpleContactInfo().getEmail().clearId();
                }
                if (null != userProfile.getSimpleContactInfo().getMobileNumber()
                        && null != userProfile.getSimpleContactInfo().getMobileNumber().getId()) {
                	
                    entityDao.detach(userProfile.getSimpleContactInfo().getMobileNumber());
                    userProfile.getSimpleContactInfo().getMobileNumber().clearId();
                }
                if (null != userProfile.getSimpleContactInfo().getPhoneNumber()
                        && null != userProfile.getSimpleContactInfo().getPhoneNumber().getId()) {
                	
                    entityDao.detach(userProfile.getSimpleContactInfo().getPhoneNumber());
                    userProfile.getSimpleContactInfo().getPhoneNumber().clearId();
                }
                
                entityDao.detach(userProfile.getSimpleContactInfo());
                userProfile.getSimpleContactInfo().clearId();
            }

            prepareUserProfile(userProfile, formUserProfile);
        }

        return userProfile;
    }

    private UserProfile prepareUserProfile(UserProfile userProfile, UserProfile formUserProfile) {
        userProfile.setPhotoUrl(formUserProfile.getPhotoUrl());
        if (formUserProfile.getSimpleContactInfo() != null) {
            SimpleContactInfo contactInfo = userProfile.getSimpleContactInfo();
            if (contactInfo == null) {
                contactInfo = new SimpleContactInfo();
            }
            Address formUserAddress = formUserProfile.getSimpleContactInfo().getAddress();
            if (formUserAddress != null) {
                // Manage Detached Address
                if (userProfile != null && userProfile.getSimpleContactInfo() != null
                        && userProfile.getSimpleContactInfo().getAddress() != null) {
                    Address address = userProfile.getSimpleContactInfo().getAddress();
                    address.setAccomodationType(formUserAddress.getAccomodationType());
                    address.setActiveAddress(formUserAddress.isActiveAddress());
                    address.setAdditionalInfo(formUserAddress.getAdditionalInfo());
                    address.setAddressLine1(formUserAddress.getAddressLine1());
                    address.setAddressLine2(formUserAddress.getAddressLine2());
                    address.setAddressLine3(formUserAddress.getAddressLine3());
                    address.setAddressLine4(formUserAddress.getAddressLine4());
                    address.setAddressType(formUserAddress.getAddressType());
                    address.setArea(formUserAddress.getArea());
                    updateCityFiledForAddress(formUserAddress, address);
                    address.setCountry(formUserAddress.getCountry());
                    address.setDistrict(formUserAddress.getDistrict());
                    address.setExpressionId(formUserAddress.getExpressionId());
                    address.setLandMark(formUserAddress.getLandMark());
                    address.setLatitude(formUserAddress.getLatitude());
                    address.setLongitude(formUserAddress.getLongitude());
                    address.setMonthsInCurrentCity(formUserAddress.getMonthsInCurrentCity());
                    address.setNumberOfMonthsAtAddress(formUserAddress.getNumberOfMonthsAtAddress());
                    address.setNumberOfYearsAtAddress(formUserAddress.getNumberOfYearsAtAddress());
                    address.setOccupancyEndDate(formUserAddress.getOccupancyEndDate());
                    address.setOccupancyStartDate(formUserAddress.getOccupancyStartDate());
                    address.setOtherResidenceType(formUserAddress.getOtherResidenceType());
                    address.setPhoneNumberList(formUserAddress.getPhoneNumberList());
                    address.setPrimaryAddress(formUserAddress.isPrimaryAddress());
                    address.setRegion(formUserAddress.getRegion());
                    address.setResidenceType(formUserAddress.getResidenceType());
                    address.setSameAsAddress(formUserAddress.getSameAsAddress());
                    address.setSendParcel(formUserAddress.isSendParcel());
                    address.setState(formUserAddress.getState());
                    address.setTaluka(formUserAddress.getTaluka());
                    address.setVillage(formUserAddress.getVillage());
                    address.setYearsInCurrentCity(formUserAddress.getYearsInCurrentCity());
                    address.setZipcode(formUserAddress.getZipcode());
                    address.setPoBox(formUserAddress.getPoBox());
                    address.setStreet(formUserAddress.getStreet());
                    address.setCustomPincodeValue(formUserAddress.getCustomPincodeValue());
                    address.setStreetMaster(formUserAddress.getStreetMaster());
                    address.setAdditionalDropdownField1(formUserAddress.getAdditionalDropdownField1());
                    address.setAdditionalDropdownField2(formUserAddress.getAdditionalDropdownField2());
                    formUserAddress = address;
                }

                /**To remove the mandatory check for address for a User**/
                if (formUserAddress.getCountry() != null && formUserAddress.getCountry().getId() != null) {
                    contactInfo.setAddress(formUserAddress);
                } else {
                    contactInfo.setAddress(null);
                }
                if (contactInfo.getAddress() != null) {
                    if (contactInfo.getAddress().getArea() == null || contactInfo.getAddress().getArea().getId() == null) {
                        contactInfo.getAddress().setArea(null);
                    }
                    if (contactInfo.getAddress().getDistrict() == null
                            || contactInfo.getAddress().getDistrict().getId() == null) {
                        contactInfo.getAddress().setDistrict(null);
                    }

                    if (contactInfo.getAddress().getRegion() == null || contactInfo.getAddress().getRegion().getId() == null) {
                        contactInfo.getAddress().setRegion(null);
                    }

                    if (contactInfo.getAddress().getZipcode() == null
                            || contactInfo.getAddress().getZipcode().getId() == null) {
                        contactInfo.getAddress().setZipcode(null);
                    }
                }
            }else{
                contactInfo.setAddress(formUserAddress);
            }
            if (formUserProfile.getSimpleContactInfo() != null) {
                if (formUserProfile.getSimpleContactInfo().getMobileNumber() != null) {
                    contactInfo.setMobileNumber(formUserProfile.getSimpleContactInfo().getMobileNumber());
                }
                if (formUserProfile.getSimpleContactInfo().getPhoneNumber() != null) {
                    contactInfo.setPhoneNumber(formUserProfile.getSimpleContactInfo().getPhoneNumber());
                }
                if (formUserProfile.getSimpleContactInfo().getEmail() != null) {
                    contactInfo.setEmail(formUserProfile.getSimpleContactInfo().getEmail());
                }
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
            userProfile.setUserAccessType(formUserProfile.getUserAccessType());
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

    }

	private void updateCityFiledForAddress(Address formUserAddress, Address address) {
		if(null!= formUserAddress.getCity() && null!=formUserAddress.getCity().getId()) {
			address.setCity(formUserAddress.getCity());
		}else {
			address.setCity(null);
		}
	}

    /**  movement from listener **/

    @Override
    public OrganizationBranch getUserPrimaryOrganizationBranch(Long userId)  {
        NamedQueryExecutor<OrganizationBranch> branchQuery = new NamedQueryExecutor<OrganizationBranch>(
                "User.primaryOrganizationBranchbyUserId").addParameter("associatedUserId", userId).addQueryHint(
                QueryHint.QUERY_HINT_CACHEABLE, Boolean.TRUE);
        return   getPrimaryOrganizationBranch(branchQuery, userId);

    }

    // Added for BP Organization branch

    @Override
    public OrganizationBranch getUserprimaryOrganizationBranchBPbyUserId(Long userId)  {
        NamedQueryExecutor<OrganizationBranch> branchQuery = new NamedQueryExecutor<OrganizationBranch>(
                "User.primaryOrganizationBranchFromBusinessPartnerbyUserId").addParameter("associatedUserId", userId)
                .addQueryHint(QueryHint.QUERY_HINT_CACHEABLE, Boolean.TRUE);
        return   getPrimaryOrganizationBranch(branchQuery, userId);
    }

    @Override
    public OrganizationBranch getBPPrimaryOrganizationBranch(Long bpId)  {
        NamedQueryExecutor<OrganizationBranch> branchQuery = new NamedQueryExecutor<OrganizationBranch>(
                "BPUser.primaryOrganizationBranchbyBPUserId").addParameter("associatedBPUserId", bpId);
        return   getPrimaryOrganizationBranch(branchQuery, bpId);
    }

    @Override
    public OrganizationBranch getBPPrimaryOrganizationBranchWithoutSysBranchCheck(Long bpId) {
        NamedQueryExecutor<OrganizationBranch> branchQuery = new NamedQueryExecutor<OrganizationBranch>(
                "BPUser.primaryOrganizationBranchbyBPUserId").addParameter("associatedBPUserId", bpId);

        return entityDao.executeQueryForSingleValue(branchQuery);
    }

    public OrganizationBranch getPrimaryOrganizationBranch(NamedQueryExecutor<OrganizationBranch> branchQuery, Long userid)
    {

        try {

            List<OrganizationBranch> orgBrachList = entityDao.executeQuery(branchQuery);

            if (orgBrachList == null || orgBrachList.size() != 1) {
                throw new SystemException("User can't have none or multiple primary branches :: Issue with UserId = "
                        + userid);

            } else {
                return orgBrachList.get(0);
            }

        } catch (SystemException exception) {
            BaseLoggers.exceptionLogger.info("Issue with UserId = " + userid + "Exception :: " + exception.getMessage());
            throw exception;
        }
    }

    @Override
    public Map<OrganizationBranch, Long> getOrgBranchesWithChildCountUnderCurrentUser(Long userId, String systemName) {
        NeutrinoValidator.notNull(userId, "User Id cannot be null");
        NamedQueryExecutor<Map<String, Object>> executor = new NamedQueryExecutor<Map<String, Object>>(
                "Organization.getOrgBranchesUnderCurrentUser");
        executor.addParameter("userID", userId);
        executor.addParameter("systemName", systemName);
        List<Map<String, Object>> resultList = entityDao.executeQuery(executor);

        /* Get All OrgBrnaches and there child count */
        Map<OrganizationBranch, Long> orgBranchChildCountMap = null;
        if (resultList != null && resultList.size() > 0) {
            orgBranchChildCountMap = new HashMap<OrganizationBranch, Long>();
            for (Map<String, Object> mapVar : resultList) {
                OrganizationBranch organizationBranch = (OrganizationBranch) mapVar.get("orgBranch");
                if (organizationBranch != null) {
                    orgBranchChildCountMap.put(organizationBranch, (Long) mapVar.get("childCount"));
                }
            }
        }

        return orgBranchChildCountMap;
    }

    @Override
    public UserOrgBranchMapping getUserBranchMappingForPrimaryBranch(Long userId)  {
        NeutrinoValidator.notNull(userId, "User Id cannot be null");



        String queryString = "SELECT u FROM UserOrgBranchMapping u where u.associatedUser.id=:associatedUserId and u.isPrimaryBranch=:isPrimaryBranch";
        JPAQueryExecutor<UserOrgBranchMapping> jpaQueryExecutor = new JPAQueryExecutor<UserOrgBranchMapping>(queryString);
        jpaQueryExecutor.addParameter("associatedUserId", userId)
        				.addParameter("isPrimaryBranch", true);

        List<UserOrgBranchMapping> userOrgPrimaryBranchMappings = entityDao.executeQuery(jpaQueryExecutor);

        UserOrgBranchMapping userOrgBranchMapping = null;
        if (userOrgPrimaryBranchMappings != null && userOrgPrimaryBranchMappings.size() > 0) {
            userOrgBranchMapping = userOrgPrimaryBranchMappings.get(0);
        }

        return userOrgBranchMapping;

    }

    @Override
    public List<UserOrgBranchMapping> getUserOrgBranchMappingsForBranches(List<Long> orgBranchIds, Long userId) {
        NeutrinoValidator.notNull(orgBranchIds, "Organization Branch Ids cannot be null");
        NeutrinoValidator.notNull(userId, "User Id cannot be null");

        List<UserOrgBranchMapping> orgBranchMappings = null;
        if (!orgBranchIds.isEmpty()) {
            NamedQueryExecutor<UserOrgBranchMapping> executor = new NamedQueryExecutor<UserOrgBranchMapping>(
                    "UserManagement.getUserOrgBranchByBranchIdsAndUserId");
            executor.addParameter("branchIDs", orgBranchIds);
            executor.addParameter("userID", userId);
            orgBranchMappings = entityDao.executeQuery(executor);
        }
        return orgBranchMappings;
    }


    @Override
    public User getUserByBusinessPartnerId(Long bpId) {
        User user = null;
        NeutrinoValidator.notNull(bpId);
        if (bpId != null) {
            NamedQueryExecutor<User> bpQuery = new NamedQueryExecutor<User>("UserBPMapping.getUserByBPId").addParameter(
                    "bpId", bpId);
            user = entityDao.executeQueryForSingleValue(bpQuery);
        }
        return user;
    }

    @Override
    public boolean getBranchAdminFlagForCurrentUser(Long userId) {
        NeutrinoValidator.notNull(userId, "User Id Cannot be null");
        boolean userIsBranchAdminFlag = false;
        NamedQueryExecutor<Boolean> executor = new NamedQueryExecutor<Boolean>("UserOrgBranchMapping.getBranchAdmin")
                .addParameter("userId", userId)
                .addParameter("isBranchAdmin",true)
                .addParameter("approvalStatus", ApprovalStatus.APPROVED)
                .addParameter("activeFlag",Boolean.TRUE);
        List<Boolean> isBranchAdminList = entityDao.executeQuery(executor);
        if (CollectionUtils.isNotEmpty(isBranchAdminList) && isBranchAdminList.size() > 0) {
            userIsBranchAdminFlag = true;
        } else {
            userIsBranchAdminFlag = false;
        }
        return userIsBranchAdminFlag;
    }


    @Override
    public Map<String, Object> findUserById(Long userId) {
        Map<String, Object> userManagementMap = new HashMap<String, Object>();
        NamedQueryExecutor<User> userExecutor = new NamedQueryExecutor<User>("Users.userById")
                .addParameter("userId", userId);
        List<User> userList = userManagementDao.executeQuery(userExecutor);

        User user = null;
        if (userList.size() > 0) {
            user = userList.get(0);
            userManagementMap.put("user", user);
            userManagementMap.put("userPreference",
                    configurationService.getFinalUserModifiableConfigurationForEntity(user.getEntityId()));
            userManagementMap.put("userProfile", userService.getUserProfile(user));
        }
        return userManagementMap;
    }

    @Override
    public List<Object> getAllActiveUsers() {
        return userManagementDao.getAllActiveUsers();
    }

    @Override
    public Map<OrganizationBranch, Long> getOrgBranchesWithChildCountUnderCurrentUserByOrganizationType(Long userId,
                                                                                                        String systemName,String organizationType) {
        NeutrinoValidator.notNull(userId, "User Id cannot be null");
        NamedQueryExecutor<Map<String, Object>> executor = new NamedQueryExecutor<Map<String, Object>>(
                "Organization.getOrgBranchesUnderCurrentUserByOrganizationType");
        executor.addParameter("userID", userId);
        executor.addParameter("systemName", systemName);
        executor.addParameter("orgType",OrganizationType.ORGANIZATION_TYPE_BRANCH);
        List<Map<String, Object>> resultList = entityDao.executeQuery(executor);

        /* Get All OrgBrnaches and there child count */
        Map<OrganizationBranch, Long> orgBranchChildCountMap = new HashMap<OrganizationBranch, Long>();
        if (ValidatorUtils.hasElements(resultList)) {

            for (Map<String, Object> mapVar : resultList) {
                OrganizationBranch organizationBranch = (OrganizationBranch) mapVar.get("orgBranch");
                if (organizationBranch != null) {
                    orgBranchChildCountMap.put(organizationBranch, (Long) mapVar.get("childCount"));
                }
            }
        }

        return orgBranchChildCountMap;
    }
    
    
    
    @Override
    public Map<OrganizationBranch, Long> getOrgBranchesWithChildCountUnderCurrentUserByOrganizationTypeWithState(Long userId,
                                                                                                        String systemName,String organizationType, String stateName) {
        NeutrinoValidator.notNull(userId, "User Id cannot be null");
        NamedQueryExecutor<Map<String, Object>> executor = new NamedQueryExecutor<Map<String, Object>>(
                "Organization.getOrgBranchesUnderCurrentUserByOrganizationTypeWithState");
        executor.addParameter("userID", userId);
        executor.addParameter("systemName", systemName);
        executor.addParameter("orgType",OrganizationType.ORGANIZATION_TYPE_BRANCH);
        executor.addParameter("stateName", stateName);
        List<Map<String, Object>> resultList = entityDao.executeQuery(executor);

        /* Get All OrgBrnaches and there child count */
        Map<OrganizationBranch, Long> orgBranchChildCountMap = new HashMap<OrganizationBranch, Long>();
        if (ValidatorUtils.hasElements(resultList)) {

            for (Map<String, Object> mapVar : resultList) {
                OrganizationBranch organizationBranch = (OrganizationBranch) mapVar.get("orgBranch");
                if (organizationBranch != null) {
                    orgBranchChildCountMap.put(organizationBranch, (Long) mapVar.get("childCount"));
                }
            }
        }

        return orgBranchChildCountMap;
    }
    
    

    @Override
    public Map<String,String> getAllReasons(){
        List<BlockReason> blockReasonList = genericParameterService.retrieveTypes(BlockReason.class);
        List<InactiveReason> inactiveReasonList = genericParameterService.retrieveTypes(InactiveReason.class);
        List<ActiveReason> activeReasonList = genericParameterService.retrieveTypes(ActiveReason.class);

        Map<String,String> reasonMap = new HashMap<String,String>();
        if(null != blockReasonList){
            for(BlockReason eachReason : blockReasonList)
                reasonMap.put(eachReason.getCode(),eachReason.getDescription());
        }

        if(null != inactiveReasonList){
            for(InactiveReason eachReason : inactiveReasonList)
                reasonMap.put(eachReason.getCode(),eachReason.getDescription());
        }

        if(null != activeReasonList){
            for(ActiveReason eachReason : activeReasonList)
                reasonMap.put(eachReason.getCode(),eachReason.getDescription());
        }
        return reasonMap;
    }

    @Override
    public ReasonVO getReasonByUserId(Long userId){

        List<UserAuditLog> userAuditList = getUserAuditLogListByUserId(userId);
        ReasonVO reasonVO = new ReasonVO();
        if(userAuditList!=null && userAuditList.size()>0){
            UserAuditLog userAuditLog = userAuditList.get(0);
            reasonVO.setRemarks(userAuditLog.getReasonRemarks());
            if(null != userAuditLog.getBlockReason()){
                reasonVO.setName(userAuditLog.getBlockReason().getName());
                reasonVO.setCode(userAuditLog.getBlockReason().getCode());
                reasonVO.setDescription(userAuditLog.getBlockReason().getDescription());
                reasonVO.setDaysToBlock(getDaysToBlock(userId));
            }else if(null != userAuditLog.getInactiveReason()){
                reasonVO.setName(userAuditLog.getInactiveReason().getName());
                reasonVO.setCode(userAuditLog.getInactiveReason().getCode());
                reasonVO.setDescription(userAuditLog.getInactiveReason().getDescription());
            }
            else if(null != userAuditLog.getActiveReason()){
                reasonVO.setName(userAuditLog.getActiveReason().getName());
                reasonVO.setCode(userAuditLog.getActiveReason().getCode());
                reasonVO.setDescription(userAuditLog.getActiveReason().getDescription());
            }

        }

        return reasonVO;
    }

    @Override
    public ReasonVO getBlockInactiveReasonByUserId(Long userId){

        List<UserAuditLog> userAuditList = getUserAuditLogListByUserId(userId);
        ReasonVO reasonVO = new ReasonVO();
        if(userAuditList!=null && userAuditList.size()>0){
            for(UserAuditLog userAuditLog : userAuditList) {
                reasonVO.setRemarks(userAuditLog.getReasonRemarks());
                if (null != userAuditLog.getBlockReason()) {
                    reasonVO.setName(userAuditLog.getBlockReason().getName());
                    reasonVO.setCode(userAuditLog.getBlockReason().getCode());
                    reasonVO.setDescription(userAuditLog.getBlockReason().getDescription());
                    reasonVO.setDaysToBlock(getDaysToBlock(userId));
                    break;
                } else if (null != userAuditLog.getInactiveReason()) {
                    reasonVO.setName(userAuditLog.getInactiveReason().getName());
                    reasonVO.setCode(userAuditLog.getInactiveReason().getCode());
                    reasonVO.setDescription(userAuditLog.getInactiveReason().getDescription());
                    break;
                }
            }
        }

        return reasonVO;
    }

    private Integer getDaysToBlock(Long userId) {
        NamedQueryExecutor<Integer> executor = new NamedQueryExecutor<Integer>(QUERY_FOR_FETCHING_DAYS_TO_BLOCK_OF_USER)
                .addParameter("userId", userId);
        return entityDao.executeQueryForSingleValue(executor);
    }

    @Override
    public UserAuditLog updateReason(UserAuditLog userAuditLog, Long reasonId, String type){
        if(type.equalsIgnoreCase(UserConstants.USER_BLOCK_EVENT)){
            BlockReason blockReason = genericParameterService.findById(reasonId, BlockReason.class);
            if(blockReason!=null){
                userAuditLog.setBlockReason(blockReason);
            }else{
                throw new SystemException("BlockReason not found for Id = "+ reasonId);
            }
        }else if(type.equalsIgnoreCase(UserConstants.USER_INACTIVATE_EVENT)){
            InactiveReason inactiveReason = genericParameterService.findById(reasonId, InactiveReason.class);
            if(inactiveReason!=null){
                userAuditLog.setInactiveReason(inactiveReason);
            }else{
                throw new SystemException("InactiveReason not found for Id = "+ reasonId);
            }
        }
        else if(type.equalsIgnoreCase(UserConstants.USER_ACTIVATE_EVENT)){
            ActiveReason activeReason = genericParameterService.findById(reasonId, ActiveReason.class);
            if(activeReason!=null){
                userAuditLog.setActiveReason(activeReason);
            }else{
                throw new SystemException("ActiveReason not found for Id = "+ reasonId);
            }
        }
        return userAuditLog;
    }

    @Override
    public void notifyNewUser(String link, User user){
        NeutrinoValidator.notNull(user,"user can't be null");
        try{

            BaseLoggers.flowLogger.info("sending notification to new user"+user.getUsername());
            RandomValueStringGenerator randomValueStringGenerator= new RandomValueStringGenerator();
            randomValueStringGenerator.setLength(8);
            randomValueStringGenerator.setRandom(new SecureRandom());

            String randomPassword= randomValueStringGenerator.generate();

            String passwordPrefix=configurationService.getPropertyValueByPropertyKey("config.password.prefix","Configuration.getPropertyValueFromPropertyKey");

            if(passwordPrefix!=null && !Objects.equals(passwordPrefix, "")){
                randomPassword=passwordPrefix+randomPassword;
            }
            String userHash=null;
            if(StringUtils.isNotEmpty(user.getHashKey())) {
                userHash = user.getHashKey();
            }else if(user.getEntityLifeCycleData() != null && user.getEntityLifeCycleData().getCreationTimeStamp() != null) {
                userHash = Long.valueOf(user.getEntityLifeCycleData().getCreationTimeStamp().getMillis()).toString();
                user.setHashKey(userHash);
            } else {
                userHash = String.valueOf(System.currentTimeMillis());
                user.setHashKey(userHash);
            }
            String encryptedPassword=passwordEncoder.encode(randomPassword);
            user.setPassword(encryptedPassword);
            entityDao.update(user);
            String sourceSystem = user.getSourceSystem();



            Map<String, String> mapKeys = new HashMap<String, String>();
            mapKeys.put("USER", user.getDisplayName());
            String mailServiceProvider = templateService.getResolvedStringFromResourceBundle(
                    "mail.service.provider", null, mapKeys);
            String appPath = null;
            if(neutrinoSessionRegistry.isSsoActive()){
            	if(StringUtils.isEmpty(ssoUrl)){
            		throw new SystemException("Sso url property core.web.config.SSO.ticketvalidator.url.value can not be null.");
            	}
            	appPath = ssoUrl;
            }else{
            	 appPath = getWebApplicationUrl();
            }


            Map<String,Object> data=new HashMap<>();
            if(user.getDisplayName()==null || user.getUsername()==null || randomPassword==null || appPath==null){
                BaseLoggers.flowLogger.info("One of the variables in ftl file is null"+user.getDisplayName()+","+user.getUsername()+","+appPath+"password length"+randomPassword.length());
            }
            data.put("displayName",user.getDisplayName());
            data.put("userName",user.getUsername());
            data.put("appPath",appPath);
            
            String emailSubject="";
            
            if(UserService.SOURCE_DB.equalsIgnoreCase(sourceSystem)) {
            	emailSubject = configurationService.getPropertyValueByPropertyKey(
						"config.newUser.passwordReset.emailSubject", "Configuration.getPropertyValueFromPropertyKey");
                data.put("password",randomPassword);
            }else {
            	emailSubject = configurationService.getPropertyValueByPropertyKey(
						"config.ldapUser.import.emailSubject", "Configuration.getPropertyValueFromPropertyKey");
                randomPassword=PASSWORD_FOR_LDAP_USERS;
                data.put("password",randomPassword);
            }

            String to = user.getMailId();
            String emailBody=resetPasswordEmailHelper.getEmailBody(data,"welcomePasswordEmail.vm");

            if(StringUtils.isEmpty(emailBody)|| StringUtils.isEmpty(to)||StringUtils.isEmpty(from)||StringUtils.isEmpty(emailSubject)){
                BaseLoggers.flowLogger.info("message "+emailBody+" for user mail "+to+" is empty from"+from+" with subject"+emailSubject);
            }
            MimeMailMessageBuilder mime = mailService.createMimeMailBuilder();
            mime.setFrom(from);
            mime.setTo(to);
            mime.setSubject(emailSubject);
            mime.setHtmlBody(emailBody);
            if (("direct").equalsIgnoreCase(mailServiceProvider)) {
                mailService.sendMail(mime);
            } else {
                mailMessageIntegrationService.sendMailMessageToIntegrationServer(mime.getMimeMessage());
            }

        } catch (NullPointerException | MessagingException e) {
            BaseLoggers.exceptionLogger.error(e.getMessage(),e);
        } catch (IOException e) {
            BaseLoggers.exceptionLogger.error(e.getMessage(),e);
        }
    }

    private String generateNewUserEmailBody(Map<String,Object> data){
        //Configuration configuration=new Configuration();
        //StringWriter stringWriter= new StringWriter();
        /*File path= new ClassPathResource("ftlTemplates/newUserEmail").getFile();
        BaseLoggers.flowLogger.info("reading mail template from path"+path.getPath());
        configuration.setDirectoryForTemplateLoading(path);
        Template template = configuration.getTemplate("/newUserWelcomeEmail.ftl");
        template.process(data,stringWriter);*/

        String messageBody=
               "<html><body><div style='background: center; background-color: beige;'>Dear " +data.get("displayName")+"<br>" +

                "                     &nbsp;&nbsp;&nbsp; Please set your password by clicking on the following link <a href='" +
                data.get("appPath") +
                "                     ' >Login and Password Reset Link</a> <br> &nbsp;&nbsp;&nbsp; Your Username is :" +
                data.get("userName")+
                "                     <br>" +
                "                     Password is :" +
                data.get("password")+
                "</div></body></html>";

        return messageBody;
    }

    private String generateNewLDAPUserEmailBody(Map<String,Object> data){

        String messageBody=
                "<html><body><div style='background: center; background-color: beige;'>Dear " +data.get("displayName")+"<br>" +

                        "                     &nbsp;&nbsp;&nbsp; Your account has been successfully created. Please proceed to login <a href='" +
                        data.get("appPath") +
                        "                     ' >Login Link</a> <br> &nbsp;&nbsp;&nbsp; Your Username is :" +
                        data.get("userName")+
                        "                     <br>" +
                        "                     Password is :" +
                        data.get("password")+
                        "</div></body></html>";

        return messageBody;
    }


    private String getWebApplicationUrl() {
        String portNumber = "";
        String appContext = null;
        String ipAddress = null;
        String urlProtocol=null;
        String appPath = "";
        ServletRequestAttributes sra=null;
        HttpServletRequest req=null;
        String fullUrl=null;
        URL url=null;
        sra = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if(sra!=null) {
            req = sra.getRequest();
        }if(req!=null) {
            try {
                url=new URL(req.getRequestURL().toString());
            } catch (MalformedURLException e) {
                BaseLoggers.exceptionLogger.error(e.getMessage(), e);
            }
        }
        if(url.getPort()!=-1){
            portNumber=String.valueOf(url.getPort());
        }

        if(url.getProtocol()!=null){
            urlProtocol=url.getProtocol();
        }else {
            urlProtocol="http";
        }

        if(url.getPath()!=null){
           int tempIndex=url.getPath().indexOf("/",url.getPath().indexOf("/")+1);
            appContext = (tempIndex != -1) ? (url.getPath().substring(0, tempIndex)) : (url.getPath());
        }else {
            ConfigurationVO appContextVO = configurationService.getConfigurationPropertyFor(SystemEntity.getSystemEntityId(),
                    "config.notification.app.context");
            if (appContextVO != null) {
                appContext = appContextVO.getText();
            } else {
                throw new InvalidDataException("Application Context can not be null");
            }
        }


            if(url.getHost()!=null){
                ipAddress=url.getHost();
            } else {
            InetAddress ip;
            try {

                ip = InetAddress.getLocalHost();
                ipAddress = ip.getHostAddress();

            } catch (UnknownHostException e) {
                throw new SystemException("Unable to connect to Remote IP", e);

            }
        }

        appPath = urlProtocol+"://" + ipAddress + ":" + portNumber +  appContext;

        return appPath;
    }

	@Override
	public UserCalendar getUserCalendarByUserId(Long userId) {
		UserCalendar calendar = null;
		try{
			if(userId!=null){
				NamedQueryExecutor<UserCalendar> executor = new NamedQueryExecutor<UserCalendar>(QUERY_FOR_FETCHING_USER_CALENDAR)
		                .addParameter("userId", userId);
				calendar = entityDao.executeQueryForSingleValue(executor);
			}			
		}catch(Exception e){
			BaseLoggers.exceptionLogger.error("Erro in Org Retrival",e);
		}		
		return calendar;
	}


    @Override
    public List<UserAuditLog> getUserAuditLogListByUserId(Long userId){
        NeutrinoValidator.notNull(userId);
        NamedQueryExecutor<UserAuditLog> executorUserAuditLog = new NamedQueryExecutor<UserAuditLog>(
                QUERY_FOR_FETCHING_USER_AUDIT_LOGS).addParameter("userId", userId);

        List<UserAuditLog> userAuditList = entityDao.executeQuery(executorUserAuditLog);

        return userAuditList;
    }


    @Override
    public List<Long> getOrgBranchesWithCurrentUserByOrganizationType(Long userId,String organizationType) {

        NeutrinoValidator.notNull(userId, "User Id cannot be null");
        NamedQueryExecutor<Long> executor = new NamedQueryExecutor<Long>(
                "Organization.getOrgBranchesUnderCurrentUserByOrganizationTypeList");
        executor.addParameter("userID", userId);
        executor.addParameter("orgType",OrganizationType.ORGANIZATION_TYPE_BRANCH);
        //List<Long> resultList = entityDao.executeQuery(executor);

        return entityDao.executeQuery(executor);
    }
    
    @Override
    public String validatePassowrd(String passwordEntered, String username,String passwordPattern) {

        if (StringUtils.isBlank(passwordEntered))
            return "Password cannot be empty";
        String errorMsg = "";

        List<PasswordCreationPolicy> passwordCreationPolicies = passwordValidationService.getEnabledPasswordPolicy();
        if(passwordCreationPolicies.isEmpty()){
            Boolean result = false;
            result = passwordEntered.matches(passwordPattern);
            if(!result)
                return "Invalid Password";
        }else {
            List dictWords = new ArrayList();
            List specialChars = new ArrayList();
            List<PasswordPolicyDictWords> dictWordsEntity = entityDao.findAll(PasswordPolicyDictWords.class);
            List<PasswordPolicySpecChars> specialCharsEntity = entityDao.findAll(PasswordPolicySpecChars.class);
            for(PasswordPolicyDictWords passwordPolicyDictWord : dictWordsEntity){
                dictWords.add(passwordPolicyDictWord.getDictWords());
            }
            for(PasswordPolicySpecChars passwordPolicySpecChars : specialCharsEntity){
                specialChars.add(passwordPolicySpecChars.getSpecChar());
            }

            for (PasswordCreationPolicy passwordCreationPolicy : passwordCreationPolicies) {

                    Message configError = new Message();
                    configError.setI18nCode(configErrorCode);
                    configError.setMessageArguments(passwordCreationPolicy.getName());
                    String configErrorMsg = passwordValidationService.getMessageDescription(configError, passwordValidationService.getLocale());

                    Message message = new Message();
                    message.setI18nCode(passwordCreationPolicy.getErrorCode());
                    message.setMessageArguments(passwordCreationPolicy.getConfigValue().toString());
                    String validationError = passwordValidationService.getMessageDescription(message, passwordValidationService.getLocale());

                    AbstractPasswordValidation abstractPasswordValidation = PasswordCreationConfigurationFactory.getPasswordValidationInstance(passwordCreationPolicy.getName());
                    String err = abstractPasswordValidation.validate(passwordEntered, username, passwordCreationPolicy.getConfigValue(), configErrorMsg, validationError,dictWords,specialChars);
                    if (err != null && !err.isEmpty())
                        errorMsg += err + "\n";

            }


        }
        return errorMsg;
    
    }



}
