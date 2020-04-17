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
package com.nucleus.cfi.sms.service;

import com.nucleus.cfi.sms.pojo.ShortMessageSendResponsePojo;
import com.nucleus.cfi.sms.pojo.SmsMessage;


/**
 * @author Nucleus Software Exports Limited
 * Service class to send SMS.
 */
public interface ShortMessageIntegrationService {

    ShortMessageSendResponsePojo sendShortMessage(SmsMessage smsMessage);
    
	/**
	 * Async call to integration server for sending sms.
	 * @param smsMessage
	 * @return
	 */
	ShortMessageSendResponsePojo sendShortMessageAsynchronously(SmsMessage smsMessage);

	ShortMessageSendResponsePojo sendShortMessage(SmsMessage smsMessage, boolean isAsync);

}
