package com.nucleus.core.additionalEmployerDetails.entity;

import javax.persistence.Entity;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.nucleus.core.genericparameter.entity.GenericParameter;

@Entity
@DynamicUpdate
@DynamicInsert
public class OwnershipStatus extends GenericParameter {

    private static final long serialVersionUID = -1717574467576728627L;

}
