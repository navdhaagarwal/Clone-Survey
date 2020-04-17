/**
 * Copyright Nucleus Software India Pvt Ltd. All rights reserved.
 */
package com.nucleus.notificationMaster.service;

import static com.nucleus.cas.loan.workflow.CoreWorkflowConstants.HOLD_TEAM_LEAD;
import static com.nucleus.cas.loan.workflow.CoreWorkflowConstants.REASSIGN_TO_TEAM;
import static com.nucleus.cas.loan.workflow.CoreWorkflowConstants.REASSIGN_TO_USER;
import static com.nucleus.finnone.pro.general.util.ValidatorUtils.notNull;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Named;
import javax.mail.BodyPart;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMultipart;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.Hibernate;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.PdfStamper;
import com.nucleus.NeutrinoUUIDGenerator;
import com.nucleus.cfi.mail.service.MailMessageIntegrationService;
import com.nucleus.cfi.push.service.PushNotificationIntegrationService;
import com.nucleus.cfi.sms.pojo.ShortMessageSendResponsePojo;
import com.nucleus.cfi.sms.pojo.SmsMessage;
import com.nucleus.cfi.sms.service.ShortMessageIntegrationService;
import com.nucleus.contact.EMailInfo;
import com.nucleus.contact.PhoneNumber;
import com.nucleus.core.NeutrinoSpringAppContextUtil;
import com.nucleus.core.communication.service.CommunicationService;
import com.nucleus.core.datastore.service.DatastorageService;
import com.nucleus.core.initialization.ProductInformationLoader;
import com.nucleus.core.misc.util.DateUtils;
import com.nucleus.core.notification.CommonMailContent;
import com.nucleus.core.notification.Notification;
import com.nucleus.core.notification.service.NotificationService;
import com.nucleus.core.passwordhook.service.DocumentTemplateUtilityService;
import com.nucleus.core.validation.util.NeutrinoValidator;
import com.nucleus.dao.query.NamedQueryExecutor;
import com.nucleus.document.core.entity.Document;
import com.nucleus.entity.ApprovalStatus;
import com.nucleus.entity.EntityId;
import com.nucleus.event.EventTypes;
import com.nucleus.event.GenericEvent;
import com.nucleus.fileTemplateService.FileTemplateService;
import com.nucleus.finnone.pro.base.exception.ExceptionBuilder;
import com.nucleus.finnone.pro.base.exception.SystemException;
import com.nucleus.finnone.pro.base.utility.BeanAccessHelper;
import com.nucleus.finnone.pro.communicationgenerator.domainobject.AttachmentEncryptionPolicy;
import com.nucleus.finnone.pro.general.util.ValidatorUtils;
import com.nucleus.finnone.pro.password.service.IPasswordProvider;
import com.nucleus.logging.BaseLoggers;
import com.nucleus.mail.MailService;
import com.nucleus.mail.MimeMailMessageBuilder;
import com.nucleus.message.entity.MessageDeliveryStatus;
import com.nucleus.message.entity.ShortMessageExchangeRecord;
import com.nucleus.message.entity.WhatsAppExchangeRecord;
import com.nucleus.notificationMaster.NotificationAudienceDetails;
import com.nucleus.notificationMaster.NotificationMaster;
import com.nucleus.notificationMaster.NotificationMasterType;
import com.nucleus.pdfutility.NeutrinoPdfUtility;
import com.nucleus.persistence.EntityDao;
import com.nucleus.pushnotification.service.PushNotificationSenderService;
import com.nucleus.rules.model.SourceProduct;
import com.nucleus.security.oauth.service.RESTfulAuthenticationService;
import com.nucleus.service.BaseServiceImpl;
import com.nucleus.template.TemplateService;
import com.nucleus.user.User;
import com.nucleus.user.UserInfo;
import com.nucleus.user.UserService;
import com.nucleus.whatsApp.service.WhatsAppMessageService;

import fr.opensagres.xdocreport.template.formatter.FieldsMetadata;

/**
 * The Class NotificationMasterServiceImpl.
 *
 * @author Nucleus Software India Pvt Ltd
 */
@Transactional(propagation = Propagation.REQUIRED)
public class NotificationMasterServiceImpl extends BaseServiceImpl implements NotificationMasterService {


    private static final java.lang.String FILE_ATTACHMENT_NOT_NULL = "File Attachment not null.";

	private static final java.lang.String NON_ACRO_FORM_PDF_FILE = "Reading Bytes from Non-AcroForm pdf File.";

	private static final java.lang.String ACRO_FORM_PDF_FILE = "Reading Bytes from AcroForm pdf File.";

	private static final String            COMMUNICATION_SUBJECT = ".communication.subject";

    private static final String            COMMUNICATION_BODY    = ".communication.body";

    private static final String DELIVERED="DELIVERED";
    
    private static final String FAILED="FAILED";
    
    private static final String DELAYED="DELAYED";
    
    private static final String PENDING="PENDING";
    
    private static final String FAILED_TO_SEND = "FAILED_TO_SEND";

    private static final String UNABLE_TO_PROCESS_NOTIFICATION="Exception occured while processing Notification ";
    
    private static final String ODT="odt";
    
    private static final String PDF="pdf";
    
    public static final String ACCESS_TOKEN = "?access_token=";
    
    /** The mail service. */
    @Inject
    @Named("mailService")
    private MailService                    mailService;

    /** The mail message integration service. */
    @Inject
    @Named("mailMessageIntegrationService")
    private MailMessageIntegrationService  mailMessageIntegrationService;

    /** The user service. */
    @Inject
    @Named("userService")
    private UserService                    userService;


    /** The short message integration service. */
    @Inject
    @Named("shortMessageIntegrationService")
    private ShortMessageIntegrationService shortMessageIntegrationService;
    
 /*   
    *//** The push notification integration service. *//*
    @Inject
    @Named("pushNotificationIntegrationService")
    private PushNotificationIntegrationService pushNotificationIntegrationService;*/

    @Inject
    @Named("entityDao")
    private EntityDao                      entityDao;

    @Inject
    @Named("templateService")
    private TemplateService                templateService;

    @Inject
    @Named("fileTemplateService")
    private FileTemplateService            fileTemplateService;

    @Inject
    @Named("couchDataStoreDocumentService")
    private DatastorageService             couchDatastoreService;
    
    @Inject
    @Named("whatsAppMessageService")
    WhatsAppMessageService whatsAppMessageService;
    
    @Inject
    private BeanAccessHelper beanAccessHelper;
    

    @Inject
    @Named("notificationService")
    private NotificationService            notificationService;
    
    @Inject
    @Named("warningNotificationService")
    private WarningNotificationService     warningNotificationService;
    
    @Value(value = "#{'${underwriter.forward.mail.from}'}")
    private String                       fromEmaiId;
    
    

    /** The Constant INAPP_MAIL_FROM. */
    private static final String            INAPP_MAIL_FROM       = "system";
    
    private static final String            DOCUMENT_NAME       = "DocumentName"; 
    
    private static final String            DOCUMENT       = "Document"; 
    
    private final NeutrinoUUIDGenerator uuidGenerator = new NeutrinoUUIDGenerator(ProductInformationLoader.getProductCode() + "-");
    
    /** The role based applicant service list. */
    public List<RoleBasedApplicantService> roleBasedApplicantServiceList;
    


   
    @Inject
    @Named("InAppHelper")
    InAppMailHelper                        inAppMailHelper;

    @Inject
    @Named("communicationService")
    private CommunicationService           communicationService;

    @Inject
    @Named("messageSource")
    private MessageSource                  messageSource;
    
    @Inject
    @Named("pushNotificationSenderService")
    private PushNotificationSenderService pushNotificationSenderService;
    
    /**
     * Gets the role based applicant service list.
     *
     * @return the role based applicant service list
     */
    public List<RoleBasedApplicantService> getRoleBasedApplicantServiceList() {
        return roleBasedApplicantServiceList;
    }

    /**
     * Sets the role based applicant service list.
     *
     * @param roleBasedApplicantServiceList the new role based applicant service list
     */
    public void setRoleBasedApplicantServiceList(List<RoleBasedApplicantService> roleBasedApplicantServiceList) {
        this.roleBasedApplicantServiceList = roleBasedApplicantServiceList;
    }

    /* (non-Javadoc)
     * @see com.nucleus.core.notification.notificationMaster.NotificationMasterService#sendNotification(com.nucleus.notificationMaster.NotificationMaster, java.util.Map, java.lang.String)
     */

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public void sendNotification(NotificationMaster unmanagedNotificationMaster, Map contextmap, String ownerEntityUri,
            FieldsMetadata metadata) {
    	NotificationMaster notificationMaster = null;
        try {        	
        	notificationMaster = entityDao.find(NotificationMaster.class, unmanagedNotificationMaster.getId());
        	if(!ApprovalStatus.APPROVED_RECORD_STATUS_LIST.contains(notificationMaster.getApprovalStatus())
        			|| !notificationMaster.isActiveFlag()){
        		return;
        	}
        	
            AddOnDataProviderForNotificationGeneration addOnDataProviderForNotification = NeutrinoSpringAppContextUtil
                    .getBeanByName("addOnDataProviderForNotificationGeneration",
                            AddOnDataProviderForNotificationGeneration.class);
            addOnDataProviderForNotification.provideDataForCommunicationGeneration(notificationMaster, contextmap, metadata);
            addOnDataProviderForNotification.reInitializeLoanApplicationData(contextmap);
        } catch (NoSuchBeanDefinitionException e) {
            BaseLoggers.exceptionLogger.error("No implementation is available for interface AddOnDataProviderForNotificationGeneration moving ahead.");
        }
        NotificationMasterType notificationMasterType = notificationMaster.getNotificationMasterType();
        List<String> userIds = getUserIds(contextmap, notificationMaster);

        try {
            String msgBody = createMessageBody(contextmap, notificationMaster);

            if (notificationMasterType != null) {

                if (NotificationMasterType.EMAIL_TYPE_NOTIFICATION.equalsIgnoreCase(notificationMasterType.getCode())) {
                	// code to get external users email
            		List<String> emailAddressExternalUser = getEmailAddressOfExternalUsers(contextmap, notificationMaster);

            		// code to get role based applicant's email ids will come here
            		List<String> emailAddressAudienceBased = getEmailIdsAudienceBased(contextmap, notificationMaster);
            		// code to get internal users email
            		Set<String> userEmails = getEmailIds(userIds, emailAddressExternalUser, emailAddressAudienceBased);
                    sendEmailTypeNotification(contextmap, metadata, notificationMaster,userEmails);
					return;
                }

                // if inapp mail notification
                if (NotificationMasterType.INAPPMAIL_TYPE_NOTIFICATION.equalsIgnoreCase(notificationMasterType.getCode())) {
                    EmailVO emailVO = createEmailBodyAndSubject(notificationMaster, contextmap);
                    String subject = emailVO.getEmailSubject();
                    String emailBody = emailVO.getEmailBody();
                    CommonMailContent commonMailContent = getCommonMailContent(emailBody, subject);
                    
                    
                    List<String> userIdsAudienceBased = new ArrayList<>();
                    if (notificationMaster.getNotificationAudienceDetailsList() != null && !notificationMaster.getNotificationAudienceDetailsList().isEmpty()) {
                    	for(NotificationAudienceDetails tabDetails:notificationMaster.getNotificationAudienceDetailsList())
                    	{
                    		userIdsAudienceBased.addAll(getUserIdListBasedOnAudience(contextmap,tabDetails.getType(),tabDetails.getAudienceDetails()));
                        }
                    }
                    if (userIdsAudienceBased != null && !userIdsAudienceBased.isEmpty()) {
                    	userIds.addAll(userIdsAudienceBased);
                    }
                    addUserIdForReassign(contextmap, userIds);
                    sendNotificationMail(userIds, commonMailContent);
                    return;
                }
             // if warning notification
                if (NotificationMasterType.WARNING_TYPE_NOTIFICATION.equalsIgnoreCase(notificationMasterType.getCode())) {
                    if (contextmap.get("NotificationUser") != null) {
                        UserInfo userInfo = (UserInfo) contextmap.get("NotificationUser");
                        String userId = userInfo.getId().toString();
                        GenericEvent event = new GenericEvent(EventTypes.WORKFLOW_ASSIGN_NOTIFICATION_EVENT);
                        event.addPersistentProperty("WARNING_NOTIFICATION", notificationMaster.getAdditionalTextBox());
                        event.addNonWatcherToNotify(userId);
                        event.addPersistentProperty(GenericEvent.SUCCESS_FLAG, "success");
                        event.setAssociatedUserUri("com.nucleus.user.User:"+userId);
                        GenericEvent eventObj = entityDao.saveOrUpdate(event);
                        warningNotificationService.processWarningNotificationTask(userId, msgBody, eventObj, contextmap);
                    }else{
                        BaseLoggers.exceptionLogger.error("No user found to send Warning Notification");
                    }
                    return;
                }

                // if sms notification
                if (NotificationMasterType.SMS_TYPE_NOTIFICATION.equalsIgnoreCase(notificationMasterType.getCode())) {

                	// code to get external users phone numbers
            		Set<String> phoneNumbersExternalUser = getExternalPhoneNumbers(notificationMaster);
            		// code to get internal users phone numbers.
            		Set<String> internalUserMobileNumbers = getInternalPhoneNumbers(userIds);
            		
            		// code to get role based applicants phone numbers
            		List<String> phoneNumbersAudienceBased = getPhoneNumbersAudienceBased(contextmap, notificationMaster);
            		// combined list of phone numbers
            		Set<String> phoneNumbers = getPhoneNumbers(phoneNumbersExternalUser, internalUserMobileNumbers,
            				phoneNumbersAudienceBased);
            		
                    sendSmsTypeNotification(contextmap, ownerEntityUri, notificationMaster, msgBody,phoneNumbers);
                    return;

                }
                // if whatsApp notification
                if (NotificationMasterType.WHATSAPP_TYPE_NOTIFICATION.equalsIgnoreCase(notificationMasterType.getCode())) {

                	// code to get external users phone numbers
            		Set<String> phoneNumbersExternalUser = getExternalPhoneNumbers(notificationMaster);
            		// code to get internal users phone numbers.
            		Set<String> internalUserMobileNumbers = getInternalPhoneNumbers(userIds);
            		
            		// code to get role based applicants phone numbers
            		List<String> phoneNumbersAudienceBased = getPhoneNumbersAudienceBased(contextmap, notificationMaster);
            		// combined list of phone numbers
            		Set<String> phoneNumbers = getPhoneNumbers(phoneNumbersExternalUser, internalUserMobileNumbers,
            				phoneNumbersAudienceBased);
            		
                    sendWhatsAppTypeNotification(contextmap, ownerEntityUri, notificationMaster,phoneNumbers,metadata);
                    return;

                }
                
                if(NotificationMasterType.PUSH_TYPE_NOTIFICATION.equalsIgnoreCase(notificationMasterType.getCode())){

                    List<String> userIdsAudienceBased = new ArrayList<>();
                    if (notificationMaster.getNotificationAudienceDetailsList() != null && !notificationMaster.getNotificationAudienceDetailsList().isEmpty()) {
                        for(NotificationAudienceDetails tabDetails:notificationMaster.getNotificationAudienceDetailsList())
                        {
                            userIdsAudienceBased.addAll(getUserIdListBasedOnAudience(contextmap,tabDetails.getType(),tabDetails.getAudienceDetails()));
                        }
                    }
                    if (userIdsAudienceBased != null && !userIdsAudienceBased.isEmpty()) {
                        userIds.addAll(userIdsAudienceBased);
                    }
                    addUserIdForReassign(contextmap, userIds);
                    if (contextmap.get("pushUser") != null) {
                        userIds.add((String) contextmap.get("pushUser"));
                    }


                    processPushNotificationTask(userIds, msgBody,getTrustedSourceNames(contextmap));
                   
                
                }
            }

        } catch (Exception exception) {
        	throw ExceptionBuilder.getInstance(SystemException.class,UNABLE_TO_PROCESS_NOTIFICATION+notificationMasterType.getCode(),UNABLE_TO_PROCESS_NOTIFICATION+notificationMasterType.getCode()).setOriginalException(exception)
            .setMessage(UNABLE_TO_PROCESS_NOTIFICATION).build();      
        }

    }

	/**
	 * @param contextmap
	 * @param notificationMaster
	 * @return
	 */
    @Override
	public List<String> getUserIds(Map contextmap, NotificationMaster notificationMaster) {
		List<String> userIds = new ArrayList<>();
        
        List<String>  configuredUserIds = notificationMaster.getToUsers();
        if(ValidatorUtils.hasElements(configuredUserIds)){
              userIds.addAll(configuredUserIds); 
        }
        if(contextmap.containsKey("EscalationMatrixNotifyUser") && contextmap.get("EscalationMatrixNotifyUser") != null){
            userIds.addAll((Collection<? extends String>) contextmap.get("EscalationMatrixNotifyUser"));
        }
		return userIds;
	}

	@Override
	public List<String> getTrustedSourceNames(Map contextmap) {
		List<String> trustedSourceNames = null;

		if(contextmap.containsKey("trustedSourceNames") && contextmap.get("trustedSourceNames") != null){
			trustedSourceNames = new ArrayList<>();
			trustedSourceNames.addAll((Collection<? extends String>) contextmap.get("trustedSourceNames"));
		}
		return trustedSourceNames;
	}

	/**
	 * @param contextmap
	 * @param notificationMaster
	 * @return
	 * @throws IOException
	 */
    @Override
	public String createMessageBody(Map contextmap, NotificationMaster notificationMaster) throws IOException {
		String msgBody = "";
		if (notificationMaster.getAdditionalTextBox() != null) {
		    String msgBodyCacheKey = "additionalTextBox"
		            + notificationMaster.getEntityLifeCycleData().getLastUpdatedTimeStamp().toString();
		    msgBody = templateService.getResolvedStringFromTemplate(msgBodyCacheKey,
		            notificationMaster.getAdditionalTextBox(), contextmap);
		}
		return msgBody;
	}

	/**
	 * @param contextmap
	 * @param ownerEntityUri
	 * @param notificationMaster
	 * @param msgBody
	 * @param phoneNumbers 
	 */
    @Override
	public void sendSmsTypeNotification(Map contextmap, String ownerEntityUri, NotificationMaster notificationMaster, String msgBody, Set<String> phoneNumbers) {
		String extIdentifier = null;
		try {
			RoleBasedApplicantService roleBasedAppService = NeutrinoSpringAppContextUtil
					.getBeanByName("casRoleBasedApplicantServiceImpl", RoleBasedApplicantService.class);
			String mobNumber = roleBasedAppService.getRecipentsPhoneNumberInCaseOfOTP(contextmap);
			if (null != mobNumber) {
				phoneNumbers.add(mobNumber);
			}
			Object phoneNumObj = contextmap.get("phoneNumber");
			if (null != phoneNumObj && phoneNumObj instanceof PhoneNumber) {
				PhoneNumber phoneNumber = (PhoneNumber) phoneNumObj;
				extIdentifier = String.valueOf(phoneNumber.getId());
			}

		} catch (NoSuchBeanDefinitionException e) {
			BaseLoggers.exceptionLogger.error(
					"No implementation is available for interface RoleBasedApplicantService moving ahead.", e);
		}
		addPhoneNumbersForReassign(contextmap, phoneNumbers);
		processSMSNotificationTask(phoneNumbers, msgBody, ownerEntityUri, extIdentifier);
	}
    
    
    /**
	 * @param contextmap
	 * @param ownerEntityUri
	 * @param notificationMaster
	 * @param phoneNumbers 
     * @param metadata 
     * @throws IOException 
	 */
    @Override
	public void sendWhatsAppTypeNotification(Map contextmap, String ownerEntityUri, NotificationMaster notificationMaster, Set<String> phoneNumbers, FieldsMetadata metadata) throws IOException {
		String extIdentifier = null;
		try {
			RoleBasedApplicantService roleBasedAppService = NeutrinoSpringAppContextUtil
					.getBeanByName("casRoleBasedApplicantServiceImpl", RoleBasedApplicantService.class);
			String mobNumber = roleBasedAppService.getRecipentsPhoneNumberInCaseOfOTP(contextmap);
			if (null != mobNumber) {
				phoneNumbers.add(mobNumber);
			}

		} catch (NoSuchBeanDefinitionException e) {
			BaseLoggers.exceptionLogger.error(
					"No implementation is available for interface RoleBasedApplicantService moving ahead.", e);
		}
		
		StringBuilder dataDecryptionText=new StringBuilder();
		AttachmentEncryptionPolicy encryptionPolicy = createDataDecryptionTextAndEncryptionPolicy(notificationMaster,
				dataDecryptionText);
               
		// getting message body
		
		// code for processing attachment
		String password=getPassword(notificationMaster, contextmap, encryptionPolicy);
		File attachFile=getAttachedFileObject(notificationMaster, contextmap, password, metadata);
		if(attachFile!=null && notificationMaster.isEnablePassword() && password!=null){
		    contextmap.put(AttachmentEncryptionPolicy.PSWD_DECRPT_PLACEHOLDER, dataDecryptionText.toString());
		}else{
			contextmap.put(AttachmentEncryptionPolicy.PSWD_DECRPT_PLACEHOLDER, "");
		}
		
		String templateName = null;
        if (notificationMaster.getWhatsAppBodyDocument() != null) {
            templateName = notificationMaster.getWhatsAppBodyDocument().getDocumentStoreId();
        }
        String whatsAppBody = templateService.getResolvedStringFromFTL(templateName, contextmap);
		addPhoneNumbersForReassign(contextmap, phoneNumbers);
		whatsAppMessageService.processWhatsAppNotificationTask(phoneNumbers, whatsAppBody, ownerEntityUri, extIdentifier,attachFile);
	}

	/**
	 * @param phoneNumbersExternalUser
	 * @param internalUserMobileNumbers
	 * @param phoneNumbersAudienceBased
	 * @return
	 */
    @Override
	public Set<String> getPhoneNumbers(Set<String> phoneNumbersExternalUser, Set<String> internalUserMobileNumbers,
			List<String> phoneNumbersAudienceBased) {
		Set<String> phoneNumbers = new HashSet<>();
		if (phoneNumbersExternalUser != null) {
		    phoneNumbers.addAll(phoneNumbersExternalUser);
		}
		//internal users phone numbers.
		phoneNumbers.addAll(internalUserMobileNumbers);

		if (phoneNumbersAudienceBased != null) {
		    phoneNumbers.addAll(phoneNumbersAudienceBased);
		}
		return phoneNumbers;
	}

	/**
	 * @param contextmap
	 * @param notificationMaster
	 * @return
	 */
	@Override
	public List<String> getPhoneNumbersAudienceBased(Map contextmap, NotificationMaster notificationMaster) {
		List<String> phoneNumbersAudienceBased = new ArrayList<>();
		if (notificationMaster.getNotificationAudienceDetailsList() != null && !notificationMaster.getNotificationAudienceDetailsList().isEmpty()) {
			for(NotificationAudienceDetails tabDetails:notificationMaster.getNotificationAudienceDetailsList())
			{
				phoneNumbersAudienceBased.addAll(getPhoneNumberListBasedOnAudience(contextmap,tabDetails.getType(),tabDetails.getAudienceDetails()));
		    }
		}
		return phoneNumbersAudienceBased;
	}

	/**
	 * @param userIds
	 * @return
	 */
	@Override
	public Set<String> getInternalPhoneNumbers(List<String> userIds) {
		List<PhoneNumber> internalUsersPhoneNumbers = getEmailsOrMobileNumbersForInternalUsers("notification.users.getSmsPhoneNumbers", userIds);
		Set<String> internalUserMobileNumbers = new HashSet<>();
		for (PhoneNumber phoneNumber : internalUsersPhoneNumbers) {
			if (phoneNumber.getIsdCode() != null && phoneNumber.getPhoneNumber() != null) {
				internalUserMobileNumbers.add(phoneNumber.getIsdCode() + phoneNumber.getPhoneNumber());
			}
		}
		return internalUserMobileNumbers;
	}

	/**
	 * @param notificationMaster
	 * @return
	 */
	@Override
	public Set<String> getExternalPhoneNumbers(NotificationMaster notificationMaster) {
		Set<String> phoneNumbersExternalUser = null;
		if (notificationMaster.getNotificationAdditionalInformation() != null) {
		    List<PhoneNumber> phoneExternalUsers = notificationMaster.getNotificationAdditionalInformation()
		            .getPhoneNumber();
		    if (phoneExternalUsers != null) {

		        phoneNumbersExternalUser = new HashSet<>();
		        Hibernate.initialize(phoneExternalUsers);
		        for (PhoneNumber phoneNumber : phoneExternalUsers) {
		            String number = phoneNumber.getIsdCode() + phoneNumber.getPhoneNumber();
		            phoneNumbersExternalUser.add(StringUtils.deleteWhitespace(number));

		        }
		    }
		}
		return phoneNumbersExternalUser;
	}

	/**
	 * @param contextmap
	 * @param metadata
	 * @param notificationMaster
	 * @throws IOException
	 * @throws MessagingException
	 */
    @Override
	public void sendEmailTypeNotification(Map contextmap, FieldsMetadata metadata,
			NotificationMaster notificationMaster, Set<String> userEmails) throws IOException, MessagingException {
		StringBuilder dataDecryptionText=new StringBuilder();
		AttachmentEncryptionPolicy encryptionPolicy = createDataDecryptionTextAndEncryptionPolicy(notificationMaster,
				dataDecryptionText);
               
		// getting message body
		
		// code for processing attachment
			
		String password=getPassword(notificationMaster, contextmap, encryptionPolicy);
            
		File attachFile=getAttachedFileObject(notificationMaster, contextmap, password, metadata);
		if(attachFile!=null && notificationMaster.isEnablePassword() && password!=null){
		    contextmap.put(AttachmentEncryptionPolicy.PSWD_DECRPT_PLACEHOLDER, dataDecryptionText.toString());
		}else{
			contextmap.put(AttachmentEncryptionPolicy.PSWD_DECRPT_PLACEHOLDER, "");
		}
		EmailVO emailVO = createEmailBodyAndSubject(notificationMaster, contextmap);
		String subject = emailVO.getEmailSubject();
		String emailBody = emailVO.getEmailBody();
		addEmailForReassign(contextmap, userEmails);
		initiateManualEmail(notificationMaster, subject, emailBody, userEmails, attachFile, password, contextmap);
		return;
	}

	/**
	 * @param notificationMaster
	 * @param dataDecryptionText
	 * @return
	 */
	private AttachmentEncryptionPolicy createDataDecryptionTextAndEncryptionPolicy(
			NotificationMaster notificationMaster, StringBuilder dataDecryptionText) {
		AttachmentEncryptionPolicy encryptionPolicy=null;
		if(notificationMaster.isAttachment() && notificationMaster.isEnablePassword()){
		         encryptionPolicy=notificationMaster.getAttachmentEncryptionPolicy();
		}
		if(notificationMaster.isEnablePassword() && encryptionPolicy!=null &&
		 encryptionPolicy.getPasswordDecryptionText()!=null && 
				 notNull(ProductInformationLoader.getProductCode()) && encryptionPolicy.getPasswordDecryptionText().getSysName()!=null &&
				 ProductInformationLoader.getProductCode().equalsIgnoreCase(encryptionPolicy.getPasswordDecryptionText().getSysName().getCode())){
			dataDecryptionText.append(encryptionPolicy.getPasswordDecryptionText().getText());
		
		 }
		return encryptionPolicy;
	}

	/**
	 * @param contextmap
	 * @param notificationMaster
	 * @return
	 */
	@Override
	public List<String> getEmailAddressOfExternalUsers(Map contextmap, NotificationMaster notificationMaster) {
		List<String> emailAddressExternalUser = null;
		if (notificationMaster.getNotificationAdditionalInformation() != null) {
		    List<EMailInfo> emailExternalUsers = notificationMaster.getNotificationAdditionalInformation()
		            .getEmail();
		    if (emailExternalUsers != null) {
		        emailAddressExternalUser = new ArrayList<>();
		        Hibernate.initialize(emailExternalUsers);
		        for (EMailInfo eMailInfo : emailExternalUsers) {
		            emailAddressExternalUser.add(eMailInfo.getEmailAddress());
		        }
		    }
		}

		String emailForQuesPDF =(String)contextmap.get("emailForQuesPDF");
		if(emailForQuesPDF!=null && !emailForQuesPDF.isEmpty()) {
		    if(CollectionUtils.isNotEmpty(emailAddressExternalUser)) {
		        emailAddressExternalUser.add(emailForQuesPDF);
		    }else{
		        emailAddressExternalUser = new ArrayList<>();
		        emailAddressExternalUser.add(emailForQuesPDF);
		    }
		}
		return emailAddressExternalUser;
	}

	/**
	 * @param contextmap
	 * @param notificationMaster
	 * @return
	 */
	@Override
	public List<String> getEmailIdsAudienceBased(Map contextmap, NotificationMaster notificationMaster) {
		List<String> emailAddressAudienceBased = new ArrayList<>();
		if (notificationMaster.getNotificationAudienceDetailsList() != null && !notificationMaster.getNotificationAudienceDetailsList().isEmpty()) {
			for(NotificationAudienceDetails tabDetails:notificationMaster.getNotificationAudienceDetailsList())
			{
				emailAddressAudienceBased.addAll(getEmailListBasedOnAudience(contextmap,tabDetails.getType(),tabDetails.getAudienceDetails()));
		    }
		}
		return emailAddressAudienceBased;
	}

	/**
	 * @param userIds
	 * @param emailAddressExternalUser
	 * @param emailAddressAudienceBased
	 * @return
	 */
	@Override
	public  Set<String> getEmailIds(List<String> userIds, List<String> emailAddressExternalUser,
			List<String> emailAddressAudienceBased) {
		List<String> list = getEmailsOrMobileNumbersForInternalUsers("notification.users.getEmail", userIds);
		
		// combined list of email ids
		Set<String> userEmails = new HashSet<>();
		addNonEmptyEmail(userEmails, list);
		addNonEmptyEmail(userEmails, emailAddressExternalUser);
		addNonEmptyEmail(userEmails, emailAddressAudienceBased);
		return userEmails;
	}

    /**
     * This method is used to get mobile numbers or emails for internal users.
     * 
     * @param namedQueryName
     * @param userIds
     * @return
     */
    private <T> List<T> getEmailsOrMobileNumbersForInternalUsers(String namedQueryName, List<String> userIds) {
    	List<T> list = new ArrayList<>();
        List<Long> idList = new ArrayList<>();
        if (userIds != null && !userIds.isEmpty()) {
            for (String userId : userIds) {
                Long id = Long.valueOf(userId);
                idList.add(id);
            }
        }
        if (!idList.isEmpty()) {
            NamedQueryExecutor<T> executor = new NamedQueryExecutor<T>(namedQueryName)
                    .addParameter("userIds", idList);
            list = entityDao.executeQuery(executor);
        }
    	return list;
	}

	private void addEmailForReassign(Map contextmap, Set<String> userEmails){
        if(contextmap != null
                && ((String)contextmap.get(REASSIGN_TO_USER) != null
                || (String)contextmap.get(REASSIGN_TO_TEAM) != null
                || (String)contextmap.get(HOLD_TEAM_LEAD) != null)){
            List<RoleBasedApplicantService> roleBasedApplicantServices = getRoleBasedApplicantServiceList();
            for (RoleBasedApplicantService roleBasedApplicantService : roleBasedApplicantServices) {
                List<String> emails = roleBasedApplicantService.getEmailForReassign(contextmap);
                if (emails != null) {
                    userEmails.addAll(emails);
                }
            }
        }
    }

    private void addPhoneNumbersForReassign(Map contextmap, Set<String> userPhoneNumbers){
        if(contextmap != null
                && ((String)contextmap.get(REASSIGN_TO_USER) != null
                || (String)contextmap.get(REASSIGN_TO_TEAM) != null
                || (String)contextmap.get(HOLD_TEAM_LEAD) != null)){
            List<RoleBasedApplicantService> roleBasedApplicantServices = getRoleBasedApplicantServiceList();
            for (RoleBasedApplicantService roleBasedApplicantService : roleBasedApplicantServices) {
                List<String> phoneNumbers = roleBasedApplicantService.getPhoneNumbersForReassign(contextmap);
                if (phoneNumbers != null) {
                    userPhoneNumbers.addAll(phoneNumbers);
                }
            }
        }
    }

    private void addUserIdForReassign(Map contextmap, List<String> userIds) {
        if(contextmap != null
                && ((String)contextmap.get(REASSIGN_TO_USER) != null
                || (String)contextmap.get(REASSIGN_TO_TEAM) != null
                || (String)contextmap.get(HOLD_TEAM_LEAD) != null)){
            List<RoleBasedApplicantService> roleBasedApplicantServices = getRoleBasedApplicantServiceList();
            for (RoleBasedApplicantService roleBasedApplicantService : roleBasedApplicantServices) {

                List<String> userIdList = roleBasedApplicantService.getUserIdsForReassign(contextmap);
                if (userIdList != null) {
                    userIds.addAll(userIdList);
                }
            }
        }
    }

        List<String> getEmailListBasedOnAudience(Map contextMap, String type, String audienceDetails)
        {
        List<RoleBasedApplicantService> roleBasedApplicantServices = getRoleBasedApplicantServiceList();
        List<String> emailIds = new ArrayList<>();

        if (contextMap == null) {
            return emailIds;
        }
        if (roleBasedApplicantServices == null || roleBasedApplicantServices.isEmpty()) {
            return emailIds;
        }

        for (RoleBasedApplicantService roleBasedApplicantService : roleBasedApplicantServices) {
                 	
                 List<String> emails = roleBasedApplicantService.getEmailIdsBasedOnAudienceType(contextMap, type, audienceDetails);
            if (emails != null) {
                emailIds.addAll(emails);
            }
        }
        return emailIds;
    }
		  private  Map<String, Object> getDocumentMap(NotificationMaster notificationMaster, Map contextmap) {
			  Map<String, Object> documentMap=new HashMap<String, Object>();
			  try {
              	DocumentTemplateUtilityService documentTemplateUtilityService = NeutrinoSpringAppContextUtil
                          .getBeanByName("documentTemplateUtilityService",DocumentTemplateUtilityService.class);
              	documentMap= documentTemplateUtilityService.documentType(notificationMaster.getLetterType(),contextmap);
              } catch (NoSuchBeanDefinitionException e) {
                  BaseLoggers.exceptionLogger.error("No implementation is available for interface DocumentTemplateUtilityService moving ahead.");
              }
			  return documentMap;
		  }
		  
		  private String getPassword(NotificationMaster notificationMaster, Map contextmap,AttachmentEncryptionPolicy encryptionPolicy) {
			  String password=null;
			    if(notificationMaster.isEnablePassword()){
                    String passwordProviderBean=encryptionPolicy.getPasswordProviderBean();	
                    IPasswordProvider passwordProvider=beanAccessHelper.getBean(passwordProviderBean, IPasswordProvider.class);
                    try{
                        password=passwordProvider.computePassword(encryptionPolicy,contextmap);
                    }catch (Exception e) {
                    	BaseLoggers.exceptionLogger.error("Password was not generated by module for attachments for notification with code " +notificationMaster.getNotificationCode()+ " and URI "+ contextmap.get("uri"),e);
                    }
                }
			    return password;
		  }
		  
		  private  File getAttachedFileObject(NotificationMaster notificationMaster, Map contextmap,String password,FieldsMetadata metadata) throws IOException{
			  File attachFile=null;
		      if(notificationMaster.isAttachment() && notificationMaster.isAttachmentOption()){
		    	    
                  Map<String, Object> documentMap=getDocumentMap(notificationMaster, contextmap);

                
                  if(documentMap!=null && documentMap.get(DOCUMENT_NAME)!=null && documentMap.get(DOCUMENT)!=null)
                     {
                  	String document=(String)documentMap.get(DOCUMENT_NAME);
                  	String documentName=document.concat(".pdf");
                  	Document latestDocument = (Document)documentMap.get(DOCUMENT);
                  	attachFile=encryptAttchedFile(latestDocument, documentName, password);
	                      
                     }
                  }
                  else if(notificationMaster.isAttachment() && !(notificationMaster.isAttachmentOption())){
                            attachFile = processAttachedFile(notificationMaster, contextmap, metadata,password); 
                  }
		      
		      return attachFile;
		  }
		  
		  private  File encryptAttchedFile(Document latestDocument ,String documentName,String password) throws IOException{
			         File attachFile=null;
                     File file;
                     if(latestDocument!=null){
                  	  attachFile=new File(documentName);
                     file = couchDatastoreService.retriveDocument(latestDocument.getDocumentStoreId());
                     if(StringUtils.isNotEmpty(password)){
	                       byte[] byte12=FileUtils.readFileToByteArray(file);
	                       byte[] output1=NeutrinoPdfUtility.encryptPdfContent(byte12,password,password);
	                       FileUtils.writeByteArrayToFile(attachFile, output1);
	                       }
	                       else{
	                    	  byte[] byte12=FileUtils.readFileToByteArray(file);
	                    	  FileUtils.writeByteArrayToFile(attachFile, byte12);
	                       }			
                     }
                     return attachFile;          
          }

		  
        private void addNonEmptyEmail(Set<String> addEmailId, List<String> emailIds) {
            if(CollectionUtils.isNotEmpty(emailIds)) {
                for(String email : emailIds) {
                    if(StringUtils.isNotBlank(email)) {
                        addEmailId.add(email);
                    }
                }
            }
        }
        
      public  List<String> getUserIdListBasedOnAudience(Map contextMap, String type, String audienceDetails)
        {
        List<RoleBasedApplicantService> roleBasedApplicantServices = getRoleBasedApplicantServiceList();
        List<String> userIds = new ArrayList<String>();

        if (contextMap == null) {
            return userIds;
        }
        if (roleBasedApplicantServices == null || roleBasedApplicantServices.isEmpty()) {
            return userIds;
        }

        for (RoleBasedApplicantService roleBasedApplicantService : roleBasedApplicantServices) {
                 	
                 List<String> userIds1 = roleBasedApplicantService.getUserIdsBasedOnAudienceType(contextMap, type, audienceDetails);
            if (userIds1 != null) {
            	userIds.addAll(userIds1);
            }
        }
        return userIds;
    }
        public List<String> getPhoneNumberListBasedOnAudience(Map contextMap, String type, String audienceDetails){
           
        List<RoleBasedApplicantService> roleBasedApplicantServices = getRoleBasedApplicantServiceList();
        List<String> phoneNumberList = new ArrayList<String>();

        if (contextMap == null) {
            return phoneNumberList;
        }

        if (roleBasedApplicantServices == null || roleBasedApplicantServices.isEmpty()) {
            return phoneNumberList;
        }

        for (RoleBasedApplicantService roleBasedApplicantService : roleBasedApplicantServices) {
                 List<String> phoneNumbers = roleBasedApplicantService.getPhoneNumberBasedOnAudienceType(contextMap,type,audienceDetails);
            if (phoneNumbers != null) {
                phoneNumberList.addAll(phoneNumbers);
            }
        }

        return phoneNumberList;

    }
    
        
    /**
     * @param notificationMaster
     * @param subject
     * @param emailBody
     * @param userEmails
     * @param attachFile
     * @param password
     * @param contextmap
     * @throws MessagingException
     */
    private void initiateManualEmail(NotificationMaster notificationMaster,String subject, String emailBody, Set<String> userEmails, File attachFile, String password, Map contextmap) throws MessagingException{
    	try {
        	if(!(notificationMaster.isEnablePassword())||(notificationMaster.isEnablePassword() && password!=null)){
        	          sendManualEmail(subject, emailBody, userEmails,attachFile);
        	}else{
        		BaseLoggers.exceptionLogger.error("Mail not sent. password was not generated by module for attachments for notification with code " +notificationMaster.getNotificationCode()+ " and URI "+ contextmap.get("uri"));
        	}
        }
        catch (IOException e) {
            BaseLoggers.exceptionLogger.error("Exception occured while reading/writing file" + e.getMessage());
        }
    }
    
    @Override
    public void sendManualEmail(String subject, String emailBody, Set<String> userEmails, File attachFile)
            throws MessagingException, IOException {
        MimeMailMessageBuilder mimeMailMessageBuilder = getSmtpMailBuilder(emailBody, subject, attachFile);
        sendMailToMailIds(userEmails, mimeMailMessageBuilder);
    }

    /**
     * Send mail to mail ids.
     *
     * @param emailAddressList the email address list
     * @param mimeMailMessageBuilder the mime mail message builder
     * @throws IOException 
     * @throws MessagingException 
     */
    private void sendMailToMailIds(Set<String> emailAddressList, MimeMailMessageBuilder mimeMailMessageBuilder)
            throws MessagingException, IOException {

        String[] emailArr = null;
        if (emailAddressList != null) {
            int emailListSize = emailAddressList.size();
            if (emailListSize > 0) {
                emailArr = emailAddressList.toArray(new String[emailListSize]);
                mimeMailMessageBuilder.setTo(emailArr);
                mailMessageIntegrationService.sendMailMessageToIntegrationAsynchronously(mimeMailMessageBuilder.getMimeMessage());

            }
        }
    }

    /**
     * Gets the smtp mail builder.
     *
     * @param body the body
     * @return the smtp mail builder
     * @throws IOException 
     */
    private MimeMailMessageBuilder getSmtpMailBuilder(String body, String subject, File file) 

	throws IOException {

		MimeMailMessageBuilder mimeMailMessageBuilder = mailService
				.createMimeMailBuilder();
		Map<String, String> keyMap = new HashMap<>();
		if (file != null) {
			return mimeMailMessageBuilder.setFrom(fromEmaiId)
					.setSubject(subject).setHtmlBody(body)
					.addAttachment(file.getName(), file);
		} else {
			try {
			mimeMailMessageBuilder.getMimeMessage().setHeader("Content-Type", "text/html; charset=UTF-8");
			mimeMailMessageBuilder.setSubject(subject);
			mimeMailMessageBuilder.setFrom(fromEmaiId);
			BodyPart messageBodyPart = new MimeBodyPart();
			messageBodyPart.setContent(body, "text/html; charset=utf-8");
			Multipart multipart = new MimeMultipart();
			multipart.addBodyPart(messageBodyPart);
		
			mimeMailMessageBuilder.getMimeMessage().setContent(multipart);
		
		
			} catch (MessagingException e) {
		
				BaseLoggers.exceptionLogger.error	("Exception occured while processing mimeMailMessageBuilder '"
								+ e.getMessage() + "'");
			}
		
		}
		return mimeMailMessageBuilder;

	}



    /**
     * Send notification mail.
     *
     * @param userIdsForMail the user ids for mail
     * @param commonMailContent the common mail content
     */
    public void sendNotificationMail(List<String> userIdsForMail, CommonMailContent commonMailContent) {

        Set<String> userUris = new HashSet<>();
        if (userIdsForMail != null && !userIdsForMail.isEmpty()) {

            for (String userId : userIdsForMail) {

                String userUri = EntityId.getUri(Long.parseLong(userId), User.class);
                userUris.add(userUri);

            }
            inAppMailHelper.sendNotificationAndCorporateEmails(userUris, commonMailContent, true, null);

        }
    }
    /**
     * Gets the common mail content.
     *
     * @param body the body
     * @return the common mail content
     */
    /**
     * Gets the common mail content.
     *
     * @param body the body
     * @return the common mail content
     */
    public CommonMailContent getCommonMailContent(String body, String subject) {

        CommonMailContent commonMailContent = new CommonMailContent();
        String systemUserUri = userService.getUserFromUsername(INAPP_MAIL_FROM).getUserEntityId().getUri();
        commonMailContent.setFromUserUri(systemUserUri);
        commonMailContent.setMsgSentTimeStamp(DateUtils.getCurrentUTCTime());
        commonMailContent.setSubject(subject);
        commonMailContent.setBody(body);
        commonMailContent.getEntityLifeCycleData().setCreatedByUri(systemUserUri);

        return commonMailContent;
    }

    /**
     * Process popup notification task.
     *
     * @param userIdsForPopupNotification the user ids for popup notification
     * @param body the body
     */
    public void processPopupNotificationTask(List<String> userIdsForPopupNotification, String body) {

        if (userIdsForPopupNotification != null && !userIdsForPopupNotification.isEmpty()) {
            Set<String> userUris = new HashSet<>();
            for (String userId : userIdsForPopupNotification) {

                String userUri = userService.getUserById(Long.parseLong(userId)).getUserEntityId().getUri();
                userUris.add(userUri);

            }


            List<Notification> notifications = new ArrayList<>();
            NeutrinoValidator.notNull(userUris, "Notification user set can not be null");
            for (String userUri : userUris) {
                Notification notification = new Notification();
                notification.setNotificationUserUri(userUri);
                notification.setNotificationType("SUCCESS_FLAG");
                notification.setSeen(false);
                notifications.add(notification);
            }
            for (Notification notification : notifications) {
                entityDao.saveOrUpdate(notification);
            }
        }

    }

    /**
     * Process sms notification task.
     *
     * @param phoneNumbers the phone numbers
     * @param body the body
     * @param ownerEntityUri the owner entity uri
     */

	@Override
	public void processSMSNotificationTask(Set<String> phoneNumbers, String body, String ownerEntityUri) {
		processSMSNotificationTask(phoneNumbers, body, ownerEntityUri, null);
	}

    private void processSMSNotificationTask(Set<String> phoneNumbers, String body, String ownerEntityUri, String extIdentifier) {

    	if (!phoneNumbers.isEmpty()) {
            for (String phonenumString : phoneNumbers) {
                SmsMessage smsMessage = new SmsMessage();
                smsMessage.setTo(phonenumString);
                smsMessage.setBody(body);
                smsMessage.setUniqueRequestId(uuidGenerator.generateUuid());
                ShortMessageSendResponsePojo smsSendResponsePojo = shortMessageIntegrationService.sendShortMessageAsynchronously(smsMessage);
                ShortMessageExchangeRecord exchangeRecord = new ShortMessageExchangeRecord();
                exchangeRecord.setOwnerEntityUri(ownerEntityUri);
                exchangeRecord.setSmsBody(body);
                exchangeRecord.setSmsTo(phonenumString);
                exchangeRecord.setUniqueRequestId(smsMessage.getUniqueRequestId());
                exchangeRecord.setExtIdentifier(extIdentifier);
                if (ValidatorUtils.notNull(smsSendResponsePojo)) {
                	setDeliveryStatusinExchangeRecord(exchangeRecord,smsSendResponsePojo);				
                    exchangeRecord.setDeliveryTimestamp(smsSendResponsePojo.getReceiptTimestamp());
                    exchangeRecord.setMessageReceiptId(smsSendResponsePojo.getMessageReceiptId());                   
                    exchangeRecord.setStatusMessage(smsSendResponsePojo.getMessageStatus());
                }
				if (StringUtils.isNumeric(extIdentifier)) {
					PhoneNumber phoneNumber = entityDao.find(PhoneNumber.class, Long.parseLong(extIdentifier));
					if (null != phoneNumber) {						
						phoneNumber.setVerCodeDeliveryStatus(null == exchangeRecord.getDeliveryStatus()
								? MessageDeliveryStatus.PENDING : exchangeRecord.getDeliveryStatus());
						phoneNumber.setVerCodeDelStatusMessage(exchangeRecord.getStatusMessage());
						entityDao.update(phoneNumber);
					}
				}
                entityDao.persist(exchangeRecord);
            }
        }
    }

    
   
	

    /**
     * Process sms notification task.
     *
     * @param fcmIds the phone numbers
     * @param body the body
     * @param ownerEntityUri the owner entity uri
     */
    public void processFCMNotificationTask(List<String> fcmIds, String body, String ownerEntityUri) {
    	/*
    	        if (!fcmIds.isEmpty()) {
    	            for (String fcmId : fcmIds) {
    	                PushNotification pushNotification = new PushNotification();
    	                pushNotification.setDeviceId(fcmId);
    	                pushNotification.setBody(body);
    	                pushNotification.setUniqueRequestId(uuidGenerator.generateUuid());
    	                PushNotificationResponsePojo pushNotificationPojo = pushNotificationIntegrationService.sendPushNotificationAsynchronously(pushNotification);
    	                PushNotificationExchangeRecord exchangeRecord = new PushNotificationExchangeRecord();
    	                exchangeRecord.setOwnerEntityUri(ownerEntityUri);
    	                exchangeRecord.setPushMessageBody(body);
    	                exchangeRecord.setPushToDeviceId(fcmId);
    	                exchangeRecord.setUniqueRequestId(pushNotification.getUniqueRequestId());
    	                if (ValidatorUtils.notNull(pushNotificationPojo)) {
    	                    setDeliveryStatusinExchangeRecord(exchangeRecord,pushNotificationPojo);
    	                    exchangeRecord.setDeliveryTimestamp(pushNotificationPojo.getReceiptTimestamp());
    	                    exchangeRecord.setMessageReceiptId(pushNotificationPojo.getMessageReceiptId());
    	                    exchangeRecord.setPushStatusMessage(pushNotificationPojo.getMessageStatus());
    	                }
    	                entityDao.persist(exchangeRecord);
    	            }
    	        }*/
    	    
    }
    
    private void processPushNotificationTask(List<String> userIds, String body,List<String> trustedSourceNames) {
    	List<Long> idList = new ArrayList<>();
        if (userIds != null && !userIds.isEmpty()) {
            for (String userId : userIds) {
                Long id = Long.valueOf(userId);
                idList.add(id);
            }
        }
    	pushNotificationSenderService.sendNotificationToSpecificUsersByUserIdAndTrustedSourceModules(body, idList,trustedSourceNames);
    }
    
    
    private void setDeliveryStatusinExchangeRecord(
			ShortMessageExchangeRecord exchangeRecord,
			ShortMessageSendResponsePojo messageSendResponsePojo) {
    	
        if(messageSendResponsePojo.getDeliveryStatus().equals(DELIVERED))
        	exchangeRecord.setDeliveryStatus(MessageDeliveryStatus.DELIVERED);
        else if(messageSendResponsePojo.getDeliveryStatus().equals(DELAYED))
        	exchangeRecord.setDeliveryStatus(MessageDeliveryStatus.DELAYED);
		else if (messageSendResponsePojo.getDeliveryStatus().equals(FAILED)
				|| messageSendResponsePojo.getDeliveryStatus().equals(FAILED_TO_SEND))
			exchangeRecord.setDeliveryStatus(MessageDeliveryStatus.FAILED);
        else if(messageSendResponsePojo.getDeliveryStatus().equals(PENDING))
        	exchangeRecord.setDeliveryStatus(MessageDeliveryStatus.PENDING);
        else
        	exchangeRecord.setDeliveryStatus(MessageDeliveryStatus.NOT_APPLICABLE);
		
	}
    
    
    
  /* private void setDeliveryStatusinExchangeRecord(
            PushNotificationExchangeRecord exchangeRecord,
            PushNotificationResponsePojo messageSendResponsePojo) {
        
        if(messageSendResponsePojo.getDeliveryStatus().equals(DELIVERED))
            exchangeRecord.setDeliveryStatus(MessageDeliveryStatus.DELIVERED);
        else if(messageSendResponsePojo.getDeliveryStatus().equals(DELAYED))
            exchangeRecord.setDeliveryStatus(MessageDeliveryStatus.DELAYED);
        else if(messageSendResponsePojo.getDeliveryStatus().equals(FAILED))
            exchangeRecord.setDeliveryStatus(MessageDeliveryStatus.FAILED);
        else if(messageSendResponsePojo.getDeliveryStatus().equals(PENDING))
            exchangeRecord.setDeliveryStatus(MessageDeliveryStatus.PENDING);
        else
            exchangeRecord.setDeliveryStatus(MessageDeliveryStatus.NOT_APPLICABLE);
        
    }*/

	@Override
    public List<Map<String, Object>> findNotificationMasterByNotificationType(String notificationType) {

        NeutrinoValidator.notNull(notificationType, "Notification Type cannot be null");
        List<Integer> statusList = new ArrayList<>();
        statusList.add(ApprovalStatus.APPROVED);
        statusList.add(ApprovalStatus.APPROVED_MODIFIED);
        NamedQueryExecutor<Map<String, Object>> queryExecutor = new NamedQueryExecutor<Map<String, Object>>(
                "NotificationMaster.getNotificationMasterByNotificationType").addParameter("notificationType",
                notificationType).addParameter("statusList", statusList);
        return entityDao.executeQuery(queryExecutor);

    }
	
	@Override
    public List<AttachmentEncryptionPolicy> findAttachmentEncryptionPolicyBySourceProduct(SourceProduct sourceProduct) {

        NeutrinoValidator.notNull(sourceProduct, "Source Product cannot be null");
        NamedQueryExecutor<AttachmentEncryptionPolicy> queryExecutor = new NamedQueryExecutor<AttachmentEncryptionPolicy>(
                "NotificationMaster.getAttachmentEncryptionPolicyBySourceProduct").addParameter("sourceProduct",
                		sourceProduct);
        return entityDao.executeQuery(queryExecutor);

    }

    @SuppressWarnings("rawtypes")
    @Override
    public void sendManualEmailCommunication(NotificationMaster notificationMaster, List<String> emailIdList,
            FieldsMetadata metadata, Map contextmap) throws MessagingException, IOException {

        EmailVO emailVO = createEmailBodyAndSubject(notificationMaster, contextmap);

        String subject = emailVO.getEmailSubject();
        String emailBody = emailVO.getEmailBody();

        // code for processing attachment
        File attachFile = processAttachedFile(notificationMaster, contextmap, metadata,null);
        MimeMailMessageBuilder mimeMailMessageBuilder = getSmtpMailBuilder(emailBody, subject, attachFile);
        sendMailToMailIds(new HashSet<String>(emailIdList), mimeMailMessageBuilder);
        return;

    }

    @SuppressWarnings("rawtypes")
    @Override
    public void sendManualPhoneCommnication(NotificationMaster notificationMaster, Map contextMap,
            List<String> phoneNumbers, String ownerEntityUri) throws IOException {

        String msgBody = createMessageBody(contextMap, notificationMaster);

        if (phoneNumbers != null && !phoneNumbers.isEmpty()) {
            processSMSNotificationTask(new HashSet<String>(phoneNumbers), msgBody, ownerEntityUri);
        }
        return;

    }

    /**
     * 
     * This method is used to resolve the email subject and email body of notificationMaster and will set these field in EmailVO and then
     * will return this emailVO.
     * @param notificationMaster
     * @param contextMap
     * @return
     * @throws IOException
     */

    private EmailVO createEmailBodyAndSubject(NotificationMaster notificationMaster, Map contextMap) throws IOException {
        EmailVO emailVO = new EmailVO();

        // getting mail subject
        String subject = createEmailSubject(notificationMaster, contextMap);

        // Get Email Body
        String emailBody = createEmailBody(notificationMaster, contextMap);
        
        emailVO.setEmailBody(emailBody);
        emailVO.setEmailSubject(subject);
        return emailVO;

    }

	/**
	 * @param notificationMaster
	 * @param contextMap
	 * @return
	 * @throws IOException
	 */
    @Override
	public String createEmailBody(NotificationMaster notificationMaster, Map contextMap) throws IOException {
		String emailBody = "";
        if (notificationMaster.getEmailBodyType().equals(NotificationMaster.InlineTextBody)) {
        	String lastUpdatedTimeStamp = "";
        	String reviewedTimeStamp = "";
        	if(notificationMaster.getEntityLifeCycleData() != null && notificationMaster.getEntityLifeCycleData().getLastUpdatedTimeStamp() != null){
        		lastUpdatedTimeStamp = notificationMaster.getEntityLifeCycleData().getLastUpdatedTimeStamp().toString();
			}
			if(notificationMaster.getMasterLifeCycleData() != null && notificationMaster.getMasterLifeCycleData().getReviewedTimeStamp() != null){
				reviewedTimeStamp = notificationMaster.getMasterLifeCycleData().getReviewedTimeStamp().toString();
			}
            String emailBodyCacheKey = "templateText" + lastUpdatedTimeStamp + reviewedTimeStamp;
            emailBody = templateService.getResolvedStringFromTemplate(emailBodyCacheKey,
                    notificationMaster.getTemplateText(), contextMap);
        } else if (notificationMaster.getEmailBodyType().equals(NotificationMaster.UploadedEmailBody)) {
            String templateName = null;
            if (notificationMaster.getEmailBodyDocument() != null) {
                templateName = notificationMaster.getEmailBodyDocument().getDocumentStoreId();
            }
            emailBody = templateService.getResolvedStringFromFTL(templateName, contextMap);
        }
		return emailBody;
	}

	/**
	 * @param notificationMaster
	 * @param contextMap
	 * @return
	 * @throws IOException
	 */
    @Override
    public String createEmailSubject(NotificationMaster notificationMaster, Map contextMap) throws IOException {
		String subject = "";
        if (notificationMaster.getMailSubject() != null) {
            String subjectCacheKey = "mailSubject"
                    + notificationMaster.getEntityLifeCycleData().getLastUpdatedTimeStamp().toString();
            subject = templateService.getResolvedStringFromTemplate(subjectCacheKey, notificationMaster.getMailSubject(),
                    contextMap);
        }
		return subject;
	}

    /**
     * 
     * This method is used for resolving the template attached in notificationMaster with the help of contextMap
     * @param notificationMaster
     * @param contextMap
     * @param metadata
     * @param password 
     * @return
     * @throws IOException
     */

    private File processAttachedFile(NotificationMaster notificationMaster, Map contextMap, FieldsMetadata metadata, String password)
			throws IOException {
		
    	// code for processing attachment
		File attachFile = null;
		
		Document document = notificationMaster.getAttachedDocument();
		
		if (document == null) {
			return attachFile;
		}
		// there are three cases
		// 1. odt file - with parameters or static text
		// 2. acroform file -pdf file
		// other files - assumed to be static like xls
		String fileName = document.getUploadedFileName();
		String extension = FilenameUtils.getExtension(fileName);
		String mimeType;
		
		
		if (ODT.equalsIgnoreCase(extension)) {
			
			byte[] fileBytes = processODT(notificationMaster, contextMap, metadata);
			if (fileBytes != null) {
				fileName = FilenameUtils.removeExtension(fileName);
				attachFile = new File(fileName + ".pdf");
				if (StringUtils.isNotEmpty(password)) {
					fileBytes = NeutrinoPdfUtility.encryptPdfContent(fileBytes, password, password);
				}
				FileUtils.writeByteArrayToFile(attachFile, fileBytes);
			}
			mimeType="application/pdf";
		
		} else if (PDF.equalsIgnoreCase(extension)) {
			// process acroform with PDFDocumentGenerator in core
			// reporting
			byte[] bytes = processAcroForms(notificationMaster, contextMap);
			if (bytes != null && bytes.length>0) {
				attachFile = new File(fileName);
				if (StringUtils.isNotEmpty(password)) {
					bytes = NeutrinoPdfUtility.encryptPdfContent(bytes, password, password);
				}
				FileUtils.writeByteArrayToFile(attachFile, bytes);
			}
			mimeType="application/pdf";
		
		} else {
			
			attachFile = new File(fileName);
			File file = couchDatastoreService.retriveDocument(notificationMaster.getAttachedDocument().getDocumentStoreId());
			FileUtils.copyFile(file, attachFile);
			mimeType=notificationMaster.getAttachedDocument().getContentType();
		}
		if(attachFile != null) {
			BaseLoggers.flowLogger.debug(FILE_ATTACHMENT_NOT_NULL);
			InputStream ios = new FileInputStream(attachFile);		
			byte[]  commBytes =IOUtils.toByteArray(ios);
			String loanApplicationUri=null;
			
			if(contextMap.get("contextObjectLoanApplicationUri")!=null)
			    loanApplicationUri=(java.lang.String) contextMap.get("contextObjectLoanApplicationUri");
			
			communicationService.persistDynamicGeneratedEmailAttachment(commBytes, attachFile, loanApplicationUri, contextMap,null,mimeType);
		}
		return attachFile;
	}
    
    
	private byte[] processODT(NotificationMaster notificationMaster, Map<Object, Object> contextMap,
			FieldsMetadata metadata) throws FileNotFoundException {

		byte[] fileBytes = (byte[]) fileTemplateService.generateDocumentFromKey(
				notificationMaster.getAttachedDocument().getDocumentStoreId(), contextMap, metadata);

		if (fileBytes == null) {
			File file = couchDatastoreService
					.retriveDocument(notificationMaster.getAttachedDocument().getDocumentStoreId());
			if (file != null) {
				try (InputStream inputStream = new FileInputStream(file)) {
					fileBytes = (byte[]) fileTemplateService.generateDocument(inputStream,
							notificationMaster.getAttachedDocument().getDocumentStoreId(), contextMap, metadata);
				} catch (IOException e) {
					throw new SystemException("IO Exception while processing ODT in notification master "
							+ notificationMaster.getNotificationCode(), e);
				}
			}
		}
		return fileBytes;

	}
    
	private byte[] processAcroForms(NotificationMaster notificationMaster, Map<Object, Object> contextMap) {

		ByteArrayOutputStream out = new ByteArrayOutputStream();
		File file = couchDatastoreService
				.retriveDocument(notificationMaster.getAttachedDocument().getDocumentStoreId());

		if (file == null) {
			return out.toByteArray();
		}
		byte[] fileAttachmentByteArray;
		try (InputStream inputStream = new FileInputStream(file)) {

			PdfReader pdfReader = new PdfReader(inputStream);
			PdfStamper pdfStamper = new PdfStamper(pdfReader, out);
			for (Map.Entry<Object, Object> entry : contextMap.entrySet()) {
				if (entry.getKey() != null && entry.getValue() != null) {
					pdfStamper.getAcroFields().setField(entry.getKey().toString(), entry.getValue().toString());
				}
			}
			if(pdfStamper.getAcroFields().getFields() != null && !pdfStamper.getAcroFields().getFields().isEmpty()) {
				BaseLoggers.flowLogger.debug(ACRO_FORM_PDF_FILE);
				pdfStamper.getAcroFields().setGenerateAppearances(false);
				pdfStamper.close();
				fileAttachmentByteArray = out.toByteArray();
			} else {
				BaseLoggers.flowLogger.debug(NON_ACRO_FORM_PDF_FILE);
				fileAttachmentByteArray = Files.readAllBytes(file.toPath());
			}
		} catch (Exception e) {
			throw new SystemException("IO Exception while processing AcroForm in notification master "
					+ notificationMaster.getNotificationCode(), e);
		}

		return fileAttachmentByteArray;
	}

    // code for sending Email from EmailEventListener
    @Override
    public void constructEmailAndSend(List<String> emailIdList, Map contextmap, String emailSubject, String emailBoby,
            Document emailAttachedDocument) throws IOException, MessagingException {

        // Attached file for Email when using EmailEventWorker for sending Email
        MimeMailMessageBuilder mimeMailMessageBuilder = getSmtpMailBuilder(emailIdList, contextmap, emailSubject, emailBoby,
                emailAttachedDocument);
        sendEmail(mimeMailMessageBuilder);
        return;

    }

    public MimeMailMessageBuilder getSmtpMailBuilder(List<String> emailAddressList, Map contextmap, String emailSubject,
            String emailBoby, Document emailAttachedDocument) throws IOException, MessagingException {
        File attachedFile = attchFileWithEmail(emailAttachedDocument, contextmap);
        MimeMailMessageBuilder mimeMailMessageBuilder = getSmtpMailBuilder(emailBoby, emailSubject, attachedFile);
        String[] emailArr;
        if (emailAddressList != null) {
            int emailListSize = emailAddressList.size();
            if (emailListSize > 0) {
                emailArr = emailAddressList.toArray(new String[emailListSize]);
                mimeMailMessageBuilder.setTo(emailArr);
            }
        }
        return mimeMailMessageBuilder;
    }

    public void sendEmail(MimeMailMessageBuilder builder) throws IOException, MessagingException {
        mailMessageIntegrationService.sendMailMessageToIntegrationAsynchronously(builder.getMimeMessage());
    }

    private File attchFileWithEmail(Document document, Map contextMap) throws IOException {

        FieldsMetadata metadata = null;
        // code for processing attachment
        File attachFile = null;
        if (document != null) {
            byte[] fileBytes = (byte[]) fileTemplateService.generateDocumentFromKey(document.getDocumentStoreId(),
                    contextMap, metadata);

            if (fileBytes == null) {
                File file = couchDatastoreService.retriveDocument(document.getDocumentStoreId());
                if (file != null) {
                    InputStream inputStream = new FileInputStream(file);
                    fileBytes = (byte[]) fileTemplateService.generateDocument(inputStream, document.getDocumentStoreId(),
                            contextMap, metadata);
                }
            }
            attachFile = new File(document.getUploadedFileName());
            FileUtils.writeByteArrayToFile(attachFile, fileBytes);

        }
        return attachFile;

    }
	
	@Override
    public void sendAutoCommunicationWithTemplate(String templateName, boolean sendMail, Map contextMap, String ownerUri,
            byte[] documentToCreate) {
    	sendAutoCommunicationWithTemplate( templateName,  sendMail,  contextMap,  ownerUri,
                 documentToCreate, null);
        }

    @Override
    public void sendAutoCommunicationWithTemplate(String templateName, boolean sendMail, Map contextMap, String ownerUri,
            byte[] documentToCreate,String suffix) {

        // call to communication service to persist the communication

        if (StringUtils.isBlank(templateName)) {
            return;
        }
        String baseKeyName = templateName.replaceAll("_", ".");

        try {

            Document document = communicationService.persistEmailCommunication(documentToCreate, baseKeyName, ownerUri,
                    contextMap,suffix);

            if (sendMail) {

                String emailBody = templateService.getResolvedStringFromResourceBundle(baseKeyName + COMMUNICATION_BODY, null, null);
                emailBody = messageSource.getMessage(emailBody, null, null, getUserLocale());
                String emailSubject = templateService.getResolvedStringFromResourceBundle(baseKeyName + COMMUNICATION_SUBJECT, null, null);
                emailSubject = messageSource.getMessage(emailSubject, null, null, getUserLocale());

                String mailTo =templateService.getResolvedStringFromResourceBundle(baseKeyName + COMMUNICATION_SUBJECT, null, null);
                mailTo = messageSource.getMessage(mailTo, null, null, getUserLocale());
                List<String> emailIdList = new ArrayList<>();
                emailIdList.add(mailTo);

                constructEmailAndSend(emailIdList, contextMap, emailSubject, emailBody, document);

            }

        } catch (IOException e) {
            BaseLoggers.exceptionLogger.error("Exception while saving Communication Or Sending Email", e);
        } catch (MessagingException e) {
            BaseLoggers.exceptionLogger.error("Exception while  Sending Email", e);
        }

    }

    

    /**
     * Process warning notification task.
     *
     * @param body the body
     * @param eventObj the generic event
     */
    public void processWarningNotificationTask(String userIdForPopupNotification, String body,GenericEvent eventObj,Map contextmap) {
	
        if (userIdForPopupNotification != null && !userIdForPopupNotification.isEmpty()) {
                String userUri = userService.getUserById(Long.parseLong(userIdForPopupNotification)).getUserEntityId().getUri();

            NeutrinoValidator.notNull(userUri, "Notification user set can not be null");
                Notification notification = new Notification();
                notification.setNotificationUserUri(userUri);
                notification.setNotificationType("warning");
                notification.setSeen(false);
                notification.setGenericEvent(eventObj);
                notification.setEventType(eventObj.getEventType());
                entityDao.saveOrUpdate(notification);
        }else{
            BaseLoggers.exceptionLogger.error("No user found to send Warning Notification");
            }

    }
    

}
