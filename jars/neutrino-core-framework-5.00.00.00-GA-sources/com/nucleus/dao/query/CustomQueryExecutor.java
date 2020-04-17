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

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;

import com.nucleus.core.validation.util.NeutrinoValidator;
import com.nucleus.entity.Entity;

/**
 * Query executor class to facilitate fast and paginated retrieval of tabular data on UI.
 * @author Nucleus Software Exports Limited
 */
public class CustomQueryExecutor<T> extends HQLQueryExecutor<T> {

    // Entity property OGNL vs alias map.
    protected final String entityClassName;
    protected List<String> customClauses;
    protected final String alias;
    protected String       orderByClause;

    public CustomQueryExecutor(Class<? extends Entity> queryClass) {
        this(queryClass, null);
    }

    public CustomQueryExecutor(Class<? extends Entity> queryClass, String entityAlias) {
        Validate.notNull(queryClass, "Class for execution of query cannot be null");
        entityClassName = queryClass.getSimpleName();
        this.alias = entityAlias;
    }

    private String getQueryString() {
        StringBuilder sb = new StringBuilder();

        sb.append("from ").append(entityClassName).append(" ").append(getAliasName()).append(" ");

        if (customClauses != null) {
            for (String customClause : customClauses) {
                sb.append(customClause).append(" ");
            }
        }

        if (StringUtils.isNoneEmpty(orderByClause)) {
            sb.append(orderByClause).append(" ");
        }

        return sb.toString();
    }

    protected String getAliasName() {
        return StringUtils.isNotBlank(alias) ? alias : "";
    }

    public CustomQueryExecutor<T> addAndClause(String clause) {
        if (customClauses == null) {
            customClauses = new ArrayList<String>();
            customClauses.add(" where " + clause);
        } else {
            customClauses.add(" and " + clause);
        }
        return this;
    }

    public CustomQueryExecutor<T> addOrderByClause(String clause) {
        orderByClause = clause;
        return this;
    }

    public CustomQueryExecutor<T> addBoundParameter(String paramName, Object paramValue) {
        NeutrinoValidator.isTrue(StringUtils.isNotBlank(paramName), "Parameter name cannot be blank");
        NeutrinoValidator.notNull(paramValue, "param value Object cannot be null");
        // Adding param name to string pool in next line
        boundParameters.put(paramName.intern(), paramValue);
        return this;
    }

    public CustomQueryExecutor<T> addOrClause(String clause) {
        if (customClauses == null) {
            customClauses = new ArrayList<String>();
            customClauses.add(" where " + clause);
        } else {
            customClauses.add(" or " + clause);
        }
        return this;
    }

    @Override
    public Long executeTotalRowsQuery(EntityManager em) {
        return executeCountQuery(em, convertToCountQueryString(getQueryString()));
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<T> executeQuery(EntityManager em, Integer startIndex, Integer pageSize) {
        return (List<T>) executeQuery(em, getQueryString(), startIndex, pageSize);
    }

    /**
     * Add query hints
     * @param queryHintName
     * @param queryHintValue
     * @return
     */
    public CustomQueryExecutor<T> addQueryHint(String queryHintName, Object queryHintValue) {
        NeutrinoValidator.isTrue(StringUtils.isNotBlank(queryHintName), "Query Hint name cannot be blank");
        NeutrinoValidator.notNull(queryHintValue, "Query Hint value cannot be null");
        queryHints.put(queryHintName.intern(), queryHintValue);
        return this;
    }

}