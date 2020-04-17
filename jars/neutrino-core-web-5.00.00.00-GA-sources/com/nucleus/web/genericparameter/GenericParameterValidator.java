/**
 * This file and a proportion of its content is copyright of Nucleus Software Exports Limited - © 2012. All rights reserved.
 * Any redistribution or reproduction of part or all of the contents in any form is prohibited other than the following:
 * - you cannot print or download to a local hard disk extract contents either part or full for personal/ commercial/
 * academic or any other use
 * - you may not copy the content to individual/ third parties for any type of use, either as compiled or source format
 * without the knowledge and consent of Nucleus SOftware
 * - You may not, except with our express written permission, distribute or commercially exploit the content. Nor may you
 * transmit it or store it in any other web site or other form of electronic retrieval system.
 */
package com.nucleus.web.genericparameter;

import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

/**
 * @author Nucleus Software Exports Limited
 * TODO -> amit.parashar Add documentation to class
 */
public class GenericParameterValidator implements Validator {

    /* (non-Javadoc) @see org.springframework.validation.Validator#supports(java.lang.Class) */
    @Override
    public boolean supports(Class<?> clazz) {
        return GenericParameterForm.class.isAssignableFrom(clazz);
    }

    /* (non-Javadoc) @see org.springframework.validation.Validator#validate(java.lang.Object, org.springframework.validation.Errors) */
    @Override
    public void validate(Object target, Errors errors) {
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "code","label.required.GenericParameters.code");
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "name","label.required.GenericParameters.name");
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "description","label.required.GenericParameters.description");
    }

}
