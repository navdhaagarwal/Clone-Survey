package com.nucleus.core.workflowconfig.entity;

import javax.persistence.Entity;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.nucleus.core.genericparameter.entity.GenericParameter;

@Entity
@DynamicUpdate
@DynamicInsert
public class WorkflowConfigurationType extends GenericParameter {

    private static final long  serialVersionUID                      = 7807123737531118L;

    public static final String LOAN_WORKFLOW_CONFIG                  = "loan_workflow_config";
    public static final String LOAN_WORKFLOW_CONFIG_LEAD             = "loan_workflow_config_lead";
    public static final String LEAD_WORKFLOW_CONFIG                  = "lead_workflow_config";
    public static final String PROPOSAL_WORKFLOW_CONFIG              = "proposal_workflow_config";
    public static final String CONSUMER_DURABLE_WORKFLOW_CONFIG      = "consumer_durable_workflow_config";
    public static final String CREDIT_CARD_WORKFLOW_CONFIG           = "cas_credit_card_process";
    public static final String HOME_LOAN_WORKFLOW_CONFIG             = "home_loan_workflow_config";
    public static final String MICRO_HOUSING_WORKFLOW_CONFIG         = "micro_housing_loan_config";
    public static final String LAP_WORKFLOW_CONFIG                   = "lap_workflow_config";
    public static final String DISBURSAL_INITIATION_WORKFLOW_CONFIG  = "disbursal_initiation_workflow_config";
    public static final String PERSONAL_LOAN_WORKFLOW_CONFIG         = "personal_loan_workflow_config";
    public static final String COMMERCIAL_AUTO_LOAN_WORKFLOW_CONFIG  = "commercial_loan_workflow_config";
    public static final String SHOPING_CREDIT_LOAN_WORKFLOW_CONFIG   = "shoping_credit_workflow_config";
    public static final String LAP_LOAN_WORKFLOW_CONFIG              = "lap_loan_workflow_config";
    public static final String APLUS_HOME_LOAN_WORKFLOW_CONFIG       = "aplus_home_loan_workflow_config";
    public static final String FARM_EQUIPMENT_WORKFLOW_CONFIG        = "farm_equipment_workflow_config";
    public static final String CONSUMER_LOAN_WORKFLOW_CONFIG         = "consumer_loan_workflow_config";
    public static final String EDUCATION_LOAN_WORKFLOW_CONFIG        = "education_loan_workflow_config";
    public static final String OMNI_LOAN_WORKFLOW_CONFIG             =  "omni_loan_workflow_config";
    public static final String AGRICULTURE_LOAN_WORKFLOW_CONFIG      = "agriculture_loan_workflow_config";
    public static final String KISAN_CREDIT_CARD_WORKFLOW_CONFIG     = "kisan_credit_card_workflow_config";
    public static final String SELF_HELP_GROUP_WORKFLOW_CONFIG       = "self_help_group_workflow_config";
    public static final String JOINT_LIABILITY_GROUP_WORKFLOW_CONFIG = "joint_liability_group_workflow_config";
    public static final String TRANCHE_DISBURSAL_INITIATION_WORKFLOW_CONFIG  = "tranche_disbursal_initiation_workflow_config";
    public static final String COMMERCIAL_EQUIPMENT_WORKFLOW_CONFIG  = "commercial_equipment_workflow_config";
    public static final String GOLD_LOAN_WORKFLOW_CONFIG  			 = "gold_loan_workflow_config";
    public static final String NEW_CREDIT_CARD_WORKFLOW_CONFIG           = "new_cas_credit_card_process";
    public static final String FINANCE_AGAINST_SECURITY_WORKFLOW_CONFIG  = "finance_against_security_workflow_config";
    
    

}
