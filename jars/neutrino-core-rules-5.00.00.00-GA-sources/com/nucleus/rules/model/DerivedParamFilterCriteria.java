package com.nucleus.rules.model;

import javax.persistence.Cacheable;
import javax.persistence.Entity;
import javax.persistence.Transient;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.nucleus.core.annotations.Synonym;
import com.nucleus.entity.BaseEntity;
import com.nucleus.entity.CloneOptions;

@Entity
@DynamicUpdate
@DynamicInsert
@Cacheable
@Synonym(grant="ALL")
public class DerivedParamFilterCriteria extends BaseEntity {

    private static final long serialVersionUID = 1L;
    private String            collectionName;
    private String            whereExpression;
    private Integer           orderSequence;

    @Transient
    private String            whereExpressionInName;

    public String getCollectionName() {
        return collectionName;
    }

    public void setCollectionName(String collectionName) {
        this.collectionName = collectionName;
    }

    public String getWhereExpression() {
        return whereExpression;
    }

    public Integer getOrderSequence() {
        return orderSequence;
    }

    public void setOrderSequence(Integer orderSequence) {
        this.orderSequence = orderSequence;
    }

    public void setWhereExpression(String whereExpression) {
        this.whereExpression = whereExpression;
    }

    public String getWhereExpressionInName() {
        return whereExpressionInName;
    }

    public void setWhereExpressionInName(String whereExpressionInName) {
        this.whereExpressionInName = whereExpressionInName;
    }

    @Override
    protected void populate(BaseEntity baseEntity, CloneOptions cloneOptions) {
        DerivedParamFilterCriteria derivedParamFilterCriteria = (DerivedParamFilterCriteria) baseEntity;
        super.populate(derivedParamFilterCriteria, cloneOptions);
        derivedParamFilterCriteria.setCollectionName(collectionName);
        derivedParamFilterCriteria.setWhereExpression(whereExpression);
        derivedParamFilterCriteria.setOrderSequence(orderSequence);
    }

    @Override
    protected void populateFrom(BaseEntity baseEntity, CloneOptions cloneOptions) {
        DerivedParamFilterCriteria derivedParamFilterCriteria = (DerivedParamFilterCriteria) baseEntity;
        super.populateFrom(derivedParamFilterCriteria, cloneOptions);
        this.setCollectionName(derivedParamFilterCriteria.getCollectionName());
        this.setWhereExpression(derivedParamFilterCriteria.getWhereExpression());
        this.setOrderSequence(derivedParamFilterCriteria.getOrderSequence());

    }

}
