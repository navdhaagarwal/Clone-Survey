package com.nucleus.service;

import java.text.ParseException;
import java.util.Locale;

import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.nucleus.config.persisted.vo.ConfigurationVO;
import com.nucleus.core.mutitenancy.service.MultiTenantService;
import com.nucleus.event.EventBus;
import com.nucleus.persistence.EntityDao;
import com.nucleus.user.UserInfo;

@Transactional(propagation = Propagation.REQUIRED)
public abstract class BaseFormServiceImpl extends BaseServiceImpl {

    private static final String DEFAULT_DATE_FORMAT = "MM/dd/yyyy";
    private static final String DEFAULT_TIME_FORMAT = "hh:mm:ss a";
    private static final String SPACE_STRING        = " ";

    @Autowired
    protected EventBus          eventBus;

    @Inject
    @Named("entityDao")
    protected EntityDao         entityDao;

    @Override
    public EventBus getEventBus() {
        return eventBus;
    }

	@Override
    public UserInfo getCurrentUser() {
        UserInfo userInfo = null;
        SecurityContext securityContext = SecurityContextHolder.getContext();
        if (securityContext != null && null != securityContext.getAuthentication()) {
            Object principal = securityContext.getAuthentication().getPrincipal();
            if (UserInfo.class.isAssignableFrom(principal.getClass())) {
                userInfo = (UserInfo) principal;
            }
        }
        return userInfo;
    }

    /**
     * Returns preferred date format string of current user. If there is no current user then a default
     * date format is returned.
     *   
     * @return user preferred date format
     */
    @Override
    public String getUserPreferredDateFormat() {
        String dateFormat = DEFAULT_DATE_FORMAT;
        UserInfo userInfo = getCurrentUser();
        if (userInfo != null) {
            dateFormat = userInfo.getUserPreferences().get("config.date.formats").getText();
        }
        return dateFormat;
    }

    /**
     * Returns preferred date and time format string of current user. If there is no current user then a default
     * date/time format is returned.
     *   
     * @return user preferred date format
     */
    @Override
    public String getUserPreferredDateTimeFormat() {
        return getUserPreferredDateFormat() + SPACE_STRING + DEFAULT_TIME_FORMAT;
    }

    /**
     * Returns the current user's locale.If can not be determined,default locale is returned.
     * 
     * @return current user's locale
     */
    @Override
    public Locale getUserLocale() {
        Locale locale = null;
        UserInfo ui = getCurrentUser();
        if (ui != null) {
            ConfigurationVO preferences = ui.getUserPreferences().get("config.user.locale");
            String[] localeString = preferences.getText().split("_");
            if (localeString.length >= 2) {
                locale = new Locale(localeString[0], localeString[1]);
            }
        } else {
            if (getSystemLocale() != null) {
                locale = getSystemLocale();
            } else {
                locale = Locale.getDefault();
            }
        }
        return locale;

    }

    /**
     * 
     * Format amount to be used in Money tag
     * @param money
     * @return
     */
    /* public String print(String money) {

         Locale userLocale = getUserLocale();

         return MoneyUtils.formatMoneyByLocale(money, userLocale);

     }*/

    /**
     * 
     * Remove comma from the amount
     * @param text
     * @return
     * @throws ParseException
     */
    public String parse(String text) {
        String newStr = text.replaceAll("[^\\d.]+", "");
        return newStr;
    }
}
