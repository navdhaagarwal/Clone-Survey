package com.nucleus.security.masking.types;

import org.apache.commons.lang3.StringUtils;

import com.nucleus.security.masking.entities.MaskingDefinition;

public class IndexBasedMasking implements MaskingType,Constants {

	@Override
	public String getMaskedValue(String value, MaskingDefinition maskingDefinition) {

		
		String[] elems = maskingDefinition.getExpression().split(Constants.COLON);  //02:04
		int startIndex = Integer.parseInt(elems[0]);
		int endIndex = Integer.parseInt(elems[1]);
		
		int length = value.length();  // calculating the length only once
		
		if(length<startIndex) // ram 08:09 then return the value as it is
			return value;
		
		int numofCharectersToBeMasked = 0;
		
		if(length<endIndex){  // Noida  03:08 then No***
			numofCharectersToBeMasked = length-startIndex+1;
		}else{ // Allahabad 03:05 then All***bad
			numofCharectersToBeMasked = endIndex-startIndex+1;
		}
		
		
		return Utility.midMasking(value, startIndex-1, numofCharectersToBeMasked, maskingDefinition.getMaskingCharacter());
	}

	@Override
	public boolean isValidExpression(String expression) {
		return StringUtils.isNotBlank(expression) && expression.matches(Constants.INDEX_MASKING_REGEX);
	}

}
