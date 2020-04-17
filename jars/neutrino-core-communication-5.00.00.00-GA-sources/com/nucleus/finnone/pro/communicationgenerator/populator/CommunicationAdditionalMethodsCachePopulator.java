package com.nucleus.finnone.pro.communicationgenerator.populator;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import com.nucleus.finnone.pro.cache.common.FWCachePopulator;
import com.nucleus.finnone.pro.cache.constants.FWCacheConstants;
import com.nucleus.finnone.pro.communicationgenerator.businessobject.ICommunicationGeneratorBusinessObject;
import com.nucleus.finnone.pro.communicationgenerator.vo.DataPreparationServiceMethodVO;
import com.nucleus.finnone.pro.general.util.ValidatorUtils;
import com.nucleus.logging.BaseLoggers;

@Named("communicationAdditionalMethodsCachePopulator")
public class CommunicationAdditionalMethodsCachePopulator  extends FWCachePopulator {
	
	@Inject
	@Named("communicationGeneratorBusinessObject")
	private ICommunicationGeneratorBusinessObject communicationGeneratorBusinessObject; 
	@Override
	public String getNeutrinoCacheName() {
		return FWCacheConstants.COMMUNICATION_CODE_ADDITIONAL_METHODS;
	}
	
	@Override
	public String getCacheGroupName() {
		return FWCacheConstants.COMMUNICATION_CACHE_GROUP;
	}

	@Override
	public void init() {
		BaseLoggers.flowLogger.debug("Init Called : CommunicationAdditionalMethodsCachePopulator.");
	}

	@Override
	public Object fallback(Object key) {
		String communicationCode = (String) key; 
		List<DataPreparationServiceMethodVO> listOfAdditionalMethods = communicationGeneratorBusinessObject.findAdditionalMethodsForCommunicationDataPreperation(communicationCode);
    	if (listOfAdditionalMethods != null) {
    		return listOfAdditionalMethods;
    	}
		return null;
	}

	@Override
	public void build(Long tenantId) {
		//Not a table data. No need to build this data.
	}

	@Override
	public void update(Action action, Object object) {
		if (action.equals(Action.UPDATE) && ValidatorUtils.notNull(object)) {
			putAll(object);
		}
		BaseLoggers.flowLogger.debug("Update Called : ScriptRuleEvaluatorCachePopulator.");
	}

}
