package com.nucleus.security.masking.types;

import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

import com.nucleus.security.masking.entities.MaskingDefinition;

public class EmailMasking implements MaskingType,Constants {

	
	@Override
	public String getMaskedValue(String value, MaskingDefinition maskingDefinition) {

		// validate email address 
		String[] values = value.split(Constants.AT_THE_RATE);

		String[] elems = maskingDefinition.getExpression().split(Constants.COLON);  //04:F:02:L  


		String before = null;
		String after = null;
		
		String temp = values[1].substring(0, values[1].indexOf(Constants.DOT)); // part of mail after @ and before first dot

		if(Constants.FIRST.equals(elems[1])){

			before = applyFirstCharMasking(values[0], Integer.parseInt(elems[0]), maskingDefinition.getMaskingCharacter());
		}else{

			before = applyLastCharMasking(values[0], Integer.parseInt(elems[0]), maskingDefinition.getMaskingCharacter());
		}


		if(Constants.FIRST.equals(elems[3])){

			after = applyFirstCharMasking(temp, Integer.parseInt(elems[2]), maskingDefinition.getMaskingCharacter());
		}else{

			after = applyLastCharMasking(temp, Integer.parseInt(elems[2]), maskingDefinition.getMaskingCharacter());
		}

		
		value = value.replace(values[0], before);
		value = value.replace(temp, after);

		return value;
	}

	private String applyLastCharMasking(String value, 
			int numofCharectersToBeMasked, String maskingCharacter){

		int length = value.length();
		/*  if numofCharectersToBeMasked is 0 or -1 or any negative number just return the value as it is
		 */
		if(numofCharectersToBeMasked<1){
			return value;
		}

		/* numofCharectersToBeMasked=4 and String is ram then all characters will be masked
		 *  and there will be no need to apply masking any further
		 */
		if(length<=numofCharectersToBeMasked){
			return Utility.getReplaceCharacters(length, maskingCharacter);
		}

		String replace = Utility.getReplaceCharacters(numofCharectersToBeMasked, maskingCharacter);
		String regex = Constants.LAST_CHAR_REGEX.replace(Constants.X, Integer.toString(numofCharectersToBeMasked));

		Pattern p = Utility.getPattern(regex);

		return  p.matcher(value).replaceFirst(replace);
	}

	private String applyFirstCharMasking(String value, 
			int numofCharectersToBeMasked, String maskingCharacter){

		int length = value.length();

		/*  if numofCharectersToBeMasked is 0 or -1 or any negative number just return the value as it is
		 */
		if(numofCharectersToBeMasked<1){
			return value;
		}

		/* numofCharectersToBeMasked=4 and String is ram then all characters will be masked
		 *  and there will be no need to apply masking any further
		 */
		if(length<=numofCharectersToBeMasked){
			return Utility.getReplaceCharacters(length, maskingCharacter);
		}


		String replace = Utility.getReplaceCharacters(numofCharectersToBeMasked, maskingCharacter);

		String regex = Constants.FIRST_CHAR_REGEX.replace(Constants.X, Integer.toString(numofCharectersToBeMasked));

		Pattern p = Utility.getPattern(regex);

		return p.matcher(value).replaceFirst(replace);
	}

	@Override
	public boolean isValidExpression(String expression) {
		return StringUtils.isNotBlank(expression) && expression.matches(Constants.INDEX_MASKING_REGEX);
	}
}
