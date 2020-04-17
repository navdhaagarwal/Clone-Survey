package com.nucleus.eventInterfaceCode;

import javax.persistence.Entity;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.nucleus.core.genericparameter.entity.GenericParameter;

@Entity
@DynamicUpdate
@DynamicInsert
public class EventInterfaceCode extends GenericParameter {

	private static final long serialVersionUID = -363348372169434134L;

}
