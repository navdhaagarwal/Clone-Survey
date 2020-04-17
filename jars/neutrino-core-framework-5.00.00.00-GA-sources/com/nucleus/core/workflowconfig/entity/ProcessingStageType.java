package com.nucleus.core.workflowconfig.entity;

import javax.persistence.Entity;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.nucleus.core.genericparameter.entity.GenericParameter;

@Entity
@DynamicUpdate
@DynamicInsert
public class ProcessingStageType extends GenericParameter {

    private static final long  serialVersionUID              = 7807322237539208428L;
    public static final String CLDE                          = "CLDE";
    public static final String QDE                           = "QDE";
    public static final String DDE                           = "DDE";
    public static final String DEDUPE                        = "DEDUPE";
    public static final String FI                            = "FI";
    public static final String POLICY_EXECUTION              = "POLICY_EXECUTION";
    public static final String UNDERWRITING                  = "UNDERWRITING";
    public static final String DISBURSAL                     = "DISBURSAL";
    public static final String REJECTION                     = "REJECTION";
    public static final String LEAD                          = "LEAD";
    public static final String PROPOSAL                      = "PROPOSAL";
    public static final String CONSUMER_DURABLE              = "CONSUMER_DURABLE";
    public static final String POLICY_DECISION               = "POLICY_DECISION";
    public static final String BUREAU                        = "BUREAU";
    public static final String CREDIT_BUREAU                 = "CREDIT_BUREAU";
    public static final String POST_APPROVAL                 = "POST_APPROVAL";
    public static final String POLICY_CHECK                  = "POLICY_CHECK";
    public static final String CANCELLATION                  = "CANCELLATION";
    public static final String ELIGIBILITY_CHECK             = "ELIGIBILITY_CHECK";
    public static final String VERIFICATION_SELECTION        = "VERIFICATION_SELECTION";
    public static final String OPERATIONS                    = "OPERATIONS";
    public static final String OPERATIONS_CHECK              = "OPERATIONS_CHECK";
    public static final String TELV                          = "TELV";
    public static final String RESV                          = "RESV";
    public static final String BUSV                          = "BUSV";
    public static final String OFFV                          = "OFFV";
    public static final String SCORING                       = "SCORING";
    public static final String CCDE                          = "CCDE";
    public static final String REJECTED                      = "REJECTED";
    public static final String CREDIT_CARD_NUMBER_GENERATION = "CREDIT_CARD_NUMBER_GENERATION";
    public static final String KYC                           = "KYC";
    public static final String ADE                           = "ADE";
    public static final String RECOMMANDATION                = "RECOMMENDATION";
    public static final String DISBURSAL_AUTHOR              = "DISBURSAL_AUTHOR";
    public static final String IPD                           = "IPD";
    public static final String FIC                           = "FIC";
    public static final String FIV                           = "FIV";
    public static final String FII                           = "FII";
    public static final String PROPERTY_DETAIL               = "PROPERTY_DETAILS";
    public static final String QUALITY_CHECK                 = "QUALITY_CHECK";
    public static final String SALES_QUEUE                   = "SALES_QUEUE";
    public static final String LOGIN_ACCEPTANCE              = "LOGIN_ACCEPTANCE";
    public static final String CREDIT_APPROVAL               = "CREDIT_APPROVAL";
    public static final String RECONSIDERATION               = "RECONSIDERATION";
    public static final String SEND_TO_OPTS                  = "SEND_TO_OPTS";
    public static final String MAKER_DATA_ENTRY              = "MAKER_DATA_ENTRY";
    public static final String CHECKER_DATA_ENTRY            = "CHECKER_DATA_ENTRY";
    public static final String LEAD_DETAILS                  = "LEAD_DETAILS";
    public static final String PRIME                         = "PRIME";
    public static final String RCU_INITIATION                = "RCU_INITIATION";
    public static final String SAMPLING                      = "SAMPLING";
    public static final String DISBURSAL_DECIDER             = "DISBURSAL_DECIDER";
    public static final String RCU                           = "RCU";
    public static final String VALUATION                     = "VALUATION";
    public static final String ICD                           = "ICD";
    public static final String MAKER1                        = "MAKER1";
    public static final String MAKER2                        = "MAKER2";
    public static final String CHECKER                       = "CHECKER";
    public static final String APPLICATION_CAPTURE           = "APPLICATION_CAPTURE";
    public static final String MISSING_INFORMATION           = "MISSING_INFORMATION";
    public static final String CARD_MANAGEMENT_SYSTEM        = "CARD_MANAGEMENT_SYSTEM";
    public static final String CREDIT_BUREAU_REFERRAL        = "CREDIT_BUREAU_REFERRAL";
    public static final String CCDE_DOCUMENTATION            = "CCDE_DOCUMENTATION";
    public static final String CCO_MAKER                     = "CCO_MAKER";
    public static final String RCU_REFERRAL 				 = "RCU_REFERRAL";
    public static final String CURING 				 		 = "CURING";
    
    /**
     * FII_IN_PROGRESS in a temporary stage label and is not a workflow stage. This label is used to avoid showing bulkFI initiated application in the application grid.
     * This is to prevent re-initiation of the FII stage application whose FI is already in progress under FIVTaskAllocationMngmt
     */
    public static final String FII_IN_PROGRESS               = "FII_IN_PROGRESS";
    public static final String TRANCHE_INITIATION            = "TRANCHE_INITIATION";
	public static final String ACCOUNT_UPDATION              = "ACCOUNT_UPDATION";
	public static final String PSV                           = "PSV";
	public static final String DCC_SCHEDULING                = "DCC_SCHEDULING";
	public static final String DCC_EXECUTION                 = "DCC_EXECUTION";
	public static final String GOLD_VALUATION                 = "GOLD_VALUATION";
	public static final String TRANCHE_APPROVAL              = "TRANCHE_APPROVAL";

	public static final String DDE_QC                        = "DDE_QC";
	public static final String SECURITY_LODGEMENT              = "SECURITY_LODGEMENT";
	public static final String SECURITY_LODGEMENT_QC              = "SECURITY_LODGEMENT_QC";
	public static final String AUCTION_AGENCY_ALLOCATION              = "AUCTION_AGENCY_ALLOCATION";
	public static final String SECURITY_RELODGEMENT              = "SECURITY_RELODGEMENT";
	public static final String SECURITY_RELODGEMENT_QC              = "SECURITY_RELODGEMENT_QC";
	public static final String SECURITY_WITHDRAWAL              = "SECURITY_WITHDRAWAL";
	public static final String SECURITY_SWAP              = "SECURITY_SWAP";
	public static final String SECURITY_WITHDRAWAL_QC              = "SECURITY_WITHDRAWAL_QC";
	public static final String SECURITY_CLOSURE              = "SECURITY_CLOSURE";
	public static final String SECURITY_CLOSURE_QC              = "SECURITY_CLOSURE_QC";
	public static final String ACCOUNT_MODIFICATION              = "ACCOUNT_MODIFICATION";
	public static final String ACCOUNT_MODIFICATION_QC              = "ACCOUNT_MODIFICATION_QC";
	public static final String CERTIFICATION_RECTIFICATION              = "CERTIFICATION_RECTIFICATION";
	public static final String CERTIFICATION_RECTIFICATION_QC              = "CERTIFICATION_RECTIFICATION_QC";

}