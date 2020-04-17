package com.nucleus.user;

import javax.persistence.Entity;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.nucleus.core.genericparameter.entity.GenericParameter;

@Entity
@DynamicUpdate
@DynamicInsert
public class DeviceIdentifierType  extends GenericParameter{

	/**
	 * 
	 */
	private static final long serialVersionUID = 3905704207134361914L;
	
	
	public static final String DEVICE_IDENTIFIER_TYPE_IMEI  = "IMEI";
	public static final String DEVICE_IDENTIFIER_TYPE_MEID  = "MEID";
	public static final String DEVICE_IDENTIFIER_TYPE_FCM   = "FCMID";
    
}
