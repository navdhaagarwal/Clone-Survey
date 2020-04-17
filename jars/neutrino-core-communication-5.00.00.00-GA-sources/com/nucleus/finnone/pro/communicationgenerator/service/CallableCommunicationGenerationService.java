package com.nucleus.finnone.pro.communicationgenerator.service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import com.nucleus.core.NeutrinoSpringAppContextUtil;
import com.nucleus.finnone.pro.communicationgenerator.vo.DataPreparationServiceMethodVO;
import com.nucleus.finnone.pro.communicationgenerator.vo.GeneratedContentVO;
import com.nucleus.standard.context.NeutrinoContextInitializationAspect;

public class CallableCommunicationGenerationService implements Callable<GeneratedContentVO>{

	private Long communicationRequestDtlId;
	
	private Map<String, Object> localCacheMap;
	
	private Map<String, List<DataPreparationServiceMethodVO>> additionalMethodsMap;
	
	private boolean onDemand;
	
	private boolean generateContentOnly;
	
	
    private ICommunicationGenerationDelegate communicationGenerationDelegate;

    
	public ICommunicationGenerationDelegate getSingleCommunicationGenerationService() {
		return communicationGenerationDelegate;
	}

	public void setSingleCommunicationGenerationService(
			ICommunicationGenerationDelegate communicationGenerationDelegate) {
		this.communicationGenerationDelegate = communicationGenerationDelegate;
	}

	public Map<String, Object> getLocalCacheMap() {
		return localCacheMap;
	}

	public void setLocalCacheMap(Map<String, Object> localCacheMap) {
		this.localCacheMap = localCacheMap;
	}

	public Map<String, List<DataPreparationServiceMethodVO>> getAdditionalMethodsMap() {
		return additionalMethodsMap;
	}

	public void setAdditionalMethodsMap(Map<String, List<DataPreparationServiceMethodVO>> additionalMethodsMap) {
		this.additionalMethodsMap = additionalMethodsMap;
	}

	public boolean isOnDemand() {
		return onDemand;
	}

	public void setOnDemand(boolean onDemand) {
		this.onDemand = onDemand;
	}

	public boolean isGenerateContentOnly() {
		return generateContentOnly;
	}

	public void setGenerateContentOnly(boolean generateContentOnly) {
		this.generateContentOnly = generateContentOnly;
	}

	public Long getCommunicationRequestDtlId() {
		return communicationRequestDtlId;
	}

	public void setCommunicationRequestDtlId(Long communicationRequestDtlId) {
		this.communicationRequestDtlId = communicationRequestDtlId;
	}




	@Override
	public GeneratedContentVO call() throws Exception {
		
		NeutrinoContextInitializationAspect neutrinoContextInitialization = NeutrinoSpringAppContextUtil
				.getBeanByName("frameworkContextInitializationAspect", NeutrinoContextInitializationAspect.class);
		neutrinoContextInitialization.initializeNonLoginUserContext(null);
		/*return communicationGenerationDelegate.generateSchedularBasedCommunicationInNewTransaction(
				communicationRequestDtlId, localCacheMap, additionalMethodsMap, false, false);
		*/
		return null;
	}
	
	

}
