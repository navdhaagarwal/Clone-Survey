package com.nucleus.finnone.pro.base.utility;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.nucleus.finnone.pro.general.util.ValidatorUtils.notNull;

public class SpecialCharacterDecoder {

	public static final List<String> specialCharacter;
	public static final List<String> specialCharacterMask;

	static{
		String[] specialCharacterArray={ "<", ">", "(", ")", "'", "!", ";", "alert", "confirm", "prompt"};
		String[] specialCharacterMaskArray={"$az@", "@za$",  "$a2z@", "@z2a$", "$a3z@", "$a4z@", "$a5z@", "$a6z@", "$a7z@", "$a8z@"};
		specialCharacter=Collections.unmodifiableList(Arrays.asList(specialCharacterArray));
		specialCharacterMask=Collections.unmodifiableList(Arrays.asList(specialCharacterMaskArray));
	}

	private SpecialCharacterDecoder(){
	  
	}
	
	public static String decodeParameterValue(String requestParamValueTmp) {
	    String requestParamValue=requestParamValueTmp;
		if (notNull(requestParamValue)) {
			for (int i = 0; i < specialCharacter.size(); i++) {
				if (requestParamValue.contains(specialCharacterMask.get(i))) {

				    int specCharCount = getSpecCharCount(requestParamValue,specialCharacterMask.get(i));
					for (int j = 0; j < specCharCount; j++) {
						if (requestParamValue.contains(specialCharacterMask.get(i))) {
						  if(specialCharacter.get(i).length() > 1) 
						    requestParamValue = requestParamValue.replace(specialCharacterMask.get(i), "");
						  else 
						    requestParamValue = requestParamValue.replace(specialCharacterMask.get(i), specialCharacter.get(i));						    
						}

					}
				}
			}
		}
		return requestParamValue;
	}

	private static int getSpecCharCount(String requestParamValue,String specialCharacterMask) {
		int lastIndex = 0;
		int count = 0;
		while (lastIndex != -1) {
			lastIndex = requestParamValue.indexOf(specialCharacterMask, lastIndex);
			if (lastIndex != -1) {
				count++;
				lastIndex += specialCharacterMask.length();
			}
		}
		return count;
	}
}
