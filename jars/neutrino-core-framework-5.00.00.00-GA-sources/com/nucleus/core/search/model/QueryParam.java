package com.nucleus.core.search.model;

import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.nucleus.core.annotations.Synonym;
import com.nucleus.entity.BaseEntity;

/**
 * Represents a Condition Expression in the system.
 */
@Entity
@DynamicUpdate
@DynamicInsert
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@Synonym(grant="ALL")
public class QueryParam extends BaseEntity {

    private static final long serialVersionUID = 1;

    private String            name;

    private String            description;

    private Integer           dataType;

    private Integer           paramType;

    private String            sourceProduct;

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
     * 
     * Default Constructor
     */
    public QueryParam() {

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
     * Getter for Parameter Type Property
     * @return
     */
    public Integer getParamType() {
        return paramType;
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

    /**
     * 
     * Setter for Parameter Type Property
     * @param paramType
     */
    public void setParamType(Integer paramType) {
        this.paramType = paramType;
    }

}
