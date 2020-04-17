package com.nucleus.security.masking.types;

import java.util.regex.Pattern;

public interface Constants {
	String COLON = ":";  // used as delimeter
	String X = "X";  // to be replaced in regex
	String Y = "Y";  // to be replaced in regex
	String MASKED_CHARECTERS = "0000000000000000000000000000000000000000000000000000000000000000000000000000";
	String FIRST_CHAR_REGEX = "^.{X}";
	String LAST_CHAR_REGEX = "(.{X}$)";
	String MID_CHAR_REGEX = "(?<=^.{X}).{Y}";
	String AT_THE_RATE = "@";
	String DOT = ".";
	String COMMA = ",";
	String FIRST = "F";
	
	Pattern ZERO_PATTERN = Pattern.compile("0");
	
	String NO_ACTION = "no_action";
	String MASKING_POLICY = "maskingPolicy";
	String MASKING_POLICY_NAME="maskingPolicyName";
	String MASKING_POLICY_CODE= "maskingPolicyCode";
	
	String PATTERN_MASKING_REGEX="[0-9][0-9]:[0-9][0-9]:[0-9][0-9]";
	String INDEX_MASKING_REGEX = "[0-9][0-9]:[0-9][0-9]";
	String EMAIL_MASKING_REGEX="[0-9][0-9]:[F,L]:[0-9][0-9]:[F,L]";
}
