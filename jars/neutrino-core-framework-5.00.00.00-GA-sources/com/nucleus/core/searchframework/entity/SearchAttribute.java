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
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.nucleus.core.annotations.Synonym;
import com.nucleus.entity.BaseEntity;

/**
 * 
 * @author Nucleus Software Exports Limited
 */
@Entity
@DynamicUpdate
@DynamicInsert
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@Synonym(grant="ALL")
public class SearchAttribute extends BaseEntity {

    // ~ Static fields/initializers =================================================================

    private static final long serialVersionUID = 1;

    // ~ Instance fields ============================================================================

    private String            name;

    private String            description;

    private Integer           dataType;

    private String            sourceProduct;

    /**
     * 
     * Default Constructor
     */
    public SearchAttribute() {

    }

    /**
     * 
     * Getter for sourceProduct
     * @return
     */
    public String getSourceProduct() {
        return sourceProduct;
    }

    /**
     * 
     * Setter for sourceProduct Property
     * @param sourceProduct
     */
    public void setSourceProduct(String sourceProduct) {
        this.sourceProduct = sourceProduct;
    }

    /**
     * Getter for Description
     * @return
     */
    public String getDescription() {
        return description;
    }

    /**
     * Setter for Description
     * @param
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * 
     * Getter for Name Property
     * @return
     */
    public String getName() {
        return name;
    }

    /**
     * 
     * Getter for data Type Property
     * @return
     */
    public Integer getDataType() {
        return dataType;
    }

    /**
     * 
     * Setter for Name Property
     * @param name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * 
     * Setter for data Type Property
     * @param dataType
     */
    public void setDataType(Integer dataType) {
        this.dataType = dataType;
    }

}
