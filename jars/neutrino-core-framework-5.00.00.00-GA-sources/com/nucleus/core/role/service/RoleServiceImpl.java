package com.nucleus.core.role.service;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import com.nucleus.authority.Authority;
import com.nucleus.core.role.entity.Role;
import com.nucleus.dao.query.NamedQueryExecutor;
import com.nucleus.entity.ApprovalStatus;
import com.nucleus.persistence.EntityDao;

@Named("roleService")
public class RoleServiceImpl implements RoleService {

    @Inject
    @Named("entityDao")
    private EntityDao entityDao;

    @Override
    public List<Role> getRolesByAuthority(long authorityId) {
        NamedQueryExecutor<Role> executor = new NamedQueryExecutor<Role>("role.getRolesByAuthority");
        executor.addParameter("authorityId", authorityId);
        List<Role> roleList = entityDao.executeQuery(executor);
        return roleList;
    }

    @Override
    public Authority getAuthorityByCode(String authorityCode) {

        NamedQueryExecutor<Authority> executor = new NamedQueryExecutor<Authority>("authority.getByCode");
        executor.addParameter("authorityCode", authorityCode);
        return entityDao.executeQueryForSingleValue(executor);

    }

	@Override
	public List<Object[]> getAllApprovedAndActiveRoles() {
		NamedQueryExecutor<Object[]> executor = new NamedQueryExecutor<Object[]>("role.getAllApprovedAndActiveRoles");
		executor.addParameter("approvalStatusList", ApprovalStatus.APPROVED_RECORD_STATUS_LIST);
        return entityDao.executeQuery(executor);
	}

}
