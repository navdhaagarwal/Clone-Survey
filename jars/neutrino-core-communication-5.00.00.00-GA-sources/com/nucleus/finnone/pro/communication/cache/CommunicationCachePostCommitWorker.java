package com.nucleus.finnone.pro.communication.cache;

import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;

import com.nucleus.core.transaction.TransactionPostCommitWork;
import com.nucleus.finnone.pro.communication.cache.service.ICommunicationCacheService;

@Named("communicationCachePostCommitWorker")
public class CommunicationCachePostCommitWorker implements TransactionPostCommitWork {

	@Inject
	@Named("communicationCacheService")
	public ICommunicationCacheService communicationCacheService;

	@SuppressWarnings("unchecked")
	@Override
	public void work(Object argument) {
		communicationCacheService.refreshCommunicationCache((Map<String,Object>) argument);

	}



}
