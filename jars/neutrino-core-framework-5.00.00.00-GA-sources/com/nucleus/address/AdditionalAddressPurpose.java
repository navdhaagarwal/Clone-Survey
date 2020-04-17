package com.nucleus.address;

import javax.persistence.Entity;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.nucleus.core.genericparameter.entity.GenericParameter;

@Entity
@DynamicUpdate
@DynamicInsert
public class AdditionalAddressPurpose extends GenericParameter {

    private static final long  serialVersionUID             = 7807376737539208428L;

    public static final String PURPOSE_BILLING     = "Billing";

}
