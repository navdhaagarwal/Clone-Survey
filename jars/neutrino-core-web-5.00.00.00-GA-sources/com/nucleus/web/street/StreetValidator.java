package com.nucleus.web.street;

import com.nucleus.address.*;
import com.nucleus.jsMessageResource.service.*;
import com.nucleus.web.common.controller.*;
import org.springframework.validation.*;

import javax.inject.*;

public class StreetValidator implements Validator {

    @Inject
    @Named("jsMessageResourceService")
    public JsMessageResourceService jsMessageResourceService;

    @Override
    public boolean supports(Class<?> aClass) {
        return Street.class.isAssignableFrom(aClass);
    }

    @Override
    public void validate(Object o, Errors errors) {
        CASValidationUtils.rejectIfEmptyOrWhitespace(errors, "streetCode", "label.required.street.code");
        CASValidationUtils.rejectIfEmptyOrWhitespace(errors, "streetName", "label.required.street.name");
        CASValidationUtils.rejectIfEmptyOrWhitespace(errors, "abbreviation", "label.required.street.abbreviation");
        CASValidationUtils.rejectIfEmptyOrWhitespace(errors, "city.id", "label.required.city");

    }
}
