package com.nucleus.money;



import static com.nucleus.finnone.pro.general.util.ValidatorUtils.isNull;
import static com.nucleus.finnone.pro.general.util.ValidatorUtils.notNull;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;
import javax.transaction.Transactional;
import javax.transaction.Transactional.TxType;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;

import com.ibm.icu.text.NumberFormat;
import com.nucleus.config.persisted.service.ConfigurationService;
import com.nucleus.config.persisted.vo.ConfigurationVO;
import com.nucleus.core.misc.util.DateUtils;
import com.nucleus.core.money.builder.MoneyBuilder;
import com.nucleus.core.money.entity.Money;
import com.nucleus.core.money.utils.FormatterCustomizer;
import com.nucleus.core.money.utils.MoneyUtils;
import com.nucleus.core.validation.util.NeutrinoValidator;
import com.nucleus.currency.ConversionRate;
import com.nucleus.currency.ConversionRateMap;
import com.nucleus.currency.Currency;
import com.nucleus.currency.CurrencyCacheService;
import com.nucleus.dao.query.NamedQueryExecutor;
import com.nucleus.entity.ApprovalStatus;
import com.nucleus.entity.EntityId;
import com.nucleus.entity.SystemEntity;
import com.nucleus.finnone.pro.general.util.CoreMathUtility;
import com.nucleus.logging.BaseLoggers;
import com.nucleus.money.dao.MoneyDao;
import com.nucleus.query.constants.QueryHint;
import com.nucleus.service.BaseServiceImpl;

import net.bull.javamelody.MonitoredWithSpring;

@Named("moneyService")
@MonitoredWithSpring(name = "moneyService_IMPL_")
public class MoneyServiceImpl extends BaseServiceImpl implements MoneyService {
	
	@Inject
	@Named("currencyCacheService")
	private CurrencyCacheService             currencyCacheService;

    @Inject
    @Named("configurationService")
    protected ConfigurationService configurationService;
    
    
    @Inject
    @Named("moneyDao")
    private MoneyDao moneyDao;
    
    public static final String PRECISION = "PRECISION";	
	public static final String MULTIPLESOFF = "MULTIPLESOFF";	
	public static final String APPLICABLE_CURRENCY = "applicableCurrency";
	public static final String CONFIG_DECIMAL_GROUPING_CONSTANT_CONFIG_KEY="config.decimal.grouping.constant";
	public static final String CONFIG_DIGIT_GROUPING_CONSTANT_CONFIG_KEY="config.digit.grouping.constant";
	public static final String CONFIG_AMOUNT_FORMAT_CONSTANT_CONFIG_KEY="config.amount.format.constant";
	public static final String CONFIG_AMOUNT_FORMAT_CONSTANT="config.amount.format.description.constant";
	public static final String CONFIG_DECIMAL_GROUPING_CONSTANT=".";
	public static final String CONFIG_DIGIT_GROUPING_CONSTANT=",";
	public static final String CONFIG_AMOUNT_FORMAT_INDIAN_DESCRIPTION_CONSTANT="##C##C##C##C##C##C##C##0D";
	public static final String INDIAN="I";
	public static final String CONFIG_AMOUNT_FORMAT_MILLION_DESCRIPTION_CONSTANT="##C###C###C###C###C##0D";
	public static final String MILLION="M";
    private static final String CONFIGURATION_QUERY = "Configuration.getPropertyValueFromPropertyKey";
    public static final String CONFIG_NUMBER_FORMAT="numberFormat";
    private static ThreadLocal<Map<String, BigDecimal>> appEffectiveRateMapHolder = new ThreadLocal<Map<String, BigDecimal>>(){
																        @Override
																        protected Map<String, BigDecimal> initialValue() {
																            return new HashMap<String, BigDecimal>();
																        }
																    };

    @Override
    public List<Currency> retrieveAllActiveCurrencies() {
    	
    	return currencyCacheService.getAllCurrencies();

		
    }

    @Override
    public Currency getCurrencyById(long id) {
    	
    	Currency currency  = currencyCacheService.getCurrencyById(id);
    	if(currency!=null){
    		BaseLoggers.flowLogger.debug(" Returning Currency using currency id from Currency Cache:: getCurrencyById ");
			return currency;
		}
    	
        return entityDao.find(Currency.class, id);
    }

    /**
     * This method will retrieve the Currency from a country's ISO code.
     * If the parameter ISO code is empty or null it will return the very first existing Currency.
     * In case there is no existing Currency this would return null. 
     *
     * @param ISOCode 
     * @return Currency
     */
    @Override
    public Currency getCurrencyByCurrencyCode(String ISOCode) {
    	
    	Currency currencyByIsoCode  = currencyCacheService.getCurrencyByIsoCode(ISOCode);

		if(currencyByIsoCode!=null){
			BaseLoggers.flowLogger.debug(" Returning Currency based on currency ISOCode from Currency Cache:: getCurrencyById ");
			return currencyByIsoCode;
		}
    	
        NeutrinoValidator.notNull(ISOCode, "ISO code can't be null");
        List<Integer> statusList = new ArrayList<Integer>();
        statusList.add(ApprovalStatus.APPROVED);
        statusList.add(ApprovalStatus.APPROVED_MODIFIED);
        statusList.add(ApprovalStatus.APPROVED_DELETED);
        statusList.add(ApprovalStatus.APPROVED_DELETED_IN_PROGRESS);
        NamedQueryExecutor<Currency> executor = new NamedQueryExecutor<Currency>("Currency.findByIsoCode").addParameter("approvalStatus", statusList).addParameter(
                "isoCode", ISOCode).addQueryHint(QueryHint.QUERY_HINT_CACHEABLE, Boolean.TRUE);
        List<Currency> currency = entityDao.executeQuery(executor);
        if (currency != null && currency.size() > 0) {
            return currency.get(0);
        } else {
            return null;
        }
    }

    /**
     * This method parses the amount as per the locale of user.
     * For a negative String being passed this would return the amount's double value.
     * For an invalid String being passed it would return zero. For a null string or an empty string
     * or an 'NaN' it would return null.  
     *
     * @param amount
     * @return BigDecimal
     */
    @Override
    public BigDecimal parseCurrency(String amount) {

        return MoneyUtils.parseCurrencyByLocale(amount, getUserLocale());
    }

    /**
     * This method formats money by Locale. For a null,empty String or an 'NaN' amount
     * it would return null. If a locale passed is null it would pick the user locale by default.
     *
     * @param amount
     * @param locale
     * @return String
     */

    @Override
    @Transactional(value=TxType.NOT_SUPPORTED)
    public String formatMoney(String amount, Locale locale) { 
    	if (locale == null) {
            locale = getUserLocale();
    }
    	return MoneyUtils.formatMoney(amount, locale);
    }
    
    @Override
    public Money parseMoney(String amount, Locale locale) {

        String[] moneyStr = amount.split(MoneyUtils.MONEY_DELIMITER);
        if (locale == null) {
            getUserLocale();
        }
        if(moneyStr.length == 3){
        	BigDecimal parsedAmount = parseCurrency(moneyStr[2]);
            // String newStr = text.replaceAll("[^\\d.]+", "");        			
        	Money money = new MoneyBuilder().setNonBaseAmountvalue(parsedAmount.toString()).getMoney();
            money.getNonBaseAmount().setCurrency(moneyStr[1]);
            
            Currency currency = getCurrencyByCurrencyCode(money.getNonBaseAmount().getCurrencyCode());
            BigDecimal rate = getAppEffectiveRate(currency, new Long(moneyStr[0]));
            if(rate!=null){
            	try {
                    BigDecimal baseAmount = money.getNonBaseAmount().getValue().multiply(rate);
                    money.getBaseAmount().setValue(baseAmount.toString());
                    money.getBaseAmount().setCurrency(Money.getBaseCurrency());
                    return money;
                } catch (Exception e) {
                    BaseLoggers.exceptionLogger.error("Exception occured while accessing conversion rate for "
                            + money.getNonBaseAmount().getCurrencyCode() + "' :" + e.getMessage());
                }     
            }
            return setBaseAmount(money);
        }else{
        	BigDecimal parsedAmount = parseCurrency(moneyStr[1]);
            // String newStr = text.replaceAll("[^\\d.]+", "");
            Money money = new MoneyBuilder().setNonBaseAmountvalue(parsedAmount.toString()).getMoney();
            money.getNonBaseAmount().setCurrency(moneyStr[0]);
            return setBaseAmount(money);
        }
    }

    @Override
    public String formatBigDecimal(String amount, Locale locale) {

        if (amount != null && !(amount.equals("") && amount != "NaN")) {
            if (locale == null) {
                locale = getUserLocale();
            }
            return MoneyUtils.formatBigDecimalByLocale(amount, locale);
        } else {
            return null;
        }

    }

    @Override
    public String formatMoney(String amount, Locale locale, String currCode) {

        if (amount != null && !(amount.equals("") && amount != "NaN")) {
            if (locale == null) {
                locale = getUserLocale();
            }
            return MoneyUtils.formatMoneyByLocale(new BigDecimal(amount).toPlainString(), locale, currCode);
        } else {
            return null;
        }

    }

    @Override
    public String formatMoneyAmount(String amount, Locale locale, String currencyCode) {
    	Locale uLocale = locale;
        if ( StringUtils.isBlank(amount) ) {
        	return null;
        }

        if (uLocale == null) {
        	uLocale = getUserLocale();
        }
        return MoneyUtils.formatMoneyAmount(amount, uLocale, currencyCode);
    }

    @Override
    public BigDecimal getEffectiveRate(Currency currency) {
    	
    	BigDecimal effectiveConversionFactor  = currencyCacheService.getEffectiveConversionFactor(currency.getId());
    	if(effectiveConversionFactor != null) {
    		return effectiveConversionFactor;
    	}
    	
        NamedQueryExecutor<ConversionRate> executor = new NamedQueryExecutor<ConversionRate>("Currency.getEffectiveRate")
                .addParameter("currencyId", currency.getId()).addParameter("today", DateUtils.getCurrentUTCTime().toDate());
        List<ConversionRate> rates = entityDao.executeQuery(executor);
        if (rates != null && !rates.isEmpty()) {
            return rates.get(0).getConversionFactor();
        }
        return null;
    }
    
    @Override
    public ConversionRate getConversionRateBasedOnEffectiveDate(Long currencyId,DateTime effectiveDate){
    	
    	ConversionRate conversionRate  = currencyCacheService.getConversionFactorForSpecifiedDate(currencyId, effectiveDate);
    	if(conversionRate != null) {
    		return conversionRate;
    	}
    	
    	return moneyDao.getConversionRateBasedOnEffectiveDate(currencyId, effectiveDate.toDate());
    }
    
    @Override
    public BigDecimal getAppEffectiveRate(Currency currency, Long appId) {  	
    	Map<String, BigDecimal> appEffectiveRateMap = appEffectiveRateMapHolder.get();
    	
    	//if found in Thread Local Map
    	BigDecimal effectiveRate =  appEffectiveRateMap.get(currency.getIsoCode() + "_" + appId);
		if (effectiveRate != null) {
			return effectiveRate;
		}
		
		//Else fetch from DB
        NamedQueryExecutor<BigDecimal> executor = new NamedQueryExecutor<BigDecimal>("Currency.getAppEffectiveRate")
                .addParameter("currency", currency.getIsoCode()).addParameter("appId", appId);
        List<BigDecimal> rate = entityDao.executeQuery(executor);
        
        if (rate != null && !rate.isEmpty()) {
        	appEffectiveRateMap.put(currency.getIsoCode() + "_" + appId, rate.get(0));
        	effectiveRate = rate.get(0);
        }else{
        	
        	NamedQueryExecutor<Long> exe = new NamedQueryExecutor<Long>("Currency.getSubLoanIdFromAppId")
                     .addParameter("appId", appId);
            List<Long> subLoanIds = entityDao.executeQuery(exe);
            if(!subLoanIds.isEmpty()){
            	effectiveRate = getEffectiveRate(currency);
            	Long subLoanId = subLoanIds.get(0);
                ConversionRateMap convRateMap = new ConversionRateMap();
                convRateMap.setConvRate(effectiveRate);
                convRateMap.setCurrency(currency.getIsoCode());
                convRateMap.setSubLoanId(subLoanId);
                entityDao.persist(convRateMap);
            }
        }
        return effectiveRate; 
    }

    @Override
    public Money setNonBaseAmount(String nonBaseCurrCode, Money money, BigDecimal convRate) {
        if (money == null) {
            return null;
        }
        Currency currency = getCurrencyByCurrencyCode(nonBaseCurrCode);
        
        BigDecimal rate = convRate;
        if(rate == null)
        	rate = getEffectiveRate(currency);
        try {
            if (!rate.equals(BigDecimal.ZERO)) {
                BigDecimal nonBaseAmount = money.getBaseAmount().getValue().divide(rate, 10, RoundingMode.FLOOR );
                money.getNonBaseAmount().setCurrency(currency.getIsoCode());
                money.getNonBaseAmount().setValue(nonBaseAmount.toString());
            }
        } catch (Exception e) {

            BaseLoggers.exceptionLogger.error("Exception occured while accessing conversion rate for " + nonBaseCurrCode
                    + "' :" + e.getMessage());
        }

        return money;

    }

    @Override
    public Money setBaseAmount(Money money, BigDecimal convRate) {
        if (money == null) {
            return null;
        }
        Currency currency = getCurrencyByCurrencyCode(money.getNonBaseAmount().getCurrencyCode());
        BigDecimal rate = convRate;
        if(rate == null)
        	rate = getEffectiveRate(currency);
        try {
            BigDecimal baseAmount = money.getNonBaseAmount().getValue().multiply(rate);
            money.getBaseAmount().setValue(baseAmount.toString());
            money.getBaseAmount().setCurrency(Money.getBaseCurrency());
        } catch (Exception e) {

            BaseLoggers.exceptionLogger.error("Exception occured while accessing conversion rate for "
                    + money.getNonBaseAmount().getCurrencyCode() + "' :" + e.getMessage());
        }

        return money;

    }
    
    @Override
    public Money setNonBaseAmount(String nonBaseCurrCode, Money money) {
        if (money == null) {
            return null;
        }
        Currency currency = getCurrencyByCurrencyCode(nonBaseCurrCode);
        BigDecimal rate = getEffectiveRate(currency);
        try {
            if (!rate.equals(BigDecimal.ZERO)) {
                BigDecimal nonBaseAmount = money.getBaseAmount().getValue().divide(rate, 10, RoundingMode.FLOOR );
                money.getNonBaseAmount().setCurrency(currency.getIsoCode());
                money.getNonBaseAmount().setValue(nonBaseAmount.toString());
            }
        } catch (Exception e) {

            BaseLoggers.exceptionLogger.error("Exception occured while accessing conversion rate for " + nonBaseCurrCode
                    + "' :" + e.getMessage());
        }

        return money;

    }

    @Override
    public Money setBaseAmount(Money money) {
        if (money == null) {
            return null;
        }
        Currency currency = getCurrencyByCurrencyCode(money.getNonBaseAmount().getCurrencyCode());
        BigDecimal rate = getEffectiveRate(currency);
        try {
            BigDecimal baseAmount = money.getNonBaseAmount().getValue().multiply(rate);
            money.getBaseAmount().setValue(baseAmount.toString());
            money.getBaseAmount().setCurrency(Money.getBaseCurrency());
        } catch (Exception e) {

            BaseLoggers.exceptionLogger.error("Exception occured while accessing conversion rate for "
                    + money.getNonBaseAmount().getCurrencyCode() + "' :" + e.getMessage());
        }

        return money;

    }

    @Override
    public boolean deleteConversionRateFromCurrency(Long rateID) {
        if (rateID != null) {
            ConversionRate fundSource = entityDao.find(ConversionRate.class, rateID);
            entityDao.delete(fundSource);
            return true;
        } else {
            return false;
        }

    }

    @Override
    public BigDecimal formatFractionalPartOfMoney(BigDecimal moneyValue, Integer decimalPlaces) {
        if (decimalPlaces == null) {
            decimalPlaces = getDecimalPlacesForDefaultBaseCurrency();
        }
        moneyValue = moneyValue.setScale(decimalPlaces.intValue(), BigDecimal.ROUND_HALF_EVEN);
        return moneyValue;
    }

    @Override
    public int getDecimalPlacesForDefaultBaseCurrency() {
        int decimalPlaces = 2;  // Default value
        Currency baseCurrency = getCurrencyByCurrencyCode(Money.getBaseCurrency().getCurrencyCode());
        if (baseCurrency != null) {
            decimalPlaces = baseCurrency.getDecimalPlaces();
        }
        return decimalPlaces;
    }

    @Override
    public Money formatFractionalPartOfMoney(Money money, Integer decimalPlaces) {
        BigDecimal baseAmountValue = money.getBaseAmount().getValue();
        baseAmountValue = formatFractionalPartOfMoney(baseAmountValue, decimalPlaces);
        money.getBaseAmount().setNumber(baseAmountValue);
        return money;
    }
    
    private List<Integer> getApprovalStatusList() {
        List<Integer> statusList = new ArrayList<Integer>();
        statusList.add(ApprovalStatus.APPROVED);
        statusList.add(ApprovalStatus.APPROVED_MODIFIED);
        statusList.add(ApprovalStatus.APPROVED_DELETED);
        statusList.add(ApprovalStatus.APPROVED_DELETED_IN_PROGRESS);
        return statusList;
    }
    
    @Override
    public Money getMoneyFromBaseAmount(BigDecimal baseAmount, String nonBasecurrency){
    	if(nonBasecurrency==null)
    		nonBasecurrency = Money.getBaseCurrency().getCurrencyCode();
    	String currency = nonBasecurrency;
    	BigDecimal convRate = null;
    	if(nonBasecurrency.contains("~")){
    		String temp[] = nonBasecurrency.split("~");
    		currency = temp[0];
    		convRate = new BigDecimal(temp[1]);
    	}
    	Money money = new MoneyBuilder().setBaseAmountvalue(baseAmount.toPlainString()).getMoney();
    	setNonBaseAmount(currency, money, convRate);    	
    	return money;
    }
    
    @Override
    public Money getMoneyFromNonBaseAmount(BigDecimal nonBaseAmount, String nonBasecurrency){
    	if(nonBasecurrency==null)
    		nonBasecurrency = Money.getBaseCurrency().getCurrencyCode();
    	String currency = nonBasecurrency;
    	BigDecimal convRate = null;
    	if(nonBasecurrency.contains("~")){
    		String temp[] = nonBasecurrency.split("~");
    		currency = temp[0];
    		convRate = new BigDecimal(temp[1]);
    	}
    	Money money = new MoneyBuilder().setNonBaseAmountvalue(nonBaseAmount.toPlainString()).setNonBaseAmountCurrency(currency).getMoney();
    	setBaseAmount(money, convRate);
    	return money;
    }

    @Override
    public Currency getBaseCurrency() {
    	return currencyCacheService.getBaseCurrency();
    }
    
    @Override
    public Map<String,Object> fetchAmountDetails(Currency applicableCurrency) {
    	return fetchAmountDetails(applicableCurrency, null);
	}
    
    @Override
	public Map<String, Object> fetchAmountDetails(Currency applicableCurrency, Map<String, String> amountConfigurations) {
    	Map<String,Object> amountMap = new HashMap<>();
		int decimalPlaces = 0 ;
		if (notNull(applicableCurrency)) {
			decimalPlaces=applicableCurrency.getDecimalPlaces();
		} else {
			decimalPlaces=getBaseCurrency().getDecimalPlaces();
		}
		amountMap.put(PRECISION, decimalPlaces);
		amountMap.put(MULTIPLESOFF, fetchMultiplesOffFromPrecision(decimalPlaces));
		if (amountConfigurations == null || amountConfigurations.isEmpty()) {
			amountConfigurations = fetchConfigurationProperties();
		}
		amountMap.put(CONFIG_DIGIT_GROUPING_CONSTANT_CONFIG_KEY, amountConfigurations.get(CONFIG_DIGIT_GROUPING_CONSTANT_CONFIG_KEY));
		amountMap.put(CONFIG_DECIMAL_GROUPING_CONSTANT_CONFIG_KEY, amountConfigurations.get(CONFIG_DECIMAL_GROUPING_CONSTANT_CONFIG_KEY));
		amountMap.put(CONFIG_AMOUNT_FORMAT_CONSTANT_CONFIG_KEY, amountConfigurations.get(CONFIG_AMOUNT_FORMAT_CONSTANT_CONFIG_KEY));		
		return amountMap;
	}
	
	protected double fetchMultiplesOffFromPrecision(int precision) {
		double multiplesOff=1;
		if (precision!=0) {		
			multiplesOff= (1/Math.pow(10, (double)precision));
		}
		return multiplesOff;
	}
	
	//Amount Formatting 
	
	@Override
	public String roundAndFormatNumber(BigDecimal unformattedNumber, Map<String,Object> amountMap, RoundingMode roundingMethod) {
		if ((isNull(amountMap.get(PRECISION))) || (isNull(amountMap.get(MULTIPLESOFF)))) {
			 Map<String,Object> fetchedAmountMap=fetchAmountDetails((Currency)amountMap.get(APPLICABLE_CURRENCY));
			 if(amountMap!=null) {
				 if(amountMap.get(PRECISION)!=null) {
					 fetchedAmountMap.put(PRECISION,amountMap.get(PRECISION));
				 }
				 if(amountMap.get(MULTIPLESOFF)!=null) {
					 fetchedAmountMap.put(MULTIPLESOFF,amountMap.get(MULTIPLESOFF));
				 }
			 }
			 amountMap = fetchedAmountMap;
		}
		BigDecimal roundedNumber = CoreMathUtility.getRoundedValue(unformattedNumber, new BigDecimal(amountMap.get(MULTIPLESOFF).toString()), roundingMethod);
		return getFormattedNumber(roundedNumber,amountMap);
	}
	
	
	
	public String getFormattedNumber(BigDecimal rawNumber, Map<String, Object> amountMap) {
		BigDecimal unFormattedNumber = rawNumber;
		if (unFormattedNumber == null) {
			unFormattedNumber = BigDecimal.ZERO;
		}
		return MoneyUtils.formatMoneyAmount(unFormattedNumber, getSystemLocale(),
				Money.getBaseCurrency().getCurrencyCode(), (formatter) -> {
					if(amountMap.get(PRECISION)!=null) {
						formatter.setMaximumFractionDigits((int) amountMap.get(PRECISION));
						formatter.setMinimumFractionDigits((int) amountMap.get(PRECISION));
					}
				});
	}	
	
	
	
	
	
	
	
	
	public Map<String,String> fetchConfigurationProperties() {

		String digitGroupingConstant = CONFIG_DIGIT_GROUPING_CONSTANT;
		String decimalGroupingConstant = CONFIG_DECIMAL_GROUPING_CONSTANT;
		String amountFormatConstant = CONFIG_AMOUNT_FORMAT_CONSTANT;
		Map<String, String> amountConfigurations = new HashMap<String, String>();
		amountConfigurations.put(
				CONFIG_DIGIT_GROUPING_CONSTANT_CONFIG_KEY,
				digitGroupingConstant);
		amountConfigurations
				.put(CONFIG_DECIMAL_GROUPING_CONSTANT_CONFIG_KEY,
						decimalGroupingConstant);
		amountConfigurations
		.put(CONFIG_AMOUNT_FORMAT_CONSTANT_CONFIG_KEY,
				amountFormatConstant);
//		ConfigurationGroup configurationGroup = configurationService
//				.getConfigurationGroupFor(SystemEntity.getSystemEntityId());
//
//		if (isNull(configurationGroup)
//				|| !hasElements(configurationGroup.getConfiguration())) {
//			return amountConfigurations;
//		}
//
//		List<Configuration> configurations = configurationGroup
//				.getConfiguration();		
//		Hibernate.initialize(configurations);
//		for (Configuration configuration : configurations) {

		EntityId systemEntityId = SystemEntity.getSystemEntityId();
		ConfigurationVO configurationVO = configurationService.getConfigurationPropertyFor(systemEntityId, CONFIG_DIGIT_GROUPING_CONSTANT_CONFIG_KEY);
			
		if (notNull(configurationVO)
				&&notNull(configurationVO.getPropertyValue())) {
				amountConfigurations
						.put(CONFIG_DIGIT_GROUPING_CONSTANT_CONFIG_KEY,
								configurationVO
										.getPropertyValue());
			
		}
		configurationVO = configurationService.getConfigurationPropertyFor(systemEntityId, CONFIG_DECIMAL_GROUPING_CONSTANT_CONFIG_KEY);
		if (notNull(configurationVO)
				&&notNull(configurationVO.getPropertyValue())) {
				amountConfigurations
						.put(CONFIG_DECIMAL_GROUPING_CONSTANT_CONFIG_KEY,
								configurationVO
										.getPropertyValue());
			
		}
		configurationVO = configurationService.getConfigurationPropertyFor(systemEntityId, CONFIG_AMOUNT_FORMAT_CONSTANT_CONFIG_KEY);
		if (notNull(configurationVO)
				&&notNull(configurationVO.getPropertyValue())) {
				amountConfigurations
						.put(CONFIG_AMOUNT_FORMAT_CONSTANT_CONFIG_KEY,
								configurationVO
										.getPropertyValue());
			
		}						
			
		
		return amountConfigurations;
	}
	
	 
}
