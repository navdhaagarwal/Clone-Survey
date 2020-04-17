package com.nucleus.config.persisted.configconvertors;

import org.apache.commons.lang3.StringUtils;

public class ConfigConvertorUtils {
    
    public static String[] splitValue(String value,String separator) {
        String[] stringArr = StringUtils.split(value, separator);

        int counter = 0;
        for (String string : stringArr) {
            stringArr[counter] = string.trim();
            counter++;
        }
        return stringArr;
    }

}
