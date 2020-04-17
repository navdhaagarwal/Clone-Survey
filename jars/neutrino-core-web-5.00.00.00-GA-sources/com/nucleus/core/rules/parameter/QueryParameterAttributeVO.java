package com.nucleus.core.rules.parameter;

import java.io.Serializable;


public class QueryParameterAttributeVO implements Serializable{

    private String            queryParameterName;

    public String             objectGraph;

    public String getQueryParameterName() {
        return queryParameterName;
    }

    public void setQueryParameterName(String queryParameterName) {
        this.queryParameterName = queryParameterName;
    }

    public String getObjectGraph() {
        return objectGraph;
    }

    public void setObjectGraph(String objectGraph) {
        this.objectGraph = objectGraph;
    }
}
