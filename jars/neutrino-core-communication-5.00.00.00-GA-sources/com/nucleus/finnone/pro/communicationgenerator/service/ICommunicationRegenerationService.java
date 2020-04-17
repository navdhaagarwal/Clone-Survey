package com.nucleus.finnone.pro.communicationgenerator.service;

import java.util.Map;

public interface ICommunicationRegenerationService {
	
	public  Map<String, byte[]> regenerateStoredLetterByUniqueRequestId(String uniqueRequestId);
	
	public boolean resendCommunication(String uniqueRequestId);

}
