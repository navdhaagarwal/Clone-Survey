package com.nucleus.core.genericparameter.service;

import java.util.List;
import java.util.Map;

import com.nucleus.core.genericparameter.entity.GenericParameter;
import com.nucleus.core.genericparameter.entity.GenericParameterAssociation;
import com.nucleus.core.genericparameter.entity.GenericParameterMetaData;
import com.nucleus.finnone.pro.cache.entity.ImpactedCache;
import com.nucleus.service.BaseService;

/**
 * 
 * @author Nucleus Software Exports Limited This Interface is basically used to
 *         fetch the Generic Parameters by code by name by id by class
 */

public interface GenericParameterService extends BaseService {

	/**
	 * 
	 * Method to load generic parameter based on entity class name
	 * 
	 * @param entityClass
	 * @return
	 */

	public <T extends GenericParameter> List<T> retrieveTypes(Class<T> entityClass);

	/**
	 * 
	 * Method to load generic parameter based on entity class name and update flag
	 * to specify if the loaded data will be subsequently updated or not
	 * 
	 * @param entityClass
	 * @return
	 */

	public <T extends GenericParameter> List<T> retrieveTypes(Class<T> entityClass, boolean includeOnlyActive);

	/**
	 * 
	 * Method to find generic parameter based on code
	 * 
	 * @param code
	 * @param entityClass
	 * @return
	 */

	public <T extends GenericParameter> T findByCode(String code, Class<T> entityClass);

	/**
	 * 
	 * Method to find generic parameter based on code and active flag
	 * 
	 * @param code
	 * @param entityClass
	 * @return
	 */

	public <T extends GenericParameter> T findByCode(String code, Class<T> entityClass, Boolean onlyActive);

	/**
	 * 
	 * Method to load generic parameter based on id
	 * 
	 * @param id
	 * @param entityClass
	 * @return
	 */

	public <T extends GenericParameter> T findById(Long id, Class<T> entityClass);

	/**
	 * 
	 * Method to create new generic parameter
	 * 
	 * @param genericParameter
	 */

	public void createGenericParameter(GenericParameter genericParameter);

	/**
	 * 
	 * Method to load generic parameter based on association name
	 * 
	 * @param genericParameter
	 * @param associationName
	 * @return
	 */

	public List<GenericParameter> findAssociatedParameters(GenericParameter genericParameter, String associationName);

	/**
	 * 
	 * Method to load generic parameter based on generic parameter association id
	 * 
	 * @param id
	 * @return
	 */

	public GenericParameterAssociation retrieveGenericParameterAssociation(Long id);

	/**
	 * 
	 * Method to create generic parameter association
	 * 
	 * @param genericParameterAssociation
	 */

	public void createGenericParameterAssociation(GenericParameterAssociation genericParameterAssociation);

	/**
	 * 
	 * Method to load all generic parameters
	 * 
	 * @return
	 */

	public List<String> findAllGenericParameterTypes();

	/**
	 *
	 * Method to load all generic parameters excluding marked dtypes in DB
	 *
	 * @return
	 */
	List<String> findAllGenericParameterTypesFromDB();

	/**
	 *
	 * Method to find eligible parent codes for given generic parameter
	 *
	 * @return
	 */
	List<String> findParentsForGenericParameter(String dType);

	/**
	 * 
	 * Method to update generic parameters
	 * 
	 * @param genericParameter
	 * @return
	 */

	public GenericParameter updateGenericParameter(GenericParameter genericParameter);

	/**
	 * 
	 * Method to load generic parameter based on authority codes
	 * 
	 * @param authCodes
	 * @param entityClass
	 * @return
	 */

	public <T extends GenericParameter> List<T> findByAuthorities(List<String> authCodes, Class<T> entityClass);

	/**
	 * 
	 * Method to load generic parameters list based on space separated parent codes - ALL
	 * 
	 * @param parentCode
	 * @param childEntityClass
	 * @return
	 */

	public <T extends GenericParameter> List<T> findChildrenByParentCode(String parentCodes, Class<T> childEntityClass);

	/**
	 * 
	 * Method to load generic parameters list based on space separated parent codes - Active/All
	 * 
	 * @param parentCodes
	 * @param childEntityClass
	 * @param onlyActive
	 * @return
	 */
	public <T extends GenericParameter> List<T> findChildrenByParentCode(String parentCodes, Class<T> childEntityClass, Boolean includeOnlyActive);
	
	/**
	 * 
	 * Method to find generic parameter based on name
	 * 
	 * @param name
	 * @param entityClass
	 * @return
	 */

	public <T extends GenericParameter> T findByName(String name, Class<T> entityClass);

	public <T extends GenericParameter> List<T> findGenericParameterBasedOnOfflineFlag(Class<T> entityClass,
			String authorizationBusinessDate);

	/**
	 * 
	 * Method to find list of generic parameter based on name to extract list of
	 * billing due day for a credit card type
	 * 
	 * @param name
	 * @param entityClass
	 * @return
	 */
	public <T extends GenericParameter> List<T> findBillingDueDayByName(String code, Class<T> entityClass);

	/**
	 *
	 * Method to find purpose associated with given generic paramter class
	 *
	 * @param dType_class
	 * @return String
	 */
	GenericParameterMetaData getDTypeMetaData(String dType_class);

	<T> List<Map<String, Object>> findGenericParameterBasedOnFieldValue(Class<T> genericEntityClassName,
			Map<String, Object> propertyNameEqualsValueMap, Map<String, Object> propertyNameNotEqualsValueMap,
			String[] searchColumnList);

	public void createOrUpdateGenericParameterCache(GenericParameter genericParameter,
			GenericParameter oldGenericParameter, Map<String, ImpactedCache> impactedCacheMap);

	public void createOrUpdateGenericParameterCache(GenericParameter genericParameter,
			GenericParameter oldGenericParameter, Map<String, ImpactedCache> impactedCacheMap,
			Boolean doNotUpdateIdCache);

	public void sortGenericParameterList(List<? extends GenericParameter> genericParameterFinalList, String sortBy,
			String comparatorClassName);

	List<Map<String, ?>> searchDtypes(String className, String itemVal, String[] searchColumnList, String value,
			Boolean loadApprovedEntityFlag, String itemsList, Boolean strictSearchOnitemsList, int page);

	public <T extends GenericParameter> T getDefaultValue(Class<T> entityClass);

	public List<String> findAllViewableGenericParameterTypesFromDB();

	public <T extends GenericParameter> T getDefaultValueForDynamicDtype(Class<T> entityClass, String dtype);

	public List<String> findAllDynamicGenericParameter();

	public Class findGenericParameterTypes(String dTypeSimpleName);

	public <T extends GenericParameter> List<T> retrieveTypesForDuplication(Class<T> entityClass);

	public <T extends GenericParameter> List<T> findChildrenByParentCodeAndNullCode(String parentCode,Class<T> entityClass,Boolean parentCodeNullFlag, boolean includeOnlyActive);

}
