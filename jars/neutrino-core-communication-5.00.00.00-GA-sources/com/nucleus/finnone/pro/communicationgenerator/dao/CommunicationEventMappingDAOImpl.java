package com.nucleus.finnone.pro.communicationgenerator.dao;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.NoResultException;
import javax.persistence.Query;

import com.nucleus.core.event.EventCode;
import com.nucleus.entity.ApprovalStatus;
import com.nucleus.finnone.pro.base.Message;
import com.nucleus.finnone.pro.base.Message.MessageType;
import com.nucleus.finnone.pro.base.exception.BusinessException;
import com.nucleus.finnone.pro.base.exception.ExceptionBuilder;
import com.nucleus.finnone.pro.communicationgenerator.constants.CommunicationType;
import com.nucleus.finnone.pro.communicationgenerator.domainobject.CommunicationEventMappingHeader;
import com.nucleus.finnone.pro.communicationgenerator.domainobject.CommunicationName;
import com.nucleus.finnone.pro.communicationgenerator.domainobject.CommunicationTemplate;
import com.nucleus.finnone.pro.communicationgenerator.domainobject.EventCommCriteriaTypeMapping;
import com.nucleus.finnone.pro.communicationgenerator.util.CommunicationEventMappingConstants;
import com.nucleus.finnone.pro.general.constants.ExceptionSeverityEnum;
import com.nucleus.logging.BaseLoggers;
import com.nucleus.persistence.EntityDao;
import com.nucleus.query.constants.QueryHint;
import com.nucleus.rules.model.SourceProduct;

@Named("communicationEventMappingDAO")
public class CommunicationEventMappingDAOImpl implements CommunicationEventMappingDAO {

	@Inject
	@Named("entityDao")
	protected EntityDao entityDao;

	@SuppressWarnings("unchecked")
	public List<CommunicationName> getCommunicationCodesBySourceProductId(Long sourceProductId) {
		Query namedQuery = entityDao.getEntityManager().createNamedQuery("getCommunicationCodesBySourceProductId");
		namedQuery.setParameter(CommunicationEventMappingConstants.SOURCE_PRODUCT_ID, sourceProductId);
		return namedQuery.getResultList();
	}

	public CommunicationType getCommunicationTypeByCommunicationCodeId(Long communicationId) {
		Query namedQuery = entityDao.getEntityManager().createNamedQuery("getCommunicationTypeByCommunicationCodeId");
		namedQuery.setParameter(CommunicationEventMappingConstants.COMM_CODE_ID, communicationId);
		return (CommunicationType) namedQuery.getSingleResult();
	}

	public List<CommunicationTemplate> getCommunicationTemplateByCommunicationCodeId(Long communicationId) {
		CommunicationName communicationName = entityDao.find(CommunicationName.class, communicationId);
		return communicationName.getCommunicationTemplates();
	}

	@SuppressWarnings("unchecked")
	public List<CommunicationEventMappingHeader> checkDuplicateCommunicationEventMapping(Long moduleId, Long eventId) {
		Query query = entityDao.getEntityManager().createNamedQuery("checkDuplicateCommunicationEventMapping");
		query.setParameter(CommunicationEventMappingConstants.SOURCE_PRODUCT_ID, moduleId);
		query.setParameter(CommunicationEventMappingConstants.EVENT_CODE_ID, eventId);
		return query.getResultList();
	}

	@SuppressWarnings("unchecked")
	public List<CommunicationTemplate> getCommunicationTemplateByCommunicationTypeId(Long communicationTypeId) {
		Query query = entityDao.getEntityManager().createNamedQuery("getCommunicationTemplateByCommunicationTypeId");
		query.setParameter(CommunicationEventMappingConstants.COMM_TYPE_ID, communicationTypeId);
		return query.getResultList();
	}

	public CommunicationEventMappingHeader getCommunicationEventMapping(String eventCode, SourceProduct module) {
		Query query = null;
		CommunicationEventMappingHeader commEventMapHdr = null;
		query = entityDao.getEntityManager().createNamedQuery("getCommunicationEventMappings");
		query.setParameter(CommunicationEventMappingConstants.EVENT_CODE, eventCode);
		query.setParameter(CommunicationEventMappingConstants.SOURCE_PRODUCT_ID, module.getId());
		query.setParameter(CommunicationEventMappingConstants.APPROVAL_STATUS, getApprovalStatusList());
		try {
			commEventMapHdr = (CommunicationEventMappingHeader) query.getSingleResult();
		} catch (NoResultException ex) {
			BaseLoggers.exceptionLogger.error(
					"No Communication Event Mapping for Event Code: {} and Source Product: {}", eventCode,
					module.getDisplayName());
		}
		return commEventMapHdr;
	}

	private List<Integer> getApprovalStatusList() {
		List<Integer> approvalStatusList = new ArrayList<>();
		approvalStatusList.add(ApprovalStatus.APPROVED);
		approvalStatusList.add(ApprovalStatus.APPROVED_MODIFIED);
		approvalStatusList.add(ApprovalStatus.APPROVED_DELETED);
		approvalStatusList.add(ApprovalStatus.APPROVED_DELETED_IN_PROGRESS);
		return approvalStatusList;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<EventCode> getEventCodesBySourceProductId(Long sourceProductId) {
		Query namedQuery = entityDao.getEntityManager().createNamedQuery("getEventCodesBySourceProductId");
		namedQuery.setParameter(CommunicationEventMappingConstants.SOURCE_PRODUCT_ID, sourceProductId);
		return namedQuery.getResultList();
	}

	@Override
	public EventCommCriteriaTypeMapping getEventCommCriteriaTypeMapping(Long sourceProductId, Long eventCodeId) {
		Query namedQuery = entityDao.getEntityManager().createNamedQuery("getCriteriaTypeBySourceAndEvent");
		namedQuery.setParameter(CommunicationEventMappingConstants.SOURCE_PRODUCT_ID, sourceProductId);
		namedQuery.setParameter(CommunicationEventMappingConstants.EVENT_CODE_ID, eventCodeId);
		namedQuery.setHint(QueryHint.QUERY_HINT_CACHEABLE, true);
		try {
			return (EventCommCriteriaTypeMapping) namedQuery.getSingleResult();
		} catch (NoResultException ex) {
			Message msg = new Message(
					"No Event Communication Criteria Type Mapping for Event Code: {0} and Source Product: {1}",
					MessageType.ERROR, eventCodeId.toString(), sourceProductId.toString());
			BaseLoggers.exceptionLogger.error(msg.getI18nCode(), eventCodeId.toString(), sourceProductId.toString());
			throw ExceptionBuilder.getInstance(BusinessException.class).setMessage(msg)
					.setSeverity(ExceptionSeverityEnum.SEVERITY_MEDIUM.getEnumValue()).build();
		}
	}

}
