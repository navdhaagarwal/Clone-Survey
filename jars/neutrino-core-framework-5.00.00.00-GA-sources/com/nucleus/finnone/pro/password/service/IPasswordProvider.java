package com.nucleus.finnone.pro.password.service;

import java.util.Map;

import com.nucleus.finnone.pro.communicationgenerator.domainobject.AttachmentEncryptionPolicy;

public interface IPasswordProvider {
    
	  String computePassword(AttachmentEncryptionPolicy encryptionPolicy, Map<String, Object> dataMap);
	
}
