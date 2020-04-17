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
package com.nucleus.cfi.mail.service;

import java.io.IOException;
import java.math.BigInteger;

import javax.inject.Named;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Value;

import com.nucleus.NeutrinoUUIDGenerator;
import com.nucleus.cfi.integration.common.AbstractIntegrationService;
import com.nucleus.cfi.integration.common.TransactionHeaderMessageCallback;
import com.nucleus.cfi.mail.pojo.MailMessageSendResponsePojo;
import com.nucleus.cfi.ws.client.stub.mailMessageService.MailMessageContent;
import com.nucleus.cfi.ws.client.stub.mailMessageService.MailMessageMetadata;
import com.nucleus.cfi.ws.client.stub.mailMessageService.MailSendRequest;
import com.nucleus.cfi.ws.client.stub.mailMessageService.MailSendResponse;
import com.nucleus.cfi.ws.client.stub.mailMessageService.SMTPServerInfo;
import com.nucleus.core.calendar.util.XMLGregorianCalendarUtils;
import com.nucleus.core.initialization.ProductInformationLoader;
import com.nucleus.core.mime.util.MimeMessageUtils;

/**
 * @author Nucleus Software Exports Limited
 *
 */
@Named("mailMessageIntegrationService")
public class MailMessageIntegrationServiceImpl extends AbstractIntegrationService implements MailMessageIntegrationService {

    @Value("${cfi.integration.params.common.messageOriginatorId}")
    private String  messageOriginatorId;

    @Value("${cfi.integration.params.common.messageOriginatorName}")
    private String  messageOriginatorName;

    @Value("${cfi.integration.params.common.destinationSystemId}")
    private String  destinationSystemId;

    // parameters specific to smtp mail message exchanges ===========================
    @Value("${cfi.integration.params.mail.smtpHostIp}")
    private String  smtpHostIp;

    @Value("${cfi.integration.params.mail.smtpPort}")
    private Integer smtpPort;

    @Value("${cfi.integration.params.mail.smtpProtocol}")
    private String  smtpProtocol;

    @Value("${cfi.integration.params.mail.authenticationRequired}")
    private Boolean authenticationRequired;

    @Value("${cfi.integration.params.mail.username}")
    private String  username;

    @Value("${cfi.integration.params.mail.password}")
    private String  password;
    
    private final NeutrinoUUIDGenerator uuidGenerator = new NeutrinoUUIDGenerator();

    @Value("${cfi.ws.client.url.mailMessageServiceURL}")
    @Override
    public void setWebServiceUrl(String webServiceUrl) {
        LOGGER.info("Setting web service url to {}", webServiceUrl);
        this.webServiceUrl = webServiceUrl;
    }
    
    public String getMessageOriginatorId() {
        return messageOriginatorId;
    }

    public String getMessageOriginatorName() {
        return messageOriginatorName;
    }

    public String getDestinationSystemId() {
        return destinationSystemId;
    }

    public String getSmtpHostIp() {
        return smtpHostIp;
    }

    public Integer getSmtpPort() {
        return smtpPort;
    }

    public String getSmtpProtocol() {
        return smtpProtocol;
    }

    public Boolean getAuthenticationRequired() {
        return authenticationRequired;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

	@Override
	public void setRemoteSystemWebServiceUrl(String remoteSystemwebServiceUrl) {
		//
	}
	
	@Override
	public MailMessageSendResponsePojo sendMailMessageToIntegration(MimeMessage mimeMessage, String uniqueId,
			boolean isAsync) throws MessagingException, IOException {
		//In case of async uniqueId and corelationId will be same.
		String correlationId = uniqueId != null ? uniqueId : (uniqueId = uuidGenerator.generateUuid());
		LOGGER.info("Sending web service request to integration server for mail message service");
		Object serviceResponse = webServiceTemplate.marshalSendAndReceive(webServiceUrl,
                prepareMailSendRequest(mimeMessage, correlationId, isAsync), new TransactionHeaderMessageCallback(
                        getNamespaceURIForJaxbObject(new MailSendRequest())));
		LOGGER.info("Received web service response from integration server for for mail message service");
		if (serviceResponse instanceof MailSendResponse) {
	        MailSendResponse mailSendResponse = (MailSendResponse) serviceResponse;
			MailMessageSendResponsePojo messageSendResponsePojo = new MailMessageSendResponsePojo();
	        messageSendResponsePojo.setMessageReceiptId(mailSendResponse.getMessageReceiptId());
	        messageSendResponsePojo.setReceiptTimestamp(new DateTime(mailSendResponse.getReceiptTimestamp().toGregorianCalendar()));
	        messageSendResponsePojo.setDeliveryStatus(mailSendResponse.getDeliveryStatus().value());
	        messageSendResponsePojo.setUniqueId(uniqueId);
	        return messageSendResponsePojo;
		} else {
			/**
			 * This web service call will return acknowledgement as CommAsyncRequest. 
			 * Which can be processed like --- processAcknowledgeMent(asyncRequest);
			 */
		    return null;
		}
	}

	@Override
    public MailMessageSendResponsePojo sendMailMessageToIntegrationAsynchronously(MimeMessage mimeMessage)
            throws MessagingException, IOException {
    	return sendMailMessageToIntegration(mimeMessage, null, true);
    }
    
    @Override
    public MailMessageSendResponsePojo sendMailMessageToIntegrationServer(MimeMessage mimeMessage)
            throws MessagingException, IOException {
    	return sendMailMessageToIntegrationServer(mimeMessage, null);
    }
    
	@Override
	public MailMessageSendResponsePojo sendMailMessageToIntegrationServer(MimeMessage mimeMessage, String uniqueId)
			throws IOException, MessagingException {
    	return sendMailMessageToIntegration(mimeMessage, uniqueId, false);
	}
    
    private MailSendRequest prepareMailSendRequest(MimeMessage mimeMessage, String uniqueId, boolean isAsync) throws MessagingException, IOException {
        //SMTP server info creation.
        SMTPServerInfo smtpServerInfo = new SMTPServerInfo();
        smtpServerInfo.setHost(getSmtpHostIp());
        smtpServerInfo.setPort(new BigInteger(getSmtpPort().toString()));
        smtpServerInfo.setProtocol(StringUtils.lowerCase(getSmtpProtocol()));
        smtpServerInfo.setAuthenticationRequired(getAuthenticationRequired());
        smtpServerInfo.setUsername(getUsername());
        smtpServerInfo.setPassword(getPassword());
        //mailMessageMetadata creation.
        MailMessageMetadata mailMessageMetadata = new MailMessageMetadata();
        mailMessageMetadata.setUniqueId(uniqueId);
        mailMessageMetadata.setCorrelationId(uniqueId);
        mailMessageMetadata.setMessageOriginatorId(ProductInformationLoader.getProductCode());
        mailMessageMetadata.setSentTimestamp(XMLGregorianCalendarUtils.getCurrentTimestamp());
        mailMessageMetadata.setSmtpServerInfo(smtpServerInfo);
        //mailMessageContent creation.
        MailMessageContent mailMessageContent = new MailMessageContent();
        mailMessageContent.setMessageContentByteStream(MimeMessageUtils.getMimeMessageByteDatahandler(mimeMessage));
        //mailSendRequest creation.
        MailSendRequest mailSendRequest = new MailSendRequest();
        mailSendRequest.setMessageContent(mailMessageContent);
        mailSendRequest.setMessageMetadata(mailMessageMetadata);
        mailSendRequest.setAsyncRequest(isAsync);
        return mailSendRequest;
    }

}
