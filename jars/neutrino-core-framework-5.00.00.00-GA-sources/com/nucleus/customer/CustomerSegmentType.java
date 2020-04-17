package com.nucleus.customer;

import javax.persistence.Entity;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.nucleus.core.genericparameter.entity.GenericParameter;

@Entity
@DynamicUpdate
@DynamicInsert
public class CustomerSegmentType extends GenericParameter {

    private static final long  serialVersionUID                        = 5296101890072659165L;

    public static final String CUSTOMER_SEGMENT_TYPE_NEWCUSTOMER       = "NEWCUSTOMER";
    public static final String CUSTOMER_SEGMENT_TYPE_PREFERREDCUSTOMER = "PREFERREDCUSTOMER";
    public static final String CUSTOMER_SEGMENT_TYPE_HNI               = "HNI";
}
