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
import javax.persistence.Transient;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.nucleus.core.annotations.Synonym;
import com.nucleus.master.BaseMasterEntity;

/**
 * @author Nucleus Software Exports Limited
 */
@Entity
@DynamicUpdate
@DynamicInsert
@Inheritance(strategy = InheritanceType.JOINED)
@Cacheable
@Synonym(grant="SELECT")
public class DocumentList extends BaseMasterEntity{

    private static final long serialVersionUID = -5906212309235851528L;

    @ManyToOne(fetch = FetchType.EAGER)
    private DocumentClassificationType        documentClassificationType;
    
    @ManyToOne(fetch = FetchType.EAGER)
    private DocumentDefinition                documentDescription;
    
    private Boolean                           verificationRequired=Boolean.FALSE;
    
  private Boolean                           mandatory=Boolean.FALSE;
    
    private Boolean                           original=Boolean.FALSE;
    
    private String                            documentSource;
    
    @Transient
    private int index;

    public DocumentClassificationType getDocumentClassificationType() {
        return documentClassificationType;
    }

    public void setDocumentClassificationType(DocumentClassificationType documentClassificationType) {
        this.documentClassificationType = documentClassificationType;
    }

    public DocumentDefinition getDocumentDescription() {
        return documentDescription;
    }

    public void setDocumentDescription(DocumentDefinition documentDescription) {
        this.documentDescription = documentDescription;
    }

    public boolean isVerificationRequired() {
        return verificationRequired;
    }

    public void setVerificationRequired(boolean verificationRequired) {
        this.verificationRequired = verificationRequired;
    }



    public boolean isOriginal() {
        return original;
    }

    public void setOriginal(boolean original) {
        this.original = original;
    }

    public String getDocumentSource() {
        return documentSource;
    }

    public void setDocumentSource(String documentSource) {
        this.documentSource = documentSource;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public boolean isMandatory() {
        return mandatory;
    }

    public void setMandatory(boolean mandatory) {
        this.mandatory = mandatory;
    }

    

    
}
