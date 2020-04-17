package com.nucleus.rules.model.ruleMatrixMaster;

import javax.persistence.Entity;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.nucleus.core.genericparameter.entity.GenericParameter;

@Entity
@DynamicUpdate
@DynamicInsert
public class RuleMatrixMasterType extends GenericParameter {

    private static final long serialVersionUID = 1L;

    public static final String RULE_MATRIX_MASTER_TYPE_SANCTION_LIMIT = "SanctionLimit";
    public static final String RULE_MATRIX_MASTER_TYPE_RATE_LIMIT     = "RateLimit";
    public static final String RULE_MATRIX_MASTER_TYPE_CHARGE_LIMIT     = "ChargeLimit";
    public static final String RULE_MATRIX_MASTER_TYPE_LIVING_EXPENSE =  "LivingExpense";


}
