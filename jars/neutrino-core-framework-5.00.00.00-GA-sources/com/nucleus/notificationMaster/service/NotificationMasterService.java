/**
 * Copyright Nucleus Software India Pvt Ltd. All rights reserved.
 */
package com.nucleus.notificationMaster.service;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.mail.MessagingException;

import com.nucleus.core.transaction.TransactionPostCommitWork;
import com.nucleus.document.core.entity.Document;
import com.nucleus.finnone.pro.communicationgenerator.domainobject.AttachmentEncryptionPolicy;
import com.nucleus.mail.MimeMailMessageBuilder;
import com.nucleus.notificationMaster.NotificationMaster;
import com.nucleus.rules.model.SourceProduct;
import com.nucleus.service.BaseService;

import fr.opensagres.xdocreport.template.formatter.FieldsMetadata;
import freemarker.template.TemplateException;

/**
 * The Interface NotificationMasterService.
 *
 * @author Nucleus Software India Pvt Ltd
 */
public interface NotificationMasterService extends BaseService {

    /**
     * Send notification.
     *
     * @param notificationMaster the notification master
     * @param contextmap the contextmap
     * @param ownerEntityUri the owner entity uri
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws TemplateException the template exception
     * @throws MessagingException the messaging exception
     */
	void sendNotification(NotificationMaster notificationMaster, Map contextmap, String ownerEntityUri,
            FieldsMetadata metadata);

    /**
     * Find notification master by notification type.
     *
     * @param notificationType the notification type
     * @return the list
     */
    List<Map<String, Object>> findNotificationMasterByNotificationType(String notificationType);

    /**
     * This method is used to email notification through manual communication
     * @param notificationMaster
     * @param emailIdList
     * @param metadata
     * @param contextmap
     * @throws MessagingException
     * @throws IOException
     */
    void sendManualEmailCommunication(NotificationMaster notificationMaster, List<String> emailIdList,
            FieldsMetadata metadata, Map contextmap) throws MessagingException, IOException;

    /**
     * 
     * This method is used to do manual phone communication
     * @param notificationMaster
     * @param contextMap
     * @param phoneNumbers
     * @param ownerEntityUri
     * @throws IOException
     */
    void sendManualPhoneCommnication(NotificationMaster notificationMaster, Map contextMap,
            List<String> phoneNumbers, String ownerEntityUri) throws IOException;

    void constructEmailAndSend(List<String> emailIdList, Map contextmap, String emailSubject, String emailBoby,
            Document emailAttachedDocument) throws IOException, MessagingException;

    void sendEmail(MimeMailMessageBuilder builder) throws IOException, MessagingException;

    void processSMSNotificationTask(Set<String> phoneNumbers, String body, String ownerEntityUri);

    void sendManualEmail(String subject, String emailBody, Set<String> userEmails, File attachFile)
            throws MessagingException, IOException;
    
    
    /**
     * This method is used to generate the template and send mail if boolean send mail is set to true
     * Currently used for generating the DeliveryOrder and Sanction Letter template
     *
     * @param templateName the template name
     * @param sendMail the send mail
     * @param contextMap the context map
     * @param ownerUri the owner uri
     * @param documentToCreate the document to create
     */
    void sendAutoCommunicationWithTemplate(String templateName, boolean sendMail, Map contextMap, String ownerUri,
            byte[] documentToCreate);
			
	void sendAutoCommunicationWithTemplate(String templateName, boolean sendMail, Map contextMap, String ownerUri,
            byte[] documentToCreate, String suffix);
    
    List<AttachmentEncryptionPolicy> findAttachmentEncryptionPolicyBySourceProduct(SourceProduct sourceProduct);

    /**
     * 
     * @param contextmap
     * @param ownerEntityUri
     * @param notificationMaster
     * @param userIds
     * @param msgBody
     */
    void sendSmsTypeNotification(Map contextmap, String ownerEntityUri, NotificationMaster notificationMaster,
			String msgBody, Set<String> phoneNumbers);

    /**
     * 
     * @param contextmap
     * @param metadata
     * @param notificationMaster
     * @param userEmails
     * @throws IOException
     * @throws MessagingException
     */
	public void sendEmailTypeNotification(Map contextmap, FieldsMetadata metadata, NotificationMaster notificationMaster,
			Set<String> userEmails) throws IOException, MessagingException;

	/**
	 * 
	 * @param contextmap
	 * @param notificationMaster
	 * @return
	 * @throws IOException
	 */
	public String createMessageBody(Map contextmap, NotificationMaster notificationMaster) throws IOException;

	/**
	 * 
	 * @param notificationMaster
	 * @param contextMap
	 * @return
	 * @throws IOException
	 */
	public String createEmailBody(NotificationMaster notificationMaster, Map contextMap) throws IOException;

	/**
	 * 
	 * @param notificationMaster
	 * @param contextMap
	 * @return
	 * @throws IOException
	 */
	public String createEmailSubject(NotificationMaster notificationMaster, Map contextMap) throws IOException;

	/**
	 * 
	 * @param contextmap
	 * @param ownerEntityUri
	 * @param notificationMaster
	 * @param msgBody
	 * @param phoneNumbers
	 * @throws IOException 
	 */

	public void sendWhatsAppTypeNotification(Map contextmap, String ownerEntityUri, NotificationMaster notificationMaster,
			Set<String> phoneNumbers, FieldsMetadata metadata) throws IOException;

	/**
	 * 
	 * @param contextmap
	 * @param notificationMaster
	 * @return
	 */
	public List<String> getEmailAddressOfExternalUsers(Map contextmap, NotificationMaster notificationMaster);

	/**
	 * 
	 * @param contextmap
	 * @param notificationMaster
	 * @return
	 */
	public List<String> getEmailIdsAudienceBased(Map contextmap, NotificationMaster notificationMaster);

	/**
	 * 
	 * @param userIds
	 * @param emailAddressExternalUser
	 * @param emailAddressAudienceBased
	 * @return
	 */
	public Set<String> getEmailIds(List<String> userIds, List<String> emailAddressExternalUser,
			List<String> emailAddressAudienceBased);

	/**
	 * 
	 * @param contextmap
	 * @param notificationMaster
	 * @return
	 */
	public List<String> getPhoneNumbersAudienceBased(Map contextmap, NotificationMaster notificationMaster);

	/**
	 * 
	 * @param userIds
	 * @return
	 */
	public Set<String> getInternalPhoneNumbers(List<String> userIds);

	/**
	 * 
	 * @param notificationMaster
	 * @return
	 */
	public Set<String> getExternalPhoneNumbers(NotificationMaster notificationMaster);


	
	/**
	 * 
	 * @param contextmap
	 * @param notificationMaster
	 * @return
	 */
	public List<String> getUserIds(Map contextmap, NotificationMaster notificationMaster);


	List<String> getTrustedSourceNames(Map contextmap);

	/**
	 * 
	 * @param phoneNumbersExternalUser
	 * @param internalUserMobileNumbers
	 * @param phoneNumbersAudienceBased
	 * @return
	 */
	public Set<String> getPhoneNumbers(Set<String> phoneNumbersExternalUser, Set<String> internalUserMobileNumbers,
			List<String> phoneNumbersAudienceBased);

	
}
