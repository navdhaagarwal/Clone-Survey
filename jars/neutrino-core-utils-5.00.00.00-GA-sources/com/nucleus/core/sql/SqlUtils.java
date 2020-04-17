package com.nucleus.core.sql;

import org.apache.commons.lang3.StringUtils;

import java.util.regex.Pattern;

/**
 * Created by gajendra.jatav on 10/18/2019.
 */
public class SqlUtils {

    private final static Pattern wildCardPattern = Pattern.compile("([_%])");

    private static final String ESCAPE_CLAUSE = " ESCAPE '|' ";

    private SqlUtils(){

    }

    public static String escapeWildcards(String param){
        if (StringUtils.isBlank(param)){
            return param;
        }
        return wildCardPattern.matcher(param).replaceAll("|$1");
    }

    public static String getEscapeClause() {
        return ESCAPE_CLAUSE;
    }
}
