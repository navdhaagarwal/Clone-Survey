package com.nucleus.web.formatter;

import java.text.ParseException;
import java.util.Locale;

import javax.inject.Inject;
import javax.inject.Named;

import org.joda.time.DateTime;
import org.springframework.format.Formatter;

import com.nucleus.core.misc.util.DateUtils;
import com.nucleus.user.UserService;

public class DateTimeFormatter implements Formatter<DateTime> {

    @Inject
    @Named("userService")
    private UserService userService;

    @Override
    public String print(DateTime dateTime, Locale locale) {
        String pattern = userService.getUserPreferredDateFormat();
        return (dateTime != null ? DateUtils.getFormattedDate(dateTime, pattern) : "");
    }

    @Override
    public DateTime parse(String text, Locale locale) throws ParseException {

        return userService.parseDateTime(text);
    }
}
