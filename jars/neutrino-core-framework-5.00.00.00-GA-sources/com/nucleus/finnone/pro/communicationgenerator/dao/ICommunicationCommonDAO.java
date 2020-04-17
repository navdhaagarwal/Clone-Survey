package com.nucleus.finnone.pro.communicationgenerator.dao;

import java.util.Map;

import com.nucleus.entity.Entity;
import com.nucleus.persistence.EntityDao;


public interface ICommunicationCommonDAO extends EntityDao {

	<T extends Entity> T findMasterByCode(Class<T> entityClass, Map<String, Object> variablesMap);
}
