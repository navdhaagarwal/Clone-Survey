package com.nucleus.finnone.pro.communicationgenerator.dao;

import static com.nucleus.finnone.pro.general.util.ValidatorUtils.notNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.Query;

import org.springframework.stereotype.Repository;

import com.nucleus.core.event.EventCode;
import com.nucleus.dao.query.NamedQueryExecutor;
import com.nucleus.entity.ApprovalStatus;
import com.nucleus.finnone.pro.communicationgenerator.domainobject.CommunicationName;
import com.nucleus.persistence.EntityDao;
import com.nucleus.persistence.EntityDaoImpl;
import com.nucleus.query.constants.QueryHint;
import com.nucleus.rules.model.SourceProduct;

@Repository("communicationSchedulerDAO")
public class CommunicationSchedulerDAO extends EntityDaoImpl implements
		ICommunicationSchedulerDAO {

	@Inject
	@Named("entityDao")
	private EntityDao entityDao;
	
	private static final String SCHEDULER_NAME="schedulerName";
	private static final String SOURCE_PRODUCT="sourceProduct";
	private static final String APPROVAL_STATUS_LIST="approvalStatusList";
	private static final String ID="id";
	private static final String UUID="uuid";
	
	

	@Override
	public List<EventCode> getUnMappedEventCodesBasedOnModule(
			SourceProduct sourceProduct) {
		List<Integer> approvalStatusList=getApprovalStatusList();
		NamedQueryExecutor<EventCode> executor = new NamedQueryExecutor<EventCode>(
				"CommunicationEventRequestSchedulerMapping.getUnMappedEventCodesBasedOnModule")
				.addParameter(SOURCE_PRODUCT, sourceProduct).addParameter(APPROVAL_STATUS_LIST, approvalStatusList).addQueryHint(
						QueryHint.QUERY_HINT_CACHEABLE, Boolean.TRUE);
		return entityDao.executeQuery(executor);
	}

	@Override
	public List<EventCode> getEventCodeListFromIds(Long[] eventCodeIds) {
		/*NamedQueryExecutor<EventCode> executor = new NamedQueryExecutor<EventCode>(
				"CommunicationEventRequestScheduler.getEventCodeListFromIds")
				.addParameter("ids", Arrays.asList(eventCodeIds)).addQueryHint(
						QueryHint.QUERY_HINT_CACHEABLE, Boolean.TRUE);*/
		
		/*
		 *    @Override
    public List<OrgBranchInfo> getTopBranchesAmongBranchIds(String systemName, List<Long> branchIds) {
        List<Integer> statusList = new ArrayList<Integer>();
        statusList.add(ApprovalStatus.APPROVED);
        statusList.add(ApprovalStatus.APPROVED_MODIFIED);
        statusList.add(ApprovalStatus.APPROVED_DELETED);
        statusList.add(ApprovalStatus.APPROVED_DELETED_IN_PROGRESS);
        Map<String, Object> parameters=new HashMap<>();
        parameters.put(SYSTEM_NAME, systemName);
        parameters.put("approvalStatus", statusList);
        return entityDao.executeSingleInClauseHQLQuery("Organization.getTopOrgBranchesAmongBranchIds", "branchIds",
				branchIds, parameters,OrgBranchInfo.class);
    }
		 *
		 *
		 */
		
		Map<String, Object> parameters=new HashMap<>();
		
		return entityDao.executeSingleInClauseHQLQuery("CommunicationEventRequestScheduler.getEventCodeListFromIds","ids",Arrays.asList(eventCodeIds),parameters,EventCode.class);
		//return entityDao.executeQuery(executor,eventCodeIds,);
		
	}


	@Override
	public List<CommunicationName> getUnMappedCommunicationsBasedOnModule(
			SourceProduct sourceProduct) {
		List<Integer> approvalStatusList=getApprovalStatusList();
		NamedQueryExecutor<CommunicationName> executor = new NamedQueryExecutor<CommunicationName>(
				"CommunicationGenerationSchedulerMapping.getUnMappedCommunicationsBasedOnModule")
				.addParameter(SOURCE_PRODUCT, sourceProduct).addParameter(APPROVAL_STATUS_LIST, approvalStatusList).addQueryHint(
						QueryHint.QUERY_HINT_CACHEABLE, Boolean.TRUE);
		return entityDao.executeQuery(executor);
	}

	@Override
	public List<CommunicationName> getCommunicationListFromIds(
			Long[] communicationIds) {
		NamedQueryExecutor<CommunicationName> executor = new NamedQueryExecutor<CommunicationName>(
				"CommunicationGenerationScheduler.getCommunicationListFromIds")
				.addParameter("ids", Arrays.asList(communicationIds))
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
	public String fetchNumberOfDuplicateSchedulers(String schedulerName,
			boolean eventRequest, SourceProduct sourceProduct, Long id,
			String uuid) {
		

		List<Integer> approvalStatusList = getApprovalStatusList();

		StringBuilder query = new StringBuilder();
		if (eventRequest) {
			query.append("select count(*) from CommunicationEventRequestScheduler cs where ");
		} else {
			query.append("select count(*) from CommunicationGenerationScheduler cs where ");
		}
		Map<String, Object> parameterMap = new HashMap<String, Object>();

		if (notNull(schedulerName)) {
			query.append(" cs.schedulerName =:schedulerName");
			parameterMap.put(SCHEDULER_NAME, schedulerName);
		}

		if (notNull(sourceProduct)) {
			query.append(" and cs.sourceProduct =:sourceProduct");
			parameterMap.put(SOURCE_PRODUCT, sourceProduct);
		}

		query.append(" and cs.masterLifeCycleData.approvalStatus not in (:approvalStatusList)");
		parameterMap.put(APPROVAL_STATUS_LIST, approvalStatusList);

		if (notNull(id)) {
			query.append(" and cs.id not in (:id) ");
			parameterMap.put(ID, id);
		}

		if (notNull(uuid)) {
			query.append(" and cs.entityLifeCycleData.uuid not in (:uuid)");
			parameterMap.put(UUID, uuid);
		}

		String stringQuery = query.toString();
		Query dynamicQuery = getEntityManager().createQuery(stringQuery);

		for (Entry<String, Object> entry : parameterMap.entrySet()) {
			dynamicQuery.setParameter(entry.getKey(), entry.getValue());
		}

		return dynamicQuery.getSingleResult().toString();

	}

}
