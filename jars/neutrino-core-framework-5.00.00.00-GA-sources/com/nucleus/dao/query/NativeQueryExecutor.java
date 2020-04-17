package com.nucleus.dao.query;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.persistence.EntityManager;
import javax.persistence.NamedQuery;
import javax.persistence.Query;

import org.apache.commons.lang3.StringUtils;

import com.nucleus.core.validation.util.NeutrinoValidator;

/**
 * This class enables us to quickly build query criteria which can be used to fire queries using generic DAO methods. 
 * @author Nucleus Software India Pvt Ltd
 */
public class NativeQueryExecutor<T> implements QueryExecutor<T> {

    private String                nativeQueryName;
    protected Map<String, Object> boundParameters = new LinkedHashMap<String, Object>();

    /**
     * Creates a new CriteriaBuilder object for the passed {@link NamedQuery}.
     */
    protected NativeQueryExecutor() {

    }

    /**
     * Creates a new CriteriaBuilder object for the passed {@link NamedQuery}.
     * @param namedQueryName Name of the NamedQuery for which the criteria is being built 
     */
    public NativeQueryExecutor(String nativeQueryName) {
        NeutrinoValidator.isTrue(StringUtils.isNotBlank(nativeQueryName), "Name of the native query cannot be blank");
        this.nativeQueryName = nativeQueryName.intern();  // Adding the name of query to string cache
    }

    public NativeQueryExecutor<T> addLikeParameter(String paramName, String paramValue, boolean like) {
        if (like) {
            paramValue = paramValue + "%";
        }
        addParameter(paramName, paramValue);
        return this;
    }

    public NativeQueryExecutor<T> addParameter(String paramName, Object paramValue) {
        NeutrinoValidator.isTrue(StringUtils.isNotBlank(paramName), "Parameter name cannot be blank");
        NeutrinoValidator.notNull(paramValue, "param value Object cannot be null");
        // Adding param name to string pool in next line
        boundParameters.put(paramName.intern(), paramValue);
        return this;
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<T> executeQuery(EntityManager em, Integer startIndex, Integer pageSize) {
        Query query = em.createNativeQuery(nativeQueryName);
        addAllParametersIntoQuery(query);
        if (startIndex != null && pageSize != null) {
            query.setFirstResult(startIndex);
            query.setMaxResults(pageSize);
        }
        query.unwrap(org.hibernate.SQLQuery.class).addSynchronizedQuerySpace("");
        return query.getResultList();
    }

    @Override
    public Long executeTotalRowsQuery(EntityManager em) {
        return executeCountQuery(em, convertToCountQueryString(nativeQueryName));
    }

    protected void addAllParametersIntoQuery(Query query) {
        for (Entry<String, Object> parameter : boundParameters.entrySet()) {
            query.setParameter(parameter.getKey(), parameter.getValue());
        }

    }

    protected String convertToCountQueryString(String queryString) {
        return queryString.replaceFirst("^.*(?i)from", "select count (*) from ");
    }

    protected Long executeCountQuery(EntityManager em, String queryString) {
        Query query = em.createNativeQuery(queryString);
        addAllParametersIntoQuery(query);
        query.unwrap(org.hibernate.SQLQuery.class).addSynchronizedQuerySpace("");
        return (Long) query.getSingleResult();
    }
}
