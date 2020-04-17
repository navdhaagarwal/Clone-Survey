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

import java.util.ArrayList;
import java.util.List;

import javax.persistence.*;

import com.nucleus.activeInactiveReason.ReasonsActiveInactiveMapping;
import com.nucleus.cas.parentChildDeletionHandling.DeletionPreValidator;
import com.nucleus.master.audit.annotation.EmbedInAuditAsValue;
import com.nucleus.master.audit.annotation.EmbedInAuditAsValueObject;
import com.nucleus.master.audit.annotation.NeutrinoAuditableMaster;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.nucleus.core.annotations.Synonym;
import com.nucleus.core.system.util.SystemPropertyUtils;
import com.nucleus.entity.BaseEntity;
import com.nucleus.entity.CloneOptions;
import com.nucleus.master.BaseMasterEntity;

/**
 * The Class DocumentChecklist.
 *
 * @author Nucleus Software Exports Limited
 */
@Entity
@DynamicUpdate
@DynamicInsert
@DeletionPreValidator
@Cacheable
@Synonym(grant="SELECT")
@NeutrinoAuditableMaster(identifierColumn="name")
@Table(indexes={@Index(name="RAIM_PERF_45_4090",columnList="REASON_ACT_INACT_MAP")})
public class DocumentChecklist extends BaseMasterEntity {

    /** The Constant serialVersionUID. */
    private static final long         serialVersionUID = -9163380973503309306L;

    /** The name. */
    @EmbedInAuditAsValue(displayKey="label.document.checklistName")
    private String                    name;

    /** The description. */
    @EmbedInAuditAsValue(displayKey="label.document.checklistDescription")
    private String                    description;

    /** The documents. */
    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "document_checklist_fk")
    @Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL)
    @EmbedInAuditAsValueObject
    List<DocumentChecklistDefinition> documents;

    /**
     * The document count
     */
    private int                       documentCount;

    @OneToOne(cascade = CascadeType.ALL)
    @EmbedInAuditAsValueObject
    private ReasonsActiveInactiveMapping reasonActInactMap;

    /**
     * Gets the name.
     *
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name.
     *
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gets the description.
     *
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the description.
     *
     * @param description the description to set
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Gets the documents.
     *
     * @return the documents
     */
    public List<DocumentChecklistDefinition> getDocuments() {
        return documents;
    }

    /**
     * Sets the documents.
     *
     * @param documents the documents to set
     */
    public void setDocuments(List<DocumentChecklistDefinition> documents) {
        this.documents = documents;
    }

    /**
     * Gets the document count
     * 
     * @return the document count
     */
    public int getDocumentCount() {
        if (documents.size() > 0) {

            return documentCount = documents.size();
        } else {

            return documentCount = 0;
        }
    }

    /**
     * Sets the document count
     * 
     * @param documentCount
     */
    public void setDocumentCount(int documentCount) {
        this.documentCount = documentCount;
    }

    public ReasonsActiveInactiveMapping getReasonActInactMap() {
        return reasonActInactMap;
    }

    public void setReasonActInactMap(ReasonsActiveInactiveMapping reasonActInactMap) {
        this.reasonActInactMap = reasonActInactMap;
    }

    @Override
    public String getDisplayName() {
        return name;
    }

    @Override
    protected void populate(BaseEntity baseEntity, CloneOptions cloneOptions) {
        DocumentChecklist documentChecklist = (DocumentChecklist) baseEntity;
        super.populate(documentChecklist, cloneOptions);
        documentChecklist.setName(name);
        documentChecklist.setDescription(description);
        if (documents != null && documents.size() > 0) {
            List<DocumentChecklistDefinition> cloneDocuments = new ArrayList<DocumentChecklistDefinition>();
            for (DocumentChecklistDefinition documentChecklistDefinition : documents) {
                cloneDocuments.add((DocumentChecklistDefinition) documentChecklistDefinition.cloneYourself(cloneOptions));
            }
            documentChecklist.setDocuments(cloneDocuments);
            documentChecklist.setDocumentCount(cloneDocuments.size());
        }
        if (reasonActInactMap != null) {
            documentChecklist.setReasonActInactMap((ReasonsActiveInactiveMapping) this.reasonActInactMap.cloneYourself(cloneOptions));
        }

    }

    @Override
    protected void populateFrom(BaseEntity baseEntity, CloneOptions cloneOptions) {
        DocumentChecklist documentChecklist = (DocumentChecklist) baseEntity;
        super.populateFrom(documentChecklist, cloneOptions);
        this.setName(documentChecklist.getName());
        this.setDescription(documentChecklist.getDescription());
        if (documentChecklist.getDocuments() != null && documentChecklist.getDocuments().size() > 0) {
            this.getDocuments().clear();
            for (DocumentChecklistDefinition documentChecklistDefinition : documentChecklist.getDocuments()) {
                this.getDocuments().add(
                        (DocumentChecklistDefinition) documentChecklistDefinition.cloneYourself(cloneOptions));
            }
        }
        if (documentChecklist.getReasonActInactMap() != null) {
            this.setReasonActInactMap((ReasonsActiveInactiveMapping) documentChecklist.getReasonActInactMap().cloneYourself(cloneOptions));
        }
    }

    public String getLogInfo() {
        String log = null;
        StringBuffer stf = new StringBuffer();
        stf.append("Document Checklist Master received to be saved ------------>");
        stf.append(SystemPropertyUtils.getNewline());
        stf.append("Document Checklist Name :" + name);
        stf.append(SystemPropertyUtils.getNewline());
        stf.append("Document Checklist Description  :" + description);

        log = stf.toString();
        return log;
    }
}
