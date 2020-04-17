package com.nucleus.finnone.pro.communicationgenerator.util;

import javax.persistence.Entity;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.nucleus.core.genericparameter.entity.GenericParameter;

@Entity
@DynamicUpdate
@DynamicInsert
public class ServiceSelectionCriteria extends GenericParameter {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public static final String TEMPLATE_SELECTION = "TEMPLATE_SELECTION";
	public static final String COMMUNICATION_GENERATION = "COMMUNICATION_GENERATION";
	public static final String COMMUNICATION_BLOCK_STATUS_CHECK = "COMMUNICATION_BLOCK_STATUS_CHECK";
	public static final String ADHOC_BULK_COMMUNICATION = "ADHOC_BULK_COMMUNICATION";
}
