package com.nucleus.entity;

public class CloneOptionConstants {

    public static final String       COPY_ID_KEY                  = "COPY_ID";
    public static final String       SNAPSHOT_RECORD_KEY          = "SNAPSHOT_RECORD";
    public static final String       APPROVAL_STATUS_KEY          = "APPROVAL_STATUS_ID";
    public static final String       COPY_UUID_KEY                = "COPY_UUID";
    public static final String       SOFT_DELETE_CHILD_KEY        = "SOFT_DELETE_CHILD";
    public static final String       UPDATE_EXISTING_CHILD_ONLY_KEY   = "UPDATE_EXISTING_CHILD_ONLY";
    public static final String       DONT_CLONE_CHILD_KEY         = "DONT_CLONE_CHILD";
    public static final String       HYDRATE_OBJECT               = "HYDRATE_OBJECT";
    public static final int       	 APPROVAL_STATUS_COPY_TRUE    = 100;

    
    public static final CloneOptions SNAPSHOT_CLONING_OPTION      = new CloneOptions(new CloneOption(COPY_ID_KEY, false),
                                                                          new CloneOption(SNAPSHOT_RECORD_KEY, true),
                                                                          new CloneOption(APPROVAL_STATUS_KEY, -1),
                                                                          new CloneOption(COPY_UUID_KEY, true));
    public static final CloneOptions MAKER_CHECKER_CLONING_OPTION = new CloneOptions(new CloneOption(COPY_ID_KEY, false),
                                                                          new CloneOption(SNAPSHOT_RECORD_KEY, false),
                                                                          new CloneOption(APPROVAL_STATUS_KEY,
                                                                                  ApprovalStatus.UNAPPROVED),
                                                                          new CloneOption(COPY_UUID_KEY, true));
    public static final CloneOptions COPY_CLONING_OPTION          = new CloneOptions(new CloneOption(COPY_ID_KEY, false),
                                                                          new CloneOption(SNAPSHOT_RECORD_KEY, false),
                                                                          new CloneOption(APPROVAL_STATUS_KEY,
                                                                                  ApprovalStatus.UNAPPROVED));

    public static final CloneOptions MAKER_CHECKER_COPY_OPTION    = new CloneOptions(new CloneOption(COPY_ID_KEY, false),
                                                                          new CloneOption(SNAPSHOT_RECORD_KEY, false),
                                                                          new CloneOption(APPROVAL_STATUS_KEY,
                                                                                  ApprovalStatus.APPROVED), new CloneOption(
                                                                                  COPY_UUID_KEY, true));
    
    public static final CloneOptions MAKER_CHECKER_COPY_OPTN_WTH_SOFT_CHLD_DELETE = new CloneOptions(new CloneOption(COPY_ID_KEY, false),
            new CloneOption(SNAPSHOT_RECORD_KEY, false),
            new CloneOption(APPROVAL_STATUS_KEY,ApprovalStatus.APPROVED), 
            new CloneOption(COPY_UUID_KEY, true),
            new CloneOption(SOFT_DELETE_CHILD_KEY, true));    
       
    
    public static final CloneOptions CHILD_CLONING_OPTION     = new CloneOptions(new CloneOption(COPY_ID_KEY, false),
            new CloneOption(SNAPSHOT_RECORD_KEY, false),
            new CloneOption(APPROVAL_STATUS_KEY, -1),
            new CloneOption(COPY_UUID_KEY, true));

    public static final CloneOptions COPY_WITH_ID_AND_UUID     = new CloneOptions(new CloneOption(COPY_ID_KEY, true),
            new CloneOption(SNAPSHOT_RECORD_KEY, false),
            new CloneOption(APPROVAL_STATUS_KEY, -1),
            new CloneOption(COPY_UUID_KEY, true));
    
    public static final CloneOptions COPY_EXCEPT_CHILD     = new CloneOptions(new CloneOption(COPY_ID_KEY, false),
            new CloneOption(SNAPSHOT_RECORD_KEY, false),
            new CloneOption(APPROVAL_STATUS_KEY, -1),
            new CloneOption(COPY_UUID_KEY, false),
            new CloneOption(DONT_CLONE_CHILD_KEY, true));

    public static final CloneOptions COPY_WITH_ID_AND_UUID_SET_STTS_APPRVD     = new CloneOptions(new CloneOption(COPY_ID_KEY, true),
            new CloneOption(SNAPSHOT_RECORD_KEY, false),
            new CloneOption(APPROVAL_STATUS_KEY, ApprovalStatus.APPROVED),
            new CloneOption(COPY_UUID_KEY, true));
    
    
    public static final CloneOptions CHILD_CLONING_OPTION_WTH_ONLY_EXSTNG_CHLD     = new CloneOptions(new CloneOption(COPY_ID_KEY, false),
            new CloneOption(SNAPSHOT_RECORD_KEY, false),
            new CloneOption(APPROVAL_STATUS_KEY, ApprovalStatus.UNAPPROVED_HISTORY),
            new CloneOption(COPY_UUID_KEY, true),
            new CloneOption(UPDATE_EXISTING_CHILD_ONLY_KEY,true)
            );
    
    
    public static final CloneOptions COPY_AND_HYDRATE_WITH_ID_AND_UUID     = new CloneOptions(new CloneOption(COPY_ID_KEY, true),
            new CloneOption(SNAPSHOT_RECORD_KEY, false),
            new CloneOption(APPROVAL_STATUS_KEY, -1),
            new CloneOption(COPY_UUID_KEY, true),
            new CloneOption(HYDRATE_OBJECT, true));
    
    public static final CloneOptions COPY_WITH_ID_AND_UUID_AND_STATUS     = new CloneOptions(new CloneOption(COPY_ID_KEY, true),
            new CloneOption(SNAPSHOT_RECORD_KEY, false),
            new CloneOption(APPROVAL_STATUS_KEY, APPROVAL_STATUS_COPY_TRUE),
            new CloneOption(COPY_UUID_KEY, true));

}
