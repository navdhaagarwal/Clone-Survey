package com.nucleus.finnone.pro.communicationgenerator.domainobject;

import static com.nucleus.finnone.pro.communicationgenerator.constants.CommunicationGeneratorConstants.STRING_LENGTH_FOUR_THOUSAND;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;
import javax.persistence.OneToOne;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.annotations.Type;
import org.joda.time.DateTime;

import com.nucleus.entity.BaseEntity;
import com.nucleus.finnone.pro.additionaldata.domainobject.AdditionalData;
import com.nucleus.finnone.pro.general.util.ValidatorUtils;
import com.nucleus.rules.model.SourceProduct;

@MappedSuperclass
public class CommunicationRequestBase extends BaseEntity {

    /**
	 * 
	 */
    private static final long serialVersionUID = 1L;
    public static final Character INITIATED = Character.valueOf('I');
    public static final Character COMPLETED = Character.valueOf('C');
    public static final Character FAILED = Character.valueOf('F');
    public static final Character PROCESSED = Character.valueOf('P');

    @Column(name = "COMMUNICATION_CODE")
    private String communicationCode;

    private String subjectURI;

    private String applicablePrimaryEntityURI;

    private Long applicablePrimaryEntityId;

    private Long subjectId;

    @Column(name = "SUBJECT_REFERENCE_NUMBER")
    private String subjectReferenceNumber;
    
    @Column(name = "SUBJECT_REFERENCE_TYPE")
    private String subjectReferenceType;

    private Character status;

    private String communicationEventCode;

    @Column(name = "COMMUNICATION_TEMPLATE_ID")
    private Long communicationTemplateId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "COMMUNICATION_TEMPLATE_ID", insertable = false, updatable = false)
    private CommunicationTemplate communicationTemplate;

    private String regenerationReasonCode;

    private Character issueReissueFlag;

    @Column(length = STRING_LENGTH_FOUR_THOUSAND)
    private String communicationText;

    private String phoneNumber;

    private String alternatePhoneNumber;

    private String primaryEmailAddress;

    private String bccEmailAddress;

    private String ccEmailAddress;

    @OneToOne(fetch=FetchType.LAZY)
    @JoinColumn(name = "ADDL_FIELD_TXN_ID", insertable = false, updatable = false)
    private AdditionalData additionalData;

    @Column(name = "ADDL_FIELD_TXN_ID")
    private Long additionalFieldTxnId;

    private Integer retriedAttemptsDone = 0;

    private String communicationTemplateCode;

    @ManyToOne(fetch= FetchType.LAZY)
    @JoinColumn(name = "SOURCE_PRODUCT_ID")
    private SourceProduct sourceProduct;

    private Date processDate;

    @Column
    @Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
    private DateTime eventLogTimeStamp;

    private Date referenceDate;

    @Column(name = "REQUEST_REFERENCE_ID")
    private String requestReferenceId;

    private Boolean generateMergedFile;

    
    private String schedularInstanceId;
    
    @Column(name="UNIQUE_REQUEST_ID")
    private String uniqueRequestId;
    
    private String barcodeReferenceNumber;

	@Column(name = "JSON_ADDITIONAL_FIELD1", length = STRING_LENGTH_FOUR_THOUSAND)
	private String jsonAdditionalField1;

	@Column(name = "JSON_ADDITIONAL_FIELD2", length = STRING_LENGTH_FOUR_THOUSAND)
	private String jsonAdditionalField2;

	@Column(name = "JSON_ADDITIONAL_FIELD3", length = STRING_LENGTH_FOUR_THOUSAND)
	private String jsonAdditionalField3;
	
	@Column(name = "LETTER_STORAGE_ID")
	private String letterStorageId;
	
	@Column(name = "EVENT_REQUEST_LOG_ID")
	private String eventRequestLogId;

	
	@Column(name = "ATTACHMENT_NAME")
	private String attachmentName;
	
	private Boolean previewFlag;
	
	private Long deliveryPriority = 0L;
	
	private String requestType;
	
	//@Column(name = "ATTACHMENT_FILE_PATHS", length = STRING_LENGTH_THREE_HUNDRED)
	//remove this transient field to support the attachment of generated pdfs in scheduler based communication.
	private transient String attachmentFilePaths;
    
    public String getUniqueRequestId() {
		return uniqueRequestId;
	}

	public void setUniqueRequestId(String uniqueRequestId) {
		this.uniqueRequestId = uniqueRequestId;
	}
    

	public Long getDeliveryPriority() {
		return deliveryPriority;
	}

	public void setDeliveryPriority(Long deliveryPriority) {
		this.deliveryPriority = deliveryPriority;
	}
	
    public String getSchedularInstanceId() {
		return schedularInstanceId;
	}

	public void setSchedularInstanceId(String schedularInstanceId) {
		this.schedularInstanceId = schedularInstanceId;
	}

	public AdditionalData getAdditionalData() {
        return additionalData;
    }

    public void setAdditionalData(AdditionalData additionalData) {
        this.additionalData = additionalData;
    }

    public Long getCommunicationTemplateId() {
        return communicationTemplateId;
    }

    public void setCommunicationTemplateId(Long communicationTemplateId) {
        this.communicationTemplateId = communicationTemplateId;
    }

    public String getCommunicationCode() {
        return communicationCode;
    }

    public void setCommunicationCode(String communicationCode) {
        this.communicationCode = communicationCode;
    }

    public String getSubjectURI() {
        return subjectURI;
    }

    public void setSubjectURI(String subjectURI) {
        this.subjectURI = subjectURI;
    }

    public Character getStatus() {
        return status;
    }

    public void setStatus(Character status) {
        this.status = status;
    }

    public Character getIssueReissueFlag() {
        return issueReissueFlag;
    }

    public void setIssueReissueFlag(Character issueReissueFlag) {
        this.issueReissueFlag = issueReissueFlag;
    }

    public String getCommunicationText() {
        return communicationText;
    }

    public void setCommunicationText(String communicationText) {
        this.communicationText = communicationText;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getAlternatePhoneNumber() {
        return alternatePhoneNumber;
    }

    public void setAlternatePhoneNumber(String alternatePhoneNumber) {
        this.alternatePhoneNumber = alternatePhoneNumber;
    }

    public String getPrimaryEmailAddress() {
        return primaryEmailAddress;
    }

    public void setPrimaryEmailAddress(String primaryEmailAddress) {
        this.primaryEmailAddress = primaryEmailAddress;
    }

    public String getBccEmailAddress() {
        return bccEmailAddress;
    }

    public void setBccEmailAddress(String bccEmailAddress) {
        this.bccEmailAddress = bccEmailAddress;
    }

    public String getCcEmailAddress() {
        return ccEmailAddress;
    }

    public void setCcEmailAddress(String ccEmailAddress) {
        this.ccEmailAddress = ccEmailAddress;
    }

    /**
     * @return the retriedAttemptsDone
     */
    public Integer getRetriedAttemptsDone() {
        return retriedAttemptsDone;
    }

    /**
     * @param retriedAttemptsDone
     *            the retriedAttemptsDone to set
     */
    public void setRetriedAttemptsDone(Integer retriedAttemptsDone) {
        if (retriedAttemptsDone == null) {
            this.retriedAttemptsDone = 0;
        }
        this.retriedAttemptsDone = retriedAttemptsDone;
    }

    /**
     * @return the communicationEventCode
     */
    public String getCommunicationEventCode() {
        return communicationEventCode;
    }

    /**
     * @param communicationEventCode
     *            the communicationEventCode to set
     */
    public void setCommunicationEventCode(String communicationEventCode) {
        this.communicationEventCode = communicationEventCode;
    }

    /**
     * @return the communicationTemplateCode
     */
    public String getCommunicationTemplateCode() {
        return communicationTemplateCode;
    }

    /**
     * @param communicationTemplateCode
     *            the communicationTemplateCode to set
     */
    public void setCommunicationTemplateCode(String communicationTemplateCode) {
        this.communicationTemplateCode = communicationTemplateCode;
    }

    public String getApplicablePrimaryEntityURI() {
        return applicablePrimaryEntityURI;
    }

    public void setApplicablePrimaryEntityURI(String applicablePrimaryEntityURI) {
        this.applicablePrimaryEntityURI = applicablePrimaryEntityURI;
    }

    public String getRegenerationReasonCode() {
        return regenerationReasonCode;
    }

    public void setRegenerationReasonCode(String regenerationReasonCode) {
        this.regenerationReasonCode = regenerationReasonCode;
    }

    public SourceProduct getSourceProduct() {
        return sourceProduct;
    }

    public void setSourceProduct(SourceProduct sourceProduct) {
        this.sourceProduct = sourceProduct;
    }

    public Long getApplicablePrimaryEntityId() {
        return applicablePrimaryEntityId;
    }

    public void setApplicablePrimaryEntityId(Long applicablePrimaryEntityId) {
        this.applicablePrimaryEntityId = applicablePrimaryEntityId;
    }

    public Long getSubjectId() {
        return subjectId;
    }

    public void setSubjectId(Long subjectId) {
        this.subjectId = subjectId;
    }

    public Long getAdditionalFieldTxnId() {
        return additionalFieldTxnId;
    }

    public void setAdditionalFieldTxnId(Long additionalFieldTxnId) {
        this.additionalFieldTxnId = additionalFieldTxnId;
    }

    public Date getProcessDate() {
        return processDate;
    }

    public void setProcessDate(Date processDate) {
        this.processDate = processDate;
    }

    public CommunicationTemplate getCommunicationTemplate() {
        return communicationTemplate;
    }

    public void setCommunicationTemplate(
            CommunicationTemplate communicationTemplate) {
        this.communicationTemplate = communicationTemplate;
    }

    public String getSubjectReferenceNumber() {
        return subjectReferenceNumber;
    }

    public void setSubjectReferenceNumber(String subjectReferenceNumber) {
        this.subjectReferenceNumber = subjectReferenceNumber;
    }
    
    
    

	public String getSubjectReferenceType() {
		return subjectReferenceType;
	}

	public void setSubjectReferenceType(String subjectReferenceType) {
		this.subjectReferenceType = subjectReferenceType;
	}

	public DateTime getEventLogTimeStamp() {
        return eventLogTimeStamp;
    }

    public void setEventLogTimeStamp(DateTime eventLogTimeStamp) {
        this.eventLogTimeStamp = eventLogTimeStamp;
    }

    public Date getReferenceDate() {
        return referenceDate;
    }

    public void setReferenceDate(Date referenceDate) {
        this.referenceDate = referenceDate;
    }

    public String getRequestReferenceId() {
        return requestReferenceId;
    }

    public void setRequestReferenceId(String requestReferenceId) {
        this.requestReferenceId = requestReferenceId;
    }

    public Boolean getGenerateMergedFile() {
        if (ValidatorUtils.isNull(this.generateMergedFile)) {
            generateMergedFile = false;
        }
        return generateMergedFile;
    }

    public void setGenerateMergedFile(Boolean generateMergedFile) {
        this.generateMergedFile = generateMergedFile;
    }

    public String getPrimaryAppOrSubjectUri()
    {
    	if(this.applicablePrimaryEntityURI!=null)
    	{
    		return this.applicablePrimaryEntityURI;
    	}
    	return this.subjectURI;
    }

	public String getJsonAdditionalField1() {
		return jsonAdditionalField1;
	}

	public void setJsonAdditionalField1(String jsonAdditionalField1) {
		this.jsonAdditionalField1 = jsonAdditionalField1;
	}

	public String getJsonAdditionalField2() {
		return jsonAdditionalField2;
	}

	public void setJsonAdditionalField2(String jsonAdditionalField2) {
		this.jsonAdditionalField2 = jsonAdditionalField2;
	}

	public String getJsonAdditionalField3() {
		return jsonAdditionalField3;
	}

	public void setJsonAdditionalField3(String jsonAdditionalField3) {
		this.jsonAdditionalField3 = jsonAdditionalField3;
	}
	
	public String getLetterStorageId() {
		return letterStorageId;
	}

	public void setLetterStorageId(String letterStorageId) {
		this.letterStorageId = letterStorageId;
	}

	public String getAttachmentFilePaths() {
		return attachmentFilePaths;
	}

	public void setAttachmentFilePaths(String attachmentFilePaths) {
		this.attachmentFilePaths = attachmentFilePaths;
	}
	
	public String getEventRequestLogId() {
		return eventRequestLogId;
	}

	public void setEventRequestLogId(String eventRequestLogId) {
		this.eventRequestLogId = eventRequestLogId;
	}

	public String getAdditionalJsonData() {
	     return	StringUtils.join(this.getJsonAdditionalField1(), this.getJsonAdditionalField2(), this.getJsonAdditionalField3());
	}

	public String getBarcodeReferenceNumber() {
		return barcodeReferenceNumber;
	}

	public void setBarcodeReferenceNumber(String barcodeReferenceNumber) {
		this.barcodeReferenceNumber = barcodeReferenceNumber;
	}

	public String getAttachmentName() {
		return attachmentName;
	}

	public void setAttachmentName(String attachmentName) {
		this.attachmentName = attachmentName;
	}

	public Boolean getPreviewFlag() {
		return previewFlag;
	}

	public void setPreviewFlag(Boolean previewFlag) {
		this.previewFlag = previewFlag;
	}

	public String getRequestType() {
		return requestType;
	}

	public void setRequestType(String requestType) {
		this.requestType = requestType;
	}
	
}
