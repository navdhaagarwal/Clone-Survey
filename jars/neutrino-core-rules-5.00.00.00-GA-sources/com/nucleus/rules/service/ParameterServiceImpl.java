package com.nucleus.rules.service;

import static com.nucleus.finnone.pro.cache.constants.FWCacheConstants.KEY_DELIMITER;
import static com.nucleus.rules.service.RulesConverterUtility.commaDelimitesString;

import java.util.*;

import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.Query;

import com.nucleus.cas.parentChildDeletionHandling.*;
import com.nucleus.core.genericparameter.service.GenericParameterService;
import com.nucleus.core.notification.*;
import com.nucleus.dao.query.*;
import com.nucleus.entity.ApprovalStatus;
import com.nucleus.entity.EntityId;
import com.nucleus.event.*;
import com.nucleus.master.BaseMasterService;
import com.nucleus.persistence.DaoUtils;
import com.nucleus.rules.model.*;
import com.nucleus.user.*;
import org.apache.commons.collections.*;
import org.apache.commons.lang3.StringUtils;

import com.nucleus.core.exceptions.InvalidDataException;
import com.nucleus.finnone.pro.cache.common.NeutrinoCachePopulator;
import com.nucleus.finnone.pro.cache.common.NeutrinoCachePopulator.Action;
import com.nucleus.finnone.pro.cache.constants.FWCacheConstants;
import com.nucleus.finnone.pro.cache.entity.ImpactedCache;
import com.nucleus.logging.BaseLoggers;
import com.nucleus.rules.dao.ParameterDao;
import com.nucleus.rules.simulation.service.RuleSimulationService;
import org.apache.ibatis.jdbc.SQL;
import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.*;
import org.springframework.context.*;
import org.xlsx4j.sml.Col;

/**
 * 
 * 
 * Implementing the Service for the Parameter entity
 * 
 * @author prateek.chachra
 *
 */

@Named(value = "parameterService")
public class ParameterServiceImpl extends BaseRuleServiceImpl implements ParameterService {

	@Inject
	@Named("parameterDao")
	private ParameterDao parameterDao;
	
	@Inject
	@Named("oGParameterByOGCachePopulator")
	private NeutrinoCachePopulator oGParameterByOGCachePopulator;
	
	@Inject
	@Named("parameterByNameAndTypeCachePopulator")
	private NeutrinoCachePopulator parameterByNameAndTypeCachePopulator;
	
	@Inject
	@Named("parameterByIdCachePopulator")
	private NeutrinoCachePopulator parameterByIdCachePopulator;
	
	@Inject
	@Named("scriptParameterEvalByParamIdCachePopulator")
	private NeutrinoCachePopulator scriptParameterEvalByParamIdCachePopulator;
	
	@Inject
	@Named("decryptedParameterScriptIdCachePopulator")
	private NeutrinoCachePopulator decryptedParameterScriptIdCachePopulator;
	
	
	@Inject
	@Named("ruleSimulationService")
	private RuleSimulationService ruleSimulationService;

	@Inject
	@Named("genericParameterService")
	protected GenericParameterService genericParameterService;

	@Inject
	@Named("baseMasterService")
	private BaseMasterService baseMasterService;

	@Inject
	@Named("ruleService")
	private RuleService             ruleService;

	@Inject
	@Named("messageSource")
	private MessageSource messageSource;

	public static final String OLD_PARAMETER_OBJECT = "OLD_PARAMETER_OBJECT";
	
	public static final String PARAMETER_OBJECT = "PARAMETER_OBJECT";
	
	public static final String PARAMETER_OBJECT_ID = "PARAMETER_OBJECT_ID";

	private static final String PARAMETER_CODE_ALREADY_EXISTS_IN_RULE_WARNING = "label.paramter.ruleApprovalWarning";



	@Override
	public <T extends Parameter> T getParameterByNameAndType(String parameterName, Class<T> entityClass) {
		if (StringUtils.isBlank(parameterName) || entityClass == null) {
			throw new InvalidDataException("Entity class or name cannot be null");
		}
		Long id = (Long) parameterByNameAndTypeCachePopulator
				.get(new StringBuilder(entityClass.getName()).append(KEY_DELIMITER).append(parameterName).toString());
		if (id != null) {
			return entityDao.find(entityClass, id);
		}
		return null;
	}	
	
	
	
	
		/**
	 * This method is used to update parameter Cache.
	 *  
	 * @param parameter
	 * @param parameterId
	 * 
	 */

	@Override
	public void updateParameterCache(Map<String,Object> dataMap) {
		Parameter oldParameter = (Parameter) parameterByIdCachePopulator.get(dataMap.get(PARAMETER_OBJECT_ID));
		dataMap.put(OLD_PARAMETER_OBJECT, oldParameter);
		createOrUpdateParameterCache(dataMap);
	}

	@Override
	public boolean checkApprovalStatus(Parameter parameter) {
		return (parameter.getMasterLifeCycleData().getApprovalStatus() != null && parameter.isApproved()
				&& parameter.getName() != null);
	}

		
	@Override
	public <T extends Parameter> Long getParameterIdForParameterCacheByName(List<T> listOfAllParameters) {
		if (listOfAllParameters != null) {
			for (Parameter parameter : listOfAllParameters) {
				try {
					if (parameter.getName() != null) {
						return parameter.getId();
					}
				} catch (Exception e) {
					BaseLoggers.exceptionLogger
							.error(" Error occured in fallback of 'Parameter Cache By Name' for Parameter Id:: "
									+ parameter.getId());
				}

			}
		}

		return null;
	}
	
	@SuppressWarnings("unchecked")
	private void updateParameterByTypeAndNameCache(Class<? extends Parameter> clazz, Parameter parameter, Map<String,Long> parameterByTypeAndNameMapForAdd,
			boolean isParamNameChanged, Parameter oldParameter, Set<String> parameterByTypeAndNameMapForRemove) {
		if (isParamNameChanged) {
			String oldKey = new StringBuilder(clazz.getName()).append(KEY_DELIMITER).append(oldParameter.getName())
					.toString();
			parameterByTypeAndNameMapForRemove.add(oldKey);
		}
		parameterByTypeAndNameMapForAdd.put(
				new StringBuilder(clazz.getName()).append(KEY_DELIMITER).append(parameter.getName()).toString(),
				parameter.getId());
		if (!clazz.equals(Parameter.class)) {
			updateParameterByTypeAndNameCache((Class<? extends Parameter>) clazz.getSuperclass(), parameter,
					parameterByTypeAndNameMapForAdd, isParamNameChanged, oldParameter, parameterByTypeAndNameMapForRemove);
		}
	}
	
	@Override
	public Map<Long, String> getDecryptedParamScriptIdMap(List<ScriptParameter> parameterList) {
		Map<Long, String> decryptedParamScriptIdMap = new HashMap<>();
		for (ScriptParameter parameter : parameterList) {
			try {
				if (parameter.getName() != null && parameter.getScriptCode() != null) {
					decryptedParamScriptIdMap.put(parameter.getId(), decryptString(parameter.getScriptCode()));
				}
			} catch (Exception e) {
				BaseLoggers.exceptionLogger.debug("Error occured for DECRYPTED_PARAM_SCRIPT_ID cache Build/Update ::"
						+ parameter.getName() + " for script param id " + parameter.getId() + " ::  " + e);
			}
		}
		return decryptedParamScriptIdMap;
	}
	
	@SuppressWarnings("unchecked")
	private void createOrUpdateParameterCache(Map<String,Object> dataMap) {

		Parameter parameter = (Parameter) dataMap.get(PARAMETER_OBJECT);
		Parameter oldParameter = (Parameter) dataMap.get(OLD_PARAMETER_OBJECT);
		if (parameter.getName() != null) {

			boolean isParamNameChanged = (oldParameter != null && oldParameter.getName() != null
					&& !oldParameter.getName().equalsIgnoreCase(parameter.getName())) ? true : false;
			
			Map<String,Long> map = new HashMap<>();
			Set<String> set = new HashSet<>();
			updateParameterByTypeAndNameCache(parameter.getClass(), parameter,
					map, isParamNameChanged,
					oldParameter, set);
			parameterByNameAndTypeCachePopulator.update(Action.DELETE, set);
			parameterByNameAndTypeCachePopulator.update((Map<String, ImpactedCache>) dataMap.get(FWCacheConstants.IMPACTED_CACHE_MAP),Action.UPDATE,map);
			

			if (parameter instanceof ScriptParameter) {
				ScriptParameter scriptParam = (ScriptParameter) parameter;
				removeFromCacheIfApplicable(isParamNameChanged, decryptedParameterScriptIdCachePopulator, scriptParam.getId());
				decryptedParameterScriptIdCachePopulator.update((Map<String, ImpactedCache>) dataMap.get(FWCacheConstants.IMPACTED_CACHE_MAP),Action.INSERT,getDecryptedParamScriptIdMap(Arrays.asList(scriptParam)));
			}

			if (parameter instanceof ObjectGraphParameter) {
				updateObjectParametersCacheByOG((ObjectGraphParameter)parameter,(ObjectGraphParameter)oldParameter,oGParameterByOGCachePopulator,dataMap);
			}

			else if (parameter instanceof ScriptParameter) {
				scriptParameterEvalByParamIdCachePopulator.update((Map<String, ImpactedCache>) dataMap.get(FWCacheConstants.IMPACTED_CACHE_MAP),Action.UPDATE,parameter);
			}

			
			removeFromCacheIfApplicable(true, parameterByIdCachePopulator, parameter.getId());
			Map<Long,Parameter> idParameterMap = new HashMap<>();
			idParameterMap.put(parameter.getId(), parameter);
			parameterByIdCachePopulator.update((Map<String, ImpactedCache>) dataMap.get(FWCacheConstants.IMPACTED_CACHE_MAP),Action.INSERT,idParameterMap);
			

		}
	}

	@SuppressWarnings("unchecked")
	private void updateObjectParametersCacheByOG(ObjectGraphParameter parameter, ObjectGraphParameter oldParameter,
			NeutrinoCachePopulator objectGraphParametersByOG,Map<String,Object> dataMap) {
		Map<String,ObjectGraphParameter> map = new HashMap<>();
		if (parameter instanceof PlaceHolderParameter) {
			if ((PlaceHolderParameter) oldParameter != null
					&& ((PlaceHolderParameter) oldParameter).getContextName() != null) {
				removeFromCacheIfApplicable(true, objectGraphParametersByOG,
						((PlaceHolderParameter) oldParameter).getContextName());
			}
			if (((PlaceHolderParameter) parameter).getContextName() != null) {
				map.put(((PlaceHolderParameter) parameter).getContextName(), parameter);
			}
		} else {
			if (oldParameter != null && oldParameter.getObjectGraph() != null) {
				removeFromCacheIfApplicable(true, objectGraphParametersByOG, oldParameter.getObjectGraph());
			}
			if (parameter.getObjectGraph() != null) {
				map.put(parameter.getObjectGraph(), parameter);
			}
		}
		objectGraphParametersByOG.update((Map<String, ImpactedCache>) dataMap.get(FWCacheConstants.IMPACTED_CACHE_MAP),Action.INSERT,map);
	}
	

	private void removeFromCacheIfApplicable(boolean isParamNameChanged, NeutrinoCachePopulator chacheObject,
			String cacheKey) {
		if (isParamNameChanged) {
			chacheObject.update(Action.DELETE,cacheKey);
		}
	}
	
	private void removeFromCacheIfApplicable(boolean isParamNameChanged, NeutrinoCachePopulator chacheObject,
			Long cacheKey) {
		if (isParamNameChanged) {
			chacheObject.update(Action.DELETE,cacheKey);
		}
	}

	
	
	@Override
	public String getNameByObjectGraph(String objectGraph) {
		ObjectGraphParameter parameter = entityDao.find(ObjectGraphParameter.class,
				(Long) oGParameterByOGCachePopulator.get(objectGraph));
		if (parameter.getName() != null) {
			return parameter.getName();
		} else if (parameter.getDisplayName() != null) {
			return parameter.getDisplayName();
		}
		return null;
	}


	/*	
	@Override
	public List<Parameter> getAllParametersByDType(Class<? extends Parameter> DType){

		List<Parameter> listParams = new ArrayList<Parameter>();
		CacheManager parameterCacheManager = CacheManager.getInstance(FW_REGION);
		Map<String, Parameter> parameterByName = (Map<String, Parameter>) parameterCacheManager.get(PARAMETER_CACHE_NAME);
		Parameter param;
		for(Map.Entry<String, Parameter> parameter : parameterByName.entrySet()){
			if((param = parameter.getValue()).getClass().equals(DType)){
				listParams.add(param);
			}
		}
			return listParams;
	}*/
	
	
	
	
	@Override
	public Parameter getApprovedParameterByName(String parameterName) {
		return getParameterByNameAndType(parameterName, Parameter.class);
		

	}


	@Override
	public ScriptParameterEvaluator getScriptParameterEvaluatorById(
			Long scriptParamId) {
		
		
		if ((scriptParameterEvalByParamIdCachePopulator != null) && (scriptParamId != null)) {
			return (ScriptParameterEvaluator) scriptParameterEvalByParamIdCachePopulator.get(scriptParamId);
		}
		return null;
	}

	@Override
	public ScriptParameterEvaluator generateScriptParameterEvaluator(ScriptParameter scriptParameter) {
		try {
			StringBuilder dynamicClassSyntax = new StringBuilder();
			dynamicClassSyntax.append(getClassDeclarationWithRandomClassName(scriptParameter))
				.append(ScriptParameterEvaluator.METHOD_DEFINITION_STRING)
				.append(scriptParameter.getScriptCodeValue())
				.append(ScriptParameterEvaluator.DOUBLE_CLOSING_CURLY_BRACES);
			Class<?> clazz = GROOVY_CLASS_LOADER_PARAMETER.parseClass(dynamicClassSyntax.toString());
			return (ScriptParameterEvaluator) clazz.newInstance();
		} catch (Exception e) {
			BaseLoggers.exceptionLogger
			.error("Error occured in generating ScriptParameter for scriptCode id:: "
					+ scriptParameter.getId()
					+ " and ScriptParameter Name  "
					+ scriptParameter.getName() + "::" + e);
		}
		return null;
	}

	@Override
	public String getClassDeclarationWithRandomClassName(ScriptParameter scriptParamenter) {
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append(ScriptParameterEvaluator.CLASS_DECLARATION);
		//Assumes that <code>parameterCode</code> will never be null. Which is fine.
		stringBuilder.append(scriptParamenter.getCode().replaceAll("[^a-zA-Z0-9]", ""));
		if (scriptParamenter.getId() == null) {
			stringBuilder.append(getRandomStringOfNumbers());
		} else {
			stringBuilder.append(scriptParamenter.getId());
		}
		stringBuilder.append(ScriptParameterEvaluator.IMPLEMENTS_STRING);
		stringBuilder.append(ScriptParameterEvaluator.OPENING_CURLY_BRACE);	
		return stringBuilder.toString();
	}

	/**
	 * Reducing the possibility of name conflict in generated class. 
	 * @return
	 */
	private String getRandomStringOfNumbers() {
		StringBuilder sb = new StringBuilder();
		Random random = new Random();
		sb.append(random.nextInt(100000));
		sb.append(random.nextInt(100000));
		sb.append(random.nextInt(100000));
		return sb.toString();
	}

	
	@Override
	public Parameter getParametersFromCacheById(Long id) {
		Parameter parameter = (Parameter) parameterByIdCachePopulator.get(id);
		if (parameter instanceof ScriptParameter) {
			ScriptParameter scriptParameter = (ScriptParameter) parameter;
			scriptParameter.setScriptCodeValue(
					(String) decryptedParameterScriptIdCachePopulator.get(id));
		}
		return parameter;
	}

	@Override
	public SQLParameter encryptSQLParam(SQLParameter sqlRule) {
		if (null != sqlRule) {
			sqlRule.setSqlQuery(encryptString(sqlRule.getSqlQueryPlain()));
		}
		return sqlRule;
	}

	@Override
	public SQLParameter decryptSQLParam(SQLParameter scriptRule) {
		if (null != scriptRule) {
			scriptRule.setSqlQueryPlain(decryptString(scriptRule.getSqlQuery()));
		}
		return scriptRule;
	}

	@Override
	public List<ParameterSimilarVO> findRecordForParameter(Parameter parameter,String parameterExp,String placeHolderName,String parameterValue,boolean isAdvanceRuleView){

		if(parameter instanceof ConstantParameter){
			Integer dataType=parameter.getDataType();
			String literal =((ConstantParameter) parameter).getLiteral();
			NamedQueryExecutor<ConstantParameter> executor = new NamedQueryExecutor<ConstantParameter>("ParameterMaster.findConstantParameter")
					.addParameter("dataType", dataType)
					.addParameter("literal", literal)
					.addParameter("approvalStatus", ApprovalStatus.APPROVED_RECORD_STATUS_LIST);
			List<ConstantParameter> parameters = entityDao.executeQuery(executor);
			if(CollectionUtils.isNotEmpty(parameters)){
				List<ParameterSimilarVO> parameterSimilarVOList=new ArrayList<>();
				for(ConstantParameter constantParameter:parameters) {
					if (!constantParameter.getCode().equals(parameter.getCode())) {
						ParameterSimilarVO parameterSimilarVO = new ParameterSimilarVO();
						parameterSimilarVO.setParamType(ParameterType.PARAMETER_TYPE_CONSTANT);
						parameterSimilarVO.setParameterCode(constantParameter.getCode());
						parameterSimilarVO.setLiteral(constantParameter.getLiteral());
						ParameterDataType paramCodeValue = genericParameterService.findByCode(String.valueOf(constantParameter.getDataType()), ParameterDataType.class);
						if (paramCodeValue != null) {
							parameterSimilarVO.setDataType(paramCodeValue.getName());
						}
						parameterSimilarVOList.add(parameterSimilarVO);
					}
				}
				return parameterSimilarVOList;
			}
		}
		if(parameter instanceof ObjectGraphParameter && parameter.getParamType()==ParameterType.PARAMETER_TYPE_OBJECT_GRAPH){
			String objectGraph=((ObjectGraphParameter) parameter).getObjectGraph();
			NamedQueryExecutor<ObjectGraphParameter> executor = new NamedQueryExecutor<ObjectGraphParameter>("ParameterMaster.findObjectGraphParameter")
					.addParameter("objectGraph", objectGraph)
					.addParameter("approvalStatus", ApprovalStatus.APPROVED_RECORD_STATUS_LIST);
			List<ObjectGraphParameter> parameters = entityDao.executeQuery(executor);
			if(CollectionUtils.isNotEmpty(parameters)){
				List<ParameterSimilarVO> parameterSimilarVOList =new ArrayList<>();
				for(ObjectGraphParameter objectGraphParameter:parameters){
					if(!objectGraphParameter.getCode().equals(parameter.getCode())) {
						ParameterSimilarVO parameterSimilarVO = new ParameterSimilarVO();
						parameterSimilarVO.setParamType(ParameterType.PARAMETER_TYPE_OBJECT_GRAPH);
						parameterSimilarVO.setParameterCode(objectGraphParameter.getCode());
						Map<String, Object> variable1 = new HashMap<String, Object>();
						variable1.put("objectGraph", objectGraphParameter.getObjectGraph());
						ObjectGraphTypes objectGraphTypes = null;
						List<ObjectGraphTypes> objectGraphTypesList = baseMasterService.getApprovedAndActiveEntityForAllPropertyNameValueMap(ObjectGraphTypes.class, variable1);
						if (CollectionUtils.isNotEmpty(objectGraphTypesList)) {
							objectGraphTypes = objectGraphTypesList.get(0);
							parameterSimilarVO.setObjectGraph(objectGraphTypes.getDisplayName());
						}
						parameterSimilarVOList.add(parameterSimilarVO);
					}
				}
				return parameterSimilarVOList;
			}
		}
		if(parameter instanceof CompoundParameter){
			NamedQueryExecutor<CompoundParameter> executor = new NamedQueryExecutor<CompoundParameter>("ParameterMaster.findCompoundParameter")
					.addParameter("parameterExpression", parameterExp)
					.addParameter("approvalStatus", ApprovalStatus.APPROVED_RECORD_STATUS_LIST);
			List<CompoundParameter> parameters = entityDao.executeQuery(executor);
			if(CollectionUtils.isNotEmpty(parameters)){

				List<ParameterSimilarVO> parameterSimilarVOList =new ArrayList<>();
				for(CompoundParameter compoundParameter:parameters){
					if(!compoundParameter.getCode().equals(parameter.getCode())) {
						ParameterSimilarVO parameterSimilarVO = new ParameterSimilarVO();
						parameterSimilarVO.setParamType(ParameterType.PARAMETER_TYPE_COMPOUND);
						parameterSimilarVO.setParameterCode(compoundParameter.getCode());
						String compoundExpression = buildParameterExpression(compoundParameter
								.getParameterExpression());
						parameterSimilarVO.setParameterExpression(compoundExpression);
						parameterSimilarVOList.add(parameterSimilarVO);
					}
				}
				return parameterSimilarVOList;
			}
		}
		if(parameter instanceof PlaceHolderParameter && parameter.getParamType()==ParameterType.PARAMETER_TYPE_PLACEHOLDER){
			if (placeHolderName.equalsIgnoreCase("obj")) {
				String objectGraph=((PlaceHolderParameter) parameter).getObjectGraph();
				NamedQueryExecutor<PlaceHolderParameter> executor = new NamedQueryExecutor<PlaceHolderParameter>("ParameterMaster.findPlaceHolderObjectGraphParameter")
						.addParameter("objectGraph", objectGraph)
						.addParameter("approvalStatus", ApprovalStatus.APPROVED_RECORD_STATUS_LIST);
				List<PlaceHolderParameter> parameters = entityDao.executeQuery(executor);
				if(CollectionUtils.isNotEmpty(parameters)){

					List<ParameterSimilarVO> parameterSimilarVOList =new ArrayList<>();
					for(PlaceHolderParameter placeHolderParameter:parameters){
						if(!placeHolderParameter.getCode().equals(parameter.getCode())) {
							ParameterSimilarVO parameterSimilarVO = new ParameterSimilarVO();
							parameterSimilarVO.setParamType(ParameterType.PARAMETER_TYPE_PLACEHOLDER);
							parameterSimilarVO.setParameterCode(placeHolderParameter.getCode());
							Map<String, Object> variable1 = new HashMap<String, Object>();
							variable1.put("objectGraph", placeHolderParameter.getObjectGraph());
							ObjectGraphTypes objectGraphTypes = null;
							List<ObjectGraphTypes> objectGraphTypesList = baseMasterService.getApprovedAndActiveEntityForAllPropertyNameValueMap(ObjectGraphTypes.class, variable1);
							if (CollectionUtils.isNotEmpty(objectGraphTypesList)) {
								objectGraphTypes = objectGraphTypesList.get(0);
								parameterSimilarVO.setObjectGraph(objectGraphTypes.getDisplayName());
							}
							parameterSimilarVOList.add(parameterSimilarVO);
						}
					}
					return parameterSimilarVOList;
				}

			} else if (placeHolderName.equalsIgnoreCase("context")) {
				((PlaceHolderParameter) parameter).setObjectGraph("");
				String contextName=((PlaceHolderParameter) parameter).getContextName();
				NamedQueryExecutor<PlaceHolderParameter> executor = new NamedQueryExecutor<PlaceHolderParameter>("ParameterMaster.findPlaceHolderParameter")
						.addParameter("contextName", contextName)
						.addParameter("approvalStatus", ApprovalStatus.APPROVED_RECORD_STATUS_LIST);
				List<PlaceHolderParameter> parameters = entityDao.executeQuery(executor);
				if(CollectionUtils.isNotEmpty(parameters)){

					List<ParameterSimilarVO> parameterSimilarVOList =new ArrayList<>();
					for(PlaceHolderParameter placeHolderParameter:parameters){
						if(!placeHolderParameter.getCode().equals(parameter.getCode())) {
							ParameterSimilarVO parameterSimilarVO = new ParameterSimilarVO();
							parameterSimilarVO.setParamType(ParameterType.PARAMETER_TYPE_PLACEHOLDER);
							parameterSimilarVO.setParameterCode(placeHolderParameter.getCode());
							parameterSimilarVO.setContextName(placeHolderParameter.getContextName());
							parameterSimilarVOList.add(parameterSimilarVO);
						}
					}
					return parameterSimilarVOList;
				}
			}
		}
		if(parameter instanceof QueryParameter){

			List<QueryParameterAttribute> queryParameterAttributeList=((QueryParameter) parameter).getTempQueryParamAttribute();
			List<String> queryParamName = new ArrayList<>();
          if(CollectionUtils.isNotEmpty(queryParameterAttributeList)){
			Collections.sort(queryParameterAttributeList, new Comparator<QueryParameterAttribute>() {
				@Override
				public int compare(QueryParameterAttribute o1, QueryParameterAttribute o2) {
					return o1.getQueryParameterName().compareTo(o2.getQueryParameterName());
				}
			});
			for(QueryParameterAttribute queryParameterAttribute : queryParameterAttributeList) {
				queryParamName.add(queryParameterAttribute.getQueryParameterName());
			}
			NamedQueryExecutor<QueryParameter> executor = new NamedQueryExecutor<QueryParameter>("ParameterMaster.findQueryParameter")
					.addParameter("query", ((QueryParameter) parameter).getQuery())
					.addParameter("approvalStatus", ApprovalStatus.APPROVED_RECORD_STATUS_LIST);
			List<QueryParameter> parameters = entityDao.executeQuery(executor);
			List<ParameterSimilarVO>parameterSimilarVOList =new ArrayList<>();
			if(CollectionUtils.isNotEmpty(parameters)){
				for(QueryParameter queryParameter:parameters){
					if(!queryParameter.getCode().equals(parameter.getCode())) {
						List<QueryParameterAttribute> dbQueryParamList = queryParameter.getQueryParameterAttributes();
						List<String> dbQueryParamName = new ArrayList<>();
						if (CollectionUtils.isNotEmpty(dbQueryParamList)) {

							Collections.sort(dbQueryParamList, new Comparator<QueryParameterAttribute>() {
								@Override
								public int compare(QueryParameterAttribute o1, QueryParameterAttribute o2) {
									return o1.getQueryParameterName().compareTo(o2.getQueryParameterName());
								}
							});
							for (QueryParameterAttribute queryParameterAttribute : dbQueryParamList) {
								dbQueryParamName.add(queryParameterAttribute.getQueryParameterName());
							}
							boolean result = dbQueryParamName.equals(queryParamName);
							if (result) {
								ParameterSimilarVO parameterSimilarVO = new ParameterSimilarVO();
								parameterSimilarVO.setParamType(ParameterType.PARAMETER_TYPE_QUERY);
								parameterSimilarVO.setParameterCode(queryParameter.getCode());
								parameterSimilarVO.setQuery(queryParameter.getQuery());
								parameterSimilarVO.setQueryParameterNames(StringUtils.join(queryParamName.toArray(), " , "));

								parameterSimilarVOList.add(parameterSimilarVO);
							}
						}
					}
				}
				return parameterSimilarVOList;
			}
		}
		}
		if(parameter instanceof ReferenceParameter){

			EntityId referenceEntityId= EntityId.fromUri(parameterValue);
			String referenceURI = referenceEntityId.getUri();
			NamedQueryExecutor<ReferenceParameter> executor = new NamedQueryExecutor<ReferenceParameter>("ParameterMaster.findReferenceParameter")
					.addParameter("referenceURI", referenceURI)
					.addParameter("approvalStatus", ApprovalStatus.APPROVED_RECORD_STATUS_LIST);
			List<ReferenceParameter> parameters = entityDao.executeQuery(executor);
			if(CollectionUtils.isNotEmpty(parameters)){

				List<ParameterSimilarVO> parameterSimilarVOList =new ArrayList<>();
				for(ReferenceParameter referenceParameter:parameters){
					if(!referenceParameter.getCode().equals(parameter.getCode())) {
						ParameterSimilarVO parameterSimilarVO = new ParameterSimilarVO();
						parameterSimilarVO.setParamType(ParameterType.PARAMETER_TYPE_REFERENCE);
						parameterSimilarVO.setParameterCode(referenceParameter.getCode());
						EntityId entityId = referenceParameter.getReferenceEntityId();
						EntityType entityType = ruleService.getEntityTypeData(entityId.getUri().substring(0, entityId.getUri().indexOf(":")));

						String fields = entityType.getFields();
						String entityName = referenceParameter.getReferenceEntityId().getEntityClass().getSimpleName();
						String fieldName = fields.substring(0, fields.indexOf(","));
						Long id = referenceParameter.getReferenceEntityId().getLocalId();
						String qlString = "select " + fieldName + " FROM " + entityName + " WHERE id=" + id;
						Query qry = entityDao.getEntityManager().createQuery(qlString);
						Object o = DaoUtils.executeQuery(entityDao.getEntityManager(), qry).get(0);

						parameterSimilarVO.setReferenceEntityName(entityType.getDisplayEntityName());
						parameterSimilarVO.setReferenceValue(o.toString());
						parameterSimilarVOList.add(parameterSimilarVO);
					}
				}
				return parameterSimilarVOList;
			}
		}
		if(parameter instanceof SQLParameter){

			List<SQLParameterMapping> sqlRuleParameterMappingList=((SQLParameter) parameter).getParamMapping();
			List<String> parameterList=new ArrayList<>();
			for(SQLParameterMapping sqlRuleParameterMapping:sqlRuleParameterMappingList){
				Long id=sqlRuleParameterMapping.getParameter().getId();
				Parameter parameter1=baseMasterService.findById(Parameter.class,id);
				if(parameter1!=null){
					parameterList.add(parameter1.getCode());
				}
			}
			Collections.sort(parameterList);

			NamedQueryExecutor<SQLParameter> executor = new NamedQueryExecutor<SQLParameter>("ParameterMaster.findSQLParameter")
					.addParameter("approvalStatus", ApprovalStatus.APPROVED_RECORD_STATUS_LIST);
			List<SQLParameter> parameters = entityDao.executeQuery(executor);
			if(CollectionUtils.isNotEmpty(parameters)){
				List<ParameterSimilarVO> parameterSimilarVOList=new ArrayList<>();
				for(SQLParameter sqlParameter:parameters) {
					if (!sqlParameter.getCode().equals(parameter.getCode())) {
						SQLParameter sq = decryptSQLParam(sqlParameter);
						if (sq.getSqlQueryPlain().equalsIgnoreCase(((SQLParameter) parameter).getSqlQueryPlain())) {

							List<SQLParameterMapping> dbSqlParameterMappingList = new ArrayList<>();
							List<String> dbParameterList = new ArrayList<>();
							dbSqlParameterMappingList = sqlParameter.getParamMapping();
							if (CollectionUtils.isNotEmpty(dbSqlParameterMappingList)) {
								for (SQLParameterMapping dbParam : dbSqlParameterMappingList) {
									dbParameterList.add(dbParam.getParameter().getCode());
								}
								Collections.sort(dbParameterList);
								boolean result = parameterList.equals(dbParameterList);
								if (result) {
									ParameterSimilarVO parameterSimilarVO = new ParameterSimilarVO();
									parameterSimilarVO.setParameterCode(sqlParameter.getCode());
									parameterSimilarVO.setParamType(ParameterType.PARAMETER_TYPE_SQL);
									parameterSimilarVO.setQuery(sq.getSqlQueryPlain());
									parameterSimilarVO.setParameterNames(StringUtils.join(parameterList.toArray(), " , "));
									parameterSimilarVOList.add(parameterSimilarVO);
								}
							 }
						   }
					}
				}
				return parameterSimilarVOList;
			}
		}
		return null;
	}

	private String buildParameterExpression(String parameterExpression) {
		StringBuilder expression = new StringBuilder();
		if (StringUtils.isNotBlank(parameterExpression)) {

			String[] tokens = parameterExpression.split(" ");
			if (tokens != null && tokens.length > 0) {
				for (String token : tokens) {
					token = token.trim();
					// if token is bracket and operator
					if (token.equals(RuleConstants.LEFT_PAREN) || token.equals(RuleConstants.RIGHT_PAREN)
							|| commaDelimitesString(ExpressionValidationConstants.ARITHMETIC_OPS).indexOf(token) != -1) {
						expression.append(token).append(" ");
					} else {
						Parameter parameter = entityDao.find(Parameter.class, Long.parseLong(token));
						if (parameter != null) {
							expression.append(parameter.getName()).append(" ");
						}
					}
				}
			}
			if (expression.length() > 0) {
				return expression.toString();
			}
		}
		return "";
	}


	public Set<Rule> findRulesNeedToBeApprovedAgain(Long parameterId,String sourceProduct){
		Set<Rule> ruleSet = new HashSet<>();

		Map<Class,String> map = BaseMasterDependency.getDependencyGraphForEntity(Parameter.class);
		String query=null;
		if(MapUtils.isNotEmpty(map)){
			query = map.get(Condition.class);
		}
		if(StringUtils.isNotEmpty(query)) {
			JPAQueryExecutor jpaQueryExecutor = new JPAQueryExecutor(query);
			jpaQueryExecutor.addParameter("id", parameterId);
			List<Condition> conditionList = entityDao.executeQuery(jpaQueryExecutor);
			if (CollectionUtils.isNotEmpty(conditionList)) {
				for (Condition condition : conditionList) {
					List<Rule> list = ruleService.getRulesUsingThisCondition(condition.getId(), condition.getSourceProduct());
					if (CollectionUtils.isNotEmpty(list)) {
						ruleSet.addAll(list);
					}
				}
			}
		}

		return ruleSet;
	}

	public void sendNotificationTaskForRulesToBeApproved(String parmeterCode){
		BaseLoggers.flowLogger.info("calling Send warning message for approving rule asynchronously");

		GenericEvent genericEvent = new GenericEvent(EventTypes.PARAMETER_APPROVAL_EVENT);
		UserInfo userInfo = getCurrentUser();
		genericEvent.addPersistentProperty(GenericEvent.SUCCESS_FLAG, "warning");
		String warningMsg = messageSource.getMessage(PARAMETER_CODE_ALREADY_EXISTS_IN_RULE_WARNING, null, null,
				getUserLocale()) + parmeterCode ;
		genericEvent.addPersistentProperty("WARNING_NOTIFICATION",warningMsg);
		if(userInfo!=null && userInfo.getId()!=null) {
			String userId = userInfo.getId().toString();
			if (userId != null && !userId.isEmpty()) {
				genericEvent.addNonWatcherToNotify(userId);
				GenericEvent eventObj = entityDao.saveOrUpdate(genericEvent);
				String userUri = userInfo.getUserEntityId().getUri();
				Notification notification = new Notification();
				notification.setNotificationUserUri(userUri);
				notification.setNotificationType("warning");
				notification.setSeen(false);
				notification.setGenericEvent(eventObj);
				notification.setEventType(eventObj.getEventType());
				entityDao.saveOrUpdate(notification);
			} else {
				BaseLoggers.exceptionLogger.error("No user found to send warning Notification");
			}
		}
	}

}