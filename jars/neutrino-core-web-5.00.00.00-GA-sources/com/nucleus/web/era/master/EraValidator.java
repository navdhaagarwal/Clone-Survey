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
package com.nucleus.web.era.master;

import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import com.nucleus.era.Era;
import com.nucleus.web.common.controller.CASValidationUtils;

/**
 * @author Nucleus Software Exports Limited To validate operations on 
 *         Era Master
 */
public class EraValidator implements Validator {

    @Override
    public boolean supports(Class<?> clazz) {
        return Era.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        CASValidationUtils.rejectIfEmptyOrWhitespace(errors, "eraName", "label.era.required.era.eraName");
        CASValidationUtils.rejectIfEmptyOrWhitespace(errors, "eraSymbol", "label.era.required.era.eraSymbol");
        CASValidationUtils.rejectIfEmptyOrWhitespace(errors, "startYear", "label.era.required.era.startYear");

        Era era = (Era) target;

        if (!CASValidationUtils.isCharactersOnly(era.getEraName()))
            errors.rejectValue("eraName", "label.for.character");

        if (!CASValidationUtils.isCharactersOnly(era.getEraSymbol().toString()))
            errors.rejectValue("eraSymbol", "label.for.character");

        if (!CASValidationUtils.isDigitOnly(era.getStartYear().toString()))
            errors.rejectValue("startYear", "label.for.digit");

    }

}
