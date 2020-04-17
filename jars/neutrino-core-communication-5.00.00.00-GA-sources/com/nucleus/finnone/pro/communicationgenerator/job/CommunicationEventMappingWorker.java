package com.nucleus.finnone.pro.communicationgenerator.job;

import javax.inject.Inject;
import javax.inject.Named;

import com.nucleus.core.transaction.TransactionPostCommitWork;
import com.nucleus.finnone.pro.communicationgenerator.domainobject.CommunicationEventRequestLog;
import com.nucleus.finnone.pro.communicationgenerator.service.ICommunicationEventLoggerDelegate;

@Named("communicationEventMappingWorker")
public class CommunicationEventMappingWorker implements TransactionPostCommitWork {

	@Inject
	@Named("communicationEventLoggerDelegate")
	private ICommunicationEventLoggerDelegate communicationEventLoggerDelegate;


	@Override
	public void work(Object argument) {
		if (argument instanceof CommunicationEventRequestLog) {
			CommunicationEventRequestLog commEventRequestLog = (CommunicationEventRequestLog) argument;
			communicationEventLoggerDelegate.selectTemplateAndGenerateCommForImmediateReq(commEventRequestLog);
		}
	}

}
