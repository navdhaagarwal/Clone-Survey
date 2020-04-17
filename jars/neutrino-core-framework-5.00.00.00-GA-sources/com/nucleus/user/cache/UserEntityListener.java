package com.nucleus.user.cache;

import javax.persistence.PostPersist;
import javax.persistence.PostRemove;

import com.nucleus.core.NeutrinoSpringAppContextUtil;
import com.nucleus.logging.BaseLoggers;
import com.nucleus.user.User;

/**
 * 
 * @author gajendra.jatav
 *
 */
public class UserEntityListener {
	private static final String USERCACHESERVICE="userCacheService";
		
		private IUserCacheService getUserCacheUpdateService()
		{
			return NeutrinoSpringAppContextUtil
					.getBeanByName(USERCACHESERVICE, IUserCacheService.class);
		}
	
		@PostPersist
		public void userPostPersist(User user) {
			BaseLoggers.flowLogger.debug("UserEntityListener called after entity Persist : {}",user);
			updateInPostTransaction(user);
		}
	
		private void updateInPostTransaction(User user) {
			getUserCacheUpdateService().updateUserNameCacheInPostTransaction(user);
		}
		
		@PostRemove
		public void userPostRemove(User user) {
			BaseLoggers.flowLogger.debug("UserEntityListener called after entity delete {}",user);
			deleteInPostTransaction(user);
		}

		private void deleteInPostTransaction(User user) {
			getUserCacheUpdateService().deleteUserNameCacheInPostTransaction(user);
		}

}
