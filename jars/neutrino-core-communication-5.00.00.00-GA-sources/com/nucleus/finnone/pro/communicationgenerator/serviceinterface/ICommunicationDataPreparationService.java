package com.nucleus.finnone.pro.communicationgenerator.serviceinterface;

import java.util.List;
import java.util.Map;

import com.nucleus.finnone.pro.communicationgenerator.domainobject.CommunicationRequestDetail;
import com.nucleus.finnone.pro.communicationgenerator.vo.DataPreparationServiceMethodVO;
import com.nucleus.rules.model.SourceProduct;

public interface ICommunicationDataPreparationService {
	Map<String, Object> prepareContextMapForTemplateSelectionOrCommunicationGeneration(
			SourceProduct module, Map<String, Object> contextMap,
			Map<String, Object> localCacheMap, Boolean isOnDemandGeneration,
			String serviceSelectionCode);

	Map<String, Object> prepareDataForCommunicationGeneration(
			CommunicationRequestDetail communicationRequestDetail,	
			Map<String, Object> contextMap,
			Map<String, Object> localCacheMap,
			Map<String, List<DataPreparationServiceMethodVO>> additionalMethodsMap);

	Map<String, Object> getInitializedReferencedObjects(
			CommunicationRequestDetail communicationRequestDetail, Map<String, Object> localCacheMap);
}
