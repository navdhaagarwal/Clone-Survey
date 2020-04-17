package com.nucleus.web.common.controller;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.lang3.RandomStringUtils;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.nucleus.cfi.sms.pojo.ShortMessageSendResponsePojo;
import com.nucleus.cfi.sms.pojo.SmsMessage;
import com.nucleus.cfi.sms.service.ShortMessageIntegrationService;
import com.nucleus.contact.PhoneNumber;
import com.nucleus.contact.PhoneNumberType;
import com.nucleus.core.event.service.EventExecutionService;
import com.nucleus.core.genericparameter.service.GenericParameterService;
import com.nucleus.core.misc.util.DateUtils;
import com.nucleus.logging.BaseLoggers;
import com.nucleus.message.entity.MessageDeliveryStatus;
import com.nucleus.message.entity.ShortMessageExchangeRecord;
import com.nucleus.persistence.EntityDao;
import com.nucleus.user.UserService;
import com.nucleus.web.common.PhoneVerificationService;

import flexjson.JSONSerializer;

@Transactional
@Controller
@RequestMapping(value = "/PhoneNumber")
public class PhoneNumberController extends BaseController {

    @Inject
    @Named("userService")
    protected UserService                  userService;

    @Inject
    @Named("genericParameterService")
    private GenericParameterService        genericParameterService;

    @Inject
    @Named("shortMessageIntegrationService")
    private ShortMessageIntegrationService shortMessageIntegrationService;

    @Inject
    @Named("entityDao")
    protected EntityDao                    entityDao;    
    
    @Inject
    @Named(value = "eventExecutionService")
    private EventExecutionService eventExecutionService;   
  
    
    @Autowired(required = false)
    @Qualifier("phoneVerificationServiceImpl")
    PhoneVerificationService phoneVerService;


    @Value(value = "#{'${phone.code.for.regions}'}")
    private String                         phoneCodeForRegions;    
    
    public static final String MAX_RETRY_EXCEEDED = "Max Trial Reached";
    
    public static final String PHONE_DOES_NOT_EXIST = "Phone Number Does Not exist in the System";
    
    public static final String INVALID_OTP = "Invalid OTP/OTP Expired";
    
  
    
    public static final String DEFAULT_MOBILE_TOKEN_COMPLEXITY = "5";
    
    public static final String DEFAULT_MOBILE_TOKEN_VALIDITY_IN_MILLISECOND = "900000";
    
    public static final String EXTERNAL_PHONE_VERIFICATION_SYSTEM = "EXTERNAL";
    
    public static final String INTERNAL_PHONE_VERIFICATION_SYSTEM = "INTERNAL";    
    
    public static final String TOKEN_GENERATION_ERROR = "Error While Generating OTP/Token";
    
    public static final String PHONE_VERIFIED = "Number Verified Successfully";
    
    private static final String DELIVERED="DELIVERED";
    
    private static final String FAILED="FAILED";
    
    private static final String DELAYED="DELAYED";
    
    private static final String PENDING="PENDING";
    
    private static final String FAILED_TO_SEND = "FAILED_TO_SEND";

  

    @RequestMapping(value = "/loadCountryCodes")
    public @ResponseBody
    String loadPhoneNumber(ModelMap map) {
        PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();

        SortedMap<String, Object> numberMap = new TreeMap<String, Object>();
        String language = "en";

        String[] regionSet = getRegions();
        Map<String, String> otherMap = new HashMap<String, String>();

        if (regionSet != null) {
            for (String region : regionSet) {
                if (region != null && !region.equals("")) {
                    Locale loc = new Locale(language, region);
                    String countryName = loc.getCountry();
                    numberMap.put(countryName, "" + phoneUtil.getCountryCodeForRegion(region));
                }
            }
        }
        JSONSerializer iSerializer = new JSONSerializer();
        // countryCode is the code of current user
        otherMap.put("countryCode", userService.getUserLocale().getCountry());
        map.put("Supported_Regions_List", numberMap);

        otherMap.put("isMobileNumber", String.valueOf(genericParameterService.findByCode(PhoneNumberType.MOBILE_NUMBER,
                PhoneNumberType.class).getId()));
        otherMap.put("isLandlineNumber", String.valueOf(genericParameterService.findByCode(PhoneNumberType.LANDLINE_NUMBER,
                PhoneNumberType.class).getId()));

        numberMap.put("otherMap", otherMap);

        return iSerializer.serialize(numberMap);

    }

	@RequestMapping(value = "/sendVerCode/{id}")
	public @ResponseBody ResponseEntity<String> sendVerificationCode(ModelMap map, @PathVariable("id") Long id,
			@RequestParam(value = "taskId", required = false) String taskId) {

		PhoneNumber phoneNumber = entityDao.find(PhoneNumber.class, id);
		String returnIt = "";

		if (phoneNumber != null) {

			if (null != taskId && !taskId.isEmpty() && null != phoneVerService) {
				String phoneVerificationSystemType = phoneVerService.getPhoneVerificationSystemType();
				if (INTERNAL_PHONE_VERIFICATION_SYSTEM.equalsIgnoreCase(phoneVerificationSystemType)) {
					if (phoneVerService.isMaxTrialReached(phoneNumber)) {
						// Max Re-trial exceeded.
						return new ResponseEntity<>(MAX_RETRY_EXCEEDED, HttpStatus.FORBIDDEN);
					}

					String eventCode = phoneVerService.getEventCodeIfValidByTaskId(taskId);
					if (null != eventCode && !eventCode.isEmpty()) {
						String otp = phoneVerService.getOTPForPhoneVerification();
						String timeToken = phoneVerService.getTokenForPhoneVerification(taskId, phoneNumber, otp);
						DateTime expireVal = phoneVerService.getExpiryDateTime();

						Map<Object, Object> contextMap = phoneVerService.createContextMapForEventExec(taskId, timeToken,
								otp, phoneNumber);
						eventExecutionService.fireEventExecution(eventCode, contextMap, null);
						phoneNumber.setVerificationCode(otp);
						phoneNumber.setCodeExpirationTime(expireVal);
						phoneNumber.setVerificationToken(timeToken);
						phoneNumber.setTrialCount(
								null == phoneNumber.getTrialCount() ? 1 : phoneNumber.getTrialCount() + 1);
						DateTimeFormatter format = DateTimeFormat.forPattern(getUserDateFormat() + getAppTimeFormat());
						returnIt = format.print(expireVal);
						return new ResponseEntity<>(returnIt, HttpStatus.OK);
					}
				} else if (EXTERNAL_PHONE_VERIFICATION_SYSTEM.equalsIgnoreCase(phoneVerificationSystemType)) {
					Map<String, Object> resultMap = new HashMap<>();
					if (phoneVerService.sendOTPGenReqToThirdParty(taskId, phoneNumber, resultMap)) {
						phoneNumber.setCodeExpirationTime((DateTime) resultMap.get("tokenValidity"));
						DateTimeFormatter format = DateTimeFormat.forPattern(getUserDateFormat() + getAppTimeFormat());
						returnIt = format.print(phoneNumber.getCodeExpirationTime());
						return new ResponseEntity<>(returnIt, HttpStatus.OK);
					}
					return new ResponseEntity<>(TOKEN_GENERATION_ERROR, HttpStatus.SERVICE_UNAVAILABLE);
				}
			}

			String smsTo = phoneNumber.getIsdCode() + phoneNumber.getPhoneNumber();
			String smsBody = RandomStringUtils.randomNumeric(Integer.valueOf(DEFAULT_MOBILE_TOKEN_COMPLEXITY));

			// As limitation with our trial SMS account we can't send sms to
			// unverified numbers.
			
			SmsMessage smsMessage = new SmsMessage();
			smsMessage.setTo(smsTo);
			smsMessage.setBody(smsBody);
			ShortMessageExchangeRecord exchangeRecord = new ShortMessageExchangeRecord();
			exchangeRecord.setOwnerEntityUri(phoneNumber.getUri());
			exchangeRecord.setSmsBody(smsBody);
			exchangeRecord.setSmsTo(smsTo);
			try {
				ShortMessageSendResponsePojo messageSendResponsePojo = shortMessageIntegrationService
						.sendShortMessage(smsMessage);

				if (messageSendResponsePojo != null) {
					setDeliveryStatusinExchangeRecord(exchangeRecord, messageSendResponsePojo);
					phoneNumber.setVerCodeDeliveryStatus(exchangeRecord.getDeliveryStatus());
					phoneNumber.setVerCodeDelStatusMessage(exchangeRecord.getStatusMessage());
					exchangeRecord.setDeliveryTimestamp(messageSendResponsePojo.getReceiptTimestamp());
					
				}

			} catch (Exception e) {
				BaseLoggers.exceptionLogger.error("Error in sending verification code {}", smsBody);
				exchangeRecord.setDeliveryStatus(MessageDeliveryStatus.FAILED);
				phoneNumber.setVerCodeDeliveryStatus(MessageDeliveryStatus.FAILED);
			}
			entityDao.persist(exchangeRecord);
			phoneNumber.setVerificationCode(smsBody);
			// DateTime dateTime = DateTime.now().plusDays(2);
			DateTime dateTime = DateUtils.getCurrentUTCTime();
			dateTime = dateTime.plusMillis(Integer.valueOf(DEFAULT_MOBILE_TOKEN_VALIDITY_IN_MILLISECOND));
			phoneNumber.setCodeExpirationTime(dateTime);
			DateTimeFormatter format = DateTimeFormat.forPattern(getUserDateFormat() + getAppTimeFormat());
			returnIt = format.print(dateTime);
			return new ResponseEntity<>(returnIt, HttpStatus.OK);

		}
		return new ResponseEntity<>(PHONE_DOES_NOT_EXIST, HttpStatus.NOT_FOUND);

	}

	@RequestMapping(value = "/submitVerCode/{id}")
	public @ResponseBody ResponseEntity<String> submitVerificationCode(ModelMap map, @RequestParam("verCode") String verCode,
			@PathVariable("id") Long id, @RequestParam(value = "taskId", required = false) String taskId) {

		PhoneNumber phoneNumber = entityDao.find(PhoneNumber.class, id);

		if (phoneNumber != null) {
			if (null != taskId && !taskId.isEmpty() && null != phoneVerService) {
				String phoneVerificationSystemType = phoneVerService.getPhoneVerificationSystemType();
				if (EXTERNAL_PHONE_VERIFICATION_SYSTEM.equalsIgnoreCase(phoneVerificationSystemType)) {
					Map<String, Object> responseMap = phoneVerService.verifyPhoneOTPViaThirdParty(phoneNumber, verCode,
							taskId);
					if (HttpStatus.OK.equals(responseMap.get("responseStatus"))) {
						phoneNumber.setVerified(true);
					}
					return new ResponseEntity<>((String) responseMap.get("responseMessage"),
							(HttpStatus) responseMap.get("responseStatus"));
				}
			}
			if (null != phoneNumber.getVerificationCode() && null != phoneNumber.getCodeExpirationTime()) {
				if (phoneNumber.getCodeExpirationTime().isAfterNow()
						&& phoneNumber.getVerificationCode().equals(verCode)) {
					phoneNumber.setVerified(true);
					return new ResponseEntity<>(PHONE_VERIFIED, HttpStatus.OK);
				}
			}
			return new ResponseEntity<>(INVALID_OTP, HttpStatus.BAD_REQUEST);
		}
		return new ResponseEntity<>(PHONE_DOES_NOT_EXIST, HttpStatus.NOT_FOUND);
	}

    public String[] getRegions() {
        String[] regionCodes = phoneCodeForRegions.split(",");
        return regionCodes;

    }
    
	private void setDeliveryStatusinExchangeRecord(ShortMessageExchangeRecord exchangeRecord,
			ShortMessageSendResponsePojo messageSendResponsePojo) {
		
		if (messageSendResponsePojo.getDeliveryStatus().equals(DELIVERED))
			exchangeRecord.setDeliveryStatus(MessageDeliveryStatus.DELIVERED);
		else if (messageSendResponsePojo.getDeliveryStatus().equals(DELAYED))
			exchangeRecord.setDeliveryStatus(MessageDeliveryStatus.DELAYED);
		else if (messageSendResponsePojo.getDeliveryStatus().equals(FAILED)
				|| messageSendResponsePojo.getDeliveryStatus().equals(FAILED_TO_SEND))
			exchangeRecord.setDeliveryStatus(MessageDeliveryStatus.FAILED);
		else if (messageSendResponsePojo.getDeliveryStatus().equals(PENDING))
			exchangeRecord.setDeliveryStatus(MessageDeliveryStatus.PENDING);
		else
			exchangeRecord.setDeliveryStatus(MessageDeliveryStatus.NOT_APPLICABLE);
		exchangeRecord.setStatusMessage(messageSendResponsePojo.getMessageStatus());
		
	}
}
