package com.nucleus.finnone.pro.communication.cache.service;

import java.util.List;
import java.util.Map;

import com.nucleus.finnone.pro.communicationgenerator.domainobject.CommunicationDataPreparationDetail;
import com.nucleus.finnone.pro.communicationgenerator.domainobject.CommunicationName;
import com.nucleus.finnone.pro.communicationgenerator.domainobject.CommunicationTemplate;
import com.nucleus.finnone.pro.communicationgenerator.util.ServiceSelectionCriteria;
import com.nucleus.finnone.pro.communicationgenerator.vo.DataPreparationServiceMethodVO;
import com.nucleus.rules.model.SourceProduct;

public interface ICommunicationCacheService {
	
	public static final String RETRY_ATTEMPTS					= "RETRY_ATTEMPTS";

	public ServiceSelectionCriteria getServiceSelectionCriteria(String serviceSelectionCriteriaCode);

	public CommunicationDataPreparationDetail getActiveApprovedDetailBasedOnServiceSouceAndModule(SourceProduct sourceProduct,
			String serviceSelectionCriteriaCode);
	
	public CommunicationName getCommunicationName(Long id);
	
	public CommunicationTemplate getCommunicationTemplate(Long id);

	

	public Map<String, Integer> getRetryAttemptsConfiguration();

	public void initializeCommunication(CommunicationName communicationName);

	public void refreshCommunicationCache(Map<String, Object> argument);
public List<DataPreparationServiceMethodVO> getAdditionalMethodsForDataPreparation(String communicationCode);

List<CommunicationDataPreparationDetail> getActiveApprovedDetailsBasedOnServiceSouceAndModule(
		SourceProduct sourceProduct, String serviceSelectionCriteriaCode);
}
