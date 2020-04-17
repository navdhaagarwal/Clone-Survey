package com.nucleus.finnone.pro.general.util;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import com.nucleus.finnone.pro.general.constants.CurrencyEnum;
import com.nucleus.finnone.pro.general.domainobject.NumberConverterVO;


public class CurrencyUtility {
	private static final String SPACE=" ";
	 static String string;
	 static  String st1[] = { "", "One", "Two", "Three", "Four", "Five", "Six", "Seven","Eight", "Nine", };
		static String st2[] = { "Hundred", "Thousand", "Lakh", "Crore","Arab","Kharab","Neel" };
		static String st3[] = { "Ten", "Eleven", "Twelve", "Thirteen", "Fourteen", "Fifteen", "Sixteen", "Seventeen", "Eighteen", "Ninteen", };
		static String st4[] = { "Twenty", "Thirty", "Fourty", "Fifty", "Sixty", "Seventy","Eighty", "Ninety" };
		private static final String[] majorNames = { ""," Thousand", " Million", " Billion", " Trillion", " Quadrillion", " Quintillion" }; 
		private static final String[] tensNames = {"", " Ten", " Twenty"," Thirty", " Fourty", " Fifty", " Sixty", " Seventy", " Eighty", " Ninety"}; 
		private static final String[] numNames = { ""," One"," Two"," Three"," Four"," Five"," Six"," Seven"," Eight"," Nine"," Ten"," Eleven", " Twelve", 
												   " Thirteen", " Fourteen", " Fifteen", " Sixteen", " Seventeen", " Eighteen", " Nineteen"}; 
	static Map<String,NumberConverterVO> currencyMap = new HashMap<String, NumberConverterVO>();
	static
	{
		NumberConverterVO numberConverterVO= new NumberConverterVO();
		numberConverterVO.setIndianPluralCurrency("Rupees");
		numberConverterVO.setIndianPluralCurrencyUnit("Paise");
		numberConverterVO.setIndianSingularCurrency("Rupee");
		numberConverterVO.setIndianSingularCurrencyUnit("Paisa");
		currencyMap.put(CurrencyEnum.INDIAN_RUPEE.getEnumValue(),numberConverterVO);
		
		NumberConverterVO numberConverterVOEnglish= new NumberConverterVO();
		numberConverterVOEnglish.setEnglishPluralCurrency("Dollars");
		numberConverterVOEnglish.setEnglishPluralCurrencyUnit("Cents");
		numberConverterVOEnglish.setEnglishSingularCurrency("Dollar");
		numberConverterVOEnglish.setEnglishSingularCurrencyUnit("Cent");
		currencyMap.put(CurrencyEnum.ENGLISH_DOLLAR.getEnumValue(), numberConverterVOEnglish);
	}
	
	public static  String convert(int number) {
        int n = 1;
        int word;
        String previousString ="";
        while (number != 0) {
                switch (n) {
                case 1:
                        word = number % 100;
                        previousString=pass(word,previousString);
                        /*if (number > 100 && number % 100 != 0) {
                                show("and ");
                        }*/
                        number /= 100;
                        break;

                case 2:
                        word = number % 10;
                         if (word != 0) {
                        	previousString=show(SPACE,previousString);
                        	previousString=show(st2[0],previousString);
                        	previousString=show(SPACE,previousString);
                        	previousString=pass(word,previousString);
                        }
                        number /= 10;
                        break;

                case 3:
                        word = number % 100;
                         if (word != 0) {
                        	previousString=show(SPACE,previousString);
                        	previousString=show(st2[1],previousString);
                        	previousString=show(SPACE,previousString);
                        	previousString=pass(word,previousString);
                        }
                        number /= 100;
                        break;

                case 4:
                        word = number % 100;
                       	if (word != 0) {
                        	previousString=show(SPACE,previousString);
                        	previousString=show(st2[2],previousString);
                        	previousString=show(SPACE,previousString);
                        	previousString=pass(word,previousString);
                        }
                        number /= 100;
                        break;

                case 5:
                        word = number % 100;
                 		 if (word != 0) {
                        	previousString=show(SPACE,previousString);
                        	previousString=show(st2[3],previousString);
                        	previousString=show(SPACE,previousString);
                        	previousString=pass(word,previousString);
                        }
                        number /= 100;
                        break;

                }
                n++;
        }
	 return previousString;
	}
	public static String pass(int number, String previousString) {
        int word, q;
        if (number < 10) {
        	previousString= show(st1[number],previousString);
        }
        if (number > 9 && number < 20) {
        	previousString= show(st3[number - 10],previousString);
        }
        if (number > 19) {
                word = number % 10;
                if (word == 0) {
                        q = number / 10;
                        previousString=show(st4[q - 2],previousString);
                } else {
                        q = number / 10;
                        previousString=show(st1[word],previousString);
                        previousString=show(SPACE,previousString);
                        previousString=show(st4[q - 2],previousString);
                }
       	}
        return previousString;
	}

		public static String show(String s, String previousString) {
		        String st;
		        st = previousString;
		        previousString = s;
		        previousString += st;
		        return previousString;
		}
		private static String convertLessThanOneThousand(int number) 
		{
			String soFar; 
			if (number % 100 < 20){ 
			soFar = numNames[number % 100]; 
			number /= 100; 
			} 
			else { 
			soFar = numNames[number % 10]; 
			number /= 10; 
			
			soFar = tensNames[number % 10] + soFar; 
			number /= 10; 
			} 
			if (number == 0) return soFar; 
			return numNames[number] + " hundred" + soFar; 
		} 
		public static String convertInEnglish(int number) 
		{
			 String string ="";
			/* special case */ 
			if (number == 0) 
			{ 
				return string;
			} 
			String prefix =""; 
			if (number < 0) { 
			number = -number; 
			prefix = "negative"; 
			} 
			String soFar =""; 
			int place = 0; 
			do { 
			int n = number % 1000; 
			if (n != 0){ 
			String s = convertLessThanOneThousand(n); 
			soFar = s + majorNames[place] + soFar; 
			} 
			place++; 
			number /= 1000; 
			} while (number > 0); 
			return (prefix + soFar).trim(); 
		}
		
		/**
		 * 
		 * 
		 * @param number
		 * @param roundTill
		 * @return
		 */
	    public static String convertNumberToWord(BigDecimal number, String currency){    
	        NumberConverterVO currencyObject= currencyMap.get(currency);
	        String numberInWords ="";
	        if(CurrencyEnum.ENGLISH_DOLLAR.getEnumValue().equals(currency))
	        {
	        	numberInWords=getWordEquivalentInEnglishDollarCurrency(number,currency,currencyObject);
	        }
	        else
	        {
	        	numberInWords=getWordEquivalentInIndianRupeeCurrency(number,currency,currencyObject); 
	        }
	        
	        return numberInWords;
	 }
	    
	  private static String getWordEquivalentInEnglishDollarCurrency(BigDecimal number, String currency,NumberConverterVO currencyObject){
		  String numberInWords ="";
		  if(String.valueOf(number).contains(".")){
              String numberString = String.valueOf(number);
              numberString = numberString.substring(numberString.indexOf(".")).substring(1);
              if(numberString.length()>2){
            	  numberString=numberString.substring(0, 2);
              }
              else if(numberString.length()==1){
            	  numberString=numberString.concat("0");
              }
              String inwordsWholeValue = convert(number.intValue());
              String inwordsDecimals = convert(Integer.parseInt(numberString));
              if(inwordsWholeValue.equals(st1[0])
            		 &&  inwordsDecimals.equals(st1[0])){
                  numberInWords= currencyObject.getEnglishSingularCurrency().concat(SPACE).concat(numberInWords.concat("Zero "));
              	  return numberInWords;
              }
              else if(inwordsWholeValue.equals(st1[0])
               		 &&  !(inwordsDecimals.equals(st1[0]))){
                   numberInWords= currencyObject.getEnglishSingularCurrency().concat(SPACE).concat(numberInWords.concat("Zero "));
                   numberInWords=getEquivalentNumberForDecimalInEnglishDollarCurrency(numberInWords,inwordsDecimals,currencyObject);
                   return numberInWords;
               }
              numberInWords = numberInWords.concat(inwordsWholeValue);
              if(inwordsWholeValue.equals(st1[1]))
                     numberInWords = currencyObject.getEnglishSingularCurrency().concat(SPACE).concat(numberInWords).concat(SPACE);
              else
                     numberInWords  = currencyObject.getEnglishPluralCurrency().concat(SPACE).concat(numberInWords).concat(SPACE);
              if(inwordsDecimals!="" && !inwordsDecimals.equalsIgnoreCase("ZERO"))
              {
            	  numberInWords=getEquivalentNumberForDecimalInEnglishDollarCurrency(numberInWords,inwordsDecimals,currencyObject);
              }
        }
        else
        {
              numberInWords = convertInEnglish(number.intValue());
              if(numberInWords.equals(st1[0]))
              {
                     /**Handling for Zero **/
                     numberInWords= currencyObject.getEnglishSingularCurrency().concat(SPACE).concat(numberInWords.concat("Zero "));

              }
              else if(numberInWords.equals(st1[1]))
                     numberInWords=currencyObject.getEnglishSingularCurrency().concat(SPACE).concat(numberInWords).concat(SPACE);
              else
                     numberInWords=currencyObject.getEnglishPluralCurrency().concat(SPACE).concat(numberInWords).concat(SPACE);
        }
		  return numberInWords;
	  }
	  
	  private static String getWordEquivalentInIndianRupeeCurrency(BigDecimal number, String currency,NumberConverterVO currencyObject){
		  String numberInWords ="";
		  if(String.valueOf(number).contains(".")){
              BigDecimal wholeValue = number.abs();
              String numberString = String.valueOf(number);
              numberString = numberString.substring(numberString.indexOf(".")).substring(1);
              if(numberString.length()>2){
            	  numberString=numberString.substring(0, 2);
              }
              else if(numberString.length()==1){
            	  numberString=numberString.concat("0");
              }
              String inwordsWholeValue = convert(wholeValue.intValue());
              String inwordsDecimals = convert(Integer.parseInt(numberString));
              
              if(inwordsWholeValue.equals(st1[0])
             		 &&  inwordsDecimals.equals(st1[0])){
                     numberInWords= currencyObject.getIndianSingularCurrency().concat(SPACE).concat(numberInWords.concat("Zero "));
                     return numberInWords;
              }
              else if(inwordsWholeValue.equals(st1[0])
              		 &&  !(inwordsDecimals.equals(st1[0]))){
                  numberInWords= currencyObject.getIndianSingularCurrency().concat(SPACE).concat(numberInWords.concat("Zero "));
                  numberInWords= getEquivalentNumberForDecimalInIndianCurrency(numberInWords,inwordsDecimals,currencyObject);
                  return numberInWords;
              }
              numberInWords = numberInWords.concat(inwordsWholeValue);
              if(inwordsWholeValue.equals(st1[1]))
                     numberInWords = currencyObject.getIndianSingularCurrency().concat(SPACE).concat(numberInWords).concat(SPACE);
              else
                     numberInWords  = currencyObject.getIndianPluralCurrency().concat(SPACE).concat(numberInWords).concat(SPACE);
              
              if(inwordsDecimals!="" && !inwordsDecimals.equalsIgnoreCase("ZERO"))
              {
            	  numberInWords= getEquivalentNumberForDecimalInIndianCurrency(numberInWords,inwordsDecimals,currencyObject);
              }
        }
        else
        {
              numberInWords = convert(number.intValue());
              if(numberInWords.equals(st1[0]))
              {
                     /**Handling for Zero **/
                     numberInWords= currencyObject.getIndianSingularCurrency().concat(SPACE).concat(numberInWords.concat("Zero "));

              }
              else if(numberInWords.equals(st1[1]))
                     numberInWords= currencyObject.getIndianSingularCurrency().concat(SPACE).concat(numberInWords).concat(SPACE);
              else
                     numberInWords= currencyObject.getIndianPluralCurrency().concat(SPACE).concat(numberInWords).concat(SPACE);
        }
		  return numberInWords;
	  }
	  
	  /**
	   * return word equivalent for decimal in indian currency
	   * @param numberInWords
	   * @param inwordsDecimals
	   * @param currencyObject
	   * @return
	   */
	  private static String getEquivalentNumberForDecimalInIndianCurrency(String numberInWords,String inwordsDecimals,NumberConverterVO currencyObject){
		  numberInWords = numberInWords.concat("and ");
          if(inwordsDecimals.equals(st1[1]))
                 numberInWords = numberInWords.concat(currencyObject.getIndianSingularCurrencyUnit()).concat(SPACE).concat(inwordsDecimals).concat(SPACE);
          else
                 numberInWords = numberInWords.concat(currencyObject.getIndianPluralCurrencyUnit()).concat(SPACE).concat(inwordsDecimals).concat(SPACE);
          return numberInWords;
	  }
	  
	  /**
	   * return word equivalent for decimal in english dollar currency
	   * @param numberInWords
	   * @param inwordsDecimals
	   * @param currencyObject
	   * @return
	   */
	  private static String getEquivalentNumberForDecimalInEnglishDollarCurrency(String numberInWords,String inwordsDecimals,NumberConverterVO currencyObject){
		  numberInWords = numberInWords.concat("and ");
          if(inwordsDecimals.equals(st1[1]))
                 numberInWords = numberInWords.concat(currencyObject.getEnglishSingularCurrencyUnit()).concat(SPACE).concat(inwordsDecimals).concat(SPACE);
          else
                 numberInWords = numberInWords.concat(currencyObject.getEnglishPluralCurrencyUnit()).concat(SPACE).concat(inwordsDecimals).concat(SPACE);
          return numberInWords;
	  }
	  
}
