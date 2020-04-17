package com.nucleus.rules.model;

import java.io.Serializable;


public class ParameterSimilarVO implements Serializable {

    private String parameterCode;
    private int paramType;
    private String dataType;
    private String literal;
    private String objectGraph;
    private String parameterExpression;
    private String contextName;
    private String referenceEntityName;
    private String referenceValue;
    private String queryParameterNames;
    private String parameterNames;
    private String query;

    public String getParameterCode() {
        return parameterCode;
    }

    public void setParameterCode(String parameterCode) {
        this.parameterCode = parameterCode;
    }

    public String getDataType() {
        return dataType;
    }

    public void setDataType(String dataType) {
        this.dataType = dataType;
    }

    public String getLiteral() {
        return literal;
    }

    public void setLiteral(String literal) {
        this.literal = literal;
    }

    public String getObjectGraph() {
        return objectGraph;
    }

    public void setObjectGraph(String objectGraph) {
        this.objectGraph = objectGraph;
    }

    public String getParameterExpression() {
        return parameterExpression;
    }

    public void setParameterExpression(String parameterExpression) {
        this.parameterExpression = parameterExpression;
    }

    public String getContextName() {
        return contextName;
    }

    public void setContextName(String contextName) {
        this.contextName = contextName;
    }

    public String getReferenceEntityName() {
        return referenceEntityName;
    }

    public void setReferenceEntityName(String referenceEntityName) {
        this.referenceEntityName = referenceEntityName;
    }

    public int getParamType() {
        return paramType;
    }

    public void setParamType(int paramType) {
        this.paramType = paramType;
    }

    public String getReferenceValue() {
        return referenceValue;
    }

    public void setReferenceValue(String referenceValue) {
        this.referenceValue = referenceValue;
    }

    public String getQueryParameterNames() {
        return queryParameterNames;
    }

    public void setQueryParameterNames(String queryParameterNames) {
        this.queryParameterNames = queryParameterNames;
    }

    public String getParameterNames() {
        return parameterNames;
    }

    public void setParameterNames(String parameterNames) {
        this.parameterNames = parameterNames;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }
}
