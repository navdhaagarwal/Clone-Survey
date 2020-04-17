package com.nucleus.notificationMaster;

import javax.persistence.Entity;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.nucleus.core.genericparameter.entity.GenericParameter;

@Entity
@DynamicUpdate
@DynamicInsert
public class NotificationMasterType extends GenericParameter {

    /**
     * 
     */
    private static final long  serialVersionUID            = 1L;
    public static final String EMAIL_TYPE_NOTIFICATION     = "EMAIL";
    public static final String INAPPMAIL_TYPE_NOTIFICATION = "INAPPMAIL";
    public static final String POPUP_TYPE_NOTIFICATION     = "POPUP";
    public static final String SMS_TYPE_NOTIFICATION       = "SMS";
    public static final String WARNING_TYPE_NOTIFICATION   = "WARNING";
    public static final String PUSH_TYPE_NOTIFICATION      = "PUSHNOTIFICATION";
    public static final String WHATSAPP_TYPE_NOTIFICATION  = "WHATSAPP";

}
