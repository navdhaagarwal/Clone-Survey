package com.nucleus.money;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.joda.time.DateTime;

import com.nucleus.core.money.entity.Money;
import com.nucleus.currency.ConversionRate;
import com.nucleus.currency.Currency;

public interface MoneyService {

    /**
     * This method will the retrieve the list of existing currencies
     *
     * @return 
     */
    public List<Currency> retrieveAllActiveCurrencies();

    /**
     * This method fetches the Currency from an existing currency id
     *
     * @param id 
     * @return Currency
     */
    public Currency getCurrencyById(long id);

    /**
     * This method will retrieve the Currency from a country's ISO code
     *
     * @param ISOCode 
     * @return Currency
     */
    public Currency getCurrencyByCurrencyCode(String ISOCode);

    /**
     * This method parses the amount as per the locale of user
     *
     * @param amount
     * @return BigDecimal
     */
    public BigDecimal parseCurrency(String amount);

    /**
     * This method formats money by Locale.
     *
     * @param money
     * @param locale
     * @return String
     */
    public String formatMoney(String money, Locale locale, String currCode);
    
    /**
     * This method returns formatted amount as per user locale and fraction digits as per currency.
     * @param amount
     * @param locale
     * @param currencyCode
     * @return
     */
    public String formatMoneyAmount(String amount, Locale locale, String currencyCode);

    public BigDecimal getEffectiveRate(Currency currency);

    public String formatMoney(String amount, Locale locale);

    public Money setNonBaseAmount(String nonBaseCurrCode, Money money);

    public Money setBaseAmount(Money money);

    public Money setNonBaseAmount(String nonBaseCurrCode, Money money, BigDecimal convRate);

    public Money setBaseAmount(Money money, BigDecimal convRate);

    public String formatBigDecimal(String amount, Locale locale);
    
    public Money parseMoney(String amount, Locale locale);

    public boolean deleteConversionRateFromCurrency(Long rateID);

    /**
     * Formats fractional part of Money's value. Number of digits after decimal place will be decided by "decimalPlaces" parameter. 
     * Default "decimalPlaces" value of base currency (from Currency table) will be used, if decimalPlaces is null
     *
     * @param moneyValue the money's value
     * @param decimalPlaces the decimal places
     * @return the big decimal
     */
    public BigDecimal formatFractionalPartOfMoney(BigDecimal moneyValue, Integer decimalPlaces);

    /**
     * Gets the decimal places for default base currency.
     *
     * @return the default decimal places for base currency
     */
    public int getDecimalPlacesForDefaultBaseCurrency();

    /**
     * Formats fractional part of Money's value. Number of digits after decimal place will be decided by "decimalPlaces" parameter. 
     * Default "decimalPlaces" value of base currency (from Currency table) will be used, if decimalPlaces is null
     *
     * @param money the money
     * @param decimalPlaces the decimal places
     * @return the money
     */
    public Money formatFractionalPartOfMoney(Money money, Integer decimalPlaces);

	Money getMoneyFromBaseAmount(BigDecimal baseAmount, String nonBasecurrency);

	Money getMoneyFromNonBaseAmount(BigDecimal nonBaseAmount, String nonBasecurrency);

	BigDecimal getAppEffectiveRate(Currency currency, Long appId);
	
	@Deprecated
	Map<String,Object> fetchAmountDetails(Currency applicableCurrency);
	
	String roundAndFormatNumber(BigDecimal unformattedNumber, Map<String,Object> amountMap, RoundingMode roundingMethod);
	
	Currency getBaseCurrency();

	ConversionRate getConversionRateBasedOnEffectiveDate(Long currencyId,DateTime effectiveDate);

	@Deprecated
	Map<String, String> fetchConfigurationProperties();

	@Deprecated
	public Map<String, Object> fetchAmountDetails(Currency currency, Map<String, String> cachedConfiguration);
	
}
