/**
 * This file and a proportion of its content is copyright of Nucleus Software Exports Limited - � 2012. All rights reserved.
 * Any redistribution or reproduction of part or all of the contents in any form is prohibited other than the following:
 * - you cannot print or download to a local hard disk extract contents either part or full for personal/ commercial/
 * academic or any other use
 * - you may not copy the content to individual/ third parties for any type of use, either as compiled or source format
 * without the knowledge and consent of Nucleus SOftware
 * - You may not, except with our express written permission, distribute or commercially exploit the content. Nor may you
 * transmit it or store it in any other web site or other form of electronic retrieval system.
 */
package com.nucleus.web.convertes;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import com.nucleus.logging.BaseLoggers;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.convert.converter.Converter;

public class NeutrinoCustomConverter {
    
    //To be configured as a configurable property : This cannot be predicted from Locale.
    public static final String defaultDateFormat =  "MM/dd/yyyy";

    public static final Converter<String, Calendar> StringToCalendarConverter = new Converter<String, Calendar>() {
          public Calendar convert(final String source) {
              if (StringUtils.isNotBlank(source)) {
                  DateFormat formatter;
                  Date date = null;
                  formatter = new SimpleDateFormat(defaultDateFormat);
                      try {
                          date = (Date) formatter.parse(source);
                      } catch (ParseException e) {
                          BaseLoggers.exceptionLogger.error(e.getMessage(), e);
                      }
                      Calendar cal = Calendar.getInstance();
                      cal.setTime(date);
                      return cal;
                  }
                  return null;
              };
          };

}
