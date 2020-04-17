package com.nucleus.core.genericparameter.service;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import com.nucleus.autocomplete.AutocompleteService;
import com.nucleus.config.persisted.service.ConfigurationService;
import com.nucleus.core.cache.FWCacheHelper;
import com.nucleus.core.exceptions.InvalidDataException;
import com.nucleus.core.genericparameter.dao.GenericParameterDao;
import com.nucleus.core.genericparameter.entity.GenericParameter;
import com.nucleus.core.genericparameter.entity.GenericParameterAssociation;
import com.nucleus.core.genericparameter.entity.GenericParameterMetaData;
import com.nucleus.core.genericparameter.entity.ParentCode;
import com.nucleus.core.transaction.TransactionPostCommitWork;
import com.nucleus.core.transaction.TransactionPostCommitWorker;
import com.nucleus.core.validation.util.NeutrinoValidator;
import com.nucleus.entity.Entity;
import com.nucleus.entity.PersistenceStatus;
import com.nucleus.finnone.pro.base.exception.SystemException;
import com.nucleus.finnone.pro.cache.common.NeutrinoCachePopulator;
import com.nucleus.finnone.pro.cache.common.NeutrinoCachePopulator.Action;
import com.nucleus.finnone.pro.cache.constants.FWCacheConstants;
import com.nucleus.finnone.pro.cache.entity.ImpactedCache;
import com.nucleus.finnone.pro.general.util.ValidatorUtils;
import com.nucleus.logging.BaseLoggers;
import com.nucleus.persistence.HibernateUtils;
import com.nucleus.service.BaseServiceImpl;

import net.bull.javamelody.MonitoredWithSpring;


@Named("genericParameterService")
@MonitoredWithSpring(name = "GenericParameterService_IMPL_")
public class GenericParameterServiceImpl extends BaseServiceImpl implements GenericParameterService {
	
	@Inject
	@Named("fwCacheHelper")
	private FWCacheHelper fwCacheHelper;
	
	@Inject
	@Named("genericParameterByTypeCachePopulator")
	private NeutrinoCachePopulator genericParameterByTypeCachePopulator;
	
	@Inject
	@Named("genericParameterByCodeCachePopulator")
	private NeutrinoCachePopulator genericParameterByCodeCachePopulator;
	
	@Inject
	@Named("genericParameterByNameCachePopulator")
	private NeutrinoCachePopulator genericParameterByNameCachePopulator;
	
	@Inject
	@Named("genericParameterByAuthoritiesCachePopulator")
	private NeutrinoCachePopulator genericParameterByAuthoritiesCachePopulator;
	
	@Inject
	@Named("genericParameterByParentCodeCachePopulator")
	private NeutrinoCachePopulator genericParameterByParentCodeCachePopulator;
	
	@Inject
	@Named("genericParameterPostCommitWork")
	private TransactionPostCommitWork genericParameterPostCommitWork;

	@Inject
	@Named("autocompleteService")
	private AutocompleteService autocompleteService;
	
	@Inject
	@Named("genericParameterDao")
	private GenericParameterDao genericParameterDao;

	@Inject
	@Named("configurationService")
	private ConfigurationService configurationService;

	private static final String ERROR_MESSAGE_1="Entity class or code cannot be null";
	public static final String NEW_GENERIC_PARAMETER = "NEW_GENERIC_PARAMETER";
	public static final String OLD_GENERIC_PARAMETER = "OLD_GENERIC_PARAMETER";
	public static final String IMPACTED_CACHE_MAP = "IMPACTED_CACHE_MAP";
	
	public static final String GENERIC_PARAM_CODE_IMPACTED_CACHE = FWCacheConstants.FW_CACHE_REGION
			+ FWCacheConstants.KEY_DELIMITER + FWCacheConstants.GENERIC_PARAMETER_CODE_ENTITY;
	public static final String GENERIC_PARAM_NAME_IMPACTED_CACHE = FWCacheConstants.FW_CACHE_REGION
			+ FWCacheConstants.KEY_DELIMITER + FWCacheConstants.GENERIC_PARAMETER_NAME_ENTITY;
	public static final String GENERIC_PARAM_PARENTCODE_IMPACTED_CACHE = FWCacheConstants.FW_CACHE_REGION
			+ FWCacheConstants.KEY_DELIMITER + FWCacheConstants.GENERIC_PARAMETER_PARENTCODE_ENTITIES;
	public static final String GENERIC_PARAM_AUTHCODE_IMPACTED_CACHE = FWCacheConstants.FW_CACHE_REGION
			+ FWCacheConstants.KEY_DELIMITER + FWCacheConstants.GENERIC_PARAMETER_AUTHCODE_ENTITIES;
	public static final String GENERIC_PARAM_TYPE_IMPACTED_CACHE = FWCacheConstants.FW_CACHE_REGION
			+ FWCacheConstants.KEY_DELIMITER + FWCacheConstants.GENERIC_PARAMETER_TYPE_ENTITIES;
	
	private static final String DEFAULT_GENERIC_PARAMETER_SORT_COLUMN = "name";
    private static final String DYNAMIC_DTYPE_CLASS = "com.nucleus.core.genericparameter.entity.DynamicGenericParameter"; 
	
	@Override
	@MonitoredWithSpring(name = "GPSI_FETCH_GENERIC_TYPES")
	public <T extends GenericParameter> List<T> retrieveTypes(Class<T> entityClass) {
		return retrieveTypes(entityClass, true);
	}

	@Override
	@MonitoredWithSpring(name = "GPSI_FETCH_GENERIC_TYPES")
	public <T extends GenericParameter> List<T> findGenericParameterBasedOnOfflineFlag(Class<T> entityClass,
			String authorizationBusinessDate) {
		List<T> entities = genericParameterDao.findGenericParameterBasedOnOfflineFlag(entityClass, false,
				authorizationBusinessDate);
		for (Entity entity : entities) {
			entity = HibernateUtils.initializeAndUnproxy(entity);
		}
		return entities;
	}

	@SuppressWarnings("unchecked")
	@Override
	@MonitoredWithSpring(name = "GPSI_FETCH_GENERIC_TYPES")
	public <T extends GenericParameter> List<T> retrieveTypes(Class<T> entityClass, boolean includeOnlyActive) {
		if (entityClass == null) {
			throw new InvalidDataException("Entity class cannot be null");
		}
		List<Long> genericParameterIdList = (List<Long>) genericParameterByTypeCachePopulator
				.get(entityClass.getName());
		return filterListOfGenericParametersByActiveFlag(genericParameterIdList, entityClass, includeOnlyActive);
	}

	
	@Override
	public <T extends GenericParameter> T findByCode(String code, Class<T> entityClass) {
		return findByCode(code, entityClass, false);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T extends GenericParameter> T findByName(String name, Class<T> entityClass) {
		if (StringUtils.isBlank(name) || entityClass == null) {
			throw new InvalidDataException("Entity class or name cannot be null");
		}
		GenericParameter genericParameterEntity = null;
		Long id = (Long) genericParameterByNameCachePopulator.get(new StringBuilder(entityClass.getName())
				.append(FWCacheConstants.KEY_DELIMITER).append(name).toString());
		if (id != null) {
			genericParameterEntity = findById(id, entityClass);
		}

		return (T) genericParameterEntity;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T extends GenericParameter> List<T> findByAuthorities(List<String> authCodes, Class<T> entityClass) {
		if (CollectionUtils.isEmpty(authCodes) || entityClass == null) {
			throw new InvalidDataException(ERROR_MESSAGE_1);
		}

		List<T> genericParameterFinalList = new ArrayList<>();
		Set<Long> genericParameterFinalIdSet = new HashSet<>();

		for (String authCode : authCodes) {
			genericParameterFinalIdSet.addAll(
					(Set<Long>) genericParameterByAuthoritiesCachePopulator.get(new StringBuilder(entityClass.getName())
							.append(FWCacheConstants.KEY_DELIMITER).append(authCode).toString()));
		}

		if (!genericParameterFinalIdSet.isEmpty()) {
			for (Long id : genericParameterFinalIdSet) {
				genericParameterFinalList.add(this.findById(id, entityClass));
			}
		}

		return genericParameterFinalList;

	}
	

	
	@Override
	public <T extends GenericParameter> T findById(Long id, Class<T> entityClass) {
		if (id == null || entityClass == null) {
			throw new InvalidDataException(ERROR_MESSAGE_1);
		}

		return genericParameterDao.findById(id, entityClass);
	}

	@Override
	public void createGenericParameter(GenericParameter genericParameter) {
		if (genericParameter == null) {
			throw new InvalidDataException("Generic Parameter cannot be null");
		}
		entityDao.persist(genericParameter);
		
		Map<String,Object> argumentsMap = new HashMap<>();
		argumentsMap.put(NEW_GENERIC_PARAMETER, genericParameter);
		argumentsMap.put(OLD_GENERIC_PARAMETER, null);
		argumentsMap.put(IMPACTED_CACHE_MAP, fwCacheHelper.createAndGetImpactedCachesFromCacheNames(
				FWCacheConstants.GENERIC_PARAMETER_AUTHCODE_ENTITIES, FWCacheConstants.GENERIC_PARAMETER_CODE_ENTITY,
				FWCacheConstants.GENERIC_PARAMETER_NAME_ENTITY, FWCacheConstants.GENERIC_PARAMETER_PARENTCODE_ENTITIES,
				FWCacheConstants.GENERIC_PARAMETER_TYPE_ENTITIES));
		
		TransactionPostCommitWorker.handlePostCommit(genericParameterPostCommitWork, argumentsMap, false);
	}

	@Override
	public List<GenericParameter> findAssociatedParameters(GenericParameter genericParameter, String associationName) {
		if (StringUtils.isBlank(associationName) || genericParameter == null) {
			throw new InvalidDataException("Generic Parameter or association name cannot be null");
		}
		return genericParameterDao.findAllTypes(genericParameter, associationName);
	}

	@Override
	public GenericParameterAssociation retrieveGenericParameterAssociation(Long associationId) {
		if (associationId == null) {
			throw new InvalidDataException("Id cannot be null");
		}
		GenericParameterAssociation gpa = entityDao.find(GenericParameterAssociation.class, associationId);
		gpa.getGenericParameter().getId();
		gpa.getAssociatedGenericParameter().size();
		return gpa;
	}

	@Override
	public void createGenericParameterAssociation(GenericParameterAssociation genericParameterAssociation) {
		if (genericParameterAssociation == null) {
			throw new InvalidDataException("Generic Parameter Association cannot be null");
		}
		entityDao.persist(genericParameterAssociation);
	}

	
	@Override
	public List<String> findAllGenericParameterTypes() {
		return genericParameterDao.findAllGenericParameterTypes();
	}

	@Override
	public List<String> findAllDynamicGenericParameter() {
		return genericParameterDao.findAllDynamicGenericParameter();
	}
	@Override
	public List<String> findAllGenericParameterTypesFromDB() {
		return genericParameterDao.findAllGenericParameterTypesFromDB();
	}

	@Override
	public List<Map<String, ?>> searchDtypes(String className, String itemVal,
														String[] searchColumnList, String value,
														Boolean loadApprovedEntityFlag, String itemsList,
														Boolean strictSearchOnitemsList, int page) {


		List<String> genericParameterList = genericParameterDao.findAllGenericParameterTypesFromDB();
		if(genericParameterList.contains(DYNAMIC_DTYPE_CLASS)){
			genericParameterList.remove(DYNAMIC_DTYPE_CLASS);
		}
		List<String> similarItems=new ArrayList<>();
		if(!value.contains("%%%")) {
			for (String s : genericParameterList) {
				if (s.toLowerCase().contains(value.toLowerCase())) {
					similarItems.add(s);
				}
			}
		} else{
			similarItems = genericParameterList;
		}

		List<Map<String, ?>> newList = new ArrayList<>();

			for (int x = 0; x < similarItems.size(); x++) {
				LinkedHashMap crsMap = new LinkedHashMap();
				String genparam = similarItems.get(x);

					Class<GenericParameter> suppliedGenericParameterClass = GenericParameter.class;
					try {
						suppliedGenericParameterClass = (Class<GenericParameter>) Class.forName(genparam);
					} catch (ClassNotFoundException e) {
						BaseLoggers.exceptionLogger.error("Error in loading master data:" + genparam, e.getMessage());
						return null;
					}


					crsMap.put("id", genparam);
					crsMap.put("dtypeSimpleName", suppliedGenericParameterClass.getSimpleName());
					newList.add(crsMap);

			}
			List<String> dynamicGenricParameters = genericParameterDao.findAllDynamicGenericParameter();
			similarItems=new ArrayList<>();
			if(!value.contains("%%%")) {
				for (String s : dynamicGenricParameters) {
					if (s.toLowerCase().contains(value.toLowerCase())) {
						similarItems.add(s);
					}
				}
			} else{
				similarItems = dynamicGenricParameters;
			}
			for (int x = 0; x < similarItems.size(); x++) {
				LinkedHashMap crsMap = new LinkedHashMap();
				crsMap.put("id", similarItems.get(x));
				crsMap.put("dtypeSimpleName", similarItems.get(x));
				newList.add(crsMap);
			}
			newList.sort((o1, o2) -> o1.get("dtypeSimpleName").toString().compareToIgnoreCase(o2.get("dtypeSimpleName").toString()));
		List<Map<String, ?>> newList1 = new ArrayList<>();
		if(!newList.isEmpty()){
			int startIndex = page*3;
			int endIndex = (page*3)+2;
			if(endIndex < newList.size()){
				newList1.addAll(newList.subList(startIndex, (endIndex + 1)));
			}
			else {
				endIndex = newList.size()-1;
				newList1.addAll(newList.subList(startIndex, endIndex));
				newList1.add(newList.get(endIndex));
			}
		}
		HashMap sizeMap = new HashMap();
		sizeMap.put("size", newList.size());
		newList1.add(sizeMap);



		return newList1;
	}

	@Override
	public List<String> findParentsForGenericParameter(String dType) {
		List<ParentCode> parentCodeGenParamList=retrieveTypes(ParentCode.class);
		List<String> eligibleParentsList=new ArrayList<>();
		for(ParentCode parentGenParam:parentCodeGenParamList){
			HashSet<String> containingGenParamList = new HashSet<>();
			if(StringUtils.isNotEmpty(parentGenParam.getParentCode())) {
				containingGenParamList = new HashSet<>(Arrays.asList(parentGenParam.getParentCode().split(",")));
			}
			if(containingGenParamList.contains(dType)){
				eligibleParentsList.add(parentGenParam.getCode());
			}
		}

		return eligibleParentsList;
	}

	@Override
	public GenericParameter updateGenericParameter(GenericParameter genericParameter) {
		NeutrinoValidator.notNull(genericParameter, "GenericParameter can't be null");
		NeutrinoValidator.notNull(genericParameter.getId(), "Id can't be null");
		
		GenericParameter oldGenericParameter = this.findById(genericParameter.getId(), genericParameter.getClass());
 		if (oldGenericParameter == null) {
			throw new InvalidDataException("Generic Parameter cannot be null");
		}
 		oldGenericParameter.initializeAuthorities();
 		fwCacheHelper.detachEntity(oldGenericParameter);
		
		GenericParameter newGenericParameter = this.findById(genericParameter.getId(), genericParameter.getClass());
		newGenericParameter.setName(genericParameter.getName());
		newGenericParameter.setDescription(genericParameter.getDescription());
		newGenericParameter.setOfflineFlag(genericParameter.getOfflineFlag());
		newGenericParameter.setParentCode(genericParameter.getParentCode());
		newGenericParameter.setPersistenceStatus(genericParameter.getPersistenceStatus());
		newGenericParameter.setDefaultFlag(genericParameter.getDefaultFlag());
		newGenericParameter.setActiveFlag(genericParameter.isActiveFlag());
		entityDao.update(newGenericParameter);
		
		Map<String,Object> argumentsMap = new HashMap<>();
        argumentsMap.put(NEW_GENERIC_PARAMETER, newGenericParameter);
        argumentsMap.put(OLD_GENERIC_PARAMETER, oldGenericParameter);
        argumentsMap.put(IMPACTED_CACHE_MAP, fwCacheHelper.createAndGetImpactedCachesFromCacheNames(
				FWCacheConstants.GENERIC_PARAMETER_AUTHCODE_ENTITIES, FWCacheConstants.GENERIC_PARAMETER_CODE_ENTITY,
				FWCacheConstants.GENERIC_PARAMETER_NAME_ENTITY, FWCacheConstants.GENERIC_PARAMETER_PARENTCODE_ENTITIES,
				FWCacheConstants.GENERIC_PARAMETER_TYPE_ENTITIES));
        
        TransactionPostCommitWorker.handlePostCommit(genericParameterPostCommitWork, argumentsMap, false);
		
		return genericParameter;
	}

	
	
	@Override
	public <T extends GenericParameter> List<T> findChildrenByParentCode(String parentCodes,
			Class<T> childEntityClass) {
		return findChildrenByParentCode(parentCodes, childEntityClass, false);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public <T extends GenericParameter> List<T> findChildrenByParentCode(String parentCodes,
			Class<T> childEntityClass, Boolean includeOnlyActive) {
		if (StringUtils.isBlank(parentCodes) || childEntityClass == null || includeOnlyActive == null) {
			throw new InvalidDataException("ChildEntity class or parentCode cannot be null");
		}

		Set<Long> genericParameterFinalIdSet = new HashSet<>();
		String[] parentCodesArray = parentCodes.split(" ");

		for (String parentCode : parentCodesArray) {
			genericParameterFinalIdSet.addAll((Set<Long>) genericParameterByParentCodeCachePopulator
					.get(new StringBuilder(childEntityClass.getName()).append(FWCacheConstants.KEY_DELIMITER)
							.append(parentCode.trim()).toString()));
		}
		return filterListOfGenericParametersByActiveFlag(genericParameterFinalIdSet, childEntityClass, includeOnlyActive);
	}
	
	
	@Override
	public <T extends GenericParameter> List<T> findBillingDueDayByName(String name, Class<T> entityClass) {
		if (StringUtils.isBlank(name) || entityClass == null) {
			throw new InvalidDataException("ChildEntity class or parentCode cannot be null");
		}
		return genericParameterDao.findBillingDueDayByName(name, entityClass);
	}

	@Override
	public <T> List<Map<String, Object>> findGenericParameterBasedOnFieldValue(Class<T> genericEntityClassName,
			Map<String, Object> propertyNameEqualsValueMap, Map<String, Object> propertyNameNotEqualsValueMap,
			String[] searchColumnList) {
		return genericParameterDao.findGenericParameterBasedOnFieldValue(genericEntityClassName,
				propertyNameEqualsValueMap, propertyNameNotEqualsValueMap, searchColumnList);
	}

	
	public void createOrUpdateGenericParameterCache(GenericParameter genericParameter,
			GenericParameter oldGenericParameter, Map<String,ImpactedCache> impactedCacheMap) {
		createOrUpdateGenericParameterCache(genericParameter, oldGenericParameter, impactedCacheMap, Boolean.FALSE);
	}

	@Override
	public void createOrUpdateGenericParameterCache(GenericParameter genericParameter,
			GenericParameter oldGenericParameter, Map<String,ImpactedCache> impactedCacheMap, Boolean doNotUpdateIdCache) {

		BaseLoggers.flowLogger.debug(" Start GenericParameterServiceImpl:: updateGenericParameterCache ");
		
		if (ValidatorUtils.notNull(oldGenericParameter)) {
			removeExistingEntriesFromCache(oldGenericParameter, doNotUpdateIdCache);
		}
		
		genericParameterByCodeCachePopulator.update(impactedCacheMap, Action.UPDATE, genericParameter);
		genericParameterByNameCachePopulator.update(impactedCacheMap, Action.UPDATE, genericParameter);
		genericParameterByAuthoritiesCachePopulator.update(impactedCacheMap, Action.UPDATE, genericParameter);
		genericParameterByParentCodeCachePopulator.update(impactedCacheMap, Action.UPDATE, genericParameter);
		
		if(!doNotUpdateIdCache) {
			genericParameterByTypeCachePopulator.update(impactedCacheMap, Action.UPDATE, genericParameter);
		}
		

		BaseLoggers.flowLogger.debug(" End GenericParameterServiceImpl:: updateGenericParameterCache ");

	}

	private void removeExistingEntriesFromCache(GenericParameter oldGenericParameter, Boolean doNotUpdateIdCache) {
		genericParameterByCodeCachePopulator.update(Action.DELETE, oldGenericParameter);
		genericParameterByNameCachePopulator.update(Action.DELETE, oldGenericParameter);
		genericParameterByAuthoritiesCachePopulator.update(Action.DELETE, oldGenericParameter);
		genericParameterByParentCodeCachePopulator.update(Action.DELETE, oldGenericParameter);
		
		if(!doNotUpdateIdCache){
			genericParameterByTypeCachePopulator.update(Action.DELETE, oldGenericParameter);
		}
	}
		
		
	@SuppressWarnings("unchecked")
	@Override
	public GenericParameterMetaData getDTypeMetaData(String dTypeClassName){
		Class<GenericParameter> genParamClass=GenericParameter.class;
		try {
			genParamClass= (Class<GenericParameter>) Class.forName(dTypeClassName);
		} catch (ClassNotFoundException e) {

			BaseLoggers.exceptionLogger.error("Could not find generic parameter class",e);
		}
		return genericParameterDao.getDTypeMetaData(genParamClass.getSimpleName());		 
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public void sortGenericParameterList(List<? extends GenericParameter> genericParameterFinalList, String sortBy,
			String comparatorClassName) {

		if (StringUtils.isNotEmpty(comparatorClassName)) {
			Class<GenericParameterComparator> genParamCompClass;
			try {
				genParamCompClass = (Class<GenericParameterComparator>) Class.forName(comparatorClassName);
				Collections.sort(genericParameterFinalList, genParamCompClass.newInstance());
			} catch (Exception e) {
				BaseLoggers.exceptionLogger
						.error("No Such Class Exist Exception occurred for class : " + comparatorClassName + " {} ", e);
				throw new SystemException(
						"No Such Class Exist Exception occurred for class : " + comparatorClassName + " {} ", e);
			}
		} else {
			String sortByColumn = StringUtils.isNotEmpty(sortBy) ? sortBy : DEFAULT_GENERIC_PARAMETER_SORT_COLUMN;
			Collections.sort(genericParameterFinalList, new DefaultGenericParameterComparator(sortByColumn, null));
		}

	}

	@Override
	public <T extends GenericParameter> T getDefaultValue(Class<T> entityClass){

		GenericParameter genericParameterEntity=genericParameterDao.findByDefaultValue(entityClass);

		return (T) genericParameterEntity;

	}
	
	@Override
	public <T extends GenericParameter> T getDefaultValueForDynamicDtype(Class<T> entityClass,String dtype){

		GenericParameter genericParameterEntity=genericParameterDao.findByDefaultValue(entityClass,dtype);

		return (T) genericParameterEntity;

	}


	public List<String> findAllViewableGenericParameterTypesFromDB() {
		return genericParameterDao.findAllViewableGenericParameterTypesFromDB();
	}
	

	@SuppressWarnings("unchecked")
	@Override
	public <T extends GenericParameter> T findByCode(String code, Class<T> entityClass, Boolean onlyActive) {
		if (StringUtils.isBlank(code) || entityClass == null) {
			throw new InvalidDataException(ERROR_MESSAGE_1);
		}

		GenericParameter genericParameterEntity = null;
		Long id = (Long) genericParameterByCodeCachePopulator.get(new StringBuilder(entityClass.getName())
				.append(FWCacheConstants.KEY_DELIMITER).append(code).toString());
		if (id != null) {
			genericParameterEntity = findById(id, entityClass);
		}

		if (onlyActive && genericParameterEntity != null
				&& (genericParameterEntity.getPersistenceStatus() == null
						|| !genericParameterEntity.getPersistenceStatus().equals(PersistenceStatus.ACTIVE)
						|| !genericParameterEntity.isActiveFlag())) {
			genericParameterEntity = null;
		}

		return (T) genericParameterEntity;
	}
	
	public Class findGenericParameterTypes(String dTypeSimpleName){
		return genericParameterDao.findGenericParameterTypes(dTypeSimpleName);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T extends GenericParameter> List<T> retrieveTypesForDuplication(Class<T> entityClass) {
		if (entityClass == null) {
			throw new InvalidDataException("Entity class cannot be null");
		}

		List<GenericParameter> genericParameterList = null;

		genericParameterList = (List<GenericParameter>) genericParameterDao
				.populateDTypeOfGenericParameterForDuplication(entityClass.getName());

		return (List<T>) genericParameterList;

	}

	@Override
	public <T extends GenericParameter> List<T> findChildrenByParentCodeAndNullCode(String parentCode,Class<T> entityClass,Boolean parentCodeNullFlag, boolean includeOnlyActive){
		List<Long> genericParameterIdList = (List<Long>) genericParameterByTypeCachePopulator.get(entityClass.getName());
		List<GenericParameter> activeGenericParametersList = new ArrayList<>();
		List<GenericParameter> allGenericParametersList = new ArrayList<>();
		if (ValidatorUtils.hasElements(genericParameterIdList)) {
			GenericParameter genericParameter = null;
			for (Long id : genericParameterIdList) {
				genericParameter = this.findById(id, entityClass);
				genericParameter.initializeAuthorities();

				if (genericParameter.getParentCode() == null|| (genericParameter.getParentCode()!=null && genericParameter.getParentCode().equalsIgnoreCase(parentCode))) {
					allGenericParametersList.add(genericParameter);
					if(genericParameter.isActiveFlag()) {
						activeGenericParametersList.add(genericParameter);
					}
				}
			}
		}
		if(includeOnlyActive) {
			return (List<T>) activeGenericParametersList;
		}

		return (List<T>) allGenericParametersList;
	}
	
	@SuppressWarnings("unchecked")
	private <T extends GenericParameter> List<T> filterListOfGenericParametersByActiveFlag(Collection<Long> genericParameterIdCollection, Class<T> entityClass, Boolean includeOnlyActive){
		List<GenericParameter> activeGenericParametersList = new ArrayList<>();
		List<GenericParameter> allGenericParametersList = new ArrayList<>();
		
		if (ValidatorUtils.hasElements(genericParameterIdCollection)) {
			GenericParameter genericParameter = null;
			for (Long id : genericParameterIdCollection) {
				genericParameter = this.findById(id, entityClass);
				genericParameter.initializeAuthorities();
				allGenericParametersList.add(genericParameter);
				if (genericParameter.getPersistenceStatus().equals(PersistenceStatus.ACTIVE) && genericParameter.isActiveFlag()) {
					activeGenericParametersList.add(genericParameter);
				}
			}
		} 
		
		if(includeOnlyActive) {
			return (List<T>) activeGenericParametersList;
		}
		
		return (List<T>) allGenericParametersList;
	}

}
