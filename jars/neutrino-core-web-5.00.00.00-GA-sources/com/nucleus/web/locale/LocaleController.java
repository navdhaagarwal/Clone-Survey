package com.nucleus.web.locale;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.nucleus.logging.BaseLoggers;
import com.nucleus.user.UserService;
import com.nucleus.web.common.controller.BaseController;


@Transactional
@Controller
@RequestMapping(value = "/locale")
public class LocaleController extends BaseController {

	@Inject
    @Named("messageSource")
    protected MessageSource           messageSource;

	@Inject
    @Named("userService")
    private UserService                 userService;
	
    /**
     * This method is being used for validating the locale from the available system locales.
     * @param locale
     * @return boolean
     */
    @ResponseBody
    @RequestMapping(value = "/validateLocale/{locale}", method = RequestMethod.GET)
    public Map<String, Object> validateLocale(@PathVariable String locale, ModelMap map) {
		Map<String, Object> resultMap = new HashMap<String, Object>();
		boolean isValidLocaleFlag = false;
		
        if(LocaleUtils.isValidLocale(locale)) {
        	isValidLocaleFlag = true;
        }
        
		try {
			 resultMap.put("valid", isValidLocaleFlag);
			 String invalidLocaleMsg = messageSource.getMessage("label.currency.validation.invalidLocale", null, userService.getUserLocale());
			 resultMap.put("message", invalidLocaleMsg);
		} catch (Exception ex) {
			BaseLoggers.exceptionLogger.error("Exception: " + ex.getMessage(),
					ex);
		}
		return resultMap;
    }
}
