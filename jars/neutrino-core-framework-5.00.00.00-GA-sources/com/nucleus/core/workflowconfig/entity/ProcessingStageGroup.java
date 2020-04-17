package com.nucleus.core.workflowconfig.entity;

import javax.persistence.Entity;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.nucleus.core.genericparameter.entity.GenericParameter;

@Entity
@DynamicUpdate
@DynamicInsert
public class ProcessingStageGroup extends GenericParameter {

    private static final long serialVersionUID = 1L;
  
    public static final String PROCESSING_STAGE_CHECKER = "CHECKER";
    public static final String PROCESSING_STAGE_MAKER1  = "MAKER1";
    public static final String PROCESSING_STAGE_MAKER2  = "MAKER2";
    public static final String PROCESSING_STAGE_MAKER   = "MAKER";
    public static final String PROCESSING_STAGE_DEDUPE   = "DEDUPE";
	public static final String PROCESSING_STAGE_TA      = "TRANCHE_APPROVAL";


}
