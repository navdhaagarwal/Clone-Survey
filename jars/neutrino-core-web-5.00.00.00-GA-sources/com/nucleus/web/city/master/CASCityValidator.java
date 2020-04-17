package com.nucleus.web.city.master;

import org.springframework.validation.Errors;

import com.nucleus.address.City;
import com.nucleus.web.common.controller.CASValidationUtils;

import com.nucleus.jsMessageResource.service.JsMessageResourceService;
import com.nucleus.user.UserService;

import javax.inject.Inject;
import javax.inject.Named;

public class CASCityValidator implements CityValidator {
	

    @Inject
    @Named("jsMessageResourceService")
    public JsMessageResourceService jsMessageResourceService;

    @Inject
    @Named("userService")
    public UserService userService;

    @Override
    public boolean supports(Class<?> clazz) {
        return City.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        CASValidationUtils.rejectIfEmptyOrWhitespace(errors, "country.id", "label.requiredcountry");
        CASValidationUtils.rejectIfEmptyOrWhitespace(errors, "state.id", "label.required.state.name");
        CASValidationUtils.rejectIfEmptyOrWhitespace(errors, "cityName", "label.requiredcityname");
        CASValidationUtils.rejectIfEmptyOrWhitespace(errors, "cityCode", "label.requiredcitycode");
        CASValidationUtils.rejectIfEmptyOrWhitespace(errors, "stdCode", "label.requiredstdcode");

        City city = (City) target;
        if (!CASValidationUtils.isAlphaNumericAndUnderScore(city.getCityCode()))
            errors.rejectValue("cityCode", "label.for.alphanumeric");

        if (!errors.hasFieldErrors("stdCode") && !CASValidationUtils.isDigitOnly(city.getStdCode()))
            errors.rejectValue("stdCode", "label.for.digit");
        
       String regexForCityName=jsMessageResourceService.getAppendedPropertyForKeys("allowed.specChars.city.name","core.web.validation.config.customValidatorForCityName");
        if (!CASValidationUtils.isSpecialCharsAndRegex(city.getCityName(),regexForCityName) )
            errors.rejectValue("cityName", "label.for.character");
    }
}
