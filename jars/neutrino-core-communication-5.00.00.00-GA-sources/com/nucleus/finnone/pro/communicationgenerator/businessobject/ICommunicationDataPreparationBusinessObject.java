package com.nucleus.finnone.pro.communicationgenerator.businessobject;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.nucleus.finnone.pro.communicationgenerator.domainobject.CommunicationDataPreparationDetail;
import com.nucleus.finnone.pro.communicationgenerator.domainobject.CommunicationRequestDetail;
import com.nucleus.finnone.pro.communicationgenerator.vo.DataPreparationServiceMethodVO;
import com.nucleus.rules.model.SourceProduct;

public interface ICommunicationDataPreparationBusinessObject {
	
	Map<String, Object> getInitializedReferencedObjects(CommunicationRequestDetail communicationRequestDetail,
			Map<String, Object> localCacheMap);
	Map<String, Object> callAdditionalMethods(Set<DataPreparationServiceMethodVO> dataPreparationServiceMethodVOs,CommunicationRequestDetail communicationRequestDetail);
	Map<String,Object> prepareContextMapForTemplateSelectionOrCommunicationGenerationOrAdhocAndBulk(SourceProduct module,Map<String,Object> contextMap,Map<String,Object> localCacheMap,String serviceSelectionCode);
	CommunicationDataPreparationDetail getCommunicationPreparationDetail(String serviceSelectionCode, SourceProduct module);
	List<CommunicationDataPreparationDetail> getCommunicationPreparationDetails(String serviceSelectionCode,
			SourceProduct module);
	
}