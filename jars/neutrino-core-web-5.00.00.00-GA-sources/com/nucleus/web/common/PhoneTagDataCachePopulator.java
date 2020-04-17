package com.nucleus.web.common;

import static com.nucleus.web.common.CommonConfigUtility.GETCOUNTRYCODEALPHA2ALPHA3MAP;
import static com.nucleus.web.common.CommonConfigUtility.GETCOUNTRYCODEFROMCOUNTRYMASTER;
import static com.nucleus.web.common.CommonConfigUtility.GETPHONETAGDATA;
import static com.nucleus.web.common.CommonConfigUtility.GETPHONETAGINITIALIZERDATA;

import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.transaction.annotation.Transactional;

import com.nucleus.finnone.pro.cache.common.FWCachePopulator;
import com.nucleus.finnone.pro.cache.constants.FWCacheConstants;
import com.nucleus.logging.BaseLoggers;

@Named("phoneTagDataCachePopulator")
public class PhoneTagDataCachePopulator extends FWCachePopulator {

	@Inject
	@Named("commonConfigUtility")
	private CommonConfigUtility commonConfigUtility;

	@Override
	public void init() {
		BaseLoggers.flowLogger.debug("Init Called : PhoneTagDataCachePopulator");
	}

	@Override
	@Transactional(readOnly = true)
	public Object fallback(Object key) {
		String cacheKey = (String) key;

		if (cacheKey.equals(GETCOUNTRYCODEALPHA2ALPHA3MAP)) {
			return commonConfigUtility.getUpdatedCountryCodeAlpha2Alpha3Map();
		} else if (cacheKey.equals(GETCOUNTRYCODEFROMCOUNTRYMASTER)) {
			return commonConfigUtility.getUpdatedCountryCodeFromCountryMaster();
		} else if (cacheKey.contains(GETPHONETAGDATA)) {
			String[] keyArray = cacheKey.split(FWCacheConstants.REGEX_DELIMITER, 2);
			return commonConfigUtility.getUpdatedPhoneTagData(keyArray[1]);
		} else if (cacheKey.contains(GETPHONETAGINITIALIZERDATA)) {
			String[] keyArray = cacheKey.split(FWCacheConstants.REGEX_DELIMITER, 2);
			return commonConfigUtility.getUpdatedPhoneTagInitializerData(keyArray[1]);
		}
		return null;
	}

	@Override
	public void build(Long tenantId) {
		BaseLoggers.flowLogger.debug("Build Called : PhoneTagDataCachePopulator");
	}

	@Override
	public void update(Action action, Object object) {
		if (action.equals(Action.DELETE)) {
			clear();
		}
	}

	@Override
	public String getNeutrinoCacheName() {
		return FWCacheConstants.PHONE_TAG_DATA;
	}

	@Override
	public String getCacheGroupName() {
		return FWCacheConstants.MISCELLANEOUS_CACHE_GROUP;
	}

}
