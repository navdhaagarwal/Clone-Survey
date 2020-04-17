/**
 * This file and a proportion of its content is copyright of Nucleus Software Exports Limited - � 2012. All rights reserved.
 * Any redistribution or reproduction of part or all of the contents in any form is prohibited other than the following:
 * - you cannot print or download to a local hard disk extract contents either part or full for personal/ commercial/
 * academic or any other use
 * - you may not copy the content to individual/ third parties for any type of use, either as compiled or source format
 * without the knowledge and consent of Nucleus SOftware
 * - You may not, except with our express written permission, distribute or commercially exploit the content. Nor may you
 * transmit it or store it in any other web site or other form of electronic retrieval system.
 */
package com.nucleus.core.search.model;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.nucleus.core.annotations.Synonym;
import com.nucleus.core.genericparameter.entity.GenericParameter;
import com.nucleus.entity.BaseEntity;
import com.nucleus.entity.CloneOptions;

/**
 * 
 * @author Nucleus Software Exports Limited
 * TODO -> ruchir.sachdeva Add documentation to class
 */
@Entity
@DynamicUpdate
@DynamicInsert
@Synonym(grant="ALL")
public class QueryObjectGraphTypes extends BaseEntity {

    private static final long serialVersionUID = -119243391423862264L;

    private String            displayName;

    private String            objectGraph;

    private String            sourceProduct;

    private String            fieldName;

    private String            selectedFieldDisplayType;
    
    //This field is used to determine parent Class of OGNL
    private String            searchLevel;

    public String getSelectedFieldDisplayType() {
        return selectedFieldDisplayType;
    }

    public void setSelectedFieldDisplayType(String selectedFieldDisplayType) {
        this.selectedFieldDisplayType = selectedFieldDisplayType;
    }

    @ManyToOne
    private GenericParameter dataType;

    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    public String getSourceProduct() {
        return sourceProduct;
    }

    public void setSourceProduct(String sourceProduct) {
        this.sourceProduct = sourceProduct;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getObjectGraph() {
        return objectGraph;
    }

    public void setObjectGraph(String objectGraph) {
        this.objectGraph = objectGraph;
    }

    public GenericParameter getDataType() {
        return dataType;
    }

    public void setDataType(GenericParameter dataType) {
        this.dataType = dataType;
    }

    @Override
    protected void populate(BaseEntity baseEntity, CloneOptions cloneOptions) {
        QueryObjectGraphTypes objectGraphTypes = (QueryObjectGraphTypes) baseEntity;
        super.populate(objectGraphTypes, cloneOptions);
        objectGraphTypes.setDisplayName(displayName);
        objectGraphTypes.setObjectGraph(objectGraph);
        objectGraphTypes.setSourceProduct(sourceProduct);
        objectGraphTypes.setFieldName(fieldName);
        objectGraphTypes.setDataType(dataType);
    }

    /**
     * @return the searchLevel
     */
    public String getSearchLevel() {
        return searchLevel;
    }

    /**
     * @param searchLevel the searchLevel to set
     */
    public void setSearchLevel(String searchLevel) {
        this.searchLevel = searchLevel;
    }
}
