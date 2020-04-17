package com.nucleus.core.loanproduct;

import javax.persistence.Entity;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.nucleus.core.genericparameter.entity.GenericParameter;

@Entity
@DynamicUpdate
@DynamicInsert
public class ProductCategory extends GenericParameter {

    private static final long  serialVersionUID   = 1L;

    public static final String MOVABLE_ASSET_LOAN = "MAL";
    public static final String PERSONAL_FINANCE   = "PF";
    public static final String CORPORATE_LOAN     = "CL";
    public static final String MORTGAGE           = "MORTGAGE";
    public static final String MICRO_FINANCE      = "MF";
    public static final String LEASE              = "LEASE";
    public static final String EDUCATION_LOAN     = "EDU";
    public static final String AGRICULTURE_LOAN   = "AGR";
    public static final String OMNI_LOAN          = "OMNI";
    public static final String GOLD_LOAN          = "GL";

}
