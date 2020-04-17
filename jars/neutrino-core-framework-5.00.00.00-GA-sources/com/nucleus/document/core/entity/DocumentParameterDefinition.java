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
package com.nucleus.document.core.entity;

import javax.persistence.Cacheable;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.ManyToOne;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.nucleus.core.annotations.Synonym;
import com.nucleus.entity.BaseEntity;
import com.nucleus.entity.CloneOptions;

/**
 * @author Nucleus Software Exports Limited
 */
@Entity
@DynamicUpdate
@DynamicInsert
@Cacheable
@Inheritance(strategy = InheritanceType.JOINED)
@Synonym(grant="ALL")
public class DocumentParameterDefinition extends BaseEntity {

    private static final long     serialVersionUID = 7589954340868292136L;

    private String                parameterCode;

    private String                name;

    private String                description;

    @ManyToOne(fetch = FetchType.EAGER)
    private DocumentParameterType type;

    private boolean               mandatory;

    private String                validationRegex;

    private Integer               length;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public DocumentParameterType getType() {
        return type;
    }

    public void setType(DocumentParameterType type) {
        this.type = type;
    }

    public boolean isMandatory() {
        return mandatory;
    }

    public void setMandatory(boolean mandatory) {
        this.mandatory = mandatory;
    }

    public String getValidationRegex() {
        return validationRegex;
    }

    public void setValidationRegex(String validationRegex) {
        this.validationRegex = validationRegex;
    }

    public String getParameterCode() {
        return parameterCode;
    }

    public void setParameterCode(String parameterCode) {
        this.parameterCode = parameterCode;
    }

    public Integer getLength() {
        return length;
    }

    public void setLength(Integer length) {
        this.length = length;
    }
    
    protected void populate(BaseEntity baseEntity, CloneOptions cloneOptions) {
        DocumentParameterDefinition documentParameterDefinition = (DocumentParameterDefinition) baseEntity;
        super.populate(documentParameterDefinition, cloneOptions);
        documentParameterDefinition.setDescription(description);
        documentParameterDefinition.setLength(length);
        documentParameterDefinition.setMandatory(mandatory);
        documentParameterDefinition.setName(name);
        documentParameterDefinition.setParameterCode(parameterCode);
        documentParameterDefinition.setType(type);
        documentParameterDefinition.setValidationRegex(validationRegex);
    }
    
    
    @Override
    protected void populateFrom(BaseEntity baseEntity, CloneOptions cloneOptions) {
        DocumentParameterDefinition documentParameterDefinition = (DocumentParameterDefinition) baseEntity;
        super.populateFrom(documentParameterDefinition, cloneOptions);
        this.setDescription(documentParameterDefinition.getDescription());
        this.setLength(documentParameterDefinition.getLength());
        this.setMandatory(documentParameterDefinition.isMandatory());
        this.setName(documentParameterDefinition.getName());
        this.setParameterCode(documentParameterDefinition.getParameterCode());
        this.setType(documentParameterDefinition.getType());
        this.setValidationRegex(documentParameterDefinition.getValidationRegex());
                
                
    }

}
