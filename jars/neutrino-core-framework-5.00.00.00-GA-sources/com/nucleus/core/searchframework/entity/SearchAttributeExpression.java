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

import javax.persistence.Entity;
import javax.persistence.Index;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

/**
 * @author Nucleus Software Exports Limited
 *
 */
@Entity
@DynamicUpdate
@DynamicInsert
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@Table(indexes={@Index(name="scc_entity_fk_index",columnList ="scc_entity")})
public class SearchAttributeExpression extends SearchExpressionTree {
    // ~ Static fields/initializers =================================================================

    private static final long       serialVersionUID = 1;

    // ~ Instance fields ============================================================================

    @OneToOne
    private SearchAttribute         searchAttribute;

    

    /**
     * @return the searchAttribute
     */
    public SearchAttribute getSearchAttribute() {
        return searchAttribute;
    }

    /**
     * @param searchAttribute the searchAttribute to set
     */
    public void setSearchAttribute(SearchAttribute searchAttribute) {
        this.searchAttribute = searchAttribute;
    }

    public SearchAttributeExpression(String op, SearchAttribute searchAttribute, SearchExpressionTree leftExp,
            SearchExpressionTree rightExp) {
        super(op, leftExp, rightExp);
        this.searchAttribute = searchAttribute;
    }

    public SearchAttributeExpression() {
        super();
    }

}
