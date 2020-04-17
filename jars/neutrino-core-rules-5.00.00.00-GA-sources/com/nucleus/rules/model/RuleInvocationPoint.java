package com.nucleus.rules.model;

import javax.persistence.Entity;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.nucleus.core.genericparameter.entity.GenericParameter;

/**
 * @author Nucleus Software Exports Limited Constants for Parameter Data type
 */

@Entity
@DynamicUpdate
@DynamicInsert
public class RuleInvocationPoint extends GenericParameter {

	private static final long serialVersionUID = -5265654164895690972L;

	public static final String RULE_INVOCATION_POINT_SAVE_APPLICATION = "SAVE_APPLICATION";
	public static final String RULE_INVOCATION_POINT_QDE_EXIT_WF = "QDE_EXIT_WF";
	public static final String RULE_INVOCATION_POINT_DDE_EXIT_WF = "DDE_EXIT_WF";
	public static final String RULE_INVOCATION_POINT_SAVE_CUSTOMER = "SAVE_CUSTOMER";
	public static final String RULE_INVOCATION_POINT_SAVE_PROPOSAL = "SAVE_PROPOSAL";
	public static final String RULE_INVOCATION_POINT_SAVE_AREA = "SAVE_AREA";
	public static final String RULE_INVOCATION_RESULT_TRANSACTION_ID = "RULE_INVOCATION_RESULT_TRANSACTION_ID";
	public static final String RULE_INVOCATION_RESULT_VALUE = "RULE_INVOCATION_RESULT_VALUE";
	public static final String RULE_INVOCATION_CRITERIA_RULE_RESULT_VALUE = "RULE_INVOCATION_CRITERIA_RULE_RESULT_VALUE";
	public static final String RULE_INVOCATION_POINT_SAVE_PRODUCT = "SAVE_PRODUCT";
	public static final String RULE_INVOCATION_POINT_SHOW_OFFERS = "SHOW_OFFERS";
	public static final String RULE_INVOCATION_POINT_SHOW_OFFERS_FOR_CREDIT_CARD = "SHOW_OFFERS_CREDIT_CARD";
	public static final String RULE_INVOCATION_POINT_SAVE_CITY = "SAVE_CITY";
	public static final String RULE_INVOCATION_POINT_SAVE_COUNTRY = "SAVE_COUNTRY";
	public static final String RULE_INVOCATION_POINT_SAVE_STATE = "SAVE_STATE";
	public static final String RULE_INVOCATION_POINT_SAVE_REGION = "SAVE_REGION";
	public static final String RULE_INVOCATION_POINT_SAVE_ZIPCODE = "SAVE_ZIPCODE";
	public static final String RULE_INVOCATION_POINT_SAVE_DISTRICT = "SAVE_DISTRICT";
	public static final String RULE_INVOCATION_POINT_SAVE_CUSTOMER_CATEGORY = "SAVE_CUSTOMER_CATEGORY";
	public static final String RULE_INVOCATION_POINT_SAVE_CUSTOMER_CONSTITUTION = "SAVE_CUSTOMER_CONSTITUTION";
	public static final String RULE_INVOCATION_POINT_SAVE_CUSTOMER_INCOMESOURCE = "SAVE_CUSTOMER_INCOMESOURCE";
	public static final String RULE_INVOCATION_POINT_SAVE_INDUSTRY = "SAVE_INDUSTRY";
	public static final String RULE_INVOCATION_POINT_SAVE_EXTERNALBANK = "SAVE_EXTERNALBANK";
	public static final String RULE_INVOCATION_POINT_SAVE_BUSINESSPARTNER = "SAVE_BUSINESSPARTNER";
	public static final String RULE_INVOCATION_POINT_SAVE_EMPLOYER = "SAVE_EMPLOYER";
	public static final String RULE_INVOCATION_POINT_SAVE_ASSETMODEL = "SAVE_ASSETMODEL";
	public static final String RULE_INVOCATION_POINT_SAVE_VEHICLECATEGORY = "SAVE_VEHICLECATEGORY";
	public static final String RULE_INVOCATION_LAD_DOCUMENT = "LAD_DOCUMENT";
	public static final String RULE_INVOCATION_CREDIT_POLICY_EXECUTION = "CREDIT_POLICY_EXECUTION";
	public static final String SAVE_CUSTOMER_PERSONAL_TAB = "SAVE_CUSTOMER_PERSONAL_TAB";
	public static final String RULE_INVOCATION_ELIGIBILITY_CHECK_INTERNET_CHANNEL = "ELIGIBILITY_CHECK_INTERNET_CHANNEL";
	public static final String SAVE_CUSTOMER_EMPLOYMENT_TAB = "SAVE_CUSTOMER_EMPLOYMENT_TAB";
	public static final String SAVE_CUSTOMER_COMMUNICATION_TAB = "SAVE_CUSTOMER_COMMUNICATION_TAB";
	public static final String SAVE_CUSTOMER_INCOME_TAB = "SAVE_CUSTOMER_INCOME_TAB";
	public static final String SAVE_CUSTOMER_BANK_TAB = "SAVE_CUSTOMER_BANK_TAB";
	public static final String RULE_INVOCATION_REQUIRED_DOCUMENTS_IC = "REQUIRED_DOCUMENTS_IC";
	public static final String RULE_INVOCATION_ELIGIBILITY_POLICY_EXECUTION = "ELIGIBILITY_POLICY_EXECUTION";
	public static final String RULE_INVOCATION_ELIGIBILITY_SET_EXECUTION = "ELIGIBILITY_SET_EXECUTION";
	public static final String RULE_INVOCATION_DEVIATION_POLICY_EXECUTION = "DEVIATION_POLICY_EXECUTION";

	// Invocation point to calculate ability indicators on underWriter Screen

	public static final String CALCULATE_GMP_UNDERWRITER = "CALCULATE_GMP_UNDERWRITER";
	public static final String CALCULATE_IIR_UNDERWRITER = "CALCULATE_IIR_UNDERWRITER";
	public static final String CALCULATE_FOIR_UNDERWRITER = "CALCULATE_FOIR_UNDERWRITER";
    
    //// Calculation to FOIR and Unsecured FOIR at Recommendation Stage
    
    public static final String CALCULATE_TOTAL_FOIR_AT_RECOMMENDATION             = "CALCULATE_TOTAL_FOIR_AT_RECOMMENDATION";
    public static final String CALCULATE_UNSECURED_FOIR_AT_RECOMMENDATION         = "CALCULATE_UNSECURED_FOIR_AT_RECOMMENDATION";
    
    /////

	public static final String INVOKE_ELIGIBILTY_RULES = "INVOKE_ELIGIBILTY_RULES";

	public static final String SAVE_APPLICATION_SOURCING_TAB = "SAVE_APPLICATION_SOURCING_TAB";
	public static final String SAVE_APPLICATION_ASSET_TAB = "SAVE_APPLICATION_ASSET_TAB";
	public static final String SAVE_APPLICATION_LOANPARAMETERS_TAB = "SAVE_APPLICATION_LOANPARAMETERS_TAB";
	public static final String CALCULATE_GMP_CREDITCARD = "CALCULATE_GMP_CREDITCARD";
	public static final String CALCULATE_IIR_CREDITCARD = "CALCULATE_IIR_CREDITCARD";
	public static final String CALCULATE_AIIR_CREDITCARD = "CALCULATE_AIIR_CREDITCARD";

	public static final String RULE_INVOCATION_ELIGIBILITY_CHECK_LEAD = "ELIGIBILITY_CHECK_LEAD";
	public static final String RULE_INVOCATION_POINT_SHOW_OFFERS_FOR_LEAD = "SHOW_OFFERS_CREDIT_CARD_LEAD";
	public static final String RULE_INVOCATION_CHARGE_POLICY_EXECUTION = "CHARGE_POLICY_EXECUTION";

	public static final String CV_RECONSIDERATION_COMPLETION = "CV_RECONSIDERATION_COMPLETION";
	public static final String CV_RECONSIDERATION_EXIT = "CV_RECONSIDERATION_EXIT";
	
	public static final String RULE_INVOCATION_LOAN_QUESTION_EXECUTION="RULE_INVOCATION_LOAN_QUESTION_EXECUTION";

	/*
	 * public static final String SAVE_APPLICATION = "SAVE_APPLICATION"; public
	 * static final String QDE_COMPLETION_RULES_INVOCATION_POINT =
	 * "qde_completion_rules_invocation_point"; public static final String
	 * DDE_COMPLETION_RULES_INVOCATION_POINT =
	 * "dde_completion_rules_invocation_point"; public static final String
	 * dde_exit_rules_invocation_point = "dde_exit_rules_invocation_point";
	 * public static final String SHOW_OFFERS = "SHOW_OFFERS"; public static
	 * final String REQUIRED_DOCUMENTS_IC = "REQUIRED_DOCUMENTS_IC"; public
	 * static final String SAVE_CUSTOMER_CATEGORY = "SAVE_CUSTOMER_CATEGORY";
	 * public static final String SAVE_CUSTOMER_CONSTITUTION =
	 * "SAVE_CUSTOMER_CONSTITUTION"; public static final String
	 * SAVE_CUSTOMER_INCOMESOURCE = "SAVE_CUSTOMER_INCOMESOURCE"; public static
	 * final String ELIGIBILITY_CHECK_INTERNET_CHANNEL =
	 * "ELIGIBILITY_CHECK_INTERNET_CHANNEL"; public static final String
	 * SAVE_CUSTOMER_PERSONAL_TAB = "SAVE_CUSTOMER_PERSONAL_TAB"; public static
	 * final String SAVE_CUSTOMER_EMPLOYMENT_TAB =
	 * "SAVE_CUSTOMER_EMPLOYMENT_TAB"; public static final String
	 * SAVE_CUSTOMER_COMMUNICATION_TAB = "SAVE_CUSTOMER_COMMUNICATION_TAB";
	 * public static final String SAVE_CUSTOMER_INCOME_TAB =
	 * "SAVE_CUSTOMER_INCOME_TAB"; public static final String
	 * SAVE_CUSTOMER_BANK_TAB = "SAVE_CUSTOMER_BANK_TAB"; public static final
	 * String CALCULATE_GMP_UNDERWRITER = "CALCULATE_GMP_UNDERWRITER"; public
	 * static final String CALCULATE_IIR_UNDERWRITER =
	 * "CALCULATE_IIR_UNDERWRITER"; public static final String
	 * CALCULATE_FOIR_UNDERWRITER = "CALCULATE_FOIR_UNDERWRITER"; public static
	 * final String INVOKE_ELIGIBILTY_RULES = "INVOKE_ELIGIBILTY_RULES"; public
	 * static final String fi_completion_rules_invocation_point =
	 * "fi_completion_rules_invocation_point"; public static final String
	 * SAVE_APPLICATION_SOURCING_TAB = "SAVE_APPLICATION_SOURCING_TAB"; public
	 * static final String SAVE_APPLICATION_ASSET_TAB =
	 * "SAVE_APPLICATION_ASSET_TAB"; public static final String
	 * SAVE_APPLICATION_LOANPARAMETERS_TAB =
	 * "SAVE_APPLICATION_LOANPARAMETERS_TAB"; public static final String
	 * LAD_DOCUMENT = "LAD_DOCUMENT"; public static final String
	 * BUREAU_COMPLETION_RULES_INVOCATION_POINT =
	 * "bureau_completion_rules_invocation_point"; public static final String
	 * SCORING_COMPLETION_RULES_INVOCATION_POINT =
	 * "scoring_completion_rules_invocation_point"; public static final String
	 * SCORING_EXIT_RULES_INVOCATION_POINT =
	 * "scoring_exit_rules_invocation_point"; public static final String
	 * APPLICANT_SCORE = "APPLICANT_SCORE"; public static final String
	 * GUARANTOR_SCORE = "GUARANTOR_SCORE"; public static final String
	 * COLLATERAL_SCORE = "COLLATERAL_SCORE"; public static final String
	 * APPLICANT_CHARACTER_SCORE = "APPLICANT_CHARACTER_SCORE"; public static
	 * final String ELIGIBILITY_COMPLETION_RULES_INVOCATION_POINT =
	 * "eligibility_completion_rules_invocation_point"; public static final
	 * String POLICY_COMPLETION_RULES_INVOCATION_POINT =
	 * "policy_completion_rules_invocation_point"; public static final String
	 * CD_DEDUPE_ROUTING_RULES_INVOCATION_POINT =
	 * "cd_dedupe_routing_rules_invocation_point"; public static final String
	 * CD_FI_ROUTING_RULES_INVOCATION_POINT =
	 * "cd_fi_routing_rules_invocation_point"; public static final String
	 * CD_CREDIT_BUREAU_ROUTING_RULES_INVOCATION_POINT =
	 * "cd_credit_bureau_routing_rules_invocation_point"; public static final
	 * String CD_DEDUPE_COMPLETION_RULES_INVOCATION_POINT =
	 * "cd_dedupe_completion_rules_invocation_point"; public static final String
	 * CD_DEDUPE_EXIT_RULES_INVOCATION_POINT =
	 * "cd_dedupe_exit_rules_invocation_point"; public static final String
	 * CREDIT_BUREAU_COMPLETION_RULES_INVOCATION_POINT =
	 * "credit_bureau_completion_rules_invocation_point"; public static final
	 * String CREDIT_BUREAU_EXIT_RULES_INVOCATION_POINT =
	 * "credit_bureau_exit_rules_invocation_point"; public static final String
	 * POLICY_CHECK_COMPLETION_RULES_INVOCATION_POINT =
	 * "policy_check_completion_rules_invocation_point"; public static final
	 * String POLICY_CHECK_EXIT_RULES_INVOCATION_POINT =
	 * "policy_check_exit_rules_invocation_point"; public static final String
	 * CD_POLICY_CHECK_ROUTING_RULES_INVOCATION_POINT =
	 * "cd_policy_check_routing_rules_invocation_point"; public static final
	 * String fi_exit_rules_invocation_point = "fi_exit_rules_invocation_point";
	 */

}