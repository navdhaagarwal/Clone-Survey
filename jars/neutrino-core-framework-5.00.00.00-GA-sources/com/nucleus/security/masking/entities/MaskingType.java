package com.nucleus.security.masking.entities;

import javax.persistence.Entity;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.nucleus.core.genericparameter.entity.GenericParameter;

@Entity
@DynamicUpdate
@DynamicInsert
public class MaskingType extends GenericParameter {

	private static final long serialVersionUID = 1L;
	public static final String MASKING_TYPE_PATTERN= "pattern";
	public static final String MASKING_TYPE_INDEXED = "indexed";
	public static final String MASKING_TYPE_EMAIL = "email";

}
