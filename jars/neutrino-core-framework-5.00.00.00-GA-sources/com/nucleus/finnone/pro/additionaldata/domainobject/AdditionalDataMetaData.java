/**
@author merajul.ansari
Creation Date: 29/01/2013
Copyright: Nucleus Software Exports Ltd
Description: Entity for Additional Field Configuration
Program Specs Referred: 
----------------------------------------------------------------------------------------------------------------
Revision:  Version	Last Revision Date	 	Name		Function / Module affected       Modifications Done
----------------------------------------------------------------------------------------------------------------	
	       1.0		29/01/2013				Merajul Hasan Ansari 	initial version      
----------------------------------------------------------------------------------------------------------------
 *
*/
package com.nucleus.finnone.pro.additionaldata.domainobject;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.nucleus.core.annotations.Synonym;
import com.nucleus.entity.BaseEntity;
import com.nucleus.entity.CloneOptions;
import com.nucleus.master.BaseMasterEntity;
@Entity 
@DynamicInsert 
@DynamicUpdate
@Table(name = "COM_ADDL_FIELDS_CONFIG_MST")

@NamedQueries({
@NamedQuery(name="getAdditionalDataMetaDataByTransactionType",query="select additionalDataMetaData from AdditionalDataMetaData additionalDataMetaData where additionalDataMetaData.activeFlag=true and additionalDataMetaData.masterLifeCycleData.approvalStatus in(:approvalStatus) And additionalDataMetaData.additionalDataTransactionType.serviceRequestTransactionTypeCode=:transactionType order by additionalDataMetaData.displayOrder,additionalDataMetaData.id"),
@NamedQuery(name="getAdditionalDataByTransactionTypeForDisplayOrder",query="select additionalDataMetaData from AdditionalDataMetaData additionalDataMetaData where additionalDataMetaData.masterLifeCycleData.approvalStatus in(:approvalStatus) And additionalDataMetaData.additionalDataTransactionType.id=:transactionTypeId order by additionalDataMetaData.displayOrder,additionalDataMetaData.id"),
@NamedQuery(name="getAdditionalDataMetaDataByTransactionTypeID",query="select additionalDataMetaData from AdditionalDataMetaData additionalDataMetaData where additionalDataMetaData.masterLifeCycleData.approvalStatus in(:approvalStatus) And additionalDataMetaData.additionalDataTransactionType.id=:transactionTypeId order by additionalDataMetaData.displayOrder")
})
@Synonym(grant="SELECT")
public class AdditionalDataMetaData  extends BaseMasterEntity implements Serializable {
	
	@Transient
	private static final long serialVersionUID = 1L;
	
	@Column(name="FIELD_NAME")
	private String fieldName;
	
	@Column(name="DATA_TYPE")
	private Long dataType; //A	Alphanumeric,	N	Amount (Numeric),	D	Date,	I	Integer,	F	Floating Number
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "DATA_TYPE", referencedColumnName = "id", updatable = false,insertable=false)
	private CustomFieldDataType customFieldDataTypeObject;

	
	@Column(name="FIELD_LABEL_ID")
	private String fieldLabelId;
	
	@Column(name="FIELD_LABEL")
	private String fieldLabel;
	
	@Column(name = "FIELD_LENGTH")
	private Integer length;
	
	@Column(name = "FIELD_PRECISION")
	private Integer precision;
	
	@Column(name = "FIELD_MANDATORY_FLAG")
	private Character mandatory;
	
	@Column(name="FIELD_TOOLTIP")
	private String fieldToolTip;
	
	@Column(name="FIELD_TOOLTIP_ID")
	private String fieldToolTipId;
	
	@Column(name="FIELD_PLACEHOLDER_TEXT_ID")
	private String fieldPlaceHolderId;
	
	@Column(name="FIELD_PLACEHOLDER_TEXT")
	private String fieldPlaceHolder;
	
	@Column(name = "LIST_OF")
	private String listOf;
	
	@Column(name = "LIST_FIELD")
	private String listField;
	
	@Column(name = "FIELD_DISPLAY_ORDER")
	private Integer displayOrder;
	
	@Column(name = "FIELD_VALIDATION_RULEID")
	private Long validationRuleId;
	
	@Column(name="MAPPING_FIELD")
	private String mappingField;
	
	@Transient
	private String roundingMethod="RO";
	
	@Column(name="TRANSACTION_TYPE_ID")
	private Long transactionTypeId;
	
	public Long getTransactionTypeId() {
		return transactionTypeId;
	}

	public void setTransactionTypeId(Long transactionTypeId) {
		this.transactionTypeId = transactionTypeId;
	}

	@ManyToOne
	@JoinColumn(name = "TRANSACTION_TYPE_ID", referencedColumnName="ID", updatable = false,insertable=false)
	private ServiceRequestTransactionType additionalDataTransactionType;

	public String getListField() {
		return listField;
	}

	public void setListField(String listField) {
		this.listField = listField;
	}

	public String getListOf() {
		return listOf;
	}

	public void setListOf(String listOf) {
		this.listOf = listOf;
	}

	/** 
	 * @return the displayOrder
	 */
	public Integer getDisplayOrder() {
		// begin-user-code
		return displayOrder;
		// end-user-code
	}

	/** 
	 * @return the fieldLabelId
	 */
	public String getFieldLabelId() {
		// begin-user-code
		return fieldLabelId;
		// end-user-code
	}

	/** 
	 * @return the fieldName
	 */
	public String getFieldName() {
		// begin-user-code
		return fieldName;
		// end-user-code
	}

	/** 
	 * @return the fieldPlaceHolder
	 * @generated "UML to Java V5.0 (com.ibm.xtools.transform.uml2.java5.internal.UML2JavaTransform)"
	 */
	public String getFieldPlaceHolder() {
		// begin-user-code
		return fieldPlaceHolder;
		// end-user-code
	}

	/** 
	 * @return the fieldToolTip
	 * @generated "UML to Java V5.0 (com.ibm.xtools.transform.uml2.java5.internal.UML2JavaTransform)"
	 */
	public String getFieldToolTip() {
		// begin-user-code
		return fieldToolTip;
		// end-user-code
	}

	/** 
	 * @return the id
	 */
	/*public Long getId() {
		// begin-user-code
		return id;
		// end-user-code
	}*/

	/** 
	 * @return the length
	 */
	public Integer getLength() {
		// begin-user-code
		return length;
		// end-user-code
	}

	
	/** 
	 * @return the mandatory
	 */
	public Character getMandatory() {
		// begin-user-code
		return mandatory;
		// end-user-code
	}
	
	public String getMandatoryDescription() {
		if (mandatory == null) {
			return "No";
		}
		return mandatory.equals("Y") ? "Yes" : "No";
		
		// begin-user-code
		//return mandatory;
		// end-user-code
	}

	/** 
	 * @return the precision
	 */
	public Integer getPrecision() {
		// begin-user-code
		return precision;
		// end-user-code
	}

	/** 
	 * @return the roundingMethod
	 * @generated "UML to Java V5.0 (com.ibm.xtools.transform.uml2.java5.internal.UML2JavaTransform)"
	 */
	public String getRoundingMethod() {
		// begin-user-code
		return roundingMethod;
		// end-user-code
	}

	/** 
	 * @return the tenantId
	 * @generated "UML to Java V5.0 (com.ibm.xtools.transform.uml2.java5.internal.UML2JavaTransform)"
	 */
	/*public Long getTenantId() {
		// begin-user-code
		return tenantId;
		// end-user-code
	}*/

	/** 
	 * @return the validationRuleId
	 */
	public Long getValidationRuleId() {
		// begin-user-code
		return validationRuleId;
		// end-user-code
	}



	public ServiceRequestTransactionType getAdditionalDataTransactionType() {
		return additionalDataTransactionType;
	}

	public void setAdditionalDataTransactionType(
			ServiceRequestTransactionType additionalDataTransactionType) {
		this.additionalDataTransactionType = additionalDataTransactionType;
	}

	/** 
	 * @param theDisplayOrder the displayOrder to set
	 */
	public void setDisplayOrder(Integer theDisplayOrder) {
		// begin-user-code
		displayOrder = theDisplayOrder;
		// end-user-code
	}

	/** 
	 * @param theFieldLabelId the fieldLabelId to set
	 */
	public void setFieldLabelId(String theFieldLabelId) {
		// begin-user-code
		fieldLabelId = theFieldLabelId;
		// end-user-code
	}

	/** 
	 * @param theFieldName the fieldName to set
	 */
	public void setFieldName(String theFieldName) {
		// begin-user-code
		fieldName = theFieldName;
		// end-user-code
	}

	/** 
	 * @param theFieldPlaceHolder the fieldPlaceHolder to set
	 */
	public void setFieldPlaceHolder(String theFieldPlaceHolder) {
		// begin-user-code
		fieldPlaceHolder = theFieldPlaceHolder;
		// end-user-code
	}

	/** 
	 * @param theFieldToolTip the fieldToolTip to set
	 * @generated "UML to Java V5.0 (com.ibm.xtools.transform.uml2.java5.internal.UML2JavaTransform)"
	 */
	public void setFieldToolTip(String theFieldToolTip) {
		// begin-user-code
		fieldToolTip = theFieldToolTip;
		// end-user-code
	}

	/** 
	 * @param theId the id to set
	 */
	/*public void setId(Long theId) {
		// begin-user-code
		id = theId;
		// end-user-code
	}*/

	/** 
	 * @param theLength the length to set
	 */
	public void setLength(Integer theLength) {
		// begin-user-code
		length = theLength;
		// end-user-code
	}

	/** 
	 * @param theMandatory the mandatory to set
	 */
	public void setMandatory(Character theMandatory) {
		// begin-user-code
		mandatory = theMandatory;
		// end-user-code
	}

	/** 
	 * @param thePrecision the precision to set
	 */
	public void setPrecision(Integer thePrecision) {
		// begin-user-code
		precision = thePrecision;
		// end-user-code
	}

	/** 
	 * @param theRoundingMethod the roundingMethod to set
	 * @generated "UML to Java V5.0 (com.ibm.xtools.transform.uml2.java5.internal.UML2JavaTransform)"
	 */
	public void setRoundingMethod(String theRoundingMethod) {
		// begin-user-code
		roundingMethod = theRoundingMethod;
		// end-user-code
	}

	/** 
	 * @param theTenantId the tenantId to set
	 * @generated "UML to Java V5.0 (com.ibm.xtools.transform.uml2.java5.internal.UML2JavaTransform)"
	 */
	/*public void setTenantId(Long theTenantId) {
		// begin-user-code
		tenantId = theTenantId;
		// end-user-code
	}*/
	
	public String getFieldLabel() {
		return fieldLabel;
	}

	public void setFieldLabel(String fieldLabel) {
		this.fieldLabel = fieldLabel;
	}

	public String getFieldToolTipId() {
		return fieldToolTipId;
	}

	public void setFieldToolTipId(String fieldToolTipId) {
		this.fieldToolTipId = fieldToolTipId;
	}

	public String getFieldPlaceHolderId() {
		return fieldPlaceHolderId;
	}

	public void setFieldPlaceHolderId(String fieldPlaceHolderId) {
		this.fieldPlaceHolderId = fieldPlaceHolderId;
	}

	public String getMappingField() {
		return mappingField;
	}

	public void setMappingField(String mappingField) {
		this.mappingField = mappingField;
	}

	/** 
	 * @param theValidationRuleId the validationRuleId to set
	 */
	public void setValidationRuleId(Long theValidationRuleId) {
		// begin-user-code
		validationRuleId = theValidationRuleId;
		// end-user-code
	}
	/**
	 * @return the dataType
	 */
	public Long getDataType() {
		return dataType;
	}

	/**
	 * @param dataType the dataType to set
	 */
	public void setDataType(Long dataType) {
		this.dataType = dataType;
	}

	/**
	 * @return the customFieldDataTypeObject
	 */
	public CustomFieldDataType getCustomFieldDataTypeObject() {
		return customFieldDataTypeObject;
	}

	/**
	 * @param customFieldDataTypeObject the customFieldDataTypeObject to set
	 */
	public void setCustomFieldDataTypeObject(
			CustomFieldDataType customFieldDataTypeObject) {
		this.customFieldDataTypeObject = customFieldDataTypeObject;
	}
	
	protected void populate(BaseEntity baseEntity, CloneOptions cloneOptions){
		AdditionalDataMetaData additionalDataMetaData = (AdditionalDataMetaData)baseEntity;
	    super.populate(additionalDataMetaData, cloneOptions);
	    additionalDataMetaData.setAdditionalDataTransactionType(this.additionalDataTransactionType);
	    additionalDataMetaData.setTransactionTypeId(this.transactionTypeId);
	    additionalDataMetaData.setDataType(this.dataType);
	    additionalDataMetaData.setDisplayOrder(this.displayOrder);
	    additionalDataMetaData.setFieldLabel(this.fieldLabel);
	    additionalDataMetaData.setFieldLabelId(this.fieldLabelId);
	    additionalDataMetaData.setFieldName(this.fieldName);
	    additionalDataMetaData.setFieldPlaceHolder(this.fieldPlaceHolder);
	    additionalDataMetaData.setFieldPlaceHolderId(this.fieldPlaceHolderId);
	    additionalDataMetaData.setFieldToolTip(this.fieldToolTip);
	    additionalDataMetaData.setFieldToolTipId(this.fieldToolTipId);
	    additionalDataMetaData.setLength(this.length);
	    additionalDataMetaData.setMandatory(this.mandatory);
	    additionalDataMetaData.setMappingField(this.mappingField);
	    additionalDataMetaData.setPrecision(this.precision);
	    additionalDataMetaData.setCustomFieldDataTypeObject(this.customFieldDataTypeObject);
	   // additionalDataMetaData.setTenantId(this.getTenantId());
	    additionalDataMetaData.setMakeBusinessDate(this.getMakeBusinessDate());
	  }

	protected void populateFrom(BaseEntity baseEntity, CloneOptions cloneOptions){
		AdditionalDataMetaData additionalDataMetaData = (AdditionalDataMetaData)baseEntity;
	    super.populateFrom(additionalDataMetaData, cloneOptions);
	    setAdditionalDataTransactionType(additionalDataMetaData.getAdditionalDataTransactionType());
	    setDataType(additionalDataMetaData.getDataType());
	    setTransactionTypeId(additionalDataMetaData.getTransactionTypeId());
	    setCustomFieldDataTypeObject(additionalDataMetaData.getCustomFieldDataTypeObject());
	    setDisplayOrder(additionalDataMetaData.getDisplayOrder());
	    setFieldLabel(additionalDataMetaData.getFieldLabel());
	    setFieldLabelId(additionalDataMetaData.getFieldLabelId());
		setFieldName(additionalDataMetaData.getFieldName());
		setFieldPlaceHolder(additionalDataMetaData.getFieldPlaceHolder());
		setFieldPlaceHolderId(additionalDataMetaData.getFieldPlaceHolderId());
		setFieldToolTip(additionalDataMetaData.getFieldToolTip());
		setFieldToolTipId(additionalDataMetaData.getFieldToolTipId());
		setLength(additionalDataMetaData.getLength());
		setMandatory(additionalDataMetaData.getMandatory());
		setMappingField(additionalDataMetaData.getMappingField());
		setPrecision(additionalDataMetaData.getPrecision());
		//setTenantId(additionalDataMetaData.getTenantId());
		setMakeBusinessDate(additionalDataMetaData.getMakeBusinessDate());
	}
	public String getDisplayName(){
		return "Custom Data";
	}
}