package com.nucleus.finnone.pro.general.domainobject;

import javax.persistence.Entity;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.nucleus.core.genericparameter.entity.GenericParameter;

@Entity 
@DynamicInsert 
@DynamicUpdate
public class MonthsOfYearType extends GenericParameter {
	private static final long serialVersionUID = 1L;

	public static final String January="01";
	public static final String February="02";
	public static final String March="03";
	public static final String April="04";
	public static final String May="05";
	public static final String June="06";
	public static final String July="07";
	public static final String August="08";
	public static final String September="09";
	public static final String October="10";
	public static final String November="11";
	public static final String December="12";
}