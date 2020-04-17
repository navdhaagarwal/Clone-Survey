/**
 * Copyright Nucleus Software India Pvt Ltd. All rights reserved.
 */
package com.nucleus.dao.query;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.NamedQuery;

import org.apache.commons.lang3.StringUtils;

import com.nucleus.core.validation.util.NeutrinoValidator;

/**
 * This class enables us to quickly build query criteria which can be used to fire queries using generic DAO methods. 
 * @author Nucleus Software India Pvt Ltd
 */
public class NamedQueryExecutor<T> extends HQLQueryExecutor<T> {

    private String namedQueryName;

    /**
     * Creates a new CriteriaBuilder object for the passed {@link NamedQuery}.
     */
    protected NamedQueryExecutor() {

    }

    /**
     * Creates a new CriteriaBuilder object for the passed {@link NamedQuery}.
     * @param namedQueryName Name of the NamedQuery for which the criteria is being built 
     */
    public NamedQueryExecutor(String namedQueryName) {
        NeutrinoValidator.isTrue(StringUtils.isNotBlank(namedQueryName), "Name of the named query cannot be blank");
        this.namedQueryName = namedQueryName.intern();  // Adding the name of query to string cache
    }

    public NamedQueryExecutor<T> addLikeParameter(String paramName, String paramValue, boolean like) {
        if (like) {
            paramValue = paramValue + "%";
        }
        addParameter(paramName, paramValue);
        return this;
    }

    public NamedQueryExecutor<T> addParameter(String paramName, Object paramValue) {
        NeutrinoValidator.isTrue(StringUtils.isNotBlank(paramName), "Parameter name cannot be blank");
        NeutrinoValidator.notNull(paramValue, "param value Object cannot be null");
        // Adding param name to string pool in next line
        boundParameters.put(paramName.intern(), paramValue);
        return this;
    }
    
    public NamedQueryExecutor<T> addQueryHint(String queryHintName, Object queryHintValue) {
        NeutrinoValidator.isTrue(StringUtils.isNotBlank(queryHintName), "Query Hint name cannot be blank");
        NeutrinoValidator.notNull(queryHintValue, "Query Hint value cannot be null");
        queryHints.put(queryHintName.intern(), queryHintValue);
        return this;
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<T> executeQuery(EntityManager em, Integer startIndex, Integer pageSize) {
        String queryString = em.createNamedQuery(namedQueryName).unwrap(org.hibernate.Query.class).getQueryString();
        return (List<T>) executeQuery(em, queryString, startIndex, pageSize);
    }

    @Override
    public Long executeTotalRowsQuery(EntityManager em) {
        String queryString = em.createNamedQuery(namedQueryName).unwrap(org.hibernate.Query.class).getQueryString();
        return executeCountQuery(em, convertToCountQueryString(queryString));
    }

}
