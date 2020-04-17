/**
 * This file and a proportion of its content is copyright of Nucleus Software Exports Limited - � 2012. All rights
 * reserved.
 * Any redistribution or reproduction of part or all of the contents in any form is prohibited other than the following:
 * - you cannot print or download to a local hard disk extract contents either part or full for personal/ commercial/
 * academic or any other use
 * - you may not copy the content to individual/ third parties for any type of use, either as compiled or source format
 * without the knowledge and consent of Nucleus Software
 * - You may not, except with our express written permission, distribute or commercially exploit the content. Nor may you
 * transmit it or store it in any other web site or other form of electronic retrieval system.
 */
package com.nucleus.core.money.utils;

import static com.nucleus.logging.BaseLoggers.exceptionLogger;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.ParseException;
import java.util.Currency;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.ibm.icu.text.DecimalFormat;
import com.ibm.icu.text.DecimalFormatSymbols;
import com.ibm.icu.text.NumberFormat;
import com.ibm.icu.text.RuleBasedNumberFormat;
import com.nucleus.core.misc.util.StringUtil;
import com.nucleus.core.money.entity.Money;
import com.nucleus.logging.BaseLoggers;

/**
 * A utility class to provide some basic operations on money.
 * 
 * @author Nucleus Software Exports Limited
 */
public class MoneyUtils {

    public static final String    MONEY_DELIMITER = "~";
    private static final String[] specialNames    = { "", " thousand", " million", " billion", " trillion", " quadrillion",
            " quintillion"                       };
    private static final String[] tensNames       = { "", " ten", " twenty", " thirty", " fourty", " fifty", " sixty",
            " seventy", " eighty", " ninety", "hundred" };

    private static final String[] numNames        = { "", " one", " two", " three", " four", " five", " six", " seven",
            " eight", " nine", " ten", " eleven", " twelve", " thirteen", " fourteen", " fifteen", " sixteen", " seventeen",
            " eighteen", " nineteen"             };

    //Arabic Digits Unicode Notations Constants
    private static final String ARABIC_ZERO    					= "\u0660";
    private static final String ARABIC_ONE     					= "\u0661";
    private static final String ARABIC_TWO     					= "\u0662";
    private static final String ARABIC_THREE   					= "\u0663";
    private static final String ARABIC_FOUR    					= "\u0664";
    private static final String ARABIC_FIVE    					= "\u0665";
    private static final String ARABIC_SIX     					= "\u0666";
    private static final String ARABIC_SEVEN   					= "\u0667";
    private static final String ARABIC_EIGHT   					= "\u0668";
    private static final String ARABIC_NINE   					= "\u0669";
    private static final String ARABIC_GROUPING_SEPARATOR		= "\u066c";
    private static final String ARABIC_DECIMAL_SEPARATOR 		= "\u066b";
    //Thai Digits Unicode Notations Constants
    private static final String THAI_ZERO  						= "\u0e50";
    public static final String THAI_ONE   						= "\u0e51";
    public static final String THAI_TWO   						= "\u0e52";
    public static final String THAI_THREE 						= "\u0e53";
    public static final String THAI_FOUR  						= "\u0e54";
    public static final String THAI_FIVE  						= "\u0e55";
    public static final String THAI_SIX   						= "\u0e56";
    public static final String THAI_SEVEN 						= "\u0e57";
    public static final String THAI_EIGHT 						= "\u0e58";
    public static final String THAI_NINE  						= "\u0e59";
    //Indo Arabic Digits Constants    
    public static final String INDO_ARABIC_ZERO    				= "0";
    public static final String INDO_ARABIC_ONE     				= "1";
    public static final String INDO_ARABIC_TWO     				= "2";
    public static final String INDO_ARABIC_THREE   				= "3";
    public static final String INDO_ARABIC_FOUR    				= "4";
    public static final String INDO_ARABIC_FIVE    				= "5";
    public static final String INDO_ARABIC_SIX     				= "6";
    public static final String INDO_ARABIC_SEVEN   				= "7";
    public static final String INDO_ARABIC_EIGHT   				= "8";
    public static final String INDO_ARABIC_NINE    				= "9";
    public static final String INDO_ARABIC_GROUPING_SEPARATOR  	= ",";
    public static final String INDO_ARABIC_DECIMAL_SEPARATOR 	= ".";
    
	private static Map<String, String> localeMap = null;
	
	/**
	 * this method formats money amount as per locale, also it can extract currency
	 * by splitting amount string with MoneyUtils.MONEY_DELIMITER 
	 * 
	 * @param amount
	 * @param locale
	 * @return CURRENCY_CODE~FORMATTED_AMOUNT
	 */
	public static String formatMoney(String amount, Locale locale) {
		String currencyCode = Money.getBaseCurrency().getCurrencyCode();
        if (amount != null && !(amount.equals("") && amount != "NaN")) {
            if (amount.contains(MoneyUtils.MONEY_DELIMITER)) {
                String[] amountVar = amount.split(MoneyUtils.MONEY_DELIMITER);
                amount = amountVar[1];
                currencyCode = amountVar[0];
            }
            return formatMoneyByLocale(amount, locale, currencyCode);
        } else {
            return null;
        }
	} 
	
    /**
     * Formats money(in form of @see Money) with specified locale.
     * 
     * @param money
     * @param locale
     * @return
     */

    public static String formatMoneyByLocale(Money money, Locale locale) {
        if (money != null) {
            if (money.getNonBaseAmount() == null || money.getNonBaseAmount().getValue() == null) {
                String moneyStr = money.getBaseAmount().getValue().toPlainString();
                return formatMoneyByLocale(moneyStr, locale, money.getBaseAmount().getCurrency().getCurrencyCode());
            }
            BigDecimal moneyStr = money.getNonBaseAmount().getValue();
            return getAmountWithCurrencyCode(formatMoneyAmount(moneyStr, locale, 
            		money.getNonBaseAmount().getCurrency().getCurrencyCode()),
            		money.getNonBaseAmount().getCurrency().getCurrencyCode());
        } else {

            return "";
        }

    }

	/**
     * Formats money(in form of string) with specified locale.
     * If currency code is passed it will prepend currency code with money delimiter "~"
     * @param moneyString
     * @param locale
     * @return CURRENCY_CODE~FORMATTED_AMOUNT
     */
    public static String formatMoneyByLocale(String moneyString, Locale locale, String currencyCode) {

        if (StringUtils.isBlank(moneyString) && locale == null) {
        	return null;
        }
        
    	if (!StringUtils.isBlank(moneyString)) {
    		//to avoid unwanted decimal points
            NumberFormat parser = NumberFormat.getInstance(locale);
            try {
                Number amount = parser.parse(moneyString);
                moneyString = getMoneyStringFromNumber(amount);
            } catch (ParseException e) {
                BaseLoggers.flowLogger.error("Exception in converting money string",e);
            }
        }
        String currencyCodeForFormatting=currencyCode;
        if(currencyCodeForFormatting==null) {
        	currencyCodeForFormatting=Money.getBaseCurrency().getCurrencyCode();
        }
    	String currencyOut = formatMoneyAmount(moneyString, locale, currencyCodeForFormatting);
    	return getAmountWithCurrencyCode(currencyOut,currencyCode);
    }
    
    private static String getMoneyStringFromNumber(Number amount) {

		BigDecimal bigDecimal = null;
		
		if (amount instanceof Long) {
			bigDecimal = new BigDecimal(amount.longValue());
		} else if (amount instanceof com.ibm.icu.math.BigDecimal) {
			bigDecimal = ((com.ibm.icu.math.BigDecimal) amount).toBigDecimal();
		} else {
			bigDecimal = new BigDecimal(amount.doubleValue());
		}

		return bigDecimal.stripTrailingZeros().toPlainString();
	}
    
    private static String getAmountWithCurrencyCode(String amount,String currencyCode) {
        if (StringUtils.isNotBlank(currencyCode)) {
            return currencyCode + MONEY_DELIMITER + amount;
        }else {
        	return amount;
        }
    }
    /**
     * This method returns formatted amount as per user locale and fraction digits as per currency.
     * @param amount
     * @param locale
     * @param currencyCode
     * @return
     */
    public static String formatMoneyAmount(String amount, Locale locale, String currencyCode ,FormatterCustomizer ... customizers) {
        return doFormatting(new BigDecimal(amount),locale,currencyCode , customizers);
    }
    
    /**
     * This method returns formatted amount as per user locale and fraction digits as per currency.
     * @param amount
     * @param locale
     * @param currencyCode
     * @return
     */
    public static String formatMoneyAmount(BigDecimal amount, Locale locale, String currencyCode, FormatterCustomizer ... customizers) {
        return doFormatting(amount, locale, currencyCode ,customizers);
    }
    
    private static String doFormatting(BigDecimal amount, Locale locale, String currencyCode , FormatterCustomizer ... customizers) {
        NumberFormat formatter=getFormatter(locale,currencyCode);
        if(customizers!=null && customizers.length>0) {
        	for(int i=0;i<customizers.length;i++) {
        		customizers[i].customize(formatter);
        	}
        }
        String currencyOut = (formatter.format(amount)).trim();
        
        // call method replaceNonIndoArabicDigits only if formatted amount contains Arabic or Thai symbols.
        if(currencyOut.matches(".*[\u0660-\u0669\u066c\u066b\u0e50-\u0e59].*")){
        	currencyOut = replaceNonIndoArabicDigits(currencyOut);
        }
        currencyOut = currencyOut.replaceAll("[^-0-9.,()]+", "");
        return currencyOut;
    }


    
    private static NumberFormat getFormatter(Locale locale, String currencyCode) {
		return getNumberFormat(locale, currencyCode);
	}

	private static NumberFormat getNumberFormat(Locale locale, String currencyCode) {
		NumberFormat formatter = NumberFormat.getCurrencyInstance(locale);
        
        if (StringUtils.isNotBlank(currencyCode)) {
            Currency currency = Currency.getInstance(currencyCode);
            formatter.setMinimumFractionDigits(currency.getDefaultFractionDigits());
            formatter.setMaximumFractionDigits(currency.getDefaultFractionDigits());
        }

        DecimalFormatSymbols decimalFormatSymbols = ((DecimalFormat) formatter).getDecimalFormatSymbols();
        decimalFormatSymbols.setCurrencySymbol("");
        ((DecimalFormat) formatter).setDecimalFormatSymbols(decimalFormatSymbols);
        
        return formatter;
    }
    
	/**
	 * validate and delegate to formatMoneyAmount(moneyString, locale, currencyCode); add currency symbol
	 * Also this will always prepend base currency symbol to formatted amount.
	 * @param moneyString
	 * @param locale
	 * @return
	 */
    public static String formatMoneyByLocale(String moneyString, Locale locale) {
        if (moneyString != null && !moneyString.equals("") && locale != null) {

            String currencyOut = "";
            if (moneyString.contains(MONEY_DELIMITER)) {
                String[] moneyVar = moneyString.split(MONEY_DELIMITER);
                if (moneyVar.length == 2) {
                    currencyOut = formatMoneyByLocale(moneyVar[1], locale, null);
                }
            } else {
                currencyOut = formatMoneyByLocale(moneyString, locale, null);
            }

            return Money.getBaseCurrency().getCurrencyCode() + MONEY_DELIMITER + currencyOut;

        } else {

            return null;
        }

    }

    /**
     * Formats amount based on locale without currency.
     * @param moneyString
     * @param locale
     * @return
     */
    public static String formatBigDecimalByLocale(String moneyString, Locale locale) {

    	return formatMoneyByLocale(moneyString, locale, null);

    }

    /**
     * This method parses the amount as per the locale provided 
     * 
     * @param amount without currency symbol
     * @return BigDecimal
     */
    public static BigDecimal parseCurrencyByLocale(String amount, Locale locale) {
        if (amount != null && !(amount.equals("")) && !(amount.equals("NaN")) && locale != null) {
            amount = amount.trim();
            boolean isNegative = false;
            if (amount.startsWith("-")) {
                amount = amount.substring(1);
                isNegative = true;
            }

            NumberFormat parser = NumberFormat.getInstance(locale);
            Number parsedAmount = 0;
            try {
                parsedAmount = parser.parse(amount);
            } catch (ParseException e) {
                exceptionLogger.error("Exception : ", e);
            }
            if (isNegative) {
                BigDecimal parsedDecimal = BigDecimal.valueOf(parsedAmount.doubleValue());
                return parsedDecimal.negate();
            }
            return new BigDecimal(parsedAmount.toString());
        } else {
            return null;
        }
    }

    /* 
     * We were trying to use com.ibm.icu.text.RuleBasedNumberFormat to convert currency into words but there was a problem: It was generating 
     * 12342718.282 = twelve million three hundred forty-two thousand seven hundred eighteen point two eight two .
     * So we are using a user defined method to convert number into words.The use of RuleBasedNumberFormat can be done here by applying rule based customizations.
     * This will be done while re-factoring.
     * 
     * public static void main(String ar[]){
    BigDecimal big = new BigDecimal(51200.09);
    double num = 12342718.282;
    Locale locale = new Locale("fr" ,"FR");
    NumberFormat formatter = 
        new RuleBasedNumberFormat(locale,RuleBasedNumberFormat.SPELLOUT);
    String result = formatter.format(num);
    }*/

    // International conversion of number to words starts
    // currently not being used converts word to international System
    public static String convertNumberToWordsInternationalSystem(BigDecimal value) {
        if (value == null)
            return "Zero";
        return convert(value.intValue());

    }

    private static String convert(int number) {

        if (number == 0) {
            return "zero";
        }

        String prefix = "";

        if (number < 0) {
            number = -number;
            prefix = "negative";
        }

        String current = "";
        int place = 0;

        do {
            int n = number % 1000;
            if (n != 0) {
                String s = convertLessThanOneThousand(n);
                current = s + specialNames[place] + current;
            }
            place++;
            number /= 1000;
        } while (number > 0);

        return StringUtil.capitalizeFirstLetter((prefix + current).trim());
    }

    private static String convertLessThanOneThousand(int num) {
    	  String current = "";
      
        if (num % 100 < 20) {
            current = numNames[num % 100];
            num /= 100;
        } else {
            current = numNames[num % 10];
            num /= 10;

            current = tensNames[num % 10] + current;
            num /= 10;            
        }
      
        if (num == 0)
            return current;
        return numNames[num] + " hundred" + current;
    }

    // International conversion of number to words ends

    // Indian conversion of number to words starts

    public static String convertNumberToWords(BigDecimal value) {
        if (value == null) {
            return "Zero";
        }

        value = value.stripTrailingZeros();
        BigDecimal fraction = new BigDecimal("0");
        String decimal = value.toPlainString();
        String paiseInWords = "";
        String prefix = "";
        String current = "";

        if (value.compareTo(BigDecimal.ZERO) == 0) {
            current = "Zero";
        }
        if (value.compareTo(BigDecimal.ZERO) < 0) {
            value = value.negate();
            prefix = "Negative";
        }
        if (value.compareTo(BigDecimal.ZERO) != 0) {
            current = convertNumberToWordsIndianSystem(value);
        }
        int index = decimal.indexOf('.');
        if (index > 0) {
            // taking two places after decimal
            String num = StringUtils.substring((value.setScale(2)).toPlainString(), index + 1);

            fraction = new BigDecimal(num);
        }
        if (fraction.compareTo(BigDecimal.ZERO) != 0) {
            paiseInWords = " and" + convertNumberToWordsIndianSystem(fraction) + " paise";
        }

     
        return StringUtil.capitalizeFirstLetter((prefix + current + paiseInWords).trim());
    }
    
   

    private static String convertNumberToWordsIndianSystem(BigDecimal number) {

        String current = "";
        current = current
                + numberToword((number.divide(new BigDecimal("100000000000000000"))).remainder(new BigDecimal(100)), " shankh");
        current = current
                + numberToword((number.divide(new BigDecimal("1000000000000000"))).remainder(new BigDecimal(100)), " padm");
        current = current
                + numberToword((number.divide(new BigDecimal("10000000000000"))).remainder(new BigDecimal(100)), " neel");
        current = current
                + numberToword((number.divide(new BigDecimal("100000000000"))).remainder(new BigDecimal(100)), " kharab");
        current = current
                + numberToword((number.divide(new BigDecimal(1000000000))).remainder(new BigDecimal(100)), " arab");

        current = current
                + numberToword((number.divide(new BigDecimal(10000000))).remainder(new BigDecimal(100)), " crore");
        current = current + numberToword(((number.divide(new BigDecimal(100000))).remainder(new BigDecimal(100))), " lakh");
        current = current
                + numberToword(((number.divide(new BigDecimal(1000))).remainder(new BigDecimal(100))), " thousand");
        current = current + numberToword(((number.divide(new BigDecimal(100))).remainder(new BigDecimal(10))), " hundred");
        current = current + numberToword((number.remainder(new BigDecimal(100))), " ");

        return current;

    }

    
   
    
    
    private static String numberToword(Object n, String ch) {
        String current = "";
        BigDecimal bigDecimal = (BigDecimal) n;
        Integer num = bigDecimal.intValue();
        if (num > 19) {
            current = tensNames[num / 10] + numNames[num % 10];
        } else {
            current = (numNames[num]);
        }
        if (num > 0) {
            current = current + ch;
        }
        return current;
    }
    public static String convertNumToWordsInternationalSystem(BigDecimal num) {
        String output = "";
        BigDecimal dec = num.subtract(num.setScale(0, RoundingMode.FLOOR)).setScale(2, RoundingMode.HALF_DOWN);
        Locale locale = Locale.ENGLISH;
        NumberFormat formatter = new RuleBasedNumberFormat(locale, RuleBasedNumberFormat.SPELLOUT);
        String integerPart = formatter.format(num.setScale(0, RoundingMode.FLOOR));
        integerPart = integerPart.replace("-", " ");
        dec = new BigDecimal(dec.toString().replace(".", ""));
        if (dec.compareTo(BigDecimal.ZERO) == 1) {
            String fractionalPart = formatter.format(dec);
            fractionalPart = fractionalPart.replace("-", " ");
            output = integerPart + " and " + fractionalPart + " ";
        } else {
            output = integerPart;
        }
        return StringUtil.capitalizeFirstLetter(output);
    }
    
   
    
    // Indian conversion of number to words ends
    
	public static String getDecimalSeparatorFromLocale(Locale userLocale) {
		String decimalSeparator;
		NumberFormat formatter = NumberFormat.getCurrencyInstance(userLocale);
		DecimalFormatSymbols decimalFormatSymbols = ((DecimalFormat) formatter).getDecimalFormatSymbols();
		decimalSeparator = String.valueOf(decimalFormatSymbols.getDecimalSeparator());
		decimalSeparator = decimalSeparator.replace(ARABIC_DECIMAL_SEPARATOR, INDO_ARABIC_DECIMAL_SEPARATOR);
		return decimalSeparator;
	}

	public static String getGroupingSeparatorFromLocale(Locale userLocale) {
		String groupingSeparator;
		NumberFormat formatter = NumberFormat.getCurrencyInstance(userLocale);
		DecimalFormatSymbols decimalFormatSymbols = ((DecimalFormat) formatter).getDecimalFormatSymbols();
		groupingSeparator = String.valueOf(decimalFormatSymbols.getGroupingSeparator());
		groupingSeparator = groupingSeparator.replace(ARABIC_GROUPING_SEPARATOR, INDO_ARABIC_GROUPING_SEPARATOR);
		return groupingSeparator;
	}	

    private static String replaceNonIndoArabicDigits(String currencyOut) {
    	String amount = currencyOut;
    	//replace native arabic digits to indo arabic digits
    	amount = amount.replace(ARABIC_ZERO, INDO_ARABIC_ZERO).replace(ARABIC_ONE, INDO_ARABIC_ONE).replace(ARABIC_TWO, INDO_ARABIC_TWO)
        						 .replace(ARABIC_THREE, INDO_ARABIC_THREE).replace(ARABIC_FOUR, INDO_ARABIC_FOUR).replace(ARABIC_FIVE, INDO_ARABIC_FIVE)
        						 .replace(ARABIC_SIX, INDO_ARABIC_SIX).replace(ARABIC_SEVEN, INDO_ARABIC_SEVEN).replace(ARABIC_EIGHT, INDO_ARABIC_EIGHT)
        						 .replace(ARABIC_NINE, INDO_ARABIC_NINE).replace(ARABIC_GROUPING_SEPARATOR, INDO_ARABIC_GROUPING_SEPARATOR)
        						 .replace(ARABIC_DECIMAL_SEPARATOR, INDO_ARABIC_DECIMAL_SEPARATOR);
    	
        //replace native thai digits to indo arabic digits
    	amount = amount.replace(THAI_ZERO, INDO_ARABIC_ZERO).replace(THAI_ONE, INDO_ARABIC_ONE).replace(THAI_TWO, INDO_ARABIC_TWO)
        						 .replace(THAI_THREE, INDO_ARABIC_THREE).replace(THAI_FOUR, INDO_ARABIC_FOUR).replace(THAI_FIVE, INDO_ARABIC_FIVE)
        						 .replace(THAI_SIX, INDO_ARABIC_SIX).replace(THAI_SEVEN, INDO_ARABIC_SEVEN).replace(THAI_EIGHT, INDO_ARABIC_EIGHT)
        						 .replace(THAI_NINE, INDO_ARABIC_NINE);
        return amount;
    }
    
    private static void initializeLocaleMap() {
    	localeMap = new HashMap<String, String> ();
    	localeMap.put("cs_CZ", "cs_CZ");
   	    localeMap.put("et_EE", "et_EE");
    	localeMap.put("fi_FI", "fi_FI");
    	localeMap.put("fr_CA", "fr_CA");
    	localeMap.put("fr_FR", "fr_FR");
    	localeMap.put("fr_LU", "fr_LU");
    	localeMap.put("hu_HU", "hu_HU");
    	localeMap.put("lt_LT", "lt_LT");
    	localeMap.put("lv_LV", "lv_LV");
    	localeMap.put("pl_PL", "pl_PL");
    	localeMap.put("ru_RU", "ru_RU");    	
    	localeMap.put("sk_SK", "sk_SK");
    	localeMap.put("sr_BA_#Latn", "sr_BA_#Latn");
    	localeMap.put("sr_ME_#Latn", "sr_ME_#Latn");
    	localeMap.put("sr_RS_#Latn", "sr_RS_#Latn");
    	localeMap.put("sv_SE", "sv_SE");
    	localeMap.put("uk_UA", "uk_UA");
    }
    
    public static void printStats() {
    	Locale currentLocale = new Locale("en", "IN");
        String currCode = com.nucleus.currency.Currency.CURR_CODE_INDIA;
    	System.out.println(MoneyUtils.formatMoneyByLocale("999999999999999999", currentLocale,currCode));
    	System.out.println(MoneyUtils.formatMoneyByLocale("999999999999999999.99", currentLocale,currCode));
    	System.out.println(MoneyUtils.formatMoneyByLocale("999999999999999", currentLocale,currCode));
    	System.out.println(MoneyUtils.formatMoneyByLocale("999999999999999.99", currentLocale,currCode));
    	System.out.println(MoneyUtils.formatMoneyByLocale("9999999999999999", currentLocale,currCode));
    	System.out.println(MoneyUtils.formatMoneyByLocale("9999999999999999.99", currentLocale,currCode));
	}

}
