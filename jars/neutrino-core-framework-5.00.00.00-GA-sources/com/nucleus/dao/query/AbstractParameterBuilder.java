/**
 * Copyright Nucleus Software India Pvt Ltd. All rights reserved.
 */
package com.nucleus.dao.query;

import javax.persistence.Query;

import org.apache.commons.lang3.StringUtils;

import com.nucleus.core.validation.util.NeutrinoValidator;

class ParameterBuilder implements QueryParameterBuilder {

    private String paramName;
    private Object paramValue;

    public ParameterBuilder(String paramName, Object paramValue) {
        this.paramName = paramName;
        this.paramValue = paramValue;
        validate();
    }

    private void validate() {
        NeutrinoValidator.isTrue(StringUtils.isNotBlank(paramName), "Parameter name cannot be blank");
        NeutrinoValidator.notNull(paramValue, "Parameter value cannot be null");
    }

    public Object getParamValue() {
        return paramValue;
    }

    public String getParamName() {
        return paramName;
    }

    /* (non-Javadoc) @see com.nucleus.dao.query.QueryParameterBuilder#setParameterIntoQuery(javax.persistence.Query) */
    @Override
    public void setParameterIntoQuery(Query query) {
        query.setParameter(paramName, paramValue);
    }

    public void setParamName(String paramName) {
        this.paramName = paramName;
    }

    public void setParamValue(Object paramValue) {
        this.paramValue = paramValue;
    }

}
