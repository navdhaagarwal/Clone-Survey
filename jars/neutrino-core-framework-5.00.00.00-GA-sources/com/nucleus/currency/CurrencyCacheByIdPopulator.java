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

@Named("currencyCacheByIdPopulator")
public class CurrencyCacheByIdPopulator extends FWCachePopulator {

	@Inject
	@Named("currencyCacheService")
	private CurrencyCacheService currencyCacheService;

	@Inject
	@Named("fwCacheHelper")
	private FWCacheHelper fwCacheHelper;

	@Inject
	@Named("entityDao")
	private EntityDao entityDao;

	@Override
	public void init() {
		BaseLoggers.flowLogger.debug("Init Called : CurrencyCacheByIdPopulator");
	}

	@Override
	@Transactional(readOnly = true)
	public Object fallback(Object key) {
		Long id = (Long) key;
		return currencyCacheService.checkCurrencyActiveAndApproved(entityDao.find(Currency.class, id));
	}

	@Override
	@Transactional(readOnly = true)
	public void build(Long tenantId) {
		for (Currency currency : currencyCacheService.getAllActiveApprovedCurrency()) {
			currency.initializeConversionRateList();
			fwCacheHelper.detachEntity(currency);
			put(currency.getId(), currency);
		}
	}

	@Override
	public void update(Action action, Object object) {
		if (action.equals(Action.INSERT) && ValidatorUtils.notNull(object)) {
			putAll(object);
		}
	}

	@Override
	public String getNeutrinoCacheName() {
		return FWCacheConstants.CURRENCY_CACHE_BY_ID;
	}

	@Override
	public String getCacheGroupName() {
		return FWCacheConstants.CURRENCY_CACHE_GROUP;
	}
	
}
