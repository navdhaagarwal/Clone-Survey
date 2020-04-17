package com.nucleus.pushnotification.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;

import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import com.google.common.collect.Maps;
import com.nucleus.core.common.NeutrinoRestTemplateFactory;
import com.nucleus.finnone.pro.base.exception.SystemException;
import com.nucleus.logging.BaseLoggers;
import com.nucleus.master.BaseMasterService;
import com.nucleus.pushnotification.vo.PushNotificationData;
import com.nucleus.pushnotification.vo.PushNotificationRequest;
import com.nucleus.pushnotification.vo.PushNotificationResponse;
import com.nucleus.security.oauth.service.RESTfulAuthenticationService;
import com.nucleus.ws.core.entities.PushNotificationClientDetail;

@Named("pushNotificationSenderService")
public class PushNotificationSenderService {

	@Inject
	@Named("baseMasterService")
	private BaseMasterService baseMasterService;

	private RestTemplate restTemplate;
	@Autowired
	private NeutrinoRestTemplateFactory neutrinoRestTemplateFactory;
	
	
	@Value(value = "#{'${cfi.ws.client.url.pushNotificationServiceURL}'}")
	private String api;
	@Value(value = "#{'${config.pushNotificationService.authorization.key}'}")
	private String authorization;
	private static final String SUCCESS = "success";
	
	@Inject
	@Named("oauthauthenticationService")
	private RESTfulAuthenticationService oauthauthenticationService;

	@Value("${soap.service.trusted.client.id}")
	private String clientID;
	
	@Inject
	@Named("pushNotificationClientService")
	private PushNotificationClientService pushNotificationClientService;

	/**
	 * This method will return NoticationClientId vs Response from google api
	 * Empty map is returned in case no NoticationClientId is registered
	 * @param msg
	 * @return
	 */
	@Deprecated
	public Map<String, PushNotificationResponse> sendNotifaction(String msg,String accessToken)  {
		Map<String, PushNotificationResponse> responseMap = null;
		List<PushNotificationClientDetail> pushNotificationClientDtl = baseMasterService
				.getAllApprovedAndActiveEntities(PushNotificationClientDetail.class);
		if (!pushNotificationClientDtl.isEmpty()) {
			responseMap = pushNotificationClientDtl.parallelStream()
					.map(entry -> this.sendNotificationToIntg(entry, msg,accessToken)).collect(Collectors
							.toMap(responseEntry -> responseEntry.getKey(), responseEntry -> responseEntry.getValue()));

		}
		return responseMap==null?new HashMap<>():responseMap;

	}
	
	/**
	 * This method will return NoticationClientId vs Response from google api
	 * Empty map is returned in case no NoticationClientId is registered
	 * @param msg
	 * @return
	 */
	public Map<String, PushNotificationResponse> sendNotifaction(String msg)  {
		Map<String, PushNotificationResponse> responseMap = null;
		List<PushNotificationClientDetail> pushNotificationClientDtl = baseMasterService
				.getAllApprovedAndActiveEntities(PushNotificationClientDetail.class);
		if (!pushNotificationClientDtl.isEmpty()) {
			responseMap = pushNotificationClientDtl.parallelStream()
					.map(entry -> this.sendNotificationToIntg(entry, msg,getAccessToken(clientID))).collect(Collectors
							.toMap(responseEntry -> responseEntry.getKey(), responseEntry -> responseEntry.getValue()));

		}
		return responseMap==null?new HashMap<>():responseMap;

	}
	
	
	/**
	 * This method will return NoticationClientId vs Response from google api, takes msg and list of fcm ids as input
	 * Empty map is returned in case no NoticationClientId is registered
	 * @param msg
	 * @param notificationClientIds
	 * @return
	 */
	public Map<String, PushNotificationResponse> sendNotifactionToSpecificUsers(String msg,List<String> notificationClientIds)  {
		Map<String, PushNotificationResponse> responseMap = null;
		List<PushNotificationClientDetail> pushNotificationClientDtl = pushNotificationClientService.findActivePushNotificationClientDetailByNotificationClientIds(notificationClientIds);
		if (!pushNotificationClientDtl.isEmpty()) {
			responseMap = pushNotificationClientDtl.parallelStream()
					.map(entry -> this.sendNotificationToIntg(entry, msg,getAccessToken(clientID))).collect(Collectors
							.toMap(responseEntry -> responseEntry.getKey(), responseEntry -> responseEntry.getValue()));

		}
		return responseMap==null?new HashMap<>():responseMap;

	}
	
	/**
	 * This method will return NoticationClientId vs Response from google api, takes msg and list of user ids as input
	 * Empty map is returned in case no NoticationClientId is registered
	 * @param msg
	 * @param userIds
	 * @param trustedSourceNames
	 * @return
	 */
	public Map<String, PushNotificationResponse> sendNotificationToSpecificUsersByUserIdAndTrustedSourceModules(String msg,List<Long> userIds,List<String> trustedSourceNames)  {
		Map<String, PushNotificationResponse> responseMap = null;
		List<PushNotificationClientDetail> pushNotificationClientDtl = null;
				if(trustedSourceNames == null || trustedSourceNames.isEmpty()){
					pushNotificationClientDtl = pushNotificationClientService.findActivePushNotificationClientDetailByUserIds(userIds);
				}else{
					pushNotificationClientDtl = pushNotificationClientService.findActivePushNotificationClientDetailByUserIdsAndTrustedSourceModules(userIds,trustedSourceNames);
				}
		if (pushNotificationClientDtl!=null && !pushNotificationClientDtl.isEmpty()) {
			responseMap = pushNotificationClientDtl.parallelStream()
					.map(entry -> this.sendNotificationToIntg(entry, msg,getAccessToken(clientID))).collect(Collectors
							.toMap(responseEntry -> responseEntry.getKey(), responseEntry -> responseEntry.getValue()));

		}
		return responseMap==null?new HashMap<>():responseMap;

	}
	
	private String getAccessToken(String clientId) {
		String accessToken = oauthauthenticationService.getSecurityToken(clientID);
		BaseLoggers.flowLogger.debug("Access token generated for client id: {} token generated is: {}",clientID,accessToken);
		if(accessToken==null) {
			BaseLoggers.flowLogger.error("Access token generated for client id: {} is null",clientID);
			throw new RuntimeException("Access Token can't be null for client id: "+clientId);
		}
		
		return accessToken;
	}

	private Map.Entry<String, PushNotificationResponse> sendNotificationToIntg(
			PushNotificationClientDetail pushNotificationClientDtl, String msg,String accessToken) {

		
		if(authorization==null ||authorization.equals(""))
		{
		   throw	new SystemException("Authorization key is not configured for Push Notification Service");
		}
		PushNotificationResponse res = new PushNotificationResponse();

		HttpEntity<PushNotificationRequest> pushNotificationRequest = prepareRequestData(pushNotificationClientDtl,
				msg, accessToken);
		try {

			ResponseEntity<JSONObject> responseMap = restTemplate.postForEntity(api, pushNotificationRequest,
					JSONObject.class);
			if (responseMap.getStatusCodeValue() == 200 && responseMap.getBody().get(SUCCESS) != null
					&& (responseMap.getBody().get(SUCCESS)).equals(1)) {
				res.setSuccess(true);

			} else if (responseMap.getStatusCodeValue() == 200 && responseMap.getBody().get(SUCCESS) != null
					&& ( responseMap.getBody().get(SUCCESS)).equals(0)) {
				res.setSuccess(false);
				res.setErrorMsg((String)((HashMap)((ArrayList)responseMap.getBody().get("results")).get(0)).get("error"));
			}
		} catch (Exception ex) {
			res.setSuccess(false);
			res.setErrorMsg(ex.getMessage());
			BaseLoggers.exceptionLogger.error("Exception occured while sending push Notification", ex);
		}
		return Maps.immutableEntry(pushNotificationClientDtl.getNotificationClientId(), res);

	}

	@PostConstruct
	void initializeRestTemplate() {
		this.restTemplate = neutrinoRestTemplateFactory.createRestTemplate(null, null, null, null, null);
	}

	private HttpEntity<PushNotificationRequest> prepareRequestData(
			PushNotificationClientDetail pushNotificationClientDtl, String msg,String accessToken) {

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.add("Authorization", authorization);
		headers.add("access_token", accessToken);
		PushNotificationRequest pushNotificationRequest = new PushNotificationRequest();
		PushNotificationData data = new PushNotificationData();
		data.setMessage(msg);
		data.setId(pushNotificationClientDtl.getUserId());
		data.setTitle(pushNotificationClientDtl.getTrustedSourceId());
		data.setUserId(pushNotificationClientDtl.getUsername());
		pushNotificationRequest.setData(data);
		pushNotificationRequest.setTo(pushNotificationClientDtl.getNotificationClientId());
		return new HttpEntity<>(pushNotificationRequest, headers);

	}
	

}
