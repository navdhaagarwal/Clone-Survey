package com.nucleus.shortform;

import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import com.nucleus.shortFormMaster.ShortForm;
import com.nucleus.web.common.controller.CASValidationUtils;

/**
 * Server Side Validator Class for ShortForm Master
 * @author Nucleus Software Exports Limited
 * 
 */

public class ShortFormValidator implements Validator {

    @Override
    public boolean supports(Class<?> clazz) {

        return false;
    }

    @Override
    public void validate(Object target, Errors errors) {
        CASValidationUtils.rejectIfEmptyOrWhitespace(errors, "code", "label.required.code");
        CASValidationUtils.rejectIfEmptyOrWhitespace(errors, "description", "label.required.description");

        ShortForm shortForm = (ShortForm) target;
        if (shortForm.getCode() != null) {
            if (!CASValidationUtils.isAlphaNumeric(shortForm.getCode())) {
                errors.rejectValue("code", "label.for.alphanumeric");
            }
        }

        if (shortForm.getDescription() != null) {
            if (!CASValidationUtils.isAlphaNumeric(shortForm.getDescription())) {
                errors.rejectValue("description", "label.for.alphanumeric");
            }
        }

    }

}
