/**
 * Copyright Nucleus Software India Pvt Ltd. All rights reserved.
 */
package com.nucleus.makerchecker;

/**
 * @author Nucleus Software India Pvt Ltd
 * This class holds all the String/numeric constants used in various places for Master approval flow.
 */
public interface MasterApprovalFlowConstants {

    public static final String WORKFLOW_DEFINITION_ID                = "MasterApprovalProcess";
    public static final String CHECKER_APPROVAL_TASK_WF_ID           = "ApprovalTaskForChecker";
    public static final String MAKER_CHANGES_SEND_BACK_WF_ID         = "ChangesSentBackToMaker";
    public static final String WF_APPROVAL_TASK_ACTIONS_VARIABLE_KEY = "actions";
    public static final String WF_PROCESS_ENTITY_VARIABLE_KEY        = "processEntity";
    public static final String AUTOAPPROVAL_WORKFLOW_DEFINITION_ID   = "AutoApprovalProcess";

    // UI required constants.
    public static final String edit                                  = "Edit";
    public static final String sendForApproval                       = "SendForApproval";
    public static final String SEND_FOR_APPROVAL                       = "Send for approval";
    public static final String autoApproval                          = "AutoApproval";
    public static final String delete                                = "Delete";
    public static final String CLONE                                 = "Clone";
    public static final String SEND_BACK                             = "Send Back";
    public static final String APPROVED                              = "Approved";
    public static final String REJECTED                              = "Rejected";
    

    public static final String FLAG_Y                                = "Y";
    public static final String FLAG_N                                = "N";
}
