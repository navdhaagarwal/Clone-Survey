package com.nucleus.finnone.pro.communicationgenerator.populator;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.transaction.annotation.Transactional;

import com.nucleus.core.genericparameter.service.GenericParameterService;
import com.nucleus.finnone.pro.base.exception.SystemException;
import com.nucleus.finnone.pro.cache.common.FWCachePopulator;
import com.nucleus.finnone.pro.cache.constants.FWCacheConstants;
import com.nucleus.finnone.pro.communicationgenerator.domainobject.CommunicationDataPreparationDetail;
import com.nucleus.finnone.pro.communicationgenerator.serviceinterface.ICommunicationGeneratorService;
import com.nucleus.finnone.pro.communicationgenerator.util.ServiceSelectionCriteria;
import com.nucleus.finnone.pro.general.util.ValidatorUtils;
import com.nucleus.logging.BaseLoggers;
import com.nucleus.rules.model.SourceProduct;

@Named("commnDataPreparationDtlCachePopulator")
public class CommnDataPreparationDtlCachePopulator extends FWCachePopulator {

	@Inject
	@Named("communicationGeneratorService")
	private ICommunicationGeneratorService communicationGeneratorService;

	@Inject
	@Named("genericParameterService")
	private GenericParameterService genericParameterService;

	@Override
	public void init() {
		BaseLoggers.flowLogger.debug("Init Called : CommnDataPreparationDtlCachePopulator");
	}

	@Override
	@Transactional(readOnly = true)
	public Object fallback(Object key) {
		String[] keyArray = ((String) key).split(FWCacheConstants.REGEX_DELIMITER);
		ServiceSelectionCriteria serviceSelectionCriteria = genericParameterService.findByCode(keyArray[0],
				ServiceSelectionCriteria.class, true);
		SourceProduct sourceProduct = genericParameterService.findByCode(keyArray[1], SourceProduct.class, true);
		List<CommunicationDataPreparationDetail> communicationDataPreparationDetails = communicationGeneratorService
				.getActiveApprovedDetailBasedOnServiceSouceAndModule(sourceProduct, serviceSelectionCriteria.getId());
		if (ValidatorUtils.hasElements(communicationDataPreparationDetails)) {
			List<Long> commDataPrepDtlIds = new ArrayList<>();
			for(CommunicationDataPreparationDetail commDataPrepDtl : communicationDataPreparationDetails) {
				commDataPrepDtlIds.add(commDataPrepDtl.getId());
			}
			return commDataPrepDtlIds;
		}
		return null;
	}

	@Override
	@Transactional(readOnly = true)
	public void build(Long tenantId) {
		List<ServiceSelectionCriteria> serviceSelectionCriteriaList = genericParameterService
				.retrieveTypes(ServiceSelectionCriteria.class);
		List<SourceProduct> allModulesList = genericParameterService.retrieveTypes(SourceProduct.class);

		if (ValidatorUtils.hasElements(allModulesList)) {
			for (SourceProduct sourceProduct : allModulesList) {
				buildCommPrepDtlCacheForModule(sourceProduct, serviceSelectionCriteriaList);
			}
		}
	}

	private void buildCommPrepDtlCacheForModule(SourceProduct sourceProduct,
			List<ServiceSelectionCriteria> serviceSelectionCriteriaList) {

		if (ValidatorUtils.hasElements(serviceSelectionCriteriaList)) {
			for (ServiceSelectionCriteria serviceSelectionCriteria : serviceSelectionCriteriaList) {
				List<CommunicationDataPreparationDetail> communicationDataPreparationDetails = communicationGeneratorService
						.getActiveApprovedDetailBasedOnServiceSouceAndModule(sourceProduct,
								serviceSelectionCriteria.getId());
				if (ValidatorUtils.hasElements(communicationDataPreparationDetails)) {
					for (CommunicationDataPreparationDetail commDataPrepDtl : communicationDataPreparationDetails) {
						String key = serviceSelectionCriteria.getCode() + FWCacheConstants.KEY_DELIMITER
								+ sourceProduct.getCode();
						List<Long> commDataPrepDtlIds = (List<Long>) get(key);
						if (commDataPrepDtlIds == null) {
							commDataPrepDtlIds = new ArrayList<>();
						}
						commDataPrepDtlIds.add(commDataPrepDtl.getId());
						put(key, commDataPrepDtlIds);
					}
				}
			}
		}

	}

	@Override
	public void update(Action action, Object object) {
		BaseLoggers.flowLogger.debug("Update Called : CommnDataPreparationDtlCachePopulator");
		throw new SystemException(UPDATE_ERROR_MSG + getNeutrinoCacheName());
	}

	@Override
	public String getNeutrinoCacheName() {
		return FWCacheConstants.COMM_DATA_PREP_DTL;
	}

	@Override
	public String getCacheGroupName() {
		return FWCacheConstants.COMMUNICATION_CACHE_GROUP;
	}

}
