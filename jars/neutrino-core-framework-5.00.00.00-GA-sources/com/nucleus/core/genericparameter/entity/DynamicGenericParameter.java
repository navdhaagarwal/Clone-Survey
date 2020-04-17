package com.nucleus.core.genericparameter.entity;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.Cacheable;
import javax.persistence.Entity;

@Entity
@Cacheable
@DynamicUpdate
@DynamicInsert
public class DynamicGenericParameter extends GenericParameter {

}
