package com.nucleus.finnone.pro.general.domainobject;

import javax.persistence.Entity;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.nucleus.core.genericparameter.entity.GenericParameter;

@Entity 
@DynamicInsert 
@DynamicUpdate
public class DaysOfWeekType extends GenericParameter {

	private static final long serialVersionUID = -2550115953409977672L;
	public static final String Sunday = "1";
	public static final String Monday = "2";
	public static final String Tuesday = "3";
	public static final String Wednesday = "4";
	public static final String Thursday = "5";
	public static final String Friday = "6";
	public static final String Saturday = "7";

}
