package com.nucleus.otp;

import org.apache.commons.lang.StringUtils;

/**
 * Created by gajendra.jatav on 4/23/2019.
 */
public class Utility {

    private Utility() {

    }

    public static String maskString(String inputString, int startIndex, int endIndex, String maskingChar) {
        if (inputString == null || inputString.equals(""))
            return "";

        if (startIndex < 0) {
            startIndex = 0;
        }

        if (endIndex > inputString.length()) {
            endIndex = inputString.length();
        }

        if (startIndex > endIndex) {
            return inputString;
        }

        int maskLength = endIndex - startIndex;

        if (maskLength == 0) {
            return inputString;
        }

        String maskString = StringUtils.repeat(maskingChar, maskLength);

        return StringUtils.overlay(inputString, maskString, startIndex, endIndex);
    }

}
