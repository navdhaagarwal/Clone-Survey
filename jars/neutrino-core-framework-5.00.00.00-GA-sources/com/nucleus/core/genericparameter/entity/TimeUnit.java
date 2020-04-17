package com.nucleus.core.genericparameter.entity;

import javax.persistence.Entity;
import javax.persistence.Transient;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.nucleus.core.genericparameter.entity.GenericParameter;

@Entity
@DynamicUpdate
@DynamicInsert
public class TimeUnit extends GenericParameter {

    @Transient
    private static final long serialVersionUID = 1L;

}
