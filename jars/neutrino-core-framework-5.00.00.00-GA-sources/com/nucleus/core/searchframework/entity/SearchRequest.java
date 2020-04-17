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

import java.util.List;

import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinTable;
import javax.persistence.OneToOne;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.nucleus.core.annotations.Synonym;
import com.nucleus.entity.BaseEntity;

/**
 * Search Request entity will hold information like the entity to be searched and the
 * Search criteria and the field list to be included in the resultset. 
 * @author Nucleus Software Exports Limited
 */

@Entity
@DynamicUpdate
@DynamicInsert
@Inheritance(strategy = InheritanceType.JOINED)
@Synonym(grant="ALL")
public class SearchRequest extends BaseEntity {

    // ~ Static fields/initializers =================================================================

    private static final long    serialVersionUID = 1;

    // ~ Instance fields ============================================================================

    private String               searchOn;

    @ElementCollection
    @JoinTable(name = "search_query_field")
    private List<String>         fieldList;

    @OneToOne
    private SearchCriteriaClause whereClause;

    @ElementCollection
    @JoinTable(name = "search_groupBy_field")
    private List<String>         groupByList;

    private String               searchType;

    /**
     * @return the searchOn
     */
    public String getSearchOn() {
        return searchOn;
    }

    /**
     * @return the fieldList
     */
    public List<String> getFieldList() {
        return fieldList;
    }

    /**
     * @return the whereClause
     */
    public SearchCriteriaClause getWhereClause() {
        return whereClause;
    }

    /**
     * @param searchOn the searchOn to set
     */
    public void setSearchOn(String searchOn) {
        this.searchOn = searchOn;
    }

    /**
     * @param fieldList the fieldList to set
     */
    public void setFieldList(List<String> fieldList) {
        this.fieldList = fieldList;
    }

    /**
     * @param whereClause the whereClause to set
     */
    public void setWhereClause(SearchCriteriaClause whereClause) {
        this.whereClause = whereClause;
    }

    /**
     * @return the groupByList
     */
    public List<String> getGroupByList() {
        return groupByList;
    }

    /**
     * @param groupByList the groupByList to set
     */
    public void setGroupByList(List<String> groupByList) {
        this.groupByList = groupByList;
    }

    /**
     * @return the searchType
     */
    public String getSearchType() {
        return searchType;
    }

    /**
     * @param searchType the searchType to set
     */
    public void setSearchType(String searchType) {
        this.searchType = searchType;
    }

}
