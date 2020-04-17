package com.nucleus.cas.delegation.service;


import com.nucleus.authority.Authority;
import com.nucleus.core.organization.entity.OrganizationBranch;
import com.nucleus.service.BaseService;
import com.nucleus.user.UserInfo;

import java.util.List;
import java.util.Set;

public interface DelegationLoginService extends BaseService {

    void getOrganizationBranchesDelegatedToUser(UserInfo delegatedUser, List<OrganizationBranch> orgBranchList);

    void getAuthoritiesDelegatedToUser(UserInfo delegatedUser, Set<Authority> authSet);

    void setDelegatedFromUserAndTeamUriSet(UserInfo userInfo);
}