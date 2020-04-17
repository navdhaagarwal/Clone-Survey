package com.nucleus.entity;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * @author amit.parashar
 *
 */
public interface ApprovalStatus {

    // status of an approved entity.
    int APPROVED                     = 0;

    // status of an un approved entity.
    int UNAPPROVED                   = 1;

    // status of an entity for which workflow is in progress. i.e it has been either sent for approval,edition or deletion.
    int WORFLOW_IN_PROGRESS          = 2;

    // Status of an entity which is approved,An edited version of this entity has been created which is not yet approved.
    int APPROVED_MODIFIED            = 3;
    // Status of an entity which has been created after editing an approved version, This entity has not been sent to
    // workflow yet.
    int UNAPPROVED_MODIFIED          = 8;

    // Status of an entity which is approved, and it has been marked for deletion approval to checker.
    int APPROVED_DELETED             = 4;

    // Status of an entity which is approved, and it has been sent for deletion approval to checker.
    int APPROVED_DELETED_IN_PROGRESS = 6;

    // Status of an entity which has been newly created in maker's list, and has not been sent for any approval. ( Draft mode
    // )
    int UNAPPROVED_ADDED             = 7;

    // Status of an entity which was un approved in history.
    int UNAPPROVED_HISTORY           = 10;

    // Status of an entity which is the clone of an object and has not been edited.
    int CLONED                       = 12;

    // Status of an entity which was initially approved , then send for deletion approval , finally approved by checker
    int DELETED_APPROVED_IN_HISTORY  = 5;
    
    
    //status of child entity added
    int CHILD_ADDED = 13;

    //Status of an child entity which was deleted 
    int CHILD_DELETED = 14;
 
    //Status of child record chich is modified
    int CHILD_MODIFIED=15;

    
	List<Integer> APPROVED_RECORD_STATUS_LIST = Collections.unmodifiableList(
			Arrays.asList(APPROVED, APPROVED_MODIFIED, APPROVED_DELETED, APPROVED_DELETED_IN_PROGRESS));
	
	List<Integer> APPROVED_RECORD_STATUS_LIST_INCLUDING_DELETED = Collections.unmodifiableList(Arrays.asList(APPROVED,
			APPROVED_MODIFIED, APPROVED_DELETED, APPROVED_DELETED_IN_PROGRESS, DELETED_APPROVED_IN_HISTORY));
	
	List<Integer> APPROVED_RECORD_STATUS_LIST_EXCLUDING_APPROVED_DELETED = Collections.unmodifiableList(
			Arrays.asList(APPROVED, APPROVED_MODIFIED,APPROVED_DELETED_IN_PROGRESS));
	
	List<Integer> APPROVED_RECORD_STATUS_IN_APPROVED_APPROVED_MODIFIED = Collections.unmodifiableList(
			Arrays.asList(APPROVED, APPROVED_MODIFIED));
	
	List<Integer> HISTORY_RECORD_STATUS_LIST = Collections.unmodifiableList(
			Arrays.asList(UNAPPROVED_HISTORY, DELETED_APPROVED_IN_HISTORY));
	
	List<Integer> UNAPPROVED_AND_HISTORY_RECORD_STATUS_LIST = Collections.unmodifiableList(
			Arrays.asList(UNAPPROVED,UNAPPROVED_HISTORY, DELETED_APPROVED_IN_HISTORY));
    
}
