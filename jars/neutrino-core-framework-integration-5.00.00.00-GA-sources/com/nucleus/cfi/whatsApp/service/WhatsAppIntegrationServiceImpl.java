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
package com.nucleus.cfi.whatsApp.service;

import java.io.IOException;

import javax.annotation.PostConstruct;
import javax.inject.Named;

import org.slf4j.Logger;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nucleus.cfi.whatsApp.pojo.WhatsAppMessage;
import com.nucleus.cfi.whatsApp.pojo.WhatsAppMessageSendResponse;
import com.nucleus.logging.BaseLoggers;

/**
 * @author Nucleus Software Exports Limited
 *
 */
@Named("whatsAppIntegrationService")
public class WhatsAppIntegrationServiceImpl implements WhatsAppIntegrationService {

	protected final Logger LOGGER = BaseLoggers.integrationLogger;;

	public static final String FAILED = "FAILED";

	public static final String FAILED_TO_SEND = "FAILED_TO_SEND";


	private RestTemplate restTemplate;
	
    public static final String ACCESS_TOKEN = "access_token";



	@Override
	public WhatsAppMessageSendResponse sendWhatsAppMessage(WhatsAppMessage whatsAppMessage, String url, String token) throws IOException {

		BaseLoggers.exceptionLogger.error("-------------------- The request for sendWhatsAppMessage "
				+ "is being sent to this URL ------------------------> " + url);
		  ResponseEntity<String> responseEntity=getResponseEntity(url, whatsAppMessage, HttpMethod.POST, String.class, token);
		  WhatsAppMessageSendResponse whatsAppMessageSendResponse = getObjectFromResponse(responseEntity.getBody());
		  return whatsAppMessageSendResponse;
	}

	/**
	 * 
	 * @param url
	 * @param requestEntity
	 * @param httpMethod
	 * @param responseType
	 * @param token
	 * @return
	 */
	
	 private <T> ResponseEntity<T> getResponseEntity(String url, Object requestEntity, HttpMethod httpMethod, Class<T> responseType, String token) {
	        HttpHeaders headers = new HttpHeaders();
	        headers.set(ACCESS_TOKEN,token);
	        HttpEntity<Object> entityReq = new HttpEntity<Object>(requestEntity, headers);

	        return restTemplate.exchange(url,httpMethod,entityReq,responseType);

	    }
	
	 
	 /**
	  * 
	  * @param responseString
	  * @return
	 * @throws IOException 
	  */
	 
	 private WhatsAppMessageSendResponse getObjectFromResponse(String responseString) throws IOException{

		 WhatsAppMessageSendResponse whatsAppMessageSendResponse;
	        ObjectMapper objectMapper = new ObjectMapper();
	        try {
	        	  whatsAppMessageSendResponse= objectMapper.readValue(responseString, WhatsAppMessageSendResponse.class);
	        } catch (JsonParseException e) {
	        	whatsAppMessageSendResponse = null;
	            BaseLoggers.flowLogger.debug("Exception occured while converting Response String to whatsAppMessageSendResponse : ", e);
	            BaseLoggers.flowLogger.info("Exception occured while converting Response String to whatsAppMessageSendResponse : " , e);

	        } catch (JsonMappingException e) {
	        	whatsAppMessageSendResponse = null;
	            BaseLoggers.flowLogger.debug("Exception occured while converting Response String to whatsAppMessageSendResponse : " , e);
	            BaseLoggers.flowLogger.info("Exception occured while converting Response String to whatsAppMessageSendResponse : " , e);
	            throw e;

	        } catch (IOException e) {
	        	whatsAppMessageSendResponse = null;
	            BaseLoggers.flowLogger.debug("Exception occuredwhile converting Response String to whatsAppMessageSendResponse : " , e);
	            BaseLoggers.flowLogger.info("Exception occured while converting Response String to whatsAppMessageSendResponse : " , e);
	            throw e;

	        }
	        return whatsAppMessageSendResponse;
	    
		}
	 
	@PostConstruct
	void initializeRestTemplate() {
		this.restTemplate = new RestTemplate();
	}
	
	
	
}
