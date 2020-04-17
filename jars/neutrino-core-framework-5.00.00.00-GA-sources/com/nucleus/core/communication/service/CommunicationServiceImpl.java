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
package com.nucleus.core.communication.service;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;

import net.bull.javamelody.MonitoredWithSpring;

import org.apache.commons.lang3.StringUtils;
import org.apache.tika.Tika;
import org.springframework.context.MessageSource;

import com.nucleus.contact.EMailInfo;
import com.nucleus.core.communication.Communication;
import com.nucleus.core.communication.CommunicationTrail;
import com.nucleus.core.communication.EmailCommunication;
import com.nucleus.core.datastore.service.DatastorageService;
import com.nucleus.core.exceptions.SystemException;
import com.nucleus.core.misc.util.DateUtils;
import com.nucleus.core.validation.util.NeutrinoValidator;
import com.nucleus.dao.query.NamedQueryExecutor;
import com.nucleus.document.core.entity.Document;
import com.nucleus.logging.BaseLoggers;
import com.nucleus.service.BaseServiceImpl;
import com.nucleus.template.TemplateService;
import com.nucleus.user.UserService;

/**
 * @author Nucleus Software Exports Limited
 * 
 */

@Named("communicationService")
@MonitoredWithSpring(name = "communicationService_IMPL_")
public class CommunicationServiceImpl extends BaseServiceImpl implements CommunicationService {

    private static final String COMMUNICATION_ADDED_BY = ".communication.added.by";

    private static final String COMMUNICATION_TRANSCRIPT = ".communication.transcript";

    private static final String COMMUNICATION_FILE_NAME = ".communication.file.name";
    
    private static final String ADDED_BY = "system";
    
    private static final String DOT_SEPARATOR = ".";

    @Inject
    @Named("couchDataStoreDocumentService")
    private DatastorageService docService2;

    @Inject
    @Named("userService")
    private UserService        userService;

    @Inject
    @Named("templateService")
    protected TemplateService  templateService;

    @Inject
    @Named("messageSource")
    MessageSource              messageSource;
    

    @Override
    public void saveCommunication(Communication communication) {
        NeutrinoValidator.notNull(communication, "communication cannot be null during saving.");
        try {
            if (communication.getId() == null) {
                entityDao.persist(communication);
            } else {
                entityDao.update(communication);
            }
        } catch (Exception e) {
            throw new SystemException("Exception while saving communication", e);
        }
    }

    @Override
    /*    @MonitoredWithSpring(name = "CSI_FETCH_COMMUNICATION_BY_URI")*/
    public List<Communication> getAllCommunicationByUri(String ownerEntityUri) {
        NeutrinoValidator.notNull(ownerEntityUri, "ownerEntityUri cannot be null during saving.");
        try {
            NamedQueryExecutor<Communication> executor = new NamedQueryExecutor<Communication>(
                    "Communication.getAllCommunication").addParameter("entityUri", ownerEntityUri);
            return entityDao.executeQuery(executor);
        } catch (Exception e) {
            throw new SystemException("Exception while retrieving communications on the basis of given URI", e);
        }
    }

    @Override
    public List<Map<Integer, String>> getCommunicationTypes() {
        List<Map<Integer, String>> communicationType = new ArrayList<Map<Integer, String>>();

        Map<Integer, String> communicationTypeMap = new HashMap<Integer, String>();

        communicationTypeMap.put(1, "Phone");
        communicationTypeMap.put(2, "Email/EmailResponse");
        communicationTypeMap.put(3, "Personal");

        communicationType.add(communicationTypeMap);
        return communicationType;
    }

    @Override
    public List<Communication> getAllCommunicationByUriList(List<String> ownerEntityUriList) {
        NeutrinoValidator.notNull(ownerEntityUriList, "ownerEntityUri cannot be null during saving.");
        try {
            NamedQueryExecutor<Communication> executor = new NamedQueryExecutor<Communication>(
                    "Communication.getAllCommunicationForEntityUriList").addParameter("entityUriList", ownerEntityUriList);
            return entityDao.executeQuery(executor);
        } catch (Exception e) {
            throw new SystemException("Exception while retrieving communications on the basis of given URI", e);
        }
    }

	@Override
    public Document persistEmailCommunication(byte[] documentToPersist, String baseKeyName, String ownerEntityUri, Map contextMap)
            throws IOException {
    	return persistEmailCommunication( documentToPersist,  baseKeyName,  ownerEntityUri,  contextMap,  null);
    }

    @Override
    public Document persistEmailCommunication(byte[] documentToPersist, String baseKeyName, String ownerEntityUri, Map contextMap,String suffix)
            throws IOException {
       return  persistEmailCommunication(documentToPersist,baseKeyName,ownerEntityUri,contextMap,suffix,null);
    }

    @Override
    public Document persistEmailCommunication(byte[] documentToPersist, String baseKeyName, String ownerEntityUri, Map contextMap,String suffix, String tempName)
            throws IOException {

       
    	EmailCommunication emailCommunication = new EmailCommunication();
        //String uploadedFileName = baseKeyName.concat(COMMUNICATION_FILE_NAME);
        String uploadedFileName = null;
        if(tempName == null) {
            uploadedFileName = templateService.getResolvedStringFromResourceBundle(baseKeyName + COMMUNICATION_FILE_NAME, null, null);
            uploadedFileName = messageSource.getMessage(uploadedFileName, null, null, getUserLocale());
        }else{
            uploadedFileName = tempName;
        }
        String fileExtension = null;
        String mimeType = null;
        String couchDbId;
        String fileName= null;
        Document document = new Document();
        
        String stageForCAM = (String) contextMap.get("stageForCAMSave");

		if(suffix!=null){
		uploadedFileName+=suffix; 
		}
		
		 if(contextMap.get("fileExtension") != null && contextMap.get("mimeType") != null)
	        {
	        	fileExtension = contextMap.get("fileExtension").toString();
	        	mimeType = contextMap.get("mimeType").toString();
	        }
		
        // creating Attachment Object(single document)
        List<Document> docList = new ArrayList<Document>();
        // name to be stored in couch db
       
        
        if(mimeType == null && fileExtension == null)
        {
        	fileExtension="PDF";
        	mimeType="application/pdf";
        	fileName=uploadedFileName + ".pdf";
        }
        else{
        	fileName=uploadedFileName + DOT_SEPARATOR + fileExtension;
        }
        
    	couchDbId = docService2.saveDocument(new ByteArrayInputStream(documentToPersist), uploadedFileName,
    			fileExtension);
    	
    	document.setDocumentStoreId(couchDbId);
        document.setOrigReceivedFlag(false);
        document.setContentType(mimeType);
        document.setUploadedFileName(fileName);
        document.setFileSizeInBytes((int) documentToPersist.length);
        

        docList.add(document);

        // creating an instance of communication trail
        CommunicationTrail communicationTrail = new CommunicationTrail();
        communicationTrail.setAttachedDocuments(docList);
        communicationTrail.setIsCustomerTranscript(false);

        // from property file
        //String communicationTranscript = baseKeyName.concat(COMMUNICATION_TRANSCRIPT);
        String communicationTranscript=null;
        String addedBy=null;
        if(tempName==null) {
            communicationTranscript = templateService.getResolvedStringFromResourceBundle(baseKeyName + COMMUNICATION_TRANSCRIPT, null, null);
            communicationTranscript = messageSource.getMessage(communicationTranscript, null, null, getUserLocale());
            addedBy =  templateService.getResolvedStringFromResourceBundle(baseKeyName + COMMUNICATION_ADDED_BY, null, null);
            addedBy = messageSource.getMessage(addedBy, null, null, null);
        }else{
            communicationTranscript=tempName;
            addedBy="system";
        }
        communicationTrail.setCommunicationTranscript(communicationTranscript);

        // setting properties of top object email-communication and
        // persist it.
        emailCommunication.setCommunicationMode(2);
        EMailInfo eMailInfo = new EMailInfo();
        emailCommunication.setContactEmail(eMailInfo);
        //String addedBy = baseKeyName.concat(COMMUNICATION_ADDED_BY);
        emailCommunication.setAddedBy(addedBy);
		emailCommunication.setContactedBy(userService.getUserIdByUserName(addedBy).toString());
        emailCommunication.setContactTime(DateUtils.getCurrentUTCTime());
        emailCommunication.setOwnerEntityUri(ownerEntityUri);
        emailCommunication.addCommunicationTrail(communicationTrail);
        emailCommunication.setStage(stageForCAM);

        saveCommunication(emailCommunication);
        
        return document;
    }

    @Override
    public List<Communication> getOtherThanAppointmentCommunicationOrderByCreationTimeStamp(String ownerEntityUri) {
        NeutrinoValidator.notNull(ownerEntityUri, "ownerEntityUri cannot be null during saving.");
        try {
            NamedQueryExecutor<Communication> executor = new NamedQueryExecutor<Communication>(
                    "Communication.getOtherThanAppointmentCommunicationOrderByCreationTimeStamp").addParameter("entityUri", ownerEntityUri);
            return entityDao.executeQuery(executor);
        } catch (Exception e) {
            throw new SystemException("Exception while retrieving communications on the basis of given URI", e);
        }
    }
    
    @Override
    public List<Communication> getAppointmentFromCommunicationHistory(String ownerEntityUri) {
        NeutrinoValidator.notNull(ownerEntityUri, "ownerEntityUri cannot be null during saving.");
       try {
          NamedQueryExecutor<Communication> executor = new NamedQueryExecutor<Communication>(
                    "Communication.getAppointmentFromCommunicationHistory").addParameter("entityUri", ownerEntityUri);
           return entityDao.executeQuery(executor);
       } catch (Exception e) {
            throw new SystemException("Exception while retrieving communications on the basis of given URI", e);
        }
    }

	@Override
	public void persistDynamicGeneratedEmailAttachment(
			byte[] documentToPersist, File attachFile,
			String ownerEntityUri, Map contextMap, String suffix,
			String mimeType) throws IOException {
		
		if(attachFile!=null && attachFile.getName()!=null){
			String baseKeyName = attachFile.getName();
			String extension=baseKeyName.substring(baseKeyName.lastIndexOf('.') + 1); 
    		if (baseKeyName.indexOf('.') > -1)
    			baseKeyName = baseKeyName.substring(0, baseKeyName.lastIndexOf('.'));
    		
    		
    	
    		EmailCommunication emailCommunication = new EmailCommunication();
    		
    		if (suffix != null) {
    			baseKeyName += suffix;
    		}

    		// creating Attachment Object(single document)
    		List<Document> docList = new ArrayList<>();

    		// name to be stored in couch db
    		String couchDbId = docService2.saveDocument(new ByteArrayInputStream(documentToPersist), baseKeyName, extension);

    		Document document = new Document();
    		document.setDocumentStoreId(couchDbId);
    		document.setOrigReceivedFlag(false);
    		document.setContentType(mimeType);
    		document.setUploadedFileName(baseKeyName + "." + extension);
    		document.setFileSizeInBytes((int) documentToPersist.length);
    		docList.add(document);

    		// creating an instance of communication trail
    		CommunicationTrail communicationTrail = new CommunicationTrail();
    		communicationTrail.setAttachedDocuments(docList);
    		communicationTrail.setIsCustomerTranscript(false);
    		communicationTrail.setCommunicationTranscript(baseKeyName);
    		// from property file
    		
    		// setting properties of top object email-communication and persist it.
    		emailCommunication.setCommunicationMode(2);
    		EMailInfo eMailInfo = new EMailInfo();
    		emailCommunication.setContactEmail(eMailInfo);
    		
    		String addedBy = ADDED_BY;
    		emailCommunication.setAddedBy(addedBy);
			emailCommunication.setContactedBy(userService.getUserIdByUserName(addedBy).toString());
    		emailCommunication.setContactTime(DateUtils.getCurrentUTCTime());
    		emailCommunication.setOwnerEntityUri(ownerEntityUri);
    		emailCommunication.addCommunicationTrail(communicationTrail);
    		saveCommunication(emailCommunication);
        
		}		
	}

}