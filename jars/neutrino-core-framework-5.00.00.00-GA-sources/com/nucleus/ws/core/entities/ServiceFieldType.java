package com.nucleus.ws.core.entities;

import javax.persistence.Entity;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.nucleus.core.genericparameter.entity.GenericParameter;

@Entity
@DynamicUpdate
@DynamicInsert
@NamedQueries(  
	    {  
	        @NamedQuery(  
	        name = "getServiceIdentifierByServiceFieldType",  
	        query = " select si from ServiceIdentifier si WHERE  si.serviceFieldType.code in (:serviceFieldTypeCode) or  si.serviceFieldType is null"
	        )  
	    } ) 
public class ServiceFieldType extends GenericParameter{

	private static final long serialVersionUID = 1L;
	
	public static final String SEND = "send";
	public static final String RECEIVE = "receive";
	public static final String SEND_RECEIVE = "send_receive";
}
