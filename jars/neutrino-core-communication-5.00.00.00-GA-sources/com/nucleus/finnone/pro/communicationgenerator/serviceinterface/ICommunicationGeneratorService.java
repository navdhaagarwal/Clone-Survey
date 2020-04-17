package com.nucleus.finnone.pro.communicationgenerator.serviceinterface;

import java.util.List;
import java.util.Map;

import com.nucleus.finnone.pro.communicationgenerator.domainobject.CommunicationDataPreparationDetail;
import com.nucleus.finnone.pro.communicationgenerator.domainobject.CommunicationName;
import com.nucleus.finnone.pro.communicationgenerator.domainobject.CommunicationRequestDetail;
import com.nucleus.finnone.pro.communicationgenerator.domainobject.CommunicationTemplate;
import com.nucleus.finnone.pro.communicationgenerator.vo.CommunicationGenerationDetailVO;
import com.nucleus.finnone.pro.communicationgenerator.vo.DataPreparationServiceMethodVO;
import com.nucleus.rules.model.SourceProduct;

public interface ICommunicationGeneratorService {
	
	List<DataPreparationServiceMethodVO> findAdditionalMethodsForCommunicationDataPreperation(String communicationCode);
	
	List<CommunicationTemplate> getTemplateByCommunicationMasterId(Long communicationMasterId);
	
	
	List<CommunicationDataPreparationDetail> getActiveApprovedDetailBasedOnServiceSouceAndModule(SourceProduct module,Long serviceSelectionId);
	
	void generateCommunications();
	
	List<CommunicationRequestDetail> getCommunicationGenerationDetail(CommunicationGenerationDetailVO communicationGenerationDetailVO);
	
	void logAndGenerateCommunicationsForCommunicationRequests(List<CommunicationName> communicationList,
			SourceProduct module, Map<Object, Object> parameters);

	void generateCommunications(String communicationCode,SourceProduct module);
	
	void generateCommunications(CommunicationName communicationName, SourceProduct module,
			Map<Object, Object> parameters, Map<String, Object> localCacheMap,
			Map<String, List<DataPreparationServiceMethodVO>> additionalMethodsMap);

	void logAndGenerateCommunicationsForCommunicationRequests(SourceProduct module, Map<Object, Object> parameters);	
	
	
}	
