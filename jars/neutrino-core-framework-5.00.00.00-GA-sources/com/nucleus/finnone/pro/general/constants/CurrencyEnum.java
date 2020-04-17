/**
 * 
 */
package com.nucleus.finnone.pro.general.constants;

/**
 * @author shivani.aggarwal
 *
 */
public enum CurrencyEnum {
	INDIAN_RUPEE("INR","lbl.yes","Rupees"),
	ENGLISH_DOLLAR("USD","lbl.yes","Dollars");
	
	
	
	private String enumValue;
	private String description;
	private String i18nCode;
	
	 private CurrencyEnum(String enumValue,String i18nCode,String description){
		 this.i18nCode=i18nCode;
			this.enumValue=enumValue;
			this.description=description;
		}
		
		
		 public String getEnumValue() {
				return enumValue;
			}



			public String getDescription() {
				return description;
			}



			public String getI18nCode() {
				return i18nCode;
			}



}
