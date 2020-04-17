/**
 * 
 */
package com.nucleus.finnone.pro.general.vo;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Locale;

import com.nucleus.currency.Currency;
import com.nucleus.finnone.pro.general.util.CoreMathUtility;

/** 
 * @author Sourabh Aggarwal
 * VO to hold values for currency
 * Moved from common masters to FW
 */

public class CurrencyVO implements Serializable{ 
	
  /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
public static final String CURR_CODE_INDIA = "INR";
  public static final String CURR_CODE_US = "USD";
  private String isoCode;
  private String isoNumber;
  private Integer decimalPlaces;
  private String currencyName;
  private String symbol;
  private Locale locale;
  private Boolean isBaseCurrency;
  private BigDecimal multiplesOff = BigDecimal.ZERO;
 

  private String roundMethod = "RO";

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
	
	public Integer getDecimalPlaces() {
		return decimalPlaces == null? 0 : decimalPlaces;
	}
	
	public void setDecimalPlaces(Integer decimalPlaces) {
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
	
	public Locale getLocale() {
		return locale;
	}
	
	public void setLocale(Locale locale) {
		this.locale = locale;
	}
	
	public Boolean getIsBaseCurrency() {
		return isBaseCurrency;
	}
	
	public void setIsBaseCurrency(Boolean isBaseCurrency) {
		this.isBaseCurrency = isBaseCurrency;
	}
	
	public BigDecimal getMultiplesOff() {
		return multiplesOff;
	}
	
	public void setMultiplesOff(BigDecimal multiplesOff) {
		this.multiplesOff = multiplesOff;
	}

	
	public String getRoundMethod() {
		return roundMethod;
	}
	
	public void setRoundMethod(String roundMethod) {
		this.roundMethod = roundMethod;
	}
	
	public static String getCurrCodeIndia() {
		return CURR_CODE_INDIA;
	}
	
	public static String getCurrCodeUs() {
		return CURR_CODE_US;
	}
	
	public void populate(Currency currency){
		this.isoCode = currency.getIsoCode();
		this.isoNumber = currency.getIsoNumber();
		this.decimalPlaces = currency.getDecimalPlaces();
		this.currencyName = currency.getCurrencyName();
		this.symbol = currency.getSymbol();
		this.locale = currency.getLocale();
		this.isBaseCurrency = currency.getIsBaseCurrency();
		this.multiplesOff = CoreMathUtility.getMultipleOf(currency.getDecimalPlaces());

	}
	
}