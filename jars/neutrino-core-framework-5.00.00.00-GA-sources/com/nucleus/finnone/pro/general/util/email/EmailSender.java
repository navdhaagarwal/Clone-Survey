/*
 * Author: Merajul Hasan Ansari
 * Creation Date: 13-May-2013
 * Copyright: Nucleus Software Exports Ltd.
 * Description: This is an abstract which will be used for Sending Email.
 *
 * ------------------------------------------------------------------------------------------------------------------------------------
 * Revision:  Version         Last Revision Date                   Name                Function / Module affected  Modifications Done
 * ------------------------------------------------------------------------------------------------------------------------------------
 *                1.0             13/05/2013                    Merajul Hasan Ansari             Initial Version created 
 
 */
package com.nucleus.finnone.pro.general.util.email;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;
import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;

import org.springframework.core.io.InputStreamSource;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;

import com.nucleus.finnone.pro.base.Message;
import com.nucleus.finnone.pro.base.exception.ExceptionBuilder;
import com.nucleus.finnone.pro.general.util.email.constants.EmailConstatnts;
import com.nucleus.finnone.pro.general.util.templatemerging.TemplateMergingUtility;
import com.nucleus.logging.BaseLoggers;

public class EmailSender {
	
	/**
	 * Default from address. If user does not supply - this address will be used
	 */
	private InternetAddress fromAddress;	
	/**
	 * Default replyTo address. If user does not supply - this address will be used
	 */
	private InternetAddress replyTo;	
	/**
	 * Default subject. If user does not supply - this subject will be used
	 */
	private String subject;	
	/**
	 * Default to addresses . If caller supplies - this list will be added to the the supplied list
	 */
	private List<InternetAddress> toAddressList =null;
	/**
	 * Default cc addresses . If caller supplies - this list will be added to the the supplied list
	 */
	private List<InternetAddress> ccAddressList = null;	
	/**
	 * Default bcc addresses . If caller supplies - this list will be added to the the supplied list
	 */
	private List<InternetAddress> bccAddressList = null;	
	/**
	 * Default mail content . If does not supply content or template path- this will be used 
	 */
	private String content;
	/**
	 * Default mail content template path . This will be applied if user does not supply the content and template file path and  content in this instance is null
	 */
	private String templateFilePath;
		private List<String> attachementFilePaths;
	private Map<String,String> inLineAttachmentMap;
	
	private static final String DEFAULT_CHARSET=Charset.forName( "UTF-8" ).name();
	
	@Inject 
	@Named("javaMailServerConfigurationAndSender")
	private JavaMailSenderImpl mailSender;
	
	@Inject
	@Named("templateMergingUtility")
	private TemplateMergingUtility templateMergingUtility;
	
	/**
	 * Email sending allowed or not
	 */
	private boolean emailSendingAllowed;
	
	/**
	 * Sends the email
	 * @param emailVO
	 */
	public void sendEMail(EmailVO emailVO){
		if(!emailSendingAllowed){
			BaseLoggers.exceptionLogger.error("Email sending not allowed - not sending email");
			return;
		}
		MimeMessageHelper mimeMessageHelper = createMimeMessageHelper(emailVO);
		mailSender.send(mimeMessageHelper.getMimeMessage());
	}
	
	/**
	 * Choreograph mail composition
	 * @param emailVO
	 * @return
	 */
	protected MimeMessageHelper createMimeMessageHelper(EmailVO emailVO){
		MimeMessageHelper mimeMessageHelper=null;
		try {
			mimeMessageHelper = new MimeMessageHelper(mailSender.createMimeMessage(),true,DEFAULT_CHARSET);
		} catch (MessagingException e) {
			List<Message> errorMessages = new ArrayList<Message>();
			errorMessages.add(new Message(EmailConstatnts.ERROR_IN_CREATING_MIME_MESSAGE_HELPER,Message.MessageType.ERROR));
			throw ExceptionBuilder.getInstance(EmailException.class, EmailConstatnts.ERROR_IN_CREATING_MIME_MESSAGE_HELPER, "Error while creating mime message helper").setMessages(errorMessages).setOriginalException(e).build();
		}
		applyFrom(mimeMessageHelper, emailVO);
		applyReplyTo(mimeMessageHelper, emailVO);
		applyToAddressList(mimeMessageHelper, emailVO);
		applyCcAddressList(mimeMessageHelper, emailVO);
		applyBccAddressList(mimeMessageHelper, emailVO);
		applySubject(mimeMessageHelper, emailVO);
		applyMailText(mimeMessageHelper, emailVO);
		applyAttachments(mimeMessageHelper, emailVO);
		return mimeMessageHelper;
	}
	/**
	 * 
	 * @param mimeMessageHelper
	 * @param emailVO
	 */
	protected void applyFrom(MimeMessageHelper mimeMessageHelper,EmailVO emailVO ){
		try {
			if(emailVO.getFromAddress()!=null){
					mimeMessageHelper.setFrom(emailVO.getFromAddress());
			}else{
				mimeMessageHelper.setFrom(this.fromAddress);
			}
		} catch (MessagingException e) {	
			List<Message> errorMessages = new ArrayList<Message>();
			errorMessages.add(new Message(EmailConstatnts.INVALID_FROM_ADDRESS,Message.MessageType.ERROR));
			throw ExceptionBuilder.getInstance(EmailException.class, EmailConstatnts.ERROR_ADDING_EMAIL_ADDRESS, "From address was invalid.").setMessages(errorMessages).setOriginalException(e).build();
		}
	}
	/**
	 * 
	 * @param mimeMessageHelper
	 * @param emailVO
	 */
	protected void applyReplyTo(MimeMessageHelper mimeMessageHelper,EmailVO emailVO ){
		try {
			if(emailVO.getReplyTo()!=null){
					mimeMessageHelper.setReplyTo(emailVO.getFromAddress());
			}else if(this.replyTo!=null){
				mimeMessageHelper.setReplyTo(this.replyTo);
			}
		} catch (MessagingException e) {
			List<Message> errorMessages = new ArrayList<Message>();
			errorMessages.add(new Message(EmailConstatnts.INVALID_REPLY_TO_ADDRESS,Message.MessageType.ERROR));
			throw ExceptionBuilder.getInstance(EmailException.class, EmailConstatnts.ERROR_ADDING_EMAIL_ADDRESS, "replyTo address was invalid.").setMessages(errorMessages).setOriginalException(e).build();
		}
	}
	/**
	 * 
	 * @param mimeMessageHelper
	 * @param emailVO
	 */
	protected void applyToAddressList(MimeMessageHelper mimeMessageHelper,EmailVO emailVO ){
		try {
			
			List<InternetAddress> combinedToAddressList = new ArrayList<InternetAddress>();
			
			if(emailVO.getToAddressList()!=null&&emailVO.getToAddressList().size()!=0){
				combinedToAddressList.addAll(emailVO.getToAddressList());					
			}else if(this.toAddressList!=null&&this.toAddressList.size()!=0){
				combinedToAddressList.addAll(this.toAddressList);
			}
			if(combinedToAddressList.size()==0){
				List<Message> errorMessages = new ArrayList<Message>();
				errorMessages.add(new Message(EmailConstatnts.INVALID_TO_ADDRESSES,Message.MessageType.ERROR));
				throw ExceptionBuilder.getInstance(EmailException.class, EmailConstatnts.ERROR_ADDING_EMAIL_ADDRESS, "Empty toAddressList was given.").setMessages(errorMessages).build();
			}
			mimeMessageHelper.setTo(combinedToAddressList.toArray(new InternetAddress[combinedToAddressList.size()]));
		} catch (MessagingException e) {
			List<Message> errorMessages = new ArrayList<Message>();
			errorMessages.add(new Message(EmailConstatnts.INVALID_TO_ADDRESSES,Message.MessageType.ERROR));
			throw ExceptionBuilder.getInstance(EmailException.class, EmailConstatnts.ERROR_ADDING_EMAIL_ADDRESS, "to address was invalid.").setMessages(errorMessages).setOriginalException(e).build();
		}
	}
	
	/**
	 * 
	 * @param mimeMessageHelper
	 * @param emailVO
	 */
	protected void applyCcAddressList(MimeMessageHelper mimeMessageHelper,EmailVO emailVO ){
		try {
			
			List<InternetAddress> combinedCcAddressList = new ArrayList<InternetAddress>();
			
			if(emailVO.getCcAddressList()!=null&&emailVO.getCcAddressList().size()!=0){
				combinedCcAddressList.addAll(emailVO.getCcAddressList());					
			}else if(this.ccAddressList!=null&&this.ccAddressList.size()!=0){
				combinedCcAddressList.addAll(this.ccAddressList);
			}
			
			if(combinedCcAddressList.size()!=0){
				mimeMessageHelper.setCc(combinedCcAddressList.toArray(new InternetAddress[combinedCcAddressList.size()]));
			}
			
		} catch (MessagingException e) {	
			List<Message> errorMessages = new ArrayList<Message>();
			errorMessages.add(new Message(EmailConstatnts.INVALID_CC_ADDRESSES,Message.MessageType.ERROR));
			throw ExceptionBuilder.getInstance(EmailException.class, EmailConstatnts.ERROR_ADDING_EMAIL_ADDRESS, "cc address was invalid.").setMessages(errorMessages).setOriginalException(e).build();
		}
	}
	
	/**
	 * 
	 * @param mimeMessageHelper
	 * @param emailVO
	 */
	protected void applyBccAddressList(MimeMessageHelper mimeMessageHelper,EmailVO emailVO ){
		try {
			
			List<InternetAddress> combinedBccAddressList = new ArrayList<InternetAddress>();
			
			if(emailVO.getBccAddressList()!=null&&emailVO.getBccAddressList().size()!=0){
				combinedBccAddressList.addAll(emailVO.getBccAddressList());					
			}else if(this.bccAddressList!=null&&this.bccAddressList.size()!=0){
				combinedBccAddressList.addAll(this.bccAddressList);
			}
			
			if(combinedBccAddressList.size()!=0){
				mimeMessageHelper.setCc(combinedBccAddressList.toArray(new InternetAddress[combinedBccAddressList.size()]));
			}
			
		} catch (MessagingException e) {	
			List<Message> errorMessages = new ArrayList<Message>();
			errorMessages.add(new Message(EmailConstatnts.INVALID_CC_ADDRESSES,Message.MessageType.ERROR));
			throw ExceptionBuilder.getInstance(EmailException.class, EmailConstatnts.ERROR_ADDING_EMAIL_ADDRESS, "bcc address was invalid.").setMessages(errorMessages).setOriginalException(e).build();
		}
	}
	/**
	 * 
	 * @param mimeMessageHelper
	 * @param emailVO
	 */
	protected void applySubject(MimeMessageHelper mimeMessageHelper,EmailVO emailVO ){
		try {
			if(emailVO.getSubject()!=null){
				mimeMessageHelper.setSubject(emailVO.getSubject());
			}else{
				mimeMessageHelper.setSubject(this.subject);
			}
		} catch (MessagingException e) {	
			List<Message> errorMessages = new ArrayList<Message>();
			errorMessages.add(new Message(EmailConstatnts.ERROR_IN_SUBJECT,Message.MessageType.ERROR));
			throw ExceptionBuilder.getInstance(EmailException.class, EmailConstatnts.ERROR_IN_SUBJECT, "Error occured while setting subject.").setMessages(errorMessages).setOriginalException(e).build();
		}
	}
	
	/**
	 * 
	 * @param mimeMessageHelper
	 * @param emailVO
	 */
	protected void applyMailText(MimeMessageHelper mimeMessageHelper,EmailVO emailVO ){
		try {
			if(emailVO.getContent()!=null){
				mimeMessageHelper.setText(emailVO.getContent(), true);
			}else if(emailVO.getContentTemplateFilePath()!=null){
				String text = templateMergingUtility.mergeTemplateIntoString(emailVO.getContentTemplateFilePath(),
															emailVO.getTemplateMappingObject());
				mimeMessageHelper.setText(text);
			}else if(this.content!=null){
				mimeMessageHelper.setText(this.content, true);
			}else{
				String text = templateMergingUtility.mergeTemplateIntoString(this.templateFilePath,
															emailVO.getTemplateMappingObject());
				mimeMessageHelper.setText(text, true);
			}
		} catch (MessagingException e) {	
			List<Message> errorMessages = new ArrayList<Message>();
			errorMessages.add(new Message(EmailConstatnts.ERROR_IN_SUBJECT,Message.MessageType.ERROR));
			throw ExceptionBuilder.getInstance(EmailException.class, EmailConstatnts.ERROR_IN_SUBJECT, "Error occured while setting mail tex.").setMessages(errorMessages).setOriginalException(e).build();
		}
	}
	
	/**
	 * 
	 * @param mimeMessageHelper
	 * @param emailVO
	 */
	protected void applyAttachments(MimeMessageHelper mimeMessageHelper,EmailVO emailVO ){
		
			List<AttachmentVO> attachments = emailVO.getAttachments();
			if(attachments!=null&&attachments.size()>0){
				for(AttachmentVO attachment: attachments){
					if(AttachmentVO.AttachmentStyle.INLINE.equals(attachment.getAttachmentStyle())){
						applyInlineAttachment(mimeMessageHelper,attachment);
					}else{
						applyFileAttachment(mimeMessageHelper,attachment);
					}
				}
			}
			
		
	}
	
	/**
	 * 
	 * @param mimeMessageHelper
	 * @param emailVO
	 */
	protected void applyInlineAttachment(MimeMessageHelper mimeMessageHelper,AttachmentVO attachment){
		try {
				final byte[] attchementContent =attachment.getContent();
				final String contentType =attachment.getContentType();
				if(attachment.getContent()!=null){
					mimeMessageHelper.addInline(attachment.getFileName(),  
													new InputStreamSource(){
												            @Override
												            public InputStream getInputStream() throws IOException{
												                return new ByteArrayInputStream(attchementContent);
												            }
												    }
												    ,contentType);
				}else{
					mimeMessageHelper.addInline(attachment.getFileName(), new File(attachment.getFilePath()));
				}
		} catch (MessagingException e) {	
			List<Message> errorMessages = new ArrayList<Message>();
			errorMessages.add(new Message(EmailConstatnts.ERROR_IN_INLINE_ATTACHMENT,Message.MessageType.ERROR));
			throw ExceptionBuilder.getInstance(EmailException.class, EmailConstatnts.ERROR_IN_INLINE_ATTACHMENT, "Error occured while applying inline attachment.").setMessages(errorMessages).setOriginalException(e).build();
		}
	}
	
	/**
	 * 
	 * @param mimeMessageHelper
	 * @param emailVO
	 */
	protected void applyFileAttachment(MimeMessageHelper mimeMessageHelper,AttachmentVO attachment){
		try {
				final byte[] content =attachment.getContent();
				final String contentType =attachment.getContentType();
				if(attachment.getContent()!=null){
					mimeMessageHelper.addAttachment(attachment.getFileName(),  
													new InputStreamSource(){
												            @Override
												            public InputStream getInputStream() throws IOException{
												                return new ByteArrayInputStream(content);
												            }
												    }
												    ,contentType);
				}else{
					mimeMessageHelper.addAttachment(attachment.getFileName(), new File(attachment.getFilePath()));
				}
		} catch (MessagingException e) {
			List<Message> errorMessages = new ArrayList<Message>();
			errorMessages.add(new Message(EmailConstatnts.ERROR_IN_FILE_ATTACHMENT,Message.MessageType.ERROR));
			throw ExceptionBuilder.getInstance(EmailException.class, EmailConstatnts.ERROR_IN_FILE_ATTACHMENT, "Error occured while applying file attachment.").setMessages(errorMessages).setOriginalException(e).build();
		}
	}
	
	
	
	/**
	 * add provided email address to toAddressList
	 * @param internetAddress
	 */
	public void addToAddress(InternetAddress internetAddress){
		if (this.toAddressList==null){
			toAddressList = new ArrayList<InternetAddress>();
		}
		toAddressList.add(internetAddress);
	}
	
	/**
	 * add provided email address to ccAddressList
	 * @param internetAddress
	 */
	public void addCcAddress(InternetAddress internetAddress){
		if (this.ccAddressList==null){
			ccAddressList = new ArrayList<InternetAddress>();
		}
		ccAddressList.add(internetAddress);
	}
	
	/**
	 * add provided email address to bccAddressList
	 * @param internetAddress
	 */
	public void addBccAddress(InternetAddress internetAddress){
		if (this.bccAddressList==null){
			bccAddressList = new ArrayList<InternetAddress>();
		}
		bccAddressList.add(internetAddress);
	}
	
	/**
	 * add provided email address to toAddressList
	 * @param internetAddress
	 */
	
	public void addToAddress(String mailId, String alias){
		try {
			InternetAddress internetAddress = new InternetAddress(mailId, alias);
			addToAddress(internetAddress);
		} catch (UnsupportedEncodingException uee) {
			BaseLoggers.exceptionLogger.error("addToAddress", uee);
			EmailException ee = new EmailException();
			ee.setOriginalException(uee);
			ee.setLogMessage("Error in adding To Email Adress.");
			Message message = new Message(EmailConstatnts.ERROR_ADDING_EMAIL_ADDRESS, Message.MessageType.ERROR);
			List<Message> messages = new ArrayList<Message>();
			messages.add(message);
			ee.setMessages(messages);
			throw ee;
		}
		
	}
	/**
	 * add provided email address to ccAddressList
	 * @param internetAddress
	 */
	
	public void addCcAddress(String mailId, String alias){
		try {
			InternetAddress internetAddress = new InternetAddress(mailId, alias);
			addCcAddress(internetAddress);
		} catch (UnsupportedEncodingException uee) {
			BaseLoggers.exceptionLogger.error("addCcAddress", uee);
			EmailException ee = new EmailException();
			ee.setOriginalException(uee);
			ee.setLogMessage("Error in adding CC Email Adress.");
			Message message = new Message(EmailConstatnts.UNSUPPORTED_ENCODING_EXCEPTION, Message.MessageType.ERROR);
			List<Message> messages = new ArrayList<Message>();
			messages.add(message);
			ee.setMessages(messages);
			throw ee;
		}
		
	}
	
	/**
	 * add provided email address to bccAddressList
	 * @param internetAddress
	 */
	public void addBccAddress(String mailId, String alias){
		try {
			InternetAddress internetAddress = new InternetAddress(mailId, alias);
			addBccAddress(internetAddress);
		} catch (UnsupportedEncodingException uee) {
			BaseLoggers.exceptionLogger.error("addBccAddress", uee);
			EmailException ee = new EmailException();
			ee.setOriginalException(uee);
			ee.setLogMessage("Error in adding BCC Email Adress.");
			Message message = new Message(EmailConstatnts.UNSUPPORTED_ENCODING_EXCEPTION, Message.MessageType.ERROR);
			List<Message> messages = new ArrayList<Message>();
			messages.add(message);
			ee.setMessages(messages);
			throw ee;
		}
		
	}
	
	public void setFromAddress(String mailId, String alias){
		try {
			InternetAddress internetAddress = new InternetAddress(mailId, alias);
			setFromAddress(internetAddress);
		} catch (UnsupportedEncodingException uee) {
			BaseLoggers.exceptionLogger.error("setFromAddress", uee);
			EmailException ee = new EmailException();
			ee.setOriginalException(uee);
			ee.setLogMessage("Error in setting From Email Adress.");
			Message message = new Message(EmailConstatnts.UNSUPPORTED_ENCODING_EXCEPTION, Message.MessageType.ERROR);
			List<Message> messages = new ArrayList<Message>();
			messages.add(message);
			ee.setMessages(messages);
			throw ee;
		}
		
	}
	
	public void setReplyTo(String mailId, String alias){
		try {
			InternetAddress internetAddress = new InternetAddress(mailId, alias);
			setReplyTo(internetAddress);
		} catch (UnsupportedEncodingException uee) {
			BaseLoggers.exceptionLogger.error("setReplyTo", uee);
			EmailException ee = new EmailException();
			ee.setOriginalException(uee);
			ee.setLogMessage("Error in setting Reply To.");
			Message message = new Message(EmailConstatnts.UNSUPPORTED_ENCODING_EXCEPTION, Message.MessageType.ERROR);
			List<Message> messages = new ArrayList<Message>();
			messages.add(message);
			ee.setMessages(messages);
			throw ee;
		}
		
	}


	public void setFromAddress(InternetAddress fromAddress) {
		this.fromAddress = fromAddress;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}
	
	public void setToAddressList(List<InternetAddress> toAddressList) {
		this.toAddressList = toAddressList;
	}
	public void setCcAddressList(List<InternetAddress> ccAddressList) {
		this.ccAddressList = ccAddressList;
	}
	public void setBccAddressList(List<InternetAddress> bccAddressList) {
		this.bccAddressList = bccAddressList;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public void setMailSender(JavaMailSenderImpl mailSender) {
		this.mailSender = mailSender;
	}


	public void addAttachementFilePath(String attachementFilePath) {
		if(attachementFilePaths==null){
			attachementFilePaths = new ArrayList<String>();
		}
		this.attachementFilePaths.add(attachementFilePath);
	}

	public void setAttachementFilePaths(List<String> attachementFilePaths) {
		this.attachementFilePaths = attachementFilePaths;
	}

	public void setTemplateFilePath(String templateFilePath) {
		this.templateFilePath = templateFilePath;
	}

	public void addToInLineAttachmentMap(String identifierId, String attachmentFilePath) {
		if(this.inLineAttachmentMap==null){
			this.inLineAttachmentMap = new HashMap<String, String>();
		}
		this.inLineAttachmentMap.put(identifierId, attachmentFilePath) ;
	}
	
	public void setInLineAttachmentMap(Map<String, String> inLineAttachmentMap) {
		this.inLineAttachmentMap = inLineAttachmentMap;
	}

	public void setReplyTo(InternetAddress replyTo) {
		this.replyTo = replyTo;
	}

	public List<String> getAttachementFilePaths() {
		return attachementFilePaths;
	}

	public String getTemplateFilePath() {
		return templateFilePath;
	}

	public JavaMailSenderImpl getMailSender() {
		return mailSender;
	}

	public String getContent() {
		return content;
	}

	public boolean isEmailSendingAllowed() {
		return emailSendingAllowed;
	}

	public void setEmailSendingAllowed(boolean emailSendingAllowed) {
		this.emailSendingAllowed = emailSendingAllowed;
	}
	



}
