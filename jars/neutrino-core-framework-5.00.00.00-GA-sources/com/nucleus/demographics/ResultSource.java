package com.nucleus.demographics;

import javax.persistence.Entity;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.nucleus.core.genericparameter.entity.GenericParameter;

@Entity
@DynamicUpdate
@DynamicInsert
public class ResultSource extends GenericParameter{
	
	  private static final long serialVersionUID = 1L;

	public static final String RESULT_SOURCE_VISIONPLUS = "V+";

	public static final String RESULT_SOURCE_SIMAH = "Simah";
	
	public static final String RESULT_SOURCE_T24 = "T24";
	
	public static final String RESULT_SOURCE_MANUAL = "Manual";
}