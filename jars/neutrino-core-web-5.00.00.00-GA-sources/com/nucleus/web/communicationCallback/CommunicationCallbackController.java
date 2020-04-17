package com.nucleus.web.communicationCallback;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;

import com.nucleus.cfi.message.vo.GenericMessageResponse;
import com.nucleus.cfi.message.vo.MessageChannels;
import org.hibernate.Hibernate;
import org.joda.time.DateTime;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.nucleus.finnone.pro.base.exception.SystemException;
import com.nucleus.finnone.pro.communicationgenerator.businessobject.ICommunicationGeneratorBusinessObject;
import com.nucleus.finnone.pro.communicationgenerator.dao.ICommunicationGeneratorDAO;
import com.nucleus.finnone.pro.communicationgenerator.domainobject.CommunicationRequestDetail;
import com.nucleus.finnone.pro.communicationgenerator.util.CommunicationConstants;
import com.nucleus.finnone.pro.communicationgenerator.util.CommunicationGenerationHelper;
import com.nucleus.finnone.pro.general.util.ValidatorUtils;
import com.nucleus.logging.BaseLoggers;
import com.nucleus.mail.entity.MailMessageExchangeRecord;
import com.nucleus.mail.entity.MailMessageExchangeRecordHistory;
import com.nucleus.message.entity.MessageDeliveryStatus;
import com.nucleus.message.entity.MessageExchangeRecord;
import com.nucleus.message.entity.MessageExchangeRecordHistory;
import com.nucleus.web.common.controller.BaseController;

@Controller
@RequestMapping(value = "/CommunicationCallback")
public class CommunicationCallbackController extends BaseController {
	
	@Inject
	@Named("communicationGeneratorDAO")
	private ICommunicationGeneratorDAO communicationGeneratorDAO;
	
	@Inject
	@Named("communicationGeneratorBusinessObject")
	private ICommunicationGeneratorBusinessObject communicationGeneratorBusinessObject;
	
	@Inject
	@Named("communicationGenerationHelper")
	private CommunicationGenerationHelper communicationGenerationHelper;
	
	private static final String UNIQUE_ID = "uniqueId";
	private static final String STATUS = "status";
	private static final String EXCEPTION = "Exception";
	private static final String SMS_EXCHANGE_RECORD_TYPE = "SMS";
	private static final String EMAIL_EXCHANGE_RECORD_TYPE = "EMAIL";
	//private static final String WHATSAPP_EXCHANGE_RECORD_TYPE = "WHATSAPP";
	private static final String SUCCESS = "SUCCESS";
	private static final String ERROR = "ERROR";
	
	@RequestMapping(value = "/callbackFunction", method = RequestMethod.GET)
	@ResponseBody
	public Map<String, Object> callbackFunction(HttpServletRequest request, ModelMap model) {
		return processCRDAndExchangeRecord(request, SMS_EXCHANGE_RECORD_TYPE);
	}
	
	@RequestMapping(value = "/emailCallback", method = RequestMethod.GET)
	@ResponseBody
	public Map<String, Object> emailCallback(HttpServletRequest request, ModelMap model) {
		return processCRDAndExchangeRecord(request, EMAIL_EXCHANGE_RECORD_TYPE);
	}

	@RequestMapping(value = "/genericMessageCallback", method = RequestMethod.POST, produces = "application/json" , consumes = "application/json")
	@ResponseBody
	public Map<String, Object> genericMessageCallback(@RequestBody GenericMessageResponse genericMessageResponse, ModelMap model) {

		return processCRDAndExchangeRecordForGenericMessage(genericMessageResponse);
	}
	
	@RequestMapping(value = "/emailReadTrack", method = RequestMethod.GET)
	@ResponseBody
	public String emailReadTracking(HttpServletRequest request) {
		return trackEmailRead(request);
	}
	
	private String trackEmailRead(HttpServletRequest request) {
		String uniqueIdentifier = request.getParameter(UNIQUE_ID);
		if (ValidatorUtils.isNull(uniqueIdentifier)) {
			BaseLoggers.exceptionLogger.error("uniqueId not found in callback request. It can not be null.");
			return HttpStatus.BAD_REQUEST.name();
		}
		try {
			MailMessageExchangeRecord mailMessageExchangeRecord = communicationGeneratorDAO.getMessageExchangeRecordByUniqueId(CommunicationConstants.CACHED_MAIL_CLASS, uniqueIdentifier);
			if (mailMessageExchangeRecord != null && !MessageDeliveryStatus.READ.equals(mailMessageExchangeRecord.getDeliveryStatus())) {
				//This code snippet will not execute if message is already there.
				mailMessageExchangeRecord.setDeliveryStatus(MessageDeliveryStatus.READ);
				mailMessageExchangeRecord.setReadTimestamp(new DateTime());
				communicationGeneratorBusinessObject.moveMessageToHistoryAndDeleteGeneratedMessageRecord(mailMessageExchangeRecord, null);
			} else {
			    updateMessageExchangeRecordHistory(uniqueIdentifier, MessageDeliveryStatus.READ);
			}
			return SUCCESS;
		} catch (Exception e) {
			BaseLoggers.exceptionLogger.error("Callback Controller failed for unique id : "+ uniqueIdentifier, e);
			return ERROR;
		}
	}

	private void updateMessageExchangeRecordHistory(String uniqueIdentifier, MessageDeliveryStatus messageStatus) {
		MessageExchangeRecordHistory messageExchnageRecordHistory = communicationGeneratorDAO.getMessageExchangeRecordHistoryByUniqueId(uniqueIdentifier);
		if (ValidatorUtils.notNull(messageExchnageRecordHistory) && !messageStatus.equals(messageExchnageRecordHistory.getDeliveryStatus())) {
			messageExchnageRecordHistory.setDeliveryStatus(messageStatus);
			if (messageStatus.equals(MessageDeliveryStatus.READ)) {
				((MailMessageExchangeRecordHistory) messageExchnageRecordHistory).setReadTimestamp(new DateTime());
			}
			communicationGeneratorDAO.updateMessageExchangeRecordHistory(messageExchnageRecordHistory);
		}		
	}

	/**
	 * CRD delete and move to history call back to product processor message
	 * exchange record delete and move to history.
	 */
	private Map<String, Object> processCRDAndExchangeRecord(HttpServletRequest request, String exchangeRecordType) {
		Map<String, Object> responseMap = new HashMap<>();
		String uniqueIdentifier = request.getParameter(UNIQUE_ID);
		String status = request.getParameter(STATUS);
		boolean isEmailType = EMAIL_EXCHANGE_RECORD_TYPE.equals(exchangeRecordType);
		boolean isSmsType = SMS_EXCHANGE_RECORD_TYPE.equals(exchangeRecordType);

		Class<?> cachedMessageExchangeRecordClass = null;

		if (isEmailType){
			cachedMessageExchangeRecordClass = CommunicationConstants.CACHED_MAIL_CLASS;
		}
		else if (isSmsType){
			cachedMessageExchangeRecordClass = CommunicationConstants.CACHED_SMS_CLASS;
		}



		if (ValidatorUtils.isNull(uniqueIdentifier) || ValidatorUtils.isNull(status)) {
			responseMap.put(STATUS, HttpStatus.BAD_REQUEST);
			responseMap.put(EXCEPTION, "uniqueId not found in callback request. It can not be null.");
			return responseMap;
		}

		responseMap = saveOrUpdateCRDAndMessageExchangeRecord(responseMap, uniqueIdentifier, status, cachedMessageExchangeRecordClass);

		return responseMap;
	}

	private Map<String, Object> processCRDAndExchangeRecordForGenericMessage(GenericMessageResponse genericMessageResponse) {
		Map<String, Object> responseMap = new HashMap<>();


		if (genericMessageResponse == null){
			responseMap.put(STATUS, HttpStatus.NO_CONTENT);
			responseMap.put(EXCEPTION, "Response Value is null. Cannot be processed");
			return responseMap;
		}

		boolean isWhatsappType = false;

		if (genericMessageResponse.getMessageChannel().equals(MessageChannels.WHATSAPP)){
			isWhatsappType = true;
		}


		String uniqueIdentifier = null;
		String status = null;

		Class<?> cachedMessageExchangeRecordClass = null;

		uniqueIdentifier = genericMessageResponse.getUniqueRequestId();
		status = genericMessageResponse.getDeliveryStatus();


		if (isWhatsappType){

			cachedMessageExchangeRecordClass = CommunicationConstants.CACHED_WHATSAPP_CLASS;
		}




		if (ValidatorUtils.isNull(uniqueIdentifier) || ValidatorUtils.isNull(status)) {
			responseMap.put(STATUS, HttpStatus.BAD_REQUEST);
			responseMap.put(EXCEPTION, "uniqueId not found in callback request. It can not be null.");
			return responseMap;
		}

		responseMap = saveOrUpdateCRDAndMessageExchangeRecord(responseMap, uniqueIdentifier, status, cachedMessageExchangeRecordClass);

		return responseMap;
	}


	private Map<String, Object> saveOrUpdateCRDAndMessageExchangeRecord (Map<String, Object> responseMap, String uniqueIdentifier, String status, Class<?> cachedMessageExchangeRecordClass){

		try {
			CommunicationRequestDetail communicationRequestDetail = communicationGeneratorDAO
					.getCommunicationGenerationDetailByUniqueId(uniqueIdentifier);
			if (ValidatorUtils.notNull(communicationRequestDetail)) {
				Hibernate.initialize(communicationRequestDetail.getAdditionalData());
				communicationGenerationHelper.initializeCommunicationRequestDetailFromCache(communicationRequestDetail);
				communicationGeneratorBusinessObject.updateCommunicationRequestAndMoveToHistory(communicationRequestDetail, status, true);
			}
			MessageExchangeRecord messageExchangeRecord = communicationGeneratorDAO
					.getMessageExchangeRecordByUniqueId(cachedMessageExchangeRecordClass, uniqueIdentifier);
			if (messageExchangeRecord != null) {
				if (!MessageDeliveryStatus.SENT_TO_INTEGRATION.equals(messageExchangeRecord.getDeliveryStatus())) {
					throw new SystemException("MessageExchangeRecord processing exception.");
				}
				if (ValidatorUtils.isNull(messageExchangeRecord.getDeliveryTimestamp())) {
					messageExchangeRecord.setDeliveryTimestamp(new DateTime());
				}
				communicationGeneratorBusinessObject
						.moveMessageToHistoryAndDeleteGeneratedMessageRecord(messageExchangeRecord, status);
			} else {
				updateMessageExchangeRecordHistory(uniqueIdentifier, MessageDeliveryStatus.DELIVERED);
			}
			responseMap.put(STATUS, HttpStatus.OK);
		} catch (Exception e) {
			responseMap.put(STATUS, HttpStatus.INTERNAL_SERVER_ERROR);
			responseMap.put(EXCEPTION, e.getMessage());
			BaseLoggers.exceptionLogger.error("Callback Controller failed for unique id : " + uniqueIdentifier, e);
		}

		return responseMap;

	}
}
