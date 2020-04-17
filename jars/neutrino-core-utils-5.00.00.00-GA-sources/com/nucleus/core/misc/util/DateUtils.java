/**
 * This file and a proportion of its content is copyright of Nucleus Software Exports Limited - © 2012. All rights reserved.
 * Any redistribution or reproduction of part or all of the contents in any form is prohibited other than the following:
 * - you cannot print or download to a local hard disk extract contents either part or full for personal/ commercial/
 * academic or any other use
 * - you may not copy the content to individual/ third parties for any type of use, either as compiled or source format
 * without the knowledge and consent of Nucleus SOftware
 * - You may not, except with our express written permission, distribute or commercially exploit the content. Nor may you
 * transmit it or store it in any other web site or other form of electronic retrieval system.
 */
package com.nucleus.core.misc.util;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.lang3.time.FastDateFormat;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import com.nucleus.core.exceptions.InvalidDataException;
import com.nucleus.logging.BaseLoggers;

/**
 * Date Utility methods which allows fast operations on date.
 * 
 * @author Nucleus Software Exports Limited
 */
public class DateUtils {

    public static String                             ALTERNATE_TIME_FORMAT = "hh:mm a";
    public static String                             DEFAULT_TIME_FORMAT   = "hh:mm:ss a";

     public static Date businessDate = null;

    private static final Map<String, FastDateFormat> FORMATTER_CACHE       = new LinkedHashMap<String, FastDateFormat>();

    public static String formatDate(String format, Date date) {
        if (date == null) {
            return null;
        }
        return getFormatter(format).format(date);
    }

    public static String formatDate(String format, Calendar calendar) {
        if (calendar == null) {
            return null;
        }
        return getFormatter(format).format(calendar.getTime());
    }

    public static DateTime getCurrentUTCTime() {
        return getCurrentForTimeZone(DateTimeZone.UTC);
    }

    public static DateTime getCurrentForTimeZone(DateTimeZone timeZone) {
        return DateTime.now(timeZone);
    }

    private static FastDateFormat getFormatter(String pattern) {
        if (!FORMATTER_CACHE.containsKey(pattern)) {
            FORMATTER_CACHE.put(pattern, FastDateFormat.getInstance(pattern));
        }
        return FORMATTER_CACHE.get(pattern);
    }

    public static String getFormattedDate(DateTime dateTime, String dateFormat) {
        DateTimeFormatter format = DateTimeFormat.forPattern(dateFormat);
        return format.print(dateTime);

    }

    public static String getFormattedDate(String dateTime, String dateFormat) {
        DateTimeFormatter format = DateTimeFormat.forPattern(dateFormat);
        DateTime date = DateTime.parse(dateTime);
        return format.print(date);

    }

    public static DateTime convertToUtcTime(DateTime dateTime) {
        return (dateTime.withZone(DateTimeZone.UTC));
    }

    public static DateTime parse(String dateTime, String dateFormat) {
		return DateTime.parse(dateTime.trim(),
				DateTimeFormat.forPattern(dateFormat)).toDateTime();
	}

      public static Date getBusinessDate() {
          if (businessDate != null) {
              return businessDate;
          } else {

              return getCurrentForTimeZone(DateTimeZone.UTC).toDate();
          }
      }

    /* This method returns difference between two dates in year, months and days. If the start date entered is 1/4/2013 and end date is 31/3/2013  The it will return
     * 1 year i.e it will include the beginning date also. Handling has been done as per days in each month. monthDay array has been taken which contains days in each 
     *  month in order. For ex. January contains 31 days so 1st element of the array is 31 and so on. -1 is for February. 
     *  */
    public static Map<String, Integer> getDifferenceBetweenTwoDates(DateTime startDate, DateTime endDate) {

        int yearsBetween = 0;
        int monthsBetween = 0;
        int daysBetween = 0;
        Map<String, Integer> dateDifferenceMap = new HashMap<String, Integer>();
        int[] monthDay = new int[] { 31, -1, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31 };
        int increment = 0;

        Map<String, Integer> resultMap = new HashMap<String, Integer>();
        if (startDate != null) {
            try {
                if (startDate.isAfter(endDate)) {
                    throw new InvalidDataException("Invalid Data : Start Date greater than End Date");
                }
            } catch (InvalidDataException e) {
                BaseLoggers.exceptionLogger.error("Invalid Data : Start Date greater than End Date", e);
                resultMap.put("result", -1);
                return resultMap;
            }
            GregorianCalendar cal = new GregorianCalendar();

            if (startDate.getDayOfMonth() > endDate.getDayOfMonth()) {
                increment = monthDay[startDate.getMonthOfYear() - 1];
            }

            if (increment == -1) {
                if (cal.isLeapYear(startDate.getYear())) {
                    increment = 29;
                } else {
                    increment = 28;
                }
            }

            if (increment != 0) {
                daysBetween = (endDate.getDayOfMonth() + increment) - startDate.getDayOfMonth();
                increment = 1;
            } else {
                daysBetween = endDate.getDayOfMonth() - startDate.getDayOfMonth();
            }

            if ((startDate.getMonthOfYear() + increment) > endDate.getMonthOfYear()) {
                monthsBetween = (endDate.getMonthOfYear() + 12) - (startDate.getMonthOfYear() + increment);
                increment = 1;
            } else {
                monthsBetween = (endDate.getMonthOfYear()) - (startDate.getMonthOfYear() + increment);
                increment = 0;
            }

            yearsBetween = endDate.getYear() - (startDate.getYear() + increment);

            daysBetween = daysBetween + 1;

            if (monthDay[endDate.getMonthOfYear() - 1] == -1) {
                if (cal.isLeapYear(endDate.getYear())) {
                    monthDay[endDate.getMonthOfYear() - 1] = 29;
                } else {
                    monthDay[endDate.getMonthOfYear() - 1] = 28;
                }
            }
            if (daysBetween >= monthDay[endDate.getMonthOfYear() - 1]) {
                monthsBetween = monthsBetween + (daysBetween / monthDay[endDate.getMonthOfYear() - 1]);
                daysBetween = daysBetween
                        - ((daysBetween / monthDay[endDate.getMonthOfYear() - 1]) * monthDay[endDate.getMonthOfYear() - 1]);
                if (monthsBetween >= 12) {
                    yearsBetween = yearsBetween + monthsBetween / 12;
                    monthsBetween = monthsBetween - (monthsBetween / 12) * 12;

                }
            }

        }

        dateDifferenceMap.put("daysBetween", daysBetween);
        dateDifferenceMap.put("monthsBetween", monthsBetween);
        dateDifferenceMap.put("yearsBetween", yearsBetween);

        return dateDifferenceMap;

    }

}
