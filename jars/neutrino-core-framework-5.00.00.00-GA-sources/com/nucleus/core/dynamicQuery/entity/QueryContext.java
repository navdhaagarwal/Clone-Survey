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
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;

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
public class QueryContext extends BaseEntity {

    private static final long serialVersionUID = -1815850927499780862L;

    private String            queryCode;

    private String            rootEntity;
    private String            idPropertyName;

    private String            constraintPropertyPath;
    private String            constraintValue;
    private String            constraintValueType;

    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "query_context")
    private List<QueryToken>  queryTokens;

    public String getRootEntity() {
        return rootEntity;
    }

    public void setRootEntity(String rootEntity) {
        this.rootEntity = rootEntity;
    }

    public List<QueryToken> getQueryTokens() {
        return queryTokens;
    }

    public void setQueryTokens(List<QueryToken> queryTokens) {
        this.queryTokens = queryTokens;
    }

    public String getQueryCode() {
        return queryCode;
    }

    public void setQueryCode(String queryCode) {
        this.queryCode = queryCode;
    }

    public String getRootEntityAlias() {

        if (rootEntity != null && rootEntity.length() > 1) {
            char[] sourceArray = rootEntity.toCharArray();
            char[] finalArray = new char[rootEntity.length()];
            finalArray[0] = Character.toLowerCase(sourceArray[0]);
            System.arraycopy(sourceArray, 1, finalArray, 1, sourceArray.length - 1);

            return new String(finalArray);
        }
        return rootEntity;
    }

    public String getConstraintPropertyPath() {
        return constraintPropertyPath;
    }

    public void setConstraintPropertyPath(String constraintPropertyPath) {
        this.constraintPropertyPath = constraintPropertyPath;
    }

    public String getConstraintValue() {
        return constraintValue;
    }

    public void setConstraintValue(String constraintValue) {
        this.constraintValue = constraintValue;
    }

    public String getConstraintValueType() {
        return constraintValueType;
    }

    public void setConstraintValueType(String constraintValueType) {
        this.constraintValueType = constraintValueType;
    }

    public String getIdPropertyName() {
        return idPropertyName;
    }

    public void setIdPropertyName(String idPropertyName) {
        this.idPropertyName = idPropertyName;
    }

}
