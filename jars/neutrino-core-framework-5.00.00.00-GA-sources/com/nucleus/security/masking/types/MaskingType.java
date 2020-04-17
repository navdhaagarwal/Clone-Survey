package com.nucleus.security.masking.types;

import com.nucleus.security.masking.entities.MaskingDefinition;

public interface MaskingType {

	String getMaskedValue(String value, MaskingDefinition maskingDefinition) ;

	boolean isValidExpression(String expression);
}
