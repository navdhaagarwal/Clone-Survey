package com.nucleus.currency;

import static com.nucleus.currency.CurrencyCacheService.BASE_CURRENCY;

import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.transaction.annotation.Transactional;

import com.nucleus.finnone.pro.cache.common.FWCachePopulator;
import com.nucleus.finnone.pro.cache.constants.FWCacheConstants;
import com.nucleus.finnone.pro.general.util.ValidatorUtils;
import com.nucleus.logging.BaseLoggers;

@Named("currencyCommonPropsPopulator")
public class CurrencyCommonPropsPopulator extends FWCachePopulator {

	@Inject
	@Named("currencyCacheService")
	private CurrencyCacheService currencyCacheService;

	@Override
	public void init() {
		BaseLoggers.flowLogger.debug("Init Called : CurrencyCommonPropsPopulator");
	}

	@Override
	@Transactional(readOnly = true)
	public Object fallback(Object key) {
		if (((String) key).equals(BASE_CURRENCY)) {
			return currencyCacheService.getBaseCurrencyFromDB().getId();
		}

		return null;
	}

	@Override
	@Transactional(readOnly = true)
	public void build(Long tenantId) {
		put(BASE_CURRENCY, currencyCacheService.getBaseCurrencyFromDB().getId());

	}

	@Override
	public void update(Action action, Object object) {
		if (action.equals(Action.UPDATE) && ValidatorUtils.notNull(object)) {
			put(BASE_CURRENCY, ((Currency) object).getId());
		}
	}

	@Override
	public String getNeutrinoCacheName() {
		return FWCacheConstants.CURRENCY_COMMON_PROPS;
	}
	
	@Override
	public String getCacheGroupName() {
		return FWCacheConstants.CURRENCY_CACHE_GROUP;
	}

}
