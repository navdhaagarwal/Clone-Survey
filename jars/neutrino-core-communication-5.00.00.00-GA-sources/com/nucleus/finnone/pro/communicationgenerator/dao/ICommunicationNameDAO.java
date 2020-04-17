package com.nucleus.finnone.pro.communicationgenerator.dao;

import java.util.List;

import com.nucleus.finnone.pro.communicationgenerator.constants.CommunicationType;
import com.nucleus.finnone.pro.communicationgenerator.domainobject.CommunicationName;
import com.nucleus.finnone.pro.communicationgenerator.domainobject.CommunicationParameter;
import com.nucleus.finnone.pro.communicationgenerator.domainobject.CommunicationTemplate;
import com.nucleus.rules.model.SourceProduct;

public interface ICommunicationNameDAO {

	 List<CommunicationTemplate> getCommunicationTemplatesAssociatedWithComunicationName(Long id);
	 
	 List<CommunicationName> getApprovedCommunicationNames();
	 
	 List<CommunicationName> getApprovedInitializedCommunication();
	 
	 List<CommunicationTemplate> getAllInitializedCommunicationTemplates();
	 
	 List<CommunicationName> getCommunicationNamesBasedOnCommunicationType(CommunicationType communicationType,SourceProduct sourceProduct);
	 
	 List<CommunicationParameter> getCommunicationParametersForAdHocBasedOnModule(SourceProduct sourceProduct);
	 
}
