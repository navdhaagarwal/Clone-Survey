package com.nucleus.finnone.pro.communicationgenerator.constants;

import javax.persistence.Entity;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.nucleus.core.genericparameter.entity.GenericParameter;

@Entity
@DynamicUpdate
@DynamicInsert
public class CustomerInternalFlag extends GenericParameter {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4923144437466155365L;

}
