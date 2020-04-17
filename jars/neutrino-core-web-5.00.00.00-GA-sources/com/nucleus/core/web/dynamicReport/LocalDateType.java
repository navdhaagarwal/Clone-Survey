package com.nucleus.core.web.dynamicReport;

import java.util.Locale;

import net.sf.dynamicreports.report.base.datatype.AbstractDataType;
import net.sf.dynamicreports.report.base.expression.AbstractValueFormatter;
import net.sf.dynamicreports.report.constant.Constants;
import net.sf.dynamicreports.report.definition.ReportParameters;
import net.sf.dynamicreports.report.definition.expression.DRIValueFormatter;
import net.sf.dynamicreports.report.exception.DRException;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;

/**
 * @author
 */
public class LocalDateType extends AbstractDataType<LocalDate, LocalDate> {
    public LocalDateType(String dateTimePattern) {
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
    public String valueToString(LocalDate value, Locale locale) {
        if (value != null) {
            return DateTimeFormat.forPattern(getPattern()).print(value);
        }
        return null;
    }

    @Override
    public LocalDate stringToValue(String value, Locale locale) throws DRException {
        if (value != null) {
            try {
                return DateTime.parse(value, DateTimeFormat.forPattern(getPattern())).toLocalDate();
            } catch (Exception e) {
                throw new DRException("Unable to convert string value to DateTime", e);
            }
        }
        return null;
    }

    @Override
    public DRIValueFormatter<String, LocalDate> getValueFormatter() {
        return new LocalDateTextValueFormatter(getPattern());
    }

    private static class LocalDateTextValueFormatter extends AbstractValueFormatter<String, LocalDate> {

        private static final long serialVersionUID = 3043341948080014003L;

        private String            dateTimePattern;

        public LocalDateTextValueFormatter(String pattern) {
            dateTimePattern = pattern;
        }

        @Override
        public String format(LocalDate value, ReportParameters reportParameters) {
            if (value != null) {
                return DateTimeFormat.forPattern(dateTimePattern).print(value);
            }
            return null;
        }

    }
}
