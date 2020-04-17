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
package com.nucleus.web.district.master;

import org.springframework.validation.Errors;
import org.springframework.validation.Validator;


import com.nucleus.address.District;
import com.nucleus.web.common.controller.CASValidationUtils;

import com.nucleus.jsMessageResource.service.JsMessageResourceService;
import com.nucleus.user.UserService;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * Server Side Validator Class District master
 * @author Nucleus Software Exports Limited
 * 
 */
public class DistrictValidator implements Validator {
	

    @Inject
    @Named("jsMessageResourceService")
    public JsMessageResourceService jsMessageResourceService;

    @Inject
    @Named("userService")
    public UserService userService;

    @Override
    public boolean supports(Class<?> clazz) {
        return District.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        CASValidationUtils.rejectIfEmptyOrWhitespace(errors, "districtName", "label.required.district.name");
        CASValidationUtils.rejectIfEmptyOrWhitespace(errors, "districtCode", "label.required.district.code");
        CASValidationUtils.rejectIfEmptyOrWhitespace(errors, "districtAbbreviation", "label.required.district.abbreviation");
        CASValidationUtils.rejectIfEmptyOrWhitespace(errors, "state.id", "label.required.state.name");

        District district = (District) target;
        String regexForDistrictName=jsMessageResourceService.getAppendedPropertyForKeys("allowed.specChars.district.name","core.web.validation.config.customValidatorForDistrictName");
        if (!CASValidationUtils.isSpecialCharsAndRegex(district.getDisplayName(),regexForDistrictName))
            errors.rejectValue("districtName", "label.for.alphanumeric");

        if (!CASValidationUtils.isAlphaNumericAndUnderScore(district.getDistrictCode()))
            errors.rejectValue("districtCode", "label.for.alphanumeric");

        if (!CASValidationUtils.isAlphaNumeric(district.getDistrictAbbreviation()))
            errors.rejectValue("districtAbbreviation", "label.for.alphanumeric");

    }
    
}