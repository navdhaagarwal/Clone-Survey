package com.nucleus.businessmapping.service;

import java.util.List;

import javax.persistence.Query;

import com.nucleus.businessmapping.entity.UserOrgBranchMapping;
import com.nucleus.persistence.BaseDao;

public interface UserManagementDao extends BaseDao<UserOrgBranchMapping> {
    
    public List<Object> getAllUsers();
    
    public List<Object> getAllUsersInBranch(String branchId);

    public Query createQuery(String qlString);
    
    public List<Object> getAllActiveUsers();
    

}
