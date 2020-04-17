package com.nucleus.persistence;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;

import com.nucleus.core.exceptions.SystemException;
import com.nucleus.dao.query.QueryExecutor;
import com.nucleus.entity.Entity;
import com.nucleus.entity.EntityId;
import com.nucleus.master.BaseMasterEntity;

/**
 * Super DAO with generic methods to support class independent DAO operations 
 */
public interface EntityDao {

    public <T extends Entity> T find(Class<T> entityClass, Serializable id);

    public Boolean contains(Entity entity);
    
	public void persist(Entity entity);

    /**
     * Updates the entity.
     * <b>NOTE:</b> The passed instance will get detached from persistence context and returned instance should be only used
     * by the client code. Also note that passed and returned entity instance are not same.
     * @param entity The entity instance which is to be updated.
     * @return Entity instance which is result of merge operation on original entity object.
     * @throws SystemException if passed entity instance doesn't contain 'id' (which implies that it is not yet saved). 
     */
    public <T extends Entity> T update(T entity);

    /**
     * Either save/persists or updates the passed entity instance.
     * @see {@link #persist(Entity)} and {@link #update(Entity)}
     * @param entity
     * @return managed entity
     */
    public <T extends Entity> T saveOrUpdate(T entity);

    public <T extends Entity> void detach(T entity);

    /**
     * Deletes the entity from database.
     */
    public void delete(Entity entity);

    /**
     * Retrieves the entity on the basis of entity id. The class and id of entity is derived from EntityId passed as argument.
     */
    public <T extends Entity> T get(EntityId entityId);

    /**
     * Flushes the current state of EntityManager.<br/> 
     * see {@link EntityManager#flush()} for more details 
     */
    public void flush();

    /**
     * Returns all instances of passed class from database.<br/>
     */
    public <T extends Entity> List<T> findAll(Class<T> entityClass);

    public boolean entityExists(EntityId entityId);

    public <T> List<T> executeQuery(QueryExecutor<T> executor);

    public <T> List<T> executeQuery(QueryExecutor<T> executor, Integer startIndex, Integer pageSize);

    public <T> T executeQueryForSingleValue(QueryExecutor<T> executor);

    /**
     * 
     * @param entityClass
     * @param field
     * @param value
     * @return
     */
    public <T extends Entity> List<T> searchEntityOnFieldAndValue(Class<T> entityClass, String field, String value);

    /**
     * Returns the next value for sequence name. <b>NOTE</b> This will throw an unchecked exception if sequence doesn't exist in database.
     * @param sequenceName the name of sequence for which the value is to be returned.
     * @return The returned value.
     */
    public Long getNextValue(String sequenceName);

    public Long getNextValue(String sequenceName, int seqIncr);

    /**
     * Load entity by entity id and entity class.This method will load proxy obect of entity if exists, 
     * otherwise throws EntityNotFoundException
     *
     *
     * @param <T> the generic type
     * @param entityClass the entity class
     * @param id the id
     * @return the t
     */
    public <T extends Entity> T load(Class<T> entityClass, Serializable id);

    /**
     * Updates a particular entity using the passed queryString with the values entered in the passed map 
     * 
     * @description You can look at following example to use this method :
     * String queryString = "UPDATE Entity e SET e.field1 = :param1 WHERE e.field2 = :param2";
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("param1", object1);
        map.put("param2", object2);
        int count = entityDao.updateQuery(queryString,map);
     *
     *
     * @param queryString String
     * @param map Map<String, Object>
     * @return number of rows updated
     */
    public int updateQuery(String queryString, Map<String, Object> map);

    public <T> Long executeTotalRowsQuery(QueryExecutor<T> executor);

    public <T extends Entity> List<Object[]> findAllWithSpecifiedColumns(Class<T> entityClass, String[] columnNameList);

    public void clearEntireCache();

    /**
     * Gets the entity manager from the persistence context.
     *
     * @return the entity manager
     */
    public EntityManager getEntityManager();
    
    public  List<Map<String, Object>> executeSingleInClauseHQLQuery(String hqlQuery, String inParamName, Collection<?> values);

    /**
     * 
     * Method to clear cache for particular entity only
     * @param clazz
     */
    public void clearEntityLevelCache(Class clazz);

    List<Map<String, Object>> executeSingleInClauseHQLQuery(String hqlQuery, String inParamName, Collection<?> values,
            Map<String, ?> otherParams);

    <T>List<T> executeSingleInClauseHQLQuery(String hqlQuery, String inParamName, Collection<?> values,
            Map<String, ?> otherParams,Class<T> returnClass);

    
    <T> List<T> executeSingleInClauseHQLQuery(String hqlQuery, String inParamName, Collection<?> values, Class<T> returnClass);

    <T extends Entity> T saveOrHibernateUpdate(T entity);

    <T extends Entity> T hibernateSaveOrUpdate(T entity);

	void executeSingleInClauseHQLQueryForUpdateDelete(String hqlQuery,
			String inParamName, Collection<?> values);


   public BaseMasterEntity findByUUID(Class entityClass,String uuid); 


    public BaseMasterEntity findByMasterUUID(Class entityClass,String uuid);

}