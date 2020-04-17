package com.nucleus.core.genericparameter.dao;

import java.util.List;
import java.util.Map;

import com.nucleus.core.genericparameter.entity.GenericParameter;
import com.nucleus.core.genericparameter.entity.GenericParameterMetaData;

/**
 * 
 * @author Nucleus Software Exports Limited
 * Dao layer for querying generic parameters.
 * This interface basically used to query generic parametsr based on there id, or by code.
 */

public interface GenericParameterDao {

    /**
     * 
     * Method to find generic parameter based on code
     * @param typeCode
     * @param entityClass
     * @return
     */

    public <T extends GenericParameter> T  findByCode(String typeCode, Class<T> entityClass);

    /**
     * 
     * Method to load generic parameter based on id
     * @param id
     * @param entityClass
     * @return
     */

    public <T extends GenericParameter> T findById(Long id, Class<T> entityClass);

    /**
     * 
     * Method to load generic parameter based on association name
     * @param genericParameter
     * @param associatedName
     * @return
     */

    public List<GenericParameter> findAllTypes(GenericParameter genericParameter, String associatedName);

    /**
     * 
     * Method to load all generic parameters
     * @deprecated Use the method findAllGenericParameterTypesFromDB()
     * @return
     */
    @Deprecated
    public List<String> findAllGenericParameterTypes();

    /**
     *
     * Method to load all product_source generic parameters excluding marked ones in DB
     *
     * @return
     */
    List<String> findAllGenericParameterTypesFromDB();

    @Deprecated
    public List<String> findAllGenericParameterTypes(Boolean excludeNonEditableGenricParameters);
    
    /**
     * 
     * Method to load generic parameter based on authority codes
     * @param authCodes
     * @param entityClass
     * @return
     */

    public <T extends GenericParameter> List<T> findByAuthorities(List<String> authCodes, Class<T> entityClass);

    /**
     * 
     * Method to load generic parameter based on parent code
     * @param parentCode
     * @param childEntityClass
     * @return
     */

    public <T extends GenericParameter> List<T> findChildrenByParentCode(String parentCode, Class<T> childEntityClass);

    /**
     * 
     * Method to load generic parameter based on entity class name
     * @param genericEntityClassName
     * @return
     */
    public <T extends GenericParameter> List<T> findAllGenericParameter(Class<T> genericEntityClassName);
    /**
     * 
     * Method to load generic parameter based on entity class name and update flag to specify if the loaded data is readonly or not
     * @param genericEntityClassName
     * @return
     */
    public <T extends GenericParameter> List<T> findAllGenericParameter(Class<T> genericEntityClassName, boolean forUpdate);

    public <T extends GenericParameter> T findByName(String code, Class<T> entityClass);
   
    /**
     * 
     * Method to load generic parameter based on entity class name and credit card type code
     * @param name
     * @param genericEntityClassName
     * @return
     */
    public <T extends GenericParameter> List<T> findBillingDueDayByName(String name, Class<T> entityClass);

    public <T extends GenericParameter> List<T> findGenericParameterBasedOnOfflineFlag(Class<T> genericEntityClassName, boolean forUpdate, String authorizationBusinessDate);
    <T> List<Map<String,Object>> findGenericParameterBasedOnFieldValue(Class<T> genericEntityClassName,Map<String, Object> propertyNameEqualsValueMap,Map<String, Object> propertyNameNotEqualsValueMap,String[] searchColumnList);
    
    GenericParameterMetaData getDTypeMetaData(String simpleName);

    public <T extends GenericParameter> T findByDefaultValue(Class<T> entityClass);

    public List<String> findAllViewableGenericParameterTypesFromDB() ;
    public List<String> findAllDynamicGenericParameter();

    public <T extends GenericParameter> T findByDefaultValue(Class<T> entityClass, String dType);

	public  Class findGenericParameterTypes(String dTypeSimpleName);

    public List<? extends GenericParameter> populateDTypeOfGenericParameterForDuplication(String dTypeName);

    public Map<String, List<? extends GenericParameter>> getDTypeBasedMapOfGenericParameter();

	public List<Long> getDTypeBasedListOfGenericParameterIds(String dTypeName);

	public Map<String, List<Long>> getDTypeBasedMapOfGenericParameterIds();
}
