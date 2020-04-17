package com.nucleus.core.search.model;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Index;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

/**
 * Represents a Condition Expression in the system.
 */
@Entity
@DynamicUpdate
@DynamicInsert
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@Table(indexes = {@Index(name="question_cond_fk_index",columnList="Queryconditionsentity")})
public class QueryConditionExpression extends QueryExpressionTree {
    private static final long serialVersionUID = 1;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    private QueryParam    queryParameter;

    public QueryConditionExpression(String op, QueryParam queryParameter, QueryExpressionTree leftExp, QueryExpressionTree rightExp) {
        super(op, leftExp, rightExp);
        this.queryParameter = queryParameter;
    }

    public QueryConditionExpression() {
        super();
    }

    public QueryParam getQueryParameter() {
        return queryParameter;
    }

    public void setQueryParameter(QueryParam queryParameter) {
        this.queryParameter = queryParameter;
    }

}