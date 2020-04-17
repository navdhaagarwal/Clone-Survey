package com.nucleus.finnone.pro.communicationgenerator.service;

import java.util.Map;

import com.nucleus.finnone.pro.communicationgenerator.domainobject.AttachmentEncryptionPolicy;
import com.nucleus.finnone.pro.communicationgenerator.domainobject.CommunicationName;
import com.nucleus.finnone.pro.communicationgenerator.domainobject.CommunicationTemplate;
import com.nucleus.finnone.pro.password.service.IPasswordProvider;

public interface ICommunicationPasswordProvider extends IPasswordProvider{
    
	      String computePassword(AttachmentEncryptionPolicy encryptionPolicy, CommunicationName communication,
            CommunicationTemplate communicationTemplate, Map<String, Object> dataMap);

}
