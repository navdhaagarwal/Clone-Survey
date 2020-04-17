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
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Index;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import com.nucleus.master.audit.annotation.EmbedInAuditAsReference;
import com.nucleus.master.audit.annotation.EmbedInAuditAsValue;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.nucleus.core.annotations.Synonym;
import com.nucleus.entity.BaseEntity;
import com.nucleus.entity.CloneOptions;

/**
 * The Class DocumentChecklistDefinition.
 *
 * @author Nucleus Software Exports Limited
 */
@Entity
@DynamicUpdate
@DynamicInsert
@Cacheable
@Synonym(grant="ALL")
@Table(indexes={@Index(name="document_checklist_fk_index",columnList="document_checklist_fk")})
public class DocumentChecklistDefinition extends BaseEntity {

    /** The Constant serialVersionUID. */
    private static final long          serialVersionUID = 5788926723916294791L;

    /** The document classification type. */
    @ManyToOne
    @EmbedInAuditAsReference(displayKey="key.documentClassificationType")
    private DocumentClassificationType classificationType;

    /** The document. */
    @ManyToOne
    @EmbedInAuditAsReference(displayKey="key.documentDescription",columnToDisplay = "description")
    private DocumentDefinition         document;

    /** The document source. */
    @EmbedInAuditAsValue(displayKey="key.documentSource")
    private String                     source;

    /** The verification required. */
    // As per discussion with Ankur : This field must be moved to document definition. This field is not be considered for
    // validations in document checklist controller.
    // Leaving it as it is currently to avoid failure of doccheklistdefinition master jsp load filure.
    @EmbedInAuditAsValue(displayKey="label.document.verificationRequired")
    private boolean                    verificationRequired;

    /** The original required. */
    @EmbedInAuditAsValue(displayKey="label.document.originalRequired")
    private boolean                    originalRequired;

    /** The mandatory. */
    @EmbedInAuditAsValue(displayKey="label.document.ismandatory")
    private boolean                    mandatory;

    /** The order. */
    @Column(name = "orderNumber")
    @EmbedInAuditAsValue(displayKey="key.order")
    private Integer                    order;

    private Boolean                     scanMandatory;

    /**
     * Gets the classification type.
     *
     * @return the classificationType
     */
    public DocumentClassificationType getClassificationType() {
        return classificationType;
    }

    /**
     * Sets the classification type.
     *
     * @param classificationType the classificationType to set
     */
    public void setClassificationType(DocumentClassificationType classificationType) {
        this.classificationType = classificationType;
    }

    /**
     * Gets the document.
     *
     * @return the document
     */
    public DocumentDefinition getDocument() {
        return document;
    }

    /**
     * Sets the document.
     *
     * @param document the document to set
     */
    public void setDocument(DocumentDefinition document) {
        this.document = document;
    }

    /**
     * Gets the source.
     *
     * @return the source
     */
    public String getSource() {
        return source;
    }

    /**
     * Sets the source.
     *
     * @param source the source to set
     */
    public void setSource(String source) {
        this.source = source;
    }

    /**
     * Checks if is verification required.
     *
     * @return the verificationRequired
     */
    public boolean getVerificationRequired() {
        return verificationRequired;
    }

    /**
     * Sets the verification required.
     *
     * @param verificationRequired the verificationRequired to set
     */
    public void setVerificationRequired(boolean verificationRequired) {
        this.verificationRequired = verificationRequired;
    }

    /**
     * Checks if is original required.
     *
     * @return the originalRequired
     */
    public boolean getOriginalRequired() {
        return originalRequired;
    }

    /**
     * Sets the original required.
     *
     * @param originalRequired the originalRequired to set
     */
    public void setOriginalRequired(boolean originalRequired) {
        this.originalRequired = originalRequired;
    }

    /**
     * Checks if is mandatory.
     *
     * @return the mandatory
     */
    public boolean getMandatory() {
        return mandatory;
    }

    /**
     * Sets the mandatory.
     *
     * @param mandatory the mandatory to set
     */
    public void setMandatory(boolean mandatory) {
        this.mandatory = mandatory;
    }

    /**
     * Gets the order.
     *
     * @return the order
     */
    public Integer getOrder() {
        return order;
    }

    /**
     * Sets the order.
     *
     * @param order the new order
     */
    public void setOrder(Integer order) {
        this.order = order;
    }


    public Boolean getScanMandatory() {
        return scanMandatory;
    }

    public void setScanMandatory(Boolean scanMandatory) {
        this.scanMandatory = scanMandatory;
    }

    @Override
    protected void populate(BaseEntity baseEntity, CloneOptions cloneOptions) {
        DocumentChecklistDefinition documentChecklistDefinition = (DocumentChecklistDefinition) baseEntity;
        super.populate(documentChecklistDefinition, cloneOptions);
        documentChecklistDefinition.setClassificationType(classificationType);
        documentChecklistDefinition.setDocument(document);
        documentChecklistDefinition.setMandatory(mandatory);
        documentChecklistDefinition.setOriginalRequired(originalRequired);
        documentChecklistDefinition.setSource(source);
        documentChecklistDefinition.setVerificationRequired(verificationRequired);
        documentChecklistDefinition.setOrder(order);
        documentChecklistDefinition.setScanMandatory(scanMandatory);
    }

    @Override
    protected void populateFrom(BaseEntity baseEntity, CloneOptions cloneOptions) {
        DocumentChecklistDefinition documentChecklistDefinition = (DocumentChecklistDefinition) baseEntity;
        super.populateFrom(documentChecklistDefinition, cloneOptions);
        this.setClassificationType(documentChecklistDefinition.getClassificationType());
        this.setDocument(documentChecklistDefinition.getDocument());
        this.setMandatory(documentChecklistDefinition.getMandatory());
        this.setOriginalRequired(documentChecklistDefinition.getOriginalRequired());
        this.setSource(documentChecklistDefinition.getSource());
        this.setVerificationRequired(documentChecklistDefinition.getVerificationRequired());
        this.setOrder(documentChecklistDefinition.getOrder());
        this.setScanMandatory(documentChecklistDefinition.getScanMandatory());
    }

}
