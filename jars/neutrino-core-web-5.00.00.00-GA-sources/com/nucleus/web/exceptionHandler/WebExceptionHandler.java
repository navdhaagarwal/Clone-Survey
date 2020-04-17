
package com.nucleus.web.exceptionHandler;

import java.security.Principal;
import java.util.Locale;

import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.support.RequestContextUtils;

import com.nucleus.core.exceptions.services.ServiceError;
import com.nucleus.exceptionLogging.ExceptionLoggingService;
import com.nucleus.logging.BaseLoggers;
import com.nucleus.web.common.controller.BaseController;
import com.nucleus.web.security.XssException;

@Controller
@RequestMapping("/webExceptionHandler")
public class WebExceptionHandler extends BaseController {

    @Inject
    @Named("messageSource")
    protected MessageSource messageSource;
    
    @Inject
    @Named("exceptionLoggingService")
    private ExceptionLoggingService exceptionLoggingService;
    
    // this mapping is responsible for rendering view for all exceptions in nonAjax calls.
    @RequestMapping(value = "/nonAjaxErrorRedirectPage")
    public String nonAjaxErrorRedirectPage(HttpServletRequest request, Model model) {

        Locale loc = RequestContextUtils.getLocale(request);
        String nonAjaxErrorMsg = messageSource.getMessage("label.error.msg.nonAjaxCalls", null, loc);
        model.addAttribute("errorMessage", nonAjaxErrorMsg);
        String errorViewName = (String) request.getAttribute("errorViewName");
        return errorViewName;
    }

    

    // this mapping is responsible for sending error message with suitable error code for all exceptions in Ajax calls.
    @RequestMapping(value = "/ajaxErrorRedirectPage")
    @ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
    @ResponseBody
    public String ajaxErrorRedirectPage(HttpServletRequest request, Model model) {
        Locale loc = RequestContextUtils.getLocale(request);
        String ajaxErrorMsg = messageSource.getMessage("label.error.msg.ajaxCalls", null, loc);
        return ajaxErrorMsg;
    }

    @RequestMapping(value = "/pageNotFound")
    public String handlePageNotFound(ModelMap map) {
        map.put("msg", "label.page.not.found");
        return "httpError";
    }
    
    @RequestMapping(value = "/accessDenied")
    public String handlePageAccessDenied(ModelMap map,HttpServletRequest request) {
    	map.put("msg", "label.resource.access.denied");
    	return "httpError";
    }
    
    /**
     * Handle service not found and respond with acceptable mime type.
     * 
     * @param principal log userInfo, if available
     * @param authorization log authorization information attached with the request
     * 
     * @return ServiceError with error message and description
     */
    @RequestMapping(value = "/pageNotFound", produces = {"application/json", "!application/xml"})
    @ResponseStatus(value = HttpStatus.NOT_FOUND)
    public @ResponseBody ServiceError handleServiceNotFound(Principal principal, Locale locale,
            @RequestHeader(value = "Authorization", required=false) String authorization) {
        logError(principal, authorization, HttpStatus.NOT_FOUND.getReasonPhrase());
        return new ServiceError(messageSource.getMessage("label.httpstatus.notFound", null, locale),
                messageSource.getMessage("label.httpstatus.notFound.message", null, locale));
    }

    @RequestMapping(value = "/pageNotFoundTaskNull/{msg}")
    public String handlePageNotFoundForTaskNull(ModelMap map, @PathVariable("msg") String msg) {
        map.put("msg", msg);
        return "httpError";
    }

	@RequestMapping(value = "/uncaughtException")
	public String handleUncaughtException(ModelMap map, HttpServletRequest request) {
		Throwable throwable = (Throwable) request.getAttribute("javax.servlet.error.exception");
		if (ExceptionUtils.indexOfThrowable(throwable, XssException.class) != -1
				|| ExceptionUtils.indexOfThrowable(throwable, AccessDeniedException.class) != -1) {
			map.put("msg", "label.resource.access.denied");
		} else {
			map.put("msg", "label.error.page");
		}
		return "httpError";
	}

    /**
     * Handle uncaught exceptions and respond with acceptable mime type.
     * 
     * @param principal log userInfo, if available
     * @param authorization log authorization information attached with the request
     * 
     * @return ServiceError with error message and description
     */
    @RequestMapping(value = "/uncaughtException", produces = {"application/json", "!application/xml"})
    @ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
    public @ResponseBody ServiceError handleUncaughtServiceExceptions(Principal principal, Locale locale,
            @RequestHeader(value = "Authorization", required=false) String authorization) {
        logError(principal, authorization, "UncaughtServiceException");
        return new ServiceError(messageSource.getMessage("label.httpstatus.internalServerError", null, locale),
                messageSource.getMessage("label.httpstatus.internalServerError.message", null, locale));
    }
    
    /**
     * log error to baselogger and couchdb
     * 
     * @param principal log userInfo, if available
     * @param authorization log authorization information attached with the request
     * @param messageTitle tiles of message
     */
    private void logError(Principal principal, String authorization, String messageTitle) {
        if (principal != null) {
            BaseLoggers.flowLogger.info(messageTitle + " : User" + principal.getName() + ", Authorization : " + authorization);
        }else{
            BaseLoggers.flowLogger.info(messageTitle + " : Authorization : " + authorization);
        }
    }

}
