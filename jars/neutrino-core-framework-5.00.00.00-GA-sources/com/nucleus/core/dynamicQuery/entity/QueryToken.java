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
package com.nucleus.core.dynamicQuery.entity;

import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

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
@Synonym(grant="ALL")
@Table(indexes={@Index(name="query_context_index",columnList="query_context")})
public class QueryToken extends BaseEntity {

    public static final String    STRING           = "string";
    public static final String    NUMBER           = "number";
    public static final String    DATE_TIME        = "dateTime";
    public static final String    LOCAL_DATE       = "localDate";
    public static final String    CALENDAR         = "calendar";
    public static final String    BOOLEAN          = "boolean";
    public static final String    FLOAT            = "float";
    public static final String    MONEY            = "money";

    public static final int       SELECT_TYPE      = 0;
    public static final int       WHERE_TYPE       = 1;
    public static final int       BOTH             = 2;
    public static final int       UNKNOWN_TYPE     = 3;

    private static final long     serialVersionUID = 5049515474528809440L;

    private String                propertyName;
    private String                propertyEntityName;

    private String                tokenName;
    private String                tokenProertyName;
    private int                   tokenType;

    private String                valueDisplayType;
    private String                valueActualType;
    private String                fetchEntity;
    private String                fetchColumn;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name = "query_token")
    private List<QueryTokenValue> queryTokenValues;

    @ManyToOne(cascade = CascadeType.ALL)
    private QueryToken            owner;

    public String getPropertyName() {
        return propertyName;
    }

    public void setPropertyName(String propertyName) {
        this.propertyName = propertyName;
    }

    public String getPropertyEntityName() {
        return propertyEntityName;
    }

    public void setPropertyEntityName(String propertyEntityName) {
        this.propertyEntityName = propertyEntityName;
    }

    public String getTokenName() {
        return tokenName;
    }

    public void setTokenName(String tokenName) {
        this.tokenName = tokenName;
    }

    public String getTokenProertyName() {
        return tokenProertyName;
    }

    public void setTokenProertyName(String tokenProertyName) {
        this.tokenProertyName = tokenProertyName;
    }

    public QueryToken getOwner() {
        return owner;
    }

    public void setOwner(QueryToken owner) {
        this.owner = owner;
    }

    public String getAlias() {

        // alias is same as token name for a token
        if (isToken()) {
            return tokenName;
        }

        if (propertyEntityName != null && propertyEntityName.length() > 1) {
            char[] sourceArray = propertyEntityName.toCharArray();
            char[] finalArray = new char[propertyEntityName.length()];
            finalArray[0] = Character.toLowerCase(sourceArray[0]);
            System.arraycopy(sourceArray, 1, finalArray, 1, sourceArray.length - 1);

            return new String(finalArray);
        }
        return propertyEntityName;
    }

    public List<QueryTokenValue> getQueryTokenValues() {
        return queryTokenValues;
    }

    public void setQueryTokenValues(List<QueryTokenValue> queryTokenValues) {
        this.queryTokenValues = queryTokenValues;
    }

    public String getValueDisplayType() {
        return valueDisplayType;
    }

    public void setValueDisplayType(String valueDisplayType) {
        this.valueDisplayType = valueDisplayType;
    }

    public String getValueActualType() {

        if (valueActualType == null || valueActualType.isEmpty()) {
            return valueDisplayType;
        }
        return valueActualType;
    }

    public void setValueActualType(String valueActualType) {
        this.valueActualType = valueActualType;
    }

    public String getFetchEntity() {
        return fetchEntity;
    }

    public void setFetchEntity(String fetchEntity) {
        this.fetchEntity = fetchEntity;
    }

    public String getFetchColumn() {
        return fetchColumn;
    }

    public void setFetchColumn(String fetchColumn) {
        this.fetchColumn = fetchColumn;
    }

    // ===========
    public boolean isCrossToken() {
        if ((tokenProertyName == null || tokenProertyName.trim().isEmpty()) && isToken()) {
            return true;
        }
        return false;
    }

    public boolean isFetchValues() {
        if (!(valueDisplayType.equalsIgnoreCase(DATE_TIME) || valueDisplayType.equalsIgnoreCase(BOOLEAN)
                || valueDisplayType.equalsIgnoreCase(LOCAL_DATE) || valueDisplayType.equalsIgnoreCase(CALENDAR))
                && (queryTokenValues == null || queryTokenValues.isEmpty())) {
            return true;
        }
        return false;
    }

    public int getTokenType() {
        return tokenType;
    }

    public void setTokenType(int tokenType) {
        this.tokenType = tokenType;
    }

    public boolean isEntityTypeProperty() {
        if ((propertyEntityName != null && !propertyEntityName.trim().isEmpty())) {
            return true;
        }
        return false;
    }

    public boolean isToken() {
        if ((propertyName == null || propertyName.trim().isEmpty())
                && (propertyEntityName == null || propertyEntityName.trim().isEmpty())) {
            return true;
        }
        return false;
    }

}
