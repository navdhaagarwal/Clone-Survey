package com.nucleus.broadcast.entity;

import javax.persistence.Entity;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.nucleus.core.genericparameter.entity.GenericParameter;

@Entity
@DynamicUpdate
@DynamicInsert
public class MessageSeverity extends GenericParameter{
	
	 private static final long  serialVersionUID        = -119243391423862264L;

	    public static final String CRITICAL = "CRITICAL";
	    public static final String MAJOR = "MAJOR";
	    public static final String MINOR = "MINOR";

	    

}
