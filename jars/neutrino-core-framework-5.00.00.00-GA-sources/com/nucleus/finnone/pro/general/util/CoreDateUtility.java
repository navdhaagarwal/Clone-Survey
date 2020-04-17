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


package com.nucleus.finnone.pro.general.util;

import static com.nucleus.finnone.pro.general.util.ValidatorUtils.isNull;
import static com.nucleus.finnone.pro.general.util.ValidatorUtils.noNullElements;
import static com.nucleus.finnone.pro.general.util.ValidatorUtils.notNull;
import static java.util.Arrays.asList;
import static java.util.Collections.max;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.apache.commons.lang3.time.DateUtils;
import org.joda.time.DateTime;

import com.nucleus.finnone.pro.base.constants.CoreConstant;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

public  class CoreDateUtility {
	

	/**
     * Convert into date format.
     * @param   dateValue   the <code>Date</code> .
     * @param   format   the <code>string</code> to be converted.
     * @return  the value converted into passed format value 
     * @exception ParseException if date is not formatted .
 	 * */
	public static Date formatDate(Date dateValue, String format) 
	{
		Date formattedDate=null;
		DateFormat dateFormat= new SimpleDateFormat(format);
		
		 String formattedDateString = dateFormat.format(dateValue);

			 try {
				formattedDate = dateFormat.parse(formattedDateString);
			} catch (ParseException e) 
			{
				
				
				
				
				//throw ExceptionBuilder.getInstance(ServiceInputException.class, "GM_DV001", "Date format is incorrect").build();
				
			}
	

		return formattedDate;
	}
	
	/**
     * Convert into date format.
     * @param   dateValue   the <code>Date</code> .
     * @param   format   the <code>string</code> to be converted.
     * @return  the value converted into passed format value 
     * @exception ParseException if date is not formatted .
 	 * */
	public static DateTime formatDate(DateTime dateTime , String format) 
	{
		DateTime formattedDateTime=null;
		DateFormat dateFormat= new SimpleDateFormat(format);
		
		 String formattedDateString = dateFormat.format(new Date(dateTime.getMillis()));

			 try {
				Date formattedDate = dateFormat.parse(formattedDateString);
				formattedDateTime = new DateTime(formattedDate);
			} catch (ParseException e) 
			{
				
				
				
				
				//throw ExceptionBuilder.getInstance(ServiceInputException.class, "GM_DV001", "Date format is incorrect").build();
				
			}
	

		return formattedDateTime;
	}
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
	
	/**
     * Compares two Dates for ordering.
     * @param   fromDate   the <code>Date</code> .
     * @param   toDate   the <code>Date</code> to be compared.
     * @return  the value <code>0</code> if the toDate  is equal to
     *          fromDate ; a value less than <code>0</code> if fromDate 
     *          is before the toDate ; and a value greater than
     *      <code>0</code> if fromDate is after the toDate.
     * @exception NullPointerException if <code>fromDate</code> or <code>toDate</code> is null.
     */
	
	public static int compareDate(Date fromDate , Date toDate)
	{
		Date formattedFromDate=null;
		Date formattedToDate=null;
		if(isNull(fromDate) && isNull(toDate)){
			return 0;
		}
		if( isNull(fromDate) && notNull(toDate)){
			return -1;
		}
		if( notNull(fromDate) && isNull(toDate)){
			return 1;
		}
		formattedFromDate=formatDate(fromDate, CoreConstant.DATE_FORMAT);
		formattedToDate=formatDate(toDate,CoreConstant.DATE_FORMAT);
		return formattedFromDate.compareTo(formattedToDate);
	}
	
	/**
     * This method takes the date in String form and return the date type of date for the given format.
     * Input String date and String format.
     * Return Date type of date
     * @param inDate
     * @param format
     */
	public static Date getDateFromString(String inDate, String format) throws Exception {
		Date date = null;
		if (inDate == null || inDate.isEmpty()) {
			return date;
		}

		SimpleDateFormat formatter = new SimpleDateFormat(format);

		date = formatter.parse(inDate);

		String outDate = formatter.format(date);

		if (inDate.compareToIgnoreCase(outDate) != 0) {
			throw new ParseException("", 0);
		}

		return date;
	}
    
    
    /**
     * This method takes the date in String form and return the datetime type of datetime for the given format.
     * Input String date and String format.
     * Return Date type of date
     * @param inDate
     * @param format
     */
    public static DateTime getDateTimeFromString(String inDateTime, String format) throws Exception
    {	
    	//"dd/MM/yyyy HH:mm:ss"
    	
    	DateTime dateTime = null;
        if (inDateTime != null && !"".equalsIgnoreCase(inDateTime)) {
        	dateTime = com.nucleus.core.misc.util.DateUtils.parse(inDateTime, format);
        }   
        return dateTime;
    }
    
    
    public static Calendar getCalendarFromString(String inDate, String format) throws Exception
    {
    	Calendar calendar = Calendar.getInstance();
 calendar.setTime(getDateFromString( inDate,  format))       ;
	return calendar;
    }
    
	/**
	 * @see convertToDesiredDate(Date date,int days, int month, int year) The
	 *      method will convert the supplied date to the desired date of given
	 *      parameters.
	 * @throws NullPointerException if givenDate is null
	 */
	public static Date incrementCalenderDays(Date givenDate, int days, int month, int year) {
		Calendar calender = Calendar.getInstance();	
		calender.setTime(givenDate);		
		calender.add(Calendar.DATE, days);
		calender.add(Calendar.MONTH, month);
		calender.add(Calendar.YEAR, year);
		return calender.getTime();
	}

	public static Date maxDate(Date... dates){
		return noNullElements(dates)?max(asList(dates)):null;
	}
	
	public static Date getMonthEndDate(Date toDate, int incr_Decr_months){
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(toDate);
		calendar.add(Calendar.MONTH, incr_Decr_months);
		calendar.set(Calendar.DAY_OF_MONTH,calendar.getActualMaximum(calendar.DAY_OF_MONTH));
		return calendar.getTime();
	}
	
	public static Date getMonthStartDate(Date toDate, int incr_Decr_months){
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(toDate);
		calendar.add(Calendar.MONTH, incr_Decr_months);
		calendar.set(Calendar.DAY_OF_MONTH,1);
		return calendar.getTime();
	}
	
	public static boolean areDatesInSameMonth(Date first,Date second){
		Calendar firstCalender = Calendar.getInstance();
		Calendar secondCalendar = Calendar.getInstance();
		firstCalender.setTime(first);
		secondCalendar.setTime(second);
		return firstCalender.get(Calendar.YEAR)==secondCalendar.get(Calendar.YEAR) && firstCalender.get(Calendar.MONTH)==secondCalendar.get(Calendar.MONTH);
	}
	
	public static Date changeDayOfDate(Date inputDate,Integer day){
		Calendar calender = Calendar.getInstance();
		calender.setTime(inputDate);
		calender.set(Calendar.DATE, day);
		return calender.getTime();
	}
	
	/**
	 * @param date
	 * @return Day of the Month or null if date is null
	 */
	public static Integer getDay(Date date){
		if(ValidatorUtils.notNull(date)){
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(date);
			return calendar.get(Calendar.DAY_OF_MONTH);
		}else{
			return null;
		}
	}
	
	/**
	 * @param date
	 * @return Month of the Year (1 for JAN.. 12 for DEC) or null if date is null
	 */
	public static Integer getMonth(Date date){
		if(ValidatorUtils.notNull(date)){
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(date);
			return calendar.get(Calendar.MONTH)+1;
		}else{
			return null;
		}
	}
	
	/**
	 * @param date
	 * @return Year part of date or null if date is null
	 */
	public static Integer getYear(Date date){
		if(ValidatorUtils.notNull(date)){
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(date);
			return calendar.get(Calendar.YEAR);
		}else{
			return null;
		}
	}
	
	public static boolean between(Date fromDate, Date toDate, Date currentDate){
		return currentDate.after(fromDate) && currentDate.before(toDate);
	}
	
	public static  boolean isFromDateAfterToDate(Date fromDate,Date toDate){
		return  (fromDate != null && toDate != null && fromDate.after(toDate));
	}

	public static  boolean isFromDateAfterOrEqualsToDate(Date fromDate,Date toDate){
		return (fromDate != null && toDate != null && fromDate.compareTo(toDate) >= 0);
	}

	public static  boolean isFromDateBeforeToDate(Date fromDate,Date toDate){
		return (fromDate != null && toDate != null && fromDate.before(toDate));
	}

	public static  boolean isFromDateBeforeOrEqualsToDate(Date fromDate,Date toDate){
		return (fromDate != null && toDate != null && fromDate.compareTo(toDate) <= 0);
	}

	public static  boolean isFromDateEqualsToDate(Date fromDate,Date toDate){
		return (fromDate != null && toDate != null && fromDate.compareTo(toDate)== 0);
	}

	public static  boolean isDateBetweenFromDateAndToDate(Date fromDate,Date toDate,Date checkDate){
		return (isFromDateAfterOrEqualsToDate(checkDate, fromDate)
				&& isFromDateBeforeOrEqualsToDate(checkDate, toDate));
	}
	
	
	
    /**
     * Adds a number of days to a date returning a new object.
     * The original {@code Date} is unchanged
     * 
     * <p>Also it Truncates a date, leaving the field specified as the most
     * significant field.(Time part)</p>
     *
     * @param date  the date, not null
     * @param amount  the amount to add, may be negative
     *
     * <p>For example, if you had the date-time of 28 Mar 2002
     * 13:45:01.231, if you passed with HOUR, it would return 28 Mar
     * 2002 13:00:00.000.  If this was passed with MONTH, it would
     * return 1 Mar 2002 0:00:00.000.</p>
     * 
     * @param date  the date to work with, not null
     * @param field  the field from {@code Calendar} or <code>SEMI_MONTH</code>
     * @return the different truncated date, not null
     * @throws IllegalArgumentException if the date is <code>null</code>
     * @throws ArithmeticException if the year is over 280 million
     */
	public static Date addDaysAndTruncateTimeSegment(final Date toDate, int amount){
		if(isNull(toDate) || isNull(amount))
			return toDate;
		return DateUtils.truncate(DateUtils.addDays(toDate, amount), Calendar.DATE);
	}
	
	
    /**
     * <p>Truncates a date, leaving the field specified as the most
     * significant field.</p>
     *
     * <p>For example, if you had the date-time of 28 Mar 2002
     * 13:45:01.231, if you passed with HOUR, it would return 28 Mar
     * 2002 13:00:00.000.  If this was passed with MONTH, it would
     * return 1 Mar 2002 0:00:00.000.</p>
     * 
     * @param date  the date to work with, not null
     * @return the different truncated date, null
     * @throws - ArithmeticException if the year is over 280 million
     */
	public static Date truncateTimeSegmentFromDate(final Date toDate){
		if(isNull(toDate))
			return toDate;
		return DateUtils.truncate(toDate, Calendar.DATE);
		
	}

	
	/**
     * Adds a number of days to a date returning a new object.
     * The original {@code Date} is unchanged
     * 
     * <p>Also it Truncates a date, leaving the field specified as the most
     * significant field.(Time part)</p>
     *
     * @param date  the date, not null
     * @param amount  the amount to add, may be negative
     */
	public static Date addDays(final Date toDate, int amount){
		return DateUtils.addDays(toDate, amount);
	}


	/**
	 * <p>Checks if two date objects are on the same day ignoring time.</p>
	 *
	 * <p>28 Mar 2002 13:45 and 28 Mar 2002 06:01 would return true.
	 * 28 Mar 2002 13:45 and 12 Mar 2002 13:45 would return false.
	 * </p>
	 * 
	 * @param date1  the first date, not altered, not null
	 * @param date2  the second date, not altered, not null
	 * @return true if they represent the same day
	 * @throws - NA
	 * @since LMS_Rel_GA_1.2.1
	 */
	public static  boolean isSameDay(Date date1, Date date2) {
	    if (date1 == null || date2 == null) {
	        return false;
	    }
	    Calendar cal1 = Calendar.getInstance();
	    cal1.setTime(date1);
	    Calendar cal2 = Calendar.getInstance();
	    cal2.setTime(date2);
	    return CoreDateUtility.isSameDay(cal1, cal2);
	}

	/**
	 * <p>Checks if two calendar objects are on the same day ignoring time.</p>
	 *
	 * <p>28 Mar 2002 13:45 and 28 Mar 2002 06:01 would return true.
	 * 28 Mar 2002 13:45 and 12 Mar 2002 13:45 would return false.
	 * </p>
	 * 
	 * @param cal1  the first calendar, not altered, not null
	 * @param cal2  the second calendar, not altered, not null
	 * @return true if they represent the same day
	 * @throws - NA
	 * @since LMS_Rel_GA_1.2.1
	 */
	public static  boolean isSameDay(Calendar cal1, Calendar cal2) {
	    if (cal1 == null || cal2 == null) {
	        return false;
	    }
	    return (cal1.get(Calendar.ERA) == cal2.get(Calendar.ERA) &&
	            cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
	            cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR));
	}

	public static String getTime(String value,String formatMask){
		DateTimeFormatter dtf = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
		DateTime actual = dtf.parseDateTime(value);
		dtf = DateTimeFormat.forPattern(formatMask);
		String dateTime = dtf.print(actual);
		return dateTime;
	}
	
}