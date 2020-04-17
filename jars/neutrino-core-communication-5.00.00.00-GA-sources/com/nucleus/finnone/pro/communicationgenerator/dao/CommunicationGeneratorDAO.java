package com.nucleus.finnone.pro.communicationgenerator.dao;

import static com.nucleus.finnone.pro.communicationgenerator.constants.CommunicationGeneratorConstants.STATUS_COMPLETED;
import static com.nucleus.finnone.pro.communicationgenerator.constants.CommunicationGeneratorConstants.STATUS_INITIATED;
import static com.nucleus.finnone.pro.general.util.ValidatorUtils.notNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.Query;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Repository;

import com.nucleus.core.genericparameter.entity.GenericParameter;
import com.nucleus.core.validation.util.NeutrinoValidator;
import com.nucleus.dao.query.JPAQueryExecutor;
import com.nucleus.dao.query.NamedQueryExecutor;
import com.nucleus.entity.ApprovalStatus;
import com.nucleus.entity.PersistenceStatus;
import com.nucleus.finnone.pro.communicationgenerator.domainobject.CommunicationDataPreparationDetail;
import com.nucleus.finnone.pro.communicationgenerator.domainobject.CommunicationEventRequestHistory;
import com.nucleus.finnone.pro.communicationgenerator.domainobject.CommunicationEventRequestLog;
import com.nucleus.finnone.pro.communicationgenerator.domainobject.CommunicationName;
import com.nucleus.finnone.pro.communicationgenerator.domainobject.CommunicationParameter;
import com.nucleus.finnone.pro.communicationgenerator.domainobject.CommunicationRequestDetail;
import com.nucleus.finnone.pro.communicationgenerator.domainobject.CommunicationTemplate;
import com.nucleus.finnone.pro.communicationgenerator.util.CommunicationGenerationHelper;
import com.nucleus.finnone.pro.communicationgenerator.vo.CommunicationGenerationDetailVO;
import com.nucleus.finnone.pro.general.util.ValidatorUtils;
import com.nucleus.master.BaseMasterEntity;
import com.nucleus.message.entity.MessageDeliveryStatus;
import com.nucleus.message.entity.MessageExchangeRecord;
import com.nucleus.message.entity.MessageExchangeRecordHistory;
import com.nucleus.persistence.EntityDaoImpl;
import com.nucleus.query.constants.QueryHint;
import com.nucleus.rules.model.SourceProduct;

@Repository("communicationGeneratorDAO")
public class CommunicationGeneratorDAO extends EntityDaoImpl implements ICommunicationGeneratorDAO {

    @Inject
    @Named("communicationGenerationHelper")
    private CommunicationGenerationHelper communicationGenerationHelper;
    
    public static final String LTCOMMUNICATIONCODE="communicationCode";
    public static final String LTGENERATEDMFILE="generateMergedFile";
    public static final String LTSOURCEPRODUCT="sourceProduct";
    public static final String LTACTIVEFLAG="activeFlag";
    public static final String LTAPRSTTYPE ="approvalStatusTypes";
    @Override
    public List<CommunicationParameter> findAdditionalMethodsForCommunicationDataPreperation(
            String communicationCode) {
		Map<String, Object> parameterMap = new HashMap<>();
		StringBuilder query = new StringBuilder("select cn.communicationParameters from CommunicationName cn where");
		query.append(" cn.activeFlag =:activeFlag");
		query.append(" and cn.communicationCode =:communicationCode");
		query.append(" and cn.masterLifeCycleData.approvalStatus IN (:approvalStatusList)");
		parameterMap.put(LTACTIVEFLAG, true);
		parameterMap.put(LTCOMMUNICATIONCODE, communicationCode);
		List<Integer> aprrovalStatusList = new ArrayList<Integer>();
		aprrovalStatusList.add(ApprovalStatus.APPROVED_MODIFIED);
		aprrovalStatusList.add(ApprovalStatus.APPROVED_DELETED_IN_PROGRESS);
		aprrovalStatusList.add(ApprovalStatus.APPROVED);
		parameterMap.put("approvalStatusList", aprrovalStatusList);
        

		
		String stringQuery = query.toString();
        Query dynamicQuery = getEntityManager().createQuery(stringQuery);
        for (Map.Entry<String, Object> entry : parameterMap.entrySet()) {
            dynamicQuery.setParameter(entry.getKey(), entry.getValue());
        }
        
        List<CommunicationParameter> list =  dynamicQuery.getResultList();
        return list;
    }
    
    @Override
    public List<CommunicationTemplate> getTemplateByCommunicationMasterId(Long id){
        
        
    	NeutrinoValidator.notNull(id);
		CommunicationName communicationName = getEntityManager().find(CommunicationName.class, id);
		return communicationName.getCommunicationTemplates();
    }
    
   
    
    @Override
    public List<Object[]> getAttributeValueForGenericParameter(Class<? extends GenericParameter> entityClass,String columnName){
           
        String entityName=entityClass.getSimpleName();
        String aliasName=entityClass.getSimpleName().concat("alias");
        StringBuilder query=new StringBuilder();
        query.append("select ")
            .append(aliasName)
            .append(".id")
            .append(",")
            .append(aliasName)
            .append("."+columnName)
            .append(" from ")
            .append(entityName)
            .append(" ")
            .append(aliasName)
            .append(" where ")
            .append(aliasName)
            .append(".entityLifeCycleData.persistenceStatus=:persistenceStatus");
        Query typedQuery = getEntityManager().createQuery(query.toString());
        typedQuery.setParameter("persistenceStatus",PersistenceStatus.ACTIVE);
        
        
       return typedQuery.getResultList();
    }

    
    @Override
    public List<Object[]> getAttributeValueForBaseMasterEntity(
            Class<? extends BaseMasterEntity> entityClass, String columnName,String dependentColumn,Long dependentColumnValue) {
        String entityName=entityClass.getSimpleName();
        String aliasName=entityClass.getSimpleName().concat("alias");
        StringBuilder query=new StringBuilder();
        query.append("select ")
            .append(aliasName)
            .append(".id")
            .append(",")
            .append(aliasName)
            .append("."+columnName)
            .append(" from ")
            .append(entityName)
            .append(" ")
            .append(aliasName)
            .append(" where ")
            .append(aliasName)
            .append(".masterLifeCycleData.approvalStatus in (:approvalStatusTypes)");
            if(dependentColumn!=null && dependentColumnValue!=null )
            {    query.append(" and ")
                    .append(aliasName)
                    .append("."+dependentColumn)
                    .append("= :dependentColumnValue");
            }
        Query typedQuery = getEntityManager().createQuery(query.toString());
        typedQuery.setParameter(LTAPRSTTYPE,CommunicationGenerationHelper.getApprovalStatusList());
        if(dependentColumn!=null && dependentColumnValue!=null ){
        	typedQuery.setParameter("dependentColumnValue",dependentColumnValue);
        }
       return typedQuery.getResultList();
    }

    
    @Override    
    public CommunicationEventRequestLog markCommEventRequestComplete(
            CommunicationEventRequestLog communicationEventRequestLog){
        communicationEventRequestLog.setStatus(STATUS_COMPLETED);
        return update(communicationEventRequestLog);
        
    }
    
    @Override
    public void deleteCommunicationEventRequest(
            CommunicationEventRequestLog communicationEventRequestLog){
        delete(communicationEventRequestLog);
    }
    
    @Override
    public void saveCommunicationEventRequestHistory(
            CommunicationEventRequestHistory communicationEventRequestHistory){
        persist(communicationEventRequestHistory);
    }

    @Override
    public List<CommunicationDataPreparationDetail> getActiveApprovedDetailBasedOnServiceSouceAndModule(
            SourceProduct sourceProduct, Long serviceSelectionId) {

        Query query =getEntityManager().createNamedQuery("getDataPrepServiceBasedOnModuleAndServSel");
        query.setParameter(LTSOURCEPRODUCT,sourceProduct);
        query.setParameter(LTACTIVEFLAG,true);
        query.setParameter("approvalStatus", communicationGenerationHelper.getApprovalStatusList());
        query.setParameter("serviceSelectionId", serviceSelectionId);
        
        return query.getResultList();
    }
    
    
    
    
    @Override
    public void deleteGeneratedCommunicationRequest(
            CommunicationRequestDetail communicationRequestDetail) {
    	if(contains(communicationRequestDetail)){
    		delete(communicationRequestDetail);
    		return;
    	}    	
    	Query namedQuery=getEntityManager().createNamedQuery("deleteGeneratedCommunication");
        namedQuery.setParameter("id", communicationRequestDetail.getId());
        namedQuery.executeUpdate();
    }
    
    @Override
    public void deleteMessageExchangeRecord(MessageExchangeRecord messageExchangeRecord) {
    	Query namedQuery = getEntityManager().createNamedQuery("MessageExchangeRecord.deleteMessageRecord");
    	namedQuery.setParameter("id", messageExchangeRecord.getId());
    	namedQuery.executeUpdate();
    }
    
    @Override 
    public <T extends MessageExchangeRecord> void updateMessageExchangeRecord(T shortMessageExchangeRecord) {
    	update(shortMessageExchangeRecord);
    }
    
    @Override
    public List<String> getDistinctRequestReferenceId(String communicationCode,SourceProduct sourceProduct,Boolean generateMergedFile) {
        NamedQueryExecutor<String> executor = new NamedQueryExecutor<String>(
                "CommunicationGenerationMergedFile.getDistinctRequestReferenceIds")
                .addParameter(LTSOURCEPRODUCT, sourceProduct)
                .addParameter(LTGENERATEDMFILE, generateMergedFile)
                .addParameter(LTCOMMUNICATIONCODE, communicationCode);
        return executeQuery(executor);
    }
    
    protected List<Integer> getApprovalStatusList() {
         List<Integer> approvalStatusList = new ArrayList<Integer>();
         approvalStatusList.add(ApprovalStatus.UNAPPROVED);
         approvalStatusList.add(ApprovalStatus.UNAPPROVED_HISTORY);
         approvalStatusList.add(ApprovalStatus.DELETED_APPROVED_IN_HISTORY);
         return approvalStatusList;
    }
    
    @Override
    public int getCommunicationGenerationDetailTotalRecordsSize(
            CommunicationGenerationDetailVO communicationGenerationDetailVO) {
        Query dynamicQuery=buildStringQuery(communicationGenerationDetailVO,false,null);
        int count=0;
        count=Integer.parseInt(dynamicQuery.getSingleResult().toString());

        return count;
    }
    
    @Override
    public List<CommunicationRequestDetail> getCommunicationGenerationDetail(
            CommunicationGenerationDetailVO communicationGenerationDetailVO,int startIndex,int batchSize) {
        Query dynamicQuery=buildStringQuery(communicationGenerationDetailVO,true,null);
        dynamicQuery.setFirstResult(startIndex);
        dynamicQuery.setMaxResults(batchSize);        
        return dynamicQuery.getResultList();
    }
    
    
	@Override
	public List<Long> getCommunicationGenerationDetailIds(
			CommunicationGenerationDetailVO communicationGenerationDetailVO, int startIndex, int batchSize) {
       
		Query dynamicQuery=buildStringQuery(communicationGenerationDetailVO,true,"select a.id from CommunicationRequestDetail a where ");
        dynamicQuery.setFirstResult(startIndex);
        dynamicQuery.setMaxResults(batchSize);        
        return dynamicQuery.getResultList();
	}

    
    
    @Override
    public int getCommunicationGenerationDetailTotalRecordsSizeForMergedFile(
            CommunicationGenerationDetailVO communicationGenerationDetailVO) {
        Query dynamicQuery=buildStringQuery(communicationGenerationDetailVO,false,null);
        int count;
        if (notNull(dynamicQuery.getResultList())) {
            count = Integer.parseInt(dynamicQuery.getResultList().toString());
        } else {
            count = 0;
        }
        
        return count;
    }
    

        protected Query buildStringQuery(CommunicationGenerationDetailVO communicationGenerationDetailVO,boolean selectQuery,String selectClause) {
        StringBuilder query= new StringBuilder();
        
        updateQueryWithSelectClause(query,selectQuery,selectClause);
        Map<String,Object> parameterMap=new HashMap<String,Object>();
        
        query.append(" a.status =:status");  
        parameterMap.put("status", STATUS_INITIATED);
        
        if (communicationGenerationDetailVO.getApplicablePrimaryEntityId() != null) {
            query.append(" and a.applicablePrimaryEntityId =:applicablePrimaryEntityId");
            parameterMap.put("applicablePrimaryEntityId", communicationGenerationDetailVO.getApplicablePrimaryEntityId());
        }

        
        if (!StringUtils.isBlank(communicationGenerationDetailVO.getCommunicationCode())) {
            query.append(" and a.communicationCode=:communicationCode");
            parameterMap.put(LTCOMMUNICATIONCODE, communicationGenerationDetailVO.getCommunicationCode());
        }
        if (!StringUtils.isBlank(communicationGenerationDetailVO.getSubjectURI())) {
            query.append(" and a.subjectURI=:subjectURI");
            parameterMap.put("subjectURI", communicationGenerationDetailVO.getSubjectURI());
        }
        
        if (communicationGenerationDetailVO.getSourceProduct() != null) {
            query.append(" and a.sourceProduct =:sourceProduct");
            parameterMap.put(LTSOURCEPRODUCT, communicationGenerationDetailVO.getSourceProduct());
        }
        

        if (!StringUtils.isBlank(communicationGenerationDetailVO.getRequestReferenceId())) {
            query.append(" and a.requestReferenceId =:requestReferenceId");
            parameterMap.put("requestReferenceId", communicationGenerationDetailVO.getRequestReferenceId());
        }
        if (communicationGenerationDetailVO.getGenerateMergedFile() != null) {
            query.append(" and a.generateMergedFile =:generateMergedFile");
            parameterMap.put(LTGENERATEDMFILE, communicationGenerationDetailVO.getGenerateMergedFile());
        }
        if(communicationGenerationDetailVO.getParentCommunicationRequestDetailId()==null)
        {
            query.append(" and a.parentCommunicationRequestDetail is null");
        }
        else
        {
            query.append(" and a.parentCommunicationRequestDetail.id =:parentCommunicationRequestDetailId");
            parameterMap.put("parentCommunicationRequestDetailId", communicationGenerationDetailVO.getParentCommunicationRequestDetailId());
        }
        if(communicationGenerationDetailVO.getSchedularInstanceId()!=null)
        {
            query.append(" and (a.schedularInstanceId !=:schedularInstanceId or a.schedularInstanceId is null)");
            parameterMap.put("schedularInstanceId", communicationGenerationDetailVO.getSchedularInstanceId());
        }
        
        query.append(" order by  a.entityLifeCycleData.creationTimeStamp desc, a.requestReferenceId");
        
        String stringQuery = query.toString();
        Query dynamicQuery=getEntityManager().createQuery(stringQuery);
        
        for(Entry<String,Object> entry : parameterMap.entrySet()) {
            dynamicQuery.setParameter(entry.getKey(), entry.getValue());
        }
        
        return dynamicQuery;
    }
    
    private void updateQueryWithSelectClause(StringBuilder query, boolean selectQuery, String selectClause) {

    	if(StringUtils.isNotEmpty(selectClause))
    	{
    		query.append(selectClause);
    		return;
    	}
    	
    	if (selectQuery) {
    		query.append("select a from CommunicationRequestDetail a where");
        } 
    	else 
    	{
    		query.append("select count(*) from CommunicationRequestDetail a where");
        }

			
	}

	@Override
    public CommunicationName getCommunicationFromCommunicationCode(
            String communicationCode) {
        NamedQueryExecutor<CommunicationName> executor = new NamedQueryExecutor<CommunicationName>(
                "Communication.getCommunicationFromCommunicationCode")
                .addParameter(LTCOMMUNICATIONCODE,communicationCode)
                .addQueryHint(QueryHint.QUERY_HINT_CACHEABLE, Boolean.TRUE);
        return executeQueryForSingleValue(executor);
    }

    
    @Override
    public List<CommunicationRequestDetail> getAttachmentsForEmail(long parentId) {
        NamedQueryExecutor<CommunicationRequestDetail> executor = new NamedQueryExecutor<CommunicationRequestDetail>(
                "CommunicationRequestDetail.getEmailAttachments")
                .addParameter("parentId",parentId);
        return executeQuery(executor);

    }

    
    @Override
	public List<Long> getCommunicationGenerationDetailForCommunication(CommunicationGenerationDetailVO communicationGenerationDetailVO,
			int startIndex, int batchSize)
	{
		NamedQueryExecutor<Long> executor = new NamedQueryExecutor<Long>(
				"CommunicationRequestDetail.getCommunicationGenerationDetailForCommunication")
				.addParameter("status",communicationGenerationDetailVO.getStatus())
				.addParameter("communicationCode",communicationGenerationDetailVO.getCommunicationCode())
				.addParameter("sourceProduct",communicationGenerationDetailVO.getSourceProduct())
				.addParameter("generateMergedFile",communicationGenerationDetailVO.getGenerateMergedFile())
				.addParameter("schedularInstanceId",communicationGenerationDetailVO.getSchedularInstanceId());
		return executeQuery(executor,startIndex,batchSize);
    }
	

    
    
    @Override
	public List<CommunicationRequestDetail> getCommunicationGenerationDetailObjForCommunication(CommunicationGenerationDetailVO communicationGenerationDetailVO,
			int startIndex, int batchSize)
	{
		NamedQueryExecutor<CommunicationRequestDetail> executor = null;
		if (communicationGenerationDetailVO.getCommunicationCode() != null) {
			executor = new NamedQueryExecutor<CommunicationRequestDetail>(
					"CommunicationRequestDetail.getCommunicationGenerationDetailObjForCommunication")
							.addParameter("status", CommunicationRequestDetail.INITIATED)
							.addParameter("communicationCode", communicationGenerationDetailVO.getCommunicationCode())
							.addParameter("sourceProduct", communicationGenerationDetailVO.getSourceProduct())
							.addParameter("generateMergedFile", communicationGenerationDetailVO.getGenerateMergedFile())
							.addParameter("schedularInstanceId",
									communicationGenerationDetailVO.getSchedularInstanceId());
		} else {
			executor = new NamedQueryExecutor<CommunicationRequestDetail>(
					"CommunicationRequestDetail.getCommunicationGenerationDetailsForInitiatedStatus")
							.addParameter("status", CommunicationRequestDetail.INITIATED)
							.addParameter("sourceProduct", communicationGenerationDetailVO.getSourceProduct())
							.addParameter("generateMergedFile", communicationGenerationDetailVO.getGenerateMergedFile())
							.addParameter("schedularInstanceId",
									communicationGenerationDetailVO.getSchedularInstanceId());
		}
		return executeQuery(executor, startIndex, batchSize);
	}
	
    
    
	@Override
	public List<CommunicationRequestDetail> getCommunicationGenerationDetailObjForCommunicationByRefId(
			CommunicationGenerationDetailVO communicationGenerationDetailVO, int startIndex, int batchSize) {
		NamedQueryExecutor<CommunicationRequestDetail> executor = new NamedQueryExecutor<CommunicationRequestDetail>(
   				"CommunicationRequestDetail.getCommunicationGenerationDetailObjForCommunicationByRefId")
   				.addParameter("status",communicationGenerationDetailVO.getStatus())
   				.addParameter("communicationCode",communicationGenerationDetailVO.getCommunicationCode())
   				.addParameter("sourceProduct",communicationGenerationDetailVO.getSourceProduct())
   				.addParameter("generateMergedFile",communicationGenerationDetailVO.getGenerateMergedFile())
   				.addParameter("schedularInstanceId",communicationGenerationDetailVO.getSchedularInstanceId())
   				.addParameter("requestReferenceId",communicationGenerationDetailVO.getRequestReferenceId());
   		return executeQuery(executor,startIndex,batchSize);
    }
	
	@Override
	public CommunicationRequestDetail getCommunicationGenerationDetailByUniqueId( String uniqueId) {
		NamedQueryExecutor<CommunicationRequestDetail> executor = new NamedQueryExecutor<CommunicationRequestDetail>("CommunicationGenerationDetail.getCommunicationGenerationDetailByUniqueId")
				.addParameter("uniqueRequestId", uniqueId);
		List<CommunicationRequestDetail> communicationRequestDetailList = executeQuery(executor);
		if (ValidatorUtils.hasElements(communicationRequestDetailList)) {
			return communicationRequestDetailList.get(0);
		}
		return null;
	}
	
	@Override
	public <T extends MessageExchangeRecord> List<T> getUndeliveredMessageExchangeRecord(Class<?> entityClass, Long startId, int batchSize) {
		List<MessageDeliveryStatus> statusList = new ArrayList<>();
		statusList.add(MessageDeliveryStatus.FAILED);
		statusList.add(MessageDeliveryStatus.FAILED_DELIVERY);
		statusList.add(MessageDeliveryStatus.FAILED_SENDING_TO_INTEGRATION);
		statusList.add(MessageDeliveryStatus.FAILED_AT_INTEGRATION);
    	StringBuilder queryBuilder = new StringBuilder();
    	queryBuilder.append("FROM ")
    		.append(entityClass.getSimpleName())
    		.append(" mer where mer.deliveryStatus in (:deliveryStatus) and mer.id >= :id order by id asc");
		JPAQueryExecutor<T> jPAQueryExecutor = new JPAQueryExecutor<>(queryBuilder.toString());
		jPAQueryExecutor.addParameter("id", startId)
				.addParameter("deliveryStatus", statusList);
		return executeQuery(jPAQueryExecutor, 0, batchSize);
	}
	
	@Override
	public Long getCountOfFailedMessages() {
		
		List<MessageDeliveryStatus> statusList = new ArrayList<>();
		statusList.add(MessageDeliveryStatus.FAILED);
		statusList.add(MessageDeliveryStatus.FAILED_DELIVERY);
		statusList.add(MessageDeliveryStatus.FAILED_SENDING_TO_INTEGRATION);
		statusList.add(MessageDeliveryStatus.FAILED_AT_INTEGRATION);
		
		NamedQueryExecutor<Long> executor = new NamedQueryExecutor<Long>(
				"MessageExchangeRecord.CountUnsentMessages").addParameter("deliveryStatus", statusList);
		
		return executeQueryForSingleValue(executor);
		
	}

	@Override
	public <T extends MessageExchangeRecord> void saveMessageExchangeRecord(T messageExchangeRecord) {
		persist(messageExchangeRecord);
	}
		
	@Override
	public <T extends MessageExchangeRecord> T getMessageExchangeRecordByUniqueId(Class<?> exchangeRecordClass, String uniqueId) {
		StringBuilder queryBuilder = new StringBuilder();
    	queryBuilder.append("FROM ")
    		.append(exchangeRecordClass.getSimpleName())
    		.append(" mer where mer.uniqueRequestId = :uniqueRequestId");
		JPAQueryExecutor<T> jPAQueryExecutor = new JPAQueryExecutor<>(queryBuilder.toString());
		jPAQueryExecutor.addParameter("uniqueRequestId", uniqueId);
		List<T> messageExchangeRecords = executeQuery(jPAQueryExecutor);
		if (ValidatorUtils.hasElements(messageExchangeRecords)) {
			return messageExchangeRecords.get(0);
		}
		return null;
	}

	@Override
	public MessageExchangeRecordHistory getMessageExchangeRecordHistoryByUniqueId(String uniqueId) {
		NamedQueryExecutor<MessageExchangeRecordHistory> executor = new NamedQueryExecutor<MessageExchangeRecordHistory>(
				"getMessageExchangeRecordHistoryByUniqueId").addParameter("uniqueRequestId", uniqueId);
		List<MessageExchangeRecordHistory> messageExchangeHistories = executeQuery(executor);
		if (ValidatorUtils.hasElements(messageExchangeHistories)) {
			return messageExchangeHistories.get(0);
		}
		return null;
	}

	@Override
	public void updateMessageExchangeRecordHistory(
			MessageExchangeRecordHistory messageExchnageRecordHistory) {
		update(messageExchnageRecordHistory);
	}
	
}

