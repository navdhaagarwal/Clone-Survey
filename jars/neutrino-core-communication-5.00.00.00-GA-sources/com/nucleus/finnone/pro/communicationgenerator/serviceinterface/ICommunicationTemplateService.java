package com.nucleus.finnone.pro.communicationgenerator.serviceinterface;

import java.io.File;
import java.io.IOException;

import com.nucleus.core.datastore.service.DocumentMetaData;
import com.nucleus.finnone.pro.communicationgenerator.domainobject.CommunicationTemplate;

public interface ICommunicationTemplateService {
	CommunicationTemplate getCommunicationTemplateById(Long id);
	DocumentMetaData getFileFromCommunicationTemplate(CommunicationTemplate communicationTemplate) throws IOException;
	CommunicationTemplate saveCommunicationTemplate(CommunicationTemplate communicationTemplate);
	String findContentType(File file) throws IOException ;
}
