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

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.nucleus.core.annotations.Synonym;
import com.nucleus.entity.BaseEntity;

/**
 * Entity to define the Criteria clause of the SearchRequest, it will hold the expression of the Search Attributes 
 * @author Nucleus Software Exports Limited
 */
@Entity
@DynamicUpdate
@DynamicInsert
@Inheritance(strategy = InheritanceType.JOINED)
@Synonym(grant="ALL")
public class SearchCriteriaClause extends BaseEntity {

    // ~ Static fields/initializers =================================================================

    private static final long         serialVersionUID = 1;

    // ~ Instance fields ============================================================================

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "scc_entity")
    private List<SearchAttributeExpression> searchAttributeExpression;
    
    /**
     * @return the searchParamExp
     */
    public SearchAttributeExpression getSearchAttributeExpression() {
        if (searchAttributeExpression == null || searchAttributeExpression.size() == 0) {
            return null;
        }
        return searchAttributeExpression.get(0);
    }

    /**
     * @param searchAttributeExpression the searchParamExp to set
     */
    public void setSearchAttributeExpression(SearchAttributeExpression searchAttributeExpression) {
        if (this.searchAttributeExpression == null || this.searchAttributeExpression.size() == 0) {
            this.searchAttributeExpression = new ArrayList<SearchAttributeExpression>();
        } else {
            this.searchAttributeExpression.remove(0);
        }
        this.searchAttributeExpression.add(searchAttributeExpression);
    }

    

}
