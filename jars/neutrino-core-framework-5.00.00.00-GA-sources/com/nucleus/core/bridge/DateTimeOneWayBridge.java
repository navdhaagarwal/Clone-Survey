package com.nucleus.core.bridge;

import java.util.Date;

import javax.xml.datatype.XMLGregorianCalendar;

import org.hibernate.search.bridge.StringBridge;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;

import com.nucleus.core.misc.util.DateUtils;

/**
 * @author Nucleus Software Exports Limited
 *
 */
public class DateTimeOneWayBridge implements StringBridge {

    private static final String DATE_FORMAT = "yyyyMMdd";

    @Override
    public String objectToString(Object object) {

        if (object == null) {
            return null;
        }
        if (DateTime.class.isAssignableFrom(object.getClass())) {
            DateTime dateTime = (DateTime) object;
            return DateTimeFormat.forPattern(DATE_FORMAT).print(dateTime.toDateTime(DateTimeZone.UTC));
        } else if (XMLGregorianCalendar.class.isAssignableFrom(object.getClass())) {

            XMLGregorianCalendar gregorianCalendar = (XMLGregorianCalendar) object;
            DateTime dateTime = new DateTime(gregorianCalendar.toGregorianCalendar());
            return DateTimeFormat.forPattern(DATE_FORMAT).print(dateTime.toDateTime(DateTimeZone.UTC));
        } else if (LocalDate.class.isAssignableFrom(object.getClass())) {
            DateTime dateTime = ((LocalDate) object).toDateTimeAtStartOfDay();
            return DateTimeFormat.forPattern(DATE_FORMAT).print(dateTime.toDateTime(DateTimeZone.UTC));
        } else if (Date.class.isAssignableFrom(object.getClass())) {
            Date date = (Date) object;
            return DateUtils.formatDate(DATE_FORMAT, date);
        }

        else {

            throw new RuntimeException("DateTimeOneWayBridge can not be used for type:" + object.getClass().getName());
        }
    }
}

