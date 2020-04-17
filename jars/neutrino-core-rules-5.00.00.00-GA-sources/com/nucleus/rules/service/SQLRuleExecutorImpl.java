package com.nucleus.rules.service;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.NoResultException;
import javax.persistence.NonUniqueResultException;
import javax.persistence.Query;
import javax.sql.DataSource;

import com.nucleus.rules.model.*;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.query.NativeQuery;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import com.nucleus.config.persisted.enity.Configuration;
import com.nucleus.config.persisted.service.ConfigurationService;
import com.nucleus.config.persisted.vo.ConfigurationVO;
import com.nucleus.core.formsConfiguration.validationcomponent.Tuple_2;
import com.nucleus.entity.SystemEntity;
import com.nucleus.logging.BaseLoggers;
import com.nucleus.persistence.EntityDao;
import com.nucleus.rules.exception.RuleException;
import com.nucleus.rules.simulation.service.SimulationParameterVO;

@Component("sQLRuleExecutor")
public class SQLRuleExecutorImpl implements SQLRuleExecutor {

	@Inject
	@Named("ruleService")
	private RuleService ruleService;

	@Inject
	@Named("dataSource")
	private DataSource ds;

	@Inject
	@Named("entityDao")
	private EntityDao entityDao;

	@Value(value = "${database.type}")
	private String databaseType;

	@Inject
	@Named("messageSource")
	private MessageSource messageSource;
	
	@Inject
	@Named("entityDao")
	private EntityDao entiyDao;

	@Inject
	@Named("configurationService")
	private ConfigurationService configurationService;

	@Inject
	@Named("parameterService")
	private ParameterService parameterService;

	private static List<Map> excludedTableMetaData = new ArrayList<>();
	private static List<String> excludedColumnForSuperSet = new ArrayList<>();
	public static final String TABLE_NAME = "Table Name : ";
	public static final String COLUMN_EXCLUDED = " Column Excluded : ";
	public static final String FALSE = "false";
	public static final String COLUMN_NAMES = "columnNames";
	public static final String QUERY_STRING = "SELECT new Map(ss.tableName AS TableName , ss.columnNames AS columnNames) FROM SqlTableMetaData ss ";

	@Override
	public char evaluateSQLRule(SQLRule rule, Map map, Boolean isStrictMode) {
		char result = evaluateSQLRule(rule, map);
		if (!isStrictMode && result == RuleConstants.RULE_RESULT_NORESULT) {
			result = RuleConstants.RULE_RESULT_FAIL;
		}
		return result;
	}

	@Override
	public char evaluateSQLRule(SQLRule rule, Map map) {
		char result = 5;
		try {
			if (CollectionUtils.isEmpty(rule.getParamMapping())) {
				throw new RuleException("SQL Rule without any where param is not allowed :" + rule.getCode());
			}
			String sqlInPlain = ruleService.decryptSQLRule(rule).getSqlQueryPlain();
			if (StringUtils.isEmpty(sqlInPlain)) {
				throw new RuleException("SQL Rule without sql Query :" + rule.getCode());
			}
			evaluateParameter(rule, map);
			// Sort by order of parameter in sql query
			List<SQLRuleParameterMapping> tempMapping = new ArrayList<>(rule.getParamMapping());
			Collections.sort(tempMapping, new Comparator<SQLRuleParameterMapping>() {

				@Override
				public int compare(SQLRuleParameterMapping arg0, SQLRuleParameterMapping arg1) {
					return arg0.getSeq().compareTo(arg1.getSeq());
				}
			});

			List<Object> parameterValue = new ArrayList<>();
			for (int i = 0; i < tempMapping.size(); i++) {
				SQLRuleParameterMapping paramMapping = tempMapping.get(i);
				Object paramValue = map.get(generateParamKey(rule,paramMapping.getParameter()));
				if (paramValue == null) {
					result = RuleConstants.RULE_RESULT_NORESULT;
					break;
				}else{
					parameterValue.add(paramValue);
				}
				sqlInPlain = sqlInPlain.replace(paramMapping.getPlaceHolderName(), " ? ");
			}
			if (result != RuleConstants.RULE_RESULT_NORESULT) {

				try{
					List<Object> sqlResult = getSqlResultList(parameterValue, sqlInPlain);
					if(org.apache.commons.collections4.CollectionUtils.isNotEmpty(sqlResult)){
						if( sqlResult.size()==1){
							result = getResult(rule, result, sqlResult);
						}else {
							boolean match = sqlResult.stream().filter(s -> s != null).allMatch(s -> s.equals(sqlResult.get(0)));
							if(match){
								result = getResult(rule, result, sqlResult);
							}else{
								throw new NonUniqueResultException();
							}
						}
					}else {
						result = RuleConstants.RULE_RESULT_NORESULT;
					}
				}catch(NoResultException e){
					result = RuleConstants.RULE_RESULT_NORESULT;
				}catch (NonUniqueResultException e) {
					throw new RuleException(
							"Error occured for Rule :: More than One Value/No Value returned" + rule.getName());
				}catch (IllegalStateException e) {
					throw new RuleException(
							"Error occured for Rule :: UPDATE or DELETE Tried" + rule.getName());
				}
			}
		} catch (Exception e) {
			BaseLoggers.exceptionLogger.error("Error occured for Rule ::" + rule.getName() + "::", e);
			throw new RuleException("Error occured for Rule ::" + rule.getName(), e);
		}
		return result;
	}

	@Override
	public Map<String,Object> getParameterValue(SQLParameter parameter, Map map){
		Map<String,Object> resultMap = new HashMap<>();
		try{
			/*if (CollectionUtils.isEmpty(parameter.getParamMapping())) {
				throw new RuleException("SQL parameter without any where mappings is not allowed :" + parameter.getCode());
			}*/
			List<Object> parameterValue = new ArrayList<>();
			String sqlInPlain = getSqlInPlain(parameter, map, parameterValue);

			try{
				List<Object> sqlResult = getSqlResultList(parameterValue, sqlInPlain);
				if(org.apache.commons.collections4.CollectionUtils.isNotEmpty(sqlResult)){
					if( sqlResult.size()==1){
						resultMap.put(RuleConstants.SQL_PARAM_RESULT_FOUND,sqlResult.get(0));
					}else {
						boolean match = sqlResult.stream().filter(s -> s != null).allMatch(s -> s.equals(sqlResult.get(0)));
						if(match){
							resultMap.put(RuleConstants.SQL_PARAM_RESULT_FOUND,sqlResult.get(0));
						}else{
							throw new NonUniqueResultException();
						}
					}
				}else {
					resultMap.put(RuleConstants.SQL_PARAM_RESULT_NOT_FOUND,"NO Result Found");
				}
			}catch(NoResultException e){
				resultMap.put(RuleConstants.SQL_PARAM_RESULT_NOT_FOUND,"NO Result Found");
			}catch (NonUniqueResultException e) {
				resultMap.put(RuleConstants.SQL_PARAM_RESULT_NOT_FOUND,"More Than One Value Found");
			}catch (IllegalStateException e) {
				throw new RuleException(
						"Error occured for SQL Parameter :: UPDATE or DELETE Tried" + parameter.getName());
			}

			return resultMap;
		}catch (Exception e) {
			BaseLoggers.exceptionLogger.error("Error occured for Parameter ::" + parameter.getName() + "::", e);
			throw new RuleException("Error occured for Parameter ::" + parameter.getName(), e);
		}

	}

	@Override
	public List<Object> getSqlResultList(List<Object> parameterValue, String sqlInPlain) {
		Query query = entiyDao.getEntityManager().createNativeQuery(sqlInPlain);
		query.unwrap(NativeQuery.class).addSynchronizedQuerySpace("");
		AtomicInteger atomicInteger = new AtomicInteger();
		atomicInteger.set(1);
		parameterValue.forEach(pv->{
			String value = (String) pv;
			query.setParameter(atomicInteger.getAndIncrement(),value);
		});
		List<Object> sqlResult = query.getResultList();
		return sqlResult;
	}

	
	@Override
	public String getSqlInPlain(SQLParameter parameter, Map map, List<Object> parameterValue) {
		String sqlInPlain = parameterService.decryptSQLParam(parameter).getSqlQueryPlain();

		if (StringUtils.isEmpty(sqlInPlain)) {
			throw new RuleException("SQL parameter without sql Query :" + parameter.getCode());
		}
		evaluateParameter(parameter,map);
		List<SQLParameterMapping> tempMapping = new ArrayList<>(parameter.getParamMapping());
		tempMapping.sort(Comparator.comparing((SQLParameterMapping::getSeq)));

		for (int i = 0; i < tempMapping.size(); i++) {
			SQLParameterMapping paramMapping = tempMapping.get(i);
			Object paramValue = map.get(generateParamKey(parameter,paramMapping.getParameter()));
			if (paramValue == null) {
				throw new RuleException("Inner Parameter value is null :" + paramMapping.getParameter().getCode());

			}else{
				parameterValue.add(paramValue);
			}
			sqlInPlain = sqlInPlain.replace(paramMapping.getPlaceHolderName(), " ? ");
		}
		return sqlInPlain;
	}

	private char getResult(SQLRule rule, char result, List<Object> sqlResult) {
		Object singleResult = sqlResult.get(0);
		if(singleResult!=null){
			if (singleResult.toString().equals("1")) {
				result = RuleConstants.RULE_RESULT_PASS;
			} else if (singleResult.toString().equals("0")) {
				result = RuleConstants.RULE_RESULT_FAIL;
			} else {
				throw new RuleException(
						"Error occured for Rule :: Return Value can be 0 or 1 only" + rule.getName());
			}
		}
		return result;
	}

	public void evaluateParameter(SQLRule rule, Map map) {
		rule.getParamMapping().forEach((paramMapping) -> {
			Parameter param = entityDao.find(Parameter.class, paramMapping.getParameter().getId());
			paramMapping.setParameter(param);
			Object paramvalue = ruleService.evaluateParameter(param, map);
			map.put(generateParamKey(rule,param), paramvalue);
		});
	}

	public void evaluateParameter(SQLParameter parameter, Map map){
		parameter.getParamMapping().forEach((paramMapping) -> {
			Parameter param = entityDao.find(Parameter.class, paramMapping.getParameter().getId());
			paramMapping.setParameter(param);
			Object paramvalue = ruleService.evaluateParameter(param, map);
			map.put(generateParamKey(parameter,param), paramvalue);
		});
	}

	private String generateParamKey(SQLRule rule, Parameter parameter) {
		return rule.getId()+RuleConstants.PARAMETER_NAME_ID+RulesConverterUtility.replaceSpace(parameter.getName()) + RuleConstants.PARAMETER_NAME_ID
				+ parameter.getId();
	}

	private String generateParamKey(SQLParameter parameter, Parameter innerParam){
		return parameter.getId()+RuleConstants.PARAMETER_NAME_ID+RulesConverterUtility.replaceSpace(innerParam.getName()) + RuleConstants.PARAMETER_NAME_ID
				+ innerParam.getId();
	}

	@Override
	public List<SimulationParameterVO> getParametersForSimulation(SQLRule rule,
			Map contextObject) {
		List<SQLRuleParameterMapping> paramMapping = rule.getParamMapping();
		if (!CollectionUtils.isEmpty(paramMapping)) {
			final List<SimulationParameterVO> result = new ArrayList<>();
			paramMapping.forEach((mapping) -> {
				SimulationParameterVO paramVO = new SimulationParameterVO();
				paramVO.setParameterName(mapping.getParameter().getName());
				paramVO.setParameterValue(contextObject.get(generateParamKey(rule,mapping.getParameter())) != null
						? contextObject.get(generateParamKey(rule,mapping.getParameter())).toString() : null);
				result.add(paramVO);
			});
			return result;
		}
		return null;
	}

	@Override
	public String validateSQLQuery(String plainSQL) {
		StringBuilder validationError = new StringBuilder();
		if (StringUtils.isNotEmpty(plainSQL)) {
			plainSQL = mininiySQL(plainSQL);
			// should start with Select
			if (!plainSQL.startsWith(SQLRuleUtility.SELECT.toLowerCase())) {
				appendValidationMessages(validationError,"Query should start with SELECT Keyword");
			}
			// should not end with ;
			if (plainSQL.endsWith(RuleConstants.MVEL_SEMICOLON)) {
				appendValidationMessages(validationError,"Query should not end with ;");
			}
			// excluded keywords check
			List<String> excludeKeywords = SQLRuleUtility.getExcludedkeywordsByDB(databaseType);
			// split by space and curly braces
			String[] words = plainSQL.split("\\s+|(|)");
			List<String> wordsInList = new ArrayList<>();
			if(words!=null){
				wordsInList = Arrays.asList(words);
			}
			if (excludeKeywords != null) {
				for (String excludeKeyword : excludeKeywords) {
					if(wordsInList.contains(excludeKeyword)){
						appendValidationMessages(validationError,excludeKeyword + " excluded keyword found in query");
					}
				}
			}
			// balance or absence of {}
			Deque<Tuple_2> bracesStack = new ArrayDeque<>();
			boolean singleBalancedBracesFound = false;
			for (int i = 0; i < plainSQL.length(); i++) {
				String ch = Character.toString(plainSQL.charAt(i));
				if (ch.equals(RuleConstants.LEFT_CURLY_BRACES)) {
					if(!bracesStack.isEmpty()
						&& bracesStack.pop().get_2().equals(RuleConstants.LEFT_CURLY_BRACES)){
						appendValidationMessages(validationError,"Invalid " + RuleConstants.LEFT_CURLY_BRACES + "Found at " + i);
					}else{
						bracesStack.push(new Tuple_2(String.valueOf(i), ch));
					}
				} else if (ch.equals(RuleConstants.RIGHT_CURLY_BRACES)) {
					if(bracesStack.isEmpty()){
						appendValidationMessages(validationError,"Invalid " + RuleConstants.RIGHT_CURLY_BRACES + "Found at " + i);
					}else{
						Tuple_2 currentTuple = bracesStack.pop();
						if(currentTuple.get_2().equals(RuleConstants.RIGHT_CURLY_BRACES)){
							appendValidationMessages(validationError,"Invalid " + RuleConstants.RIGHT_CURLY_BRACES + "Found at " + i);
						}else if(currentTuple.get_2().equals(RuleConstants.LEFT_CURLY_BRACES)){
							singleBalancedBracesFound = true;
						}
					}
				}
			}
			while(!bracesStack.isEmpty()){
				Tuple_2 remaingTuple = bracesStack.pop();
				appendValidationMessages(validationError,"Invalid "+remaingTuple.get_2()+" to position :"+remaingTuple.get_1());
			}
			if (!singleBalancedBracesFound) {
				appendValidationMessages(validationError,"No user input where clause found");
			}
			if(validationError.length() == 0){
				appendValidationMessages(validationError,dryRunSQLRule(plainSQL));
			}
		} else {
			appendValidationMessages(validationError,"Blank SQL");
		}
		return validationError.toString();
	}

	private void appendValidationMessages(StringBuilder validationError,String message) {
		if(StringUtils.isNotEmpty(message)){
			if(validationError.length() != 0){
				validationError.append("\t");
			}
			validationError.append("").append(message);
		}
	}

	public String mininiySQL(String sql) {
		if (StringUtils.isNoneEmpty(sql)) {
			// trim
			// convert to one line -> remove new line char
			// remove more that one space
			sql = sql.trim().toLowerCase().replaceAll("\n", "").replaceAll("\r", "").replaceAll("\\s{2,}", " ");
			// remove last semo colon
			return sql;
		}
		return null;
	}
	
	public String dryRunSQLRule(String sqlInPlain){
		String validationMessage = null;
		try{
			String[] whereClauses =StringUtils.substringsBetween(sqlInPlain ,RuleConstants.LEFT_CURLY_BRACES,RuleConstants.RIGHT_CURLY_BRACES);
			
			Object[] whereParam = new Object[whereClauses.length];
			
			// replacing the input with ?
			for (int i = 0; i < whereClauses.length; i++) {
				sqlInPlain = sqlInPlain.replace(RuleConstants.LEFT_CURLY_BRACES+whereClauses[i]+RuleConstants.RIGHT_CURLY_BRACES, " ? ");
			}
			List<Object> dataExtracted = new ArrayList<>();
			Long startInMili = System.currentTimeMillis();
			
			/*Query query = entiyDao.getEntityManager().createNativeQuery(sqlInPlain);
			for (int i = 1; i <= whereClauses.length; i++) {
				query.setParameter(i, null);
			}
			query.unwrap(NativeQuery.class).addSynchronizedQuerySpace("");
			try{
				Object proposalCount = query.getSingleResult();
			}catch(NoResultException e){
				// success case expected
			}catch (NonUniqueResultException e) {
				validationMessage = "more than one result found";
			}catch (IllegalStateException e) {
				validationMessage = "UPDATE or DELETE statement";
			}*/
			JdbcTemplate jdbcTemp = new JdbcTemplate(ds);
			jdbcTemp.query(sqlInPlain, whereParam, (rs, rowNum) -> {
				dataExtracted.add(rs.getObject(1));
				return null;
			});
			Long timeInSec = (System.currentTimeMillis() - startInMili)/1000;
			ConfigurationVO configurationVO = configurationService.getConfigurationPropertyFor(SystemEntity.getSystemEntityId(),Configuration.SQL_MAX_RUN_TIME);
			if(configurationVO!=null){
				Long sqlMaxRunTime = Long.valueOf(configurationVO.getText());
				if(timeInSec > sqlMaxRunTime){
					validationMessage = "Time For Execution :"+timeInSec;
				}
			}
		}catch(Exception e){
			BaseLoggers.webLogger.error("Exception in SQL Rule Validation :",e);
			validationMessage = " Invalid SQL Query : "+e.getLocalizedMessage();
		}
		return validationMessage;
	}

	public void generateParametesList(SQLRule rule,final List<Parameter> paremeters){
		rule.getParamMapping().forEach((paramMap)->{
			paremeters.add(paramMap.getParameter());
		});
	}

	private String getMessageAgainstKey(String key, Locale locale) {
		String message = "";
		if (null != key && !key.equals("")) {
			message = messageSource.getMessage(key, null, key, locale);
		}
		return message;
	}
}
