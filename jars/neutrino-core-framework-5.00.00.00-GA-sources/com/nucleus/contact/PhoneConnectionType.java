package com.nucleus.contact;

import javax.persistence.Entity;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.nucleus.core.genericparameter.entity.GenericParameter;

/**
 * Created by rohit.chhabra on 2/7/2018.
 */
@Entity
@DynamicUpdate
@DynamicInsert
public class PhoneConnectionType  extends GenericParameter {
    private static final long serialVersionUID = 1L;

    public static final String PREPAID = "Prepaid";
    public static final String POSTPAID = "Postpaid";
}
