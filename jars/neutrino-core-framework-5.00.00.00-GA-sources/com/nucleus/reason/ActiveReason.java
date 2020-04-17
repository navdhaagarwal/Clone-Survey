package com.nucleus.reason;

import com.nucleus.core.genericparameter.entity.GenericParameter;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.Entity;

/**
 * Created by shivanshi.garg on 1/4/2019.
 */
@Entity
@DynamicUpdate
@DynamicInsert
public class ActiveReason extends GenericParameter {

    private static final long serialVersionUID = 1L;

    public static final String REASON_ACTIVE1 = "Employee Rejoining";
    public static final String REASON_ACTIVE2 = "Joining after sick leave";
}
