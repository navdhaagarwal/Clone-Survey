package com.nucleus.address;

import java.util.HashMap;
import java.util.Map;

import javax.persistence.PostPersist;
import javax.persistence.PostRemove;
import javax.persistence.PostUpdate;

import com.nucleus.core.NeutrinoSpringAppContextUtil;
import com.nucleus.core.cache.FWCacheHelper;
import com.nucleus.core.transaction.TransactionPostCommitWorker;
import com.nucleus.finnone.pro.cache.constants.FWCacheConstants;
import com.nucleus.logging.BaseLoggers;

public class CountryEntityListener {

	private static final String PHONETAGCACHEUPDATESERVICE="phoneTagCacheUpdateService";
	public static final String COUNTRY_OBJECT = "COUNTRY_OBJECT";
	public static final String FW_CACHE_HELPER = "fwCacheHelper";
	
    
    private FWCacheHelper getFWCacheHelper()
	{
		return NeutrinoSpringAppContextUtil
				.getBeanByName(FW_CACHE_HELPER, FWCacheHelper.class);
	}
    
		
		private IPhoneTagCacheUpdateService getPhoneTagCacheUpdateService()
		{
			return NeutrinoSpringAppContextUtil
					.getBeanByName(PHONETAGCACHEUPDATESERVICE, IPhoneTagCacheUpdateService.class);
		}
	
		@PostPersist
		public void userPostPersist(Country country) {
			BaseLoggers.flowLogger.debug("CountryEntityListener called after entity Persist : {}",country);
			updatePhoneTagData(country);
		}
	
		@SuppressWarnings("unchecked")
		private void updatePhoneTagData(Country country) {
			Map<String, Object> dataMap = new HashMap<>();
			dataMap.put(COUNTRY_OBJECT, country);
			dataMap.put(FWCacheConstants.IMPACTED_CACHE_MAP, getFWCacheHelper().createAndGetImpactedCachesFromCacheNames(FWCacheConstants.PHONE_TAG_DATA));
			TransactionPostCommitWorker.handlePostCommit(
					obj -> getPhoneTagCacheUpdateService().updatePhoneTagCacheData((Map<String, Object>) obj),
					dataMap, true);
		}


		@PostUpdate
		public void userPostUpdate(Country country) {
			BaseLoggers.flowLogger.debug("CountryEntityListener called after entity Update : {}",country);
			updatePhoneTagData(country);
		}
		
		@PostRemove
		public void userPostRemove(Country country) {
			BaseLoggers.flowLogger.debug("CountryEntityListener called after entity delete {}",country);
			updatePhoneTagData(country);
		}


}
