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
import java.util.Locale;

import org.springframework.format.Formatter;

/**
 * @author Nucleus Software Exports Limited
 * Formatter class to convert between String and Long type.
 */
public class LongFormatter implements Formatter<Long> {

    /* @see org.springframework.format.Printer#print(java.lang.Object, java.util.Locale) */
    @Override
    public String print(Long longValue, Locale locale) {
        return (longValue != null ? longValue.toString() : "");
    }

    /* @see org.springframework.format.Parser#parse(java.lang.String, java.util.Locale) */
    @Override
    public Long parse(String text, Locale locale) throws ParseException {
        Boolean isNegative = false;
        isNegative = text.startsWith("-");
        String newStr = text.replaceAll("[^\\d]+", "");
        if (isNegative) {
            newStr = "-" + newStr;
        }
        return Long.parseLong(newStr);
    }

}
