package com.nucleus.broadcast.entity;

import javax.persistence.Entity;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.nucleus.core.genericparameter.entity.GenericParameter;

@Entity
@DynamicUpdate
@DynamicInsert
public class MessagePriority extends GenericParameter{
	
	 private static final long  serialVersionUID        = -119243391423862264L;

	    public static final String HIGH = "HIGH";
	    public static final String MEDIUM = "MEDIUM";
	    public static final String LOW = "LOW";

	    

}
