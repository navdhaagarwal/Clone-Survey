/**
Author: Incredible
Creation Date: 27/08/2012
Copyright: Nucleus Software Exports Ltd
Description: Date Utility class is used to provide the common date function to be used  in all modules   
Program Specs Referred: R84_R85_R86_Termination_Wireframe.doc
----------------------------------------------------------------------------------------------------------------
Revision:  Version	Last Revision Date	 	Name		Function / Module affected       Modifications Done
----------------------------------------------------------------------------------------------------------------	
	       1.0		27/08/2012				Gagan Mehta 	Code for common date function to be used  in all modules      
----------------------------------------------------------------------------------------------------------------
 *
 */


package com.nucleus.hijri.util;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;


public final class DateUtility {
	


	/**
     * Convert into Date String.
     * @param   dateValue   the <code>Date</code> .
     * @param   format   the <code>string</code> to be converted.
     * @return  the value converted into passed format value 
     * */
	public static String formatDateAsString(Date dateValue, String format) 
	{
		DateFormat dateFormat= new SimpleDateFormat(format);
		String formattedDateString =null;
		if(dateValue!=null)
		{
			formattedDateString = dateFormat.format(dateValue);			
		}

		return formattedDateString;
	}
	
	
}





