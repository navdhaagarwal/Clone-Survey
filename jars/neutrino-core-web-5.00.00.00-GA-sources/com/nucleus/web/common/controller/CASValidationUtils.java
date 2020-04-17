package com.nucleus.web.common.controller;

import java.util.Locale;

import org.apache.commons.validator.routines.BigDecimalValidator;
import org.apache.commons.validator.routines.DateValidator;
import org.apache.commons.validator.routines.EmailValidator;
import org.apache.commons.validator.routines.LongValidator;
import org.apache.commons.validator.routines.PercentValidator;
import org.apache.commons.validator.routines.UrlValidator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.validation.ValidationUtils;


public class CASValidationUtils extends ValidationUtils {
	
	    private static String alphanumeric;	 	

	    private static String alphaNumericAndUnderScore;

	    private static String alphaNumericWithSomeSpecialCharacters;

	    private static String charactersOnly;
	    
	    private static String isoCode;
	    
	    private static String id;
	    
	    private static String validBranchCode;

		@Value("${core.web.validation.config.alphanumeric}")
	    public void setAlphanumeric(String alphanum) {
			alphanumeric = alphanum;
		}
	    
	    @Value("${core.web.validation.config.alphaNumericAndUnderScore}")
		public void setAlphaNumericAndUnderScore(String alphanumAndUnderScore) {
			alphaNumericAndUnderScore = alphanumAndUnderScore;
		}

	    @Value("${core.web.validation.config.alphaNumericWithSomeSpecialCharacters}")
		public void setAlphaNumericWithSomeSpecialCharacters(
				String alphaNumWithSomeSpecialCharacters) {
			alphaNumericWithSomeSpecialCharacters = alphaNumWithSomeSpecialCharacters;
		}

	    @Value("${core.web.validation.config.charactersOnly}")
		public void setCharactersOnly(String characOnly) {
			charactersOnly = characOnly;
		}

	    @Value("${core.web.validation.config.ISOCode}")
		public void setIsoCode(String isoCodeValue) {
			isoCode = isoCodeValue;
		}

	    @Value("${core.web.validation.config.id}")
		public void setId(String idValue) {
			id = idValue;
		}
	    		
	    @Value("${core.web.validation.config.validBranchCode}")
	    public void setValidBranchCode(String validBranchCodeExp) {
			validBranchCode = validBranchCodeExp;
		}
	    
    public static boolean isAlphaNumeric(String s) {
        if (s != null) {
        	return s.matches(alphanumeric);
        }
        return false;
    }
  

    public static boolean isAlphaNumericAndUnderScore(String s) {
        if (s != null) {
            return s.matches(alphaNumericAndUnderScore);
        }
        return false;
    }

    public static boolean isAlphaNumericWithSomeSpecialCharacters(String s) {
        if (s != null) {
            return s.matches(alphaNumericWithSomeSpecialCharacters);
        }
        return false;
    }   

    
    public static boolean isDigitOnly(String s) {

        return LongValidator.getInstance().isValid(s);
    }

    public static boolean isCharactersOnly(String s) {

        if (s != null) {
            return s.matches(charactersOnly);
        }
        return false;
    }

    public static boolean isISDCode(String s) {
        if (s != null) {
            return s.matches("\\+*\\d+");
        }
        return false;
    }

    public static boolean isISOCode(String s) {
        if (s != null) {
            return s.matches(isoCode);
        }
        return false;
    }

    public static boolean isId(String s) {
        if (s != null) {
            return s.matches(id);
        }
        return false;
    }

    public static boolean isSize(String s) {
        if (s != null) {
            return s.matches("^[0-9.]+(%|in|cm|mm|em|ex|pt|pc|px)?$");
        }
        return false;
    }

    public static boolean isValidEmail(String s) {

        return EmailValidator.getInstance().isValid(s);
    }

    public static boolean isValidDate(String s) {

        return DateValidator.getInstance().isValid(s, Locale.getDefault());
    }

    public static boolean isValidDateWithPattern(String s, String pattern) {

        return DateValidator.getInstance().isValid(s, pattern);
    }

    public static boolean isValidBoolean(String s) {
        if (s != null) {
            return s.matches("[tT][rR][uU][eE]|[fF][aA][lL][sS][eE]");
        }
        return false;
    }

    public static boolean isValidUrl(String s) {

        return UrlValidator.getInstance().isValid(s);
    }

    public static boolean isValidAmount(String s) {

        return BigDecimalValidator.getInstance().isValid(s);

    }

    public static boolean isValidPercent(String s) {
        return PercentValidator.getInstance().isValid(s);
    }
    
    public static boolean isSpecialCharsAndRegex(String fieldValue,String regex){
    	if (fieldValue != null) {
    		  return fieldValue.matches(regex);
        }
        return false;
    }

    public static boolean isValidBranchCode(String s) {
        if (s != null) {
            return s.matches(validBranchCode);
        }
        return false;
    }
}
