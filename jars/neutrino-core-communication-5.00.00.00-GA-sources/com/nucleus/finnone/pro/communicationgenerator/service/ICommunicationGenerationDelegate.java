package com.nucleus.finnone.pro.communicationgenerator.service;

import java.util.List;
import java.util.Map;

import com.nucleus.finnone.pro.communicationgenerator.domainobject.CommunicationRequestDetail;
import com.nucleus.finnone.pro.communicationgenerator.vo.DataPreparationServiceMethodVO;
import com.nucleus.finnone.pro.communicationgenerator.vo.GeneratedContentVO;

public interface ICommunicationGenerationDelegate {

	GeneratedContentVO generateCommunication(CommunicationRequestDetail communicationRequestDetail,
			Map<String, Object> localCacheMap, Map<String, List<DataPreparationServiceMethodVO>> additionalMethodsMap,
			boolean onDemand, boolean generateContentOnly);

	GeneratedContentVO generateCommunicationInNewTransaction(CommunicationRequestDetail communicationRequestDetail,
			Map<String, Object> localCacheMap, Map<String, List<DataPreparationServiceMethodVO>> additionalMethodsMap,
			boolean onDemand, boolean generateContentOnly);

	GeneratedContentVO generateSchedularBasedCommunicationInNewTransaction(
			CommunicationRequestDetail communicationRequestDetailId, Map<String, Object> localCacheMap,
			Map<String, List<DataPreparationServiceMethodVO>> additionalMethodsMap, boolean onDemand,
			boolean generateContentOnly);

}