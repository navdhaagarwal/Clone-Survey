package com.nucleus.demographics;

import javax.persistence.Entity;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.nucleus.core.genericparameter.entity.GenericParameter;

@Entity
@DynamicUpdate
@DynamicInsert
public class IdentificationTypeValue extends GenericParameter {

    private static final long  serialVersionUID                       = 6737776579385415892L;

    public static final String IDENTIFICATION_TYPE_VALUE_ALPHANUMERIC = "Alphanumeric";
    public static final String IDENTIFICATION_TYPE_VALUE_NUMERIC      = "Numeric";
    public static final String IDENTIFICATION_TYPE_VALUE_CHARACTER    = "Character";

}
