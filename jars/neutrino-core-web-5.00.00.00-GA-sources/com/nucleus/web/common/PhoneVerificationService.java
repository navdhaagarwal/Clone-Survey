package com.nucleus.web.common;

import java.util.Map;

import org.joda.time.DateTime;
import org.springframework.stereotype.Component;

import com.nucleus.contact.PhoneNumber;

@Component

public interface PhoneVerificationService {

	public String getEventCodeIfValidByTaskId(String taskId);

	public Map<Object, Object> createContextMapForEventExec(String taskId, String timeToken, String otp,
			PhoneNumber phoneNumber);

	public String getTokenForPhoneVerification(String taskId, PhoneNumber phoneNumber, String otp);

	public boolean isMaxTrialReached(PhoneNumber phoneNumber);

	public String getOTPForPhoneVerification();

	public DateTime getExpiryDateTime();

	public String getPhoneVerificationSystemType();

	public boolean sendOTPGenReqToThirdParty(String taskId, PhoneNumber phoneNumber, Map<String, Object> resultMap);

	public Map<String, Object> verifyPhoneOTPViaThirdParty(PhoneNumber phoneNumber, String otp, String taskId);

}
