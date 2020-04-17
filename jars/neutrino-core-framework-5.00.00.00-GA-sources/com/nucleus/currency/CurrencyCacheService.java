package com.nucleus.currency;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.math.BigDecimal;

import org.joda.time.DateTime;

import com.nucleus.currency.ConversionRate;
import com.nucleus.currency.Currency;
import com.nucleus.service.BaseService;



public interface CurrencyCacheService extends BaseService{
	
	public static final String BASE_CURRENCY = "BASE_CURRENCY";
	
	
	public List<Currency> getAllCurrencies();
	public Currency getCurrencyById(long id);
	public Currency getCurrencyByIsoCode(String ISOCode);
	public void updateCurrencyCache(Map<String,Object> dataMap);
	public BigDecimal getEffectiveConversionFactor(long currencyId);
	public ConversionRate getConversionFactorForSpecifiedDate(long currencyId, DateTime effectiveDate);
	public Currency getCurrencyById(Long currencyId);
	public Currency getBaseCurrency();
	public Currency getBaseCurrencyFromDB();
	public List<Long> getAllActiveApprovedCurrencyIds();
	public List<Currency> getAllActiveApprovedCurrency();
	public TreeMap<Long, Long> getConversionRates(Currency currency);
	public Currency checkCurrencyActiveAndApproved(Currency currency);
}
