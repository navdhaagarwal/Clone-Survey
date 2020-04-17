/**
 * This file and a proportion of its content is copyright of Nucleus Software Exports Limited - � 2012. All rights reserved.
 * Any redistribution or reproduction of part or all of the contents in any form is prohibited other than the following:
 * - you cannot print or download to a local hard disk extract contents either part or full for personal/ commercial/
 * academic or any other use
 * - you may not copy the content to individual/ third parties for any type of use, either as compiled or source format
 * without the knowledge and consent of Nucleus Software
 * - You may not, except with our express written permission, distribute or commercially exploit the content. Nor may you
 * transmit it or store it in any other web site or other form of electronic retrieval system.
 */
package com.nucleus.cfi.sms.pojo;

import java.io.Serializable;

/**
 * @author Nucleus Software Exports Limited
 *
 */
public class SmsMessage implements Serializable {

    private static final long serialVersionUID = 342342L;

    private String            messageOriginatorId;
    private String            from;
    private String            to;
    private String            body;
    private String 			  uniqueRequestId;	

    public String getUniqueRequestId() {
		return uniqueRequestId;
	}

	public void setUniqueRequestId(String uniqueRequestId) {
		this.uniqueRequestId = uniqueRequestId;
	}

	public String getMessageOriginatorId() {
        return messageOriginatorId;
    }

    public void setMessageOriginatorId(String messageOriginatorId) {
        this.messageOriginatorId = messageOriginatorId;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

}
