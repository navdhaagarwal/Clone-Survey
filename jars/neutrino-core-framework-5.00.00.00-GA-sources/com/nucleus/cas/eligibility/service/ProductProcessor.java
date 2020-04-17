package com.nucleus.cas.eligibility.service;

import com.nucleus.core.genericparameter.entity.GenericParameter;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.Entity;

@Entity
@DynamicUpdate
@DynamicInsert
public class ProductProcessor extends GenericParameter {

    private static final long  serialVersionUID = -8476878500574133533L;

    public static final String mCAS             = "mCAS";
    public static final String INTERNAL         = "INTERNAL";
    public static final String EXTERNAL         = "EXTERNAL";
    public static final String mServe         	= "mServe";
	public static final String LMS              = "LMS";
}
