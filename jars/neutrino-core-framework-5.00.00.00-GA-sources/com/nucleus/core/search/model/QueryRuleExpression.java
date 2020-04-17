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
 * Represents a Rule Expression in the system.
 */
@Entity
@DynamicUpdate
@DynamicInsert
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@Table(indexes={@Index(name="ruleentity_index",columnList="ruleentity")})
public class QueryRuleExpression extends QueryExpressionTree {

    private static final long serialVersionUID = 1;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    private QueryCondition    queryConditions;

    public QueryCondition getQueryConditions() {
        return queryConditions;
    }

    public void setQueryConditions(QueryCondition queryConditions) {
        this.queryConditions = queryConditions;
    }

    public QueryRuleExpression(String op, QueryCondition conditions, QueryExpressionTree leftExp, QueryExpressionTree rightExp) {
        super(op, leftExp, rightExp);
        this.queryConditions = conditions;
    }

    public QueryRuleExpression() {
        super();

    }

}