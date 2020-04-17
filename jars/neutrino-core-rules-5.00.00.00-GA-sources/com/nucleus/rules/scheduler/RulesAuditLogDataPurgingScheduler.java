package com.nucleus.rules.scheduler;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.Query;
import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Value;

import com.nucleus.core.scheduler.NeutrinoScheduler;
import com.nucleus.persistence.EntityDao;
import com.nucleus.rules.model.RulesAuditLog;

@Named("rulesAuditLogDataPurgingScheduler")
public class RulesAuditLogDataPurgingScheduler implements NeutrinoScheduler {

	@Inject
	@Named("entityDao")
	EntityDao entityDao;

	@Value(value = "${rule.audit.data.purge.batchsize}")
	private int batchSize;

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nucleus.core.scheduler.NeutrinoScheduler#execute()
	 */
	@Transactional
	public void execute() {

		StringBuilder query = new StringBuilder(
				"select rulesAuditLog from RulesAuditLog rulesAuditLog  where rulesAuditLog.purgingRequired= :purgingRequired ");
		Map<String, Object> parameterMap = new HashMap<>();

		parameterMap.put("purgingRequired", true);

		String stringQuery = query.toString();
		Query dynamicQuery = entityDao.getEntityManager().createQuery(stringQuery);

		for (Entry<String, Object> entry : parameterMap.entrySet()) {
			dynamicQuery.setParameter(entry.getKey(), entry.getValue());
		}
		executeQueryInBatches(dynamicQuery);
	}

	/**
	 * @param dynamicQuery
	 */
	private void executeQueryInBatches(Query dynamicQuery) {
		boolean done = false;
		int firstResult = 0;
		while (!done) {
			dynamicQuery.setFirstResult(firstResult);
			dynamicQuery.setMaxResults(batchSize);
			List<RulesAuditLog> listOfRecords = dynamicQuery.getResultList();
			if (listOfRecords != null && !listOfRecords.isEmpty()) {

				for (RulesAuditLog rulesAuditLog : listOfRecords) {
					entityDao.delete(rulesAuditLog);
				}
				if (listOfRecords.size() < batchSize) {
					done = true;
				} else {
					firstResult += batchSize;
				}
			} else {
				done = true;
			}

		}
	}

}
