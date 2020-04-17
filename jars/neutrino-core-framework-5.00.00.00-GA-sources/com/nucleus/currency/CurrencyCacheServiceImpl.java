package com.nucleus.currency;

import static com.nucleus.finnone.pro.cache.constants.FWCacheConstants.CURRENCY_CACHE_APPROVED_ACTIVE;
import static com.nucleus.finnone.pro.cache.constants.FWCacheConstants.CURRENCY_CACHE_ISO;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.inject.Inject;
import javax.inject.Named;

import org.joda.time.DateTime;
import org.springframework.transaction.annotation.Transactional;

import com.nucleus.core.cache.FWCacheHelper;
import com.nucleus.core.misc.util.DateUtils;
import com.nucleus.dao.query.NamedQueryExecutor;
import com.nucleus.entity.ApprovalStatus;
import com.nucleus.finnone.pro.cache.common.CacheManager;
import com.nucleus.finnone.pro.cache.common.NeutrinoCachePopulator;
import com.nucleus.finnone.pro.cache.common.NeutrinoCachePopulator.Action;
import com.nucleus.finnone.pro.cache.constants.FWCacheConstants;
import com.nucleus.finnone.pro.cache.entity.ImpactedCache;
import com.nucleus.persistence.EntityDao;
import com.nucleus.query.constants.QueryHint;


@Named(value = "currencyCacheService")
public class CurrencyCacheServiceImpl implements CurrencyCacheService {

	@Inject
	@Named(FWCacheConstants.CACHE_MANAGER)
	private CacheManager cacheManager;
	
	@Inject
	@Named("currencyCacheByIdPopulator")
	private NeutrinoCachePopulator currencyCacheByIdPopulator;
	
	@Inject
	@Named("currencyCommonPropsPopulator")
	private NeutrinoCachePopulator currencyCommonPropsPopulator;
	
	@Inject
	@Named("currencyCacheApprovedActivePopulator")
	private NeutrinoCachePopulator currencyCacheApprovedActivePopulator;
	
	@Inject
	@Named("currencyConversionRateCachePopulator")
	private NeutrinoCachePopulator currencyConversionRateCachePopulator;
	
	@Inject
	@Named("entityDao")
	private EntityDao entityDao;
	
	@Inject
	@Named("fwCacheHelper")
	private FWCacheHelper fwCacheHelper;
	
	public static final String CURRENCY_OBJECT = "CURRENCY_OBJECT";
	
	public static final String CURRENCY_OBJECT_ID = "CURRENCY_OBJECT_ID";

	

	@Override
	public Currency getBaseCurrencyFromDB() {
		List<Integer> statusList = new ArrayList<Integer>();
		statusList.add(ApprovalStatus.APPROVED);
		statusList.add(ApprovalStatus.APPROVED_MODIFIED);
		statusList.add(ApprovalStatus.APPROVED_DELETED);
		statusList.add(ApprovalStatus.APPROVED_DELETED_IN_PROGRESS);
		NamedQueryExecutor<Currency> executor = new NamedQueryExecutor<Currency>("Currency.findBaseCurrencyIdForCache")
				.addParameter("approvalStatus", statusList);
		return entityDao.executeQueryForSingleValue(executor);
	}
	
	public Currency getCurrencyByIsoCode(String ISOCode) {
		return (Currency) cacheManager
				.getNeutrinoCachePopulatorInstance(FWCacheConstants.FW_CACHE_REGION, CURRENCY_CACHE_ISO)
				.get(ISOCode);
	}

	
	public Currency getCurrencyById(long id) {
		return (Currency) currencyCacheByIdPopulator.get(id);
	}

	@SuppressWarnings("unchecked")
	public List<Currency> getAllCurrencies() {
		List<Long> allCurrencyIdListFromCache = (List<Long>) currencyCacheApprovedActivePopulator.get(CURRENCY_CACHE_APPROVED_ACTIVE);
		if(allCurrencyIdListFromCache==null){
			return null;
		}
		List<Currency> list = new ArrayList<>();
		for(Long id : allCurrencyIdListFromCache) {
			list.add(entityDao.find(Currency.class, id));
		}
		return list;
	}

	public BigDecimal getEffectiveConversionFactor(long currencyId) {

		ConversionRate conversionRate = getConversionFactorForSpecifiedDate(currencyId, DateUtils.getCurrentUTCTime());

		if (conversionRate != null) {
			return conversionRate.getConversionFactor();
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	public ConversionRate getConversionFactorForSpecifiedDate(long currencyId, DateTime effectiveDate) {
		TreeMap<Long, Long> currencyConversionRates = (TreeMap<Long, Long>) currencyConversionRateCachePopulator.get(currencyId);
		Long userkey = effectiveDate.getMillis();

		ConversionRate conversionRate = null;
		if (currencyConversionRates != null && currencyConversionRates.floorEntry(userkey) != null)
			conversionRate = entityDao.find(ConversionRate.class,
					currencyConversionRates.floorEntry(userkey).getValue());

		return conversionRate;
	}


	
	// To Do: merge these two queries
	@Override
	public List<Currency> getAllActiveApprovedCurrency() {
		List<Integer> statusList = new ArrayList<>();
		statusList.add(ApprovalStatus.APPROVED);
		statusList.add(ApprovalStatus.APPROVED_MODIFIED);
		statusList.add(ApprovalStatus.APPROVED_DELETED);
		statusList.add(ApprovalStatus.APPROVED_DELETED_IN_PROGRESS);
		NamedQueryExecutor<Currency> executor = new NamedQueryExecutor<Currency>("Currency.findAllForCache")
				.addParameter("approvalStatus", statusList).addQueryHint(QueryHint.QUERY_HINT_FETCHSIZE, 500);
		return entityDao.executeQuery(executor);

	}
	
	@Override
	public List<Long> getAllActiveApprovedCurrencyIds() {
		List<Integer> statusList = new ArrayList<>();
		statusList.add(ApprovalStatus.APPROVED);
		statusList.add(ApprovalStatus.APPROVED_MODIFIED);
		statusList.add(ApprovalStatus.APPROVED_DELETED);
		statusList.add(ApprovalStatus.APPROVED_DELETED_IN_PROGRESS);
		NamedQueryExecutor<Long> executor = new NamedQueryExecutor<Long>("Currency.findAllIdForCache")
				.addParameter("approvalStatus", statusList).addQueryHint(QueryHint.QUERY_HINT_FETCHSIZE, 500);
		return entityDao.executeQuery(executor);

	}

	@Override
	public TreeMap<Long, Long> getConversionRates(Currency currency) {

		TreeMap<Long, Long> mapOfConversionRate = new TreeMap<>();
		List<ConversionRate> conversionRateList = currency.getConversionRateList();
		for (ConversionRate conversionRate : conversionRateList) {
			Date date = conversionRate.getEffectiveFrom();
			Long timeInMillis = date.getTime();
			mapOfConversionRate.put(timeInMillis, conversionRate.getId());
		}
		return mapOfConversionRate;
	}

	/**
	 * This method is used to update currency Cache.
	 * 
	 * @param
	 * @param
	 * 
	 */

	@Transactional
	@Override
	@SuppressWarnings("unchecked")
	public void updateCurrencyCache(Map<String,Object> dataMap) {
		
		Currency currency = (Currency) dataMap.get(CURRENCY_OBJECT);
		Long currencyId = (Long) dataMap.get(CURRENCY_OBJECT_ID);
		
		Currency previousCurrency = (Currency) currencyCacheByIdPopulator.get(currencyId);

		List<Long> listOfAllApprovedActiveCurrenciesLocal = (List<Long>) currencyCacheApprovedActivePopulator
				.get(CURRENCY_CACHE_APPROVED_ACTIVE);
		
		Map<Long,Currency> mapOfCurrenciesById = new HashMap<>();
		mapOfCurrenciesById.put(currencyId, currency);
		currencyCacheByIdPopulator.update((Map<String, ImpactedCache>) dataMap.get(FWCacheConstants.IMPACTED_CACHE_MAP),Action.INSERT,mapOfCurrenciesById);
		
		
		if (previousCurrency != null) {
			listOfAllApprovedActiveCurrenciesLocal.remove(previousCurrency.getId());
		}
		if(currency.isActiveFlag())
		{
			listOfAllApprovedActiveCurrenciesLocal.add(currency.getId());
		}

		TreeMap<Long, Long> conversionRates = getConversionRates(currency);
		Map<Long,TreeMap<Long,Long>> conversionRatesMap = new HashMap<>();
		conversionRatesMap.put(currencyId, conversionRates);
		currencyConversionRateCachePopulator.update((Map<String, ImpactedCache>) dataMap.get(FWCacheConstants.IMPACTED_CACHE_MAP),Action.UPDATE,conversionRatesMap);
		

		currencyCacheApprovedActivePopulator.update((Map<String, ImpactedCache>) dataMap.get(FWCacheConstants.IMPACTED_CACHE_MAP),Action.UPDATE,listOfAllApprovedActiveCurrenciesLocal);
		
		if(currency.getIsBaseCurrency()) {
			currencyCommonPropsPopulator.update((Map<String, ImpactedCache>) dataMap.get(FWCacheConstants.IMPACTED_CACHE_MAP),Action.UPDATE,currency);
		}
	}

	@Transactional
	@Override
	public Currency getCurrencyById(Long currencyId) {
		List<Integer> statusList = new ArrayList<Integer>();
		statusList.add(ApprovalStatus.APPROVED);
		statusList.add(ApprovalStatus.APPROVED_MODIFIED);
		statusList.add(ApprovalStatus.APPROVED_DELETED);
		statusList.add(ApprovalStatus.APPROVED_DELETED_IN_PROGRESS);
		NamedQueryExecutor<Currency> executor = new NamedQueryExecutor<Currency>("Currency.findCurrencyById")
				.addParameter("approvalStatus", statusList).addParameter("currencyId", currencyId);
		return entityDao.executeQueryForSingleValue(executor);

	}
	
	/* (non-Javadoc)
	 * @see com.nucleus.currency.CurrencyCacheService#getBaseCurrency()
	 */
	@Override
	public Currency getBaseCurrency() {
		return entityDao.find(Currency.class,
				(Long) currencyCommonPropsPopulator.get(BASE_CURRENCY));
	}
	
	
	@Override
	public Currency checkCurrencyActiveAndApproved(Currency currency) {
		if ((currency.getApprovalStatus() == ApprovalStatus.APPROVED
				|| currency.getApprovalStatus() == ApprovalStatus.APPROVED_MODIFIED
				|| currency.getApprovalStatus() == ApprovalStatus.APPROVED_DELETED
				|| currency.getApprovalStatus() == ApprovalStatus.APPROVED_DELETED_IN_PROGRESS)
				&& (currency.getEntityLifeCycleData().getSnapshotRecord() == null
						|| !currency.getEntityLifeCycleData().getSnapshotRecord())) {
			currency.initializeConversionRateList();
			fwCacheHelper.detachEntity(currency);
			return currency;
		}
		return null;
	}

}