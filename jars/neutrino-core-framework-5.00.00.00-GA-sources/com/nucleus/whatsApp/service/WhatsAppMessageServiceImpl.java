/**
 * Copyright Nucleus Software India Pvt Ltd. All rights reserved.
 */
package com.nucleus.whatsApp.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import com.nucleus.NeutrinoUUIDGenerator;
import com.nucleus.cfi.whatsApp.pojo.WhatsAppMessage;
import com.nucleus.cfi.whatsApp.pojo.WhatsAppMessageSendResponse;
import com.nucleus.cfi.whatsApp.service.WhatsAppIntegrationService;
import com.nucleus.core.initialization.ProductInformationLoader;
import com.nucleus.finnone.pro.general.util.ValidatorUtils;
import com.nucleus.logging.BaseLoggers;
import com.nucleus.message.entity.MessageDeliveryStatus;
import com.nucleus.message.entity.WhatsAppExchangeRecord;
import com.nucleus.security.oauth.service.RESTfulAuthenticationService;
import com.nucleus.service.BaseServiceImpl;

/**
 * The Class NotificationMasterServiceImpl.
 *
 * @author Nucleus Software India Pvt Ltd
 */
@Transactional(propagation = Propagation.REQUIRED)
@Named("whatsAppMessageService")
public class WhatsAppMessageServiceImpl extends BaseServiceImpl implements WhatsAppMessageService {

	@Value(value = "${from.whatsapp.number}")
	private String fromWhatsAppNumber;

	@Value(value = "${whatsapp.file.upload.server.sftpuser}")
	private String sftpUser;

	@Value(value = "${whatsapp.file.upload.server.sftphost}")
	private String sftpHost;

	@Value(value = "${whatsapp.file.upload.server.sftpport}")
	private Integer sftpPort;

	@Value(value = "${whatsapp.file.upload.server.sftppassword}")
	private String sftpPassword;

	@Value(value = "${whatsapp.file.upload.server.sftpdirpath}")
	private String sftpDirPath;

	@Value(value = "${whatsapp.file.upload.server.baseurl}")
	private String baseurl;

	@Value("${soap.service.trusted.client.id}")
	private String clientID;
	
	@Value("${cfi.ws.client.url.whatsAppMessageServiceURL}")
	private String whatsAppWebServiceUrl;
	
    @Inject
    @Named("whatsAppIntegrationService")
    private WhatsAppIntegrationService whatsAppIntegrationService;
	
    @Inject
	@Named("oauthauthenticationService")
	private RESTfulAuthenticationService restAuthenticationService;
    
    private final NeutrinoUUIDGenerator uuidGenerator = new NeutrinoUUIDGenerator(ProductInformationLoader.getProductCode() + "-");
    
	@Override
	public void processWhatsAppNotificationTask(Set<String> phoneNumbers, String body, String ownerEntityUri,
			String extIdentifier, File attachFile) throws IOException {

		if (!phoneNumbers.isEmpty()) {
			List<String> mediaUris =new ArrayList<>();
			String fileName="";
			if(attachFile!=null){
				mediaUris=getMediaUris(attachFile);
				fileName = getFormattedFileName(attachFile);
			}
			if (!mediaUris.isEmpty()) {
				processWhatsAppMessageAndSaveInDb(phoneNumbers, fileName, ownerEntityUri, extIdentifier, mediaUris);
				processWhatsAppMessageAndSaveInDb(phoneNumbers, body, ownerEntityUri, extIdentifier, new ArrayList<>());
			} else {
				processWhatsAppMessageAndSaveInDb(phoneNumbers, body, ownerEntityUri, extIdentifier, mediaUris);
			}

		}
	}

	/**
	 * @param phoneNumbers
	 * @param body
	 * @param ownerEntityUri
	 * @param extIdentifier
	 * @param mediaUris
	 * @throws IOException
	 */
	private void processWhatsAppMessageAndSaveInDb(Set<String> phoneNumbers, String body, String ownerEntityUri,
			String extIdentifier, List<String> mediaUris) throws IOException {
		for (String phonenumString : phoneNumbers) {
			WhatsAppMessage whatsAppMessage = new WhatsAppMessage();
			whatsAppMessage.setTo(phonenumString);
			whatsAppMessage.setFrom(fromWhatsAppNumber);
			whatsAppMessage.setBody(body);
			whatsAppMessage.setUniqueRequestId(uuidGenerator.generateUuid());
			whatsAppMessage.setMediaUris(mediaUris);
			String token = restAuthenticationService.getSecurityToken(clientID);
			WhatsAppMessageSendResponse whatsAppMessageSendResponse = whatsAppIntegrationService
					.sendWhatsAppMessage(whatsAppMessage, whatsAppWebServiceUrl, token);
			WhatsAppExchangeRecord whatsAppExchangeRecord = new WhatsAppExchangeRecord();
			whatsAppExchangeRecord.setOwnerEntityUri(ownerEntityUri);
			whatsAppExchangeRecord.setMessageBody(body);
			whatsAppExchangeRecord.setMessageTo(phonenumString);
			whatsAppExchangeRecord.setUniqueRequestId(whatsAppMessage.getUniqueRequestId());
			whatsAppExchangeRecord.setExtIdentifier(extIdentifier);
			if (ValidatorUtils.notNull(whatsAppMessageSendResponse)) {
				whatsAppExchangeRecord.setDeliveryTimestamp(whatsAppMessageSendResponse.getReceiptTimestamp());
				whatsAppExchangeRecord.setMessageReceiptId(whatsAppMessageSendResponse.getMessageReceiptId());
				whatsAppExchangeRecord.setStatusMessage(whatsAppMessageSendResponse.getMessageStatus());
				whatsAppExchangeRecord.setDeliveryStatus(
						MessageDeliveryStatus.valueOf(whatsAppMessageSendResponse.getWhatsAppDeliveryStatus().name()));
			}
			entityDao.persist(whatsAppExchangeRecord);
		}
	}

	/**
	 * @param attachFile
	 * @return
	 */
	private List<String> getMediaUris(File attachFile) {
		String fileUrl = uploadFileToPublicServer(attachFile);
		List<String> mediaUris = new ArrayList<>();
		if (StringUtils.isNotBlank(fileUrl)) {
			mediaUris.add(fileUrl);
		}
		return mediaUris;
	}


	/**
	 * 
	 * @param file
	 * @return
	 */
	private String uploadFileToPublicServer(File file) {
		Session session = null;
		Channel channel = null;
		String fileUrl = "";
		ChannelSftp channelSftp = null;
		String fileName = getFormattedFileName(file);
		try {

			JSch jsch = new JSch();
			session = jsch.getSession(sftpUser, sftpHost, sftpPort);
			session.setPassword(sftpPassword);
			java.util.Properties config = new java.util.Properties();
			config.put("StrictHostKeyChecking", "no");
			session.setConfig(config);
			session.connect();
			channel = session.openChannel("sftp");
			channel.connect();
			channelSftp = (ChannelSftp) channel;
			channelSftp.cd(sftpDirPath);
			channelSftp.put(new FileInputStream(file), fileName, ChannelSftp.OVERWRITE);
			channelSftp.exit();
			session.disconnect();
			fileUrl = baseurl + fileName;
		} catch (Exception e) {
			BaseLoggers.exceptionLogger.error("Something went wrong while uploading file to the server -"+sftpHost,e);
		} finally {
			if (channel != null) {
				channel.disconnect();
			}
			if (session != null) {
				session.disconnect();
			}
			if (channelSftp != null) {
				channelSftp.disconnect();
			}
		}
		return fileUrl;
	}

	/**
	 * @param file
	 * @return
	 */
	private String getFormattedFileName(File file) {
		String fileName = file.getName().trim();
		fileName = fileName.replaceAll("\\s", "_");
		return fileName;
	}
}
