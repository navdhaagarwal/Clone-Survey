/**
@author merajul.ansari
Creation Date: 30/01/2013
Copyright: Nucleus Software Exports Ltd.
Description: Constants for Additional Data functionality
Program Specs Referred: 
----------------------------------------------------------------------------------------------------------------
Revision:  Version	Last Revision Date	 	Name		Function / Module affected       Modifications Done
----------------------------------------------------------------------------------------------------------------	
	       1.0		29/01/2013				Merajul Hasan Ansari 	initial version      
----------------------------------------------------------------------------------------------------------------
 *
 */
package com.nucleus.finnone.pro.additionaldata.constants;

/**
 * Contains constants related to FRR
 * @author Dhananjay.Jha
 *
 */
public final class AdditionalDataConstants {
	
	public static final int NUMBER_OF_ADDL_FIELDS = 25;
	public static final String DATE_FORMAT = "yyyy-MM-dd";
	public static final String ADDL_DATA_GETVAL_EXCEPTION="fmsg.00000314";
	public static final String ADDL_DATA_PARSE_EXCEPTION="fmsg.00000315";
	public static final int ADDL_DATA_STRING_LENGTH_HUNDRED = 100;
	public static final int ADDL_DATA_STRING_LENGTH_ONE =1;
	public static final int ADDL_DATA_STRING_LENGTH_TWO =2;
	public static final int ADDL_DATA_STRING_LENGTH_SIX =6;
	public static final int ADDL_DATA_NUMERIC_LENGTH_TEN = 10;
	public static final int ADDL_DATA_STRING_LENGTH_FOUR_HUNDRED = 400;
	public static final String DEFAULT_DIGIT_GROUPING_CONSTANT=",";
	public static final String DEFAULT_DECIMAL_GROUPING_CONSTANT=".";
	public static final String DEFAULT_DATE_PATTERN="dd/MM/yyyy";
	public static final String CONFIG_DIGIT_GROUPING_CONSTANT="config.digit.grouping.constant";
	public static final String CONFIG_DECIMAL_GROUPING_CONSTANT="config.decimal.grouping.constant";
	public static final String CONFIG_DATE_FORMATS="config.date.formats";
    private AdditionalDataConstants(){
		
	}
}
