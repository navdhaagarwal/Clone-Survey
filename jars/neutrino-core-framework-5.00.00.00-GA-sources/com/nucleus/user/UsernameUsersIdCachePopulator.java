package com.nucleus.user;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.transaction.annotation.Transactional;

import com.nucleus.finnone.pro.cache.common.FWCachePopulator;
import com.nucleus.finnone.pro.cache.constants.FWCacheConstants;
import com.nucleus.finnone.pro.general.util.ValidatorUtils;
import com.nucleus.logging.BaseLoggers;
import com.nucleus.user.cache.UserCacheService;

@Named("usernameUsersIdCachePopulator")
public class UsernameUsersIdCachePopulator extends FWCachePopulator {

	@Inject
	@Named(value = "userService")
	private UserService userService;

	@Inject
	@Named("userCacheService")
	private UserCacheService userCacheService;

	@Override
	public void init() {
		BaseLoggers.flowLogger.debug("Init Called : UsernameUsersIdCachePopulator");
	}

	@Override
	@Transactional(readOnly = true)
	public Object fallback(Object key) {
		List<User> userList = userService.getAllUsersByUserName((String) key);
		if (ValidatorUtils.hasElements(userList)) {
			Set<Long> userIdSet = new HashSet<>();
			for (User user : userList) {
				if(isUserApprovalStatusAppropriateForCache(user)){
					userIdSet.add(user.getId());
				}
			}
			return userIdSet;
		}
		return null;
	}

	@Override
	@Transactional(readOnly = true)
	public void build(Long tenantId) {
		List<User> users = userService.getAllUser();
		Map<String, Set<Long>> usernameUsersMap = new HashMap<>();
		for (User user : users) {
			Set<Long> userSetByUserName = usernameUsersMap.get(user.getUsername());
			if (ValidatorUtils.isNull(userSetByUserName)) {
				userSetByUserName = Collections.newSetFromMap(new ConcurrentHashMap<Long, Boolean>());
				usernameUsersMap.put(user.getUsername(), userSetByUserName);
			}
			userSetByUserName.add(user.getId());
		}
		for(Map.Entry<String, Set<Long>> entry : usernameUsersMap.entrySet()) {
			put(entry.getKey(),entry.getValue());
		}
	}

	@Override
	public void update(Action action, Object object) {
		User user = (User) object;
		if (ValidatorUtils.notNull(user)) {
			if (action.equals(Action.UPDATE)) {
				updateUser(user);
			} else if (action.equals(Action.DELETE)) {
				deleteUser(user);
			}
		}

	}

	@SuppressWarnings("unchecked")
	private void updateUser(User user) {
		Set<Long> userSetByUserName = (Set<Long>) get(user.getUsername());
		if (!isUserApprovalStatusAppropriateForCache(user)) {
			if (userSetByUserName != null && userSetByUserName.contains(user.getId())) {
				userSetByUserName.remove(user.getId());
				put(user.getUsername(), userSetByUserName);
			}
			return;
		}
		if (ValidatorUtils.isNull(userSetByUserName)) {
			userSetByUserName = Collections.newSetFromMap(new ConcurrentHashMap<Long, Boolean>());
		}
		userSetByUserName.add(user.getId());
		put(user.getUsername(), userSetByUserName);
	}

	private boolean isUserApprovalStatusAppropriateForCache(User user) {
		return UserCacheService.ALL_USER_APPROVAL_STATUS_SET.contains(user.getApprovalStatus());
	}

	@SuppressWarnings("unchecked")
	private void deleteUser(User user) {
		Set<Long> userSetByUserName = (Set<Long>) get(user.getUsername());
		if (userSetByUserName != null && userSetByUserName.contains(user.getId())) {
			userSetByUserName.remove(user.getId());
			put(user.getUsername(), userSetByUserName);
		}
	}

	@Override
	public String getNeutrinoCacheName() {
		return FWCacheConstants.USERNAME_USERS_ID_CACHE;
	}

	@Override
	public String getCacheGroupName() {
		return FWCacheConstants.USER_CACHE_GROUP;
	}

}
