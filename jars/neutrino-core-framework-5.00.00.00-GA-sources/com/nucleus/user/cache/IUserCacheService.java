package com.nucleus.user.cache;

import java.util.Map;

import com.nucleus.authority.Authority;
import com.nucleus.user.User;
import com.nucleus.user.UserProfile;

/**
 * 
 * @author gajendra.jatav
 *
 */
public interface IUserCacheService {
	
	void updateUserNameCache(Map<String, Object> dataMap);

	void updateUserNameCacheInPostTransaction(User user);

	void updateUserProfileCacheInPostTransaction(UserProfile userProfile);

	void deleteUserNameCacheInPostTransaction(User user);

	void deleteUserProfileCacheInPostTransaction(UserProfile userProfile);
	
	void updateAuthCodeAuthorityCache(Authority authority);

}
