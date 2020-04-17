package com.nucleus.finnone.pro.communicationgenerator.businessobject;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import com.nucleus.finnone.pro.communicationgenerator.constants.CommunicationType;
import com.nucleus.finnone.pro.communicationgenerator.dao.ICommunicationNameDAO;
import com.nucleus.finnone.pro.communicationgenerator.domainobject.CommunicationName;
import com.nucleus.finnone.pro.communicationgenerator.domainobject.CommunicationParameter;
import com.nucleus.finnone.pro.communicationgenerator.domainobject.CommunicationTemplate;
import com.nucleus.rules.model.SourceProduct;

@Named("communicationNameBusinessObject")
public class CommunicationNameBusinessObject implements ICommunicationNameBusinessObject {

	@Inject
	@Named("communicationNameDAO")
	ICommunicationNameDAO communicationNameDAO;
	
	@Override
	public List<CommunicationTemplate> getCommunicationTemplatesAssociatedWithComunicationName(Long id) {
		return communicationNameDAO.getCommunicationTemplatesAssociatedWithComunicationName(id);
	}
	
	@Override
	public List<CommunicationName> getApprovedCommunicationNames(){
		return communicationNameDAO.getApprovedCommunicationNames();
		
	}
	
	@Override
    public List<CommunicationParameter> getCommunicationParametersForAdHocBasedOnModule(SourceProduct sourceProduct){
        return communicationNameDAO
                .getCommunicationParametersForAdHocBasedOnModule(sourceProduct);
        
    }
	

	@Override
	public List<CommunicationName> getCommunicationNamesBasedOnCommunicationType(CommunicationType communicationType,SourceProduct sourceProduct) {
		
		return communicationNameDAO.getCommunicationNamesBasedOnCommunicationType(communicationType,sourceProduct);
	}
	
	

}
