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
 * Constants for Parameter type
 */

@Entity
@DynamicUpdate
@DynamicInsert
public class ParameterType extends GenericParameter {

    private static final long         serialVersionUID            = 5866847751882277342L;

    public static final int           PARAMETER_TYPE_OBJECT_GRAPH = 1;

    public static final int           PARAMETER_TYPE_QUERY        = 2;

    public static final int           PARAMETER_TYPE_CONSTANT     = 3;

    public static final int           PARAMETER_TYPE_REFERENCE    = 4;

    public static final int           PARAMETER_TYPE_COMPOUND     = 5;

    public static final int           PARAMETER_TYPE_PLACEHOLDER  = 6;

    public static final int           PARAMETER_TYPE_SCRIPT       = 7;

    public static final int           PARAMETER_TYPE_DERIVED      = 8;

    public static final int           PARAMETER_TYPE_SYSTEM       = 9;

    public static final int           PARAMETER_TYPE_SQL          = 10;

    public static final List<Integer> ALL_STATUSES                = Collections.unmodifiableList(Arrays.asList(
                                                                          PARAMETER_TYPE_OBJECT_GRAPH, PARAMETER_TYPE_QUERY,
                                                                          PARAMETER_TYPE_CONSTANT, PARAMETER_TYPE_REFERENCE,
                                                                          PARAMETER_TYPE_COMPOUND,
                                                                          PARAMETER_TYPE_PLACEHOLDER, PARAMETER_TYPE_SCRIPT,
                                                                          PARAMETER_TYPE_DERIVED,PARAMETER_TYPE_SQL));
}