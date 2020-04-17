package com.nucleus.reason;

import com.nucleus.core.genericparameter.entity.GenericParameter;

import javax.persistence.Entity;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

/**
 * Created by rohit.chhabra on 3/14/2018.
 */
@Entity
@DynamicUpdate
@DynamicInsert
public class InactiveReason  extends GenericParameter {

    private static final long serialVersionUID = 1L;

    public static final String REASON_INACTIVTE = "Inactivate User";
}