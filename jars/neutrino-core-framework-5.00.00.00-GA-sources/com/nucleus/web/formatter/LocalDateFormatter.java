package com.nucleus.web.formatter;

import java.text.ParseException;
import java.util.Locale;

import javax.inject.Inject;
import javax.inject.Named;

import org.joda.time.LocalDate;
import org.springframework.format.Formatter;

import com.nucleus.core.misc.util.DateUtils;
import com.nucleus.user.UserService;

public class LocalDateFormatter implements Formatter<LocalDate> {

    @Inject
    @Named("userService")
    private UserService userService;

    @Override
    public String print(LocalDate dateTime, Locale locale) {
        String pattern = userService.getUserPreferredDateFormat();
        return (dateTime != null ? DateUtils.getFormattedDate(dateTime.toDateTimeAtStartOfDay(), pattern) : "");
    }

    @Override
    public LocalDate parse(String text, Locale locale) throws ParseException {

        return userService.parseLocalDate(text);
    }
}