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

import java.util.Set;

import javax.persistence.*;

import com.nucleus.master.audit.annotation.EmbedInAuditAsValue;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.Type;
import org.joda.time.DateTime;

import com.nucleus.core.annotations.Synonym;
import com.nucleus.entity.BaseEntity;
import com.nucleus.entity.CloneOptions;

/**
 * @author Nucleus Software Exports Limited
 */
@Entity
@DynamicUpdate
@DynamicInsert
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@Synonym(grant="ALL")
@Table(indexes={@Index(name="commm_fk_idx_attchdDocuments",columnList="communication_fk"),
                @Index(name = "document_upload_filename_index", columnList = "uploadedFileName")
                })
public class Document extends BaseEntity {

    private static final long           serialVersionUID = 7589954340868292136L;

    @EmbedInAuditAsValue(displayKey="label.letter.file.name")
    private String                      uploadedFileName;

    private String                      documentStoreId;

    private String                      coversheetStoreId;

    private String                      contentType;

    private int                         fileSizeInBytes;

    private Integer                     coversheetFileSizeInBytes;

    private boolean                     requiredFlag;

    private boolean                     deletedFlag;

    private boolean                     receivedFlag;

    private boolean                     uploadedFlag;

    private boolean                     generatedFlag;

    private boolean                     emailFlag;

    private boolean                     printFlag;

    private boolean                     faxFlag;

    private boolean                     origReceivedFlag;

    private boolean                     correctFlag;

    private boolean                     coverSheetFlag;

    private Integer                     pagesIncludingCoverSheet;

    @Column
    @Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
    private DateTime                    receivedOnDateTime;

    private Integer                     majorVersion;

    private Integer                     minorVersion;

    private String                      dataStoreRefKey;

    @ManyToOne(fetch=FetchType.LAZY)
    private DocumentDefinition          documentDefinition;

    /*JoinTable name given to keep table name under 30 chars for oracle*/
    @ManyToMany
    @JoinTable(name = "JT_DOCUMENT_PARAM_VAL_MAPPING")
    private Set<DocumentParameterValue> documentParameterValues;

    @Transient
    private Set<String> fontMetaData;

    public String getUploadedFileName() {
        return uploadedFileName;
    }

    public void setUploadedFileName(String uploadedFileName) {
        this.uploadedFileName = uploadedFileName;
    }

    public String getDocumentStoreId() {
        return documentStoreId;
    }

    public void setDocumentStoreId(String documentStoreId) {
        this.documentStoreId = documentStoreId;
    }

    public String getCoversheetStoreId() {
        return coversheetStoreId;
    }

    public void setCoversheetStoreId(String coversheetStoreId) {
        this.coversheetStoreId = coversheetStoreId;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public int getFileSizeInBytes() {
        return fileSizeInBytes;
    }

    public void setFileSizeInBytes(int fileSizeInBytes) {
        this.fileSizeInBytes = fileSizeInBytes;
    }

    public Integer getCoversheetFileSizeInBytes() {
        return coversheetFileSizeInBytes;
    }

    public void setCoversheetFileSizeInBytes(Integer coversheetFileSizeInBytes) {
        this.coversheetFileSizeInBytes = coversheetFileSizeInBytes;
    }

    public boolean isRequiredFlag() {
        return requiredFlag;
    }

    public void setRequiredFlag(boolean requiredFlag) {
        this.requiredFlag = requiredFlag;
    }

    public boolean isDeletedFlag() {
        return deletedFlag;
    }

    public void setDeletedFlag(boolean deletedFlag) {
        this.deletedFlag = deletedFlag;
    }

    public boolean isReceivedFlag() {
        return receivedFlag;
    }

    public void setReceivedFlag(boolean receivedFlag) {
        this.receivedFlag = receivedFlag;
    }

    public boolean isUploadedFlag() {
        return uploadedFlag;
    }

    public void setUploadedFlag(boolean uploadedFlag) {
        this.uploadedFlag = uploadedFlag;
    }

    public boolean isGeneratedFlag() {
        return generatedFlag;
    }

    public void setGeneratedFlag(boolean generatedFlag) {
        this.generatedFlag = generatedFlag;
    }

    public boolean isEmailFlag() {
        return emailFlag;
    }

    public void setEmailFlag(boolean emailFlag) {
        this.emailFlag = emailFlag;
    }

    public boolean isPrintFlag() {
        return printFlag;
    }

    public void setPrintFlag(boolean printFlag) {
        this.printFlag = printFlag;
    }

    public boolean isFaxFlag() {
        return faxFlag;
    }

    public void setFaxFlag(boolean faxFlag) {
        this.faxFlag = faxFlag;
    }

    public boolean isOrigReceivedFlag() {
        return origReceivedFlag;
    }

    public void setOrigReceivedFlag(boolean origReceivedFlag) {
        this.origReceivedFlag = origReceivedFlag;
    }

    public boolean isCorrectFlag() {
        return correctFlag;
    }

    public void setCorrectFlag(boolean correctFlag) {
        this.correctFlag = correctFlag;
    }

    public boolean isCoverSheetFlag() {
        return coverSheetFlag;
    }

    public void setCoverSheetFlag(boolean coverSheetFlag) {
        this.coverSheetFlag = coverSheetFlag;
    }

    public Integer getPagesIncludingCoverSheet() {
        return pagesIncludingCoverSheet;
    }

    public void setPagesIncludingCoverSheet(Integer pagesIncludingCoverSheet) {
        this.pagesIncludingCoverSheet = pagesIncludingCoverSheet;
    }

    public DateTime getReceivedOnDateTime() {
        return receivedOnDateTime;
    }

    public void setReceivedOnDateTime(DateTime receivedOnDateTime) {
        this.receivedOnDateTime = receivedOnDateTime;
    }

    public Integer getMajorVersion() {
        return majorVersion;
    }

    public void setMajorVersion(Integer majorVersion) {
        this.majorVersion = majorVersion;
    }

    public Integer getMinorVersion() {
        return minorVersion;
    }

    public void setMinorVersion(Integer minorVersion) {
        this.minorVersion = minorVersion;
    }

    public DocumentDefinition getDocumentDefinition() {
        return documentDefinition;
    }

    public void setDocumentDefinition(DocumentDefinition documentDefinition) {
        this.documentDefinition = documentDefinition;
    }

    public Set<DocumentParameterValue> getDocumentParameterValues() {
        return documentParameterValues;
    }

    public void setDocumentParameterValues(Set<DocumentParameterValue> documentParameterValues) {
        this.documentParameterValues = documentParameterValues;
    }

    public String getDataStoreRefKey() {
        return dataStoreRefKey;
    }

    public void setDataStoreRefKey(String dataStoreRefKey) {
        this.dataStoreRefKey = dataStoreRefKey;
    }

    public Set<String> getFontMetaData() {
        return fontMetaData;
    }

    public void setFontMetaData(Set<String> fontMetaData) {
        this.fontMetaData = fontMetaData;
    }

    @Override
    protected void populate(BaseEntity baseEntity, CloneOptions cloneOptions) {
        Document document = (Document) baseEntity;
        super.populate(document, cloneOptions);
        document.setContentType(contentType);
        document.setCorrectFlag(correctFlag);
        document.setCoversheetFileSizeInBytes(coversheetFileSizeInBytes);
        document.setCoverSheetFlag(coverSheetFlag);
        document.setCoversheetStoreId(coversheetStoreId);
        document.setDataStoreRefKey(dataStoreRefKey);
        document.setDeletedFlag(deletedFlag);
        document.setDocumentDefinition(documentDefinition);
        document.setDocumentParameterValues(documentParameterValues);
        document.setDocumentStoreId(documentStoreId);
        document.setEmailFlag(emailFlag);
        document.setFaxFlag(faxFlag);
        document.setFileSizeInBytes(fileSizeInBytes);
        document.setGeneratedFlag(generatedFlag);
        document.setMajorVersion(majorVersion);
        document.setMinorVersion(minorVersion);
        document.setOrigReceivedFlag(origReceivedFlag);
        document.setPagesIncludingCoverSheet(pagesIncludingCoverSheet);
        document.setPrintFlag(printFlag);
        document.setReceivedFlag(origReceivedFlag);
        document.setReceivedOnDateTime(receivedOnDateTime);
        document.setRequiredFlag(requiredFlag);
        document.setUploadedFileName(uploadedFileName);
        document.setUploadedFlag(uploadedFlag);
    }

    @Override
    protected void populateFrom(BaseEntity baseEntity, CloneOptions cloneOptions) {
        Document document = (Document) baseEntity;
        super.populateFrom(document, cloneOptions);
        this.setContentType(document.getContentType());
        this.setCorrectFlag(document.isCorrectFlag());
        this.setCoversheetFileSizeInBytes(document.getCoversheetFileSizeInBytes());
        this.setCoverSheetFlag(document.isCoverSheetFlag());
        this.setCoversheetStoreId(document.getCoversheetStoreId());
        this.setDataStoreRefKey(document.getDataStoreRefKey());
        this.setDeletedFlag(document.isDeletedFlag());
        this.setDocumentDefinition(document.getDocumentDefinition());
        this.setDocumentParameterValues(document.getDocumentParameterValues());
        this.setDocumentStoreId(document.getDocumentStoreId());
        this.setEmailFlag(document.isEmailFlag());
        this.setFaxFlag(document.isFaxFlag());
        this.setFileSizeInBytes(document.getFileSizeInBytes());
        this.setGeneratedFlag(document.isGeneratedFlag());
        this.setMajorVersion(document.getMajorVersion());
        this.setMinorVersion(document.getMinorVersion());
        this.setOrigReceivedFlag(document.isOrigReceivedFlag());
        this.setPagesIncludingCoverSheet(document.getPagesIncludingCoverSheet());
        this.setPrintFlag(document.isPrintFlag());
        this.setReceivedFlag(document.isOrigReceivedFlag());
        this.setReceivedOnDateTime(document.getReceivedOnDateTime());
        this.setRequiredFlag(document.isReceivedFlag());
        this.setUploadedFileName(document.getUploadedFileName());
        this.setUploadedFlag(document.isUploadedFlag());
    }

}
