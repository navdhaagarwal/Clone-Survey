package com.nucleus.adhoc;

import javax.persistence.Entity;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.nucleus.core.genericparameter.entity.GenericParameter;

/**
 * 
 * @author Nucleus Software Exports Limited
 * class to hold values for Adhoc sub Task type
 */

@Entity
@DynamicUpdate
@DynamicInsert
public class AdhocTaskSubType extends GenericParameter {

    private static final long serialVersionUID = 7807376737539208428L;

    public static final int   CREATE           = 0;
    public static final int   EDIT             = 1;
    public static final int   DELETE           = 2;

}
