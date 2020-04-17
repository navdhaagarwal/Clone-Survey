package com.nucleus.dao.query;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;

import org.apache.commons.lang3.StringUtils;

import com.nucleus.core.validation.util.NeutrinoValidator;

/**
 * This class enables us to quickly build query criteria with query string which can be used to 
 * fire queries using generic DAO methods.
 *  
 * @author Nucleus Software India Pvt Ltd
 * 
 * @param <T>
 */
public class RuleQueryExecutor extends HQLQueryExecutor<Object> {

    private final String query;

    /**
     * @param query
     */

    public RuleQueryExecutor(String query) {
        NeutrinoValidator.isTrue(StringUtils.isNotBlank(query), "Name of the named query cannot be blank");
        this.query = query;
    }

    /**
     * @param query
     * @param boundParameters
     */

    public RuleQueryExecutor(String query, Map<String, Object> boundParameters) {
        NeutrinoValidator.isTrue(StringUtils.isNotBlank(query), "Name of the named query cannot be blank");
        this.query = query;
        this.boundParameters = boundParameters;
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<Object> executeQuery(EntityManager em, Integer startIndex, Integer pageSize) {
        return (List<Object>) super.executeQuery(em, query, startIndex, pageSize);

    }

    @Override
    public Long executeTotalRowsQuery(EntityManager em) {
        return super.executeCountQuery(em, query);
    }

    /**
     * Bind the parameterName and value and set it in the parameter map.  
     * @param parameterName
     * @param value
     */
    public void addQueryParameter(String parameterName, Object value) {
        if (boundParameters == null) {
            boundParameters = new LinkedHashMap<String, Object>();
        }

        boundParameters.put(parameterName, value);
    }
}
