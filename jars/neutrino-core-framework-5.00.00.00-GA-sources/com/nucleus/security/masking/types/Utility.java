package com.nucleus.security.masking.types;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

import com.nucleus.security.masking.types.Constants;

class Utility  {

	private static final Map<String, Pattern> patternMap = new ConcurrentHashMap<>();
	private Utility(){}

	/**
	 * @param regex
	 * @return Compiled pattern corresponding to passed regex.
	 *  Pattern compilation is expensive. Once compiled should be reused.
	 *  patternMap is concurrent hash map since it is a class level member.
	 */
	public static Pattern getPattern(final String regex) {
		Pattern p = patternMap.get(regex);
		if(p==null){
			p = Pattern.compile(regex);
			patternMap.put(regex, p);
		}
		return p;
	}

	/**
	 * 
	 * @param num
	 * @param maskingCharacter
	 * @return number of masked characters e.g. (3,*) will return ***
	 *  A maximum limit to masking should be set or this method will return one character in case max limit is breached
	 */
	public static String getReplaceCharacters(final int num,final String maskingCharacter){
		if(num>Constants.MASKED_CHARECTERS.length())
			return Constants.ZERO_PATTERN.matcher(Constants.MASKED_CHARECTERS.substring(0, 1)).replaceAll(maskingCharacter);

		return Constants.ZERO_PATTERN.matcher(Constants.MASKED_CHARECTERS.substring(0, num)).replaceAll(maskingCharacter);
	}

	/**
	 * 
	 * @param value
	 * @param startIndex
	 * @param numofCharectersToBeMasked
	 * @param maskingCharacter
	 * @return masked value.
	 *  Helper method for mid masking. It is same as index based masking
	 */
	public static String midMasking(final String value,
			final int startIndex, final int numofCharectersToBeMasked, final String maskingCharacter){

		String regex = Constants.MID_CHAR_REGEX.replace(Constants.X, Integer.toString(startIndex));
		regex = regex.replace(Constants.Y, Integer.toString(numofCharectersToBeMasked));

		String replace = getReplaceCharacters(numofCharectersToBeMasked, maskingCharacter);

		Pattern p = getPattern(regex);


		return p.matcher(value).replaceFirst(replace);
	}

}
