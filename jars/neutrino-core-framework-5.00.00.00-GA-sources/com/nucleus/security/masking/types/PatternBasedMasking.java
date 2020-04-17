package com.nucleus.security.masking.types;

import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

import com.nucleus.security.masking.entities.MaskingDefinition;

public class PatternBasedMasking implements MaskingType,Constants{


	@Override
	public String getMaskedValue(String value, MaskingDefinition maskingDefinition) {

		String[] elems = maskingDefinition.getExpression().split(Constants.COLON);  //04:02:03
		int length = value.length();  // calculating the length only once
		
		Result result =  applyFirstCharMasking(value, length, Integer.parseInt(elems[0]), maskingDefinition.getMaskingCharacter());

		if(result.isFurtherMaskingNotRequired){
			return result.value;
		}

		result = applyMidCharMasking(result.value, length, Integer.parseInt(elems[1]), maskingDefinition.getMaskingCharacter());

		if(result.isFurtherMaskingNotRequired){
			return result.value;
		}

		result = applyLastCharMasking(result.value, length, Integer.parseInt(elems[2]), maskingDefinition.getMaskingCharacter());

		return result.value;
	}
	
	@Override
	public boolean isValidExpression(String expression){
		return StringUtils.isNotBlank(expression) && expression.matches(Constants.PATTERN_MASKING_REGEX);
	}

	private Result applyFirstCharMasking(String value, 
			int length, int numofCharectersToBeMasked, String maskingCharacter){

		Result res = new Result();
		
		/*  if numofCharectersToBeMasked is 0 or -1 or any negative number just return the value as it is
		 */
		if(numofCharectersToBeMasked<1){
			res.value = value;
			return res;
		}
		
		/* numofCharectersToBeMasked=4 and String is ram then all characters will be masked
		 *  and there will be no need to apply masking any further
		 */
		if(length<=numofCharectersToBeMasked){
			res.value = Utility.getReplaceCharacters(length, maskingCharacter);
			res.isFurtherMaskingNotRequired = true; 
			return res;
		}

		
		String replace = Utility.getReplaceCharacters(numofCharectersToBeMasked, maskingCharacter);
		
		String regex = Constants.FIRST_CHAR_REGEX.replace(Constants.X, Integer.toString(numofCharectersToBeMasked));

		Pattern p = Utility.getPattern(regex);

		res.value = p.matcher(value).replaceFirst(replace);
		return res;
	}


	private Result applyMidCharMasking(String value, 
			int length, int numofCharectersToBeMasked, String maskingCharacter){

		Result res = new Result();

		/*  if numofCharectersToBeMasked is 0 or -1 or any negative number just return the value as it is
		 */
		if(numofCharectersToBeMasked<1){
			res.value = value;
			return res;
		}

		int startIndex = 0;
		int temp = 0;
		
		/* numofCharectersToBeMasked=4 and String is ram then all characters starting from mid character will be masked
		 *  and there will be no need to apply masking any further. First character masking is already applied.
		 *  
		 */
		if(length<=numofCharectersToBeMasked+1){
			startIndex = length/2;
			temp = length-startIndex;
			res.isFurtherMaskingNotRequired = true;
			res.value = Utility.midMasking(value, startIndex, temp, maskingCharacter);
			return res;
		}

		startIndex = (length-numofCharectersToBeMasked)/2;
		res.value = Utility.midMasking(value, startIndex, numofCharectersToBeMasked, maskingCharacter);
		return res;
	}


	private Result applyLastCharMasking(String value, 
			int length, int numofCharectersToBeMasked, String maskingCharacter){


		Result res = new Result();

		/*  if numofCharectersToBeMasked is 0 or -1 or any negative number just return the value as it is
		 */
		if(numofCharectersToBeMasked<1){
			res.value = value;
			return res;
		}

		/* numofCharectersToBeMasked=4 and String is ram then all characters will be masked
		 *  and there will be no need to apply masking any further
		 */
		if(length<=numofCharectersToBeMasked){
			res.value = Utility.getReplaceCharacters(length, maskingCharacter);
			return res;
		}

		String replace = Utility.getReplaceCharacters(numofCharectersToBeMasked, maskingCharacter);
		String regex = Constants.LAST_CHAR_REGEX.replace(Constants.X, Integer.toString(numofCharectersToBeMasked));

		Pattern p = Utility.getPattern(regex);


		res.value = p.matcher(value).replaceFirst(replace);
		return res;
	}

	

	
	class Result{ // Required to make thread safe otherwise need to create class level variable isFurtherMaskingRequired

		private String value;
		private boolean isFurtherMaskingNotRequired;

	}
}
