package com.nucleus.document.core.entity;

import javax.persistence.Entity;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.nucleus.core.genericparameter.entity.GenericParameter;

@Entity
@DynamicUpdate
@DynamicInsert
public class DocumentMappingCode extends GenericParameter {

	/**
	 * 
	 */

	private static final long serialVersionUID = 1L;

}
