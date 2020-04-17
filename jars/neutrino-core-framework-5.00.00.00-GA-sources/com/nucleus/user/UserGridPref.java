package com.nucleus.user;

import com.nucleus.core.genericparameter.entity.GenericParameter;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.Entity;

@Entity
@DynamicInsert
@DynamicUpdate
public class UserGridPref extends GenericParameter{

    public static final String APPLICATION_GRID         = "APPLICATION GRID";
    public static final String LEAD_GRID                = "LEADS";
    public static final String CREDIT_APPROVAL          = "CREDIT APPROVAL";

}