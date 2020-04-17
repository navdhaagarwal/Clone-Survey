package com.nucleus.core.search.model;

import javax.persistence.Entity;
import javax.persistence.OneToOne;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

@Entity
@DynamicUpdate
@DynamicInsert
public class QueryParameterExpression extends QueryExpressionTree {

    private static final long serialVersionUID = 1;

    @OneToOne
    private QueryParam        parameter;

    public QueryParam getParameter() {
        return parameter;
    }

    public void setParameter(QueryParam parameter) {
        this.parameter = parameter;
    }

    public QueryParameterExpression(String op, QueryParam parameter, QueryExpressionTree leftExp,
            QueryExpressionTree rightExp) {
        super(op, leftExp, rightExp);
        this.parameter = parameter;
    }

    public QueryParameterExpression() {
        super();
    }

}
