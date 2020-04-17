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
package com.nucleus.web.currency.master;

import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import com.nucleus.currency.Currency;
import com.nucleus.web.common.controller.CASValidationUtils;

/**
 * @author Nucleus Software Exports Limited To validate operations on 
 *         Currency Master
 */
public class CurrencyValidator implements Validator {

    @Override
    public boolean supports(Class<?> clazz) {
        return Currency.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        CASValidationUtils.rejectIfEmptyOrWhitespace(errors, "isoCode", "label.currency.required.currency.isoCode");
        CASValidationUtils.rejectIfEmptyOrWhitespace(errors, "isoNumber", "label.currency.required.currency.isoNumber");
        CASValidationUtils.rejectIfEmptyOrWhitespace(errors, "currencyName", "label.currency.required.currencyName");
        CASValidationUtils.rejectIfEmptyOrWhitespace(errors, "locale", "label.currency.required.currency.locale");
        CASValidationUtils.rejectIfEmptyOrWhitespace(errors, "symbol", "label.currency.required.currency.symbol");
        CASValidationUtils.rejectIfEmptyOrWhitespace(errors, "decimalPlaces", "label.currency.required.currency.decimalPlaces");
        CASValidationUtils.rejectIfEmptyOrWhitespace(errors, "currencyUnitName", "label.currency.required.unitName");
        CASValidationUtils.rejectIfEmptyOrWhitespace(errors, "currencyFractionName", "label.currency.required.fraction");
        
        Currency currency = (Currency) target;
        
        if (!CASValidationUtils.isCharactersOnly(currency.getCurrencyName()))
            errors.rejectValue("currencyName", "label.for.character");
        
        if (!CASValidationUtils.isISOCode(currency.getIsoCode()))
            errors.rejectValue("isoCode", "label.for.isoCode");
        
        //if (!CASValidationUtils.isCharactersOnly(currency.getSymbol()))
          //  errors.rejectValue("symbol", "label.for.character");
        
        //if (!CASValidationUtils.isCharactersOnly(currency.getLocale().toString()))
          //  errors.rejectValue("locale", "label.for.character");
        
        if (!CASValidationUtils.isDigitOnly(currency.getIsoNumber()))
            errors.rejectValue("isoNumber", "label.for.digit");
        
        if (!CASValidationUtils.isDigitOnly(String.valueOf(currency.getDecimalPlaces())))
            errors.rejectValue("decimalPlaces", "label.for.digit");
        

    }

}
