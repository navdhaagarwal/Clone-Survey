/**
 * This file and a proportion of its content is copyright of Nucleus Software Exports Limited - © 2012. All rights reserved.
 * Any redistribution or reproduction of part or all of the contents in any form is prohibited other than the following:
 * - you cannot print or download to a local hard disk extract contents either part or full for personal/ commercial/
 * academic or any other use
 * - you may not copy the content to individual/ third parties for any type of use, either as compiled or source format
 * without the knowledge and consent of Nucleus Software
 * - You may not, except with our express written permission, distribute or commercially exploit the content. Nor may you
 * transmit it or store it in any other web site or other form of electronic retrieval system.
 */
package com.nucleus.web.formatter;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Locale;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.format.Formatter;

import com.nucleus.user.UserService;

/**
 * @author Nucleus Software Exports Limited
 * Formatter class to convert between String and Calendar type.
 */
public class CalendarFormatter implements Formatter<Calendar> {

    @Inject
    @Named("userService")
    private UserService userService;

    /*@see org.springframework.format.Printer#print(java.lang.Object, java.util.Locale) */
    @Override
    public String print(Calendar calendar, Locale locale) {
        String pattern = userService.getUserPreferredDateFormat();
        return (calendar != null ? DateFormatUtils.format(calendar, pattern) : "");

    }

    /* @see org.springframework.format.Parser#parse(java.lang.String, java.util.Locale) */
    @Override
    public Calendar parse(String text, Locale locale) throws ParseException {
        String pattern = userService.getUserPreferredDateFormat();
        String alternatePattern = pattern + " HH:mm";
        String timePattern = "hh:mm a";
        return DateUtils.toCalendar(DateUtils.parseDate(text, new String[] { alternatePattern, pattern, timePattern }));
    }

}
