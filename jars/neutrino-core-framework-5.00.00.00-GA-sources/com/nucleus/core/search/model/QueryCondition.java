package com.nucleus.core.search.model;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.nucleus.core.annotations.Synonym;
import com.nucleus.entity.BaseEntity;

/**
 * Represents a Condition in the system.
 */
@Entity
@DynamicUpdate
@DynamicInsert
@Table(name = "QueryConditions")
@Inheritance(strategy = InheritanceType.JOINED)
@Synonym(grant="ALL")
public class QueryCondition extends BaseEntity {

    private static final long         serialVersionUID = 1;

    private String                    name;
    private String                    description;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "Queryconditionsentity")
    private List<QueryConditionExpression> expression;

    private String                    sourceProduct;

    public String getSourceProduct() {
        return sourceProduct;
    }

    public void setSourceProduct(String sourceProduct) {
        this.sourceProduct = sourceProduct;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public QueryConditionExpression getExpression() {
        if (expression == null || expression.size() == 0) {
            return null;
        }
        return expression.get(0);
    }

    public void setExpression(QueryConditionExpression expression) {
        this.expression = new ArrayList<QueryConditionExpression>();
        this.expression.add(expression);
    }

    public QueryCondition(String name, QueryConditionExpression expression) {
        this.name = name;
        this.expression = new ArrayList<QueryConditionExpression>();
        this.expression.add(expression);
    }

    public QueryCondition() {
    }

}