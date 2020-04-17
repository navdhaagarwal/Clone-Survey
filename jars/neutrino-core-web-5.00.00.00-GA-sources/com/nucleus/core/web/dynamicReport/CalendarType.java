package com.nucleus.core.web.dynamicReport;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import net.sf.dynamicreports.report.base.datatype.AbstractDataType;
import net.sf.dynamicreports.report.base.expression.AbstractValueFormatter;
import net.sf.dynamicreports.report.constant.Constants;
import net.sf.dynamicreports.report.definition.ReportParameters;
import net.sf.dynamicreports.report.definition.expression.DRIValueFormatter;
import net.sf.dynamicreports.report.exception.DRException;

import org.apache.commons.lang3.time.DateFormatUtils;

/**
 * @author
 */
public class CalendarType extends AbstractDataType<Calendar, Calendar> {
    public CalendarType(String dateTimePattern) {
        super();
        this.dateTimePattern = dateTimePattern;
    }

    private static final long serialVersionUID = Constants.SERIAL_VERSION_UID;

    private String            dateTimePattern;

    @Override
    public String getPattern() {
        return dateTimePattern;
    }

   /* @Override
    public HorizontalAlignment getHorizontalAlignment() {
        return Defaults.getDefaults().getDateType().getHorizontalAlignment();
    }*/

    @Override
    public String valueToString(Calendar value, Locale locale) {
        if (value != null) {
            return DateFormatUtils.format(value, getPattern());
        }
        return null;
    }

    @Override
    public Calendar stringToValue(String value, Locale locale) throws DRException {
        if (value != null) {
            try {
                SimpleDateFormat dateFormat = new SimpleDateFormat(getPattern());
                Calendar cal = Calendar.getInstance();
                cal.setTime(dateFormat.parse(value));
                return cal;
            } catch (Exception e) {
                throw new DRException("Unable to convert string value to DateTime", e);
            }
        }
        return null;
    }

    @Override
    public DRIValueFormatter<String, Calendar> getValueFormatter() {
        return new CalendarTextValueFormatter(getPattern());
    }

    private static class CalendarTextValueFormatter extends AbstractValueFormatter<String, Calendar> {

        private static final long serialVersionUID = 3043341948081115003L;

        private String            dateTimePattern;

        public CalendarTextValueFormatter(String pattern) {
            dateTimePattern = pattern;
        }

        @Override
        public String format(Calendar value, ReportParameters reportParameters) {
            if (value != null) {
                return DateFormatUtils.format(value, dateTimePattern);
            }
            return null;
        }

    }
}
