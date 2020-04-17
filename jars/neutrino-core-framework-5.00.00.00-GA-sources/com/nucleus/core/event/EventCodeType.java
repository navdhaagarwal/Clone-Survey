package com.nucleus.core.event;

import javax.persistence.Entity;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.nucleus.core.genericparameter.entity.GenericParameter;

/* EventCodeType distinguishes between
 * Transaction based EventCodes and AdHoc EventCodes
 * 
 * */

@Entity
@DynamicUpdate
@DynamicInsert
public class EventCodeType extends GenericParameter{
    
    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    
    public static final String TRANSACTION_BASED_EVENTCODE   = "TransactionEvent";
    public static final String ADHOC_EVENTCODE   = "AdHocEvent";

}
