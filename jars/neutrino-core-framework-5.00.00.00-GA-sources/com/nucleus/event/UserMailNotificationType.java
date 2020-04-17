package com.nucleus.event;

public interface UserMailNotificationType {
    /**
     * when mail is sent from sender to reciever
     */
    String USER_MAIL_NEW                             = "NEW";

    /**
     * when mail is deleted from sender'soutbox but not from reciever'sinbox
     */
    String USER_MAIL_DELETED_FROM_OUTBOX             = "DELETED_FROM_OUTBOX";

    /**
     * when mail is deleted from reciever'sinbox but not from sender'soutbox
     */
    String USER_MAIL_DELETED_FROM_INBOX              = "DELETED_FROM_INBOX";

    /**
     * when mail is first deleted from inbox then deleted from outbox
     */
    String USER_MAIL_DELETED_FROM_INBOX_AND_OUTBOX   = "DELETED_FROM_INBOX_AND_OUTBOX";

    /**
     * when mail is first deleted from sender's trash then from inbox
     */
    String USER_MAIL_DELETED_FROM_OUTBOX_TRASH_INBOX = "DELETED_FROM_OUTBOX_TRASH_INBOX";

    /**
     * message is deleted only from sender's trash
     */
    String USER_MAIL_DELETED_FROM_INBOX_TRASH        = "DELETED_FROM_INBOX_TRASH";

    /**
     * mail is deleted from reciever's trash then from outbox
     */
    String USER_MAIL_DELETED_FROM_INBOX_TRASH_OUTBOX = "DELETED_FROM_INBOX_TRASH_OUTBOX";

    /**
     * mail is deleted from sender's trash
     */
    String USER_MAIL_DELETED_FROM_OUTBOX_TRASH       = "DELETED_FROM_OUTBOX_TRASH";

    /**
     * mail is finally deleted from sender's side and reciever's side
     */
    String USER_MAIL_DELETED                         = "DELETED";

    /**
     * mail is first deleted from inbox then from sender's trash
     */
    String USER_MAIL_DELETED_FROM_INBOX_OUTBOX_TRASH = "DELETED_FROM_INBOX_OUTBOX_TRASH";

    /**
     * first deleted from outbox then from reciever's trash
     */
    String USER_MAIL_DELETED_FROM_OUTBOX_INBOX_TRASH = "DELETED_FROM_OUTBOX_INBOX_TRASH";

    /**
     * mail is deleted first from outbox then inbox
     */
    String USER_MAIL_DELETED_FROM_OUTBOX_AND_INBOX   = "DELETED_FROM_OUTBOX_AND_INBOX";

    String USER_MAIL_DELETED_FROM_TRASH              = "DELETED_FROM_TRASH";

    String USER_MAIL_READ                            = "READ";
    
    
    String TYPE_INBOX 	="inbox";
    String TYPE_OUTBOX 	="outbox";
    String TYPE_TRASH	="trash";
    
    /**
     * Field number for sorting the column on datatable of email(inbox,outbox & trash)
     */
   int SORT_COL_EMAIL_SUBJECT 				= 3;
   int SORT_COL_EMAIL_USER_NAME 			= 4;
   int SORT_COL_EMAIL_DATE 					= 5;
   int SORT_COL_EMAIL_NOTIFICATION_PRIORITY = 6;

   int SORT_COL_EMAIL_FAVOURITE = 1;
}
