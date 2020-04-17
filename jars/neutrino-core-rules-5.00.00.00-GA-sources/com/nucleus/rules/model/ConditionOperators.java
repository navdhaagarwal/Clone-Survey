package com.nucleus.rules.model;

/**
 * 
 * @author Nucleus Software Exports Limited
 * 
 */

public interface ConditionOperators {

    public static final String ADD            = "+";
    public static final String SUB            = "-";
    public static final String MUL            = "*";
    public static final String DIV            = "/";
    public static final String MODULUS        = "%";
    public static final String EQ             = "==";
    public static final String NE             = "!=";
    public static final String GT             = ">";
    public static final String LT             = "<";
    public static final String GTEQ           = ">=";
    public static final String LTEQ           = "<=";
    public static final String STR_CONTAINS   = "contains";
    public static final String INSTANCEOF     = "instanceof ";
    public static final String STR_SIMILARITY = "strsim";
    public static final String STR_SOUNDSLIKE = "soundslike";
    public static final String STR_REGEX      = "~=";
    public static final String STR_IN_COLL    = "in";
    public static final String ASSERT         = "assert";

}
