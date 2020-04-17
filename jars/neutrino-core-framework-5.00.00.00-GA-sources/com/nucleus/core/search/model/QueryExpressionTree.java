package com.nucleus.core.search.model;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.OneToOne;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.nucleus.core.annotations.Synonym;
import com.nucleus.entity.BaseEntity;

/**
 * Represents a Expression in the system.
 */
@Entity
@DynamicUpdate
@DynamicInsert
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@Synonym(grant="ALL")
public class QueryExpressionTree extends BaseEntity {
    private static final long   serialVersionUID = 1;

    private String              operator;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private QueryExpressionTree leftExpression;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private QueryExpressionTree rightExpression;

    public String getOperator() {
        return operator;
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }

    public QueryExpressionTree getLeftExpression() {
        return leftExpression;
    }

    public void setLeftExpression(QueryExpressionTree leftExpression) {
        this.leftExpression = leftExpression;
    }

    public QueryExpressionTree getRightExpression() {
        return rightExpression;
    }

    public void setRightExpression(QueryExpressionTree rightExpression) {
        this.rightExpression = rightExpression;
    }

    public QueryExpressionTree() {
    }

    public QueryExpressionTree(String op, QueryExpressionTree leftExp, QueryExpressionTree rightExp) {
        this.operator = op;
        this.leftExpression = leftExp;
        this.rightExpression = rightExp;

    }
}