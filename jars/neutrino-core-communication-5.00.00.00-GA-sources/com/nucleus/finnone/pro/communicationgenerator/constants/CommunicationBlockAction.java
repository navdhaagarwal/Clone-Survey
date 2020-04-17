package com.nucleus.finnone.pro.communicationgenerator.constants;
import javax.persistence.Entity;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.nucleus.core.genericparameter.entity.GenericParameter;

@Entity
@DynamicUpdate
@DynamicInsert
public class CommunicationBlockAction extends GenericParameter{

	private static final long serialVersionUID = 1L;
	public static final String BLOCK = "B";
	public static final String UNBLOCK = "U";

}