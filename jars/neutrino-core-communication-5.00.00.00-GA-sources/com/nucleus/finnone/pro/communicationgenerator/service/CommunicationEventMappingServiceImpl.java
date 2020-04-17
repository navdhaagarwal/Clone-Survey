package com.nucleus.finnone.pro.communicationgenerator.service;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import com.nucleus.core.event.EventCode;
import com.nucleus.core.genericparameter.entity.GenericParameter;
import com.nucleus.finnone.pro.communicationgenerator.businessobject.CommunicationEventMappingBusinessObject;
import com.nucleus.finnone.pro.communicationgenerator.constants.CommunicationType;
import com.nucleus.finnone.pro.communicationgenerator.domainobject.CommunicationEventMappingHeader;
import com.nucleus.finnone.pro.communicationgenerator.domainobject.CommunicationName;
import com.nucleus.finnone.pro.communicationgenerator.domainobject.CommunicationTemplate;
import com.nucleus.finnone.pro.communicationgenerator.util.CommunicationEventMappingConstants;
import com.nucleus.rules.model.SourceProduct;
import com.nucleus.user.User;

@Named("communicationEventMappingService")
public class CommunicationEventMappingServiceImpl implements CommunicationEventMappingService {

	@Inject
	@Named("communicationEventMappingBusinessObject")
	private CommunicationEventMappingBusinessObject communicationEventMappingBusinessObject;

	public List<CommunicationName> getCommunicationCodesBySourceProductId(Long sourceProductId) {
		return communicationEventMappingBusinessObject.getCommunicationCodesBySourceProductId(sourceProductId);
	}

	public CommunicationType getCommunicationTypeByCommunicationCodeId(Long communicationId) {
		return communicationEventMappingBusinessObject.getCommunicationTypeByCommunicationCodeId(communicationId);
	}

	public List<CommunicationTemplate> getCommunicationTemplateByCommunicationCodeId(Long communicationId) {
		return communicationEventMappingBusinessObject.getCommunicationTemplateByCommunicationCodeId(communicationId);
	}

	public CommunicationEventMappingHeader saveCommunicationEventmapping(
			CommunicationEventMappingHeader communicationEventMapping, User userReference) {
		return communicationEventMappingBusinessObject.saveCommunicationEventmapping(communicationEventMapping,
				userReference);
	}

	public boolean checkDuplicateCommunicationEventMapping(Long eventMappingHdrId, Long moduleId, Long eventId) {
		return communicationEventMappingBusinessObject.checkDuplicateCommunicationEventMapping(eventMappingHdrId,
				moduleId, eventId);
	}

	public List<CommunicationTemplate> getCommunicationTemplateByCommunicationTypeId(Long communicationTypeId) {
		return communicationEventMappingBusinessObject
				.getCommunicationTemplateByCommunicationTypeId(communicationTypeId);
	}

	public String getQueryBaseSelectClause(Long sourceProductId) {
		return CommunicationEventMappingConstants.CRITERIA_BASE_SELECT_CLAUSE;
	}

	public List<? extends GenericParameter> getSourceProductList() {
		return communicationEventMappingBusinessObject.getSourceProductList();
	}

	public CommunicationEventMappingHeader getCommunicationEventMapping(String eventCode, SourceProduct module) {
		return communicationEventMappingBusinessObject.getCommunicationEventMapping(eventCode, module);
	}

	@Override
	public List<EventCode> getEventCodesBySourceProductId(Long sourceProductId) {
		return communicationEventMappingBusinessObject.getEventCodesBySourceProductId(sourceProductId);

	}

	@Override
	public String getCommunicationCriteriaType(Long sourceProductId, Long eventCodeId) {
		return communicationEventMappingBusinessObject.getCommunicationCriteriaType(sourceProductId, eventCodeId);

	}

}
