package com.nucleus.finnone.pro.communicationgenerator.dao;

import java.util.List;

import javax.inject.Named;
import javax.persistence.FlushModeType;
import javax.persistence.Query;

import com.nucleus.dao.query.JPAQueryExecutor;
import com.nucleus.finnone.pro.communicationgenerator.domainobject.CommunicationGenerationDetailHistory;
import com.nucleus.message.entity.MessageExchangeRecordHistory;
import com.nucleus.persistence.EntityDaoImpl;
import com.nucleus.query.constants.QueryHint;

@Named("communicationTrackingDao")
public class CommunicationTrackingDao extends EntityDaoImpl implements ICommunicationTrackingDao {
	
	private static final String UNIQUE_REQUEST_ID_KEY = "uniqueRequestId";
    private static final String EVENT_REQUEST_LOG_ID_KEY = "eventRequestLogId";
    private static final String PARENT_UNIQUE_REQUEST_ID_KEY = "parentUniqueRequestId";	    
    private static final String REGENERATION_ATTEMPTS_COUNT_KEY = "regenerationAttemptsCount";
    private static final String BARCODE_REFERENCE_NUMBER_KEY = "barcodeReferenceNumber";
 
    private <T> JPAQueryExecutor<T> createJPAQueryExecutor(String query) {
		JPAQueryExecutor<T> jPAQueryExecutor = new JPAQueryExecutor<>(query);
        jPAQueryExecutor.addQueryHint(QueryHint.QUERY_HINT_READONLY, Boolean.TRUE);
        jPAQueryExecutor.addQueryHint(QueryHint.QUERY_HINT_FLUSHMODE, FlushModeType.COMMIT);
		return jPAQueryExecutor;
	}

	private String getParameterizedQuery(String attributeName, Class<?> entityClass, boolean isCountQuery) {
		StringBuilder queryBuilder = new StringBuilder();
		if (isCountQuery) {
			queryBuilder.append("SELECT count(data.id) ");
		} else {
			queryBuilder.append("SELECT data FROM ");
		}
		queryBuilder.append(entityClass.getSimpleName())
					.append(" data WHERE data.")
					.append(attributeName)
					.append("=:")
					.append(attributeName);
		if (entityClass.isAssignableFrom(CommunicationGenerationDetailHistory.class)) {
			queryBuilder.append(" and data.parentCommunicationGenerationDetailHistoryId is null");
		}
		return queryBuilder.toString();
	}
	
	private String getRegenerationParameterizedQuery(String dynamicAttributeName, boolean isCountQuery) {
		StringBuilder queryBuilder = new StringBuilder();
		if (isCountQuery) {
			queryBuilder.append(" count(merh.id)");
		}
		queryBuilder.append(" from MessageExchangeRecordHistory merh where merh.")
					.append(REGENERATION_ATTEMPTS_COUNT_KEY)
					.append(" = (select max(merh.")
					.append(REGENERATION_ATTEMPTS_COUNT_KEY)
					.append(") from MessageExchangeRecordHistory merh where merh.")
					.append(dynamicAttributeName)
					.append("=:")
					.append(dynamicAttributeName)
					.append(")");
		return queryBuilder.toString();
	}
	
	@Override
	public long getCountOfCommunicationHistoriesByEventRequestLogId(String eventRequestLogId) {
		String parameterizedQuery = getParameterizedQuery(EVENT_REQUEST_LOG_ID_KEY, CommunicationGenerationDetailHistory.class, true);
		JPAQueryExecutor<Long> jpaQueryExecutor = createJPAQueryExecutor(parameterizedQuery);
		jpaQueryExecutor.addParameter(EVENT_REQUEST_LOG_ID_KEY, eventRequestLogId);
		return executeQueryForSingleValue(jpaQueryExecutor);
	}

	@Override
	public List<CommunicationGenerationDetailHistory> getAllCommunicationHistoriesByEventRequestLogId(
			String eventRequestLogId) {
		String parameterizedQuery = getParameterizedQuery(EVENT_REQUEST_LOG_ID_KEY, CommunicationGenerationDetailHistory.class, false);
		JPAQueryExecutor<CommunicationGenerationDetailHistory> jpaQueryExecutor = createJPAQueryExecutor(parameterizedQuery);
		jpaQueryExecutor.addParameter(EVENT_REQUEST_LOG_ID_KEY, eventRequestLogId);
		return executeQuery(jpaQueryExecutor);
	}

	@Override
	public long getCountOfSentMessagesByEventRequestLogId(String eventRequestLogId) {
		String parameterizedQuery = getParameterizedQuery(EVENT_REQUEST_LOG_ID_KEY, MessageExchangeRecordHistory.class, true);
		JPAQueryExecutor<Long> jpaQueryExecutor = createJPAQueryExecutor(parameterizedQuery);
		jpaQueryExecutor.addParameter(EVENT_REQUEST_LOG_ID_KEY, eventRequestLogId);
		return executeQueryForSingleValue(jpaQueryExecutor);
	}

	@Override
	public <T extends MessageExchangeRecordHistory> List<T> getAllMessageRecordHistoryByEventRequestLogId(
			String eventRequestLogId) {
		String parameterizedQuery = getParameterizedQuery(EVENT_REQUEST_LOG_ID_KEY, MessageExchangeRecordHistory.class, false);
		JPAQueryExecutor<T> jpaQueryExecutor = createJPAQueryExecutor(parameterizedQuery);
		jpaQueryExecutor.addParameter(EVENT_REQUEST_LOG_ID_KEY, eventRequestLogId);
		return executeQuery(jpaQueryExecutor);
	}

	@Override
	public String getAttachmentStorageIds(String uniqueRequestId) {
		String query = "SELECT mmerh.attachmentStorageIds FROM MailMessageExchangeRecordHistory mmerh WHERE mmerh.uniqueRequestId =:uniqueRequestId";
		JPAQueryExecutor<String> jpaQueryExecutor = createJPAQueryExecutor(query);
		jpaQueryExecutor.addParameter(UNIQUE_REQUEST_ID_KEY, uniqueRequestId);
		return executeQueryForSingleValue(jpaQueryExecutor);
	}

	@Override
	public <T extends MessageExchangeRecordHistory> T getMessageRecordHistoryByUniqueId(String uniqueRequestId) {
		String parameterizedQuery = getParameterizedQuery(UNIQUE_REQUEST_ID_KEY, MessageExchangeRecordHistory.class, false);
		JPAQueryExecutor<T> jpaQueryExecutor = createJPAQueryExecutor(parameterizedQuery);
		jpaQueryExecutor.addParameter(UNIQUE_REQUEST_ID_KEY, uniqueRequestId);
		return executeQueryForSingleValue(jpaQueryExecutor);
	}

	@Override
	public CommunicationGenerationDetailHistory getCommunicationHistoryByUniqueRequestId(String uniqueRequestId) {
		String parameterizedQuery = getParameterizedQuery(UNIQUE_REQUEST_ID_KEY, CommunicationGenerationDetailHistory.class, false);
		JPAQueryExecutor<CommunicationGenerationDetailHistory> jpaQueryExecutor = createJPAQueryExecutor(parameterizedQuery);
		jpaQueryExecutor.addParameter(UNIQUE_REQUEST_ID_KEY, uniqueRequestId);
		return executeQueryForSingleValue(jpaQueryExecutor);
	}

	@Override
	public MessageExchangeRecordHistory getLatestMessageHistoryByEventRequestLogId(String eventRequestLogId) {
		String query = getRegenerationParameterizedQuery(EVENT_REQUEST_LOG_ID_KEY, false);
		JPAQueryExecutor<MessageExchangeRecordHistory> jPAQueryExecutor = createJPAQueryExecutor(query);
		jPAQueryExecutor.addParameter(EVENT_REQUEST_LOG_ID_KEY, eventRequestLogId);
		return executeQueryForSingleValue(jPAQueryExecutor);
	}

	@Override
	public MessageExchangeRecordHistory getLatestMessageHistoryByParentUniqueRequestId(String uniqueRequestId) {
		String query = getRegenerationParameterizedQuery(PARENT_UNIQUE_REQUEST_ID_KEY, false);
		JPAQueryExecutor<MessageExchangeRecordHistory> jPAQueryExecutor = createJPAQueryExecutor(query);
		jPAQueryExecutor.addParameter(PARENT_UNIQUE_REQUEST_ID_KEY, uniqueRequestId);
		return executeQueryForSingleValue(jPAQueryExecutor);
	}

	@Override
	public CommunicationGenerationDetailHistory getCommunicationHistoryByUniqueBarcodeReferenceNumber(String barcodeReferenceNumber) {
		Query namedQuery  =  getEntityManager().createNamedQuery("CommunicationGenerationDetailHistory.getCommunicationGenerationDetailHistoryByBarcodeReferenceNumber");
		
		namedQuery.setHint(QueryHint.QUERY_HINT_CACHEABLE, true);
		namedQuery.setParameter(BARCODE_REFERENCE_NUMBER_KEY, barcodeReferenceNumber);
		
		return (CommunicationGenerationDetailHistory) namedQuery.getSingleResult() ;
	}
}
