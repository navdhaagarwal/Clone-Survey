package com.nucleus.finnone.pro.cache.entity;

import javax.persistence.Entity;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.nucleus.core.genericparameter.entity.GenericParameter;

@Entity
@DynamicUpdate
@DynamicInsert
public class CacheUpdateType extends GenericParameter {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public static final String IMPLICIT = "IMPLICIT";
	
	public static final String EXPLICIT = "EXPLICIT";

}
