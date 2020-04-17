package com.nucleus.finnone.pro.communicationgenerator.dao;

import static com.nucleus.finnone.pro.general.util.ValidatorUtils.notNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import com.nucleus.core.event.EventCode;
import com.nucleus.core.event.EventCodeType;
import com.nucleus.dao.query.NamedQueryExecutor;
import com.nucleus.entity.ApprovalStatus;
import com.nucleus.entity.BaseEntity;
import com.nucleus.persistence.DaoUtils;
import com.nucleus.persistence.EntityDao;
import com.nucleus.query.constants.QueryHint;
import com.nucleus.rules.model.SourceProduct;

@Named("adHocEventLogCriteriaDAO")
public class AdHocEventLogCriteriaDAO implements IAdHocEventLogCriteriaDAO {

    @PersistenceContext
    private EntityManager entityManager;

    @Inject
    @Named("entityDao")
    private EntityDao entityDao;

    private static final String QUERY_SELECT = "select en";
    
    private static final String QUERY_COUNT_SELECT="select count(*)";

    private static final String QUERY_FROM_CLAUSE = " from ";

    private static final String QUERY_AS_CLAUSE = " as en";

    private static final String ADHOC_EVENT_PARENT_CODE = "adhocEvent";
    
    private static final String QUERY_ORDER_BY_CLAUSE=" order by en.id";
    
    private static final String SCHEDULER_NAME="schedulerName";
    
	private static final String SOURCE_PRODUCT="sourceProduct";
	
	private static final String APPROVAL_STATUS_LIST="approvalStatusList";
	
	private static final String ID="id";
	
	private static final String UUID="uuid";

    @SuppressWarnings("unchecked")
    @Override
    public List<Object> getEntitiesFromRootContextObject(String rootElement) {
        StringBuilder queryString = new StringBuilder(QUERY_SELECT);
        queryString.append(QUERY_FROM_CLAUSE);
        queryString.append(rootElement);
        queryString.append(QUERY_AS_CLAUSE);
        Query query = entityManager.createQuery(queryString.toString());
        return DaoUtils.executeQuery(entityManager, query);
    }

    @Override
    public List<EventCode> getEventCodesBasedOnModule(SourceProduct sourceProduct) {
        List<Integer> approvalStatusList = getApprovalStatusList();
        NamedQueryExecutor<EventCode> executor = new NamedQueryExecutor<EventCode>(
                "AdHocEventLogSchedule.getEventCodeListBasedOnModule")
                .addParameter(SOURCE_PRODUCT, sourceProduct)
                .addParameter(APPROVAL_STATUS_LIST, approvalStatusList)
                .addParameter("adhocEventParentCode", ADHOC_EVENT_PARENT_CODE)
                .addQueryHint(QueryHint.QUERY_HINT_CACHEABLE, Boolean.TRUE);
        return entityDao.executeQuery(executor);
    }

    @Override
    public  List<EventCode> getEventCodesBasedOnModuleAndEventCodeType(SourceProduct sourceProduct,EventCodeType eventCodeType) {
    	List<Integer> approvalStatusList = getApprovalStatusList();
        NamedQueryExecutor<EventCode> executor = new NamedQueryExecutor<EventCode>(
                "AdHocEventLogSchedule.getEventCodesBasedOnModuleAndEventCodeType")
                .addParameter(SOURCE_PRODUCT, sourceProduct)
                .addParameter(APPROVAL_STATUS_LIST, approvalStatusList)
                .addParameter("adhocEventParentCode", eventCodeType.getCode())
                .addQueryHint(QueryHint.QUERY_HINT_CACHEABLE, Boolean.TRUE);
        return entityDao.executeQuery(executor);
    }
    protected List<Integer> getApprovalStatusList() {
        List<Integer> approvalStatusList = new ArrayList<Integer>();
        approvalStatusList.add(ApprovalStatus.UNAPPROVED);
        approvalStatusList.add(ApprovalStatus.UNAPPROVED_HISTORY);
        approvalStatusList.add(ApprovalStatus.DELETED_APPROVED_IN_HISTORY);
        return approvalStatusList;
    }

    @Override
    public List<BaseEntity> fetchEntitiesBasedOnBatchSize(int batchCount,
            int batchSize,String rootContextObject) {
        StringBuilder queryString = new StringBuilder(QUERY_SELECT);
        queryString.append(QUERY_FROM_CLAUSE);
        queryString.append(rootContextObject);
        queryString.append(QUERY_AS_CLAUSE);
        queryString.append(QUERY_ORDER_BY_CLAUSE);
        Query query = entityManager.createQuery(queryString.toString());
        query.setFirstResult(batchCount);
        query.setMaxResults(batchSize);
        return DaoUtils.executeQuery(entityManager, query);
       
    }
        
    @Override
    public int fetchTotalRecordSize(String rootContextObject) {
        StringBuilder queryString = new StringBuilder(QUERY_COUNT_SELECT);
        queryString.append(QUERY_FROM_CLAUSE);
        queryString.append(rootContextObject);
        Query query = entityManager.createQuery(queryString.toString());
        return Integer.parseInt(query.getSingleResult().toString());       
    }
    
    @Override
    public String fetchNumberOfDuplicateSchedulersOfAdhocCommunication(String schedulerName,
			SourceProduct sourceProduct,Long id,String uuid) {
		

		List<Integer> approvalStatusList = getApprovalStatusList();

		StringBuilder query = new StringBuilder();
		query.append("select count(*) from AdHocEventLogSchedule aels where ");
		Map<String, Object> parameterMap = new HashMap<String, Object>();

		if (notNull(schedulerName)) {
			query.append(" aels.schedulerName =:schedulerName");
			parameterMap.put(SCHEDULER_NAME, schedulerName);
		}

		if (notNull(sourceProduct)) {
			query.append(" and aels.sourceProduct =:sourceProduct");
			parameterMap.put(SOURCE_PRODUCT, sourceProduct);
		}

		query.append(" and aels.masterLifeCycleData.approvalStatus not in (:approvalStatusList)");
		parameterMap.put(APPROVAL_STATUS_LIST, approvalStatusList);

		if (notNull(id)) {
			query.append(" and aels.id not in (:id) ");
			parameterMap.put(ID, id);
		}

		if (notNull(uuid)) {
			query.append(" and aels.entityLifeCycleData.uuid not in (:uuid)");
			parameterMap.put(UUID, uuid);
		}

		String stringQuery = query.toString();
		Query dynamicQuery = entityManager.createQuery(stringQuery);

		for (Entry<String, Object> entry : parameterMap.entrySet()) {
			dynamicQuery.setParameter(entry.getKey(), entry.getValue());
		}

		return dynamicQuery.getSingleResult().toString();

	}

	
}