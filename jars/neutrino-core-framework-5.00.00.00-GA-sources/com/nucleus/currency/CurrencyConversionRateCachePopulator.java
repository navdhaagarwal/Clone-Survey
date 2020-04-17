package com.nucleus.currency;

import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.transaction.annotation.Transactional;

import com.nucleus.core.cache.FWCacheHelper;
import com.nucleus.finnone.pro.cache.common.FWCachePopulator;
import com.nucleus.finnone.pro.cache.constants.FWCacheConstants;
import com.nucleus.finnone.pro.general.util.ValidatorUtils;
import com.nucleus.logging.BaseLoggers;
import com.nucleus.persistence.EntityDao;

@Named("currencyConversionRateCachePopulator")
public class CurrencyConversionRateCachePopulator extends FWCachePopulator {

	@Inject
	@Named("currencyCacheService")
	private CurrencyCacheService currencyCacheService;

	@Inject
	@Named("entityDao")
	private EntityDao entityDao;

	@Inject
	@Named("fwCacheHelper")
	private FWCacheHelper fwCacheHelper;

	@Override
	public void init() {
		BaseLoggers.flowLogger.debug("Init Called : CurrencyConversionRateCachePopulator");
	}

	@Override
	@Transactional(readOnly = true)
	public Object fallback(Object key) {
		Long id = (Long) key;
		return currencyCacheService.getConversionRates(
				currencyCacheService.checkCurrencyActiveAndApproved(entityDao.find(Currency.class, id)));
	}

	@Override
	@Transactional(readOnly = true)
	public void build(Long tenantId) {
		for (Currency currency : currencyCacheService.getAllActiveApprovedCurrency()) {
			put(currency.getId(), currencyCacheService.getConversionRates(currency));
		}
	}

	@Override
	public void update(Action action, Object object) {
		if (action.equals(Action.UPDATE) && ValidatorUtils.notNull(object)) {
			putAll(object);
		}
	}

	@Override
	public String getNeutrinoCacheName() {
		return FWCacheConstants.CURRENCY_CONVERSION_RATE;
	}
	
	@Override
	public String getCacheGroupName() {
		return FWCacheConstants.CURRENCY_CACHE_GROUP;
	}

}
