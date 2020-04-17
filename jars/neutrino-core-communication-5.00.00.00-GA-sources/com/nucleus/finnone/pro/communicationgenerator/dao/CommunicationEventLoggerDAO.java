package com.nucleus.finnone.pro.communicationgenerator.dao;

import static com.nucleus.finnone.pro.general.util.ValidatorUtils.notNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.Query;

import org.springframework.stereotype.Repository;

import com.nucleus.entity.PersistenceStatus;
import com.nucleus.finnone.pro.communicationgenerator.domainobject.CommunicationEventRequestLog;
import com.nucleus.finnone.pro.communicationgenerator.domainobject.CommunicationRequestDetail;
import com.nucleus.finnone.pro.communicationgenerator.util.CommunicationEventMappingConstants;
import com.nucleus.logging.BaseLoggers;
import com.nucleus.persistence.EntityDaoImpl;

@Repository("communicationEventLoggerDAO")
public class CommunicationEventLoggerDAO extends EntityDaoImpl implements ICommunicationEventLoggerDAO{

	// copied from CGDAO
	
	private static final  String QUERY_SELECT_COMM_EVENT_REQ_LOG = "select communicationEventRequestLog from CommunicationEventRequestLog communicationEventRequestLog  Where";
	private static final  String QUERY_APPEND_PERSISTENCE_STATUS = "  communicationEventRequestLog.entityLifeCycleData.persistenceStatus = :persistenceStatus ";
	private static final  String QUERY_APPEND_SOURCE_PRODUCT = "and communicationEventRequestLog.sourceProduct = :sourceProduct ";
	private static final  String QUERY_APPEND_STATUS = "and communicationEventRequestLog.status= :status ";
	private static final  String QUERY_APPEND_EVENT_CODE = "and communicationEventRequestLog.eventCode= :eventCode ";
	private static final  String QUERY_APPEND_SUBJECT_URI = " and communicationEventRequestLog.subjectURI= :subjectURI ";
	private static final  String QUERY_APPEND_APPLICABLE_PRIMARY_URI = " and communicationEventRequestLog.applicablePrimaryEntityURI= :applicablePrimaryEntityURI ";

	@Override
	public CommunicationRequestDetail createCommunicationGenerationDetail(
			CommunicationRequestDetail communicationGenerationDetail) {
		
		persist(communicationGenerationDetail);
		return communicationGenerationDetail;
	}

	@Override
	public CommunicationRequestDetail updateCommunicationGenerationDetail(
			CommunicationRequestDetail communicationRequestDetail) {
			update(communicationRequestDetail);
		return communicationRequestDetail;
	}
	
	@Override
	public CommunicationEventRequestLog createCommunicationEventRequest(
			CommunicationEventRequestLog communicationEventRequestLog) {
		persist(communicationEventRequestLog);
		return communicationEventRequestLog;
	}
	
	// copied from CGDAO
	
	@SuppressWarnings("unchecked")
	@Override
	public List<CommunicationEventRequestLog> searchCommEventsBasedOnCriteria(CommunicationEventRequestLog communicationEventRequestLog){
		
		Map<String, Object> parameterMap = new HashMap<String, Object>();
		StringBuilder query = new StringBuilder(QUERY_SELECT_COMM_EVENT_REQ_LOG);
		query.append(QUERY_APPEND_PERSISTENCE_STATUS);
		parameterMap.put("persistenceStatus",PersistenceStatus.ACTIVE);
		if (notNull(communicationEventRequestLog.getSourceProduct())) {
			query.append(QUERY_APPEND_SOURCE_PRODUCT);
			parameterMap.put("sourceProduct",communicationEventRequestLog.getSourceProduct());
		}
		if (notNull(communicationEventRequestLog.getStatus())) {
			query.append(QUERY_APPEND_STATUS);
			parameterMap.put("status",communicationEventRequestLog.getStatus());
		}
		if (notNull(communicationEventRequestLog.getEventCode())) {
			query.append(QUERY_APPEND_EVENT_CODE);
			parameterMap.put("eventCode",communicationEventRequestLog.getEventCode());
		}
		if (notNull(communicationEventRequestLog.getSubjectURI())) {
			query.append(QUERY_APPEND_SUBJECT_URI);
			parameterMap.put("subjectURI",communicationEventRequestLog.getSubjectURI());
		}
		if (notNull(communicationEventRequestLog.getApplicablePrimaryEntityURI())) {
			query.append(QUERY_APPEND_APPLICABLE_PRIMARY_URI);
			parameterMap.put("applicablePrimaryEntityURI",communicationEventRequestLog.getApplicablePrimaryEntityURI());
		}
		query.append(" order by  communicationEventRequestLog.id desc");
		String stringQuery = query.toString();
		Query dynamicQuery = getEntityManager().createQuery(stringQuery);
		for (Map.Entry<String, Object> entry : parameterMap.entrySet()) {
			dynamicQuery.setParameter(entry.getKey(), entry.getValue());
		}
		dynamicQuery.setHint("org.hibernate.fetchSize",1000 );
		return dynamicQuery.getResultList();
	}
	

	@Override
	public List<CommunicationEventRequestLog> searchCommEventsBasedOnCriteria(
			CommunicationEventRequestLog communicationEventRequestLog, String criteria) {
		List<CommunicationEventRequestLog> commEventRequestLogs = null;
		Map<String, Object> parameterMap = new HashMap<String, Object>();
		StringBuilder query = new StringBuilder(
				CommunicationEventMappingConstants.QUERY_NATIVE_SELECT_COMM_EVENT_REQ_LOG);
		query.append(CommunicationEventMappingConstants.CRITERIA_BASE_SELECT_CLAUSE).append(" ");
		query.append(criteria);
		query.append(") ");

		if (notNull(communicationEventRequestLog.getSourceProduct())) {
			query.append(CommunicationEventMappingConstants.QUERY_NATIVE_APPEND_SOURCE_PRODUCT);
			parameterMap.put("sourceProduct", communicationEventRequestLog.getSourceProduct());
		}
		if (notNull(communicationEventRequestLog.getStatus())) {
			query.append(CommunicationEventMappingConstants.QUERY_NATIVE_APPEND_STATUS);
			parameterMap.put("status", communicationEventRequestLog.getStatus());
		}
		if (notNull(communicationEventRequestLog.getEventCode())) {
			query.append(CommunicationEventMappingConstants.QUERY_NATIVE_APPEND_EVENT_CODE);
			parameterMap.put("eventCode", communicationEventRequestLog.getEventCode());
		}
		if (notNull(communicationEventRequestLog.getSubjectReferenceNumber())) {
			query.append(CommunicationEventMappingConstants.QUERY_NATIVE_APPEND_SUBJECT_REFERENCE);
			parameterMap.put("subjectReference", communicationEventRequestLog.getSubjectReferenceNumber());
		}
		if (notNull(communicationEventRequestLog.getSubjectURI())) {
			query.append(CommunicationEventMappingConstants.QUERY_NATIVE_APPEND_SUBJECT_URI);
			parameterMap.put("subjectURI", communicationEventRequestLog.getSubjectURI());
		}
		if (notNull(communicationEventRequestLog.getApplicablePrimaryEntityURI())) {
			query.append(CommunicationEventMappingConstants.QUERY_NATIVE_APPEND_APPLICABLE_PRIMARY_URI);
			parameterMap.put("applicablePrimaryEntityURI",
					communicationEventRequestLog.getApplicablePrimaryEntityURI());
		}
		query.append(" order by  communicationEventRequestLog.id desc");
		String stringQuery = query.toString();
		Query dynamicQuery = getEntityManager().createNativeQuery(stringQuery, CommunicationEventRequestLog.class);
		dynamicQuery.unwrap(org.hibernate.SQLQuery.class).addSynchronizedQuerySpace("");
		for (Map.Entry<String, Object> entry : parameterMap.entrySet()) {
			dynamicQuery.setParameter(entry.getKey(), entry.getValue());
		}
		dynamicQuery.setHint("org.hibernate.fetchSize", 1000);
		try {
			commEventRequestLogs = dynamicQuery.getResultList();
		} catch (Exception e) {
			BaseLoggers.exceptionLogger.error("Exeption occurred while executing the Criteria Query : {} ", stringQuery,
					e);
		}
		return commEventRequestLogs;
	}
	
	
}
