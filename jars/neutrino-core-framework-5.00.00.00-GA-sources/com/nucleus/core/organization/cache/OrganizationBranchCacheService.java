package com.nucleus.core.organization.cache;

import java.util.List;
import java.util.Map;

import com.nucleus.core.organization.entity.OrganizationBranch;

public interface OrganizationBranchCacheService {
	
	/**
	 * @return
	 */
	List<OrganizationBranch> getAllApprovedBranchesFromCache(); 
     /**
     * @param orgBranchEntityId
     * @param eventType
     */
    void refreshOrganizationBranchCache(Map<String,Object> dataMap);
}
