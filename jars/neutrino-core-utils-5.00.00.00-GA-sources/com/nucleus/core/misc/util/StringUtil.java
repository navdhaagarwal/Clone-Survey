package com.nucleus.core.misc.util;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import com.nucleus.core.validation.util.NeutrinoValidator;
import com.nucleus.logging.BaseLoggers;

public class StringUtil {

    /**
     * This will be released in commons-lang 3.0
     * 
     * <p>
     * Splits a string into an array of strings using upper-case characters as
     * separators.
     * </p>
     * 
     * <p>
     * If <code>str</code> is <code>null</code> returns <code>null</code>
     * </p>
     * 
     * <pre>
     * StringUtils.splitByCamelCase(null)                 = null
     * StringUtils.splitByCamelCase("")                   = []
     * StringUtils.splitByCamelCase("CamelCased")         = ["Camel", "Cased"]
     * StringUtils.splitByCamelCase("thisIsCamelCased")   = ["this", "Is", "Camel", "Cased"]
     * </pre>
     * 
     * @param str
     *            the String to split, may be null
     * @return an array of Strings, <code>null</code> if <code>str</code> was
     *         <code>null</code>
     */
    public static String[] splitByCamelCase(String str) {
        if (str == null) {
            return null;
        }

        int len = str.length();
        if (len == 0) {
            return ArrayUtils.EMPTY_STRING_ARRAY;
        }

        List<String> substrings = new ArrayList<String>();
        int start = 0;
        int counter = 0;

        for (counter = 0 ; counter < len ; counter++) {
            char ch = str.charAt(counter);
            if (counter != 0 && ch >= 'A' && ch <= 'Z') {
                substrings.add(str.substring(start, counter));
                start = counter;
            } else if (counter == (len - 1)) {
                substrings.add(str.substring(start, counter + 1));
            }
        }
        return substrings.toArray(new String[substrings.size()]);
    }

    public static String convertCamelWordsToSentence(String string) {
        String[] words = splitByCamelCase(string);
        String sentence = StringUtils.join(words, ' ');
        sentence = sentence.toLowerCase();
        return sentence;
    }

    /**
     * Converts a camel case string to a (lowercase) snake case string
     * (punctuation is removed and spaces are replaced by single underscores).
     * 
     * @param string
     *            a camel case string.
     * 
     * @return a snake case string.
     */
    public static String convertCamelCaseToSnakeCase(String string) {
        String[] words = splitByCamelCase(string);
        String sentence = StringUtils.join(words, '_');
        sentence = sentence.toLowerCase();
        return sentence;
    }

    /**
     * Performs variable interpolation on the specified String (using system
     * properties).
     * 
     * <p>
     * Variable interpolation (also variable substitution or variable expansion)
     * is the process of evaluating a string literal containing one or more
     * variables, yielding a result in which the variables are replaced with
     * their corresponding values.
     * 
     * <p>
     * For example, the following ...
     * 
     * <pre>
     * System.setProperty(&quot;user.name&quot;, &quot;bbunny&quot;);
     * String result = StringUtil.interpolate(&quot;Hello ${user.name}!&quot;);
     * </pre>
     * 
     * ... produces the output ...
     * 
     * <pre>
     *     Hello bbunny!
     * </pre>
     * 
     * @param templateString
     *            the template string.
     * @return the result.
     */
    public static String interpolate(String templateString) {
        String currentResult = templateString;
        String previousResult = null;

        while (!StringUtils.equals(currentResult, previousResult)) {
            previousResult = currentResult;

            int i = currentResult.indexOf("${");
            if (i > -1) {
                int j = currentResult.indexOf("}");
                if (j > i) {
                    String valueToBeSubstituted = currentResult.substring(i, j + 1);
                    String propertyName = currentResult.substring(i + 2, j);
                    currentResult = StringUtils.replace(currentResult, valueToBeSubstituted,
                            System.getProperty(propertyName));
                }
            }
        }

        return currentResult;
    }

    /**
     * Performs variable interpolation on the specified String (using the
     * specified properties Map).
     * 
     * <p>
     * Variable interpolation (also variable substitution or variable expansion)
     * is the process of evaluating a string literal containing one or more
     * variables, yielding a result in which the variables are replaced with
     * their corresponding values.
     * 
     * <p>
     * For example, the following ...
     * 
     * <pre>
     * Map&lt;String, String&gt; properties = new HashMap&lt;String, String&gt;();
     * properties.put(&quot;user.name&quot;, &quot;bbunny&quot;);
     * String result = StringUtil.interpolate(&quot;Hello ${user.name}!&quot;, properties);
     * </pre>
     * 
     * ... produces the output ...
     * 
     * <pre>
     *     Hello bbunny!
     * </pre>
     * 
     * @param templateString
     *            the template string.
     * @param properties
     *            the Map of properties to be used to resolve variables.
     * @return the result.
     */
    public static String interpolate(String templateString, Map<String, String> properties) {
        String currentResult = templateString;
        String previousResult = null;

        while (!StringUtils.equals(currentResult, previousResult)) {
            previousResult = currentResult;

            int i = currentResult.indexOf("${");
            if (i > -1) {
                int j = currentResult.indexOf("}");
                if (j > i) {
                    String valueToBeSubstituted = currentResult.substring(i, j + 1);
                    String propertyName = currentResult.substring(i + 2, j);
                    currentResult = StringUtils.replace(currentResult, valueToBeSubstituted, properties.get(propertyName));
                }
            }
        }

        return currentResult;
    }

    public static boolean isInteger(String s) {
        try {
            Integer.parseInt(s);
            return true;
        } catch (Exception e) {
            BaseLoggers.exceptionLogger.error("Exception occured in parsing string '" + s + "' to integer  : "
                    + e.getMessage());

            return false;
        }
    }

    public static boolean isFloat(String s) {
        try {
            Float.parseFloat(s);
            return true;
        } catch (Exception e) {

            BaseLoggers.exceptionLogger.error("Exception occured in parsing string '" + s + "' to float: " + e.getMessage());

            return false;
        }
    }

    public static boolean isDate(String s) {
        try {
            DateFormat.getDateInstance().parse(s);
            return true;
        } catch (Exception e) {
            BaseLoggers.exceptionLogger.error("Exception occured in parsing string '" + s + "' to date: " + e.getMessage());
            return false;
        }
    }

    public static boolean isInList(String target, String list) {
        String separator = "::";
        if (list.indexOf(separator) <= 0) {
            separator = ",";
        }
        String[] subStrings = StringUtils.split(list, separator);
        for (String s : subStrings) {
            if (s.trim().equalsIgnoreCase(target)) {
                return true;
            }
        }
        return false;
    }

    public static boolean contains(String s1, String s2) {
        boolean contains = StringUtils.contains(s1, s2);
        return contains;
    }

    public static boolean equals(String s1, String s2) {
        if (s1 == null && s2 != null) {
            return false;
        }
        if (s1 != null && s2 == null) {
            return false;
        }
        return ((s1 == null && s2 == null) || (s1 != null && s2 != null && s1.equals(s2)));
    }

    public static boolean equalsIgnoreCase(String s1, String s2) {
        if (s1 == null && s2 != null) {
            return false;
        }
        if (s1 != null && s2 == null) {
            return false;
        }
        return ((s1 == null && s2 == null) || (s1 != null && s2 != null && s1.equalsIgnoreCase(s2)));
    }
    
    
    /* to capitalize first character
     * for e.g. if the string is "my last method".
     * it will return "My last method"
    */
    public static String capitalizeFirstLetter(String str) {
        if (StringUtils.isNotEmpty(str)) {
            return str.substring(0, 1).toUpperCase() + str.substring(1);
        }
        return str;

    }
    
    public static List<String> split(String value,int maxLength) {
		List<String> values = null;
		if (StringUtils.isNotBlank(value) && maxLength>0) {
			values = new ArrayList<>();
          String originalValue = value;
          while(originalValue.length()>0){
        	  String subString =  StringUtils.substring(originalValue,0, maxLength);
        	  originalValue = StringUtils.substring(originalValue,subString.length(),originalValue.length());
        	  values.add(subString);
          }
		}
		return values;
	}
   
}