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

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.persistence.EntityManager;

import org.apache.commons.lang3.StringUtils;

import com.nucleus.core.validation.util.NeutrinoValidator;
import com.nucleus.entity.Entity;

/**
 * Query executor class to facilitate fast and paginated retrieval of tabular data on UI.
 * @author Nucleus Software Exports Limited
 */
public class MapQueryExecutor extends CustomQueryExecutor<Map<String, ?>> {

    // Entity property OGNL vs alias map.
    private final Map<String, String> selectedProperties = new LinkedHashMap<String, String>();

    public MapQueryExecutor(Class<? extends Entity> queryClass) {
        this(queryClass, null);
    }

    public MapQueryExecutor(Class<? extends Entity> queryClass, String entityAlias) {
        super(queryClass, entityAlias);
    }

    public MapQueryExecutor addQueryColumns(String... columnNames) {
        for (String columnName : columnNames) {
            String alias = columnName;
            if (columnName.contains(".")) {
                alias = columnName.replace(".", "");
            }
            selectedProperties.put(columnName, alias);

        }
        return this;
    }

    public MapQueryExecutor addColumn(String ognl, String alias) {
        selectedProperties.put(ognl, alias);
        return this;
    }

    private String getQueryString() {
        StringBuilder sb = new StringBuilder();
        sb.append("select new Map(");
        Iterator<Entry<String, String>> iterator = selectedProperties.entrySet().iterator();
        for ( ; iterator.hasNext() ;) {
            Entry<String, String> nextEntry = iterator.next();
            sb.append(nextEntry.getKey()).append(" as ").append(nextEntry.getValue());
            if (iterator.hasNext()) {
                sb.append(",");
            }
        }

        sb.append(") from ").append(entityClassName).append(" ").append(getAliasName()).append(" ");

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

    @Override
    public MapQueryExecutor addAndClause(String clause) {
        super.addAndClause(clause);
        return this;
    }

    @Override
    public MapQueryExecutor addOrderByClause(String clause) {
        super.addOrderByClause(clause);
        return this;
    }

    @Override
    public MapQueryExecutor addBoundParameter(String paramName, Object paramValue) {
        super.addBoundParameter(paramName, paramValue);
        return this;
    }

    @Override
    public MapQueryExecutor addOrClause(String clause) {
        super.addOrClause(clause);
        return this;
    }

    @Override
    public Long executeTotalRowsQuery(EntityManager em) {
        return executeCountQuery(em, convertToCountQueryString(getQueryString()));
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<Map<String, ?>> executeQuery(EntityManager em, Integer startIndex, Integer pageSize) {
        return (List<Map<String, ?>>) executeQuery(em, getQueryString(), startIndex, pageSize);
    }

    /**
     * Add query hints
     * @param queryHintName
     * @param queryHintValue
     * @return
     */
    
    public MapQueryExecutor addQueryHint(String queryHintName, Object queryHintValue) {
        NeutrinoValidator.isTrue(StringUtils.isNotBlank(queryHintName), "Query Hint name cannot be blank");
        NeutrinoValidator.notNull(queryHintValue, "Query Hint value cannot be null");
        queryHints.put(queryHintName.intern(), queryHintValue);
        return this;
    }

}