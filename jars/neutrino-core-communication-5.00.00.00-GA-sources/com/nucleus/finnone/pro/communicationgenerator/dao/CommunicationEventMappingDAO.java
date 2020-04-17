package com.nucleus.finnone.pro.communicationgenerator.dao;

import java.util.List;

import com.nucleus.core.event.EventCode;
import com.nucleus.finnone.pro.communicationgenerator.constants.CommunicationType;
import com.nucleus.finnone.pro.communicationgenerator.domainobject.CommunicationEventMappingHeader;
import com.nucleus.finnone.pro.communicationgenerator.domainobject.CommunicationName;
import com.nucleus.finnone.pro.communicationgenerator.domainobject.CommunicationTemplate;
import com.nucleus.finnone.pro.communicationgenerator.domainobject.EventCommCriteriaTypeMapping;
import com.nucleus.rules.model.SourceProduct;

public interface CommunicationEventMappingDAO {

	List<CommunicationName> getCommunicationCodesBySourceProductId(Long sourceProductId);

	CommunicationType getCommunicationTypeByCommunicationCodeId(Long communicationId);

	List<CommunicationTemplate> getCommunicationTemplateByCommunicationCodeId(Long communicationId);

	List<CommunicationEventMappingHeader> checkDuplicateCommunicationEventMapping(Long moduleId, Long eventId);

	List<CommunicationTemplate> getCommunicationTemplateByCommunicationTypeId(Long communicationTypeId);

	CommunicationEventMappingHeader getCommunicationEventMapping(String eventCode, SourceProduct module);

	List<EventCode> getEventCodesBySourceProductId(Long sourceProductId);

	EventCommCriteriaTypeMapping getEventCommCriteriaTypeMapping(Long sourceProductId, Long eventCodeId);

}
