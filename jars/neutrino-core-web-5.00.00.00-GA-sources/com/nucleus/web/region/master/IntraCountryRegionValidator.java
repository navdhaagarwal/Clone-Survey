/**
 * This file and a proportion of its content is copyright of Nucleus Software Exports Limited - � 2012. All rights reserved.
 * Any redistribution or reproduction of part or all of the contents in any form is prohibited other than the following:
 * - you cannot print or download to a local hard disk extract contents either part or full for personal/ commercial/
 * academic or any other use
 * - you may not copy the content to individual/ third parties for any type of use, either as compiled or source format
 * without the knowledge and consent of Nucleus SOftware
 * - You may not, except with our express written permission, distribute or commercially exploit the content. Nor may you
 * transmit it or store it in any other web site or other form of electronic retrieval system.
 */
package com.nucleus.web.region.master;

import org.springframework.validation.Errors;
import org.springframework.validation.Validator;



import com.nucleus.address.IntraCountryRegion;
import com.nucleus.web.common.controller.CASValidationUtils;

/**
 * Server Side Validator Class Region master
 * @author Nucleus Software Exports Limited
 * 
 */
public class IntraCountryRegionValidator implements Validator {

    @Override
    public boolean supports(Class<?> clazz) {
        return IntraCountryRegion.class.isAssignableFrom(clazz);
    }

	@Override
    public void validate(Object target, Errors errors) {
		CASValidationUtils.rejectIfEmptyOrWhitespace(errors, "intraRegionCode", "label.required.region.name");
		CASValidationUtils.rejectIfEmptyOrWhitespace(errors, "intraRegionName", "label.required.region.code");
        
		IntraCountryRegion region = (IntraCountryRegion)target;
        if(!CASValidationUtils.isAlphaNumericAndUnderScore(region.getIntraRegionCode()))
          	errors.rejectValue("intraRegionCode", "label.for.alphanumeric");
        
        if(!CASValidationUtils.isCharactersOnly(region.getIntraRegionName()))
          	errors.rejectValue("intraRegionName", "label.for.character");
    }

}
