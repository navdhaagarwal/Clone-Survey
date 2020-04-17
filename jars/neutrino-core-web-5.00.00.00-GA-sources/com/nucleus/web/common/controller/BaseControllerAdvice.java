package com.nucleus.web.common.controller;

import java.util.Locale;

import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.nucleus.config.persisted.vo.ConfigurationVO;
import com.nucleus.core.exceptions.services.ServiceError;
import com.nucleus.core.misc.util.DateUtils;
import com.nucleus.exceptionLogging.ExceptionLoggingService;
import com.nucleus.logging.BaseLoggers;
import com.nucleus.master.BaseMasterService;
import com.nucleus.user.UserInfo;
import com.nucleus.user.UserService;

/**
 * This file and a proportion of its content is copyright of Nucleus Software Exports Limited - © 2012. All rights reserved.
 * @author Nucleus Software Exports Limited
 * @description Base controller advice for all the rest controller; to provide generic services, exception handling and generic functionality.
 */

@ControllerAdvice(annotations = RestController.class)
public class BaseControllerAdvice {

    @Inject
    @Named("baseMasterService")
    protected BaseMasterService     baseMasterService;

    @Inject
    @Named("userService")
    private UserService             userService;

    @Inject
    @Named("exceptionLoggingService")
    private ExceptionLoggingService exceptionLoggingService;

    @Inject
    @Named("messageSource")
    protected MessageSource         messageSource;

    /**
     * Handle uncaught exceptions and respond with acceptable mime type.
     * 
     * @param e log exception
     * 
     * @return ServiceError with error message and description
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
    @ResponseBody
    ServiceError handleExceptions(Exception e, Locale locale) {
        logError(e);
        return new ServiceError(messageSource.getMessage("label.httpstatus.internalServerError", null, locale),
                messageSource.getMessage("label.httpstatus.internalServerError.message", null, locale));
    }

    /**
     * Handle bad request and respond with acceptable mime type.
     * 
     * @param e log exception
     * 
     * @return ServiceError with error message and description
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    @ResponseBody
    ServiceError handleBadRequest(HttpMessageNotReadableException e, Locale locale) {
        logError(e);
        return new ServiceError(messageSource.getMessage("label.httpstatus.badRequest", null, locale),
                messageSource.getMessage("label.httpstatus.badRequest.message", null, locale));
    }

    /**
     * Log exception and userInfo in logs and couchdb.
     * 
     * @param e log exception
     */
    private void logError(Exception e) {
        BaseLoggers.exceptionLogger.error("Exception in Restful services", e);
        exceptionLoggingService.saveExceptionDataInCouch(getUserDetails(), e);
    }

    /**
     * To get Loggedin userName as a Model Attribute in Controllers.
     * @return String
     */
    @ModelAttribute(value = "userName")
    public String getUsername() {
        if (SecurityContextHolder.getContext() != null) {
            return SecurityContextHolder.getContext().getAuthentication().getName();
        }
        return "Guest";
    }

    /**
     * To get userInfo object as a Model Attribute in Controllers.
     * @return UserInfo
     */
    @ModelAttribute(value = "userInfo")
    public UserInfo getUserDetails() {
        UserInfo userInfo = null;
        SecurityContext securityContext = SecurityContextHolder.getContext();
        if (securityContext != null) {
            Object principal = securityContext.getAuthentication().getPrincipal();
            if (UserInfo.class.isAssignableFrom(principal.getClass())) {
                userInfo = (UserInfo) principal;
            }
        }
        return userInfo;
    }

    /**
     * To get locale of incoming HTTP request as a Model Attribute in Controllers.
     * @return Locale
     */
    @ModelAttribute(value = "locale")
    public Locale getUserLocale() {
        Locale locale = null;
        UserInfo ui = getUserDetails();
        if (ui != null) {
        	if("anonymous".equals(ui.getUsername())){
        		
        		/**
        		 * Default locale for anonymous user
        		 * 
        		 * 
        		 */
        		locale = new Locale("en", "US");
        		return locale;
        	}
            ConfigurationVO preferences = ui.getUserPreferences().get("config.user.locale");
            String[] localeString = preferences.getText().split("_");
            if (localeString.length >= 2) {
                locale = new Locale(localeString[0], localeString[1]);
            }
        }
        return locale;
    }

    /**
     * To get user's selected date format from user preferences as a Model Attribute in Controllers.
     * @return String
     */
    @ModelAttribute(value = "userDateFormat")
    public String getUserDateFormat() {
        String pattern = "dd/MM/yyyy";
        if (getUserDetails() != null && getUserDetails().getUserPreferences() != null) {
            ConfigurationVO confVO = getUserDetails().getUserPreferences().get("config.date.formats");
            if (confVO != null) {
                pattern = confVO.getText();
            }
        }
        return pattern;
    }

    /**
     * To get user's selected date and time format from user preferences as a Model Attribute in Controllers.
     * @return String
     */
    @ModelAttribute(value = "userDateTimeFormat")
    public String getUserDateTimeFormat() {
        return getUserDateFormat() + " " + getAppTimeFormat();

    }

    /**
     * To get user's selected time format from user preferences as a Model Attribute in Controllers.
     * @return String
     */
    @ModelAttribute(value = "userTimeFormat")
    public String getAppTimeFormat() {
        return DateUtils.DEFAULT_TIME_FORMAT;
    }

}
