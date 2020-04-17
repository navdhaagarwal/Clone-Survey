package com.nucleus.core.organization.cache;

import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;

import com.nucleus.core.transaction.TransactionPostCommitWork;

@Named("organizationBranchCachePostCommitWorker")
public class OrganizationBranchCachePostCommitWorker implements TransactionPostCommitWork {
	@Inject
	@Named("organizationBranchCacheService")
	public OrganizationBranchCacheService organizationBranchCacheService;

	@SuppressWarnings("unchecked")
	@Override
	public void work(Object argument) {
		organizationBranchCacheService.refreshOrganizationBranchCache((Map<String,Object>) argument);

	}

}
