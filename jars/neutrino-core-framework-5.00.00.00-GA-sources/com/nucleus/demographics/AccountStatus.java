package com.nucleus.demographics;

import javax.persistence.Entity;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.nucleus.core.genericparameter.entity.GenericParameter;

@Entity
@DynamicUpdate
@DynamicInsert
public class AccountStatus extends GenericParameter{
	
	  private static final long serialVersionUID = 1L;

	public static final String ACCOUNT_STATUS_ACTIVE = "Active";

	public static final String ACCOUNT_STATUS_DORMANT = "Dormant";

}
