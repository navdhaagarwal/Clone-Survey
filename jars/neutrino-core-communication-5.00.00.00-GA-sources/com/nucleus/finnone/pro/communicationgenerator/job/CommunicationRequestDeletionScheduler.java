package com.nucleus.finnone.pro.communicationgenerator.job;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.Query;

import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.nucleus.core.scheduler.NeutrinoScheduler;
import com.nucleus.finnone.pro.communicationgenerator.domainobject.CommunicationRequestDetail;
import com.nucleus.finnone.pro.general.util.ProcessorAndMemoryInformationUtils;
import com.nucleus.persistence.EntityDao;

@Named("communicationRequestDeletionScheduler")
public class CommunicationRequestDeletionScheduler implements NeutrinoScheduler {
	
	private static final int BATCH_SIZE = 200;
	private static final double CPU_USAGE_THRESHOLD = 0.60; //deletion should start only in case of cpu usage is low.
	
	@Inject
	@Named("entityDao")
	private EntityDao entityDao;
	
	@Override
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void execute() {
		if (ProcessorAndMemoryInformationUtils.ifCpuUsageIsLessThanThreshold(CPU_USAGE_THRESHOLD)) {
			runDeletionInBatch(getNamedQueryForDeletion());
		}
	}

	private Query getNamedQueryForDeletion() {
		List<Character> statusListForDeletion = new ArrayList<>();
		statusListForDeletion.add(CommunicationRequestDetail.COMPLETED);
		statusListForDeletion.add(CommunicationRequestDetail.FAILED);
		Query namedQuery = entityDao.getEntityManager().createNamedQuery("getCommunicationRequestListForDeletion");
        namedQuery.setParameter("status", statusListForDeletion);
        return namedQuery;
	}

	public void runDeletionInBatch(Query namedQuery) {
        boolean done = false;
		int firstResult = 0;
		while (!done && ProcessorAndMemoryInformationUtils.ifCpuUsageIsLessThanThreshold(CPU_USAGE_THRESHOLD)) {
			namedQuery.setFirstResult(firstResult);
			namedQuery.setMaxResults(BATCH_SIZE);
			List<CommunicationRequestDetail> listOfRecords = namedQuery.getResultList();
			if (listOfRecords != null && !listOfRecords.isEmpty()) {
				for (CommunicationRequestDetail crd : listOfRecords) {
					entityDao.delete(crd);
				}
				if (listOfRecords.size() < BATCH_SIZE) {
					done = true;
				} else {
					firstResult += BATCH_SIZE;
				}
			} else {
				done = true;
			}
		}
	}

}
