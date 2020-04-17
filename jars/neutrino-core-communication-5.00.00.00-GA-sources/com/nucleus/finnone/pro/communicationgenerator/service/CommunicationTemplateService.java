package com.nucleus.finnone.pro.communicationgenerator.service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.io.FilenameUtils;
import org.apache.tika.Tika;
import org.springframework.beans.factory.annotation.Value;

import com.nucleus.core.datastore.service.DocumentMetaData;
import com.nucleus.finnone.pro.communication.cache.service.ICommunicationCacheService;
import com.nucleus.finnone.pro.communicationgenerator.domainobject.CommunicationTemplate;
import com.nucleus.finnone.pro.communicationgenerator.serviceinterface.ICommunicationTemplateService;
import com.nucleus.persistence.EntityDao;

@Named("communicationTemplateService")
public class CommunicationTemplateService implements ICommunicationTemplateService {

	@Inject
    @Named("entityDao")
    private EntityDao entityDao;
	
	@Value("${template.root.path}")
    private String templateRootPath;
	
	@Inject
	@Named("tika")
	private Tika tika;

	@Inject
	@Named("communicationCacheService")
	private ICommunicationCacheService communicationCacheService ;
	
	
	@Override
	public CommunicationTemplate getCommunicationTemplateById(Long id) {
		CommunicationTemplate communicationTemplate = communicationCacheService.getCommunicationTemplate(id);
		if(communicationTemplate != null) {
			return communicationTemplate;
		}
		return entityDao.find(CommunicationTemplate.class, id);
	}

	@Override
	public DocumentMetaData getFileFromCommunicationTemplate(CommunicationTemplate communicationTemplate) throws IOException {
		File file  = new File(templateRootPath + communicationTemplate.getCommunicationTemplateFile());
		
		return new DocumentMetaData(Files.readAllBytes(file.toPath()), FilenameUtils.getExtension(file.getName()),tika.detect(file),file.getName()) ;
		
	}

	@Override
	public CommunicationTemplate saveCommunicationTemplate(CommunicationTemplate communicationTemplate) {
		return entityDao.saveOrUpdate(communicationTemplate);
	}
	
	@Override
	public	String findContentType(File file) throws IOException {
		String contentType = tika.detect(file);
		return contentType;
	}
	
	
}
