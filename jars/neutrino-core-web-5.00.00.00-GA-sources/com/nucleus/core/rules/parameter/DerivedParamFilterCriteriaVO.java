package com.nucleus.core.rules.parameter;

import java.io.Serializable;


public class DerivedParamFilterCriteriaVO implements Serializable{

    private String            collectionName;
    private String            whereExpression;
    private Integer           orderSequence;

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
