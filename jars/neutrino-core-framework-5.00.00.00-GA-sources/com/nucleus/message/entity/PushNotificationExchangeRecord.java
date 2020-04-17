package com.nucleus.message.entity;

import javax.persistence.Column;
import javax.persistence.Entity;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

/**
 * @author Nucleus Software Exports Limited
 *
 * A class to keep record of SMS messages originating from this system.This
 * class has PUSH NOTIFICATION specific attributes.
 */
@Entity
@DynamicUpdate
@DynamicInsert
public class PushNotificationExchangeRecord extends MessageExchangeRecord {
    
    private static final long serialVersionUID =  1764237892634826349L;
    @Column(name="PUSH_MESSAGE_BODY", length=1000)
    private String            pushMessageBody;
    
    @Column(name="PUSH_TO", length=1000)
    private String            pushToDeviceId;
    private String            pushStatusMessage;

    public String getPushMessageBody() {
        return pushMessageBody;
    }

    public void setPushMessageBody(String pushMessageBody) {
        this.pushMessageBody = pushMessageBody;
    }

    public String getPushToDeviceId() {
        return pushToDeviceId;
    }

    public void setPushToDeviceId(String pushToDeviceId) {
        this.pushToDeviceId = pushToDeviceId;
    }

    public String getPushStatusMessage() {
        return pushStatusMessage;
    }

    public void setPushStatusMessage(String pushStatusMessage) {
        this.pushStatusMessage = pushStatusMessage;
    }

}
