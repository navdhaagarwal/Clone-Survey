package com.nucleus.web.masking;

import javax.inject.Named;

import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import com.nucleus.security.masking.entities.MaskingPolicy;
import com.nucleus.security.masking.types.Constants;
import com.nucleus.web.common.controller.CASValidationUtils;

@Named("maskingPolicyValidator")
public class MaskingPolicyValidator implements Validator{

	@Override
	public boolean supports(Class<?> clazz) {
		return MaskingPolicy.class.isAssignableFrom(clazz);
	}

	@Override
	public void validate(Object target, Errors errors) {
        CASValidationUtils.rejectIfEmptyOrWhitespace(errors, Constants.MASKING_POLICY_NAME, "label.required.maskingPolicy.name");
        CASValidationUtils.rejectIfEmptyOrWhitespace(errors, Constants.MASKING_POLICY_CODE, "label.required.maskingPolicy.code");
	}

}
