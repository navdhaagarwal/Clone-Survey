package com.nucleus.adhoc;

import javax.persistence.Entity;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.nucleus.core.genericparameter.entity.GenericParameter;

/**
 * 
 * @author Nucleus Software Exports Limited
 * class to hold values for Adhoc Task type
 */

@Entity
@DynamicUpdate
@DynamicInsert
public class AdhocTaskType extends GenericParameter {

    private static final long serialVersionUID   = 7807376737539208428L;
    public static final int   MASTER_MAINTENANCE = 0;
    public static final int   RULE               = 1;
    public static final int   PRODUCT            = 2;
    public static final int   CREDIT_POLICY      = 3;
    public static final int   REPAYMENT_POLICY   = 4;
    public static final int   DEVIATION_POLICY   = 5;

}
