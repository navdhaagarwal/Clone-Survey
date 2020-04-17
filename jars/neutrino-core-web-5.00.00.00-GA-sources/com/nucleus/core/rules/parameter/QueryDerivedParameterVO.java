package com.nucleus.core.rules.parameter;

import com.nucleus.rules.model.Parameter;

import java.io.Serializable;


public class QueryDerivedParameterVO implements Serializable{

    private String            queryParameterName;

    public String             queryObjectGraph;

    private String            collectionName;

    private String            whereExpression;

    private Integer           orderSequence;

    private Integer seq;

    private String placeHolderName;

    private Parameter parameter;

    public Integer getSeq() {
        return seq;
    }

    public void setSeq(Integer seq) {
        this.seq = seq;
    }

    public String getPlaceHolderName() {
        return placeHolderName;
    }

    public void setPlaceHolderName(String placeHolderName) {
        this.placeHolderName = placeHolderName;
    }

    public Parameter getParameter() {
        return parameter;
    }

    public void setParameter(Parameter parameter) {
        this.parameter = parameter;
    }

    public String getQueryParameterName() {
        return queryParameterName;
    }

    public void setQueryParameterName(String queryParameterName) {
        this.queryParameterName = queryParameterName;
    }

    public String getQueryObjectGraph() {
        return queryObjectGraph;
    }

    public void setQueryObjectGraph(String queryObjectGraph) {
        this.queryObjectGraph = queryObjectGraph;
    }

    public String getCollectionName() {
        return collectionName;
    }

    public void setCollectionName(String collectionName) {
        this.collectionName = collectionName;
    }

    public String getWhereExpression() {
        return whereExpression;
    }

    public void setWhereExpression(String whereExpression) {
        this.whereExpression = whereExpression;
    }

    public Integer getOrderSequence() {
        return orderSequence;
    }

    public void setOrderSequence(Integer orderSequence) {
        this.orderSequence = orderSequence;
    }
}
