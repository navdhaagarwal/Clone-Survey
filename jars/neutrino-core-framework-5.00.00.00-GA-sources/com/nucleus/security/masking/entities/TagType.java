package com.nucleus.security.masking.entities;

import javax.persistence.Entity;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.nucleus.core.genericparameter.entity.GenericParameter;

@Entity
@DynamicUpdate
@DynamicInsert
public class TagType extends GenericParameter {

	private static final long serialVersionUID = 1L;
	public static final String TAG_TYPE_FIELD_LABEL = "fieldLable";
	public static final String TAG_TYPE_INPUT = "input";
	public static final String TAG_TYPE_MULTI_SELECT = "multiselect";
	public static final String TAG_TYPE_SELECT = "select";
	public static final String TAG_TYPE_NO_TAG = "no_tag";
}
