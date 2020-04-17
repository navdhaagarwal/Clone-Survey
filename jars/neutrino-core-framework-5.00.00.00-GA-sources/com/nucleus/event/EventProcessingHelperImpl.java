/**
 * This file and a proportion of its content is copyright of Nucleus Software Exports Limited - © 2012. All rights reserved.
 * Any redistribution or reproduction of part or all of the contents in any form is prohibited other than the following:
 * - you cannot print or download to a local hard disk extract contents either part or full for personal/ commercial/
 * academic or any other use
 * - you may not copy the content to individual/ third parties for any type of use, either as compiled or source format
 * without the knowledge and consent of Nucleus Software
 * - You may not, except with our express written permission, distribute or commercially exploit the content. Nor may you
 * transmit it or store it in any other web site or other form of electronic retrieval system.
 */
package com.nucleus.event;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Executor;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;

import com.nucleus.cfi.mail.service.MailMessageIntegrationService;
import com.nucleus.cfi.sms.pojo.ShortMessageSendResponsePojo;
import com.nucleus.cfi.sms.pojo.SmsMessage;
import com.nucleus.cfi.sms.service.ShortMessageIntegrationService;
import com.nucleus.config.persisted.service.ConfigurationService;
import com.nucleus.config.persisted.vo.ConfigurationVO;
import com.nucleus.core.itemwatch.service.ItemWatcherService;
import com.nucleus.core.misc.util.DateUtils;
import com.nucleus.core.notification.CommonMailContent;
import com.nucleus.core.notification.Notification;
import com.nucleus.core.notification.service.NotificationService;
import com.nucleus.entity.EntityId;
import com.nucleus.external.mail.link.service.ExternalMailLinkService;
import com.nucleus.mail.MailService;
import com.nucleus.mail.MimeMailMessageBuilder;
import com.nucleus.message.entity.MessageDeliveryStatus;
import com.nucleus.message.entity.ShortMessageExchangeRecord;
import com.nucleus.notificationMaster.service.InAppMailHelper;
import com.nucleus.service.BaseServiceImpl;
import com.nucleus.template.TemplateService;
import com.nucleus.user.UserService;

/**
 * Class to perform some operations to avoid multi threading issues with
 * persistence and transactions.
 *
 * @author Nucleus Software Exports Limited
 *
 */
@Named("eventProcessingHelper")
public class EventProcessingHelperImpl extends BaseServiceImpl implements EventProcessingHelper {

    private static final Logger            LOGGER                   = LoggerFactory
                                                                            .getLogger(EventProcessingHelperImpl.class);

    private static final boolean           INTERNAL_USE             = true;
    private static final boolean           EXTERNAL_USE             = false;

    private static final String           USER_URI_PREFIX             = "com.nucleus.user.User:";

    /** The Constant SMTP_FROM. */
    private static final String            SMTP_FROM                = "neutrino@nucleussoftware.com";



    @Inject
    @Named("notificationService")
    private NotificationService            notificationService;

    

    @Inject
    @Named("userService")
    private UserService                    userService;

    @Inject
    @Named("eventService")
    private EventService                   eventService;

    @Inject
    @Named("itemWatcherService")
    private ItemWatcherService             itemWatcherService;

    @Inject
    @Named("configurationService")
    private ConfigurationService           configurationService;

    @Inject
    @Named("mailService")
    private MailService                    mailService;

    @Inject
    @Named("templateService")
    protected TemplateService              templateService;

    @Inject
    @Named("externalMailLinkService")
    private ExternalMailLinkService        externalMailLinkService;

    @Inject
    @Named("shortMessageIntegrationService")
    private ShortMessageIntegrationService shortMessageIntegrationService;

    @Inject
    @Named("messageSource")
    protected MessageSource                messageSource;

    @Inject
    @Named("mailMessageIntegrationService")
    private MailMessageIntegrationService  mailMessageIntegrationService;

    protected static final String          MAX_NUMBER_NOTIFICATIONS = "config.notifications.keepNotifications";

    protected static final String          CORPORATE_EMAIL_ENABLED  = "config.user.corporateMails.enabled";

    @Inject
    @Named("InAppHelper")
    InAppMailHelper                        inAppMailHelper;

    private ArrayBlockingQueue<Long> userQueue;

    @Inject
    @Named("neutrinoThreadPoolExecutor")
    protected Executor taskExecutor;
    
    @Value("${mail.event.type.all.smtp.from}")
	private String fromEmailAddress;

    @Value("${maximum.allowable.queue.size:1000}")
    private Integer maxQueueSize;

    @Override
    public Set<String> getAllWatchersForEntity(EntityId entityId) {
        if (entityId != null) {
            return itemWatcherService.findAllWatchersFor(entityId.getUri());
        } else {
            return Collections.emptySet();
        }
    }

    @Override
    public boolean getUserPreference(String userUri, String propertyString) {

        boolean userPreference = false;
        ConfigurationVO configurationVO = configurationService.getConfigurationPropertyFor(EntityId.fromUri(userUri),
                propertyString);

        if (configurationVO != null && configurationVO.getPropertyValue() != null) {
            userPreference = Boolean.valueOf(configurationVO.getPropertyValue().toLowerCase());
        }
        return userPreference;
    }

    @Override
    public Long getUserPreferenceMaxNotificationCount(String userUri) {

        Long maxNumber = 0l;
        ConfigurationVO configurationVO = configurationService.getConfigurationPropertyFor(EntityId.fromUri(userUri),
                MAX_NUMBER_NOTIFICATIONS);

        if (configurationVO != null && configurationVO.getPropertyValue() != null) {
            maxNumber = Long.valueOf(configurationVO.getPropertyValue().toLowerCase());
        }
        return maxNumber;
    }

    @Override
    public void saveEvent(Event genericEvent) {

        eventService.createEventEntry(genericEvent);
    }

    @Override
    public void processPopupNotificationTask(Event event, Set<String> userUrisForPopupNotification) {
        List<Notification> notifications = notificationService.createNotificationsUsingGenericEventForUsers(
                (GenericEvent) event, userUrisForPopupNotification);
        notificationService.createNotificationEntries(notifications);
        Long userId;
        for(String userUri :userUrisForPopupNotification){
            userId = getUserIdFromUserUri(userUri);
            if(!userQueue.offer(userId)){
                LOGGER.error("User Notification Queue is full and unable to send user id: "+ userId);
            }
        }

    }

    @Override
    public Long getUnseenNotificationCountForUser(String userUri) {
        Long pendingNotificationCount = notificationService.getUnseenNotificationCountByUser(userUri);

        if (pendingNotificationCount == null) {
            pendingNotificationCount = 0l;
        }
        return pendingNotificationCount;
    }
    
    @Override
    public List<Long> getNewNotificationByUserByCreationTimestamp(String userUri, Long maxNumberNotifications, List<Integer> applicableEvents){
        return notificationService.getNewNotificationByUserByCreationTimestamp(userUri, maxNumberNotifications, applicableEvents);
    }

    @Override
    public void updateUnseenNotificationByUserByCreationTimestamp(String userUri, List<Long> newNotificationsId, List<Integer> applicableEvents) {
        notificationService.updateUnseenNotificationByUserByCreationTimestamp(userUri, newNotificationsId, applicableEvents);
    }

    @Override
    public List<Integer> getApplicableEvents(){
        return notificationService.getApplicableEvents();
    }

    // INTERNAL-MAIL-TASK
    @Override
    public void processInternalMailNotificationTask(Event event, Set<String> userUrisForInMail) {

        CommonMailContent commonMailContent = getCommonMailContent(event);
        sendNotificationMail(userUrisForInMail, commonMailContent);
    }

    // INTERNAL-MAIL
    @Override
    public void sendNotificationMail(Set<String> userUris2, CommonMailContent commonMailContent) {

        inAppMailHelper.sendNotificationAndCorporateEmails(userUris2, commonMailContent, true, null);

    }

    @PostConstruct
    protected void createConsumerForNotificationSeenUpdate(){
        userQueue = new ArrayBlockingQueue(maxQueueSize);
        taskExecutor.execute(new Runnable() {
            @Override
            public void run() {
                List<Integer> applicableEvents = getApplicableEvents();
                while(true){
                    try {
                        Long userId = userQueue.take();
                        String userUri = getUserUriFromUserId(userId);
                        Long maxNumberNotifications = getUserPreferenceMaxNotificationCount(userUri);
                        List<Long> newNotificationsId = getNewNotificationByUserByCreationTimestamp(userUri, maxNumberNotifications ,applicableEvents);
                        updateUnseenNotificationByUserByCreationTimestamp(userUri, newNotificationsId, applicableEvents);
                    } catch (InterruptedException e) {
                        LOGGER.error("Exception occurred while picking up notification tasks",e);
                    }
                }

            }
        });

    }

    // INTERNAL-MAIL
    @Override
    public CommonMailContent getCommonMailContent(Event event) {
        CommonMailContent commonMailContent = new CommonMailContent();
        String fromUsername = eventService.getEventTypeStringRepresentation(event, null, FormatType.EMAIL_INTERNAL_FROM);
        String systemUserUri = userService.getUserUriByUserName(fromUsername);

        Map<String, String> contextProps = new HashMap<String, String>();
        contextProps.put("LINK", getLinkForEventEntityView(event, INTERNAL_USE));
        String mailBody = eventService.getEventTypeStringRepresentation(event, null, FormatType.EMAIL_BODY, contextProps);
        String mailSubject = eventService.getEventTypeStringRepresentation(event, null, FormatType.EMAIL_SUBJECT);
        commonMailContent.setFromUserUri(systemUserUri);
        commonMailContent.setMsgSentTimeStamp(DateUtils.getCurrentUTCTime());
        commonMailContent.setSubject(mailSubject);
        commonMailContent.setBody(mailBody);
        commonMailContent.getEntityLifeCycleData().setCreatedByUri(systemUserUri);
        return commonMailContent;
    }
    // SMTP-MAIL-TASK
    @Override
    public void processSmtpMailNotificationTask(Event event, Set<String> userUrisForSmtpMail) {

        MimeMailMessageBuilder mimeMailMessageBuilder = getSmtpMailBuilder(event);

        sendMailToMailIds(userUrisForSmtpMail, mimeMailMessageBuilder);
    }

    // SMTP-MAIL
    private void sendMailToMailIds(Set<String> userUrisForSmtpMail, MimeMailMessageBuilder mimeMailMessageBuilder) {
        Set<String> filteredEmails = new HashSet<String>();
        for (String userURI : userUrisForSmtpMail) {

            String usersMailAddress = userService.getUserMailById(EntityId.fromUri(userURI).getLocalId());
            if (usersMailAddress != null) {
                filteredEmails.add(userService.getUserMailById(EntityId.fromUri(userURI).getLocalId()));
            }
        }
        String[] emailArr = null;

        int emailListSize = filteredEmails.size();
        if (emailListSize > 0) {
            emailArr = filteredEmails.toArray(new String[emailListSize]);
            mimeMailMessageBuilder.setTo(emailArr);
            mailService.sendMail(mimeMailMessageBuilder);
        }
    }

    // SMTP-MAIL
    private MimeMailMessageBuilder getSmtpMailBuilder(Event event) {

        MimeMailMessageBuilder mimeMailMessageBuilder = mailService.createMimeMailBuilder();

        String from = eventService.getEventTypeStringRepresentation(event, null, FormatType.EMAIL_SMTP_FROM);
        String subject = eventService.getEventTypeStringRepresentation(event, null, FormatType.EMAIL_SUBJECT);

        Map<String, String> contextProps = new HashMap<String, String>();
        contextProps.put("LINK", getLinkForEventEntityView(event, EXTERNAL_USE));
        String body = eventService.getEventTypeStringRepresentation(event, null, FormatType.EMAIL_BODY, contextProps);

        return mimeMailMessageBuilder.setFrom(fromEmailAddress).setSubject(subject).setHtmlBody(body);
    }

    private String getLinkForEventEntityView(Event event, boolean forInternalUse) {
        String resultedLink = "";

        String externalMailLink = event.getPersistentProperty("ESCALATION_MAIL_URL_STRING");

        resultedLink = forInternalUse ? externalMailLink : externalMailLinkService.getExternalMailLink(externalMailLink);

        return resultedLink;
    }

    @Override
    public void processSMSNotificationTask(Event event, Set<String> phonenumbers) {

        String smsBody = eventService.getEventTypeStringRepresentation(event, null, FormatType.SMS);
        Set<String> smsTO = event.getMobileNumbersToSms();

        Set<String> finalPhoneNumbers = new HashSet<String>();
        if (phonenumbers != null) {
            finalPhoneNumbers.addAll(phonenumbers);
        }
        if (smsTO != null && !smsTO.isEmpty()) {
            finalPhoneNumbers.addAll(smsTO);
        }

        if (!finalPhoneNumbers.isEmpty()) {
            for (String phonenumString : finalPhoneNumbers) {

                SmsMessage smsMessage = new SmsMessage();
                smsMessage.setTo(phonenumString);
                smsMessage.setBody(smsBody);
                ShortMessageSendResponsePojo messageSendResponsePojo = shortMessageIntegrationService
                        .sendShortMessage(smsMessage);

                if (messageSendResponsePojo != null) {
                    ShortMessageExchangeRecord exchangeRecord = new ShortMessageExchangeRecord();
                    exchangeRecord.setOwnerEntityUri(event.getOwnerEntityId().getUri());
                    exchangeRecord.setSmsBody(smsBody);
                    exchangeRecord.setSmsTo(phonenumString);
                    exchangeRecord.setDeliveryStatus(MessageDeliveryStatus.DELIVERED);
                    exchangeRecord.setDeliveryTimestamp(messageSendResponsePojo.getReceiptTimestamp());
                    entityDao.persist(exchangeRecord);
                }
            }
        } else {
            LOGGER.info("No mobile numbers found to process sms notification for event {}", event);

        }

    }
    
    @Override
    public void markOldUnseenNotificationAsSeen(Notification notification){
        notification.setMarkedSeenBySystem(true);
        notification.setSeen(true);
        entityDao.update(notification);
    }

    @Override
   public Long getUserIdFromUserUri(String userUri){
       String[] uriArray = userUri.split(":", 2);
       return Long.parseLong(uriArray[1]);
   }

    @Override
   public String getUserUriFromUserId(Long userId){
       return (USER_URI_PREFIX).concat(Long.toString(userId));
   }

}
