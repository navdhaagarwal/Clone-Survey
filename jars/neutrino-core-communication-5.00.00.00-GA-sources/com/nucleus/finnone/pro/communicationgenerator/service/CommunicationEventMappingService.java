package com.nucleus.finnone.pro.communicationgenerator.service;

import java.util.List;

import com.nucleus.core.event.EventCode;
import com.nucleus.core.genericparameter.entity.GenericParameter;
import com.nucleus.finnone.pro.communicationgenerator.constants.CommunicationType;
import com.nucleus.finnone.pro.communicationgenerator.domainobject.CommunicationEventMappingHeader;
import com.nucleus.finnone.pro.communicationgenerator.domainobject.CommunicationName;
import com.nucleus.finnone.pro.communicationgenerator.domainobject.CommunicationTemplate;
import com.nucleus.rules.model.SourceProduct;
import com.nucleus.user.User;

public interface CommunicationEventMappingService {

	List<CommunicationName> getCommunicationCodesBySourceProductId(Long sourceProductId);

	CommunicationType getCommunicationTypeByCommunicationCodeId(Long communicationId);

	List<CommunicationTemplate> getCommunicationTemplateByCommunicationCodeId(Long communicationId);

	CommunicationEventMappingHeader saveCommunicationEventmapping(
			CommunicationEventMappingHeader communicationEventMapping, User userReference);

	boolean checkDuplicateCommunicationEventMapping(Long eventMappingHdrId, Long moduleId, Long eventId);

	List<CommunicationTemplate> getCommunicationTemplateByCommunicationTypeId(Long communicationTypeId);

	String getQueryBaseSelectClause(Long sourceProductId);

	List<? extends GenericParameter> getSourceProductList();

	CommunicationEventMappingHeader getCommunicationEventMapping(String eventCode, SourceProduct module);

	List<EventCode> getEventCodesBySourceProductId(Long sourceProductId);

	String getCommunicationCriteriaType(Long sourceProductId, Long eventCodeId);

}
