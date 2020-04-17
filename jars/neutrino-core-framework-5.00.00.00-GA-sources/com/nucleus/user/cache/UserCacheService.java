package com.nucleus.user.cache;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Named;

import com.nucleus.authority.Authority;
import com.nucleus.core.cache.FWCacheHelper;
import com.nucleus.core.transaction.TransactionPostCommitWorker;
import com.nucleus.core.validation.util.NeutrinoValidator;
import com.nucleus.entity.ApprovalStatus;
import com.nucleus.finnone.pro.cache.common.NeutrinoCachePopulator;
import com.nucleus.finnone.pro.cache.common.NeutrinoCachePopulator.Action;
import com.nucleus.finnone.pro.cache.constants.FWCacheConstants;
import com.nucleus.finnone.pro.cache.entity.ImpactedCache;
import com.nucleus.finnone.pro.cache.service.CacheCommonService;
import com.nucleus.logging.BaseLoggers;
import com.nucleus.persistence.EntityDao;
import com.nucleus.user.User;
import com.nucleus.user.UserProfile;
import com.nucleus.user.UserService;

/**
 * 
 * @author gajendra.jatav
 *
 */
@Named("userCacheService")
public class UserCacheService implements IUserCacheService{

	private static final String USER_OBJECT = "USER_OBJECT";
	private static final String USER_PROFILE_OBJECT = "USER_PROFILE_OBJECT";

	
	@Inject
    @Named(value = "entityDao")
    private EntityDao                    entityDao;

	@Inject
	@Named("usernameUsersIdCachePopulator")
	private NeutrinoCachePopulator usernameUsersIdCachePopulator;
	
	@Inject
	@Named("userIdUserProfileIdCachePopulator")
	private NeutrinoCachePopulator userIdUserProfileIdCachePopulator;
	
	@Inject
	@Named("authCodeAuthorityIdCachePopulator")
	private NeutrinoCachePopulator authCodeAuthorityIdCachePopulator;
    
    @Inject
    @Named("userService")
    private UserService userService;
    
    @Inject
    @Named("cacheCommonService")
    private CacheCommonService cacheCommonService;
    
    @Inject
    @Named("fwCacheHelper")
    private FWCacheHelper fwCacheHelper;

	public static final String ROLE_NULL_ERRER="role can't be null";
	
	public static final String AUTHORITY_NULL_ERROR="authority can't be null";
	
	public static final String USER_NULL_ERROR="user can't be null";
	
	public static final Set<Integer> ALL_USER_APPROVAL_STATUS_SET = Collections.unmodifiableSet(
			new HashSet<>(Arrays.asList(ApprovalStatus.UNAPPROVED_ADDED, ApprovalStatus.WORFLOW_IN_PROGRESS,
					ApprovalStatus.APPROVED, ApprovalStatus.APPROVED_MODIFIED, ApprovalStatus.UNAPPROVED_MODIFIED,
					ApprovalStatus.APPROVED_DELETED, ApprovalStatus.APPROVED_DELETED_IN_PROGRESS)));
	
	

	@SuppressWarnings("unchecked")
	public void updateUserProfileCache(Map<String, Object> dataMap) {
		UserProfile userProfile = (UserProfile) dataMap.get(USER_PROFILE_OBJECT);
		NeutrinoValidator.notNull(userProfile);
		BaseLoggers.flowLogger.debug("Updating UserProfile cache {}", userProfile);
		userIdUserProfileIdCachePopulator.update((Map<String, ImpactedCache>) dataMap.get(FWCacheConstants.IMPACTED_CACHE_MAP),Action.UPDATE,userProfile);
	}



	@SuppressWarnings("unchecked")
	@Override
	public void updateUserNameCache(Map<String, Object> dataMap) {
		User user = (User) dataMap.get(USER_OBJECT);
		NeutrinoValidator.notNull(user, USER_NULL_ERROR);
		NeutrinoValidator.notNull(user.getUsername(), "username can't be null");
		usernameUsersIdCachePopulator.update((Map<String, ImpactedCache>) dataMap.get(FWCacheConstants.IMPACTED_CACHE_MAP), Action.UPDATE, user);
	}


	
	public void updateAuthCodeAuthorityCache(Authority authority) {
		NeutrinoValidator.notNull(authority, AUTHORITY_NULL_ERROR);
		NeutrinoValidator.notNull(authority.getAuthCode(), "auth code can't be null");
		authCodeAuthorityIdCachePopulator.update(Action.UPDATE, authority);
	}


	@SuppressWarnings("unchecked")
	@Override
	public void updateUserNameCacheInPostTransaction(User user) {
		BaseLoggers.flowLogger.debug("Updating user in post commit worker --> {}",user);
		Map<String, Object> dataMap = new HashMap<>();
		dataMap.put(USER_OBJECT, user);
		dataMap.put(FWCacheConstants.IMPACTED_CACHE_MAP, fwCacheHelper.createAndGetImpactedCachesFromCacheNames(FWCacheConstants.USERNAME_USERS_ID_CACHE));
		TransactionPostCommitWorker.handlePostCommit(obj -> updateUserNameCache((Map<String, Object>) obj),
				dataMap, true);
	}


	@SuppressWarnings("unchecked")
	@Override
	public void updateUserProfileCacheInPostTransaction(UserProfile userProfile) {
		BaseLoggers.flowLogger.debug("Updating userProfile in post commit worker --> {}",userProfile);
		Map<String, Object> dataMap = new HashMap<>();
		dataMap.put(USER_PROFILE_OBJECT, userProfile);
		dataMap.put(FWCacheConstants.IMPACTED_CACHE_MAP, fwCacheHelper.createAndGetImpactedCachesFromCacheNames(FWCacheConstants.USERID_USERPROFILE_ID_CACHE));
		TransactionPostCommitWorker.handlePostCommit(obj -> updateUserProfileCache((Map<String, Object>) obj),
				dataMap, true);
	}


	@SuppressWarnings("unchecked")
	@Override
	public void deleteUserNameCacheInPostTransaction(User user) {
		BaseLoggers.flowLogger.debug("Deleting user in post commit worker --> {}",user);
		Map<String, Object> dataMap = new HashMap<>();
		dataMap.put(USER_OBJECT, user);
		dataMap.put(FWCacheConstants.IMPACTED_CACHE_MAP, fwCacheHelper.createAndGetImpactedCachesFromCacheNames(FWCacheConstants.USERNAME_USERS_ID_CACHE));
		TransactionPostCommitWorker.handlePostCommit(obj -> deleteUserNameCache((Map<String, Object>) obj),
				dataMap, true);
	}


	
	@SuppressWarnings("unchecked")
	private void deleteUserNameCache(Map<String, Object> dataMap) {
		User user = (User) dataMap.get(USER_OBJECT);
		NeutrinoValidator.notNull(user, USER_NULL_ERROR);
		NeutrinoValidator.notNull(user.getUsername(), "username can't be null");
		usernameUsersIdCachePopulator.update((Map<String, ImpactedCache>) dataMap.get(FWCacheConstants.IMPACTED_CACHE_MAP), Action.DELETE, user);
	}


	@SuppressWarnings("unchecked")
	@Override
	public void deleteUserProfileCacheInPostTransaction(UserProfile userProfile) {
		BaseLoggers.flowLogger.debug("deleting userProfile in post commit worker --> {}",userProfile);
		Map<String, Object> dataMap = new HashMap<>();
		dataMap.put(USER_PROFILE_OBJECT, userProfile);
		dataMap.put(FWCacheConstants.IMPACTED_CACHE_MAP, fwCacheHelper.createAndGetImpactedCachesFromCacheNames(FWCacheConstants.USERID_USERPROFILE_ID_CACHE));
		TransactionPostCommitWorker.handlePostCommit(obj -> deleteUserProfileCache((Map<String, Object>) obj),
				dataMap, true);
		
	}


	@SuppressWarnings("unchecked")
	private void deleteUserProfileCache(Map<String, Object> dataMap) {
		UserProfile userProfile = (UserProfile) dataMap.get(USER_PROFILE_OBJECT);
		NeutrinoValidator.notNull(userProfile);
		BaseLoggers.flowLogger.debug("Updating UserProfile cache {}", userProfile);
		userIdUserProfileIdCachePopulator.update((Map<String, ImpactedCache>) dataMap.get(FWCacheConstants.IMPACTED_CACHE_MAP), Action.DELETE, userProfile);
	}

	
}
