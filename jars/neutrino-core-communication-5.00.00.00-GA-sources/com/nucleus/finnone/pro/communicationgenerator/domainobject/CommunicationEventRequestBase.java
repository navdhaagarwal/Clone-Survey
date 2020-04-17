package com.nucleus.finnone.pro.communicationgenerator.domainobject;

import static com.nucleus.finnone.pro.communicationgenerator.constants.CommunicationGeneratorConstants.STRING_LENGTH_FOUR_THOUSAND;

import java.util.Date;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;
import javax.persistence.OneToOne;

import com.nucleus.entity.BaseEntity;
import com.nucleus.finnone.pro.additionaldata.domainobject.AdditionalData;
import com.nucleus.finnone.pro.general.util.ValidatorUtils;
import com.nucleus.rules.model.SourceProduct;

@MappedSuperclass
public class CommunicationEventRequestBase extends BaseEntity {
   	/**
	 * 
	 */
    private static final long serialVersionUID = 1L;

    @Column(name = "COMMUNICATION_CODE")
    private String communicationCode;
    
    @Column(name = "EVENT_CODE")
    private String eventCode;
    private String subjectURI;
    private String subjectReferenceNumber;
    
	private String subjectReferenceType;

    @Column(name = "JSON_ADDITIONAL_FIELD1", length = STRING_LENGTH_FOUR_THOUSAND)
    private String jsonAdditionalField1;
    
   	@Column(name = "JSON_ADDITIONAL_FIELD2",length = STRING_LENGTH_FOUR_THOUSAND)
    private String jsonAdditionalField2;
    
    @Column(name = "JSON_ADDITIONAL_FIELD3",length = STRING_LENGTH_FOUR_THOUSAND)
    private String jsonAdditionalField3;
    
    @OneToOne(cascade = CascadeType.PERSIST)
    @JoinColumn(name = "ADDL_FIELD_TXN_ID")
    private AdditionalData additionalData;
    private Character status;

    @ManyToOne
    @JoinColumn(name = "SOURCE_PRODUCT_ID")
    private SourceProduct sourceProduct;
    private String applicablePrimaryEntityURI;

    private Date referenceDate;

    private String requestReferenceId;

    private Boolean generateMergedFile;
    
	private String eventRequestLogId;

	private Boolean previewFlag;
	
	private Long deliveryPriority;
	
	private String requestType;
    /**
     * @return the eventCode
     */

    public String getEventCode() {
        return eventCode;
    }

    /**
     * @param eventCode
     *            the eventCode to set
     */
    public void setEventCode(String eventCode) {
        this.eventCode = eventCode;
    }

    public Long getDeliveryPriority() {
		return deliveryPriority;
	}

	public void setDeliveryPriority(Long priority) {
		this.deliveryPriority = priority;
	}
	
    /**
     * @return the subjectURI
     */
    public String getSubjectURI() {
        return subjectURI;
    }

    /**
     * @param subjectURI
     *            the subjectURI to set
     */

    public void setSubjectURI(String subjectURI) {
        this.subjectURI = subjectURI;
    }

    /**
     * @return the additionalData
     */
    public AdditionalData getAdditionalData() {
        return additionalData;
    }

    /**
     * @param additionalData
     *            the additionalData to set
     */
    public void setAdditionalData(AdditionalData additionalData) {
        this.additionalData = additionalData;
    }

    /**
     * @return the status
     */
    public Character getStatus() {
        return status;
    }

    /**
     * @param status
     *            the status to set
     */
    public void setStatus(Character status) {
        this.status = status;
    }

    /**
     * @return the module
     */
    public SourceProduct getSourceProduct() {
        return sourceProduct;
    }

    /**
     * @param sourceProduct
     *            the module to set
     */
    public void setSourceProduct(SourceProduct sourceProduct) {
        this.sourceProduct = sourceProduct;
    }

    /**
     * @return the applicablePrimaryEntityURI
     */

    public String getApplicablePrimaryEntityURI() {
        return applicablePrimaryEntityURI;
    }

    /**
     * @param applicablePrimaryEntityURI
     *            the applicablePrimaryEntityURI to set
     */
    public void setApplicablePrimaryEntityURI(String applicablePrimaryEntityURI) {
        this.applicablePrimaryEntityURI = applicablePrimaryEntityURI;
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


    public String getCommunicationCode() {
        return communicationCode;
    }

    public void setCommunicationCode(String communicationCode) {
        this.communicationCode = communicationCode;
    }

    public Date getReferenceDate() {
        Date dateTobeReturned = null;
        if (ValidatorUtils.notNull(this.referenceDate)) {
            dateTobeReturned = new Date(this.referenceDate.getTime());
        }
        return dateTobeReturned;
    }

    public void setReferenceDate(Date referenceDate) {
        if (ValidatorUtils.notNull(referenceDate)) {
            this.referenceDate = new Date(referenceDate.getTime());
        }

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

	public String getEventRequestLogId() {
		return eventRequestLogId;
	}

	public void setEventRequestLogId(String eventRequestLogId) {
		this.eventRequestLogId = eventRequestLogId;
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
