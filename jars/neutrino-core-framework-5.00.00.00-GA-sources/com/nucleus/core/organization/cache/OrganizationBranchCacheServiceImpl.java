package com.nucleus.core.organization.cache;

import static com.nucleus.core.organization.cache.OrgBranchInfoCachePopulator.ORGANIZATION_BRANCH_INFO_ID_LIST;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.transaction.annotation.Transactional;

import com.nucleus.core.organization.entity.OrganizationBranch;
import com.nucleus.core.organization.listener.OrganizationBranchApprovalListener;
import com.nucleus.core.organization.service.OrganizationService;
import com.nucleus.entity.EntityId;
import com.nucleus.event.EventTypes;
import com.nucleus.finnone.pro.cache.common.NeutrinoCachePopulator;
import com.nucleus.finnone.pro.cache.common.NeutrinoCachePopulator.Action;
import com.nucleus.finnone.pro.cache.constants.FWCacheConstants;
import com.nucleus.finnone.pro.cache.entity.ImpactedCache;
import com.nucleus.persistence.EntityDao;

/**
 * @author neha.garg1
 *
 */
@Named("organizationBranchCacheService")
public class OrganizationBranchCacheServiceImpl implements OrganizationBranchCacheService {
	
	@Inject
	@Named("orgBranchInfoCachePopulator")
	private NeutrinoCachePopulator orgBranchInfoCachePopulator;
	
	@Inject
	@Named("entityDao")
	protected EntityDao entityDao;
	
	@Inject
	@Named("organizationService")
	public OrganizationService organizationService;
	
	
	
	@SuppressWarnings("unchecked")
	@Override
	@Transactional
	public void refreshOrganizationBranchCache(Map<String,Object> dataMap) {
		EntityId orgBranchEntityId = (EntityId) dataMap.get(OrganizationBranchApprovalListener.OWNER_ENTITY_ID);
		int eventType = (int) dataMap.get(OrganizationBranchApprovalListener.EVENT_TYPE);
		OrganizationBranch orgBranch =  entityDao.get(orgBranchEntityId);

		if (eventType == EventTypes.MAKER_CHECKER_APPROVED) {
			orgBranchInfoCachePopulator.update((Map<String, ImpactedCache>) dataMap.get(FWCacheConstants.IMPACTED_CACHE_MAP), Action.INSERT, orgBranch);
		} else if (eventType == EventTypes.MAKER_CHECKER_DELETION_APPROVED) {
			orgBranchInfoCachePopulator.update((Map<String, ImpactedCache>) dataMap.get(FWCacheConstants.IMPACTED_CACHE_MAP), Action.DELETE, orgBranch);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<OrganizationBranch> getAllApprovedBranchesFromCache() {
		
		
		List<Long> organizationBranchIdList = (List<Long>) orgBranchInfoCachePopulator
				.get(ORGANIZATION_BRANCH_INFO_ID_LIST);
		
		List<OrganizationBranch> organizationBranchList = new ArrayList<>();
		for(Long orgBranchInfoId: organizationBranchIdList)
		{
			organizationBranchList.add(entityDao.find(OrganizationBranch.class, orgBranchInfoId));
		}
		return organizationBranchList;
	}
}
