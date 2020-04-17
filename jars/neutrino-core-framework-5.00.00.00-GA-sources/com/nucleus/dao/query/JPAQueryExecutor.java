/**
 * This file and a proportion of its content is copyright of Nucleus Software Exports Limited - © 2012. All rights reserved.
 * Any redistribution or reproduction of part or all of the contents in any form is prohibited other than the following:
 * - you cannot print or download to a local hard disk extract contents either part or full for personal/ commercial/
 * academic or any other use
 * - you may not copy the content to individual/ third parties for any type of use, either as compiled or source format
 * without the knowledge and consent of Nucleus SOftware
 * - You may not, except with our express written permission, distribute or commercially exploit the content. Nor may you
 * transmit it or store it in any other web site or other form of electronic retrieval system.
 */
package com.nucleus.dao.query;

import java.util.List;

import javax.persistence.EntityManager;

import org.apache.commons.lang3.StringUtils;

import com.nucleus.core.validation.util.NeutrinoValidator;

/**
 * @author Nucleus Software Exports Limited
 * TODO -> amit.parashar Add documentation to class
 */
public class JPAQueryExecutor<T> extends HQLQueryExecutor<T> {

    public JPAQueryExecutor(String queryString) {
        super();
        this.queryString = queryString;
    }

    private String queryString;

    @Override
    public List<T> executeQuery(EntityManager em, Integer startIndex, Integer pageSize) {
        @SuppressWarnings("unchecked")
        List<T> executeQuery = (List<T>) super.executeQuery(em, queryString, startIndex, pageSize);
        return executeQuery;
    }

    public JPAQueryExecutor<T> addLikeParameter(String paramName, String paramValue, boolean like) {
        if (like) {
            paramValue = paramValue + "%";
        }
        addParameter(paramName, paramValue);
        return this;
    }

    public JPAQueryExecutor<T> addParameter(String paramName, Object paramValue) {
        NeutrinoValidator.isTrue(StringUtils.isNotBlank(paramName), "Parameter name cannot be blank");
        NeutrinoValidator.notNull(paramValue, "param value Object cannot be null");
        // Adding param name to string pool in next line
        boundParameters.put(paramName.intern(), paramValue);
        return this;
    }

    @Override
    public Long executeTotalRowsQuery(EntityManager em) {
        return null;
    }

    public String getQueryString() {
        return queryString;
    }

    public void setQueryString(String queryString) {
        this.queryString = queryString;
    }
    
    /**
     * Add query hints
     * @param queryHintName
     * @param queryHintValue
     * @return
     */
    public JPAQueryExecutor<T> addQueryHint(String queryHintName, Object queryHintValue) {
        NeutrinoValidator.isTrue(StringUtils.isNotBlank(queryHintName), "Query Hint name cannot be blank");
        NeutrinoValidator.notNull(queryHintValue, "Query Hint value cannot be null");
        queryHints.put(queryHintName.intern(), queryHintValue);
        return this;
    }

    public JPAQueryExecutor<T> addNullParameter(String paramName){
        NeutrinoValidator.isTrue(StringUtils.isNotBlank(paramName), "Parameter name cannot be blank");
        boundParameters.put(paramName.intern(), null);
        return this;
    }

}
