/**
 * Copyright Nucleus Software India Pvt Ltd. All rights reserved.
 */
package com.nucleus.core.event;

import javax.persistence.Entity;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.nucleus.core.genericparameter.entity.GenericParameter;

/**
 * @author Nucleus Software Exports Limited
 * Constants for Parameter Data type
 */

@Entity
@DynamicUpdate
@DynamicInsert
public class EventCode extends GenericParameter {

    private static final long  serialVersionUID                             = 1L;

    public static final String APPLICATION_ASSIGNMENT_EVENT                 = "APPLICATION_ASSIGNMENT_EVENT";
    public static final String LEAD_ASSIGNMENT_EVENT                        = "LEAD_ASSIGNMENT_EVENT";
    public static final String QDE_COMPLETION_EVENT                         = "QDE_COMPLETION_EVENT";
    public static final String SAVE_CUSTOMER_EVENT                          = "SAVE_CUSTOMER_EVENT";
    public static final String MASTER_APPROVE_STRATEGY                      = "MASTER_APPROVE_STRATEGY";
    public static final String MASTER_SENDBACK_STRATEGY                     = "MASTER_SENDBACK_STRATEGY";

    /*Event Codes For User Management*/

    public static final String New_User_Creation                            = "New_User_Creation";
    public static final String User_Editing                                 = "User_Editing";
    public static final String Workflow_Editing                             = "Workflow_Editing";
    public static final String New_Branch_Creation                          = "New_Branch_Creation";
    public static final String Edit_Branch                                  = "Edit_Branch";
    public static final String User_Login                                   = "User_Login";
    public static final String User_Logout                                  = "User_Logout";

    /* Event Codes For Lead */

    public static final String Internet_Channel_Lead                        = "Internet_Channel_Lead";
    public static final String Lead_Assignment_For_Internet_Channel         = "Lead_Assignment_For_Internet_Channel";
    public static final String Quick_Lead_Creation                          = "Quick_Lead_Creation";
    public static final String Lead_Edit_By_Bank_Employee                   = "Lead_Edit_By_Bank_Employee";
    public static final String Lead_Edit_By_End_Applicant                   = "Lead_Edit_By_End_Applicant";
    public static final String Lead_Customer_Communication_Event            = "Lead_Customer_Communication_Event";
    public static final String Lead_Workflow_Change_Event                   = "Lead_Workflow_Change_Event";
    public static final String Lead_Status                                  = "Lead_Status";
    public static final String Lead_Creation_Upload_Channel                 = "Lead_Creation_Upload_Channel";
    public static final String Lead_Assignment                              = "Lead_Assignment";
    public static final String Lead_Customer_Communication_Sent             = "Lead_Customer_Communication_Sent";
    public static final String LEAD_TO_QC                                   = "Lead_To_QC";
    public static final String QC_TO_APPLICATION                            = "QC_To_Application";
    public static final String Lead_Hold_Event                              = "Lead_Hold_Event";
    public static final String Lead_Reject_Event                            = "Lead_Reject_Event";
    public static final String Lead_Unhold_Event                            = "Lead_Unhold_Event";
    public static final String Hold_Lead_Reject_Event                       = "Hold_Lead_Reject_Event";
    public static final String Lead_Assign_To_User                          = "Lead_Assign_To_User";
    public static final String Lead_Assign_To_Pool                          = "Lead_Assign_To_Pool";
    public static final String Hold_Lead_Assign_To_Pool                     = "Hold_Lead_Assign_To_Pool";
    public static final String Lead_To_Application                          = "Lead_To_Application";

    /*Event Codes For Loan Application*/
    public static final String APLICATION_DETAILS_SOURCING_CHANNEL         ="APPLICATION_DETAILS_SOURCING_CHANNEL";
    public static final String Loan_Application_Creation                    = "Loan_Application_Creation";
    public static final String Loan_Application_DeDupe                      = "Loan_Application_DeDupe";
    public static final String Loan_Application_Workflow_Change_Event       = "Loan_Application_Workflow_Change_Event";
    public static final String Application_Customer_Communication_Sent      = "Application_Customer_Communication_Sent";
    public static final String Email_Mandatory                              = "Email_Mandatory";
    public static final String PT_EQ_MHL                                    = "PT_EQ_MHL";

    /* Event Codes For Product */
    public static final String Create_Update_Product                        = "Create_Update_Product";
    public static final String Delete_Product_Event                         = "Delete_Product_Event";

    /* Event Codes For Scheme */
    public static final String Create_Update_Scheme                         = "Create_Update_Scheme";
    public static final String Delete_Scheme_Event                          = "Delete_Scheme_Event";

    /* Event Codes For Credit Policy */
    public static final String Create_Update_Credit_Policy                  = "Create_Update_Credit_Policy";
    public static final String Create_Update_Vap_Payout_Policy                  = "Create_Update_Vap_Payout_Policy";
    public static final String Delete_Credit_Policy_Event                   = "Delete_Credit_Policy _Event";

    
    /* Event Codes For Eligibility Policy */
    public static final String Create_Update_Eligibility_Policy             = "Create_Update_Eligibility_Policy";
    public static final String Delete_Eligibility_Policy_Event              = "Delete_Eligibility_Policy_Event";
    public static final String INVOKE_ELIGIBILTY_RULES                      = "INVOKE_ELIGIBILTY_RULES";
    public static final String ELIGIBILITY_CHECK_INTERNET_CHANNEL           = "ELIGIBILITY_CHECK_INTERNET_CHANNEL";
    public static final String Kyc_Completion                               = "kyc_completion";
    public static final String SHOW_OFFERS_CREDIT_CARD                      = "SHOW_OFFERS_CREDIT_CARD";
    public static final String SHOW_OFFERS_CREDIT_CARD_LEAD                 = "SHOW_OFFERS_CREDIT_CARD_LEAD";
    public static final String SHOW_OFFERS                                  = "SHOW_OFFERS";
    public static final String CALCULATE_GMP_CREDITCARD                     = "CALCULATE_GMP_CREDITCARD";
    public static final String CALCULATE_GMP_UNDERWRITER                    = "CALCULATE_GMP_UNDERWRITER";
    public static final String verification_selection_routing               = "verification_selection_routing";
    public static final String EVENT_EXECUTION_RESULT_TRANSACTION_ID        = "EVENT_EXECUTION_RESULT_TRANSACTION_ID";
    public static final String BUSINESS_PARTNER_CHECK_EVENT_CODE            = "BUSINESS_PARTNER_CHECK_EVENT_CODE";
    public static final String POLICY_CHECK_LOAN_SCHEME                     = "POLICY_CHECK_LOAN_SCHEME";
    public static final String Quartz_Scheduler_Mail_Event                  = "QSchMNC";
    public static final String Quartz_Scheduler_SMS_Event                   = "QSchSNC";
    public static final String DM_TEMPLATE_EVENT                            = "DM_TEMPLATE_EVENT";
    public static final String Eligibility_Limit                            = "Eligibility_Limit";
    public static final String LRD_Validation_Event                         = "LRD_Validation_Event";
    public static final String Rental_Validation_Event                      = "Rental_Validation_Event";
    public static final String CALCULATE_IIR_CAM_MHF                        = "CALCULATE_IIR_CAM_MHF";

    public static final String CV_RECONSIDERATION_COMPLETION                = "CV_RECONSIDERATION_COMPLETION";
    public static final String CV_RECONSIDERATION_EXIT                      = "CV_RECONSIDERATION_EXIT";
    /*   public static final String  Product_Modification=" Product_Modification";
       public static final String  Product_Workflow_Change_Event="Product_Workflow_Change_Event";
       public static final String  Scheme_Workflow_Change_Event="Scheme_Workflow_Change_Event";
       public static final String  Scheme_Modification="Scheme_Modification";
       */
    public static final String RUN_EVENT_FOR_APPROVE_SEND_TO_PRIME          = "RUN_EVENT_FOR_APPROVE_SEND_TO_PRIME";


    public static final String Eligibility_Limit_PL                         = "Eligibility_Limit_PL";
    public static final String Eligibility_Limit_ML                         = "Eligibility_Limit_ML";
    public static final String Eligibility_Limit_OMNI                       = "Eligibility_Limit_OMNI";
    public static final String Eligibility_Limit_MAL                        = "Eligibility_Limit_MAL";
    public static final String Eligibility_Limit_CV                         = "Eligibility_Limit_CV";
    public static final String Eligibility_Limit_CL                         = "Eligibility_Limit_CL";
    public static final String Eligibility_Limit_CC                         = "Eligibility_Limit_CC";
    public static final String Eligibility_Limit_LAP                        = "Eligibility_Limit_LAP";
    public static final String Eligibility_Limit_MHL                        = "Eligibility_Limit_MHL";
    public static final String Eligibility_Limit_FE                         = "Eligibility_Limit_FE";
    public static final String Eligibility_Limit_EDU                        = "Eligibility_Limit_EDU";

    public static final String Deviation_CV                                 = "Deviation_CV";
    public static final String Deviation_CC                                 = "Deviation_CC";
    public static final String Deviation_PL                                 = "Deviation_PL";
    public static final String Deviation_ML                                 = "Deviation_ML";
    public static final String Deviation_OMNI                               = "Deviation_OMNI";
    public static final String Deviation_MAL                                = "Deviation_MAL";
    public static final String Deviation_LAP                                = "Deviation_LAP";
    public static final String Deviation_MHL                                = "Deviation_MHL";
    public static final String Deviation_CL                                 = "Deviation_CL";
    public static final String Deviation_FE                                 = "Deviation_FE";
    public static final String Deviation_EDU                                = "Deviation_EDU";

    public static final String Disbursal_Initiation                         = "Disbursal_Initiation";

    public static final String Disbursal_Details_Save                       = "Disbursal_Details_Save";

    public static final String Payment_Details_Save                         = "Payment_Details_Save";

    /* ICICI Specific
     * Event Code for FOIR calculation at Recommendation Stage*/
    public static final String CC_RECOMMENDATION_TOTAL_FOIR_CALCULATION     = "CC_RECOMMENDATION_TOTAL_FOIR_CALCULATION";
    public static final String CC_RECOMMENDATION_UNSECURED_FOIR_CALCULATION = "CC_RECOMMENDATION_UNSECURED_FOIR_CALCULATION";

    public static final String SAVE_APPLICATION_ASSET_TAB                   = "SAVE_APPLICATION_ASSET_TAB";

    public static final String SAVE_APPLICATION_SOURCING_TAB                = "SAVE_APPLICATION_SOURCING_TAB";

    
    public static final String RUN_EVENT_FOR_REJECT                         = "RUN_EVENT_FOR_REJECT";

    /*
     * APLUS SPECIFIC
     */
    public static final String APLUS_MAKER_VALIDATION                       = "APLUS_MAKER_VALIDATION";
    
    public static final String CALCULATE_CREDIT_APPROVAL_RATIOS             = "CALCULATE_CREDIT_APPROVAL_RATIOS";

    public static final String SCORE_CARD_RATING                            = "SCORE_CARD_RATING";

    public static final String APPLICATION_ASSIGNMENT_EVENT_CC              = "APPLICATION_ASSIGNMENT_EVENT_CC";
    public static final String APPLICATION_ASSIGNMENT_EVENT_CV              = "APPLICATION_ASSIGNMENT_EVENT_CV";
    public static final String APPLICATION_ASSIGNMENT_EVENT_PL              = "APPLICATION_ASSIGNMENT_EVENT_PL";
    public static final String APPLICATION_ASSIGNMENT_EVENT_ML              = "APPLICATION_ASSIGNMENT_EVENT_ML";
    public static final String APPLICATION_ASSIGNMENT_EVENT_OMNI              = "APPLICATION_ASSIGNMENT_EVENT_OMNI";
    public static final String APPLICATION_ASSIGNMENT_EVENT_MAL             = "APPLICATION_ASSIGNMENT_EVENT_MAL";
    public static final String APPLICATION_ASSIGNMENT_EVENT_LAP             = "APPLICATION_ASSIGNMENT_EVENT_LAP";
    public static final String APPLICATION_ASSIGNMENT_EVENT_MHL             = "APPLICATION_ASSIGNMENT_EVENT_MHL";
    public static final String APPLICATION_ASSIGNMENT_EVENT_CL              = "APPLICATION_ASSIGNMENT_EVENT_CL";
    public static final String APPLICATION_ASSIGNMENT_EVENT_FE              = "APPLICATION_ASSIGNMENT_EVENT_fE";
    public static final String APPLICATION_ASSIGNMENT_EVENT_EDU             = "APPLICATION_ASSIGNMENT_EVENT_EDU";


    public static final String LEAD_ASSIGNMENT_EVENT_CC                     = "LEAD_ASSIGNMENT_EVENT_CC";
    public static final String LEAD_ASSIGNMENT_EVENT_CV                     = "LEAD_ASSIGNMENT_EVENT_CV";
    public static final String LEAD_ASSIGNMENT_EVENT_PL                     = "LEAD_ASSIGNMENT_EVENT_PL";
    public static final String LEAD_ASSIGNMENT_EVENT_ML                     = "LEAD_ASSIGNMENT_EVENT_ML";
    public static final String LEAD_ASSIGNMENT_EVENT_OMNI                   = "LEAD_ASSIGNMENT_EVENT_OMNI";
    public static final String LEAD_ASSIGNMENT_EVENT_MAL                    = "LEAD_ASSIGNMENT_EVENT_MAL";
    public static final String LEAD_ASSIGNMENT_EVENT_LAP                    = "LEAD_ASSIGNMENT_EVENT_LAP";
    public static final String LEAD_ASSIGNMENT_EVENT_MHL                    = "LEAD_ASSIGNMENT_EVENT_MHL";
    public static final String LEAD_ASSIGNMENT_EVENT_CL                     = "LEAD_ASSIGNMENT_EVENT_CL";
    public static final String LEAD_ASSIGNMENT_EVENT_FE                     = "LEAD_ASSIGNMENT_EVENT_FE";
    public static final String LEAD_ASSIGNMENT_EVENT_EDU                    = "LEAD_ASSIGNMENT_EVENT_EDU";

    public static final String CREDIT_APPROVAL_SAVE_DECISION                = "CREDIT_APPROVAL_SAVE_DECISION";
    public static final String RECOMMENDATION_SAVE_DECISION                = "RECOMMENDATION_SAVE_DECISION";
    
   public static final String PL_FI_RCU_Rejection_AutoComplete              			 = "PL_FI_RCU_Rejection_AutoComplete";
   public static final String CC_FI_RCU_Rejection_AutoComplete                           = "CC_FI_RCU_Rejection_AutoComplete";
   public static final String ML_FI_RCU_Rejection_AutoComplete                           = "ML_FI_RCU_Rejection_AutoComplete";
   public static final String OMNI_FI_RCU_Rejection_AutoComplete                         = "OMNI_FI_RCU_Rejection_AutoComplete";
   public static final String MAL_FI_RCU_Rejection_AutoComplete                          = "MAL_FI_RCU_Rejection_AutoComplete";
   public static final String CL_FI_RCU_Rejection_AutoComplete                           = "CL_FI_RCU_Rejection_AutoComplete";
   public static final String LAP_FI_RCU_Rejection_AutoComplete                          = "LAP_FI_RCU_Rejection_AutoComplete";
   public static final String CV_FI_RCU_Rejection_AutoComplete                           = "CV_FI_RCU_Rejection_AutoComplete";
   public static final String MHL_FI_RCU_Rejection_AutoComplete                          = "MHL_FI_RCU_Rejection_AutoComplete";
   public static final String FE_FI_RCU_Rejection_AutoComplete                           = "FE_FI_RCU_Rejection_AutoComplete";
   public static final String EDU_FI_RCU_Rejection_AutoComplete                          = "EDU_FI_RCU_Rejection_AutoComplete";

   public static final String PL_FI_RCU_Cancellation_AutoComplete                           = "PL_FI_RCU_Cancellation_AutoComplete";
   public static final String CC_FI_RCU_Cancellation_AutoComplete                           = "CC_FI_RCU_Cancellation_AutoComplete";
   public static final String ML_FI_RCU_Cancellation_AutoComplete                           = "ML_FI_RCU_Cancellation_AutoComplete";
   public static final String OMNI_FI_RCU_Cancellation_AutoComplete                         = "OMNI_FI_RCU_Cancellation_AutoComplete";
   public static final String MAL_FI_RCU_Cancellation_AutoComplete                          = "MAL_FI_RCU_Cancellation_AutoComplete";
   public static final String CL_FI_RCU_Cancellation_AutoComplete                           = "CL_FI_RCU_Cancellation_AutoComplete";
   public static final String LAP_FI_RCU_Cancellation_AutoComplete                          = "LAP_FI_RCU_Cancellation_AutoComplete";
   public static final String CV_FI_RCU_Cancellation_AutoComplete                           = "CV_FI_RCU_Cancellation_AutoComplete";
   public static final String MHL_FI_RCU_Cancellation_AutoComplete                          = "MHL_FI_RCU_Cancellation_AutoComplete";
   public static final String FE_FI_RCU_Cancellation_AutoComplete                           = "FE_FI_RCU_Cancellation_AutoComplete";
   public static final String EDU_FI_RCU_Cancellation_AutoComplete                          = "EDU_FI_RCU_Cancellation_AutoComplete";
   
    public static final String REJECTION_EVENT_CC_KYC                                      = "REJECTION_EVENT_CC_KYC";
    public static final String REJECTION_EVENT_CC_DDE                  				       = "REJECTION_EVENT_CC_DDE";
    public static final String REJECTION_EVENT_CC_FII                    				   = "REJECTION_EVENT_CC_FII";
    public static final String REJECTION_EVENT_CC_LOGIN_ACCEPTANCE                         = "REJECTION_EVENT_CC_LOGIN_ACCEPTANCE";
    public static final String REJECTION_EVENT_CC_RECOMMENDATION                           = "REJECTION_EVENT_CC_RECOMMENDATION";
    public static final String REJECTION_EVENT_CC_CREDIT_APPROVAL                          = "REJECTION_EVENT_CC_CREDIT_APPROVAL";
    public static final String REJECTION_EVENT_CC_POST_APPROVAL                            = "REJECTION_EVENT_CC_POST_APPROVAL";
    public static final String REJECTION_EVENT_CC_OPERATIONS_CHECK                         = "REJECTION_EVENT_CC_OPERATIONS_CHECK";
    public static final String REJECTION_EVENT_CC_RECONSIDERATION                          = "REJECTION_EVENT_CC_RECONSIDERATION";
    public static final String REJECTION_EVENT_CC_SALES_QUEUE                              = "REJECTION_EVENT_CC_SALES_QUEUE";
    public static final String REJECTION_EVENT_CC_CCDE                                     = "REJECTION_EVENT_CC_CCDE";
    public static final String REJECTION_EVENT_CC_ICD                                      = "REJECTION_EVENT_CC_ICD";
    public static final String REJECTION_EVENT_CC_CCDE_DOCUMENTATION                       = "REJECTION_EVENT_CC_CCDE_DOCUMENTATION";
    public static final String REJECTION_EVENT_CC_CCO_MAKER                                = "REJECTION_EVENT_CC_CCO_MAKER";
    public static final String REJECTION_EVENT_CC_FRAUD_DETECTION                          = "REJECTION_EVENT_CC_FRAUD_DETECTION";
    public static final String REJECTION_EVENT_CC_POLICY_EXECUTION                         = "REJECTION_EVENT_CC_POLICY_EXECUTION"; 
    
    public static final String REJECTION_EVENT_PL_KYC                        			  = "REJECTION_EVENT_PL_KYC";
    public static final String REJECTION_EVENT_PL_DDE                         		      = "REJECTION_EVENT_PL_DDE";
    public static final String REJECTION_EVENT_PL_FII                                     = "REJECTION_EVENT_PL_FII";
    public static final String REJECTION_EVENT_PL_LOGIN_ACCEPTANCE                        = "REJECTION_EVENT_PL_LOGIN_ACCEPTANCE";
    public static final String REJECTION_EVENT_PL_RECOMMENDATION                          = "REJECTION_EVENT_PL_RECOMMENDATION";
    public static final String REJECTION_EVENT_PL_CREDIT_APPROVAL                         = "REJECTION_EVENT_PL_CREDIT_APPROVAL";
    public static final String REJECTION_EVENT_PL_POST_APPROVAL                           = "REJECTION_EVENT_PL_POST_APPROVAL";
    public static final String REJECTION_EVENT_PL_DISBURSAL                               = "REJECTION_EVENT_PL_DISBURSAL";
    public static final String REJECTION_EVENT_PL_RECONSIDERATION                         = "REJECTION_EVENT_PL_RECONSIDERATION";
    public static final String REJECTION_EVENT_PL_LEAD                                    = "REJECTION_EVENT_PL_LEAD";
    public static final String REJECTION_EVENT_PL_LEAD_DETAILS                            = "REJECTION_EVENT_PL_LEAD_DETAILS";
    public static final String REJECTION_EVENT_PL_POLICY_EXECUTION                        = "REJECTION_EVENT_PL_POLICY_EXECUTION"; 
    public static final String REJECTION_EVENT_PL_CIBIL                                   = "REJECTION_EVENT_PL_CIBIL";
    public static final String REJECTION_EVENT_PL_CONSUMER_DURABLE                        = "REJECTION_EVENT_PL_CONSUMER_DURABLE";
    public static final String REJECTION_EVENT_PL_VALUATION                               = "REJECTION_EVENT_PL_VALUATION";
    public static final String REJECTION_EVENT_PL_ICD                                     = "REJECTION_EVENT_PL_ICD";
    public static final String REJECTION_EVENT_PL_FIC                                     = "REJECTION_EVENT_PL_FIC";
    public static final String REJECTION_EVENT_PL_CREDIT_BUREAU                           = "REJECTION_EVENT_PL_CREDIT_BUREAU";
    public static final String REJECTION_EVENT_PL_SALES_QUEUE                             = "REJECTION_EVENT_PL_SALES_QUEUE";
    public static final String REJECTION_EVENT_PL_FRAUD_DETECTION                         = "REJECTION_EVENT_PL_FRAUD_DETECTION";
    public static final String REJECTION_EVENT_PL_DISBURSAL_AUTHOR                        = "REJECTION_EVENT_PL_DISBURSAL_AUTHOR";
    
    
    public static final String REJECTION_EVENT_LAP_KYC                        			   = "REJECTION_EVENT_LAP_KYC";
    public static final String REJECTION_EVENT_LAP_DDE                         			   = "REJECTION_EVENT_LAP_DDE";
    public static final String REJECTION_EVENT_LAP_FII                                     = "REJECTION_EVENT_LAP_FII";
    public static final String REJECTION_EVENT_LAP_LOGIN_ACCEPTANCE                        = "REJECTION_EVENT_LAP_LOGIN_ACCEPTANCE";
    public static final String REJECTION_EVENT_LAP_RECOMMENDATION                          = "REJECTION_EVENT_LAP_RECOMMENDATION";
    public static final String REJECTION_EVENT_LAP_CREDIT_APPROVAL                         = "REJECTION_EVENT_LAP_CREDIT_APPROVAL";
    public static final String REJECTION_EVENT_LAP_POST_APPROVAL                           = "REJECTION_EVENT_LAP_POST_APPROVAL";
    public static final String REJECTION_EVENT_LAP_DISBURSAL                               = "REJECTION_EVENT_LAP_DISBURSAL";
    public static final String REJECTION_EVENT_LAP_RECONSIDERATION                         = "REJECTION_EVENT_LAP_RECONSIDERATION";
    public static final String REJECTION_EVENT_LAP_LEAD                                    = "REJECTION_EVENT_LAP_LEAD";
    public static final String REJECTION_EVENT_LAP_LEAD_DETAILS                            = "REJECTION_EVENT_LAP_LEAD_DETAILS";
    public static final String REJECTION_EVENT_LAP_POLICY_EXECUTION                        = "REJECTION_EVENT_LAP_POLICY_EXECUTION"; 
    public static final String REJECTION_EVENT_LAP_CIBIL                                   = "REJECTION_EVENT_LAP_CIBIL";
    public static final String REJECTION_EVENT_LAP_CONSUMER_DURABLE                        = "REJECTION_EVENT_LAP_CONSUMER_DURABLE";
    public static final String REJECTION_EVENT_LAP_VALUATION                               = "REJECTION_EVENT_LAP_VALUATION";
    public static final String REJECTION_EVENT_LAP_ICD                                     = "REJECTION_EVENT_LAP_ICD";
    public static final String REJECTION_EVENT_LAP_FIC                                     = "REJECTION_EVENT_LAP_FIC";
    public static final String REJECTION_EVENT_LAP_CREDIT_BUREAU                           = "REJECTION_EVENT_LAP_CREDIT_BUREAU";
    public static final String REJECTION_EVENT_LAP_SALES_QUEUE                             = "REJECTION_EVENT_LAP_SALES_QUEUE";
    
    public static final String REJECTION_EVENT_CV_KYC                        			  = "REJECTION_EVENT_CV_KYC";
    public static final String REJECTION_EVENT_CV_DDE                         			  = "REJECTION_EVENT_CV_DDE";
    public static final String REJECTION_EVENT_CV_FII                                     = "REJECTION_EVENT_CV_FII";
    public static final String REJECTION_EVENT_CV_LOGIN_ACCEPTANCE                        = "REJECTION_EVENT_CV_LOGIN_ACCEPTANCE";
    public static final String REJECTION_EVENT_CV_RECOMMENDATION                          = "REJECTION_EVENT_CV_RECOMMENDATION";
    public static final String REJECTION_EVENT_CV_CREDIT_APPROVAL                         = "REJECTION_EVENT_CV_CREDIT_APPROVAL";
    public static final String REJECTION_EVENT_CV_POST_APPROVAL                           = "REJECTION_EVENT_CV_POST_APPROVAL";
    public static final String REJECTION_EVENT_CV_DISBURSAL                               = "REJECTION_EVENT_CV_DISBURSAL";
    public static final String REJECTION_EVENT_CV_RECONSIDERATION                         = "REJECTION_EVENT_CV_RECONSIDERATION";
    public static final String REJECTION_EVENT_CV_LEAD                                    = "REJECTION_EVENT_CV_LEAD";
    public static final String REJECTION_EVENT_CV_LEAD_DETAILS                            = "REJECTION_EVENT_CV_LEAD_DETAILS";
    public static final String REJECTION_EVENT_CV_POLICY_EXECUTION                        = "REJECTION_EVENT_CV_POLICY_EXECUTION"; 
    public static final String REJECTION_EVENT_CV_CIBIL                                   = "REJECTION_EVENT_CV_CIBIL";
    public static final String REJECTION_EVENT_CV_CONSUMER_DURABLE                        = "REJECTION_EVENT_CV_CONSUMER_DURABLE";
    public static final String REJECTION_EVENT_CV_VALUATION                               = "REJECTION_EVENT_CV_VALUATION";
    public static final String REJECTION_EVENT_CV_ICD                                     = "REJECTION_EVENT_CV_ICD";
    public static final String REJECTION_EVENT_CV_FIC                                     = "REJECTION_EVENT_CV_FIC";
    public static final String REJECTION_EVENT_CV_CREDIT_BUREAU                           = "REJECTION_EVENT_CV_CREDIT_BUREAU";
    public static final String REJECTION_EVENT_CV_SALES_QUEUE                             = "REJECTION_EVENT_CV_SALES_QUEUE";
    
    public static final String REJECTION_EVENT_ML_KYC                        			  = "REJECTION_EVENT_ML_KYC";
    public static final String REJECTION_EVENT_ML_DDE                         			  = "REJECTION_EVENT_ML_DDE";
    public static final String REJECTION_EVENT_ML_FII                                     = "REJECTION_EVENT_ML_FII";
    public static final String REJECTION_EVENT_ML_LOGIN_ACCEPTANCE                        = "REJECTION_EVENT_ML_LOGIN_ACCEPTANCE";
    public static final String REJECTION_EVENT_ML_RECOMMENDATION                          = "REJECTION_EVENT_ML_RECOMMENDATION";
    public static final String REJECTION_EVENT_ML_CREDIT_APPROVAL                         = "REJECTION_EVENT_ML_CREDIT_APPROVAL";
    public static final String REJECTION_EVENT_ML_POST_APPROVAL                           = "REJECTION_EVENT_ML_POST_APPROVAL";
    public static final String REJECTION_EVENT_ML_DISBURSAL                               = "REJECTION_EVENT_ML_DISBURSAL";
    public static final String REJECTION_EVENT_ML_RECONSIDERATION                         = "REJECTION_EVENT_ML_RECONSIDERATION";
    public static final String REJECTION_EVENT_ML_LEAD                                    = "REJECTION_EVENT_ML_LEAD";
    public static final String REJECTION_EVENT_ML_LEAD_DETAILS                            = "REJECTION_EVENT_ML_LEAD_DETAILS";
    public static final String REJECTION_EVENT_ML_POLICY_EXECUTION                        = "REJECTION_EVENT_ML_POLICY_EXECUTION"; 
    public static final String REJECTION_EVENT_ML_CIBIL                                   = "REJECTION_EVENT_ML_CIBIL";
    public static final String REJECTION_EVENT_ML_CONSUMER_DURABLE                        = "REJECTION_EVENT_ML_CONSUMER_DURABLE";
    public static final String REJECTION_EVENT_ML_VALUATION                               = "REJECTION_EVENT_ML_VALUATION";
    public static final String REJECTION_EVENT_ML_ICD                                     = "REJECTION_EVENT_ML_ICD";
    public static final String REJECTION_EVENT_ML_FIC                                     = "REJECTION_EVENT_ML_FIC";
    public static final String REJECTION_EVENT_ML_CREDIT_BUREAU                           = "REJECTION_EVENT_ML_CREDIT_BUREAU";
    public static final String REJECTION_EVENT_ML_SALES_QUEUE                             = "REJECTION_EVENT_ML_SALES_QUEUE";

    public static final String REJECTION_EVENT_OMNI_KYC                        			    = "REJECTION_EVENT_OMNI_KYC";
    public static final String REJECTION_EVENT_OMNI_DDE                         			= "REJECTION_EVENT_OMNI_DDE";
    public static final String REJECTION_EVENT_OMNI_FII                                     = "REJECTION_EVENT_OMNI_FII";
    public static final String REJECTION_EVENT_OMNI_LOGIN_ACCEPTANCE                        = "REJECTION_EVENT_OMNI_LOGIN_ACCEPTANCE";
    public static final String REJECTION_EVENT_OMNI_RECOMMENDATION                          = "REJECTION_EVENT_OMNI_RECOMMENDATION";
    public static final String REJECTION_EVENT_OMNI_CREDIT_APPROVAL                         = "REJECTION_EVENT_OMNI_CREDIT_APPROVAL";
    public static final String REJECTION_EVENT_OMNI_POST_APPROVAL                           = "REJECTION_EVENT_OMNI_POST_APPROVAL";
    public static final String REJECTION_EVENT_OMNI_DISBURSAL                               = "REJECTION_EVENT_OMNI_DISBURSAL";
    public static final String REJECTION_EVENT_OMNI_RECONSIDERATION                         = "REJECTION_EVENT_OMNI_RECONSIDERATION";
    public static final String REJECTION_EVENT_OMNI_LEAD                                    = "REJECTION_EVENT_OMNI_LEAD";
    public static final String REJECTION_EVENT_OMNI_LEAD_DETAILS                            = "REJECTION_EVENT_OMNI_LEAD_DETAILS";
    public static final String REJECTION_EVENT_OMNI_POLICY_EXECUTION                        = "REJECTION_EVENT_OMNI_POLICY_EXECUTION";
    public static final String REJECTION_EVENT_OMNI_CIBIL                                   = "REJECTION_EVENT_OMNI_CIBIL";
    public static final String REJECTION_EVENT_OMNI_CONSUMER_DURABLE                        = "REJECTION_EVENT_OMNI_CONSUMER_DURABLE";
    public static final String REJECTION_EVENT_OMNI_VALUATION                               = "REJECTION_EVENT_OMNI_VALUATION";
    public static final String REJECTION_EVENT_OMNI_ICD                                     = "REJECTION_EVENT_OMNI_ICD";
    public static final String REJECTION_EVENT_OMNI_FIC                                     = "REJECTION_EVENT_OMNI_FIC";
    public static final String REJECTION_EVENT_OMNI_CREDIT_BUREAU                           = "REJECTION_EVENT_OMNI_CREDIT_BUREAU";
    public static final String REJECTION_EVENT_OMNI_SALES_QUEUE                             = "REJECTION_EVENT_OMNI_SALES_QUEUE";


    public static final String REJECTION_EVENT_MHL_KYC                        			   = "REJECTION_EVENT_MHL_KYC";
    public static final String REJECTION_EVENT_MHL_DDE                         			   = "REJECTION_EVENT_MHL_DDE";
    public static final String REJECTION_EVENT_MHL_FII                                     = "REJECTION_EVENT_MHL_FII";
    public static final String REJECTION_EVENT_MHL_LOGIN_ACCEPTANCE                        = "REJECTION_EVENT_MHL_LOGIN_ACCEPTANCE";
    public static final String REJECTION_EVENT_MHL_RECOMMENDATION                          = "REJECTION_EVENT_MHL_RECOMMENDATION";
    public static final String REJECTION_EVENT_MHL_CREDIT_APPROVAL                         = "REJECTION_EVENT_MHL_CREDIT_APPROVAL";
    public static final String REJECTION_EVENT_MHL_POST_APPROVAL                           = "REJECTION_EVENT_MHL_POST_APPROVAL";
    public static final String REJECTION_EVENT_MHL_DISBURSAL                               = "REJECTION_EVENT_MHL_DISBURSAL";
    public static final String REJECTION_EVENT_MHL_RECONSIDERATION                         = "REJECTION_EVENT_MHL_RECONSIDERATION";
    public static final String REJECTION_EVENT_MHL_LEAD                                    = "REJECTION_EVENT_MHL_LEAD";
    public static final String REJECTION_EVENT_MHL_LEAD_DETAILS                            = "REJECTION_EVENT_MHL_LEAD_DETAILS";
    public static final String REJECTION_EVENT_MHL_POLICY_EXECUTION                        = "REJECTION_EVENT_MHL_POLICY_EXECUTION"; 
    public static final String REJECTION_EVENT_MHL_CIBIL                                   = "REJECTION_EVENT_MHL_CIBIL";
    public static final String REJECTION_EVENT_MHL_CONSUMER_DURABLE                        = "REJECTION_EVENT_MHL_CONSUMER_DURABLE";
    public static final String REJECTION_EVENT_MHL_VALUATION                               = "REJECTION_EVENT_MHL_VALUATION";
    public static final String REJECTION_EVENT_MHL_ICD                                     = "REJECTION_EVENT_MHL_ICD";
    public static final String REJECTION_EVENT_MHL_FIC                                     = "REJECTION_EVENT_MHL_FIC";
    public static final String REJECTION_EVENT_MHL_CREDIT_BUREAU                           = "REJECTION_EVENT_MHL_CREDIT_BUREAU";
    public static final String REJECTION_EVENT_MHL_SALES_QUEUE                             = "REJECTION_EVENT_MHL_SALES_QUEUE";  
    
    public static final String REJECTION_EVENT_MAL_KYC                        			   = "REJECTION_EVENT_MAL_KYC";
    public static final String REJECTION_EVENT_MAL_DDE                         			   = "REJECTION_EVENT_MAL_DDE";
    public static final String REJECTION_EVENT_MAL_FII                                     = "REJECTION_EVENT_MAL_FII";
    public static final String REJECTION_EVENT_MAL_LOGIN_ACCEPTANCE                        = "REJECTION_EVENT_MAL_LOGIN_ACCEPTANCE";
    public static final String REJECTION_EVENT_MAL_RECOMMENDATION                          = "REJECTION_EVENT_MAL_RECOMMENDATION";
    public static final String REJECTION_EVENT_MAL_CREDIT_APPROVAL                         = "REJECTION_EVENT_MAL_CREDIT_APPROVAL";
    public static final String REJECTION_EVENT_MAL_POST_APPROVAL                           = "REJECTION_EVENT_MAL_POST_APPROVAL";
    public static final String REJECTION_EVENT_MAL_DISBURSAL                               = "REJECTION_EVENT_MAL_DISBURSAL";
    public static final String REJECTION_EVENT_MAL_RECONSIDERATION                         = "REJECTION_EVENT_MAL_RECONSIDERATION";
    public static final String REJECTION_EVENT_MAL_LEAD                                    = "REJECTION_EVENT_MAL_LEAD";
    public static final String REJECTION_EVENT_MAL_LEAD_DETAILS                            = "REJECTION_EVENT_MAL_LEAD_DETAILS";
    public static final String REJECTION_EVENT_MAL_POLICY_EXECUTION                        = "REJECTION_EVENT_MAL_POLICY_EXECUTION"; 
    public static final String REJECTION_EVENT_MAL_CIBIL                                   = "REJECTION_EVENT_MAL_CIBIL";
    public static final String REJECTION_EVENT_MAL_CONSUMER_DURABLE                        = "REJECTION_EVENT_MAL_CONSUMER_DURABLE";
    public static final String REJECTION_EVENT_MAL_VALUATION                               = "REJECTION_EVENT_MAL_VALUATION";
    public static final String REJECTION_EVENT_MAL_ICD                                     = "REJECTION_EVENT_MAL_ICD";
    public static final String REJECTION_EVENT_MAL_FIC                                     = "REJECTION_EVENT_MAL_FIC";
    public static final String REJECTION_EVENT_MAL_CREDIT_BUREAU                           = "REJECTION_EVENT_MAL_CREDIT_BUREAU";
    public static final String REJECTION_EVENT_MAL_SALES_QUEUE                             = "REJECTION_EVENT_MAL_SALES_QUEUE";
    

    public static final String REJECTION_EVENT_CL_FII                                     = "REJECTION_EVENT_CL_FII";
    public static final String REJECTION_EVENT_CL_CREDIT_APPROVAL                         = "REJECTION_EVENT_CL_CREDIT_APPROVAL";
    public static final String REJECTION_EVENT_CL_DISBURSAL                               = "REJECTION_EVENT_CL_DISBURSAL";
    public static final String REJECTION_EVENT_CL_CREDIT_SCORING                          = "REJECTION_EVENT_CL_CREDIT_SCORING";
    public static final String REJECTION_EVENT_CL_CREDIT_BUREAU                           = "REJECTION_EVENT_CL_CREDIT_BUREAU";
    public static final String REJECTION_EVENT_CL_CREDIT_POLICY                           = "REJECTION_EVENT_CL_CREDIT_POLICY";
    public static final String REJECTION_EVENT_CL_FIC                                     = "REJECTION_EVENT_CL_FIC";
    public static final String REJECTION_EVENT_CL_CURING                                  = "REJECTION_EVENT_CL_CURING";
    public static final String REJECTION_EVENT_CL_DEDUPE                                  = "REJECTION_EVENT_CL_DEDUPE";
    
    public static final String REJECTION_EVENT_FE_KYC                        			  = "REJECTION_EVENT_FE_KYC";
    public static final String REJECTION_EVENT_FE_DDE                         			  = "REJECTION_EVENT_FE_DDE";
    public static final String REJECTION_EVENT_FE_FII                                     = "REJECTION_EVENT_FE_FII";
    public static final String REJECTION_EVENT_FE_LOGIN_ACCEPTANCE                        = "REJECTION_EVENT_FE_LOGIN_ACCEPTANCE";
    public static final String REJECTION_EVENT_FE_RECOMMENDATION                          = "REJECTION_EVENT_FE_RECOMMENDATION";
    public static final String REJECTION_EVENT_FE_CREDIT_APPROVAL                         = "REJECTION_EVENT_FE_CREDIT_APPROVAL";
    public static final String REJECTION_EVENT_FE_POST_APPROVAL                           = "REJECTION_EVENT_FE_POST_APPROVAL";
    public static final String REJECTION_EVENT_FE_DISBURSAL                               = "REJECTION_EVENT_FE_DISBURSAL";
    public static final String REJECTION_EVENT_FE_RECONSIDERATION                         = "REJECTION_EVENT_FE_RECONSIDERATION";
    public static final String REJECTION_EVENT_FE_LEAD                                    = "REJECTION_EVENT_FE_LEAD";
    public static final String REJECTION_EVENT_FE_LEAD_DETAILS                            = "REJECTION_EVENT_FE_LEAD_DETAILS";
    public static final String REJECTION_EVENT_FE_POLICY_EXECUTION                        = "REJECTION_EVENT_FE_POLICY_EXECUTION"; 
    public static final String REJECTION_EVENT_FE_CIBIL                                   = "REJECTION_EVENT_FE_CIBIL";
    public static final String REJECTION_EVENT_FE_CONSUMER_DURABLE                        = "REJECTION_EVENT_FE_CONSUMER_DURABLE";
    public static final String REJECTION_EVENT_FE_VALUATION                               = "REJECTION_EVENT_FE_VALUATION";
    public static final String REJECTION_EVENT_FE_ICD                                     = "REJECTION_EVENT_FE_ICD";
    public static final String REJECTION_EVENT_FE_FIC                                     = "REJECTION_EVENT_FE_FIC";
    public static final String REJECTION_EVENT_FE_CREDIT_BUREAU                           = "REJECTION_EVENT_FE_CREDIT_BUREAU";
    public static final String REJECTION_EVENT_FE_SALES_QUEUE                             = "REJECTION_EVENT_FE_SALES_QUEUE";

    public static final String REJECTION_EVENT_EDU_KYC                        		       = "REJECTION_EVENT_EDU_KYC";
    public static final String REJECTION_EVENT_EDU_DDE                         		       = "REJECTION_EVENT_EDU_DDE";
    public static final String REJECTION_EVENT_EDU_FII                                     = "REJECTION_EVENT_EDU_FII";
    public static final String REJECTION_EVENT_EDU_LOGIN_ACCEPTANCE                        = "REJECTION_EVENT_EDU_LOGIN_ACCEPTANCE";
    public static final String REJECTION_EVENT_EDU_RECOMMENDATION                          = "REJECTION_EVENT_EDU_RECOMMENDATION";
    public static final String REJECTION_EVENT_EDU_CREDIT_APPROVAL                         = "REJECTION_EVENT_EDU_CREDIT_APPROVAL";
    public static final String REJECTION_EVENT_EDU_POST_APPROVAL                           = "REJECTION_EVENT_EDU_POST_APPROVAL";
    public static final String REJECTION_EVENT_EDU_DISBURSAL                               = "REJECTION_EVENT_EDU_DISBURSAL";
    public static final String REJECTION_EVENT_EDU_RECONSIDERATION                         = "REJECTION_EVENT_EDU_RECONSIDERATION";
    public static final String REJECTION_EVENT_EDU_LEAD                                    = "REJECTION_EVENT_EDU_LEAD";
    public static final String REJECTION_EVENT_EDU_LEAD_DETAILS                            = "REJECTION_EVENT_EDU_LEAD_DETAILS";
    public static final String REJECTION_EVENT_EDU_POLICY_EXECUTION                        = "REJECTION_EVENT_EDU_POLICY_EXECUTION";
    public static final String REJECTION_EVENT_EDU_CIBIL                                   = "REJECTION_EVENT_EDU_CIBIL";
    public static final String REJECTION_EVENT_EDU_CONSUMER_DURABLE                        = "REJECTION_EVENT_EDU_CONSUMER_DURABLE";
    public static final String REJECTION_EVENT_EDU_VALUATION                               = "REJECTION_EVENT_EDU_VALUATION";
    public static final String REJECTION_EVENT_EDU_ICD                                     = "REJECTION_EVENT_EDU_ICD";
    public static final String REJECTION_EVENT_EDU_FIC                                     = "REJECTION_EVENT_EDU_FIC";
    public static final String REJECTION_EVENT_EDU_CREDIT_BUREAU                           = "REJECTION_EVENT_EDU_CREDIT_BUREAU";
    public static final String REJECTION_EVENT_EDU_SALES_QUEUE                             = "REJECTION_EVENT_EDU_SALES_QUEUE";
    public static final String REJECTION_EVENT_EDU_FRAUD_DETECTION                         = "REJECTION_EVENT_EDU_FRAUD_DETECTION";
    public static final String REJECTION_EVENT_EDU_DISBURSAL_AUTHOR                        = "REJECTION_EVENT_EDU_DISBURSAL_AUTHOR";
    
    public static final String APPROVE_EVENT_PL_CREDIT_APPROVAL                           = "APPROVE_EVENT_PL_CREDIT_APPROVAL";
    public static final String APPROVE_EVENT_CC_CREDIT_APPROVAL                           = "APPROVE_EVENT_CC_CREDIT_APPROVAL";
    public static final String APPROVE_EVENT_ML_CREDIT_APPROVAL                           = "APPROVE_EVENT_ML_CREDIT_APPROVAL";
    public static final String APPROVE_EVENT_OMNI_CREDIT_APPROVAL                         = "APPROVE_EVENT_OMNI_CREDIT_APPROVAL";
    public static final String APPROVE_EVENT_MAL_CREDIT_APPROVAL                          = "APPROVE_EVENT_MAL_CREDIT_APPROVAL";
    public static final String APPROVE_EVENT_CL_CREDIT_APPROVAL                           = "APPROVE_EVENT_CL_CREDIT_APPROVAL";
    public static final String APPROVE_EVENT_LAP_CREDIT_APPROVAL                          = "APPROVE_EVENT_LAP_CREDIT_APPROVAL";
    public static final String APPROVE_EVENT_CV_CREDIT_APPROVAL                           = "APPROVE_EVENT_CV_CREDIT_APPROVAL";
    public static final String APPROVE_EVENT_MHL_CREDIT_APPROVAL                          = "APPROVE_EVENT_MHL_CREDIT_APPROVAL";
    public static final String APPROVE_EVENT_FE_CREDIT_APPROVAL                           = "APPROVE_EVENT_FE_CREDIT_APPROVAL";
    public static final String APPROVE_EVENT_EDU_CREDIT_APPROVAL                          = "APPROVE_EVENT_EDU_CREDIT_APPROVAL";
    //Event code to send SMS with PAL email
    public static final String LEAD_CHANGE_COMMUNICATION_EVENT                            = "LEAD_CHANGE_COMMUNICATION_EVENT";




    public static final String Receipt_Details_Save = "RECEIPT_DETAILS_SAVE";
    
    public static final String FIV_SAVE_NEW = "FIV_SAVE_NEW";
	
	public static final String AGRL_CALC_LAND_VALUE = "AGRL_CALC_LAND_VALUE";
	
	public static final String CHARGE_CALCULATION_FROM_RULE_MATRIX = "CHARGE_CALCULATION_FROM_RULE_MATRIX";

    public static final String LIVING_EXPENSE_CALCULATION  = "LIVING_EXPENSE_CALCULATION";

    public static final String HIGH_CONCURRENCY_LOGOUT_EVENT  = "HIGH_CONCURRENCY_LOGOUT_EVENT";
    
    public static final String DOCUMENT_APPROVAL_INITIATION_EVENT  = "DOCUMENT_APPROVAL_INITIATION_EVENT";

    public static final String DOCUMENT_APPROVAL_COMPLETION_EVENT  = "DOCUMENT_APPROVAL_COMPLETION_EVENT";

    public static final String DYNAMIC_EVENT = "_DYNAMIC_EVENT";
    public static final String DYNAMIC_EVENT_FIV = "_DYNAMIC_EVENT_FIV";
    public static final String DYNAMIC_EVENT_COLL = "_DYNAMIC_EVENT_COLL";

}