/**
 * Copyright Nucleus Software India Pvt Ltd. All rights reserved.
 */
package com.nucleus.whatsApp.service;

import java.io.File;
import java.io.IOException;
import java.util.Set;

import com.nucleus.service.BaseService;

/**
 * The Interface NotificationMasterService.
 *
 * @author Nucleus Software India Pvt Ltd
 */
public interface WhatsAppMessageService extends BaseService {

	/**
	 * This method is to send the whatsAppNotification with or without file and save the exchange record in db.
	 * @param phoneNumbers
	 * @param body
	 * @param ownerEntityUri
	 * @param extIdentifier
	 * @param attachFile
	 * @throws IOException
	 */
	public	void processWhatsAppNotificationTask(Set<String> phoneNumbers, String body, String ownerEntityUri,
			String extIdentifier, File attachFile) throws IOException;

   

	
}
