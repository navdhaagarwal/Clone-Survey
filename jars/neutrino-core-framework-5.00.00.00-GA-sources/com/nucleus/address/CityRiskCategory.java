package com.nucleus.address;

import com.nucleus.core.genericparameter.entity.GenericParameter;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.Cacheable;
import javax.persistence.Entity;

/**
 * 
 * @author Nucleus Software Exports Limited
 */
@Entity
@Cacheable
@DynamicUpdate
@DynamicInsert
public class CityRiskCategory extends GenericParameter {

	private static final String INSIGNIFICANT = "Insignificant";
	private static final String LOW = "Low";
	private static final String MODERATE = "Moderate";
	private static final String HIGH = "High";
	private static final String VERY_HIGH = "Very High";
	private static final String RESTRICTED = "Restricted";
	private static final String OFF_CREDIT = "Off-Credit";

}
