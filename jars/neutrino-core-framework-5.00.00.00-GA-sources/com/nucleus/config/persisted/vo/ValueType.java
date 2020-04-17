/*
 * 
 */
package com.nucleus.config.persisted.vo;

/**
 * The Enum ValueType that specifies the type of values that can be stored as configuration
 * 
 * @author Nucleus Software Exports Limited
 */
public enum ValueType {

    /** The normal text. */
    NORMAL_TEXT,
    /** The date. */
    DATE,
    /** The time. */
    TIME,
    /** The date range. */
    DATE_RANGE,
    /** The time range. */
    TIME_RANGE,
    /** The day of week. */
    DAY_OF_WEEK,
    /** The days of week range. */
    DAYS_OF_WEEK_RANGE,
    /**The boolean parameter. */
    BOOLEAN_VALUE,
    /**The Dashboard parameter. */
    DASHBOARD;
    
}