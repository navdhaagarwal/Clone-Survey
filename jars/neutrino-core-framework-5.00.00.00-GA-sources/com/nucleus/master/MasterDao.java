package com.nucleus.master;

import java.util.Map;

import com.nucleus.entity.Entity;

public interface MasterDao {
	public <T extends Entity> T findMasterByCode(Class<T> entityClass, Map<String, Object> variablesMap) ;
}
