package com.nucleus.core.organization.cache;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.transaction.annotation.Transactional;

import com.nucleus.core.organization.entity.OrganizationBranch;
import com.nucleus.core.organization.service.OrganizationService;
import com.nucleus.finnone.pro.cache.common.FWCachePopulator;
import com.nucleus.finnone.pro.cache.constants.FWCacheConstants;
import com.nucleus.finnone.pro.general.util.ValidatorUtils;
import com.nucleus.logging.BaseLoggers;

@Named("orgBranchInfoCachePopulator")
public class OrgBranchInfoCachePopulator extends FWCachePopulator {

	@Inject
	@Named("organizationService")
	public OrganizationService organizationService;

	public static final String ORGANIZATION_BRANCH_INFO_ID_LIST = "ORGANIZATION_BRANCH_INFO_ID_LIST";

	@Override
	public void init() {
		BaseLoggers.flowLogger.debug("Init Called : OrgBranchInfoCachePopulator");
	}

	@Override
	@Transactional(readOnly = true)
	public Object fallback(Object key) {
		if (key.equals(ORGANIZATION_BRANCH_INFO_ID_LIST)) {
			return this.getIdListFromDB();

		}
		return null;
	}

	private Object getIdListFromDB() {
		List<Long> orgBranchIdList = new ArrayList<>();
		for (OrganizationBranch orgBranch : organizationService.getAllApprovedBranches()) {
			orgBranchIdList.add(orgBranch.getId());
		}
		return orgBranchIdList;
	}

	@Override
	@Transactional(readOnly = true)
	public void build(Long tenantId) {
		put(ORGANIZATION_BRANCH_INFO_ID_LIST, this.getIdListFromDB());
	}

	@SuppressWarnings("unchecked")
	@Override
	public void update(Action action, Object object) {
		OrganizationBranch orgBranch = (OrganizationBranch) object;
		if (ValidatorUtils.notNull(orgBranch)) {
			List<Long> orgBranchIdList = (List<Long>) get(ORGANIZATION_BRANCH_INFO_ID_LIST);
			if (action.equals(Action.INSERT)) {
				orgBranchIdList.add(orgBranch.getId());
			} else if (action.equals(Action.DELETE)) {
				orgBranchIdList.remove(orgBranch.getId());
			}
			put(ORGANIZATION_BRANCH_INFO_ID_LIST, orgBranchIdList);
		}

	}

	@Override
	public String getNeutrinoCacheName() {
		return FWCacheConstants.ORGANIZATION_BRANCH_INFO_CACHE;
	}

	@Override
	public String getCacheGroupName() {
		return FWCacheConstants.ORGANIZATION_BRANCH_CACHE_GROUP;
	}

}
