package com.nucleus.customer;

import javax.persistence.Entity;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.nucleus.core.genericparameter.entity.GenericParameter;

/**
 * 
 * @author Nucleus Software Exports Limited
 */
@Entity
@DynamicUpdate
@DynamicInsert
public class RegistrationType extends GenericParameter {
    private static final long  serialVersionUID             = 7807376737539208428L;

    public static final String REGISTRATION_TYPE_LISTED     = "Listed";
    public static final String REGISTRATION_TYPE_NON_LISTED = "Non_Listed";

}
