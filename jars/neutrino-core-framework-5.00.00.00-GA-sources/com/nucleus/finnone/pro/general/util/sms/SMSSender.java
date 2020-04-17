package com.nucleus.finnone.pro.general.util.sms;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import javax.inject.Inject;
import javax.inject.Named;

import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.PhoneNumberUtil.PhoneNumberFormat;
import com.google.i18n.phonenumbers.Phonenumber.PhoneNumber;
import com.nucleus.finnone.pro.base.Message;
import com.nucleus.finnone.pro.base.exception.ExceptionBuilder;
import com.nucleus.finnone.pro.general.util.email.EmailException;
import com.nucleus.finnone.pro.general.util.email.constants.EmailConstatnts;
import com.nucleus.finnone.pro.general.util.templatemerging.TemplateMergingUtility;
import com.nucleus.logging.BaseLoggers;
import org.apache.commons.codec.binary.Base64;
import org.springframework.scheduling.annotation.Async;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

public class SMSSender {

	/**
	 * URL for sending sms on single number
	 */
	private String gatewayURLForSingleSMS;
	/**
	 * URL for sending sms on multiple number
	 */
	private String gatewayURLForBulkSMS;
	/**
	 * proxy server in between?
	 */
	private boolean throughProxy;
	/**
	 * Proxy server host
	 */
	private String proxyHost;
	/**
	 * Proxy server port
	 */
	private String proxyPort;
	/**
	 * Proxy server user id
	 */
	private String proxyUserId;
	/**
	 * Proxy server password
	 */
	private String proxyPassword;	
	/**
	 * Encoding of message content
	 */
	private String encoding;
	/**
	 * property to check if sms sending is allowed, defaults to true
	 */
	private boolean smsSendingAllowed=false;
	private static PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance(); 
	private static String EMPTY_STRING="";
	/**
	 * gateway URL parameters
	 */
	private Map<String,String> gatewayProperties=null;
	/**
	 * URL parameter for holding recipient's phone number
	 */
	private String destinationNumberKey;
	/**
	 * URL parameter for holding sms content
	 */
	private String messageKey;
	/**
	 * Template file path for getting SMS content if message content is not file path
	 */
	private String templatePath;
	/**
	 * 
	 */
	@Inject
	@Named("templateMergingUtility")
	private TemplateMergingUtility templateMergingUtil;
	
	@Async
	@Transactional(propagation=Propagation.REQUIRED)
	public String pushAsynchronousSMS(SmsVO smsVO) {
			return sendSMS(smsVO);
	}
	
	/**
	 * 
	 * @param smsVO
	 */
	public String sendSMS(SmsVO smsVO) {
		if(!smsSendingAllowed){
			return "Configuration prohibits sending SMS";
		}
		updateSMSContentFromTemplateIfNeeded(smsVO);
		validateSMS(smsVO);
		String urlString = prepareURLString(smsVO);
		URL url = this.prepareURL(urlString);
		return performPOSTOnSmsURL(url);
	}
	
	/**
	 * 
	 * @param countryCode
	 * @param localCode
	 * @param phoneNumber
	 * @param message
	 */
	public String sendSMS(int countryCode, String localCode, long phoneNumber, String message) {
		
		SmsVO smsVO = new SmsVO();
		smsVO.addTelephoneNumber(countryCode, localCode, phoneNumber);		
		return this.sendSMS(smsVO);
	}
/**
 * 
 * @param telephoneNumber
 * @param message
 */
	public String sendSMS(PhoneNumber telephoneNumber, String message) {
		SmsVO smsVO = new SmsVO();
		smsVO.addTelephoneNumber(telephoneNumber);		
		return this.sendSMS(smsVO);
	}
	
	/**
	 * 
	 */
	protected void updateSMSContentFromTemplateIfNeeded(SmsVO smsVO){
		if(smsVO.getMessage()==null){
			String templateFilePath=smsVO.getTemplateFilePath();
			if(templateFilePath==null){
				templateFilePath=templatePath;
			}
			String messageText = templateMergingUtil.mergeTemplateIntoString(templateFilePath, smsVO.getTemplateMappingObject());
			smsVO.setMessage(messageText);
		}
	}
	/**
	 * Validates whether recipient's number and message are set and is correct
	 * @param smsVO
	 */
	protected void validateSMS(SmsVO smsVO) {
		List<Message> validationmessages = new ArrayList<Message>();
		Message validationmessage = null;
		List<PhoneNumber> telephoneNumbers = smsVO.getTelephoneNumbers();
		
		if (telephoneNumbers == null || telephoneNumbers.size() == 0) {
			BaseLoggers.exceptionLogger.error("No recipients provided");
			validationmessage = new Message(SMSMessageConstants.NO_RECIPIENT_PROVIDED, Message.MessageType.ERROR);
			validationmessages.add(validationmessage);
			
		}else{
			for(PhoneNumber telephoneNumber: telephoneNumbers){
				if(! phoneUtil.isValidNumber(telephoneNumber)){
					BaseLoggers.exceptionLogger.error(phoneUtil.format(telephoneNumber, PhoneNumberFormat.E164)+" is not valid.");
					validationmessage = new Message(SMSMessageConstants.INVALID_PHONE_NUMBER, Message.MessageType.ERROR, phoneUtil.format(telephoneNumber, PhoneNumberFormat.E164));
					validationmessages.add(validationmessage);
				}
			}
		}
		if(smsVO.getMessage()==null || EMPTY_STRING.equals(smsVO.getMessage().trim())){
			validationmessage = new Message(SMSMessageConstants.MSG_STRING_EMPTY, Message.MessageType.ERROR);
			validationmessages.add(validationmessage);
		}
		if (validationmessages.size() > 0) {
			SMSException se = new SMSException();
			se.setLogMessage("SMS validation failed");
			se.setMessages(validationmessages);
			throw se;
		}
	}

	/**
	 * 
	 * @param strURL
	 * @return
	 */
	protected URL prepareURL(String strURL) {
		URL url = null;
		try {			
			url = new URL(strURL);			
		} catch (Exception e) {
			List<Message> errorMessages = new ArrayList<Message>();
			errorMessages.add(new Message(SMSMessageConstants.MALFORMED_URL,Message.MessageType.ERROR));
			throw ExceptionBuilder.getInstance(SMSException.class, SMSMessageConstants.MALFORMED_URL, "Error in preparring SMS URL.").setMessages(errorMessages).setOriginalException(e).build();
		}
		return url;
	}
	/**
	 * 
	 * @param url
	 * @return
	 */
	protected String performPOSTOnSmsURL(final URL url) {
		HttpURLConnection conn = null;
		String responseMessage = "";
		try {
			conn = (HttpURLConnection) url.openConnection();
			if (throughProxy) {
				Properties systemSettings = System.getProperties();
				systemSettings.put("proxySet", "true");
				systemSettings.put("proxyHost", proxyHost);
				systemSettings.put("proxyPort", proxyPort);
				System.setProperties(systemSettings);
				String usrPwd = proxyUserId + ":" + proxyPassword;
				String encodedUserPwd = new String(Base64.encodeBase64(usrPwd.getBytes(this.encoding)),this.encoding);
				conn.setRequestProperty("Proxy-Authorization", "Basic "
						+ encodedUserPwd);
			}
			conn.connect();
			responseMessage = conn.getResponseMessage();
			BaseLoggers.flowLogger.info("Response message from SMS gateway is "+responseMessage);
			//Need to check response code - whether 200, 404 or null
			conn.disconnect();
		} catch (IOException ioe) {
			BaseLoggers.exceptionLogger.error("httpPostSMSURL", ioe);
			List<Message> errorMessages = new ArrayList<Message>();
			errorMessages.add(new Message(SMSMessageConstants.CONNECTION_EXCEPTION,Message.MessageType.ERROR));
			throw ExceptionBuilder.getInstance(SMSException.class, SMSMessageConstants.CONNECTION_EXCEPTION, "Error in connecting SMS URL.").setMessages(errorMessages).setOriginalException(ioe).build();
		}
		return responseMessage;
	}
	

	/**
	 * 
	 * @param telephoneNumbers
	 * @return
	 */
	private String convertPhoneNumbersAsString(List<PhoneNumber> telephoneNumbers){
		String phoneNumberAsString=null;
		if(telephoneNumbers.size()==1){
			phoneNumberAsString=getPhoneNumberString(telephoneNumbers.get(0));
		}else{
			StringBuilder phoneNumbers = new StringBuilder();
			for (PhoneNumber telephoneNumber : telephoneNumbers) {
				phoneNumbers.append(",").append(getPhoneNumberString(telephoneNumber));
			}
			phoneNumberAsString= phoneNumbers.substring(1);
		}
		
		return phoneNumberAsString;
	}
	/**
	 * 
	 * @param phoneNumber
	 * @return
	 */
	private String getPhoneNumberString(PhoneNumber phoneNumber){
		
		//We should have used phoneUtil.format(phoneNumber, PhoneNumberFormat.E164) - but current gateway do not support it.
		//String telePhoneNumber = phoneUtil.format(phoneNumber, PhoneNumberFormat.E164);
		String telePhoneNumber = "";
		if(phoneNumber.hasCountryCode()){
			telePhoneNumber = telePhoneNumber + phoneNumber.getCountryCode();
		}
		if(phoneNumber.hasPreferredDomesticCarrierCode()){
			telePhoneNumber = telePhoneNumber + phoneNumber.getPreferredDomesticCarrierCode();
		}
		if(phoneNumber.hasNationalNumber()){
			telePhoneNumber = telePhoneNumber + phoneNumber.getNationalNumber();
		}		
		return telePhoneNumber;
	}
	/**
	 * Prepares parameters in SMS URL 
	 * @param smsVO
	 * @return
	 */
	private String prepareURLString(SmsVO smsVO) {
		StringBuilder urlString = new StringBuilder();
		String phoneNumberString = null;		
		List<PhoneNumber> telephoneNumbers = smsVO.getTelephoneNumbers();
		if (telephoneNumbers.size() == 1) {
			urlString.append(this.gatewayURLForSingleSMS);
		} else {
			urlString.append(this.gatewayURLForBulkSMS);	
		}
		phoneNumberString = convertPhoneNumbersAsString(telephoneNumbers);
		try {
			
			for(Entry<String,String> gatewayProperty: gatewayProperties.entrySet()){
				if(urlString.indexOf("?")<0){
					urlString.append("?");
				}else{
					urlString.append("&");
				}
				urlString.append(gatewayProperty.getKey()).append("=");
				urlString.append(URLEncoder.encode(gatewayProperty.getValue(),this.encoding));
			}
			urlString.append("&").append(destinationNumberKey).append("=");
			urlString.append(URLEncoder.encode(phoneNumberString,this.encoding));

			urlString.append("&").append(messageKey).append("=");
			urlString.append(URLEncoder.encode(smsVO.getMessage(),this.encoding));

		} catch (UnsupportedEncodingException uee) {
			BaseLoggers.exceptionLogger.error("prepareURLString", uee);
			SMSException se = new SMSException();
			se.setOriginalException(uee);
			se.setLogMessage("Error in Sending SMS.");
			Message validationmessage = new Message("Unsupported Encoding Exception", Message.MessageType.ERROR);
			List<Message> validationmessages = new ArrayList<Message>();
			validationmessages.add(validationmessage);
			se.setMessages(validationmessages);
			throw se;
		}

		return urlString.toString();
	}

	public String getGatewayURLForSingleSMS() {
		return gatewayURLForSingleSMS;
	}

	public void setGatewayURLForSingleSMS(String gatewayURLForSingleSMS) {
		this.gatewayURLForSingleSMS = gatewayURLForSingleSMS;
	}

	public String getGatewayURLForBulkSMS() {
		return gatewayURLForBulkSMS;
	}

	public void setGatewayURLForBulkSMS(String gatewayURLForBulkSMS) {
		this.gatewayURLForBulkSMS = gatewayURLForBulkSMS;
	}

	public String getProxyHost() {
		return proxyHost;
	}

	public void setProxyHost(String proxyHost) {
		this.proxyHost = proxyHost;
	}

	public String getProxyPort() {
		return proxyPort;
	}

	public void setProxyPort(String proxyPort) {
		this.proxyPort = proxyPort;
	}

	public String getProxyUserId() {
		return proxyUserId;
	}

	public void setProxyUserId(String proxyUserId) {
		this.proxyUserId = proxyUserId;
	}

	public String getProxyPassword() {
		return proxyPassword;
	}

	public void setProxyPassword(String proxyPassword) {
		this.proxyPassword = proxyPassword;
	}

	
	public String getMessageKey() {
		return messageKey;
	}

	public void setMessageKey(String messageKey) {
		this.messageKey = messageKey;
	}

	
	public boolean isThroughProxy() {
		return throughProxy;
	}

	public void setThroughProxy(boolean throughProxy) {
		this.throughProxy = throughProxy;
	}

	public String getEncoding() {
		return encoding;
	}

	public void setEncoding(String encoding) {
		this.encoding = encoding;
	}

	public Map<String, String> getGatewayProperties() {
		return gatewayProperties;
	}

	public void setGatewayProperties(Map<String, String> gatewayProperties) {
		this.gatewayProperties = gatewayProperties;
	}

	public String getDestinationNumberKey() {
		return destinationNumberKey;
	}

	public void setDestinationNumberKey(String destinationNumberKey) {
		this.destinationNumberKey = destinationNumberKey;
	}
	public String getTemplatePath() {
		return templatePath;
	}
	public void setTemplatePath(String templatePath) {
		this.templatePath = templatePath;
	}

	public boolean isSmsSendingAllowed() {
		return smsSendingAllowed;
	}

	public void setSmsSendingAllowed(boolean smsSendingAllowed) {
		this.smsSendingAllowed = smsSendingAllowed;
	}

	
	
}