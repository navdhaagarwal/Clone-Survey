/**
 * Copyright Nucleus Software India Pvt Ltd. All rights reserved.
 */
package com.nucleus.dao.query;

class StringParameterBuilder extends ParameterBuilder {

    public StringParameterBuilder(String paramName, String paramValue, boolean like) {
        super(paramName, paramValue);
        if (like) {
            setParamValue((String) getParamValue() + "%");
        }
    }

}
