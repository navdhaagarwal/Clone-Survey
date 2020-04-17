package com.nucleus.currency;

import static com.nucleus.finnone.pro.cache.constants.FWCacheConstants.CURRENCY_CACHE_APPROVED_ACTIVE;

import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.transaction.annotation.Transactional;

import com.nucleus.finnone.pro.cache.common.FWCachePopulator;
import com.nucleus.finnone.pro.cache.constants.FWCacheConstants;
import com.nucleus.finnone.pro.general.util.ValidatorUtils;
import com.nucleus.logging.BaseLoggers;

@Named("currencyCacheApprovedActivePopulator")
public class CurrencyCacheApprovedActivePopulator extends FWCachePopulator {

	@Inject
	@Named("currencyCacheService")
	private CurrencyCacheService currencyCacheService;

	@Override
	public void init() {
		BaseLoggers.flowLogger.debug("Init Called : CurrencyCacheApprovedActivePopulator");
	}

	@Override
	@Transactional(readOnly = true)
	public Object fallback(Object key) {
		if (((String) key).equals(CURRENCY_CACHE_APPROVED_ACTIVE)) {
			return currencyCacheService.getAllActiveApprovedCurrencyIds();
		}
		return null;
	}

	@Override
	@Transactional(readOnly = true)
	public void build(Long tenantId) {
		put(CURRENCY_CACHE_APPROVED_ACTIVE, currencyCacheService.getAllActiveApprovedCurrencyIds());

	}

	@Override
	public void update(Action action, Object object) {
		if (action.equals(Action.UPDATE) && ValidatorUtils.notNull(object)) {
			put(CURRENCY_CACHE_APPROVED_ACTIVE, object);
		}

	}

	@Override
	public String getNeutrinoCacheName() {
		return FWCacheConstants.CURRENCY_CACHE_APPROVED_ACTIVE;
	}

	@Override
	public String getCacheGroupName() {
		return FWCacheConstants.CURRENCY_CACHE_GROUP;
	}

}
