package com.nucleus.person.entity;

import javax.persistence.Entity;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.nucleus.core.genericparameter.entity.GenericParameter;

@Entity
@DynamicUpdate
@DynamicInsert
public class GenderType extends GenericParameter {

    private static final long  serialVersionUID          = 2158587225880893575L;

    public static final String GENDER_TYPE_MALE          = "MALE";
    public static final String GENDER_TYPE_FEMALE        = "FEMALE";
    public static final String GENDER_TYPE_NOT_SPECIFIED = "NOT_SPECIFIED";
    public static final String GENDER_TYPE_THIRD_GENDER="THIRD GENDER";
}
