package com.nucleus.demographics;

import javax.persistence.Entity;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.nucleus.core.genericparameter.entity.GenericParameter;

@Entity
@DynamicUpdate
@DynamicInsert
public class ReferenceRelationshipType extends GenericParameter {
    private static final long serialVersionUID = 6737776579385415892L;

}
