package com.nucleus.finnone.pro.additionaldata.domainobject;

import java.util.Date;

import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.nucleus.core.annotations.Synonym;
import com.nucleus.entity.BaseEntity;
import com.nucleus.finnone.pro.additionaldata.constants.AdditionalDataConstants;
import com.nucleus.finnone.pro.additionaldata.constants.CustomFieldFor;

@Entity 
@DynamicInsert 
@DynamicUpdate
@Table(name="COM_ADDL_SR_TXN_MST")
@Cacheable
@NamedQueries({
@NamedQuery
(	name="getAdditionalDataTransactionType", 
	query="select serviceRequestTransactionType from ServiceRequestTransactionType serviceRequestTransactionType where serviceRequestTransactionType.serviceRequestTransactionTypeId=:transactionTypeId order by serviceRequestTransactionType.serviceRequestTransactionTypeName"),
@NamedQuery(
		name="getTransactionTypeDetailsTest",
		query="select t from ServiceRequestTransactionType t order by t.serviceRequestTransactionTypeDescription "
		)
})
@Synonym(grant="ALL")
public class ServiceRequestTransactionType extends BaseEntity
{
  private static final long serialVersionUID = 1L;

  @Column(name = "ACTIVE_FLAG", columnDefinition = "Numeric(1,0)")
  private int activeFlag;
  
  @Column(name = "REVIEWED_BY_URI")
  private String reviewedBy;
	
  @Column(name = "REVIEWED_TIME_STAMP", length = AdditionalDataConstants.ADDL_DATA_STRING_LENGTH_SIX,nullable=true)
  private Date reviewedtimestamp;
  
  @Column(name = "APPROVAL_STATUS", columnDefinition = "Numeric(10)")
  private Integer approvalStatus;
  
  @Column(name="SR_TXN_CODE")
  private String serviceRequestTransactionTypeCode;

  @Column(name="SR_TXN_DESC")
  private String serviceRequestTransactionTypeDescription;

  @Column(name="SR_TXN_NAME")
  private String serviceRequestTransactionTypeName;

  @Column(name="SR_TXN_TYPEID", columnDefinition="Numeric(19,0)")
  private Long serviceRequestTransactionTypeId;

  @ManyToOne
  @JoinColumn(name="SR_TXN_TYPEID", columnDefinition="Numeric(19,0)", insertable=false, updatable=false, referencedColumnName="ID")
  private CustomFieldFor customFieldFor;
  
  public String getReviewedBy() {
	return reviewedBy;
}

public void setReviewedBy(String reviewedBy) {
	this.reviewedBy = reviewedBy;
}

public Date getReviewedtimestamp() {
	return reviewedtimestamp;
}

public void setReviewedtimestamp(Date reviewedtimestamp) {
	this.reviewedtimestamp = reviewedtimestamp;
}

public Integer getApprovalStatus() {
	return approvalStatus;
}

public void setApprovalStatus(Integer approvalStatus) {
	this.approvalStatus = approvalStatus;
}

public int getActiveFlag() {
	return activeFlag;
  }

  public void setActiveFlag(int activeFlag) {
	this.activeFlag = activeFlag;
  }

  public String getServiceRequestTransactionTypeCode()
  {
    return this.serviceRequestTransactionTypeCode;
  }

  public void setServiceRequestTransactionTypeCode(String serviceRequestTransactionTypeCode)
  {
    this.serviceRequestTransactionTypeCode = serviceRequestTransactionTypeCode;
  }

  public String getServiceRequestTransactionTypeDescription() {
    return this.serviceRequestTransactionTypeDescription;
  }

  public void setServiceRequestTransactionTypeDescription(String serviceRequestTransactionTypeDescription)
  {
    this.serviceRequestTransactionTypeDescription = serviceRequestTransactionTypeDescription;
  }

  public String getServiceRequestTransactionTypeName() {
    return this.serviceRequestTransactionTypeName;
  }

  public void setServiceRequestTransactionTypeName(String serviceRequestTransactionTypeName)
  {
    this.serviceRequestTransactionTypeName = serviceRequestTransactionTypeName;
  }

  public Long getServiceRequestTransactionTypeId() {
    return this.serviceRequestTransactionTypeId;
  }

  public void setServiceRequestTransactionTypeId(Long serviceRequestTransactionTypeId)
  {
    this.serviceRequestTransactionTypeId = serviceRequestTransactionTypeId;
  }


  public CustomFieldFor getCustomFieldFor() {
	return customFieldFor;
}

public void setCustomFieldFor(CustomFieldFor customFieldFor) {
	this.customFieldFor = customFieldFor;
}

public boolean isTransactionLogged()
  {
    return (super.getId() != null);
  }
}