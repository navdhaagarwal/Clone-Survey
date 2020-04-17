package com.nucleus.rules.service;

import java.util.List;
import java.util.Map;

import com.nucleus.rules.model.*;
import com.nucleus.rules.simulation.service.SimulationParameterVO;
import com.nucleus.service.BaseService;

public interface SQLRuleExecutor extends BaseService{

	public char evaluateSQLRule(SQLRule rule,Map map,Boolean isStrictMode);
	
	public char evaluateSQLRule(SQLRule rule,Map map);
	
	public List<SimulationParameterVO> getParametersForSimulation(SQLRule rule,Map contextObject);
	
	public String validateSQLQuery(String plainSQL);
	
	public void evaluateParameter(SQLRule rule, Map map);
	
	public void generateParametesList(SQLRule rule,final List<Parameter> paremeters);

	public Map<String,Object> getParameterValue(SQLParameter parameter, Map map);

	public List<Object> getSqlResultList(List<Object> parameterValue, String sqlInPlain);

	public String getSqlInPlain(SQLParameter parameter, Map map, List<Object> parameterValue);

}
