/**
 * This file and a proportion of its content is copyright of Nucleus Software Exports Limited - � 2012. All rights reserved.
 * Any redistribution or reproduction of part or all of the contents in any form is prohibited other than the following:
 * - you cannot print or download to a local hard disk extract contents either part or full for personal/ commercial/
 * academic or any other use
 * - you may not copy the content to individual/ third parties for any type of use, either as compiled or source format
 * without the knowledge and consent of Nucleus Software
 * - You may not, except with our express written permission, distribute or commercially exploit the content. Nor may you
 * transmit it or store it in any other web site or other form of electronic retrieval system.
 */
package com.nucleus.core.calendar.util;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import com.nucleus.logging.BaseLoggers;

/**
 * @author Nucleus Software Exports Limited
 *
 */
public class XMLGregorianCalendarUtils {
	
	private static DatatypeFactory dataTypeFactory = null;
	
	  static {
	    try {
	      dataTypeFactory = DatatypeFactory.newInstance();
	    } catch (DatatypeConfigurationException e) {
	      BaseLoggers.exceptionLogger.error(e.getMessage(),e);
	    }
	  }

    public static XMLGregorianCalendar getCurrentTimestamp() {
        GregorianCalendar gcal = new GregorianCalendar();
        XMLGregorianCalendar xgcal = dataTypeFactory.newXMLGregorianCalendar(gcal);;
        return xgcal;
    }

    public static XMLGregorianCalendar fromGregorianCalendar(GregorianCalendar gcal) {
        return dataTypeFactory.newXMLGregorianCalendar(gcal);
    }

    public static XMLGregorianCalendar fromCalendar(Calendar cal) {
        GregorianCalendar gcal = new GregorianCalendar();
        gcal.setTime(cal.getTime());
        return dataTypeFactory.newXMLGregorianCalendar(gcal);
    }

    public static XMLGregorianCalendar fromDate(Date date) {
        GregorianCalendar gcal = new GregorianCalendar();
        gcal.setTime(date);
        return dataTypeFactory.newXMLGregorianCalendar(gcal);
    }

    public static XMLGregorianCalendar fromDate(java.sql.Date date) {
        GregorianCalendar gcal = new GregorianCalendar();
        gcal.setTime(date);
        return dataTypeFactory.newXMLGregorianCalendar(gcal);
    }

    public static XMLGregorianCalendar fromSqlDate(java.sql.Date date) {
        GregorianCalendar gcal = new GregorianCalendar();
        gcal.setTime(date);
        return dataTypeFactory.newXMLGregorianCalendar(gcal);
    }

}
