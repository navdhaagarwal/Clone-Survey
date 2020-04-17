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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.*;

import com.nucleus.activeInactiveReason.ReasonsActiveInactiveMapping;
import com.nucleus.cas.parentChildDeletionHandling.DeletionPreValidator;
import org.apache.commons.collections.CollectionUtils;
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
 * @author Nucleus Software Exports Limited
 */
@Entity
@DynamicUpdate
@DynamicInsert
@DeletionPreValidator
@Cacheable
@Inheritance(strategy = InheritanceType.JOINED)
@Synonym(grant="ALL")
@Table(indexes={@Index(name="RAIM_PERF_45_4394",columnList="REASON_ACT_INACT_MAP"),
        @Index(name="name_index",columnList="name")})
public class DocumentDefinition extends BaseMasterEntity {

    private static final long                 serialVersionUID = 7589954340868292136L;

    private String                            code;

    private String                            name;

    private String                            description;

    @ManyToOne(fetch = FetchType.EAGER)
    private DocumentType                      documentType;

    @ManyToOne(fetch = FetchType.EAGER)
    private DocumentClassificationType        documentClassificationType;

    @ManyToMany(cascade = CascadeType.PERSIST)
    @JoinTable(name = "jt_group_document_mapping", joinColumns = @JoinColumn(name = "parent_doc_definition_fk"), inverseJoinColumns = @JoinColumn(name = "documentId"))
    @Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL)
    private Set<DocumentDefinition>           groupedDocuments;

    private boolean                           validityRequired;

    private Integer                           warningDays;

    private boolean                           verificationRequired;

    private String                            referenceImage;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = false)
    @JoinColumn(name = "document_definition_fk")
    @Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL)
    private List<DocumentParameterDefinition> documentParameterDefinitions;

    @Transient
    private Long[]                            documentIds;

    private boolean                            imageMandatory;

    @OneToOne(cascade = CascadeType.ALL)
    private ReasonsActiveInactiveMapping reasonActInactMap;

    @ManyToMany(fetch = FetchType.LAZY,cascade = CascadeType.ALL)
    @JoinTable(name = "DOCUMENT_DOCMAPPING",joinColumns = {@JoinColumn(name="DOCDEFINITION", referencedColumnName = "ID")},
            inverseJoinColumns = {@JoinColumn(name="DOCMAPPING", referencedColumnName = "ID")})
    private Set<DocumentMappingCode> mappingCode;

    @Transient
    private Long[]              mappingCodeIds;

    private Integer minimumImages;

    private Integer maximumImages;

    /* A new Flag for Document Approval is required on every document,
     * this flag should determine whether the document if marked as waived or deferred
     * would be sent in the new 'Document Approval Grid'*/
    private Boolean                            approvalRequiredInGrid = false;


    private String                quesIdsString;

    @Transient
    private String                questionIdString;

    @Transient
    private String                questionCode;

    @Transient
    private String                questionDescription;

    @Transient
    private long[]                questionIds;



    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Long[] getMappingCodeIds() {
        return mappingCodeIds;
    }

    public void setMappingCodeIds(Long[] mappingCodeIds) {
        this.mappingCodeIds = mappingCodeIds;
    }

    public Integer getMinimumImages() {
        return minimumImages;
    }

    public void setMinimumImages(Integer minimumImages) {
        this.minimumImages = minimumImages;
    }

    public Integer getMaximumImages() {
        return maximumImages;
    }

    public void setMaximumImages(Integer maximumImages) {
        this.maximumImages = maximumImages;
    }

    public Set<DocumentMappingCode> getMappingCode() {
        return mappingCode;
    }

    public void setMappingCode(Set<DocumentMappingCode> mappingCode) {
        this.mappingCode = mappingCode;
    }

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

    public List<DocumentParameterDefinition> getDocumentParameterDefinitions() {
        return documentParameterDefinitions;
    }

    public void setDocumentParameterDefinitions(List<DocumentParameterDefinition> documentParameterDefinitions) {
        this.documentParameterDefinitions = documentParameterDefinitions;
    }

    /**
     * Get the document type of the Document being defined. The possible values
     * are "Group Document" and "Individual Document"
     *
     * @return
     */
    public DocumentType getDocumentType() {
        return documentType;
    }

    /**
     * Set the document type of the Document being defined. The possible values
     * are "Group Document" and "Individual Document"
     */
    public void setDocumentType(DocumentType documentType) {
        this.documentType = documentType;
    }

    /**
     * Get the document classification .This field indicates what is the
     * Classification of the document defined. Possible values are General,
     * Legal, Security, other etc.
     *
     * @return
     */
    public DocumentClassificationType getDocumentClassificationType() {
        return documentClassificationType;
    }

    /**
     *
     * Set the document classification .This field indicates what is the
     * Classification of the document defined. Possible values are General,
     * Legal, Security, other etc.
     *
     * @param documentClassificationType
     */
    public void setDocumentClassificationType(DocumentClassificationType documentClassificationType) {
        this.documentClassificationType = documentClassificationType;
    }

    /**
     * List the Individual documents (DocumentDefinition instances where
     * DocumentType is Individual Document), this filed will only be set if
     * document type is GroupDocument
     *
     * @return
     */
    public Set<DocumentDefinition> getGroupedDocuments() {
        return groupedDocuments;
    }

    /**
     * Set the documents list grouped with the current document, this filed will
     * only be set if document type is GroupDocument
     *
     * @return
     */
    public void setGroupedDocuments(Set<DocumentDefinition> groupedDocuments) {
        this.groupedDocuments = groupedDocuments;
    }

    /**
     * Indicates if validity is required for the document being defined
     *
     * @return
     */
    public boolean isValidityRequired() {
        return validityRequired;
    }

    /**
     * Set the validityRequired field for the document being defined
     *
     * @param validityRequired
     */
    public void setValidityRequired(boolean validityRequired) {
        this.validityRequired = validityRequired;
    }

    /**
     * Get Warning days, the number of days before the Expiry/Validity date, for
     * notifying the system user.
     *
     * @return
     */
    public Integer getWarningDays() {
        return warningDays;
    }

    /**
     * Set the WarningDays
     *
     * @param warningDays
     */
    public void setWarningDays(Integer warningDays) {
        this.warningDays = warningDays;
    }

    /**
     * Returns if verification is required to be done for the document being
     * defined.
     *
     * @return
     */
    public boolean isVerificationRequired() {
        return verificationRequired;
    }

    /**
     * Set if verification is required to be done for the document being
     * defined.
     *
     * @param varificationRequired
     */
    public void setVerificationRequired(boolean varificationRequired) {
        this.verificationRequired = varificationRequired;
    }

    public String getReferenceImage() {
        return referenceImage;
    }

    public void setReferenceImage(String referenceImage) {
        this.referenceImage = referenceImage;
    }

    public Long[] getDocumentIds() {
        return documentIds;
    }

    public void setDocumentIds(Long[] documentIds) {
        this.documentIds = documentIds;
    }

    public boolean getImageMandatory() {
        return imageMandatory;
    }

    public void setImageMandatory(boolean imageMandatory) {
        this.imageMandatory = imageMandatory;
    }

    public ReasonsActiveInactiveMapping getReasonActInactMap() {
        return reasonActInactMap;
    }

    public void setReasonActInactMap(ReasonsActiveInactiveMapping reasonActInactMap) {
        this.reasonActInactMap = reasonActInactMap;
    }

    public Boolean getApprovalRequiredInGrid() {
        return approvalRequiredInGrid;
    }

    public void setApprovalRequiredInGrid(Boolean approvalRequiredInGrid) {
        this.approvalRequiredInGrid = approvalRequiredInGrid;
    }

    public String getQuesIdsString() {
        return quesIdsString;
    }

    public void setQuesIdsString(String quesIdsString) {
        this.quesIdsString = quesIdsString;
    }

    public String getQuestionIdString() {
        return questionIdString;
    }

    public void setQuestionIdString(String questionIdString) {
        this.questionIdString = questionIdString;
    }

    public String getQuestionCode() {
        return questionCode;
    }

    public void setQuestionCode(String questionCode) {
        this.questionCode = questionCode;
    }

    public String getQuestionDescription() {
        return questionDescription;
    }

    public void setQuestionDescription(String questionDescription) {
        this.questionDescription = questionDescription;
    }

    public long[] getQuestionIds() {
        return questionIds;
    }

    public void setQuestionIds(long[] questionIds) {
        this.questionIds = questionIds;
    }

    @Override
    protected void populate(BaseEntity baseEntity, CloneOptions cloneOptions) {
        DocumentDefinition documentDefinition = (DocumentDefinition) baseEntity;
        super.populate(documentDefinition, cloneOptions);
        documentDefinition.setDescription(description);
        documentDefinition.setDocumentClassificationType(documentClassificationType);
        documentDefinition.setDocumentType(documentType);
       /* documentDefinition.setGroupedDocuments((groupedDocuments != null && groupedDocuments.size() > 0) ? groupedDocuments
                : null);*/
        Set<DocumentDefinition> groupedDocumentsSet = this.groupedDocuments;
        if (CollectionUtils.isNotEmpty(groupedDocumentsSet)) {
            Set<DocumentDefinition> clonedGroupedDocumentsSet = new HashSet<DocumentDefinition>();
            for (DocumentDefinition groupedDocuments : groupedDocumentsSet) {
                clonedGroupedDocumentsSet.add(groupedDocuments);
            }
            documentDefinition.setGroupedDocuments(clonedGroupedDocumentsSet);
        }
        Set<DocumentMappingCode> documentMappingCodes = this.mappingCode;
        if(CollectionUtils.isNotEmpty(documentMappingCodes)){
            Set<DocumentMappingCode> documentMappingCodes1 = new HashSet<>();
            for (DocumentMappingCode mapingDoc : documentMappingCodes) {
                documentMappingCodes1.add(mapingDoc);
            }
            documentDefinition.setMappingCode(documentMappingCodes1);
        }
        documentDefinition.setName(name);
        documentDefinition.setValidityRequired(validityRequired);
        documentDefinition.setReferenceImage(referenceImage);
        documentDefinition.setVerificationRequired(verificationRequired);
        documentDefinition.setImageMandatory(imageMandatory);
        documentDefinition.setCode(code);
        documentDefinition.setApprovalRequiredInGrid(approvalRequiredInGrid);
        if (documentParameterDefinitions != null && documentParameterDefinitions.size() > 0) {
            List<DocumentParameterDefinition> cloneDocParamDef = new ArrayList<DocumentParameterDefinition>();
            for (DocumentParameterDefinition documentParameterDefinition : documentParameterDefinitions) {
                cloneDocParamDef.add((DocumentParameterDefinition) documentParameterDefinition.cloneYourself(cloneOptions));
            }
            documentDefinition.setDocumentParameterDefinitions(cloneDocParamDef);
        }
        if (reasonActInactMap != null) {
            documentDefinition.setReasonActInactMap((ReasonsActiveInactiveMapping) this.reasonActInactMap.cloneYourself(cloneOptions));
        }
        documentDefinition.setMappingCode(mappingCode);
        documentDefinition.setMinimumImages(minimumImages);
        documentDefinition.setMaximumImages(maximumImages);
        documentDefinition.setQuesIdsString(quesIdsString);
    }

    @Override
    protected void populateFrom(BaseEntity baseEntity, CloneOptions cloneOptions) {
        DocumentDefinition documentDefinition = (DocumentDefinition) baseEntity;
        super.populateFrom(documentDefinition, cloneOptions);
        this.setDescription(documentDefinition.getDescription());
        this.setDocumentClassificationType(documentDefinition.getDocumentClassificationType());
        this.setDocumentType(documentDefinition.getDocumentType());
        this.setCode(documentDefinition.getCode());
        this.setGroupedDocuments((documentDefinition.getGroupedDocuments() != null && documentDefinition
                .getGroupedDocuments().size() > 0) ? documentDefinition.getGroupedDocuments() : null);
		/*if (CollectionUtils.isNotEmpty(documentDefinition.getGroupedDocuments())) {
			this.groupedDocuments = new HashSet<DocumentDefinition>();
			for (DocumentDefinition groupDoc : documentDefinition.getGroupedDocuments()) {
				this.groupedDocuments
						.add(groupDoc != null ? (DocumentDefinition) groupDoc
								.cloneYourself(cloneOptions) : null);
			}
		}*/
        this.setName(documentDefinition.getName());
        this.setValidityRequired(documentDefinition.isValidityRequired());
        this.setReferenceImage(documentDefinition.getReferenceImage());
        this.setVerificationRequired(documentDefinition.isVerificationRequired());
        this.setImageMandatory(documentDefinition.getImageMandatory());
        this.getDocumentParameterDefinitions().clear();
        if (documentDefinition.getDocumentParameterDefinitions() != null
                && documentDefinition.getDocumentParameterDefinitions().size() > 0) {
            for (DocumentParameterDefinition documentParameterDefinition : documentDefinition
                    .getDocumentParameterDefinitions()) {
                this.getDocumentParameterDefinitions().add(
                        (DocumentParameterDefinition) documentParameterDefinition.cloneYourself(cloneOptions));
            }
        }
        if (documentDefinition.getReasonActInactMap() != null) {
            this.setReasonActInactMap((ReasonsActiveInactiveMapping) documentDefinition.getReasonActInactMap().cloneYourself(cloneOptions));
        }
        this.setMappingCode(documentDefinition.getMappingCode());
        this.setMinimumImages(documentDefinition.getMinimumImages());
        this.setMaximumImages(documentDefinition.getMaximumImages());
        this.setApprovalRequiredInGrid(documentDefinition.getApprovalRequiredInGrid());
        this.setQuesIdsString(documentDefinition.getQuesIdsString());

    }

    @Override
    public String getDisplayName() {
        return getName();

    }

    public String getLogInfo() {
        String log = null;
        StringBuffer stf = new StringBuffer();
        stf.append("Document Definition Master received to be saved ------------>");
        stf.append(SystemPropertyUtils.getNewline());
        stf.append("Document Name :" + name);
        stf.append(SystemPropertyUtils.getNewline());
        stf.append("Document Description :" + description);
        stf.append(SystemPropertyUtils.getNewline());

        if (documentClassificationType != null) {
            stf.append(" Document Classification Type : " + documentClassificationType.getId());
            stf.append(SystemPropertyUtils.getNewline());
        }
        if (documentType != null) {
            stf.append(" Document Type : " + documentType.getId());
            stf.append(SystemPropertyUtils.getNewline());
        }
        if (groupedDocuments != null && groupedDocuments.size() > 0) {
            stf.append(" Grouped document List Id's ------>");
            stf.append(SystemPropertyUtils.getNewline());
            for (DocumentDefinition document : groupedDocuments) {
                stf.append("Document Name :" + document.getName());
                stf.append(SystemPropertyUtils.getNewline());
                stf.append("Document Description :" + document.getDescription());
                stf.append(SystemPropertyUtils.getNewline());

                if (document.getDocumentClassificationType() != null) {
                    stf.append("Document Classification Type :" + documentClassificationType.getId());
                    stf.append(SystemPropertyUtils.getNewline());

                }
                if (document.getDocumentType() != null) {
                    stf.append(" Document Type  :" + documentType.getId());

                }
            }
        }

        if(quesIdsString!=null){
            stf.append("QuestionIdString :" +quesIdsString);
        }

        log = stf.toString();
        return log;
    }
}
