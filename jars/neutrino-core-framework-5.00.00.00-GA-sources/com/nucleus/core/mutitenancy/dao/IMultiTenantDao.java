package com.nucleus.core.mutitenancy.dao;

import com.nucleus.entity.BaseTenant;

public interface IMultiTenantDao {
	BaseTenant getTenantById(Long id);
}
