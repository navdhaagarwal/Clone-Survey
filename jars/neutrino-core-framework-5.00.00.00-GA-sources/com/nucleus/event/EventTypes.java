/**
 * Copyright Nucleus Software India Pvt Ltd. All rights reserved.
 */
package com.nucleus.event;

/**
 * Central class to store constants for all event types.
 * 
 * @author Nucleus Software India Pvt Ltd
 */
public class EventTypes {

	public static final int COMMENT_ADDED_EVENT = 1;
	public static final int COMMENT_DELETED_EVENT = 2;
	public static final int COMMENT_MODIFIED_EVENT = 3;
	public static final int WORKFLOW_TASK_EVENT = 4;
	public static final int MAKER_CHECKER_FLOW_COMPLETION_EVENT = 5;
	public static final int MAKER_CHECKER_CREATE_EVENT = 6;
	public static final int MAKER_CHECKER_UPDATE_EVENT = 7;
	public static final int MAKER_CHECKER_SEND_FOR_APPROVAL = 8;
	public static final int MAKER_CHECKER_SAVED_AND_SEND_FOR_APPROVAL = 9;
	public static final int MAKER_CHECKER_DELETE = 10;
	public static final int MAKER_CHECKER_MARKED_FOR_DELETION = 11;
	public static final int MAKER_CHECKER_APPROVED = 12;
	public static final int MAKER_CHECKER_UPDATED_APPROVED = 13;
	public static final int MAKER_CHECKER_DELETION_APPROVED = 14;
	public static final int MAKER_CHECKER_REJECTED = 15;
	public static final int MAKER_CHECKER_UPDATED_REJECTED = 16;
	public static final int MAKER_CHECKER_DELETION_REJECTED = 17;
	public static final int MAKER_CHECKER_SEND_BACK = 18;
	public static final int USER_SECURITY_TRAIL_LOGIN_SUCCESS = 19;
	public static final int USER_SECURITY_TRAIL_LOGIN_FAIL = 20;
	public static final int USER_SECURITY_TRAIL_LOGOUT = 21;
	public static final int WORKFLOW_ASSIGN_NOTIFICATION_EVENT = 22;
	public static final int WORKFLOW_ESCALATION_NOTIFICATION_EVENT = 23;
	public static final int WORKFLOW_QUICKLEAD_CREATE_EVENT = 24;
	public static final int WORKFLOW_MOVETO_APPLICATION_FROM_LEAD = 25;
	public static final int REJECT_FROM_LEAD = 26;
	public static final int WORKFLOW_ENTER_STAGE = 27;
	public static final int WORKFLOW_EXIT_STAGE = 28;
	public static final int NOTE_ADDED_EVENT = 29;
	public static final int NOTE_DELETED_EVENT = 30;
	public static final int NOTE_MODIFIED_EVENT = 31;
	public static final int WORKFLOW_INTERNET_CHANNEL_LEAD_CREATE_EVENT = 32;
	public static final int USER_ADMIN_SEND_NOTIFICATION = 33;
	public static final int RULE_INVOCATION_EVENT = 34;
	public static final int LOAN_APP_ENTITY_CREATE = 35;
	public static final int LOAN_APP_ENTITY_UPDATE = 36;
	public static final int LOAN_APP_ENTITY_DELETE = 37;
	public static final int WORKFLOW_MOVETO_APPLICATION_FROM_PROPOSAL = 38;
	public static final int WORKFLOW_PROPOSAL_CREATE_EVENT = 39;
	public static final int WORKFLOW_ADHOC_TASK_ESCALATION_EVENT = 40;
	public static final int USER_CREATED_EVENT = 41;
	public static final int USER_UPDATED_EVENT = 42;
	public static final int USER_INACTIVATED_EVENT = 43;
	public static final int USER_BLOCKED_EVENT = 44;
	public static final int USER_PASSWORD_RESET_EVENT = 45;
	public static final int USER_ACTIVATED_EVENT = 46;
	public static final int USER_REMOVED_FROM_TEAM_EVENT = 47;
	public static final int USER_ADDED_TO_TEAM_EVENT = 48;
	public static final int TEAM_LEADER_UPDATED = 49;
	public static final int TEAM_DELETED_EVENT = 50;
	public static final int PROPERTY_MASTER_CREATE_EVENT = 51;
	public static final int PROPERTY_MASTER_UPDATE_EVENT = 52;
	public static final int WORKFLOW_RESET_TAT_EVENT = 53;
	public static final int WORKFLOW_TEAM_CHANGE_EVENT = 54;
	public static final int LOAN_APP_ENTITY_BRANCH_UPDATE = 55;
	public static final int WORKFLOW_APPLICATION_ESCALATION_NOTIFICATION_EVENT = 56;
	public static final int UPDATE_EXPOSURE_DATA_ENTRY_EVENT = 57;
	public static final int UPDATE_EXPOSURE_APPROVED_EVENT = 59;
	public static final int UPDATE_EXPOSURE_DISBURSAL_EVENT = 60;
	public static final int UPDATE_EXPOSURE_CANCELLED_EVENT = 61;
	public static final int COPY_APPLICATION_EVENT = 62;
	public static final int DELETED_EXPORTED_RECORDS = 63;
	public static final int COPY_APPLICATION_SAVE_EVENT = 67;
	public static final int APPOINTMENT_ESCALATION_NOTIFICATION_EVENT = 68;
	public static final int DECISION_MARKED_EVENT = 69;
	public static final int TEMPBUILDER_PROJECT_MOVETO_APPLICATION_FROM_LEAD = 70;
	
 
    public static final int PDE_ADDRESS_UPDATE_MAKER_PROCEED                   = 71;
    public static final int PDE_ADDRESS_UPDATE_CHECKER_APPROVE                 = 72;
    public static final int PDE_ADDRESS_UPDATE_CHECKER_REJECT                 = 73;
    public static final int CONFIGURATION_UPDATED_EVENT                 = 74;
	
	public static final int WORKFLOW_SAVE_EVENT = 75;

	public static final int CONCURRENCY_LOGOUT_EVENT=76;
	public static final int DELEGATION_EVENT = 78;
	public static final int WORKFLOW_EXIT_STAGE_DELEGATION = 79;
	public static final int WORKFLOW_ASSIGN_NOTIFICATION_DELEGATION_EVENT = 80;
	public static final int MAKER_CHECKER_APPROVED_DELEGATION = 81;
	public static final int MAKER_CHECKER_UPDATED_APPROVED_DELEGATION = 82;
	public static final int MAKER_CHECKER_DELETION_APPROVED_DELEGATION = 83;
	public static final int PARAMETER_APPROVAL_EVENT = 84;
	public static final int DECISION_MARKED_DELEGATED_EVENT = 85;
	public static final int USER_PREFERENCES_UPDATED_EVENT = 86;

}
