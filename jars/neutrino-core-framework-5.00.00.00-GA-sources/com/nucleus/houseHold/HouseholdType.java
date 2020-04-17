package com.nucleus.houseHold;

import com.nucleus.core.genericparameter.entity.GenericParameter;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.Entity;

@Entity
@DynamicInsert
@DynamicUpdate
public class HouseholdType extends GenericParameter {
    public static final String SINGLE = "Single";
    public static final String COUPLE = "Couple";
    public static final String FAMILY = "Family";
}

