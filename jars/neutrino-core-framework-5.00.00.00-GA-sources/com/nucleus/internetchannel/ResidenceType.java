package com.nucleus.internetchannel;

import javax.persistence.Entity;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.nucleus.core.genericparameter.entity.GenericParameter;

@Entity
@DynamicUpdate
@DynamicInsert
public class ResidenceType extends GenericParameter {

    public static final String RESIDENCE_TYPE_FLAT      = "Flat";
    public static final String RESIDENCE_TYPE_ROW_HOUSE = "Row House";
    public static final String RESIDENCE_TYPE_BUNGLOW   = "Bungalow";
    public static final String RESIDENCE_TYPE_OTHERS    = "Others";

}
