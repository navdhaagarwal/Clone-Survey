package com.nucleus.currency;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.persistence.*;

import com.nucleus.activeInactiveReason.ReasonsActiveInactiveMapping;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.nucleus.core.annotations.Sortable;
import com.nucleus.core.annotations.Synonym;
import com.nucleus.core.system.util.SystemPropertyUtils;
import com.nucleus.entity.BaseEntity;
import com.nucleus.entity.CloneOptions;
import com.nucleus.finnone.pro.cache.annotation.CompositePredicate;
import com.nucleus.finnone.pro.cache.annotation.CustomCache;
import com.nucleus.finnone.pro.cache.annotation.CustomCaches;
import com.nucleus.finnone.pro.cache.annotation.Predicate;
import com.nucleus.finnone.pro.cache.annotation.Predicate.Operator;
import com.nucleus.finnone.pro.cache.common.CustomCacheEntityListener;
import com.nucleus.finnone.pro.cache.constants.FWCacheConstants;
import com.nucleus.finnone.pro.general.util.ValidatorUtils;
import com.nucleus.master.BaseMasterEntity;

/**
 * Master class to represent Currency. Modelled as per
 * http://en.wikipedia.org/wiki/ISO_4217
 * 
 * @author Nucleus Software Exports Limited
 */
@Entity
@DynamicUpdate
@DynamicInsert
@Inheritance(strategy = InheritanceType.JOINED)
@Cacheable
@Synonym(grant = "SELECT,REFERENCES")
@Table(indexes={@Index(name="RAIM_PERF_45_4131",columnList="REASON_ACT_INACT_MAP")})
@EntityListeners(CustomCacheEntityListener.class)
public class Currency extends BaseMasterEntity {

	private static final long serialVersionUID = -4278716851890927578L;

	public static final String CURR_CODE_INDIA = "INR";
	public static final String CURR_CODE_US = "USD";

	@CustomCaches(caches = {
			@CustomCache(name = FWCacheConstants.CURRENCY_CACHE_ISO, regionName = FWCacheConstants.FW_CACHE_REGION, groupName = FWCacheConstants.CURRENCY_CACHE_GROUP, predicates = {
					@Predicate(field = "masterLifeCycleData.approvalStatus", operator = Operator.IN, value = { "0", "3",
							"4", "6" }) }, compositePredicates = {
									@CompositePredicate(operator = CompositePredicate.Operator.OR, predicates = {
											@Predicate(field = "entityLifeCycleData.snapshotRecord", operator = Operator.IS_NULL, value = {}),
											@Predicate(field = "entityLifeCycleData.snapshotRecord", operator = Operator.EQUAL, value = {
													"false" }) }) }) })
	private String isoCode;

	private String isoNumber;

	private int decimalPlaces;

	@Sortable
	private String currencyName;

	private String symbol;

	private Locale locale;

	private Boolean isBaseCurrency = false;

	private String currencyUnitName;

	private String currencyFractionName;

	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	@JoinColumn(name = "fk_currency")
	private List<ConversionRate> conversionRateList;

	@OneToOne(cascade = CascadeType.ALL)
	private ReasonsActiveInactiveMapping reasonActInactMap;

	public Locale getLocale() {
		return locale;
	}

	public void setLocale(Locale locale) {
		this.locale = locale;
	}

	public String getIsoCode() {
		return isoCode;
	}

	public void setIsoCode(String isoCode) {
		this.isoCode = isoCode;
	}

	public String getIsoNumber() {
		return isoNumber;
	}

	public void setIsoNumber(String isoNumber) {
		this.isoNumber = isoNumber;
	}

	public int getDecimalPlaces() {
		return decimalPlaces;
	}

	public void setDecimalPlaces(int decimalPlaces) {
		this.decimalPlaces = decimalPlaces;
	}

	public String getCurrencyName() {
		return currencyName;
	}

	public void setCurrencyName(String currencyName) {
		this.currencyName = currencyName;
	}

	public String getSymbol() {
		return symbol;
	}

	public void setSymbol(String symbol) {
		this.symbol = symbol;
	}

	public Boolean getIsBaseCurrency() {
		if (ValidatorUtils.isNull(isBaseCurrency)) {
			return false;
		}
		return isBaseCurrency;
	}

	public void setIsBaseCurrency(Boolean isBaseCurrency) {
		this.isBaseCurrency = ((isBaseCurrency == null) ? Boolean.FALSE : isBaseCurrency);
	}

	public List<ConversionRate> getConversionRateList() {
		return conversionRateList;
	}

	public void setConversionRateList(List<ConversionRate> conversionRateList) {
		this.conversionRateList = conversionRateList;
	}

	public ReasonsActiveInactiveMapping getReasonActInactMap() {
		return reasonActInactMap;
	}

	public void setReasonActInactMap(ReasonsActiveInactiveMapping reasonActInactMap) {
		this.reasonActInactMap = reasonActInactMap;
	}

	@Override
	protected void populate(BaseEntity baseEntity, CloneOptions cloneOptions) {
		Currency currency = (Currency) baseEntity;
		super.populate(currency, cloneOptions);
		currency.setCurrencyName(currencyName);
		currency.setIsoCode(isoCode);
		currency.setIsoNumber(isoNumber);
		currency.setLocale(locale);
		currency.setSymbol(symbol);
		currency.setDecimalPlaces(decimalPlaces);
		currency.setIsBaseCurrency(isBaseCurrency);
		currency.setCurrencyFractionName(currencyFractionName);
		currency.setCurrencyUnitName(currencyUnitName);
		if (conversionRateList != null && conversionRateList.size() > 0) {
			currency.setConversionRateList(new ArrayList<ConversionRate>());
			for (ConversionRate rate : conversionRateList) {
				currency.getConversionRateList()
						.add(rate != null ? (ConversionRate) rate.cloneYourself(cloneOptions) : null);
			}
		}
		if (reasonActInactMap != null) {
			currency.setReasonActInactMap((ReasonsActiveInactiveMapping) this.reasonActInactMap.cloneYourself(cloneOptions));
		}
	}

	@Override
	protected void populateFrom(BaseEntity baseEntity, CloneOptions cloneOptions) {
		Currency currency = (Currency) baseEntity;
		super.populateFrom(currency, cloneOptions);
		this.setCurrencyName(currency.getCurrencyName());
		this.setIsoCode(currency.getIsoCode());
		this.setIsoNumber(currency.getIsoNumber());
		this.setLocale(currency.getLocale());
		this.setSymbol(currency.getSymbol());
		this.setDecimalPlaces(currency.getDecimalPlaces());
		this.setIsBaseCurrency(currency.getIsBaseCurrency());
		this.setCurrencyFractionName(currency.getCurrencyFractionName());
		this.setCurrencyUnitName(currency.getCurrencyUnitName());
		if (currency.getConversionRateList() != null && currency.getConversionRateList().size() > 0) {
			this.getConversionRateList().clear();
			for (ConversionRate rate : currency.getConversionRateList()) {
				this.getConversionRateList()
						.add(rate != null ? (ConversionRate) rate.cloneYourself(cloneOptions) : null);
			}
		}
		if (currency.getReasonActInactMap() != null) {
			this.setReasonActInactMap((ReasonsActiveInactiveMapping) currency.getReasonActInactMap().cloneYourself(cloneOptions));
		}
	}

	@Override
	public String getDisplayName() {
		return currencyName;
	}

	public String getLogInfo() {
		String log = null;
		StringBuffer stf = new StringBuffer();

		stf.append("ISO Code:" + isoCode);
		stf.append(SystemPropertyUtils.getNewline());
		stf.append("ISO Number:" + isoNumber);
		stf.append(SystemPropertyUtils.getNewline());
		stf.append("Currency Name:" + currencyName);
		stf.append(SystemPropertyUtils.getNewline());
		stf.append("Symbol:" + symbol);
		stf.append(SystemPropertyUtils.getNewline());
		if (locale != null) {
			stf.append("Locale" + locale.getCountry());
		}
		stf.append(SystemPropertyUtils.getNewline());
		stf.append("Decimal Places:" + decimalPlaces);
		log = stf.toString();

		return log;
	}

	public String getCurrencyUnitName() {
		return currencyUnitName;
	}

	public void setCurrencyUnitName(String currencyUnitName) {
		this.currencyUnitName = currencyUnitName;
	}

	public String getCurrencyFractionName() {
		return currencyFractionName;
	}

	public void setCurrencyFractionName(String currencyFractionName) {
		this.currencyFractionName = currencyFractionName;
	}
	
	public void initializeConversionRateList() {
		if(getConversionRateList()!=null)
    	{
			getConversionRateList().size();
    	}
	}
	
	@Override
    public void loadLazyFields() {
		super.loadLazyFields();
		if(getConversionRateList()!=null)
    	{
    		for(ConversionRate conversionRate:conversionRateList)
    		{
    			if(conversionRate!=null)
    			{
        			conversionRate.loadLazyFields();
    			}
    		}
    	}
    }
}
