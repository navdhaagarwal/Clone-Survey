/**
 * This file and a proportion of its content is copyright of Nucleus Software Exports Limited - � 2012. All rights reserved.
 * Any redistribution or reproduction of part or all of the contents in any form is prohibited other than the following:
 * - you cannot print or download to a local hard disk extract contents either part or full for personal/ commercial/
 * academic or any other use
 * - you may not copy the content to individual/ third parties for any type of use, either as compiled or source format
 * without the knowledge and consent of Nucleus SOftware
 * - You may not, except with our express written permission, distribute or commercially exploit the content. Nor may you
 * transmit it or store it in any other web site or other form of electronic retrieval system.
 */
package com.nucleus.dao.query;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import com.nucleus.persistence.DaoUtils;

/**
 * @author Nucleus Software Exports Limited
 */
public abstract class HQLQueryExecutor<T> implements QueryExecutor<T> {

    protected Map<String, Object> boundParameters = new LinkedHashMap<String, Object>();
    protected Map<String, Object> queryHints = new LinkedHashMap<String, Object>();

    protected List<?> executeQuery(EntityManager em, String queryString, Integer startIndex, Integer pageSize) {
        Query query = em.createQuery(queryString);
        addAllParametersIntoQuery(query);
        addAllHintsIntoQuery(query);
        if (startIndex != null && pageSize != null) {
            query.setFirstResult(startIndex);
            query.setMaxResults(pageSize);
        }
        List<?> entities = DaoUtils.executeQuery(em, query);
        return entities;
    }

    protected Long executeCountQuery(EntityManager em, String queryString) {
        Query query = em.createQuery(queryString);
        addAllParametersIntoQuery(query);
        addAllHintsIntoQuery(query);
        return (Long) query.getSingleResult();
    }

    protected void addAllParametersIntoQuery(Query query) {
        for (Entry<String, Object> parameter : boundParameters.entrySet()) {
            query.setParameter(parameter.getKey(), parameter.getValue());
        }
    }
    
    protected void addAllHintsIntoQuery(Query query) {
        for (Entry<String, Object> parameter : queryHints.entrySet()) {
            query.setHint(parameter.getKey(), parameter.getValue());
        }
    }

    protected String convertToCountQueryString(String queryString) {
        return queryString.replaceFirst("^.*?(?i)from", "select count (*) from ")
        		.replaceAll("(?i)[\\s]+order[\\s]+by[\\s]+[a-zA-Z_0-9,\\.\\s\\(\\)]*","");
    }

}