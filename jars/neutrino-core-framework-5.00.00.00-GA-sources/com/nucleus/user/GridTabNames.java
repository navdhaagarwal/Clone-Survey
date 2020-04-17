package com.nucleus.user;

import com.nucleus.core.genericparameter.entity.GenericParameter;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.Entity;

@Entity
@DynamicInsert
@DynamicUpdate
public class GridTabNames extends GenericParameter {

    public static final String ASSIGNED_TAB         = "Assigned";
    public static final String POOL_TAB             = "Pool";
    public static final String HOLD_TAB             = "Hold";
    public static final String ARCHIVE_TAB          = "Archive";
    public static final String REJECT_TAB           = "Reject";


}