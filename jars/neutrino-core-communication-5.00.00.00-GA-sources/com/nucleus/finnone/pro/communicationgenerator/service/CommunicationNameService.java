package com.nucleus.finnone.pro.communicationgenerator.service;

import static com.nucleus.finnone.pro.general.util.ValidatorUtils.hasElements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.web.multipart.commons.CommonsMultipartFile;

import com.nucleus.core.datastore.service.DatastorageService;
import com.nucleus.core.datastore.service.DocumentMetaData;
import com.nucleus.dao.query.NamedQueryExecutor;
import com.nucleus.entity.ApprovalStatus;
import com.nucleus.finnone.pro.communicationgenerator.businessobject.ICommunicationNameBusinessObject;
import com.nucleus.finnone.pro.communicationgenerator.constants.CommunicationType;
import com.nucleus.finnone.pro.communicationgenerator.domainobject.CommunicationName;
import com.nucleus.finnone.pro.communicationgenerator.domainobject.CommunicationParameter;
import com.nucleus.finnone.pro.communicationgenerator.domainobject.CommunicationTemplate;
import com.nucleus.finnone.pro.communicationgenerator.serviceinterface.ICommunicationNameService;
import com.nucleus.finnone.pro.communicationgenerator.vo.CommunicationNameVO;
import com.nucleus.finnone.pro.communicationgenerator.vo.CommunicationTemplateVo;
import com.nucleus.logging.BaseLoggers;
import com.nucleus.persistence.EntityDao;
import com.nucleus.rules.model.SourceProduct;
@Named("communicationNameService")
public class CommunicationNameService   implements ICommunicationNameService {

    @Inject
    @Named("communicationNameBusinessObject")
    private ICommunicationNameBusinessObject communicationNameBusinessObject;
    
    @Inject
    @Named("entityDao")
    protected EntityDao         entityDao;
    
    @Inject
    @Named("couchDataStoreDocumentService")
    private DatastorageService dataStorageService;
    
    @Override
    public List<CommunicationTemplate> getCommunicationTemplatesAssociatedWithComunicationName(
            Long id) {
        return communicationNameBusinessObject
                .getCommunicationTemplatesAssociatedWithComunicationName(id);

    }

    @Override
    public List<CommunicationName> getApprovedCommunicationNames() {

        return communicationNameBusinessObject.getApprovedCommunicationNames();

    }
    @Override
    public List<CommunicationName> getCommunicationNamesBasedOnCommunicationType(CommunicationType communicationType,SourceProduct sourceProduct)
    {
        return communicationNameBusinessObject.getCommunicationNamesBasedOnCommunicationType(communicationType,sourceProduct);
    }
    @Override
    public List<CommunicationParameter> getCommunicationParametersForAdHocBasedOnModule(
            SourceProduct sourceProduct) {
        return communicationNameBusinessObject
                .getCommunicationParametersForAdHocBasedOnModule(sourceProduct);

    }

	@Override
	public Map<Long, String> getApprovedParametersIdAndName() {
        NamedQueryExecutor<Map<String, Object>> queryExecutor = new NamedQueryExecutor<Map<String, Object>>(
                "Approved.Active.Parameters.IdAndCode").addParameter("approvalStatus", ApprovalStatus.APPROVED_RECORD_STATUS_LIST);
        List<Map<String, Object>> parametersList = entityDao.executeQuery(queryExecutor);
        Map<Long, String> parameterssMap = new LinkedHashMap<Long, String>();
        if (!CollectionUtils.isEmpty(parametersList)) {
            Iterator<Map<String, Object>> parameterIterator = parametersList.iterator();
            while (parameterIterator.hasNext()) {
                Map<String, Object> parameter = parameterIterator.next();

                if (parameter.get("code") != null) {
                    Long parameterId = (Long) parameter.get("id");
                    parameterssMap.put(parameterId, (String.valueOf(parameter.get("code"))));
                }
            }
        }

        if (parameterssMap.size() > 0)
            return parameterssMap;
        else
            return null;
    }

	@Override
	public CommunicationName convertToCommunicationName(CommunicationNameVO communicationNameVO) {


        CommunicationName communicationName=new CommunicationName();
        communicationName.setId(communicationNameVO.getId());
        communicationName.setActiveFlag(communicationNameVO.isActiveFlag());
        communicationName.setAdHocCommunication(communicationNameVO.isAdHocCommunication());
        communicationName.setCommunicationCode(communicationNameVO.getCommunicationCode());
        communicationName.setCommunicationName(communicationNameVO.getCommunicationName());
        communicationName.setCommunicationType(communicationNameVO.getCommunicationType());
        communicationName.setCustomerInternalFlag(communicationNameVO.getCustomerInternalFlag());
        communicationName.setSourceProduct(communicationNameVO.getSourceProduct());
        communicationName.setLocation(communicationNameVO.getLocation());
        communicationName.setCommunicationReferenceNumber(communicationNameVO.getCommunicationReferenceNumber());
        communicationName.setViewProperties((HashMap<String, Object>)communicationNameVO.getViewProperties());
        communicationName.setOperationType(communicationNameVO.getOperationType());
        List<CommunicationTemplateVo> communicationTemplateVos = communicationNameVO.getCommunicationTemplateVoList();
        if(hasElements(communicationTemplateVos)) {
        	 List<CommunicationTemplate> communicationTemplates = new ArrayList<>(communicationTemplateVos.size());
             for(CommunicationTemplateVo communicationTemplateVo : communicationTemplateVos) {
             	CommunicationTemplate communicationTemplate = this.convertToCommunicationTemplate(communicationTemplateVo); 
             	if(communicationTemplate.getCommunicationTemplateCode() != null && communicationTemplate.getCommunicationTemplateName() != null) {
             		communicationTemplate.setCommunication(communicationName);
                 	communicationTemplates.add(communicationTemplate);
             	}
             	
             }
             communicationName.setCommunicationTemplates(communicationTemplates);
        }
        return communicationName;
    
	
	}

	@Override
	public CommunicationNameVO convertToCommunicationNameVo(CommunicationName communicationName) {

		CommunicationNameVO communicationNameVO = new CommunicationNameVO();
        communicationNameVO.setId(communicationName.getId());
        communicationNameVO.setActiveFlag(communicationName.isActiveFlag());
        communicationNameVO.setAdHocCommunication(communicationName.isAdHocCommunication());
     
        List<CommunicationTemplate> communicationTemplates = communicationName.getCommunicationTemplates();
        int numberOfTemplatesInCommunicationName =communicationTemplates.size();
        List<CommunicationTemplateVo> communicationTemplateVos = new ArrayList<>(numberOfTemplatesInCommunicationName);
        for (CommunicationTemplate communicationTemplate  : communicationTemplates) {
			communicationTemplateVos.add(this.convertToCommunicationTemplateVo(communicationTemplate));
		}
        
        communicationNameVO.setCommunicationTemplateVoList(communicationTemplateVos);
        communicationNameVO.setCommunicationCode(communicationName.getCommunicationCode());
        communicationNameVO.setCommunicationName(communicationName.getCommunicationName());
        communicationNameVO.setCommunicationType(communicationName.getCommunicationType());
        communicationNameVO.setCustomerInternalFlag(communicationName.getCustomerInternalFlag());
        communicationNameVO.setSourceProduct(communicationName.getSourceProduct());
        communicationNameVO.setLocation(communicationName.getLocation());
        communicationNameVO.setCommunicationReferenceNumber(communicationName.getCommunicationReferenceNumber());
        communicationNameVO.setViewProperties(communicationName.getViewProperties());
		return communicationNameVO;
	
	}

	@Override
	public CommunicationTemplate convertToCommunicationTemplate(CommunicationTemplateVo communicationTemplateVo) {


		CommunicationTemplate communicationTemplate = new CommunicationTemplate();
		communicationTemplate.setId(communicationTemplateVo.getId());
		communicationTemplate.setCommunicationTemplateCode(communicationTemplateVo.getCommunicationTemplateCode());
		communicationTemplate.setCommunicationTemplateName(communicationTemplateVo.getCommunicationTemplateName());
		communicationTemplate.setOperationType(communicationTemplateVo.getOperationType());
		communicationTemplate.setSubject(communicationTemplateVo.getSubject());
		
		if (communicationTemplateVo.getUploadedTemplate() != null && communicationTemplateVo.getUploadedTemplate().getSize() != 0) {
			String attachmentCouchDbId = this.saveTemplateToStorageService(communicationTemplateVo.getUploadedTemplate());
			communicationTemplate.setUploadedDocumentId(attachmentCouchDbId);
			communicationTemplate.setCommunicationTemplateFile(communicationTemplateVo.getCommunicationTemplateFile());
		} else if (StringUtils.isEmpty(communicationTemplateVo.getUploadedDocumentId()) || !communicationTemplateVo.getCommunicationTemplateFile().equals(getFileNameForDocumentId(communicationTemplateVo.getUploadedDocumentId()))) {
			communicationTemplate.setCommunicationTemplateFile(communicationTemplateVo.getCommunicationTemplateFile());
			communicationTemplate.setUploadedDocumentId(null);
		} else {
			communicationTemplate.setUploadedDocumentId(communicationTemplateVo.getUploadedDocumentId());
		}
		return communicationTemplate;
	
	
	}

	private String saveTemplateToStorageService(CommonsMultipartFile uploadedTemplate) {

		String attachmentCouchDbId = null;
		try {
			attachmentCouchDbId = dataStorageService.saveDocument(uploadedTemplate.getInputStream(), FilenameUtils.removeExtension(uploadedTemplate.getOriginalFilename()), FilenameUtils.getExtension(uploadedTemplate.getOriginalFilename()));
			return attachmentCouchDbId;
		} catch (IOException e) {
			BaseLoggers.exceptionLogger.error("Error in saving communication template", e);
			throw new RuntimeException("Error in saving communication template", e);
		}
	
	}

	@Override
	public CommunicationTemplateVo convertToCommunicationTemplateVo(CommunicationTemplate communicationTemplate) {
		CommunicationTemplateVo communicationTemplateVo = new CommunicationTemplateVo();
		communicationTemplateVo.setId(communicationTemplate.getId());
		communicationTemplateVo.setCommunicationTemplateCode(communicationTemplate.getCommunicationTemplateCode());
		communicationTemplateVo.setCommunicationTemplateName(communicationTemplate.getCommunicationTemplateName());
		communicationTemplateVo.setSubject(communicationTemplate.getSubject());
		
		String documentId = communicationTemplate.getUploadedDocumentId();
		if (documentId != null) {
			communicationTemplateVo.setCommunicationTemplateFile(this.getFileNameForDocumentId(documentId));
		} else {
			communicationTemplateVo.setCommunicationTemplateFile(communicationTemplate.getCommunicationTemplateFile());
		}
		communicationTemplateVo.setUploadedDocumentId(communicationTemplate.getUploadedDocumentId());
		return communicationTemplateVo;
	
	
	}

	private String getFileNameForDocumentId(String documentId) {

		DocumentMetaData documentMetaData = getTemplateFromStorageService(documentId);
		return String.join(".", documentMetaData.getFileName(),documentMetaData.getFileExtension());
	
	}

	@Override
	public DocumentMetaData getTemplateFromStorageService(String documentId) {

		return dataStorageService.retrieveDocumentWithMetaData(documentId);
	
	}
	
	



}
