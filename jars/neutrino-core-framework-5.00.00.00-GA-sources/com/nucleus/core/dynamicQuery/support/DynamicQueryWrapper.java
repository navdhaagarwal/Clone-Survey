/**
 * This file and a proportion of its content is copyright of Nucleus Software Exports Limited - © 2012. All rights reserved.
 * Any redistribution or reproduction of part or all of the contents in any form is prohibited other than the following:
 * - you cannot print or download to a local hard disk extract contents either part or full for personal/ commercial/
 * academic or any other use
 * - you may not copy the content to individual/ third parties for any type of use, either as compiled or source format
 * without the knowledge and consent of Nucleus Software
 * - You may not, except with our express written permission, distribute or commercially exploit the content. Nor may you
 * transmit it or store it in any other web site or other form of electronic retrieval system.
 */
package com.nucleus.core.dynamicQuery.support;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import com.nucleus.core.dynamicQuery.entity.QueryToken;
import com.nucleus.dao.query.JPAQueryExecutor;
import com.nucleus.dao.query.QueryExecutor;

/**
 * @author Nucleus Software Exports Limited
 *
 */
public class DynamicQueryWrapper implements Serializable {

    private static final long     serialVersionUID             = 5972793832642984280L;

    private String                hqlQueryString;
    private String                hqlMapQueryString;
    private Map<String, Object>   hqlQueryNamedParameters      = new HashMap<String, Object>();

    // for running report as job we need to resolve date-time parameters later
    private Map<String, String>   hqlQueryParametersUnresolved = new HashMap<String, String>();

    private Map<Long, QueryToken> selectedTokens               = new HashMap<Long, QueryToken>();

    public DynamicQueryWrapper(String hqlQueryString, Map<String, Object> hqlQueryNamedParameters,
            Map<Long, QueryToken> selectedTokens) {
        super();
        this.hqlQueryString = hqlQueryString;
        this.hqlQueryNamedParameters = hqlQueryNamedParameters;
        this.selectedTokens = selectedTokens;
    }

    public String getHqlQueryString() {
        return hqlQueryString;
    }

    public void setHqlQueryString(String hqlQueryString) {
        this.hqlQueryString = hqlQueryString;
    }

    public Map<String, Object> getHqlQueryNamedParameters() {
        return hqlQueryNamedParameters;
    }

    public void setHqlQueryNamedParameters(Map<String, Object> hqlQueryNamedParameters) {
        this.hqlQueryNamedParameters = hqlQueryNamedParameters;
    }

    public QueryExecutor<Object> getRawQueryExecuterWithAllParameterAdded() {

        JPAQueryExecutor<Object> jpaQueryExecutor = new JPAQueryExecutor<Object>(getHqlQueryString());
        if (hqlQueryNamedParameters != null && !hqlQueryNamedParameters.isEmpty()) {
            for (String param : hqlQueryNamedParameters.keySet()) {
                jpaQueryExecutor.addParameter(param, hqlQueryNamedParameters.get(param));
            }
        }
        return jpaQueryExecutor;
    }

    public QueryExecutor<Map<String, Object>> getMapQueryExecuterWithAllParameterAdded() {

        JPAQueryExecutor<Map<String, Object>> jpaQueryExecutor = new JPAQueryExecutor<Map<String, Object>>(
                getHqlMapQueryString());
        if (hqlQueryNamedParameters != null && !hqlQueryNamedParameters.isEmpty()) {
            for (String param : hqlQueryNamedParameters.keySet()) {
                jpaQueryExecutor.addParameter(param, hqlQueryNamedParameters.get(param));
            }
        }
        return jpaQueryExecutor;
    }

    public String getHqlMapQueryString() {
        return hqlMapQueryString;
    }

    public void setHqlMapQueryString(String hqlMapQueryString) {
        this.hqlMapQueryString = hqlMapQueryString;
    }

    public Map<Long, QueryToken> getSelectedTokens() {
        return selectedTokens;
    }

    public void setHqlQueryParametersUnresolved(Map<String, String> hqlQueryParametersUnresolved) {
        this.hqlQueryParametersUnresolved = hqlQueryParametersUnresolved;
    }

    public Map<String, String> getHqlQueryParametersUnresolved() {
        return hqlQueryParametersUnresolved;
    }

}
