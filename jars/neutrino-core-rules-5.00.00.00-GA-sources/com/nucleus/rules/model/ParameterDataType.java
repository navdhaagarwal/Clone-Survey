package com.nucleus.rules.model;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.persistence.Entity;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.nucleus.core.genericparameter.entity.GenericParameter;

/**
 * @author Nucleus Software Exports Limited
 * Constants for Parameter Data type
 */

@Entity
@DynamicUpdate
@DynamicInsert
public class ParameterDataType extends GenericParameter {

    private static final long         serialVersionUID                = -5265654164895690972L;

    public static final int           PARAMETER_DATA_TYPE_STRING      = 1;
    public static final int           PARAMETER_DATA_TYPE_INTEGER     = 2;
    public static final int           PARAMETER_DATA_TYPE_NUMBER      = 3;
    public static final int           PARAMETER_DATA_TYPE_BOOLEAN     = 4;
    public static final int           PARAMETER_DATA_TYPE_DATE        = 5;
    public static final int           PARAMETER_DATA_TYPE_REFERENCE   = 6;
    public static final int           PARAMETER_DATA_TYPE_PLACEHOLDER = 7;
    public static final int           PARAMETER_DATA_TYPE_COLLECTION  = 8;
    public static final int           PARAMETER_DATA_TYPE_JAVA_UTIL_DATE  = 9;

    public static final List<Integer> ALL_STATUSES                    = Collections.unmodifiableList(Arrays.asList(
                                                                              PARAMETER_DATA_TYPE_STRING,
                                                                              PARAMETER_DATA_TYPE_INTEGER,
                                                                              PARAMETER_DATA_TYPE_NUMBER,
                                                                              PARAMETER_DATA_TYPE_BOOLEAN,
                                                                              PARAMETER_DATA_TYPE_DATE,
                                                                              PARAMETER_DATA_TYPE_REFERENCE,
                                                                              PARAMETER_DATA_TYPE_PLACEHOLDER,
                                                                              PARAMETER_DATA_TYPE_COLLECTION,
                                                                              PARAMETER_DATA_TYPE_JAVA_UTIL_DATE));
}