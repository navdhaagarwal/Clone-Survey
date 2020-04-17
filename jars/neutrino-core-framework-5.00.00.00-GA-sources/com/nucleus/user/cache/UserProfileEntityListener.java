package com.nucleus.user.cache;

import javax.persistence.PostPersist;
import javax.persistence.PostRemove;
import javax.persistence.PostUpdate;
import javax.persistence.PrePersist;
import javax.persistence.PreRemove;
import javax.persistence.PreUpdate;

import com.nucleus.core.NeutrinoSpringAppContextUtil;
import com.nucleus.logging.BaseLoggers;
import com.nucleus.user.User;
import com.nucleus.user.UserProfile;

public class UserProfileEntityListener {

	private static final String USERCACHESERVICE="userCacheService";
		
		private IUserCacheService getUserCacheUpdateService()
		{
			return NeutrinoSpringAppContextUtil
					.getBeanByName(USERCACHESERVICE, IUserCacheService.class);
		}
	
		@PostPersist
		public void userPostPersist(UserProfile userProfile) {
			BaseLoggers.flowLogger.debug("UserProfileEntityListener called after entity Persist : {}",userProfile);
			updateInPostTransaction(userProfile);
		}
	
		private void updateInPostTransaction(UserProfile userProfile) {
			getUserCacheUpdateService().updateUserProfileCacheInPostTransaction(userProfile);
		}

		@PostUpdate
		public void userPostUpdate(UserProfile userProfile) {
			BaseLoggers.flowLogger.debug("UserProfileEntityListener called after entity Update : {}",userProfile);
			updateInPostTransaction(userProfile);
		}
		
		@PostRemove
		public void userPostRemove(UserProfile userProfile) {
			BaseLoggers.flowLogger.debug("UserProfileEntityListener called after entity delete {}",userProfile);
			deleteInPostTransaction(userProfile);
		}

		private void deleteInPostTransaction(UserProfile userProfile) {
			getUserCacheUpdateService().deleteUserProfileCacheInPostTransaction(userProfile);
		}


		@PrePersist
		@PreUpdate
		@PreRemove
		public void preModification(UserProfile userProfile) {
			userProfile.getAssociatedUser();
		}



}
