package com.nucleus.core.formsConfiguration;

import javax.persistence.Cacheable;
import javax.persistence.Entity;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.nucleus.core.genericparameter.entity.GenericParameter;

/**
 * Constants for Form Field Type
 */
@Entity
@DynamicUpdate
@DynamicInsert
@Cacheable
public class FieldDataType extends GenericParameter {

    private static final long serialVersionUID         = 3931886480433026989L;

    public static int         DATA_TYPE_TEXT           = 1;

    public static int         DATA_TYPE_INTEGER        = 2;

    public static int         DATA_TYPE_NUMBER         = 3;

    public static int         DATA_TYPE_DATE           = 4;

    public static int         DATA_TYPE_TEXT_BOOLEAN   = 5;

    public static int         DATA_TYPE_TEXT_REFERENCE = 6;

    public static int         DATA_TYPE_TEXT_MODEL     = 7;

    public static int         DATA_TYPE_MONEY          = 8;

    public static int         DATA_TYPE_PHONE          = 9;
    
    public static int         DATA_TYPE_EMAIL          = 10;

    public static int         DATA_TYPE_LOV          = 11;
}
