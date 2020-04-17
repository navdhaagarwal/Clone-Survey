package com.nucleus.core.role.service;

import java.util.List;

import com.nucleus.authority.Authority;
import com.nucleus.core.role.entity.Role;

public interface RoleService {
/**
 * Get the list of Roles who have the authority
 */
	public List<Role> getRolesByAuthority(long authorityId);
	
	public Authority getAuthorityByCode(String authorityCode);
	
	public List<Object[]> getAllApprovedAndActiveRoles();
}
