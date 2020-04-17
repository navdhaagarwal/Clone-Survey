package com.nucleus.finnone.pro.additionaldata.domainobject;

import javax.persistence.Entity;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.nucleus.core.genericparameter.entity.GenericParameter;

@Entity 
@DynamicInsert 
@DynamicUpdate
public class CustomFieldDataType extends GenericParameter{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public static final String TEXT = "A";
	public static final String AMOUNT = "N";
	public static final String  DATE= "D";
	public static final String INTEGER = "I";
	public static final String  RATE = "R";
	public static final String  LIST = "L";
}
