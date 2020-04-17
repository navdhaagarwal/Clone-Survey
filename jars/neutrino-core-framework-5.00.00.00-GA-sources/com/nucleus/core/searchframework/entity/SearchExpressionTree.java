/**
 * This file and a proportion of its content is copyright of Nucleus Software Exports Limited - � 2012. All rights reserved.
 * Any redistribution or reproduction of part or all of the contents in any form is prohibited other than the following:
 * - you cannot print or download to a local hard disk extract contents either part or full for personal/ commercial/
 * academic or any other use
 * - you may not copy the content to individual/ third parties for any type of use, either as compiled or source format
 * without the knowledge and consent of Nucleus Software
 * - You may not, except with our express written permission, distribute or commercially exploit the content. Nor may you
 * transmit it or store it in any other web site or other form of electronic retrieval system.
 */
package com.nucleus.core.searchframework.entity;

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
 * @author Nucleus Software Exports Limited
 * 
 */
@Entity
@DynamicUpdate
@DynamicInsert
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@Synonym(grant="ALL")
public class SearchExpressionTree extends BaseEntity {

    // ~ Static fields/initializers =================================================================

    private static final long    serialVersionUID = 1;

    // ~ Instance fields ============================================================================

    private String               operator;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private SearchExpressionTree leftExpression;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private SearchExpressionTree rightExpression;

    /**
     * Constructor 
     */
    public SearchExpressionTree() {
    }

    public String getOperator() {
        return operator;
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }

    public SearchExpressionTree getLeftExpression() {
        return leftExpression;
    }

    public void setLeftExpression(SearchExpressionTree leftExpression) {
        this.leftExpression = leftExpression;
    }

    public SearchExpressionTree getRightExpression() {
        return rightExpression;
    }

    public void setRightExpression(SearchExpressionTree rightExpression) {
        this.rightExpression = rightExpression;
    }

    public SearchExpressionTree(String op, SearchExpressionTree leftExp, SearchExpressionTree rightExp) {
        this.operator = op;
        this.leftExpression = leftExp;
        this.rightExpression = rightExp;
    }
}
