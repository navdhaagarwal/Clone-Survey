package com.nucleus.user;

import javax.persistence.Entity;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.nucleus.core.genericparameter.entity.GenericParameter;
/**
 * User security questions 
 */
@Entity
@DynamicUpdate
@DynamicInsert
public class UserSecurityQuestion extends GenericParameter {
	
	private static final long serialVersionUID = 4720315033672581828L;
}
