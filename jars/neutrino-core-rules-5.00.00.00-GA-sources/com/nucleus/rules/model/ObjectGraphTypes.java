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
package com.nucleus.rules.model;

import javax.persistence.Cacheable;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;

import com.nucleus.master.BaseMasterEntity;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.nucleus.core.annotations.Sortable;
import com.nucleus.core.annotations.Synonym;
import com.nucleus.entity.BaseEntity;
import com.nucleus.entity.CloneOptions;

/**
 * 
 * @author Nucleus Software Exports Limited
 * Object Graph types
 */
@Entity
@DynamicUpdate
@DynamicInsert
@Cacheable
@Synonym(grant="ALL")
public class ObjectGraphTypes extends BaseMasterEntity {

    private static final long serialVersionUID = -119243391423862264L;

    @Sortable(index = 1)
    private String            displayName;

    private String            objectGraph;

    private String            sourceProduct;

    private String            description;

    @ManyToOne(fetch = FetchType.EAGER)
    private ModuleName        moduleName;

    @ManyToOne
    private ParameterDataType dataType;

    /**
     * 
     * getter for moduleName
     * @return
     */

    public ModuleName getModuleName() {
        return moduleName;
    }

    /**
     * 
     * setter for moduleName
     * @param moduleName
     */

    public void setModuleName(ModuleName moduleName) {
        this.moduleName = moduleName;
    }

    /**
     * 
     * getter for sourceProduct
     * @return
     */

    public String getSourceProduct() {
        return sourceProduct;
    }

    /**
     * 
     * setter for sourceProduct
     * @param sourceProduct
     */

    public void setSourceProduct(String sourceProduct) {
        this.sourceProduct = sourceProduct;
    }

    @Override
    public String getDisplayName() {
        return displayName;
    }

    /**
     * 
     * getter for description
     * @return
     */

    public String getDescription() {
        return description;
    }

    /**
     * 
     * setter for description
     * @param description
     */

    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * 
     * setter for display name
     * @param displayName
     */

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    /**
     * 
     * getter for objectGraph
     * @return
     */

    public String getObjectGraph() {
        return objectGraph;
    }

    /**
     * 
     * setter for objectGraph
     * @param objectGraph
     */

    public void setObjectGraph(String objectGraph) {
        this.objectGraph = objectGraph;
    }

    /**
     * 
     * getter for fataType
     * @return
     */

    public ParameterDataType getDataType() {
        return dataType;
    }

    /**
     * 
     * setter for dataType
     * @param dataType
     */

    public void setDataType(ParameterDataType dataType) {
        this.dataType = dataType;
    }

    @Override
    protected void populate(BaseEntity baseEntity, CloneOptions cloneOptions) {
        ObjectGraphTypes objectGraphTypes = (ObjectGraphTypes) baseEntity;
        super.populate(objectGraphTypes, cloneOptions);
        objectGraphTypes.setDisplayName(displayName);
        objectGraphTypes.setObjectGraph(objectGraph);
        objectGraphTypes.setDescription(description);
        objectGraphTypes.setSourceProduct(sourceProduct);
        objectGraphTypes.setDataType(dataType);
        objectGraphTypes.setModuleName(moduleName);
    }

    @Override
    protected void populateFrom(BaseEntity baseEntity, CloneOptions cloneOptions) {
        ObjectGraphTypes objectGraphTypes = (ObjectGraphTypes) baseEntity;
        super.populateFrom(objectGraphTypes, cloneOptions);
        this.setDisplayName(objectGraphTypes.getDisplayName());
        this.setObjectGraph(objectGraphTypes.getObjectGraph());
        this.setDescription(objectGraphTypes.getDescription());
        this.setSourceProduct(objectGraphTypes.getSourceProduct());
        this.setDataType(objectGraphTypes.getDataType());
        this.setModuleName(objectGraphTypes.getModuleName());
    }
}
