package com.nucleus.regional.metadata;

import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.nucleus.core.annotations.Synonym;
import com.nucleus.entity.BaseEntity;

@Entity
@DynamicUpdate
@DynamicInsert
@Cacheable
@Table(name = "REGIONAL_META_DATA")
@NamedQuery(name = "getRegionalMetaDataForSourceEntity", query = "Select regionalMetaData from RegionalMetaData regionalMetaData where regionalMetaData.fullyQualifiedEntityName = :fullyQualifiedEntityName")
@Synonym(grant="ALL")
public class RegionalMetaData extends BaseEntity {

    @Transient
    private static final long serialVersionUID = 1L;

    private String fieldName;
    private String logicalName;
    private String dataType;
    private String fullyQualifiedEntityName;
    private Boolean isInnerEntity;
    private String referencedEntityName;
    @Column(length = 4000)
    private String validationQuery;
    private String validatorBeanName;

    public String getValidatorBeanName() {
        return validatorBeanName;
    }

    public void setValidatorBeanName(String validatorBeanName) {
        this.validatorBeanName = validatorBeanName;
    }

    public Boolean getIsInnerEntity() {
        return isInnerEntity;
    }

    public String getReferencedEntityName() {
        return referencedEntityName;
    }

    public void setReferencedEntityName(String referencedEntityName) {
        this.referencedEntityName = referencedEntityName;
    }

    public String getValidationQuery() {
        return validationQuery;
    }

    public void setValidationQuery(String validationQuery) {
        this.validationQuery = validationQuery;
    }

    public void setIsInnerEntity(Boolean isInnerEntity) {
        this.isInnerEntity = isInnerEntity;
    }

    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    public String getLogicalName() {
        return logicalName;
    }

    public void setLogicalName(String logicalName) {
        this.logicalName = logicalName;
    }

    public String getDataType() {
        return dataType;
    }

    public void setDataType(String dataType) {
        this.dataType = dataType;
    }

    public String getFullyQualifiedEntityName() {
        return fullyQualifiedEntityName;
    }

    public void setFullyQualifiedEntityName(String fullyQualifiedEntityName) {
        this.fullyQualifiedEntityName = fullyQualifiedEntityName;
    }

}
