package com.nucleus.demographics;

import javax.persistence.Entity;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.nucleus.core.genericparameter.entity.GenericParameter;

@Entity
@DynamicUpdate
@DynamicInsert
public class RelationshipType extends GenericParameter {

    private static final long  serialVersionUID           = 6737776579385415892L;
    /*Parent code Family Details Relationship Type*/
    public static final String Father                     = "Father";
    public static final String Mother                     = "Mother";
    public static final String Spouse                     = "Spouse";

    public static final String PARENT_CODE_FAMILY_DETAILS = "FDRT";

    public static final String Self                       = "Self";

}
