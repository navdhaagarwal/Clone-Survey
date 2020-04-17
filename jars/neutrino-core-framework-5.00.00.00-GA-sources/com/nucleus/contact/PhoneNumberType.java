package com.nucleus.contact;

import javax.persistence.Entity;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.nucleus.core.genericparameter.entity.GenericParameter;

@Entity
@DynamicUpdate
@DynamicInsert
public class PhoneNumberType extends GenericParameter{

    private static final long serialVersionUID = 4427035503776278035L;
    
    public static final String LANDLINE_NUMBER = "Phone";
    public static final String MOBILE_NUMBER = "Mobile";
    public static final String PRIMARY_MOBILE_NUMBER = "PRIMARY_MOBILE_NUMBER";
    public static final String PRIMARY_PHONE_NUMBER = "PRIMARY_PHONE_NUMBER";
	
	public static final String FAX="FAX";
	
}
