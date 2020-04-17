package com.nucleus.user.cache;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.transaction.annotation.Transactional;

import com.nucleus.finnone.pro.cache.common.FWCachePopulator;
import com.nucleus.finnone.pro.cache.constants.FWCacheConstants;
import com.nucleus.finnone.pro.general.util.ValidatorUtils;
import com.nucleus.logging.BaseLoggers;
import com.nucleus.persistence.EntityDao;
import com.nucleus.user.UserProfile;
import com.nucleus.user.UserService;

@Named("userIdUserProfileIdCachePopulator")
public class UserIdUserProfileIdCachePopulator extends FWCachePopulator {

	@Inject
	@Named(value = "userService")
	private UserService userService;

	@Inject
	@Named(value = "entityDao")
	private EntityDao entityDao;

	@Override
	public void init() {
		BaseLoggers.flowLogger.debug("Init Called : UserIdUserProfileIdCachePopulator");
	}

	@Override
	@Transactional(readOnly = true)
	public Object fallback(Object key) {
		UserProfile userProfile = userService.getUserProfileByUserIdFromDb((Long) key);
		if (userProfile != null) {
			return userProfile.getId();
		}
		return null;
	}

	@Override
	@Transactional(readOnly = true)
	public void build(Long tenantId) {
		List<UserProfile> userProfiles = entityDao.findAll(UserProfile.class);
		if (ValidatorUtils.hasElements(userProfiles)) {
			for (UserProfile userProfile : userProfiles) {
				put(userProfile.getAssociatedUser().getId(), userProfile.getId());
			}
		}
	}

	@Override
	public void update(Action action, Object object) {
		UserProfile userProfile = (UserProfile) object;
		if (userProfile == null || userProfile.getAssociatedUser() == null
				|| userProfile.getAssociatedUser().getId() == null) {
			BaseLoggers.flowLogger.debug("No associated user found");
			return;
		}
		if (action.equals(Action.UPDATE)) {
			put(userProfile.getAssociatedUser().getId(), userProfile.getId());
		} else if (action.equals(Action.DELETE) && containsKey(userProfile.getAssociatedUser().getId())) {
			remove(userProfile.getAssociatedUser().getId());
		}

	}

	@Override
	public String getNeutrinoCacheName() {
		return FWCacheConstants.USERID_USERPROFILE_ID_CACHE;
	}

	@Override
	public String getCacheGroupName() {
		return FWCacheConstants.USER_CACHE_GROUP;
	}
	
}
