package com.nucleus.finnone.pro.communicationgenerator.serviceinterface;

import java.util.List;
import java.util.Map;

import com.nucleus.core.datastore.service.DocumentMetaData;
import com.nucleus.finnone.pro.communicationgenerator.constants.CommunicationType;
import com.nucleus.finnone.pro.communicationgenerator.domainobject.CommunicationName;
import com.nucleus.finnone.pro.communicationgenerator.domainobject.CommunicationParameter;
import com.nucleus.finnone.pro.communicationgenerator.domainobject.CommunicationTemplate;
import com.nucleus.finnone.pro.communicationgenerator.vo.CommunicationNameVO;
import com.nucleus.finnone.pro.communicationgenerator.vo.CommunicationTemplateVo;
import com.nucleus.rules.model.SourceProduct;

public interface ICommunicationNameService {
		
	    
	    List<CommunicationTemplate> getCommunicationTemplatesAssociatedWithComunicationName(Long id);
	    List<CommunicationName> getApprovedCommunicationNames();
	    List<CommunicationName> getCommunicationNamesBasedOnCommunicationType(CommunicationType communicationType,SourceProduct sourceProduct);
	    List<CommunicationParameter> getCommunicationParametersForAdHocBasedOnModule(SourceProduct sourceProduct);
	    public Map<Long, String> getApprovedParametersIdAndName();
	    
	    CommunicationName convertToCommunicationName(CommunicationNameVO communicationNameVO);
	    CommunicationNameVO convertToCommunicationNameVo(CommunicationName communicationName);
	    
	    CommunicationTemplate convertToCommunicationTemplate(CommunicationTemplateVo communicationTemplateVo); 
	    CommunicationTemplateVo convertToCommunicationTemplateVo(CommunicationTemplate communicationTemplate);
	    DocumentMetaData getTemplateFromStorageService(String documentId);
	    
}
