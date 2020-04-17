package com.nucleus.web.formatter;

import java.text.ParseException;
import java.util.Date;
import java.util.Locale;

import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.format.Formatter;

import com.nucleus.core.misc.util.DateUtils;
import com.nucleus.user.UserService;

public class DateFormatter implements Formatter<Date> {

    @Inject
    @Named("userService")
    private UserService userService;

    @Override
    public String print(Date date, Locale locale) {
        String pattern = userService.getUserPreferredDateFormat();
        return (date != null ? DateUtils.formatDate(pattern, date) : "");
    }

    @Override
    public Date parse(String text, Locale locale) throws ParseException {

        return userService.parseDate(text);
    }
}
