package com.nucleus.demographics;

import javax.persistence.Entity;
import javax.persistence.Transient;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.nucleus.core.genericparameter.entity.GenericParameter;

@Entity
@DynamicUpdate
@DynamicInsert
public class ResidentType extends GenericParameter {

    @Transient
    private static final long serialVersionUID = 1L;
    
    public static final String RESIDENT_TYPE_RESIDENT          = "Resident";
    public static final String RESIDENT_TYPE_NON_RESIDENT        = "Non-Resident";
    public static final String RESIDENT_TYPE_FOREIGN_NATIONAL = "ForeignNational";
}
