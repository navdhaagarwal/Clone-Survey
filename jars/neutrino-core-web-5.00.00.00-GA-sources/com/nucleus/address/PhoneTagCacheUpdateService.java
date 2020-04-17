package com.nucleus.address;

import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import com.nucleus.finnone.pro.cache.common.NeutrinoCachePopulator;
import com.nucleus.finnone.pro.cache.common.NeutrinoCachePopulator.Action;
import com.nucleus.finnone.pro.cache.constants.FWCacheConstants;
import com.nucleus.finnone.pro.cache.entity.ImpactedCache;

@Named("phoneTagCacheUpdateService")
public class PhoneTagCacheUpdateService implements IPhoneTagCacheUpdateService {

	@Inject
	@Named("phoneTagDataCachePopulator")
	private NeutrinoCachePopulator phoneTagDataCachePopulator;

	@SuppressWarnings("unchecked")
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void updatePhoneTagCacheData(Map<String, Object> dataMap) {
		phoneTagDataCachePopulator.update((Map<String, ImpactedCache>) dataMap.get(FWCacheConstants.IMPACTED_CACHE_MAP), Action.DELETE, null);
	}

}
