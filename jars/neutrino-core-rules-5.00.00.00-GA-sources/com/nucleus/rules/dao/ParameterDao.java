package com.nucleus.rules.dao;

import java.util.List;
import java.util.Map;

import com.nucleus.persistence.BaseDao;
import com.nucleus.rules.model.Parameter;

public interface ParameterDao extends BaseDao<Parameter>{
	
	public List<String> findAllParameterTypes();
	
	public Map<String, List<? extends Parameter>> getTypeBasedMapOfParameters();

	public <T extends Parameter> List<T> findAllParameters(Class<T> entityClassName);
	
	public <T extends Parameter> T findApprovedParameterByName(String parameterName, Class<T> entityClass);

	public <T extends Parameter> List<T> getAllParametersFromDB(Class<T> parameterClass);

	public List<Integer> getApprovedRecordStatusList();

}
