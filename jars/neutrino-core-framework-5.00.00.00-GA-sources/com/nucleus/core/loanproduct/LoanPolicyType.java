package com.nucleus.core.loanproduct;

import javax.persistence.Entity;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.nucleus.core.genericparameter.entity.GenericParameter;

/**
 * 
 * @author Nucleus Software Exports Limited
 */

@Entity
@DynamicUpdate
@DynamicInsert
public class LoanPolicyType extends GenericParameter {

    private static final long  serialVersionUID                                          = 1L;

    public static final String LOAN_POLICY_TYPE_CHARGE_POLICY                            = "ChargePolicy";
    public static final String LOAN_POLICY_TYPE_ELIGIBILITY_POLICY                       = "EligibilityPolicy";
    public static final String LOAN_POLICY_TYPE_SCORING_POLICY                           = "ScoringPolicy";
    public static final String LOAN_POLICY_TYPE_ACCOUNTING_POLICY                        = "AccountingPolicy";
    public static final String LOAN_POLICY_TYPE_CREDIT_APPROVAL_POLICY                   = "CreditApprovalPolicy";
    public static final String LOAN_POLICY_TYPE_DEVIATION_POLICY                         = "DeviationPolicy";
    public static final String LOAN_POLICY_TYPE_FI_AUTO_INITIATION_POLICY                = "FIAutoInitiationPolicy";
    public static final String LOAN_POLICY_TYPE_REPAYMENT_POLICY                         = "RepaymentPolicy";
    public static final String LOAN_POLICY_TYPE_AMOUNT_COMPUTATION_POLICY                = "AmountComputationPolicy";
    public static final String LOAN_POLICY_TYPE_PAYOUT_COMPUTATION_POLICY                = "VapComputationPolicy";
    public static final String LOAN_POLICY_TYPE_VAP_POLICY                               = "VapPolicy";
    public static final String LOAN_POLICY_TYPE_MULTIPLE_ASSET_SINGLE_APPLICATION_POLICY = "MASingleApplicationPolicy";
    public static final String LOAN_POLICY_TYPE_IMD_REFUND_POLICY                        = "IMDRefundPolicy";
    public static final String LOAN_POLICY_TYPE_DEAL_TRANCHE_POLICY                        = "DealTranchePolicy";

}
